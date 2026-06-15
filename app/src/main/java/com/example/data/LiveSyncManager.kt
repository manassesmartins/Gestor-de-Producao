package com.example.data

import android.content.Context
import org.json.JSONObject

object LiveSyncManager {
    var activeGroupCode: String? = null
        private set

    val clientUuid: String = "local-uuid"
    var isApplyingRemoteUpdate = false

    fun getStoredGroupCode(context: Context): String? = null
    fun saveGroupCode(context: Context, code: String?) {}
    fun initializeStoredSync(context: Context, repository: TransactionRepository) {}
    fun startSync(context: Context, code: String, repository: TransactionRepository) {}
    fun stopSync(context: Context) {}
    fun publishMutation(table: String, action: String, data: JSONObject) {}
}
