package com.parentalguard.parent.data

import android.content.Context
import android.content.SharedPreferences
import com.parentalguard.common.model.DailyUsageReport
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class ReportsRepository(context: Context) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("reports_prefs", Context.MODE_PRIVATE)
    
    private val json = Json { 
        ignoreUnknownKeys = true
        prettyPrint = false 
    }
    
    companion object {
        private const val KEY_REPORTS = "historical_reports"
    }

    /**
     * Save a daily report. If a report for the same date and device already exists, it updates it.
     */
    fun saveReport(report: DailyUsageReport) {
        val allReports = getAllReports().toMutableList()
        
        // Remove existing report for the same date and device to avoid duplicates
        allReports.removeAll { it.date == report.date && it.deviceName == report.deviceName }
        
        allReports.add(report)
        
        // Keep only last 30 days of reports to save space (optional, but good practice)
        val sortedReports = allReports.sortedByDescending { it.date }
        val limitedReports = if (sortedReports.size > 100) sortedReports.take(100) else sortedReports
        
        val jsonString = json.encodeToString(limitedReports)
        prefs.edit().putString(KEY_REPORTS, jsonString).apply()
    }

    /**
     * Get all saved reports
     */
    fun getAllReports(): List<DailyUsageReport> {
        val jsonString = prefs.getString(KEY_REPORTS, null) ?: return emptyList()
        return try {
            json.decodeFromString<List<DailyUsageReport>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Get reports for a specific device
     */
    fun getReportsForDevice(deviceName: String): List<DailyUsageReport> {
        return getAllReports().filter { it.deviceName == deviceName }.sortedByDescending { it.date }
    }
    
    /**
     * Get the report for today for a specific device. 
     * This is useful if we want to show today's data cached when offline.
     */
    fun getTodayReport(deviceName: String): DailyUsageReport? {
        val today = android.text.format.DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString()
        return getAllReports().find { it.date == today && it.deviceName == deviceName }
    }
}
