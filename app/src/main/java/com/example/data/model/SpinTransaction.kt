package com.example.data.model

data class SpinTransaction(
    val spinTransactionId: String = "",
    val userId: String = "",
    val spinType: String = "FREE_SPIN", // FREE_SPIN, REWARDED_AD_SPIN
    val rewardPoints: Long = 20L,
    val status: String = "COMPLETED", // COMPLETED, FAILED
    val createdAt: Long = System.currentTimeMillis(),
    val source: String = "Spin & Earn"
)
