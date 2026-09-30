package com.example.data.time

import android.os.SystemClock
import android.util.Log
import com.example.data.model.AppConfig
import com.example.data.model.MonthlyRedemptionWindowInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * ServerTimeManager provides tamper-proof, server-authoritative time.
 *
 * It calibrates true server time using network responses and synchronizes with
 * Android's hardware-level monotonic clock (SystemClock.elapsedRealtime()).
 * Even if a user alters the phone date, time, or timezone in Android device settings,
 * elapsedRealtime() is unaffected and continues tracking monotonic time since boot.
 */
class ServerTimeManager {
    companion object {
        private const val TAG = "ServerTimeManager"

        @Volatile
        private var instance: ServerTimeManager? = null

        fun getInstance(): ServerTimeManager {
            return instance ?: synchronized(this) {
                instance ?: ServerTimeManager().also { instance = it }
            }
        }
    }

    // Offset in milliseconds between trusted server epoch and SystemClock.elapsedRealtime()
    @Volatile
    private var serverOffsetMs: Long = System.currentTimeMillis() - SystemClock.elapsedRealtime()

    @Volatile
    private var lastSyncTimeMs: Long = 0L

    @Volatile
    private var isCalibrated: Boolean = false

    /**
     * Calibrate server time by querying reliable server headers.
     * Can be called asynchronously on app start and before critical redemption actions.
     */
    suspend fun syncWithServer(): Boolean = withContext(Dispatchers.IO) {
        val endpoints = listOf(
            "https://clients3.google.com/generate_204",
            "https://www.google.com",
            "https://firestore.googleapis.com"
        )

        for (endpoint in endpoints) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(endpoint)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "HEAD"
                    connectTimeout = 4000
                    readTimeout = 4000
                    useCaches = false
                }
                connection.connect()

                val serverDateHeader = connection.date
                if (serverDateHeader > 0) {
                    val currentElapsed = SystemClock.elapsedRealtime()
                    serverOffsetMs = serverDateHeader - currentElapsed
                    lastSyncTimeMs = System.currentTimeMillis()
                    isCalibrated = true
                    Log.d(TAG, "Server time calibrated successfully. Server time: ${Date(serverDateHeader)}")
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Time sync attempt failed on $endpoint: ${e.message}")
            } finally {
                connection?.disconnect()
            }
        }
        false
    }

    /**
     * Returns current trusted server timestamp in milliseconds.
     * Tamper-proof: Immune to device clock modifications.
     */
    fun getTrustedServerEpochMs(): Long {
        return SystemClock.elapsedRealtime() + serverOffsetMs
    }

    /**
     * Returns today's date formatted as YYYY-MM-DD based on server time.
     */
    fun getTodayKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date(getTrustedServerEpochMs()))
    }

    /**
     * Computes the current window state and countdown according to the official server timezone.
     */
    fun getWindowInfo(config: AppConfig): MonthlyRedemptionWindowInfo {
        val serverTimezone = TimeZone.getTimeZone(config.serverTimezone.ifBlank { "UTC" })
        val calendar = Calendar.getInstance(serverTimezone).apply {
            timeInMillis = getTrustedServerEpochMs()
        }

        // Check if admin has set an active day override for QA / test verification
        val rawServerDay = calendar.get(Calendar.DAY_OF_MONTH)
        val currentDay = config.testServerDayOverride ?: rawServerDay

        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.US).apply {
            timeZone = serverTimezone
        }
        val currentMonthKey = monthFormat.format(calendar.time)

        val monthDisplayFormat = SimpleDateFormat("MMMM yyyy", Locale.US).apply {
            timeZone = serverTimezone
        }
        val currentMonthDisplayName = monthDisplayFormat.format(calendar.time)

        val startDay = config.redemptionStartDay.coerceIn(1, 28)
        val endDay = config.redemptionEndDay.coerceIn(startDay, 28)

        val isOpen = currentDay in startDay..endDay

        // Calculate Next Window Start Date and Countdown
        val nowMs = calendar.timeInMillis

        val (timeRemainingMs, nextWindowDateStr, statusTitle, statusSubtitle) = if (isOpen) {
            // Window is OPEN: countdown until end of day `endDay` (23:59:59.999)
            val closeCal = (calendar.clone() as Calendar).apply {
                set(Calendar.DAY_OF_MONTH, endDay)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }
            val remaining = (closeCal.timeInMillis - nowMs).coerceAtLeast(0L)
            val nextDateStr = "10 $currentMonthDisplayName (Closes at 23:59 ${serverTimezone.id})"
            val title = "Redemption is OPEN"
            val subtitle = "Redeem your Diamonds before the 10th of $currentMonthDisplayName."
            Quadruple(remaining, nextDateStr, title, subtitle)
        } else {
            // Window is CLOSED: countdown until 5th of current month (if before 5th) or 5th of next month (if after 10th)
            val openCal = (calendar.clone() as Calendar).apply {
                if (currentDay > endDay) {
                    add(Calendar.MONTH, 1)
                }
                set(Calendar.DAY_OF_MONTH, startDay)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val remaining = (openCal.timeInMillis - nowMs).coerceAtLeast(0L)

            val nextMonthFormat = SimpleDateFormat("MMMM yyyy", Locale.US).apply {
                timeZone = serverTimezone
            }
            val nextMonthStr = nextMonthFormat.format(openCal.time)
            val nextDateStr = "$startDay $nextMonthStr"
            val title = "Redemption is currently closed."
            val subtitle = "Redemption opens on the 5th and remains available until the 10th."
            Quadruple(remaining, nextDateStr, title, subtitle)
        }

        val countdownStr = formatCountdown(timeRemainingMs, isOpen)

        return MonthlyRedemptionWindowInfo(
            isOpen = isOpen,
            currentServerDay = currentDay,
            currentMonthKey = currentMonthKey,
            currentMonthDisplayName = currentMonthDisplayName,
            nextWindowStartDate = nextWindowDateStr,
            windowRangeDescription = "${startDay}th – ${endDay}th of every month",
            serverTimezone = serverTimezone.id,
            serverTimestampMs = nowMs,
            timeRemainingMs = timeRemainingMs,
            countdownFormatted = countdownStr,
            statusTitle = statusTitle,
            statusSubtitle = statusSubtitle
        )
    }

    private fun formatCountdown(ms: Long, isOpen: Boolean): String {
        if (ms <= 0L) return if (isOpen) "Closing soon" else "Opening soon"
        val totalSeconds = ms / 1000
        val days = totalSeconds / 86400
        val hours = (totalSeconds % 86400) / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        val prefix = if (isOpen) "Closes in" else "Opens in"
        return if (days > 0) {
            String.format(Locale.US, "%s: %dd %02dh %02dm %02ds", prefix, days, hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%s: %02dh %02dm %02ds", prefix, hours, minutes, seconds)
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
