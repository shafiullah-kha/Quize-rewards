package com.example.data.model

data class SpinDaily(
    val userId: String = "",
    val date: String = "", // YYYY-MM-DD
    val freeSpins: Int = 3, // free spins left for today
    val adSpins: Int = 0, // ad-earned extra spins used today
    val totalSpins: Int = 0,
    val extraSpinsAvailable: Int = 0, // extra spins earned through ads ready to spin
    val lastSpinAt: Long = 0L,
    val updatedAt: Long = System.currentTimeMillis()
)
