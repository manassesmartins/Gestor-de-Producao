package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

sealed class SyncConnectionState {
    object DISCONNECTED : SyncConnectionState()
    object CONNECTED : SyncConnectionState()
    data class ERROR(val message: String) : SyncConnectionState()
}

object LiveSyncManager {
    private const val TAG = "LiveSyncManager"
    private const val PREFS_NAME = "ms_live_sync_prefs"
    private const val KEY_GROUP_CODE = "group_code"
    private const val BROKER_URL = "ssl://4e359cf3052a4eec92d47310660c8207.s1.eu.hivemq.cloud:8883"
    private const val TOPIC_PREFIX = "gestor_producao/sync/"

    var activeGroupCode: String? = null
        private set

    val clientUuid: String = UUID.randomUUID().toString().take(8)
    var isApplyingRemoteUpdate = false

    private var mqttClient: MqttClient? = null
    private var repository: TransactionRepository? = null
    private var appContext: Context? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _connectionState = MutableStateFlow<SyncConnectionState>(SyncConnectionState.DISCONNECTED)
    val connectionState: StateFlow<SyncConnectionState> = _connectionState.asStateFlow()

    private var isConnecting = false
    private var shouldReconnect = false

    fun getStoredGroupCode(context: Context): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_GROUP_CODE, null)
    }

    fun saveGroupCode(context: Context, code: String?) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_GROUP_CODE, code)
            .apply()
        activeGroupCode = code
    }

    fun initializeStoredSync(context: Context, repo: TransactionRepository) {
        appContext = context.applicationContext
        repository = repo
        val storedCode = getStoredGroupCode(context)
        if (!storedCode.isNullOrBlank()) {
            activeGroupCode = storedCode
            scope.launch {
                connectMqtt(storedCode)
            }
        }
    }

    fun startSync(context: Context, code: String, repo: TransactionRepository) {
        appContext = context.applicationContext
        repository = repo
        saveGroupCode(context, code)
        activeGroupCode = code
        scope.launch {
            connectMqtt(code)
        }
    }

    fun stopSync(context: Context) {
        shouldReconnect = false
        scope.launch {
            disconnectMqtt()
        }
        saveGroupCode(context, null)
        activeGroupCode = null
        repository = null
        appContext = null
    }

    fun publishMutation(table: String, action: String, data: JSONObject) {
        val client = mqttClient
        if (client == null || !client.isConnected || activeGroupCode == null) return

        val payload = JSONObject().apply {
            put("type", "mutation")
            put("table", table)
            put("action", action)
            put("data", data)
        }
        publishJson(payload)
    }

    private suspend fun connectMqtt(code: String) {
        if (isConnecting) return
        isConnecting = true
        shouldReconnect = true
        try {
            var retryCount = 0
            while (shouldReconnect && retryCount < 30) {
                try {
                    withContext(Dispatchers.IO) {
                        doConnect(code)
                    }
                    return
                } catch (e: Exception) {
                    Log.w(TAG, "MQTT connection attempt $retryCount failed: ${e.message}")
                    retryCount++
                    _connectionState.value = SyncConnectionState.ERROR("Falha na conexão: ${e.message}")
                }
                delay(3000L)
            }
            _connectionState.value = SyncConnectionState.ERROR("Não foi possível conectar após $retryCount tentativas")
        } catch (e: Exception) {
            Log.e(TAG, "MQTT connection error", e)
            _connectionState.value = SyncConnectionState.ERROR(e.message ?: "Erro desconhecido")
        } finally {
            isConnecting = false
        }
    }

    private fun doConnect(code: String) {
        val clientId = "gestor-android-${clientUuid}-${System.currentTimeMillis() % 10000}"
        val client = MqttClient(BROKER_URL, clientId, MemoryPersistence())

        val options = MqttConnectOptions().apply {
            isAutomaticReconnect = true
            isCleanSession = true
            connectionTimeout = 10
            keepAliveInterval = 20
            userName = "hivemq.webclient.1782681074830"
            this.password = "SD!I0c?1R;,aW23Jbhfd".toCharArray()
        }

        client.setCallback(object : MqttCallback {
            override fun connectionLost(cause: Throwable?) {
                Log.w(TAG, "MQTT connection lost: ${cause?.message}")
                _connectionState.value = SyncConnectionState.ERROR("Conexão perdida: ${cause?.message ?: "desconhecido"}")
                mqttClient = null
                if (shouldReconnect) {
                    scope.launch {
                        delay(3000L)
                        activeGroupCode?.let { connectMqtt(it) }
                    }
                }
            }

            override fun messageArrived(topic: String, message: MqttMessage) {
                try {
                    val payloadStr = String(message.payload, Charsets.UTF_8)
                    val json = JSONObject(payloadStr)
                    scope.launch {
                        handleIncomingMessage(json)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error processing incoming MQTT message", e)
                }
            }

            override fun deliveryComplete(token: org.eclipse.paho.client.mqttv3.IMqttDeliveryToken) {
            }
        })

        client.connect(options)

        val topic = TOPIC_PREFIX + code
        client.subscribe(topic, 1)

        mqttClient = client
        _connectionState.value = SyncConnectionState.CONNECTED

        scope.launch {
            publishFullSync()
        }
    }

    private suspend fun disconnectMqtt() {
        try {
            mqttClient?.disconnect()
            mqttClient?.close()
        } catch (e: Exception) {
            Log.w(TAG, "Error disconnecting MQTT", e)
        }
        mqttClient = null
        _connectionState.value = SyncConnectionState.DISCONNECTED
    }

    private fun publishJson(payload: JSONObject) {
        val client = mqttClient ?: return
        val code = activeGroupCode ?: return
        if (!client.isConnected) return
        try {
            val topic = TOPIC_PREFIX + code
            val msg = MqttMessage(payload.toString().toByteArray(Charsets.UTF_8)).apply {
                qos = 1
                isRetained = false
            }
            client.publish(topic, msg)
        } catch (e: Exception) {
            Log.e(TAG, "Error publishing MQTT message", e)
        }
    }

    private suspend fun publishFullSync() {
        val repo = repository ?: return
        val client = mqttClient ?: return
        val code = activeGroupCode ?: return
        if (!client.isConnected) return

        val transactions = withContext(Dispatchers.IO) {
            val list = repo.allTransactions.first()
            JSONArray().apply {
                list.forEach { tx ->
                    put(JSONObject().apply {
                        put("id", tx.id)
                        put("description", tx.description)
                        put("amount", tx.amount)
                        put("type", tx.type)
                        put("category", tx.category)
                        put("dateString", tx.dateString)
                        put("timestamp", tx.timestamp)
                        put("extraText", tx.extraText)
                        put("week", tx.week)
                    })
                }
            }
        }

        val orders = withContext(Dispatchers.IO) {
            val list = repo.allOrders.first()
            JSONArray().apply {
                list.forEach { o ->
                    put(JSONObject().apply {
                        put("id", o.id)
                        put("clientName", o.clientName)
                        put("pantyType", o.pantyType)
                        put("pantySize", o.pantySize)
                        put("quantity", o.quantity)
                        put("pantyValue", o.pantyValue)
                        put("totalValue", o.totalValue)
                        put("week", o.week)
                        put("businessArea", o.businessArea)
                        put("status", o.status)
                        put("timestamp", o.timestamp)
                    })
                }
            }
        }

        val categories = withContext(Dispatchers.IO) {
            val list = repo.allCategories.first()
            JSONArray().apply {
                list.forEach { c ->
                    put(JSONObject().apply {
                        put("id", c.id)
                        put("name", c.name)
                        put("type", c.type)
                    })
                }
            }
        }

        val calculations = withContext(Dispatchers.IO) {
            val list = repo.allCalculations.first()
            JSONArray().apply {
                list.forEach { calc ->
                    put(JSONObject().apply {
                        put("id", calc.id)
                        put("pano", calc.pano)
                        if (calc.kg != null) put("kg", calc.kg)
                        if (calc.valorKg != null) put("valorKg", calc.valorKg)
                        if (calc.quantidade != null) put("quantidade", calc.quantidade)
                    })
                }
            }
        }

        val clients = withContext(Dispatchers.IO) {
            val list = repo.allClients.first()
            JSONArray().apply {
                list.forEach { cl ->
                    put(JSONObject().apply {
                        put("id", cl.id)
                        put("name", cl.name)
                        put("phone", cl.phone)
                    })
                }
            }
        }

        val models = withContext(Dispatchers.IO) {
            val list = repo.allProductModels.first()
            JSONArray().apply {
                list.forEach { m ->
                    put(JSONObject().apply {
                        put("id", m.id)
                        put("name", m.name)
                    })
                }
            }
        }

        val brandConfigJson = withContext(Dispatchers.IO) {
            val config = repo.getBrandConfig()
            if (config != null) {
                JSONObject().apply {
                    put("brandName", config.brandName)
                    put("category", config.category)
                    put("niche", config.niche)
                    put("colorScheme", config.colorScheme)
                    put("logoText", config.logoText)
                    put("logoIcon", config.logoIcon)
                    if (config.logoImage != null) put("logoImage", config.logoImage)
                    put("isDarkMode", config.isDarkMode)
                    put("fontSizeScale", config.fontSizeScale)
                }
            } else null
        }

        val payload = JSONObject().apply {
            put("type", "full_sync")
            put("transactions", transactions)
            put("orders", orders)
            put("categories", categories)
            put("calculations", calculations)
            put("clients", clients)
            put("models", models)
            if (brandConfigJson != null) put("brandConfig", brandConfigJson)
        }

        publishJson(payload)
    }

    private suspend fun handleIncomingMessage(json: JSONObject) {
        val type = json.optString("type", "")
        val repo = repository ?: return
        isApplyingRemoteUpdate = true

        try {
            when (type) {
                "full_sync" -> {
                    handleFullSync(json, repo)
                }
                "mutation" -> {
                    val table = json.optString("table", "")
                    val action = json.optString("action", "")
                    val data = json.optJSONObject("data")
                    if (data != null) {
                        handleMutation(table, action, data, repo)
                    }
                }
            }
        } finally {
            isApplyingRemoteUpdate = false
        }
    }

    private suspend fun handleFullSync(json: JSONObject, repo: TransactionRepository) {
        val transactions = json.optJSONArray("transactions")
        val orders = json.optJSONArray("orders")
        val categories = json.optJSONArray("categories")
        val calculations = json.optJSONArray("calculations")
        val clients = json.optJSONArray("clients")
        val models = json.optJSONArray("models")
        val brandConfigJson = json.optJSONObject("brandConfig")

        if (transactions != null) {
            for (i in 0 until transactions.length()) {
                val txJson = transactions.getJSONObject(i)
                try {
                    repo.insert(TransactionEntity(
                        id = txJson.optLong("id", 0),
                        description = txJson.optString("description", ""),
                        amount = txJson.optDouble("amount", 0.0),
                        type = txJson.optString("type", "OUTFLOW"),
                        category = txJson.optString("category", "Geral"),
                        dateString = txJson.optString("dateString", ""),
                        timestamp = txJson.optLong("timestamp", System.currentTimeMillis()),
                        extraText = txJson.optString("extraText", ""),
                        week = txJson.optString("week", "1ª Semana")
                    ))
                } catch (e: Exception) { Log.w(TAG, "Error inserting tx", e) }
            }
        }

        if (orders != null) {
            for (i in 0 until orders.length()) {
                val oJson = orders.getJSONObject(i)
                try {
                    repo.insertOrder(OrderEntity(
                        id = oJson.optLong("id", 0),
                        clientName = oJson.optString("clientName", ""),
                        pantyType = oJson.optString("pantyType", ""),
                        pantySize = oJson.optString("pantySize", "U"),
                        quantity = oJson.optInt("quantity", 0),
                        pantyValue = oJson.optDouble("pantyValue", 0.0),
                        totalValue = oJson.optDouble("totalValue", 0.0),
                        week = oJson.optString("week", "1ª Semana"),
                        businessArea = oJson.optString("businessArea", "Geral"),
                        status = oJson.optString("status", "Pendente"),
                        timestamp = oJson.optLong("timestamp", System.currentTimeMillis())
                    ))
                } catch (e: Exception) { Log.w(TAG, "Error inserting order", e) }
            }
        }

        if (categories != null) {
            for (i in 0 until categories.length()) {
                val cJson = categories.getJSONObject(i)
                try {
                    repo.insertCategory(CategoryEntity(
                        id = cJson.optLong("id", 0),
                        name = cJson.optString("name", ""),
                        type = cJson.optString("type", "OUTFLOW")
                    ))
                } catch (e: Exception) { Log.w(TAG, "Error inserting category", e) }
            }
        }

        if (calculations != null) {
            for (i in 0 until calculations.length()) {
                val calcJson = calculations.getJSONObject(i)
                try {
                    repo.insertCalculation(PieceCalculationEntity(
                        id = calcJson.optLong("id", 0),
                        pano = calcJson.optString("pano", ""),
                        kg = if (calcJson.has("kg")) calcJson.optDouble("kg") else null,
                        valorKg = if (calcJson.has("valorKg")) calcJson.optDouble("valorKg") else null,
                        quantidade = if (calcJson.has("quantidade")) calcJson.optInt("quantidade") else null
                    ))
                } catch (e: Exception) { Log.w(TAG, "Error inserting calculation", e) }
            }
        }

        if (clients != null) {
            for (i in 0 until clients.length()) {
                val clJson = clients.getJSONObject(i)
                try {
                    repo.insertClient(ClientEntity(
                        id = clJson.optLong("id", 0),
                        name = clJson.optString("name", ""),
                        phone = clJson.optString("phone", "")
                    ))
                } catch (e: Exception) { Log.w(TAG, "Error inserting client", e) }
            }
        }

        if (models != null) {
            for (i in 0 until models.length()) {
                val mJson = models.getJSONObject(i)
                try {
                    repo.insertProductModel(ProductModelEntity(
                        id = mJson.optLong("id", 0),
                        name = mJson.optString("name", "")
                    ))
                } catch (e: Exception) { Log.w(TAG, "Error inserting model", e) }
            }
        }

        if (brandConfigJson != null) {
            try {
                repo.insertBrandConfig(BrandConfigEntity(
                    brandName = brandConfigJson.optString("brandName", ""),
                    category = brandConfigJson.optString("category", "Moda Íntima"),
                    niche = brandConfigJson.optString("niche", ""),
                    colorScheme = brandConfigJson.optString("colorScheme", "PINK"),
                    logoText = brandConfigJson.optString("logoText", ""),
                    logoIcon = brandConfigJson.optString("logoIcon", "CROWN"),
                    logoImage = brandConfigJson.optString("logoImage", null),
                    isDarkMode = brandConfigJson.optBoolean("isDarkMode", true),
                    fontSizeScale = brandConfigJson.optDouble("fontSizeScale", 1.0).toFloat()
                ))
            } catch (e: Exception) {
                Log.w(TAG, "Error applying brandConfig", e)
            }
        }
    }

    private suspend fun handleMutation(table: String, action: String, data: JSONObject, repo: TransactionRepository) {
        try {
            when (table) {
                "transactions" -> {
                    when (action) {
                        "insert" -> repo.insert(TransactionEntity(
                            id = data.optLong("id", 0),
                            description = data.optString("description", ""),
                            amount = data.optDouble("amount", 0.0),
                            type = data.optString("type", "OUTFLOW"),
                            category = data.optString("category", "Geral"),
                            dateString = data.optString("dateString", ""),
                            timestamp = data.optLong("timestamp", System.currentTimeMillis()),
                            extraText = data.optString("extraText", ""),
                            week = data.optString("week", "1ª Semana")
                        ))
                        "delete" -> repo.deleteById(data.optLong("id", 0))
                    }
                }
                "orders" -> {
                    when (action) {
                        "insert" -> repo.insertOrder(OrderEntity(
                            id = data.optLong("id", 0),
                            clientName = data.optString("clientName", ""),
                            pantyType = data.optString("pantyType", ""),
                            pantySize = data.optString("pantySize", "U"),
                            quantity = data.optInt("quantity", 0),
                            pantyValue = data.optDouble("pantyValue", 0.0),
                            totalValue = data.optDouble("totalValue", 0.0),
                            week = data.optString("week", "1ª Semana"),
                            businessArea = data.optString("businessArea", "Geral"),
                            status = data.optString("status", "Pendente"),
                            timestamp = data.optLong("timestamp", System.currentTimeMillis())
                        ))
                        "delete" -> repo.deleteOrderById(data.optLong("id", 0))
                    }
                }
                "categories" -> {
                    when (action) {
                        "insert" -> repo.insertCategory(CategoryEntity(
                            id = data.optLong("id", 0),
                            name = data.optString("name", ""),
                            type = data.optString("type", "OUTFLOW")
                        ))
                        "delete" -> repo.deleteCategoryById(data.optLong("id", 0))
                    }
                }
                "calculations" -> {
                    when (action) {
                        "insert" -> repo.insertCalculation(PieceCalculationEntity(
                            id = data.optLong("id", 0),
                            pano = data.optString("pano", ""),
                            kg = if (data.has("kg")) data.optDouble("kg") else null,
                            valorKg = if (data.has("valorKg")) data.optDouble("valorKg") else null,
                            quantidade = if (data.has("quantidade")) data.optInt("quantidade") else null
                        ))
                        "delete" -> repo.deleteCalculationById(data.optLong("id", 0))
                    }
                }
                "clients" -> {
                    when (action) {
                        "insert" -> repo.insertClient(ClientEntity(
                            id = data.optLong("id", 0),
                            name = data.optString("name", ""),
                            phone = data.optString("phone", "")
                        ))
                        "delete" -> repo.deleteClientById(data.optLong("id", 0))
                    }
                }
                "models" -> {
                    when (action) {
                        "insert" -> repo.insertProductModel(ProductModelEntity(
                            id = data.optLong("id", 0),
                            name = data.optString("name", "")
                        ))
                        "delete" -> repo.deleteProductModelById(data.optLong("id", 0))
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling mutation ($table/$action)", e)
        }
    }
}
