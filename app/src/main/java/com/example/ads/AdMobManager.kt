package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdMobManager(private val context: Context) {
    companion object {
        private const val TAG = "AdMobManager"
        // Official Google AdMob Rewarded Video Test Ad Unit ID
        const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    private var rewardedAd: RewardedAd? = null
    private val _isAdLoaded = MutableStateFlow(false)
    val isAdLoaded: StateFlow<Boolean> = _isAdLoaded.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var isInitialized = false

    fun isRunningOnEmulator(): Boolean {
        val fingerprint = android.os.Build.FINGERPRINT
        val model = android.os.Build.MODEL
        val hardware = android.os.Build.HARDWARE
        val product = android.os.Build.PRODUCT
        return fingerprint.startsWith("generic") ||
                fingerprint.startsWith("unknown") ||
                model.contains("google_sdk", ignoreCase = true) ||
                model.contains("Emulator", ignoreCase = true) ||
                model.contains("Android SDK built for", ignoreCase = true) ||
                hardware.contains("goldfish", ignoreCase = true) ||
                hardware.contains("ranchu", ignoreCase = true) ||
                product.contains("sdk_gphone", ignoreCase = true) ||
                product.contains("vbox86p", ignoreCase = true) ||
                product.contains("emulator", ignoreCase = true)
    }

    fun init() {
        if (isInitialized) return
        try {
            if (isRunningOnEmulator()) {
                Log.d(TAG, "Running on emulator environment; skipping AdMob background preload to avoid MESA DRM render node errors and unsafe header errors.")
                isInitialized = true
                return
            }

            MobileAds.disableMediationAdapterInitialization(context)
            val reqConfig = com.google.android.gms.ads.RequestConfiguration.Builder()
                .setTestDeviceIds(listOf(AdRequest.DEVICE_ID_EMULATOR))
                .build()
            MobileAds.setRequestConfiguration(reqConfig)

            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    MobileAds.initialize(context) { status ->
                        Log.d(TAG, "MobileAds initialized: ${status.adapterStatusMap}")
                        isInitialized = true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "MobileAds background init warning: ${e.message}")
                    isInitialized = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "MobileAds init failed: ${e.message}")
            isInitialized = true
        }
    }

    fun loadRewardedAd() {
        if (_isLoading.value) return
        _isLoading.value = true

        try {
            val adRequest = AdRequest.Builder().build()
            RewardedAd.load(
                context,
                TEST_REWARDED_AD_UNIT_ID,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.w(TAG, "Ad failed to load: ${loadAdError.message}, code=${loadAdError.code}")
                        rewardedAd = null
                        _isLoading.value = false
                        _isAdLoaded.value = false
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d(TAG, "RewardedAd successfully loaded")
                        rewardedAd = ad
                        _isLoading.value = false
                        _isAdLoaded.value = true
                    }
                }
            )
        } catch (e: Exception) {
            Log.w(TAG, "Exception during RewardedAd.load: ${e.message}")
            _isLoading.value = false
            _isAdLoaded.value = false
        }
    }

    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: (amount: Int) -> Unit,
        onAdClosed: () -> Unit,
        onAdFailed: (String) -> Unit
    ) {
        val ad = rewardedAd
        if (ad == null) {
            // Ad not loaded yet
            onAdFailed("Ad is not ready yet. Please wait or use Test Ad Player.")
            loadRewardedAd()
            return
        }

        try {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ad was dismissed.")
                    rewardedAd = null
                    _isAdLoaded.value = false
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ad failed to show: ${adError.message}")
                    rewardedAd = null
                    _isAdLoaded.value = false
                    onAdFailed(adError.message)
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ad showed fullscreen content.")
                }
            }

            ad.show(activity) { rewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                onUserEarnedReward(rewardItem.amount)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying RewardedAd: ${e.message}")
            rewardedAd = null
            _isAdLoaded.value = false
            onAdFailed(e.message ?: "Failed to display ad")
        }
    }
}
