package com.example.data

import android.content.Context
import org.json.JSONObject

object MqttSyncManager {
    suspend fun syncWithWeb(
        pinCode: String,
        context: Context,
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity>,
        orders: List<OrderEntity>,
        calculations: List<PieceCalculationEntity>,
        brandConfig: JSONObject?
    ): Boolean {
        return false
    }
}
