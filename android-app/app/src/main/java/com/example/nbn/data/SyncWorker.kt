package com.example.nbn.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.itemDao()
        
        try {
            val unsyncedItems = dao.getUnsyncedItems()
            if (unsyncedItems.isEmpty()) {
                return@withContext Result.success()
            }
            
            // TODO: Hook up Retrofit/Ktor call to the Serverless Backend
            // e.g., apiService.syncItems(unsyncedItems)
            
            // On success:
            val syncedIds = unsyncedItems.map { it.id }
            dao.markAsSynced(syncedIds)
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            // If offline or network error, we retry later
            Result.retry()
        }
    }
}
