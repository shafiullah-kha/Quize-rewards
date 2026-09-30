package com.example.data.model

data class QuizAttempt(
    val attemptId: String = "",
    val userId: String = "",
    val quizMode: String = "Quick Quiz", // Quick Quiz, Daily Quiz, Category Quiz, Mixed Quiz, Country Quiz, Challenge Quiz
    val category: String = "Mixed Worldwide Trivia",
    val score: Int = 0,
    val correctAnswers: Int = 0,
    val wrongAnswers: Int = 0,
    val totalQuestions: Int = 10,
    val pointsEarned: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
