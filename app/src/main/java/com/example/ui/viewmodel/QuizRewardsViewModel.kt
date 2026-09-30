package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdMobManager
import com.example.auth.GoogleAuthManager
import com.example.data.model.AppConfig
import com.example.data.model.MonthlyRedemption
import com.example.data.model.MonthlyRedemptionWindowInfo
import com.example.data.model.QuizQuestion
import com.example.data.model.RedemptionRequest
import com.example.data.model.Referral
import com.example.data.model.RewardItem
import com.example.data.model.SpinDaily
import com.example.data.model.SpinTransaction
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.example.data.repository.QuizAnswerResult
import com.example.data.repository.QuizRewardsRepository
import com.example.data.repository.SpinRewardResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuizRewardsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuizRewardsRepository.getInstance(application)
    val adMobManager = AdMobManager(application)
    val googleAuthManager = GoogleAuthManager(application)
    val isGoogleAuthInProgress = MutableStateFlow(false)

    val currentUser: StateFlow<UserProfile?> = repository.currentUser
    val quizzes: StateFlow<List<QuizQuestion>> = repository.quizzes
    val rewards: StateFlow<List<RewardItem>> = repository.rewards
    val transactions: StateFlow<List<WalletTransaction>> = repository.transactions
    val myRedemptions: StateFlow<List<RedemptionRequest>> = repository.myRedemptions
    val allRedemptions: StateFlow<List<RedemptionRequest>> = repository.allRedemptions
    val allUsers: StateFlow<List<UserProfile>> = repository.allUsers
    val appConfig: StateFlow<AppConfig> = repository.appConfig

    // Welcome Bonus in-app message
    val welcomeBonusMessage: StateFlow<String?> = repository.welcomeBonusMessage

    // Referral System
    val myReferrals: StateFlow<List<Referral>> = repository.myReferrals
    val allReferrals: StateFlow<List<Referral>> = repository.allReferrals

    // Spin & Earn
    val spinDaily: StateFlow<SpinDaily> = repository.spinDaily
    val spinHistory: StateFlow<List<SpinTransaction>> = repository.spinHistory
    val isSpinning = MutableStateFlow(false)
    val spinFeedback = MutableStateFlow<String?>(null)

    // Monthly Diamond Redemption State
    val monthlyRedemptions: StateFlow<List<MonthlyRedemption>> = repository.monthlyRedemptions
    val myMonthlyRedemptions: StateFlow<List<MonthlyRedemption>> = repository.myMonthlyRedemptions
    val monthlyWindowInfo: StateFlow<MonthlyRedemptionWindowInfo> = repository.monthlyWindowInfo

    val monthlyPlayerIdInput = MutableStateFlow("")
    val monthlySelectedPoints = MutableStateFlow(1000L)
    val monthlyRedemptionFeedback = MutableStateFlow<String?>(null)
    val monthlyRedemptionIsSubmitting = MutableStateFlow(false)

    // Wallet Breakdown Stats
    val walletBreakdown: StateFlow<WalletBreakdown> = combine(
        currentUser,
        transactions,
        myRedemptions,
        myMonthlyRedemptions
    ) { user, txs, myReds, myMonthly ->
        val total = user?.points ?: 0L
        val quizEarned = txs.filter { it.type == "QUIZ_EARN" || it.type == "quiz" }.sumOf { it.points }
        val adEarned = txs.filter { it.type == "AD_EARN" || it.type == "rewarded_ad" }.sumOf { it.points }
        val spinEarned = txs.filter { it.type == "SPIN_REWARD" }.sumOf { it.points }
        val welcomeEarned = txs.filter { it.type == "WELCOME_GIFT" }.sumOf { it.points }
        val referralEarned = txs.filter { it.type == "REFERRAL_BONUS" }.sumOf { it.points }
        val redeemed = myReds.filter { it.status == "COMPLETED" || it.status == "APPROVED" }.sumOf { it.points } +
                myMonthly.filter { it.status == MonthlyRedemption.STATUS_COMPLETED }.sumOf { it.pointsRedeemed }
        val pending = myReds.filter { it.status == "PENDING" }.sumOf { it.points } +
                myMonthly.filter { it.isPendingOrProcessing }.sumOf { it.pointsRedeemed }
        WalletBreakdown(
            totalPoints = total,
            earnedFromQuizzes = quizEarned,
            earnedFromAds = adEarned,
            earnedFromSpins = spinEarned,
            earnedFromWelcomeGift = welcomeEarned,
            earnedFromReferrals = referralEarned,
            redeemedPoints = redeemed,
            pendingPoints = pending
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WalletBreakdown()
    )

    // Quiz UI State
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _currentQuizIndex = MutableStateFlow(0)
    val currentQuizIndex: StateFlow<Int> = _currentQuizIndex.asStateFlow()

    private val _selectedOptionIndex = MutableStateFlow<Int?>(null)
    val selectedOptionIndex: StateFlow<Int?> = _selectedOptionIndex.asStateFlow()

    private val _lastAnswerResult = MutableStateFlow<QuizAnswerResult?>(null)
    val lastAnswerResult: StateFlow<QuizAnswerResult?> = _lastAnswerResult.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    // Rewarded Ad UI State
    private val _adStatusMessage = MutableStateFlow<String?>(null)
    val adStatusMessage: StateFlow<String?> = _adStatusMessage.asStateFlow()

    private val _isShowingAdSimulation = MutableStateFlow(false)
    val isShowingAdSimulation: StateFlow<Boolean> = _isShowingAdSimulation.asStateFlow()

    private val _adSimulationProgress = MutableStateFlow(0f)
    val adSimulationProgress: StateFlow<Float> = _adSimulationProgress.asStateFlow()

    private val _adCountdownSeconds = MutableStateFlow(10)
    val adCountdownSeconds: StateFlow<Int> = _adCountdownSeconds.asStateFlow()

    private var adSimulationJob: Job? = null

    // Redemption Dialog State
    private val _selectedRewardForRedemption = MutableStateFlow<RewardItem?>(null)
    val selectedRewardForRedemption: StateFlow<RewardItem?> = _selectedRewardForRedemption.asStateFlow()

    private val _recipientInput = MutableStateFlow("")
    val recipientInput: StateFlow<String> = _recipientInput.asStateFlow()

    private val _redemptionFeedback = MutableStateFlow<String?>(null)
    val redemptionFeedback: StateFlow<String?> = _redemptionFeedback.asStateFlow()

    // Auth Screen State
    private val _isSignUpMode = MutableStateFlow(false)
    val isSignUpMode: StateFlow<Boolean> = _isSignUpMode.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

    init {
        adMobManager.init()
        viewModelScope.launch {
            while (true) {
                repository.refreshMonthlyWindowInfo()
                delay(1000)
            }
        }
    }

    // Category Selection
    fun selectCategory(cat: String) {
        _selectedCategory.value = cat
        _currentQuizIndex.value = 0
        _selectedOptionIndex.value = null
        _lastAnswerResult.value = null
        _quizCompleted.value = false
    }

    fun getFilteredQuizzes(): List<QuizQuestion> {
        val list = quizzes.value
        val cat = selectedCategory.value
        return if (cat == "All") list else list.filter { it.category.equals(cat, ignoreCase = true) }
    }

    fun selectQuizOption(index: Int) {
        if (_lastAnswerResult.value != null) return // Already submitted
        _selectedOptionIndex.value = index
    }

    fun submitAnswer() {
        val selected = _selectedOptionIndex.value ?: return
        val currentQuestions = getFilteredQuizzes()
        if (currentQuestions.isEmpty() || _currentQuizIndex.value >= currentQuestions.size) return
        val currentQ = currentQuestions[_currentQuizIndex.value]

        val result = repository.submitQuizAnswer(currentQ, selected)
        _lastAnswerResult.value = result
        if (result.isCorrect) {
            _quizScore.value += result.pointsEarned
        }
    }

    fun nextQuestion() {
        val currentQuestions = getFilteredQuizzes()
        _selectedOptionIndex.value = null
        _lastAnswerResult.value = null
        if (_currentQuizIndex.value + 1 < currentQuestions.size) {
            _currentQuizIndex.value += 1
        } else {
            _quizCompleted.value = true
        }
    }

    fun restartQuiz() {
        _currentQuizIndex.value = 0
        _selectedOptionIndex.value = null
        _lastAnswerResult.value = null
        _quizScore.value = 0
        _quizCompleted.value = false
        repository.resetSessionQuestions()
    }

    // Rewarded Ad logic
    fun watchRewardedAd(activity: Activity) {
        val canWatch = repository.canWatchRewardedAd()
        if (!canWatch) {
            val limit = appConfig.value.dailyAdLimit
            _adStatusMessage.value = "Daily ad limit ($limit) reached. Please come back tomorrow!"
            return
        }

        if (adMobManager.isRunningOnEmulator()) {
            // Emulators lack hardware DRM render nodes and video codecs.
            // Launch the built-in Test Ad Player directly for a clean, error-free experience.
            startAdSimulation()
            return
        }

        if (adMobManager.isAdLoaded.value) {
            adMobManager.showRewardedAd(
                activity = activity,
                onUserEarnedReward = { amount ->
                    val result = repository.onRewardedAdCompleted("Google AdMob")
                    result.onSuccess { pts ->
                        _adStatusMessage.value = "Success! Earned $pts bonus points."
                    }.onFailure { err ->
                        _adStatusMessage.value = err.message
                    }
                },
                onAdClosed = {
                    adMobManager.loadRewardedAd()
                },
                onAdFailed = { error ->
                    _adStatusMessage.value = "Ad failed: $error. Launching Test Rewarded Video..."
                    startAdSimulation()
                }
            )
        } else {
            // Start loading for next time and play Test Ad Player immediately
            adMobManager.loadRewardedAd()
            startAdSimulation()
        }
    }

    fun startAdSimulation() {
        if (!repository.canWatchRewardedAd()) {
            _adStatusMessage.value = "Daily ad limit reached."
            return
        }
        _isShowingAdSimulation.value = true
        _adSimulationProgress.value = 0f
        _adCountdownSeconds.value = 10

        adSimulationJob?.cancel()
        adSimulationJob = viewModelScope.launch {
            val totalSeconds = 10
            for (sec in totalSeconds downTo 1) {
                _adCountdownSeconds.value = sec
                _adSimulationProgress.value = (totalSeconds - sec) / totalSeconds.toFloat()
                delay(1000)
            }
            _adSimulationProgress.value = 1f
            _adCountdownSeconds.value = 0
            _isShowingAdSimulation.value = false

            // User fully watched the ad! Now grant the reward securely
            val result = repository.onRewardedAdCompleted("AdMob Test Unit (ca-app-pub-3940256099942544/5224354917)")
            result.onSuccess { pts ->
                _adStatusMessage.value = "Congratulations! Rewarded ad completed. +$pts points added to your wallet!"
            }.onFailure { err ->
                _adStatusMessage.value = err.message
            }
        }
    }

    fun cancelAdSimulation() {
        adSimulationJob?.cancel()
        _isShowingAdSimulation.value = false
        _adStatusMessage.value = "Ad cancelled early. Points are only awarded upon completing the entire video."
    }

    fun dismissAdStatusMessage() {
        _adStatusMessage.value = null
    }

    // Redemption logic
    fun openRedemptionDialog(reward: RewardItem) {
        _selectedRewardForRedemption.value = reward
        _recipientInput.value = ""
        _redemptionFeedback.value = null
    }

    fun closeRedemptionDialog() {
        _selectedRewardForRedemption.value = null
        _recipientInput.value = ""
        _redemptionFeedback.value = null
    }

    fun setRecipientInput(input: String) {
        _recipientInput.value = input
    }

    fun confirmRedemption() {
        val reward = _selectedRewardForRedemption.value ?: return
        val details = _recipientInput.value
        val result = repository.submitRedemptionRequest(reward, details)
        result.onSuccess { req ->
            _redemptionFeedback.value = "Success! Request ${req.requestId} submitted for admin processing."
        }.onFailure { err ->
            _redemptionFeedback.value = "Error: ${err.message}"
        }
    }

    // Auth logic
    fun toggleAuthMode() {
        _isSignUpMode.value = !_isSignUpMode.value
        _authError.value = null
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            if (email.isBlank() || pass.isBlank()) {
                _authError.value = "Please enter email and password"
                return@launch
            }
            val res = repository.loginWithEmail(email, pass)
            res.onSuccess {
                _authError.value = null
                _authSuccessMessage.value = "Logged in as ${it.displayName}"
            }.onFailure {
                _authError.value = it.message ?: "Login failed"
            }
        }
    }

    fun signUp(email: String, pass: String, name: String, referralCode: String = "") {
        viewModelScope.launch {
            if (email.isBlank() || pass.isBlank()) {
                _authError.value = "Please fill in all fields"
                return@launch
            }
            if (pass.length < 6) {
                _authError.value = "Password must be at least 6 characters"
                return@launch
            }
            val res = repository.signUpWithEmail(email, pass, name, referralCode)
            res.onSuccess {
                _authError.value = null
                _authSuccessMessage.value = "Account created! Welcome ${it.displayName}"
            }.onFailure {
                _authError.value = it.message ?: "Sign up failed"
            }
        }
    }

    fun signInWithGoogle(activity: Activity, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            isGoogleAuthInProgress.value = true
            _authError.value = null
            try {
                val result = googleAuthManager.signInWithGoogle(activity)
                result.onSuccess { fbUser ->
                    val profile = repository.handleFirebaseAuthSuccess(fbUser)
                    _authError.value = null
                    val msg = "Welcome, ${profile.displayName}!"
                    _authSuccessMessage.value = msg
                    onComplete(true, msg)
                }.onFailure { err ->
                    val errorMsg = err.message ?: "Google Sign-In failed"
                    _authError.value = errorMsg
                    onComplete(false, errorMsg)
                }
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Unexpected error during Google Sign-In"
                _authError.value = errorMsg
                onComplete(false, errorMsg)
            } finally {
                isGoogleAuthInProgress.value = false
            }
        }
    }

    fun loginWithGoogleDemo(email: String, name: String) {
        viewModelScope.launch {
            repository.loginWithGoogle(email, name)
            _authSuccessMessage.value = "Signed in as $name"
        }
    }

    fun resetPassword(email: String) {
        val res = repository.sendPasswordReset(email)
        _authSuccessMessage.value = res.getOrNull()
    }

    fun logout() {
        repository.logout()
    }

    fun switchAccount(email: String) {
        repository.switchAccount(email)
    }

    // Admin methods
    fun adminUpdateRedemption(requestId: String, newStatus: String, adminNote: String) {
        repository.updateRedemptionStatus(requestId, newStatus, adminNote)
    }

    fun adminAddQuiz(quiz: QuizQuestion) {
        repository.addQuizQuestion(quiz)
    }

    fun adminUpdateQuiz(quiz: QuizQuestion) {
        repository.updateQuizQuestion(quiz)
    }

    fun adminDeleteQuiz(quizId: String) {
        repository.deleteQuizQuestion(quizId)
    }

    fun adminAddReward(reward: RewardItem) {
        repository.addRewardItem(reward)
    }

    fun adminUpdateReward(reward: RewardItem) {
        repository.updateRewardItem(reward)
    }

    fun adminDeleteReward(rewardId: String) {
        repository.deleteRewardItem(rewardId)
    }

    fun adminUpdateConfig(config: AppConfig) {
        repository.updateConfig(config)
    }

    fun adminToggleUserStatus(userId: String) {
        repository.toggleUserStatus(userId)
    }

    // Monthly Diamond Redemption Actions
    fun setMonthlyPlayerId(id: String) {
        monthlyPlayerIdInput.value = id
    }

    fun setMonthlyPoints(points: Long) {
        monthlySelectedPoints.value = points
    }

    fun clearMonthlyFeedback() {
        monthlyRedemptionFeedback.value = null
    }

    fun submitMonthlyDiamondRedemption(
        points: Long,
        playerId: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            monthlyRedemptionIsSubmitting.value = true
            monthlyRedemptionFeedback.value = null
            try {
                val result = repository.submitMonthlyDiamondRedemption(points, playerId)
                if (result.isSuccess) {
                    val red = result.getOrNull()!!
                    val msg = "Success! Redemption submitted (ID: ${red.id}). Deducted ${red.pointsRedeemed} pts for ${red.diamondAmount} Free Fire Diamonds. Remaining points stay safe in your wallet."
                    monthlyRedemptionFeedback.value = msg
                    onResult(true, msg)
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Redemption failed."
                    monthlyRedemptionFeedback.value = err
                    onResult(false, err)
                }
            } catch (e: Exception) {
                val err = e.message ?: "Redemption error occurred."
                monthlyRedemptionFeedback.value = err
                onResult(false, err)
            } finally {
                monthlyRedemptionIsSubmitting.value = false
            }
        }
    }

    fun dismissWelcomeBonusMessage() {
        repository.dismissWelcomeBonusMessage()
    }

    fun applyReferralCode(code: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = currentUser.value
            if (user == null) {
                onResult(false, "Please log in to apply a referral code.")
                return@launch
            }
            val res = repository.validateAndApplyReferral(user.userId, code)
            res.onSuccess { msg ->
                onResult(true, msg)
            }.onFailure { err ->
                onResult(false, err.message ?: "Failed to apply referral code.")
            }
        }
    }

    fun spinWheel(onResult: (SpinRewardResult) -> Unit, onError: (String) -> Unit) {
        if (isSpinning.value) return
        isSpinning.value = true
        spinFeedback.value = null

        viewModelScope.launch {
            val result = repository.spinWheel()
            result.onSuccess { spinRes ->
                onResult(spinRes)
            }.onFailure { err ->
                val errorMsg = err.message ?: "Spin failed"
                spinFeedback.value = errorMsg
                onError(errorMsg)
            }
            isSpinning.value = false
        }
    }

    fun watchAdForSpin(activity: Activity, onComplete: () -> Unit) {
        val daily = spinDaily.value
        val maxAdLimit = appConfig.value.dailyAdSpinLimit
        if (daily.adSpins >= maxAdLimit) {
            spinFeedback.value = "Daily extra spin limit ($maxAdLimit) reached. Come back tomorrow."
            return
        }

        if (adMobManager.isRunningOnEmulator()) {
            startAdSimulationForSpin(onComplete)
            return
        }

        if (adMobManager.isAdLoaded.value) {
            adMobManager.showRewardedAd(
                activity = activity,
                onUserEarnedReward = {
                    val res = repository.onRewardedAdSpinCompleted("Google AdMob")
                    res.onSuccess {
                        spinFeedback.value = "Ad completed! +1 Extra Spin granted 🎡"
                        onComplete()
                    }.onFailure { err ->
                        spinFeedback.value = err.message
                    }
                },
                onAdClosed = {
                    adMobManager.loadRewardedAd()
                },
                onAdFailed = {
                    startAdSimulationForSpin(onComplete)
                }
            )
        } else {
            adMobManager.loadRewardedAd()
            startAdSimulationForSpin(onComplete)
        }
    }

    private fun startAdSimulationForSpin(onComplete: () -> Unit) {
        _isShowingAdSimulation.value = true
        _adSimulationProgress.value = 0f
        _adCountdownSeconds.value = 10
        adSimulationJob?.cancel()
        adSimulationJob = viewModelScope.launch {
            for (sec in 10 downTo 1) {
                _adCountdownSeconds.value = sec
                _adSimulationProgress.value = (10 - sec) / 10f
                delay(1000)
            }
            _adSimulationProgress.value = 1f
            _adCountdownSeconds.value = 0
            _isShowingAdSimulation.value = false
            val res = repository.onRewardedAdSpinCompleted("Test Ad Player")
            res.onSuccess {
                spinFeedback.value = "Ad completed! +1 Extra Spin granted 🎡"
                onComplete()
            }.onFailure { err ->
                spinFeedback.value = err.message
            }
        }
    }

    fun updateConfig(config: AppConfig) {
        repository.updateConfig(config)
    }

    fun adminUpdateMonthlyRedemption(
        id: String,
        newStatus: String,
        providerTxId: String? = null,
        failureReason: String? = null,
        adminNote: String? = null
    ) {
        repository.adminUpdateMonthlyRedemption(id, newStatus, providerTxId, failureReason, adminNote)
    }

    fun adminSetTestServerDayOverride(day: Int?) {
        repository.adminSetTestServerDayOverride(day)
    }

    fun adminSetServerTimezone(timezone: String) {
        repository.adminSetServerTimezone(timezone)
    }
}

data class WalletBreakdown(
    val totalPoints: Long = 0L,
    val earnedFromQuizzes: Long = 0L,
    val earnedFromAds: Long = 0L,
    val earnedFromSpins: Long = 0L,
    val earnedFromWelcomeGift: Long = 0L,
    val earnedFromReferrals: Long = 0L,
    val redeemedPoints: Long = 0L,
    val pendingPoints: Long = 0L
)
