package com.example.data.model

data class RedemptionRequest(
    val requestId: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val userName: String = "",
    val rewardId: String = "",
    val rewardTitle: String = "Free Fire Diamonds",
    val diamonds: Int = 60,
    val points: Long = 1000L,
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED, COMPLETED
    val recipientDetails: String = "", // Player ID / In-game Name
    val adminNote: String = "", // Voucher PIN, receipt or reason for rejection
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
