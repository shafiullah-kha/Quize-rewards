package com.example.data.model

data class QuizQuestion(
    val quizId: String = "",
    val question: String = "",
    val options: List<String> = emptyList(), // exactly 4 options
    val correctAnswer: Int = 0, // 0..3
    val category: String = "General Knowledge",
    val difficulty: String = "Easy", // Easy, Medium, Hard
    val country: String = "Global", // Global, Pakistan, India, United States, United Kingdom, etc.
    val language: String = "English", // English, Urdu, Hindi, Arabic
    val points: Int = 10,
    val active: Boolean = true,
    val explanation: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
