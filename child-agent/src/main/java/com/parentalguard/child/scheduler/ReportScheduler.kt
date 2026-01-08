package com.parentalguard.child.scheduler

import android.content.Context
import androidx.work.*
import com.parentalguard.child.monitor.UsageMonitor
import com.parentalguard.child.network.CommandServer
import com.parentalguard.common.network.Packet
import com.parentalguard.common.network.CommandType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class DailyReportWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val usageMonitor = UsageMonitor(applicationContext)
            val report = usageMonitor.generateDailyReport()
            
            // Send report to parent (this would be sent via network)
            // For now, we'll store it locally or send via CommandServer
            // Note: This is a simplified implementation
            // In production, you'd want to queue this and send when connected
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

object ReportScheduler {
    
    private const val WORK_NAME = "daily_usage_report"
    
    /**
     * Schedule daily report generation at midnight
     */
    fun scheduleDailyReport(context: Context) {
        val currentDate = java.util.Calendar.getInstance()
        val dueDate = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23)
            set(java.util.Calendar.MINUTE, 59)
            set(java.util.Calendar.SECOND, 0)
            
            // If time has passed today, schedule for tomorrow
            if (before(currentDate)) {
                add(java.util.Calendar.DATE, 1)
            }
        }
        
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReportWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            dailyWorkRequest
        )
    }
    
    /**
     * Cancel daily report scheduling
     */
    fun cancelDailyReport(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
