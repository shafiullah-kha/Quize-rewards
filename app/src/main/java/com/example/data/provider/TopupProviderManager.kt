package com.example.data.provider

import android.util.Log
import com.example.data.model.AppConfig
import com.example.data.model.MonthlyRedemption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * TopupProviderManager handles authorized diamond delivery.
 *
 * Adheres strictly to policy:
 * - Credentials sourced from environment variables:
 *     TOPUP_PROVIDER_API_URL
 *     TOPUP_PROVIDER_API_KEY
 *     TOPUP_PROVIDER_SECRET
 *     TOPUP_PROVIDER_MERCHANT_ID
 * - Does not invent mock responses or simulate fake delivery.
 * - When credentials are absent or blank, keeps the request in PENDING status
 *   for manual processing by the authorized administrator.
 * - Never collects or asks for Free Fire passwords.
 */
class TopupProviderManager {
    companion object {
        private const val TAG = "TopupProviderManager"

        val envApiUrl: String = System.getenv("TOPUP_PROVIDER_API_URL") ?: ""
        val envApiKey: String = System.getenv("TOPUP_PROVIDER_API_KEY") ?: ""
        val envSecret: String = System.getenv("TOPUP_PROVIDER_SECRET") ?: ""
        val envMerchantId: String = System.getenv("TOPUP_PROVIDER_MERCHANT_ID") ?: ""
    }

    fun isProviderConfigured(config: AppConfig): Boolean {
        val apiUrl = config.topupProviderApiUrl.ifBlank { envApiUrl }
        val apiKey = config.topupProviderApiKey.ifBlank { envApiKey }
        return apiUrl.isNotBlank() && apiKey.isNotBlank()
    }

    suspend fun attemptAutomatedDelivery(
        redemption: MonthlyRedemption,
        config: AppConfig
    ): DeliveryResult = withContext(Dispatchers.IO) {
        val apiUrl = config.topupProviderApiUrl.ifBlank { envApiUrl }
        val apiKey = config.topupProviderApiKey.ifBlank { envApiKey }

        if (apiUrl.isBlank() || apiKey.isBlank()) {
            Log.d(TAG, "No automated top-up provider configured. Keeping request in PENDING for manual admin fulfillment.")
            return@withContext DeliveryResult.KeptPending(
                note = "Awaiting manual admin review & official voucher issuance. (No automated top-up provider API configured)"
            )
        }

        // Real provider endpoint dispatch would occur here with real HMAC signatures & HTTP request.
        // If not connected to a live production server, fail safely or keep pending.
        return@withContext DeliveryResult.KeptPending(
            note = "Automated provider dispatch pending verification with provider endpoint $apiUrl."
        )
    }

    sealed class DeliveryResult {
        data class KeptPending(val note: String) : DeliveryResult()
        data class Success(val providerTxId: String, val note: String) : DeliveryResult()
        data class Failure(val reason: String) : DeliveryResult()
    }
}
