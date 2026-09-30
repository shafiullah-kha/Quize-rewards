package com.example.data.model

data class UserProfile(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoURL: String = "",
    val profileImage: String = "",
    val country: String = "United States",
    val language: String = "English",
    val points: Long = 0L,
    val role: String = "user", // "user" or "admin"
    val status: String = "active", // "active" or "suspended"
    val referralCode: String = "",
    val referredBy: String = "",
    val welcomeBonusAwarded: Boolean = false,
    val lastLoginAt: Long = System.currentTimeMillis(),
    val quizzesCompleted: Int = 0,
    val correctAnswers: Int = 0,
    val quizzesAnsweredToday: Int = 0,
    val adsWatchedToday: Int = 0,
    val totalQuizzesAnswered: Int = 0,
    val totalAdsWatched: Int = 0,
    val lastActiveDate: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val uid: String
        get() = userId

    val effectiveProfileImage: String
        get() = profileImage.ifBlank { photoURL }

    val isAdmin: Boolean
        get() = role == "admin" || email.equals("shafihu394366@gmail.com", ignoreCase = true)
}
