package com.example.data.model

data class LeaderboardEntry(
    val rank: Int = 0,
    val userId: String = "",
    val displayName: String = "",
    val country: String = "Global",
    val points: Long = 0L,
    val quizzesCompleted: Int = 0,
    val flagEmoji: String = "🌍"
)
