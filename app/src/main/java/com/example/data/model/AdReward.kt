package com.example.data.model

data class AdReward(
    val transactionId: String = "",
    val userId: String = "",
    val points: Int = 25,
    val adProvider: String = "Google AdMob",
    val rewarded: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val dateKey: String = "" // e.g. "2026-09-24"
)
