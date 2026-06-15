package com.example.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object DatabaseBackupManager {
    private const val TAG = "DatabaseBackup"

    // Closes current active database, overwrites it with backupBytes, resets WAL / SHM files
    private fun restoreDatabase(context: Context, backupBytes: ByteArray): Boolean {
        val dbName = "ms_modaintima_database"
        val dbFile = context.getDatabasePath(dbName)
        val dbWal = File(dbFile.path + "-wal")
        val dbShm = File(dbFile.path + "-shm")

        try {
            // Force-close and clear Room instance
            AppDatabase.clearInstance()

            if (!dbFile.parentFile.exists()) {
                dbFile.parentFile.mkdirs()
            }
            
            dbFile.outputStream().use { output ->
                output.write(backupBytes)
            }

            // Delete Write-Ahead Logs and Shared Memory from previous DB state
            if (dbWal.exists()) dbWal.delete()
            if (dbShm.exists()) dbShm.delete()

            Log.i(TAG, "Local database file restored successfully and journal files purged.")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error writing restored database file", e)
            return false
        }
    }

    // Handles importing bytes from inputStream, then restoring SQLite file
    fun importLocalDatabase(context: Context, inputStream: InputStream): Boolean {
        return try {
            val bytes = inputStream.readBytes()
            if (bytes.isEmpty()) {
                Log.e(TAG, "Import failed: Selected backup file is empty.")
                false
            } else {
                restoreDatabase(context, bytes)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error importing local database", e)
            false
        }
    }

    // Exports standard local database after closing connection to flush all WAL frames to main database file
    fun exportLocalDatabase(context: Context, outputStream: OutputStream): Boolean {
        return try {
            // Close active AppDatabase instance to safely flush all WAL frames to main database file
            AppDatabase.clearInstance()

            val dbFile = context.getDatabasePath("ms_modaintima_database")
            if (!dbFile.exists()) {
                Log.e(TAG, "Export failed: Database file does not exist.")
                false
            } else {
                dbFile.inputStream().use { input ->
                    input.copyTo(outputStream)
                }
                Log.i(TAG, "Local database exported successfully.")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error exporting local database", e)
            false
        }
    }
}
