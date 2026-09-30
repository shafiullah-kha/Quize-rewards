package com.example.data.model

data class RewardItem(
    val rewardId: String = "",
    val title: String = "Free Fire Diamonds",
    val description: String = "",
    val diamonds: Int = 60,
    val requiredPoints: Long = 1000L,
    val category: String = "Free Fire",
    val bannerIcon: String = "diamond",
    val enabled: Boolean = true,
    val requiresPlayerId: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
