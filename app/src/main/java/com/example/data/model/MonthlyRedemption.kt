package com.example.data.model

data class MonthlyRedemption(
    val id: String = "", // userId_YYYY_MM
    val userId: String = "",
    val month: String = "", // e.g. "2026-09"
    val pointsRedeemed: Long = 1000L,
    val diamondAmount: Int = 60,
    val playerId: String = "",
    val status: String = STATUS_PENDING, // PENDING, PROCESSING, COMPLETED, FAILED, REJECTED
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val providerTransactionId: String? = null,
    val failureReason: String? = null,
    val adminNote: String? = null,
    val userName: String = "",
    val userEmail: String = ""
) {
    companion object {
        const val STATUS_PENDING = "PENDING"
        const val STATUS_PROCESSING = "PROCESSING"
        const val STATUS_COMPLETED = "COMPLETED"
        const val STATUS_FAILED = "FAILED"
        const val STATUS_REJECTED = "REJECTED"

        val ALLOWED_STATUSES = listOf(
            STATUS_PENDING,
            STATUS_PROCESSING,
            STATUS_COMPLETED,
            STATUS_FAILED,
            STATUS_REJECTED
        )
    }

    val isPendingOrProcessing: Boolean
        get() = status == STATUS_PENDING || status == STATUS_PROCESSING

    val isTerminal: Boolean
        get() = status == STATUS_COMPLETED || status == STATUS_FAILED || status == STATUS_REJECTED
}
