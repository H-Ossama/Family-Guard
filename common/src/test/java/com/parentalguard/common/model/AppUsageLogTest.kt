package com.parentalguard.common.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AppUsageLogTest {
    @Test
    fun mergesDuplicatePackagesAndKeepsLatestMetadata() {
        val merged = listOf(
            AppUsageLog("com.google.android.youtube", 1_000, 10, "2026-08-19"),
            AppUsageLog("com.google.android.youtube", 2_000, 20, "2026-08-19"),
            AppUsageLog("com.example.other", 5_000, 15, "2026-08-19")
        ).mergedByPackage()

        assertEquals(2, merged.size)
        assertEquals("com.google.android.youtube", merged[1].packageName)
        assertEquals(3_000, merged[1].totalTimeInForeground)
        assertEquals(20, merged[1].lastTimeUsed)
    }
}
