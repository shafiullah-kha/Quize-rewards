package com.example.data.repository

import com.example.data.model.QuizQuestion
import org.json.JSONArray
import org.json.JSONObject

data class BulkImportResult(
    val totalProcessed: Int,
    val successCount: Int,
    val failedCount: Int,
    val errors: List<String>,
    val validatedQuestions: List<QuizQuestion>
)

object QuestionBulkImporter {

    fun parseAndValidate(
        rawText: String,
        format: String, // "JSON" or "CSV"
        existingQuestions: List<QuizQuestion>,
        allowedCategories: List<String>
    ): BulkImportResult {
        return if (format.equals("JSON", ignoreCase = true) || rawText.trim().startsWith("[")) {
            parseJson(rawText, existingQuestions, allowedCategories)
        } else {
            parseCsv(rawText, existingQuestions, allowedCategories)
        }
    }

    private fun parseJson(
        jsonString: String,
        existingQuestions: List<QuizQuestion>,
        allowedCategories: List<String>
    ): BulkImportResult {
        val errors = mutableListOf<String>()
        val validated = mutableListOf<QuizQuestion>()
        val existingQuestionTexts = existingQuestions.map { it.question.trim().lowercase() }.toMutableSet()

        val jsonArray: JSONArray
        try {
            jsonArray = JSONArray(jsonString.trim())
        } catch (e: Exception) {
            return BulkImportResult(
                totalProcessed = 0,
                successCount = 0,
                failedCount = 1,
                errors = listOf("Invalid JSON syntax: ${e.message}"),
                validatedQuestions = emptyList()
            )
        }

        for (i in 0 until jsonArray.length()) {
            val itemNum = i + 1
            try {
                val obj = jsonArray.getJSONObject(i)
                val qText = obj.optString("question", "").trim()
                if (qText.isBlank()) {
                    errors.add("Item #$itemNum: Question text cannot be empty.")
                    continue
                }

                if (existingQuestionTexts.contains(qText.lowercase())) {
                    errors.add("Item #$itemNum: Duplicate question detected ('$qText').")
                    continue
                }

                val optionsArray = obj.optJSONArray("options")
                if (optionsArray == null || optionsArray.length() != 4) {
                    errors.add("Item #$itemNum: Exactly 4 options required (found ${optionsArray?.length() ?: 0}).")
                    continue
                }

                val options = mutableListOf<String>()
                var hasEmptyOption = false
                for (j in 0 until optionsArray.length()) {
                    val opt = optionsArray.getString(j).trim()
                    if (opt.isBlank()) hasEmptyOption = true
                    options.add(opt)
                }
                if (hasEmptyOption) {
                    errors.add("Item #$itemNum: All 4 options must be non-empty strings.")
                    continue
                }

                val correctAnswer = obj.optInt("correctAnswer", -1)
                if (correctAnswer !in 0..3) {
                    errors.add("Item #$itemNum: correctAnswer must be 0, 1, 2, or 3 (found $correctAnswer).")
                    continue
                }

                var category = obj.optString("category", "General Knowledge").trim()
                if (category.isBlank() || !allowedCategories.any { it.equals(category, ignoreCase = true) }) {
                    category = "General Knowledge"
                }

                var difficulty = obj.optString("difficulty", "Easy").trim()
                if (!listOf("Easy", "Medium", "Hard").any { it.equals(difficulty, ignoreCase = true) }) {
                    difficulty = "Easy"
                }

                val country = obj.optString("country", "Global").trim().ifBlank { "Global" }
                val language = obj.optString("language", "English").trim().ifBlank { "English" }
                val points = obj.optInt("points", 10).coerceIn(5, 50)
                val explanation = obj.optString("explanation", "").trim()

                val validQuestion = QuizQuestion(
                    quizId = "q_bulk_" + System.currentTimeMillis() + "_" + i,
                    question = qText,
                    options = options,
                    correctAnswer = correctAnswer,
                    category = category,
                    difficulty = difficulty,
                    country = country,
                    language = language,
                    points = points,
                    active = true,
                    explanation = explanation
                )
                validated.add(validQuestion)
                existingQuestionTexts.add(qText.lowercase())
            } catch (e: Exception) {
                errors.add("Item #$itemNum: Parsing error: ${e.message}")
            }
        }

        return BulkImportResult(
            totalProcessed = jsonArray.length(),
            successCount = validated.size,
            failedCount = errors.size,
            errors = errors,
            validatedQuestions = validated
        )
    }

    private fun parseCsv(
        csvString: String,
        existingQuestions: List<QuizQuestion>,
        allowedCategories: List<String>
    ): BulkImportResult {
        val errors = mutableListOf<String>()
        val validated = mutableListOf<QuizQuestion>()
        val existingQuestionTexts = existingQuestions.map { it.question.trim().lowercase() }.toMutableSet()

        val lines = csvString.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return BulkImportResult(0, 0, 0, listOf("CSV is empty"), emptyList())
        }

        val startIndex = if (lines[0].contains("question", ignoreCase = true) && lines[0].contains("option", ignoreCase = true)) 1 else 0

        for (i in startIndex until lines.size) {
            val lineNum = i + 1
            val parts = lines[i].split(",").map { it.trim().removeSurrounding("\"") }
            if (parts.size < 6) {
                errors.add("Line #$lineNum: Invalid CSV row format. Minimum 6 columns: question, optA, optB, optC, optD, correctAnswerIndex")
                continue
            }

            val qText = parts[0]
            if (qText.isBlank()) {
                errors.add("Line #$lineNum: Question text cannot be empty.")
                continue
            }

            if (existingQuestionTexts.contains(qText.lowercase())) {
                errors.add("Line #$lineNum: Duplicate question detected ('$qText').")
                continue
            }

            val options = listOf(parts[1], parts[2], parts[3], parts[4])
            if (options.any { it.isBlank() }) {
                errors.add("Line #$lineNum: All 4 options must be non-empty.")
                continue
            }

            val correctAnswer = parts[5].toIntOrNull() ?: -1
            if (correctAnswer !in 0..3) {
                errors.add("Line #$lineNum: Correct answer must be index 0, 1, 2, or 3.")
                continue
            }

            val category = parts.getOrNull(6)?.ifBlank { "General Knowledge" } ?: "General Knowledge"
            val difficulty = parts.getOrNull(7)?.ifBlank { "Easy" } ?: "Easy"
            val country = parts.getOrNull(8)?.ifBlank { "Global" } ?: "Global"
            val language = parts.getOrNull(9)?.ifBlank { "English" } ?: "English"
            val points = parts.getOrNull(10)?.toIntOrNull() ?: 10

            val validQuestion = QuizQuestion(
                quizId = "q_csv_" + System.currentTimeMillis() + "_" + i,
                question = qText,
                options = options,
                correctAnswer = correctAnswer,
                category = category,
                difficulty = difficulty,
                country = country,
                language = language,
                points = points,
                active = true,
                explanation = parts.getOrNull(11) ?: ""
            )
            validated.add(validQuestion)
            existingQuestionTexts.add(qText.lowercase())
        }

        return BulkImportResult(
            totalProcessed = lines.size - startIndex,
            successCount = validated.size,
            failedCount = errors.size,
            errors = errors,
            validatedQuestions = validated
        )
    }

    fun getSampleJsonTemplate(): String {
        return """
[
  {
    "question": "What is the capital of Australia?",
    "options": ["Sydney", "Melbourne", "Canberra", "Brisbane"],
    "correctAnswer": 2,
    "category": "Countries & Capitals",
    "difficulty": "Medium",
    "country": "Australia",
    "language": "English",
    "points": 10,
    "explanation": "Canberra was selected as the capital city in 1908 as a compromise between Sydney and Melbourne."
  },
  {
    "question": "How many players are there on an American football team on the field at one time?",
    "options": ["9", "10", "11", "12"],
    "correctAnswer": 2,
    "category": "Sports",
    "difficulty": "Easy",
    "country": "United States",
    "language": "English",
    "points": 10,
    "explanation": "Each American football team fields 11 players during a play."
  }
]
        """.trimIndent()
    }
}
