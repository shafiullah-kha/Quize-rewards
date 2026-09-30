package com.example.data.model

data class WalletTransaction(
    val transactionId: String = "",
    val userId: String = "",
    val type: String = "quiz", // quiz, rewarded_ad, redemption, refund, admin_adjustment
    val points: Long = 0L, // Positive for earnings, negative for redemptions
    val source: String = "",
    val referenceId: String = "",
    val status: String = "COMPLETED",
    val createdAt: Long = System.currentTimeMillis()
)
