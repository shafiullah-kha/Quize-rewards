package com.example.data.model

data class MonthlyRedemptionWindowInfo(
    val isOpen: Boolean = false,
    val currentServerDay: Int = 1,
    val currentMonthKey: String = "", // e.g. "2026-09"
    val currentMonthDisplayName: String = "", // e.g. "September 2026"
    val nextWindowStartDate: String = "5th of next month",
    val windowRangeDescription: String = "5th – 10th of every month",
    val serverTimezone: String = "UTC",
    val serverTimestampMs: Long = System.currentTimeMillis(),
    val timeRemainingMs: Long = 0L,
    val countdownFormatted: String = "",
    val statusTitle: String = "Redemption is currently closed.",
    val statusSubtitle: String = "Redemption opens on the 5th and remains available until the 10th.",
    val hasSubmittedThisMonth: Boolean = false,
    val existingMonthlyRedemption: MonthlyRedemption? = null
)
