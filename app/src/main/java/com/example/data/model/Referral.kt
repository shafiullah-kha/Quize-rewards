package com.example.data.model

data class Referral(
    val referralId: String = "",
    val referrerUserId: String = "",
    val referredUserId: String = "",
    val referralCode: String = "",
    val status: String = "PENDING", // PENDING, QUALIFIED, REWARDED, REJECTED
    val rewardPoints: Long = 100L,
    val referredUserName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = System.currentTimeMillis()
)
