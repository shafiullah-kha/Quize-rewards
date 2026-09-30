package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.AdReward
import com.example.data.model.AppConfig
import com.example.data.model.LeaderboardEntry
import com.example.data.model.MonthlyRedemption
import com.example.data.model.MonthlyRedemptionWindowInfo
import com.example.data.model.QuizAttempt
import com.example.data.model.QuizQuestion
import com.example.data.model.RedemptionRequest
import com.example.data.model.Referral
import com.example.data.model.RewardItem
import com.example.data.model.SpinDaily
import com.example.data.model.SpinTransaction
import com.example.data.model.UserProfile
import com.example.data.model.WalletTransaction
import com.example.data.provider.TopupProviderManager
import com.example.data.time.ServerTimeManager
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class QuizRewardsRepository(private val context: Context) {
    companion object {
        private const val TAG = "QuizRewardsRepo"
        const val ADMIN_EMAIL = "shafihu394366@gmail.com"

        @Volatile
        private var INSTANCE: QuizRewardsRepository? = null

        fun getInstance(context: Context): QuizRewardsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: QuizRewardsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO)

    // Firebase instances (safely initialized)
    private var firebaseAuth: FirebaseAuth? = null
    private var firestore: FirebaseFirestore? = null

    // In-memory state flows for UI reactivity
    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    private val _quizzes = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizzes: StateFlow<List<QuizQuestion>> = _quizzes.asStateFlow()

    private val _rewards = MutableStateFlow<List<RewardItem>>(emptyList())
    val rewards: StateFlow<List<RewardItem>> = _rewards.asStateFlow()

    private val _transactions = MutableStateFlow<List<WalletTransaction>>(emptyList())
    val transactions: StateFlow<List<WalletTransaction>> = _transactions.asStateFlow()

    private val _myRedemptions = MutableStateFlow<List<RedemptionRequest>>(emptyList())
    val myRedemptions: StateFlow<List<RedemptionRequest>> = _myRedemptions.asStateFlow()

    private val _allRedemptions = MutableStateFlow<List<RedemptionRequest>>(emptyList())
    val allRedemptions: StateFlow<List<RedemptionRequest>> = _allRedemptions.asStateFlow()

    private val _allUsers = MutableStateFlow<List<UserProfile>>(emptyList())
    val allUsers: StateFlow<List<UserProfile>> = _allUsers.asStateFlow()

    private val _appConfig = MutableStateFlow(AppConfig())
    val appConfig: StateFlow<AppConfig> = _appConfig.asStateFlow()

    // Server Time and Top-up Provider Managers
    val serverTimeManager: ServerTimeManager = ServerTimeManager.getInstance()
    val topupProviderManager: TopupProviderManager = TopupProviderManager()

    // Monthly Diamond Redemption StateFlows
    private val _monthlyRedemptions = MutableStateFlow<List<MonthlyRedemption>>(emptyList())
    val monthlyRedemptions: StateFlow<List<MonthlyRedemption>> = _monthlyRedemptions.asStateFlow()

    private val _myMonthlyRedemptions = MutableStateFlow<List<MonthlyRedemption>>(emptyList())
    val myMonthlyRedemptions: StateFlow<List<MonthlyRedemption>> = _myMonthlyRedemptions.asStateFlow()

    private val _monthlyWindowInfo = MutableStateFlow(MonthlyRedemptionWindowInfo())
    val monthlyWindowInfo: StateFlow<MonthlyRedemptionWindowInfo> = _monthlyWindowInfo.asStateFlow()

    // Welcome Bonus In-App Notification Message
    private val _welcomeBonusMessage = MutableStateFlow<String?>(null)
    val welcomeBonusMessage: StateFlow<String?> = _welcomeBonusMessage.asStateFlow()

    // Referral System StateFlows
    private val _myReferrals = MutableStateFlow<List<Referral>>(emptyList())
    val myReferrals: StateFlow<List<Referral>> = _myReferrals.asStateFlow()

    private val _allReferrals = MutableStateFlow<List<Referral>>(emptyList())
    val allReferrals: StateFlow<List<Referral>> = _allReferrals.asStateFlow()

    // Spin & Earn StateFlows
    private val _spinDaily = MutableStateFlow(SpinDaily())
    val spinDaily: StateFlow<SpinDaily> = _spinDaily.asStateFlow()

    private val _spinHistory = MutableStateFlow<List<SpinTransaction>>(emptyList())
    val spinHistory: StateFlow<List<SpinTransaction>> = _spinHistory.asStateFlow()

    // Answered question IDs in current session/day to prevent abuse
    private val answeredQuestionIds = mutableSetOf<String>()

    init {
        initFirebase()
        seedInitialData()
        // If not logged in, provide a default guest or sign-in state
        if (_currentUser.value == null) {
            setupInitialSession()
        }
        scope.launch {
            serverTimeManager.syncWithServer()
            refreshMonthlyWindowInfo()
            refreshSpinDaily()
            refreshMyReferrals()
        }
    }

    private var firebaseStorage: FirebaseStorage? = null
    private var firebaseFunctions: FirebaseFunctions? = null

    private fun initFirebase() {
        try {
            val app = if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            } else {
                FirebaseApp.getInstance()
            }
            if (app != null) {
                firebaseAuth = FirebaseAuth.getInstance(app)
                firestore = FirebaseFirestore.getInstance(app)
                try {
                    firebaseStorage = FirebaseStorage.getInstance(app)
                } catch (e: Exception) {
                    Log.w(TAG, "Storage init warning: ${e.message}")
                }
                try {
                    firebaseFunctions = FirebaseFunctions.getInstance(app)
                } catch (e: Exception) {
                    Log.w(TAG, "Functions init warning: ${e.message}")
                }
                Log.d(TAG, "Firebase initialized with project: ${app.options.projectId}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization error: ${e.message}")
        }
    }

    private fun setupInitialSession() {
        val auth = firebaseAuth
        val currentFbUser = auth?.currentUser
        if (currentFbUser != null) {
            scope.launch {
                handleFirebaseAuthSuccess(currentFbUser)
            }
        } else {
            val admin = _allUsers.value.firstOrNull { it.role == "admin" }
            if (admin != null) {
                _currentUser.value = admin
            }
        }
    }

    private fun seedInitialUsers() {
        val today = getTodayKey()
        val adminUser = UserProfile(
            userId = "admin_user_01",
            displayName = "Admin Shafihu",
            email = ADMIN_EMAIL,
            photoURL = "",
            profileImage = "",
            country = "Pakistan",
            language = "English",
            points = 2500L,
            role = "admin",
            status = "active",
            referralCode = "QB-ADM777",
            referredBy = "",
            welcomeBonusAwarded = true,
            quizzesCompleted = 18,
            correctAnswers = 142,
            quizzesAnsweredToday = 3,
            adsWatchedToday = 2,
            lastActiveDate = today,
            totalQuizzesAnswered = 15,
            totalAdsWatched = 8
        )
        _allUsers.value = listOf(
            adminUser,
            UserProfile(
                userId = "gamer_boy_99",
                displayName = "Alex Hunter",
                email = "alex.hunter@example.com",
                country = "United States",
                language = "English",
                points = 2100L,
                role = "user",
                referralCode = "QB-ALX999",
                referredBy = "",
                welcomeBonusAwarded = true,
                quizzesCompleted = 15,
                correctAnswers = 118,
                quizzesAnsweredToday = 5,
                adsWatchedToday = 3,
                lastActiveDate = today
            ),
            UserProfile(
                userId = "priya_sharma",
                displayName = "Priya Sharma",
                email = "priya.sharma@example.com",
                country = "India",
                language = "Hindi",
                points = 1850L,
                role = "user",
                referralCode = "QB-PRI104",
                referredBy = "gamer_boy_99",
                welcomeBonusAwarded = true,
                quizzesCompleted = 14,
                correctAnswers = 104,
                quizzesAnsweredToday = 4,
                adsWatchedToday = 2,
                lastActiveDate = today
            ),
            UserProfile(
                userId = "liam_smith",
                displayName = "Liam Smith",
                email = "liam.smith@example.com",
                country = "United Kingdom",
                language = "English",
                points = 1450L,
                role = "user",
                referralCode = "QB-LIA221",
                referredBy = "admin_user_01",
                welcomeBonusAwarded = true,
                quizzesCompleted = 10,
                correctAnswers = 82,
                quizzesAnsweredToday = 3,
                adsWatchedToday = 1,
                lastActiveDate = today
            ),
            UserProfile(
                userId = "tariq_uae",
                displayName = "Tariq Al-Mansoor",
                email = "tariq.mansoor@example.com",
                country = "United Arab Emirates",
                language = "Arabic",
                points = 1320L,
                role = "user",
                referralCode = "QB-TRQ332",
                referredBy = "",
                welcomeBonusAwarded = true,
                quizzesCompleted = 9,
                correctAnswers = 71,
                quizzesAnsweredToday = 2,
                adsWatchedToday = 1,
                lastActiveDate = today
            ),
            UserProfile(
                userId = "ff_pro_player",
                displayName = "FireSniper",
                email = "firesniper@example.com",
                country = "Brazil",
                language = "English",
                points = 980L,
                role = "user",
                referralCode = "QB-FPS888",
                referredBy = "",
                welcomeBonusAwarded = true,
                quizzesCompleted = 7,
                correctAnswers = 50,
                quizzesAnsweredToday = 2,
                adsWatchedToday = 1,
                lastActiveDate = today
            )
        )
    }

    private fun getTodayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    private fun seedInitialData() {
        seedInitialUsers()
        // High quality Free Fire & Gaming Trivia quizzes
        val initialQuizzes = listOf(
            QuizQuestion(
                quizId = "q1",
                question = "What is the name of the classic island map in Free Fire?",
                options = listOf("Bermuda", "Erangel", "Kalahari", "Purgatory"),
                correctAnswer = 0,
                points = 10,
                category = "Free Fire",
                explanation = "Bermuda is the original and most famous battle royale map in Free Fire."
            ),
            QuizQuestion(
                quizId = "q2",
                question = "Which character has the ability 'Drop the Beat' which creates an aura restoring HP?",
                options = listOf("Chrono", "DJ Alok", "K", "Skyler"),
                correctAnswer = 1,
                points = 10,
                category = "Characters",
                explanation = "DJ Alok's 'Drop the Beat' creates a 5m aura that increases move speed and restores HP."
            ),
            QuizQuestion(
                quizId = "q3",
                question = "What type of weapon is the 'AWM' in Free Fire?",
                options = listOf("Shotgun", "Submachine Gun (SMG)", "Sniper Rifle", "Assault Rifle"),
                correctAnswer = 2,
                points = 10,
                category = "Weapons",
                explanation = "The AWM is a high-damage bolt-action sniper rifle found exclusively in airdrops."
            ),
            QuizQuestion(
                quizId = "q4",
                question = "Which throwable item creates a protective temporary shield wall in Free Fire?",
                options = listOf("Smoke Grenade", "Gloo Wall", "Flashbang", "Frag Grenade"),
                correctAnswer = 1,
                points = 15,
                category = "Tactics",
                explanation = "Gloo Walls are deployable defensive covers essential for close-range survival."
            ),
            QuizQuestion(
                quizId = "q5",
                question = "What is the maximum number of players in a standard Free Fire Battle Royale match?",
                options = listOf("100", "50", "64", "40"),
                correctAnswer = 1,
                points = 10,
                category = "General",
                explanation = "Standard Free Fire matches feature 50 players parachuting onto the island."
            ),
            QuizQuestion(
                quizId = "q6",
                question = "What is the name of the desert-themed map introduced to Free Fire?",
                options = listOf("Alpine", "Purgatory", "Kalahari", "NeXTerra"),
                correctAnswer = 2,
                points = 10,
                category = "Free Fire",
                explanation = "Kalahari is the arid desert map known for high vantage points like the Submarine."
            ),
            QuizQuestion(
                quizId = "q7",
                question = "Which pet in Free Fire detects enemies using medkits or inhalers within 30m?",
                options = listOf("Ottero", "Dreki", "Falco", "Rockie"),
                correctAnswer = 1,
                points = 15,
                category = "Pets",
                explanation = "Dreki uses 'Dragon Glare' to spot opponents healing within range."
            ),
            QuizQuestion(
                quizId = "q8",
                question = "How are real Free Fire Diamonds processed legitimately through Quiz Rewards?",
                options = listOf(
                    "Through automated diamond hacking tools",
                    "Through authorized top-up vouchers and legitimate admin processing",
                    "Through random number generation",
                    "Through modified APK files"
                ),
                correctAnswer = 1,
                points = 20,
                category = "Fair Play & Safety",
                explanation = "Quiz Rewards never uses fake generators. All rewards are legitimately purchased vouchers or authorized top-ups by the administrator."
            )
        )
        _quizzes.value = WorldwideQuestionsSeed.getInitialWorldwideQuestions() + initialQuizzes

        // Store Rewards - Exactly adhering to 1000 Points = 60 Free Fire Diamonds
        val initialRewards = listOf(
            RewardItem(
                rewardId = "rew_ff_60",
                title = "Free Fire Diamonds",
                description = "Legitimate 60 Diamonds top-up. 1000 Points = 60 Free Fire Diamonds. Delivered by admin via authorized voucher/player ID top-up.",
                diamonds = 60,
                requiredPoints = 1000L,
                category = "Free Fire",
                bannerIcon = "diamond",
                requiresPlayerId = true
            ),
            RewardItem(
                rewardId = "rew_ff_120",
                title = "Free Fire Diamonds (Double Pack)",
                description = "Legitimate 120 Diamonds top-up. 2000 Points = 120 Free Fire Diamonds. Delivered by admin via authorized voucher.",
                diamonds = 120,
                requiredPoints = 2000L,
                category = "Free Fire",
                bannerIcon = "diamond",
                requiresPlayerId = true
            ),
            RewardItem(
                rewardId = "rew_ff_180",
                title = "Free Fire Diamonds (Triple Pack)",
                description = "Legitimate 180 Diamonds bundle. 3000 Points = 180 Free Fire Diamonds. Fast authorized admin processing.",
                diamonds = 180,
                requiredPoints = 3000L,
                category = "Free Fire",
                bannerIcon = "diamond",
                requiresPlayerId = true
            ),
            RewardItem(
                rewardId = "rew_ff_300",
                title = "Free Fire Diamonds (Elite Pack)",
                description = "Special bundle of 300 Diamonds for 5000 Points. Authorized admin delivery.",
                diamonds = 300,
                requiredPoints = 5000L,
                category = "Free Fire",
                bannerIcon = "diamond",
                requiresPlayerId = true
            )
        )
        _rewards.value = initialRewards

        // Initial Transactions
        val now = System.currentTimeMillis()
        val initialTxs = listOf(
            WalletTransaction(
                transactionId = "tx_" + UUID.randomUUID().toString().take(8),
                userId = "admin_user_01",
                type = "QUIZ_EARN",
                points = 20L,
                source = "Quiz: Fair Play & Safety",
                referenceId = "q8",
                createdAt = now - 1800000
            ),
            WalletTransaction(
                transactionId = "tx_" + UUID.randomUUID().toString().take(8),
                userId = "admin_user_01",
                type = "AD_EARN",
                points = 25L,
                source = "Watched Rewarded Ad Bonus",
                referenceId = "ad_01",
                createdAt = now - 3600000
            ),
            WalletTransaction(
                transactionId = "tx_welcome_seed",
                userId = "admin_user_01",
                type = "WELCOME_GIFT",
                points = 100L,
                source = "🎁 Welcome Gift",
                referenceId = "welcome_admin",
                status = "COMPLETED",
                createdAt = now - 86400000
            ),
            WalletTransaction(
                transactionId = "tx_referral_seed",
                userId = "admin_user_01",
                type = "REFERRAL_BONUS",
                points = 100L,
                source = "👥 Referral Bonus (Liam Smith)",
                referenceId = "ref_liam",
                status = "COMPLETED",
                createdAt = now - 43200000
            ),
            WalletTransaction(
                transactionId = "tx_spin_seed",
                userId = "admin_user_01",
                type = "SPIN_REWARD",
                points = 80L,
                source = "🎡 Spin & Earn (Free Spin)",
                referenceId = "spin_seed_01",
                status = "COMPLETED",
                createdAt = now - 1800000
            ),
            WalletTransaction(
                transactionId = "tx_" + UUID.randomUUID().toString().take(8),
                userId = "admin_user_01",
                type = "ADMIN_ADJUST",
                points = 2175L,
                source = "Admin Welcome & Verification Bonus",
                referenceId = "init",
                createdAt = now - 86400000
            )
        )
        _transactions.value = initialTxs

        // Sample initial referrals
        val sampleReferrals = listOf(
            Referral(
                referralId = "ref_liam_smith",
                referrerUserId = "admin_user_01",
                referredUserId = "liam_smith",
                referralCode = "QB-ADM777",
                status = "REWARDED",
                rewardPoints = 100L,
                referredUserName = "Liam Smith",
                createdAt = now - 43200000,
                completedAt = now - 43200000
            ),
            Referral(
                referralId = "ref_priya_sharma",
                referrerUserId = "gamer_boy_99",
                referredUserId = "priya_sharma",
                referralCode = "QB-ALX999",
                status = "REWARDED",
                rewardPoints = 100L,
                referredUserName = "Priya Sharma",
                createdAt = now - 86400000,
                completedAt = now - 86400000
            )
        )
        _allReferrals.value = sampleReferrals
        refreshMyReferrals()

        // Sample initial spin daily & history
        val todayKey = getTodayKey()
        _spinDaily.value = SpinDaily(
            userId = "admin_user_01",
            date = todayKey,
            freeSpins = 3,
            adSpins = 0,
            extraSpinsAvailable = 0,
            totalSpins = 1,
            lastSpinAt = now - 1800000
        )
        _spinHistory.value = listOf(
            SpinTransaction(
                spinTransactionId = "spin_seed_01",
                userId = "admin_user_01",
                spinType = "FREE_SPIN",
                rewardPoints = 80L,
                status = "COMPLETED",
                createdAt = now - 1800000,
                source = "Spin & Earn"
            )
        )

        // Sample initial redemption requests for admin demo
        val initialRedemptions = listOf(
            RedemptionRequest(
                requestId = "REQ-FF-" + UUID.randomUUID().toString().take(6).uppercase(),
                userId = "gamer_boy_99",
                userEmail = "alex.hunter@example.com",
                userName = "Alex Hunter",
                rewardId = "rew_ff_60",
                rewardTitle = "Free Fire Diamonds",
                diamonds = 60,
                points = 1000L,
                status = "PENDING",
                recipientDetails = "Player ID: 294819024 (IGN: AlexHunter99)",
                createdAt = now - 7200000
            ),
            RedemptionRequest(
                requestId = "REQ-FF-COMPLETED",
                userId = "ff_pro_player",
                userEmail = "firesniper@example.com",
                userName = "FireSniper",
                rewardId = "rew_ff_60",
                rewardTitle = "Free Fire Diamonds",
                diamonds = 60,
                points = 1000L,
                status = "COMPLETED",
                recipientDetails = "Player ID: 104829104 (IGN: SniperGod)",
                adminNote = "Authorized Voucher PIN: FF99-K39B-810A sent successfully.",
                createdAt = now - 86400000,
                updatedAt = now - 43200000
            )
        )
        _allRedemptions.value = initialRedemptions
        _myRedemptions.value = emptyList() // current admin has not submitted redemption yet

        // Seed sample monthly redemptions adhering to monthlyRedemptions/{userId_YYYY_MM}
        val sampleMonthly = listOf(
            MonthlyRedemption(
                id = "gamer_boy_99_2026-08",
                userId = "gamer_boy_99",
                userName = "Alex Hunter",
                userEmail = "alex.hunter@example.com",
                month = "2026-08",
                pointsRedeemed = 1000L,
                diamondAmount = 60,
                playerId = "294819024",
                status = MonthlyRedemption.STATUS_COMPLETED,
                providerTransactionId = "TOPUP-VOUCHER-FF-882194",
                adminNote = "Delivered via official Garena voucher code.",
                createdAt = now - 2592000000L,
                processedAt = now - 2505600000L
            ),
            MonthlyRedemption(
                id = "priya_sharma_2026-09",
                userId = "priya_sharma",
                userName = "Priya Sharma",
                userEmail = "priya.sharma@example.com",
                month = "2026-09",
                pointsRedeemed = 2000L,
                diamondAmount = 120,
                playerId = "481920381",
                status = MonthlyRedemption.STATUS_PROCESSING,
                providerTransactionId = "PENDING_DISPATCH",
                adminNote = "Under review for legitimate player ID verification.",
                createdAt = now - 86400000L
            )
        )
        _monthlyRedemptions.value = sampleMonthly
        _myMonthlyRedemptions.value = emptyList()
    }

    // ==========================================
    // AUTHENTICATION & PROFILE PERSISTENCE
    // ==========================================

    suspend fun handleFirebaseAuthSuccess(user: FirebaseUser, referralCodeInput: String = ""): UserProfile {
        val uid = user.uid
        val email = user.email ?: "user_${uid.take(6)}@gmail.com"
        val name = user.displayName?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")
        val photo = user.photoUrl?.toString() ?: ""

        val profile = fetchOrCreateUserProfile(uid, email, name, photo, referralCodeInput)
        _currentUser.value = profile

        // Persist session locally to keep user signed in across app restarts
        try {
            context.getSharedPreferences("quiz_rewards_session", Context.MODE_PRIVATE)
                .edit()
                .putString("last_user_id", uid)
                .putString("last_email", email)
                .apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to persist session prefs: ${e.message}")
        }

        // Sync existing user data from Firestore
        loadUserDataFromFirestore(uid)
        refreshSpinDaily()
        refreshMyReferrals()

        return profile
    }

    suspend fun loginWithGoogle(email: String, name: String, photoUrl: String = ""): UserProfile {
        val uid = "google_" + kotlin.math.abs(email.hashCode()).toString()
        val profile = fetchOrCreateUserProfile(uid, email, name, photoUrl)
        _currentUser.value = profile
        try {
            context.getSharedPreferences("quiz_rewards_session", Context.MODE_PRIVATE)
                .edit()
                .putString("last_user_id", uid)
                .putString("last_email", email)
                .apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to persist session prefs: ${e.message}")
        }
        loadUserDataFromFirestore(uid)
        refreshSpinDaily()
        refreshMyReferrals()
        return profile
    }

    suspend fun loginWithEmail(email: String, pass: String): Result<UserProfile> {
        val auth = firebaseAuth ?: return Result.failure(Exception("Firebase Authentication is not available. Please check configuration."))
        return try {
            val result = auth.signInWithEmailAndPassword(email, pass).await()
            val fbUser = result.user ?: return Result.failure(Exception("Authentication succeeded but no user record was returned."))
            val profile = handleFirebaseAuthSuccess(fbUser)
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "loginWithEmail failed: ${e.message}", e)
            val friendly = when {
                e is FirebaseNetworkException -> "Network error. Please check your internet connection."
                e.message?.contains("no user record", ignoreCase = true) == true -> "No account found with this email. Please switch to Sign Up tab."
                e.message?.contains("password is invalid", ignoreCase = true) == true ||
                e.message?.contains("credential is incorrect", ignoreCase = true) == true -> "Incorrect password. Please try again or tap 'Forgot password?'."
                e.message?.contains("blocked", ignoreCase = true) == true -> "Access temporarily disabled due to too many failed attempts. Try again later."
                else -> e.localizedMessage ?: "Login failed. Please check your credentials."
            }
            Result.failure(Exception(friendly))
        }
    }

    suspend fun signUpWithEmail(email: String, pass: String, name: String, referralCodeInput: String = ""): Result<UserProfile> {
        val auth = firebaseAuth ?: return Result.failure(Exception("Firebase Authentication is not available. Please check configuration."))
        return try {
            val result = auth.createUserWithEmailAndPassword(email, pass).await()
            val fbUser = result.user ?: return Result.failure(Exception("Failed to create user in Firebase."))
            if (name.isNotBlank()) {
                try {
                    val update = UserProfileChangeRequest.Builder()
                        .setDisplayName(name)
                        .build()
                    fbUser.updateProfile(update).await()
                } catch (pe: Exception) {
                    Log.w(TAG, "Profile name update warning: ${pe.message}")
                }
            }
            val profile = handleFirebaseAuthSuccess(fbUser, referralCodeInput)
            Result.success(profile)
        } catch (e: Exception) {
            Log.e(TAG, "signUpWithEmail failed: ${e.message}", e)
            val friendly = when {
                e is FirebaseNetworkException -> "Network error. Please check your internet connection."
                e.message?.contains("email address is already in use", ignoreCase = true) == true -> "An account already exists with this email. Please Sign In instead."
                e.message?.contains("badly formatted", ignoreCase = true) == true -> "Invalid email address format. Please enter a valid email."
                e.message?.contains("at least 6 characters", ignoreCase = true) == true -> "Password is too weak. Must be at least 6 characters."
                else -> e.localizedMessage ?: "Sign up failed. Please try again."
            }
            Result.failure(Exception(friendly))
        }
    }

    fun switchAccount(targetEmail: String) {
        val user = _allUsers.value.find { it.email.equals(targetEmail, ignoreCase = true) }
            ?: UserProfile(
                userId = "usr_" + targetEmail.hashCode(),
                displayName = targetEmail.substringBefore("@"),
                email = targetEmail,
                points = 1000L,
                role = if (targetEmail.equals(ADMIN_EMAIL, ignoreCase = true)) "admin" else "user",
                status = "active",
                referralCode = generateUniqueReferralCode()
            )
        _currentUser.value = user
        refreshMyReferrals()
        refreshSpinDaily()
    }

    fun logout() {
        try {
            firebaseAuth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Sign out error", e)
        }
        try {
            context.getSharedPreferences("quiz_rewards_session", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()
        } catch (e: Exception) {
            Log.w(TAG, "Clear session prefs warning: ${e.message}")
        }
        _currentUser.value = null
        _myReferrals.value = emptyList()
    }

    fun sendPasswordReset(email: String): Result<String> {
        val auth = firebaseAuth ?: return Result.failure(Exception("Firebase Auth not available"))
        return try {
            auth.sendPasswordResetEmail(email)
            Result.success("Password reset instructions sent to $email")
        } catch (e: Exception) {
            Log.w(TAG, "Password reset error", e)
            val friendly = if (e is FirebaseNetworkException) "Network error. Check connection." else (e.localizedMessage ?: "Failed to send reset link.")
            Result.failure(Exception(friendly))
        }
    }

    fun generateUniqueReferralCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        val codeSuffix = (1..6).map { chars.random() }.joinToString("")
        return "QB-$codeSuffix"
    }

    fun saveUserProfile(profile: UserProfile) {
        if (_currentUser.value?.userId == profile.userId) {
            _currentUser.value = profile
        }
        val exists = _allUsers.value.any { it.userId == profile.userId }
        _allUsers.value = if (exists) {
            _allUsers.value.map { if (it.userId == profile.userId) profile else it }
        } else {
            _allUsers.value + profile
        }
        try {
            val data = mapOf(
                "uid" to profile.userId,
                "userId" to profile.userId,
                "displayName" to profile.displayName,
                "email" to profile.email,
                "photoURL" to profile.photoURL,
                "profileImage" to profile.effectiveProfileImage,
                "country" to profile.country,
                "language" to profile.language,
                "points" to profile.points,
                "role" to profile.role,
                "status" to profile.status,
                "referralCode" to profile.referralCode,
                "referredBy" to profile.referredBy,
                "welcomeBonusAwarded" to profile.welcomeBonusAwarded,
                "lastLoginAt" to profile.lastLoginAt,
                "quizzesCompleted" to profile.quizzesCompleted,
                "correctAnswers" to profile.correctAnswers,
                "quizzesAnsweredToday" to profile.quizzesAnsweredToday,
                "adsWatchedToday" to profile.adsWatchedToday,
                "totalQuizzesAnswered" to profile.totalQuizzesAnswered,
                "totalAdsWatched" to profile.totalAdsWatched,
                "lastActiveDate" to profile.lastActiveDate,
                "createdAt" to profile.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )
            firestore?.collection("users")?.document(profile.userId)?.set(data, SetOptions.merge())
        } catch (e: Exception) {
            Log.w(TAG, "Firestore user profile save error: ${e.message}")
        }
    }

    fun dismissWelcomeBonusMessage() {
        _welcomeBonusMessage.value = null
    }

    fun triggerWelcomeBonusJob(userId: String) {
        scope.launch {
            // Short server-controlled delay as strictly required (Starts at 0, then credits +100)
            delay(2500)
            awardWelcomeBonus(userId)
        }
    }

    suspend fun awardWelcomeBonus(userId: String): Boolean {
        val user = _allUsers.value.find { it.userId == userId }
            ?: _currentUser.value?.takeIf { it.userId == userId }
            ?: return false

        if (user.welcomeBonusAwarded) {
            Log.d(TAG, "Welcome bonus already claimed for $userId")
            return false
        }

        // Idempotency check via Firestore welcomeRewards collection
        try {
            val doc = firestore?.collection("welcomeRewards")?.document(userId)?.get()?.await()
            if (doc != null && doc.exists()) {
                Log.d(TAG, "welcomeRewards document already exists for $userId")
                return false
            }
        } catch (e: Exception) {
            Log.w(TAG, "welcomeRewards check fallback: ${e.message}")
        }

        val bonusPts = _appConfig.value.welcomeBonusPoints
        val updatedUser = user.copy(
            points = user.points + bonusPts,
            welcomeBonusAwarded = true,
            updatedAt = System.currentTimeMillis()
        )
        if (_currentUser.value?.userId == userId) {
            _currentUser.value = updatedUser
        }
        saveUserProfile(updatedUser)

        // Create idempotent welcome reward record
        try {
            val record = mapOf(
                "userId" to userId,
                "points" to bonusPts,
                "status" to "COMPLETED",
                "awardedAt" to System.currentTimeMillis()
            )
            firestore?.collection("welcomeRewards")?.document(userId)?.set(record)
        } catch (e: Exception) {
            Log.w(TAG, "welcomeRewards save fallback: ${e.message}")
        }

        // Create transaction: type = "WELCOME_GIFT"
        val tx = WalletTransaction(
            transactionId = "tx_welcome_${userId.take(8)}_${UUID.randomUUID().toString().take(4)}",
            userId = userId,
            type = "WELCOME_GIFT",
            points = bonusPts,
            source = "🎁 Welcome Gift",
            referenceId = "welcome_$userId",
            status = "COMPLETED",
            createdAt = System.currentTimeMillis()
        )
        recordTransaction(tx)

        _welcomeBonusMessage.value = "Welcome Gift 🎁 +$bonusPts Points added!"
        return true
    }

    suspend fun validateAndApplyReferral(newUserId: String, referralCodeInput: String): Result<String> {
        val cleanCode = referralCodeInput.trim().uppercase()
        if (cleanCode.isBlank()) return Result.failure(Exception("Referral code cannot be blank"))

        // 1. Verify new user exists and is not already referred
        val newUser = _allUsers.value.find { it.userId == newUserId }
            ?: _currentUser.value?.takeIf { it.userId == newUserId }
            ?: return Result.failure(Exception("New user account not found"))

        if (newUser.referredBy.isNotBlank()) {
            return Result.failure(Exception("This account has already used a referral code."))
        }

        // 2. Find referrer by referral code
        val referrer = _allUsers.value.find { it.referralCode.equals(cleanCode, ignoreCase = true) }
            ?: return Result.failure(Exception("Invalid referral code ($cleanCode). Please verify and try again."))

        // 3. Ensure referrer is not the same user
        if (referrer.userId == newUserId || (referrer.email.isNotBlank() && referrer.email.equals(newUser.email, ignoreCase = true))) {
            return Result.failure(Exception("Self-referral is not allowed."))
        }

        // 4. Ensure same new user has not already been referred
        val existingRef = _allReferrals.value.find { it.referredUserId == newUserId }
        if (existingRef != null && existingRef.status == "REWARDED") {
            return Result.failure(Exception("Referral reward has already been credited for this account."))
        }

        // 5. Award referral bonus to referrer
        val referralPts = _appConfig.value.referralBonusPoints
        val updatedReferrer = referrer.copy(
            points = referrer.points + referralPts,
            updatedAt = System.currentTimeMillis()
        )
        saveUserProfile(updatedReferrer)

        // 6. Update new user profile with referredBy
        val updatedNewUser = newUser.copy(
            referredBy = referrer.userId,
            updatedAt = System.currentTimeMillis()
        )
        if (_currentUser.value?.userId == newUserId) {
            _currentUser.value = updatedNewUser
        }
        saveUserProfile(updatedNewUser)

        // 7. Create referral record in referrals/{referralId}
        val refId = "ref_${newUserId.take(12)}"
        val referralRecord = Referral(
            referralId = refId,
            referrerUserId = referrer.userId,
            referredUserId = newUserId,
            referralCode = cleanCode,
            status = "REWARDED",
            rewardPoints = referralPts,
            referredUserName = newUser.displayName.ifBlank { "New Player" },
            createdAt = System.currentTimeMillis(),
            completedAt = System.currentTimeMillis()
        )
        _allReferrals.value = listOf(referralRecord) + _allReferrals.value.filterNot { it.referralId == refId }
        refreshMyReferrals()

        try {
            firestore?.collection("referrals")?.document(refId)?.set(referralRecord)
        } catch (e: Exception) {
            Log.w(TAG, "Referral firestore fallback", e)
        }

        // 8. Create wallet transaction for referrer
        val refTx = WalletTransaction(
            transactionId = "tx_ref_${UUID.randomUUID().toString().take(8)}",
            userId = referrer.userId,
            type = "REFERRAL_BONUS",
            points = referralPts,
            source = "👥 Referral Bonus (${newUser.displayName.ifBlank { "New Player" }})",
            referenceId = refId,
            status = "COMPLETED",
            createdAt = System.currentTimeMillis()
        )
        recordTransaction(refTx)

        return Result.success("Referral code accepted! Referrer awarded +$referralPts Points.")
    }

    fun refreshMyReferrals() {
        val uid = _currentUser.value?.userId ?: return
        val list = _allReferrals.value.filter { it.referrerUserId == uid }
        _myReferrals.value = list
    }

    fun refreshSpinDaily() {
        val uid = _currentUser.value?.userId ?: return
        val today = serverTimeManager.getTodayKey()
        val current = _spinDaily.value
        if (current.userId == uid && current.date == today) {
            return
        }

        val docId = "${uid}_$today"
        scope.launch {
            try {
                val doc = firestore?.collection("spinDaily")?.document(docId)?.get()?.await()
                if (doc != null && doc.exists()) {
                    val sp = doc.toObject(SpinDaily::class.java)
                    if (sp != null) {
                        _spinDaily.value = sp
                        return@launch
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Load spinDaily fallback: ${e.message}")
            }

            _spinDaily.value = SpinDaily(
                userId = uid,
                date = today,
                freeSpins = _appConfig.value.dailyFreeSpins,
                adSpins = 0,
                extraSpinsAvailable = 0,
                totalSpins = 0,
                lastSpinAt = 0L,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    suspend fun spinWheel(): Result<SpinRewardResult> {
        val user = _currentUser.value ?: return Result.failure(Exception("Please log in to spin the wheel"))
        if (user.status == "suspended") {
            return Result.failure(Exception("Account is suspended due to fair-play violations."))
        }

        val today = serverTimeManager.getTodayKey()
        var currentDaily = _spinDaily.value
        if (currentDaily.userId != user.userId || currentDaily.date != today) {
            currentDaily = SpinDaily(
                userId = user.userId,
                date = today,
                freeSpins = _appConfig.value.dailyFreeSpins,
                adSpins = 0,
                extraSpinsAvailable = 0,
                totalSpins = 0
            )
        }

        val spinType: String
        val newFreeSpins: Int
        val newExtraAvailable: Int
        val newAdSpinsUsed: Int

        if (currentDaily.freeSpins > 0) {
            spinType = "FREE_SPIN"
            newFreeSpins = currentDaily.freeSpins - 1
            newExtraAvailable = currentDaily.extraSpinsAvailable
            newAdSpinsUsed = currentDaily.adSpins
        } else if (currentDaily.extraSpinsAvailable > 0) {
            spinType = "REWARDED_AD_SPIN"
            newFreeSpins = 0
            newExtraAvailable = currentDaily.extraSpinsAvailable - 1
            newAdSpinsUsed = currentDaily.adSpins + 1
        } else {
            return Result.failure(Exception("No spins remaining. Watch a rewarded video to get more spins!"))
        }

        // Server-authorized points: 20, 30, 40, 50, 60, 70, 80, 90, 100
        val rewardOptions = listOf(20L, 30L, 40L, 50L, 60L, 70L, 80L, 90L, 100L)
        val selectedPoints = rewardOptions.random()

        val spinTxId = "spin_" + UUID.randomUUID().toString().take(8)
        val now = System.currentTimeMillis()

        // 1. Update SpinDaily
        val updatedDaily = currentDaily.copy(
            freeSpins = newFreeSpins,
            extraSpinsAvailable = newExtraAvailable,
            adSpins = newAdSpinsUsed,
            totalSpins = currentDaily.totalSpins + 1,
            lastSpinAt = now,
            updatedAt = now
        )
        _spinDaily.value = updatedDaily

        // 2. Save spin transaction
        val spinTx = SpinTransaction(
            spinTransactionId = spinTxId,
            userId = user.userId,
            spinType = spinType,
            rewardPoints = selectedPoints,
            status = "COMPLETED",
            createdAt = now,
            source = "Spin & Earn"
        )
        _spinHistory.value = listOf(spinTx) + _spinHistory.value

        // 3. Atomically update wallet points
        val updatedUser = user.copy(
            points = user.points + selectedPoints,
            updatedAt = now
        )
        _currentUser.value = updatedUser
        saveUserProfile(updatedUser)

        // 4. Create wallet transaction
        val walletTx = WalletTransaction(
            transactionId = "tx_" + spinTxId,
            userId = user.userId,
            type = "SPIN_REWARD",
            points = selectedPoints,
            source = "🎡 Spin & Earn (${if (spinType == "FREE_SPIN") "Free Spin" else "Ad Spin"})",
            referenceId = spinTxId,
            status = "COMPLETED",
            createdAt = now
        )
        recordTransaction(walletTx)

        // 5. Persist to Firestore
        try {
            val docId = "${user.userId}_$today"
            firestore?.collection("spinDaily")?.document(docId)?.set(updatedDaily)
            firestore?.collection("spinTransactions")?.document(spinTxId)?.set(spinTx)
        } catch (e: Exception) {
            Log.w(TAG, "Spin persistence fallback: ${e.message}")
        }

        return Result.success(
            SpinRewardResult(
                points = selectedPoints,
                spinType = spinType,
                freeSpinsLeft = newFreeSpins,
                extraSpinsLeft = newExtraAvailable,
                adSpinsUsedToday = newAdSpinsUsed,
                maxAdSpins = _appConfig.value.dailyAdSpinLimit,
                transactionId = spinTxId
            )
        )
    }

    fun onRewardedAdSpinCompleted(source: String = "Google AdMob"): Result<Int> {
        val user = _currentUser.value ?: return Result.failure(Exception("Please log in to claim spins"))
        if (user.status == "suspended") {
            return Result.failure(Exception("Account is suspended"))
        }

        val today = serverTimeManager.getTodayKey()
        var currentDaily = _spinDaily.value
        if (currentDaily.userId != user.userId || currentDaily.date != today) {
            currentDaily = SpinDaily(
                userId = user.userId,
                date = today,
                freeSpins = _appConfig.value.dailyFreeSpins,
                adSpins = 0,
                extraSpinsAvailable = 0,
                totalSpins = 0
            )
        }

        val maxAdLimit = _appConfig.value.dailyAdSpinLimit
        if (currentDaily.adSpins >= maxAdLimit) {
            return Result.failure(Exception("Daily extra spin limit ($maxAdLimit) reached. Come back tomorrow!"))
        }

        val updatedDaily = currentDaily.copy(
            extraSpinsAvailable = currentDaily.extraSpinsAvailable + 1,
            updatedAt = System.currentTimeMillis()
        )
        _spinDaily.value = updatedDaily

        try {
            val docId = "${user.userId}_$today"
            firestore?.collection("spinDaily")?.document(docId)?.set(updatedDaily)
        } catch (e: Exception) {
            Log.w(TAG, "Save spinDaily extra spin fallback", e)
        }

        return Result.success(updatedDaily.extraSpinsAvailable)
    }

    private suspend fun fetchOrCreateUserProfile(
        uid: String,
        email: String,
        name: String,
        photoUrl: String = "",
        referralCodeInput: String = ""
    ): UserProfile {
        return try {
            // 1. Check existing document by UID
            val doc = firestore?.collection("users")?.document(uid)?.get()?.await()
            if (doc != null && doc.exists()) {
                val existing = doc.toObject(UserProfile::class.java)
                if (existing != null) {
                    val updated = existing.copy(
                        userId = uid,
                        lastLoginAt = System.currentTimeMillis(),
                        displayName = if (existing.displayName.isNotBlank()) existing.displayName else name,
                        photoURL = if (existing.photoURL.isNotBlank()) existing.photoURL else photoUrl,
                        profileImage = if (existing.profileImage.isNotBlank()) existing.profileImage else photoUrl,
                        email = if (existing.email.isNotBlank()) existing.email else email,
                        updatedAt = System.currentTimeMillis()
                    )
                    // Keep existing points, referralCode, transactions, history strictly intact
                    saveUserProfile(updated)

                    val list = _allUsers.value.toMutableList()
                    val idx = list.indexOfFirst { it.userId == uid }
                    if (idx >= 0) list[idx] = updated else list.add(updated)
                    _allUsers.value = list
                    return updated
                }
            }

            // 2. Check if user already exists with this email under prior registration
            if (email.isNotBlank()) {
                val emailQuery = firestore?.collection("users")
                    ?.whereEqualTo("email", email)
                    ?.limit(1)
                    ?.get()
                    ?.await()
                if (emailQuery != null && !emailQuery.isEmpty) {
                    val existingDoc = emailQuery.documents.first()
                    val existing = existingDoc.toObject(UserProfile::class.java)
                    if (existing != null) {
                        val targetUid = if (existing.userId.isNotBlank()) existing.userId else uid
                        val updated = existing.copy(
                            userId = targetUid,
                            lastLoginAt = System.currentTimeMillis(),
                            displayName = if (existing.displayName.isNotBlank()) existing.displayName else name,
                            photoURL = if (existing.photoURL.isNotBlank()) existing.photoURL else photoUrl,
                            profileImage = if (existing.profileImage.isNotBlank()) existing.profileImage else photoUrl,
                            updatedAt = System.currentTimeMillis()
                        )
                        saveUserProfile(updated)
                        val list = _allUsers.value.toMutableList()
                        val idx = list.indexOfFirst { it.userId == targetUid }
                        if (idx >= 0) list[idx] = updated else list.add(updated)
                        _allUsers.value = list
                        return updated
                    }
                }
            }

            // 3. New user registration: starts at 0 points as strictly specified!
            val referralCode = generateUniqueReferralCode()
            val newProfile = UserProfile(
                userId = uid,
                displayName = name.ifBlank { email.substringBefore("@") },
                email = email,
                photoURL = photoUrl,
                profileImage = photoUrl,
                points = 0L, // Initial balance 0 Points (NOT 100 or 200 immediately)
                role = if (email.equals(ADMIN_EMAIL, ignoreCase = true)) "admin" else "user",
                status = "active",
                referralCode = referralCode,
                referredBy = "",
                welcomeBonusAwarded = false,
                lastLoginAt = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                lastActiveDate = getTodayKey()
            )
            saveUserProfile(newProfile)

            val list = _allUsers.value.toMutableList()
            list.add(newProfile)
            _allUsers.value = list

            // Validate referral relationship if a code was provided during registration
            if (referralCodeInput.isNotBlank()) {
                scope.launch {
                    validateAndApplyReferral(uid, referralCodeInput)
                }
            }

            // Automatically schedule server-controlled welcome bonus (+100 Points after delay)
            triggerWelcomeBonusJob(uid)

            newProfile
        } catch (e: Exception) {
            Log.w(TAG, "fetchOrCreateUserProfile fallback: ${e.message}")
            val existing = _allUsers.value.find { it.userId == uid || it.email.equals(email, ignoreCase = true) }
            if (existing != null) {
                existing.copy(lastLoginAt = System.currentTimeMillis())
            } else {
                val newP = UserProfile(
                    userId = uid,
                    displayName = name,
                    email = email,
                    photoURL = photoUrl,
                    profileImage = photoUrl,
                    points = 0L,
                    role = if (email.equals(ADMIN_EMAIL, ignoreCase = true)) "admin" else "user",
                    referralCode = generateUniqueReferralCode(),
                    referredBy = "",
                    welcomeBonusAwarded = false,
                    lastLoginAt = System.currentTimeMillis()
                )
                val list = _allUsers.value.toMutableList()
                list.add(newP)
                _allUsers.value = list
                if (referralCodeInput.isNotBlank()) {
                    scope.launch { validateAndApplyReferral(uid, referralCodeInput) }
                }
                triggerWelcomeBonusJob(uid)
                newP
            }
        }
    }

    private fun loadUserDataFromFirestore(uid: String) {
        val fs = firestore ?: return
        scope.launch {
            try {
                // Load user transactions
                val txDocs = fs.collection("transactions")
                    .whereEqualTo("userId", uid)
                    .get()
                    .await()
                if (!txDocs.isEmpty) {
                    val userTxs = txDocs.toObjects(WalletTransaction::class.java)
                    val merged = (_transactions.value + userTxs).distinctBy { it.transactionId }
                    _transactions.value = merged
                }
            } catch (e: Exception) {
                Log.w(TAG, "Load transactions for $uid error: ${e.message}")
            }

            try {
                // Load user monthly redemptions
                val monthlyDocs = fs.collection("monthlyRedemptions")
                    .whereEqualTo("userId", uid)
                    .get()
                    .await()
                if (!monthlyDocs.isEmpty) {
                    val userMonthly = monthlyDocs.toObjects(MonthlyRedemption::class.java)
                    _myMonthlyRedemptions.value = userMonthly
                }
            } catch (e: Exception) {
                Log.w(TAG, "Load monthly redemptions for $uid error: ${e.message}")
            }

            try {
                // Load user referrals
                val refDocs = fs.collection("referrals")
                    .whereEqualTo("referrerUserId", uid)
                    .get()
                    .await()
                if (!refDocs.isEmpty) {
                    val userRefs = refDocs.toObjects(Referral::class.java)
                    val mergedRefs = (_allReferrals.value + userRefs).distinctBy { it.referralId }
                    _allReferrals.value = mergedRefs
                    refreshMyReferrals()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Load referrals error: ${e.message}")
            }

            try {
                // Load user spin history
                val spinDocs = fs.collection("spinTransactions")
                    .whereEqualTo("userId", uid)
                    .get()
                    .await()
                if (!spinDocs.isEmpty) {
                    val userSpins = spinDocs.toObjects(SpinTransaction::class.java)
                    val mergedSpins = (_spinHistory.value + userSpins).distinctBy { it.spinTransactionId }
                    _spinHistory.value = mergedSpins
                }
            } catch (e: Exception) {
                Log.w(TAG, "Load spin history error: ${e.message}")
            }
        }
    }

    // ==========================================
    // QUIZ SYSTEM & ANTI-ABUSE
    // ==========================================

    fun isQuestionAnswered(quizId: String): Boolean {
        return answeredQuestionIds.contains(quizId)
    }

    fun submitQuizAnswer(quiz: QuizQuestion, selectedOptionIndex: Int): QuizAnswerResult {
        val user = _currentUser.value ?: return QuizAnswerResult(false, 0, 0, "", "Not logged in")

        if (user.status == "suspended") {
            return QuizAnswerResult(false, 0, 0, "", "Your account is suspended due to fair-play violation.")
        }

        if (answeredQuestionIds.contains(quiz.quizId)) {
            return QuizAnswerResult(false, 0, 0, "", "You have already answered this question in this session.")
        }

        val isCorrect = selectedOptionIndex == quiz.correctAnswer
        val earnedPoints = if (isCorrect) quiz.points else 0

        // Mark as answered in this session
        answeredQuestionIds.add(quiz.quizId)

        if (isCorrect) {
            val updatedPoints = user.points + earnedPoints
            val updatedUser = user.copy(
                points = updatedPoints,
                correctAnswers = user.correctAnswers + 1,
                quizzesAnsweredToday = user.quizzesAnsweredToday + 1,
                totalQuizzesAnswered = user.totalQuizzesAnswered + 1,
                updatedAt = System.currentTimeMillis()
            )
            _currentUser.value = updatedUser
            saveUserProfile(updatedUser)

            // Record transaction
            val tx = WalletTransaction(
                transactionId = "tx_quiz_" + UUID.randomUUID().toString().take(8),
                userId = user.userId,
                type = "quiz",
                points = earnedPoints.toLong(),
                source = "Quiz: ${quiz.question.take(28)}...",
                referenceId = quiz.quizId,
                createdAt = System.currentTimeMillis()
            )
            recordTransaction(tx)
        } else {
            val updatedUser = user.copy(
                quizzesAnsweredToday = user.quizzesAnsweredToday + 1,
                totalQuizzesAnswered = user.totalQuizzesAnswered + 1,
                updatedAt = System.currentTimeMillis()
            )
            _currentUser.value = updatedUser
            saveUserProfile(updatedUser)
        }

        return QuizAnswerResult(
            isCorrect = isCorrect,
            pointsEarned = earnedPoints,
            correctOptionIndex = quiz.correctAnswer,
            explanation = quiz.explanation
        )
    }

    fun recordQuizAttempt(attempt: QuizAttempt) {
        val user = _currentUser.value ?: return
        val updatedUser = user.copy(
            quizzesCompleted = user.quizzesCompleted + 1,
            correctAnswers = user.correctAnswers + attempt.correctAnswers,
            updatedAt = System.currentTimeMillis()
        )
        _currentUser.value = updatedUser
        saveUserProfile(updatedUser)

        try {
            firestore?.collection("quizAttempts")?.document(attempt.attemptId)?.set(attempt)
        } catch (e: Exception) {
            Log.w(TAG, "Quiz attempt save fallback", e)
        }
    }

    fun getQuestionsForQuiz(
        mode: String,
        selectedCategory: String,
        selectedCountry: String,
        selectedLanguage: String,
        difficulty: String = "All",
        limit: Int = 10
    ): List<QuizQuestion> {
        val allActive = _quizzes.value.filter { it.active }
        val filtered = when (mode) {
            "Daily Quiz" -> {
                val seed = getTodayKey().hashCode()
                allActive.shuffled(java.util.Random(seed.toLong()))
            }
            "Category Quiz" -> {
                if (selectedCategory == "All" || selectedCategory == "Mixed Worldwide Trivia") {
                    allActive
                } else {
                    allActive.filter { it.category.equals(selectedCategory, ignoreCase = true) }
                }
            }
            "Country Quiz" -> {
                if (selectedCountry == "Global") {
                    allActive
                } else {
                    allActive.filter {
                        it.country.equals(selectedCountry, ignoreCase = true) ||
                                it.category.equals(selectedCountry, ignoreCase = true)
                    }
                }
            }
            "Challenge Quiz" -> {
                allActive.filter { it.difficulty.equals("Hard", ignoreCase = true) }
            }
            else -> { // Quick Quiz, Mixed Quiz
                allActive.shuffled()
            }
        }

        val diffFiltered = if (difficulty != "All") {
            val matching = filtered.filter { it.difficulty.equals(difficulty, ignoreCase = true) }
            if (matching.size >= limit) matching else filtered
        } else filtered

        val langFiltered = if (selectedLanguage.isNotBlank() && selectedLanguage != "All") {
            val matching = diffFiltered.filter { it.language.equals(selectedLanguage, ignoreCase = true) }
            if (matching.size >= limit) matching else diffFiltered
        } else diffFiltered

        // Prefer unanswered in session
        val unAnswered = langFiltered.filter { !answeredQuestionIds.contains(it.quizId) }
        return if (unAnswered.size >= limit) {
            unAnswered.take(limit)
        } else if (langFiltered.isNotEmpty()) {
            langFiltered.take(limit)
        } else {
            allActive.take(limit)
        }
    }

    fun bulkImportQuestions(rawText: String, format: String): BulkImportResult {
        val result = QuestionBulkImporter.parseAndValidate(
            rawText = rawText,
            format = format,
            existingQuestions = _quizzes.value,
            allowedCategories = _appConfig.value.categories
        )
        if (result.validatedQuestions.isNotEmpty()) {
            _quizzes.value = _quizzes.value + result.validatedQuestions
            try {
                val batch = firestore?.batch()
                if (batch != null) {
                    result.validatedQuestions.take(500).forEach { q ->
                        val docRef = firestore?.collection("questions")?.document(q.quizId)
                        if (docRef != null) batch.set(docRef, q)
                    }
                    batch.commit()
                }
            } catch (e: Exception) {
                Log.w(TAG, "Bulk import firestore fallback", e)
            }
        }
        return result
    }

    fun getLeaderboard(period: String): List<LeaderboardEntry> {
        val users = _allUsers.value.sortedByDescending { it.points }
        val pointsMultiplier = when (period) {
            "Daily" -> 0.15f
            "Weekly" -> 0.45f
            "Monthly" -> 0.8f
            else -> 1.0f
        }
        return users.mapIndexed { index: Int, u: UserProfile ->
            val flag = when (u.country) {
                "United States" -> "🇺🇸"
                "United Kingdom" -> "🇬🇧"
                "India" -> "🇮🇳"
                "Pakistan" -> "🇵🇰"
                "Canada" -> "🇨🇦"
                "Australia" -> "🇦🇺"
                "Germany" -> "🇩🇪"
                "France" -> "🇫🇷"
                "United Arab Emirates" -> "🇦🇪"
                "Saudi Arabia" -> "🇸🇦"
                "Brazil" -> "🇧🇷"
                "Indonesia" -> "🇮🇩"
                "Japan" -> "🇯🇵"
                else -> "🌍"
            }
            LeaderboardEntry(
                rank = index + 1,
                userId = u.userId,
                displayName = u.displayName,
                country = u.country,
                points = (u.points * pointsMultiplier).toLong().coerceAtLeast(50L),
                quizzesCompleted = (u.quizzesCompleted * pointsMultiplier).toInt().coerceAtLeast(1),
                flagEmoji = flag
            )
        }
    }

    fun updateUserPreferences(country: String, language: String) {
        val user = _currentUser.value ?: return
        val updated = user.copy(
            country = country,
            language = language,
            updatedAt = System.currentTimeMillis()
        )
        _currentUser.value = updated
        saveUserProfile(updated)
    }

    fun resetSessionQuestions() {
        answeredQuestionIds.clear()
    }

    // ==========================================
    // REWARDED ADS
    // ==========================================

    fun canWatchRewardedAd(): Boolean {
        val user = _currentUser.value ?: return false
        val limit = _appConfig.value.dailyAdLimit
        return user.adsWatchedToday < limit && user.status != "suspended"
    }

    fun onRewardedAdCompleted(adProvider: String = "Google AdMob"): Result<Int> {
        val user = _currentUser.value ?: return Result.failure(Exception("User not authenticated"))

        if (user.status == "suspended") {
            return Result.failure(Exception("Account is suspended"))
        }

        val limit = _appConfig.value.dailyAdLimit
        if (user.adsWatchedToday >= limit) {
            return Result.failure(Exception("Daily rewarded ad limit of $limit reached. Come back tomorrow!"))
        }

        val bonusPoints = _appConfig.value.rewardedAdPoints
        val today = getTodayKey()
        val txId = "tx_ad_" + UUID.randomUUID().toString().take(8)

        // 1. Update wallet points securely
        val updatedUser = user.copy(
            points = user.points + bonusPoints,
            adsWatchedToday = user.adsWatchedToday + 1,
            totalAdsWatched = user.totalAdsWatched + 1,
            updatedAt = System.currentTimeMillis()
        )
        _currentUser.value = updatedUser
        saveUserProfile(updatedUser)

        // 2. Save AdReward record
        val adReward = AdReward(
            transactionId = txId,
            userId = user.userId,
            points = bonusPoints,
            adProvider = adProvider,
            rewarded = true,
            createdAt = System.currentTimeMillis(),
            dateKey = today
        )
        try {
            firestore?.collection("adRewards")?.document(txId)?.set(adReward)
        } catch (e: Exception) {
            Log.w(TAG, "AdReward firestore save fallback", e)
        }

        // 3. Save Wallet transaction
        val tx = WalletTransaction(
            transactionId = txId,
            userId = user.userId,
            type = "rewarded_ad",
            points = bonusPoints.toLong(),
            source = "Watched Rewarded Ad Bonus",
            referenceId = txId,
            createdAt = System.currentTimeMillis()
        )
        recordTransaction(tx)

        return Result.success(bonusPoints)
    }

    // ==========================================
    // REWARD REDEMPTION & WALLET
    // ==========================================

    fun submitRedemptionRequest(
        reward: RewardItem,
        recipientDetails: String
    ): Result<RedemptionRequest> {
        val user = _currentUser.value ?: return Result.failure(Exception("Please log in to redeem rewards"))

        if (user.status == "suspended") {
            return Result.failure(Exception("Account suspended. Cannot redeem points."))
        }

        if (recipientDetails.isBlank()) {
            return Result.failure(Exception("Please provide your Free Fire Player ID or delivery details"))
        }

        if (reward.requiredPoints < 1000L) {
            return Result.failure(Exception("Minimum redemption is 1000 Points = 60 Free Fire Diamonds."))
        }

        if (user.points < reward.requiredPoints) {
            return Result.failure(Exception("Insufficient points. You need ${reward.requiredPoints} points but have ${user.points}."))
        }

        // Safe point deduction & hold
        val updatedPoints = user.points - reward.requiredPoints
        val updatedUser = user.copy(
            points = updatedPoints,
            updatedAt = System.currentTimeMillis()
        )
        _currentUser.value = updatedUser
        saveUserProfile(updatedUser)

        val requestId = "REQ-" + UUID.randomUUID().toString().take(8).uppercase()
        val request = RedemptionRequest(
            requestId = requestId,
            userId = user.userId,
            userEmail = user.email,
            userName = user.displayName,
            rewardId = reward.rewardId,
            rewardTitle = reward.title,
            diamonds = reward.diamonds,
            points = reward.requiredPoints,
            status = "PENDING",
            recipientDetails = recipientDetails,
            adminNote = "Under review by admin for legitimate voucher issuance.",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Save to redemptions list
        _myRedemptions.value = listOf(request) + _myRedemptions.value
        _allRedemptions.value = listOf(request) + _allRedemptions.value

        try {
            firestore?.collection("redemptionRequests")?.document(requestId)?.set(request)
        } catch (e: Exception) {
            Log.w(TAG, "Redemption firestore save fallback", e)
        }

        // Record Hold Transaction
        val tx = WalletTransaction(
            transactionId = "tx_red_" + UUID.randomUUID().toString().take(8),
            userId = user.userId,
            type = "redemption",
            points = -reward.requiredPoints,
            source = "Redemption Hold: ${reward.title}",
            referenceId = requestId,
            createdAt = System.currentTimeMillis()
        )
        recordTransaction(tx)

        return Result.success(request)
    }

    // ==========================================
    // MONTHLY DIAMOND REDEMPTION SYSTEM
    // ==========================================

    fun refreshMonthlyWindowInfo() {
        val info = serverTimeManager.getWindowInfo(_appConfig.value)
        val uid = _currentUser.value?.userId ?: ""
        val myExisting = _monthlyRedemptions.value.find { it.userId == uid && it.month == info.currentMonthKey }
        val hasSubmitted = myExisting != null && (
            myExisting.status == MonthlyRedemption.STATUS_PENDING ||
            myExisting.status == MonthlyRedemption.STATUS_PROCESSING ||
            myExisting.status == MonthlyRedemption.STATUS_COMPLETED
        )

        _monthlyWindowInfo.value = info.copy(
            hasSubmittedThisMonth = hasSubmitted,
            existingMonthlyRedemption = myExisting
        )
    }

    suspend fun submitMonthlyDiamondRedemption(
        pointsToRedeem: Long,
        playerId: String
    ): Result<MonthlyRedemption> {
        val user = _currentUser.value ?: return Result.failure(
            Exception("Authentication required. Please sign in to submit a redemption request.")
        )

        if (user.status == "suspended") {
            return Result.failure(
                Exception("Account suspended due to fair-play violation. Cannot submit redemption.")
            )
        }

        // 1. Server-Side Date Control: Calibrate true server time and verify window
        serverTimeManager.syncWithServer()
        val windowInfo = serverTimeManager.getWindowInfo(_appConfig.value)

        if (!windowInfo.isOpen) {
            return Result.failure(
                Exception(
                    "Monthly Diamond Redemption is currently CLOSED.\n" +
                    "Redemption is open strictly from the 5th through the 10th of every month (Server Timezone: ${windowInfo.serverTimezone}).\n" +
                    "Your accumulated points NEVER expire and remain in your wallet for ${windowInfo.nextWindowStartDate}."
                )
            )
        }

        // 2. Validate Free Fire Player ID
        val cleanPlayerId = playerId.trim()
        if (cleanPlayerId.isBlank() || cleanPlayerId.length < 5 || !cleanPlayerId.all { it.isLetterOrDigit() }) {
            return Result.failure(
                Exception("Invalid Free Fire Player ID. Please enter a valid Player ID (numeric or alphanumeric).")
            )
        }

        // 3. Minimum points validation
        val minPoints = _appConfig.value.minRedemptionPoints
        if (pointsToRedeem < minPoints) {
            return Result.failure(
                Exception("Minimum redemption is $minPoints Points (${(minPoints / 1000) * _appConfig.value.diamondsPerThousandPoints} Diamonds).")
            )
        }

        if (pointsToRedeem % 1000L != 0L) {
            return Result.failure(
                Exception("Redemption amount must be in multiples of 1,000 Points (1,000 pts = ${_appConfig.value.diamondsPerThousandPoints} Diamonds).")
            )
        }

        // 4. Wallet balance verification
        if (user.points < pointsToRedeem) {
            return Result.failure(
                Exception("Insufficient points: You have ${user.points} points, but selected ${pointsToRedeem} points.")
            )
        }

        // 5. Duplicate Protection: unique monthly redemption identifier (userId + YYYY-MM)
        val monthKey = windowInfo.currentMonthKey
        val docId = "${user.userId}_${monthKey}"

        val existingSubmission = _monthlyRedemptions.value.find {
            (it.id == docId || (it.userId == user.userId && it.month == monthKey)) &&
            it.status in listOf(
                MonthlyRedemption.STATUS_PENDING,
                MonthlyRedemption.STATUS_PROCESSING,
                MonthlyRedemption.STATUS_COMPLETED
            )
        }

        if (existingSubmission != null) {
            return Result.failure(
                Exception(
                    "Duplicate request rejected: You have already submitted a redemption request for $monthKey (ID: ${existingSubmission.id}, Status: ${existingSubmission.status}). Only one redemption request is permitted per monthly cycle."
                )
            )
        }

        // 6. Point conversion calculation
        val diamondsPer1k = _appConfig.value.diamondsPerThousandPoints
        val diamondAmount = ((pointsToRedeem / 1000L) * diamondsPer1k).toInt()

        // 7. Deduct ONLY the redeemed points. Never reset the wallet! Remainder carries forward.
        val updatedUser = user.copy(
            points = user.points - pointsToRedeem,
            updatedAt = System.currentTimeMillis()
        )
        _currentUser.value = updatedUser
        saveUserProfile(updatedUser)

        // 8. Create MonthlyRedemption record in monthlyRedemptions/{userId_YYYY_MM}
        val redemption = MonthlyRedemption(
            id = docId,
            userId = user.userId,
            userName = user.displayName,
            userEmail = user.email,
            month = monthKey,
            pointsRedeemed = pointsToRedeem,
            diamondAmount = diamondAmount,
            playerId = cleanPlayerId,
            status = MonthlyRedemption.STATUS_PENDING,
            createdAt = System.currentTimeMillis(),
            processedAt = null,
            providerTransactionId = null,
            failureReason = null,
            adminNote = "Submitted during monthly redemption window. Under authorized review."
        )

        _monthlyRedemptions.value = listOf(redemption) + _monthlyRedemptions.value
        _myMonthlyRedemptions.value = listOf(redemption) + _myMonthlyRedemptions.value

        try {
            firestore?.collection("monthlyRedemptions")?.document(docId)?.set(redemption)
        } catch (e: Exception) {
            Log.w(TAG, "Monthly redemption firestore save fallback", e)
        }

        // 9. Record transaction in wallet history
        val tx = WalletTransaction(
            transactionId = "tx_mred_" + UUID.randomUUID().toString().take(8),
            userId = user.userId,
            type = "monthly_redemption",
            points = -pointsToRedeem,
            source = "Monthly Diamond Redemption: $diamondAmount Free Fire Diamonds ($monthKey)",
            referenceId = docId,
            createdAt = System.currentTimeMillis()
        )
        recordTransaction(tx)

        refreshMonthlyWindowInfo()
        return Result.success(redemption)
    }

    fun adminUpdateMonthlyRedemption(
        id: String,
        newStatus: String,
        providerTxId: String? = null,
        failureReason: String? = null,
        adminNote: String? = null
    ) {
        val target = _monthlyRedemptions.value.find { it.id == id } ?: return
        val now = System.currentTimeMillis()
        val updated = target.copy(
            status = newStatus,
            providerTransactionId = providerTxId ?: target.providerTransactionId,
            failureReason = failureReason ?: target.failureReason,
            adminNote = adminNote ?: target.adminNote,
            processedAt = if (newStatus in listOf(MonthlyRedemption.STATUS_COMPLETED, MonthlyRedemption.STATUS_REJECTED, MonthlyRedemption.STATUS_FAILED)) now else target.processedAt
        )

        _monthlyRedemptions.value = _monthlyRedemptions.value.map { if (it.id == id) updated else it }
        _myMonthlyRedemptions.value = _myMonthlyRedemptions.value.map { if (it.id == id) updated else it }

        // If REJECTED or FAILED, securely refund points to user!
        if (newStatus == MonthlyRedemption.STATUS_REJECTED || newStatus == MonthlyRedemption.STATUS_FAILED) {
            val user = _currentUser.value?.takeIf { it.userId == target.userId }
                ?: _allUsers.value.find { it.userId == target.userId }
            if (user != null) {
                val refundedUser = user.copy(
                    points = user.points + target.pointsRedeemed,
                    updatedAt = now
                )
                if (_currentUser.value?.userId == target.userId) {
                    _currentUser.value = refundedUser
                }
                _allUsers.value = _allUsers.value.map {
                    if (it.userId == target.userId) refundedUser else it
                }
                saveUserProfile(refundedUser)

                val tx = WalletTransaction(
                    transactionId = "tx_ref_" + UUID.randomUUID().toString().take(8),
                    userId = target.userId,
                    type = "refund",
                    points = target.pointsRedeemed,
                    source = "Refund for ${newStatus.lowercase()} monthly redemption: ${target.diamondAmount} Diamonds",
                    referenceId = target.id,
                    createdAt = now
                )
                recordTransaction(tx)
            }
        }

        try {
            firestore?.collection("monthlyRedemptions")?.document(id)?.set(updated)
        } catch (e: Exception) {
            Log.w(TAG, "Update monthly redemption firestore error", e)
        }

        refreshMonthlyWindowInfo()
    }

    fun adminSetTestServerDayOverride(day: Int?) {
        val newConfig = _appConfig.value.copy(testServerDayOverride = day)
        updateConfig(newConfig)
        refreshMonthlyWindowInfo()
    }

    fun adminSetServerTimezone(timezone: String) {
        val newConfig = _appConfig.value.copy(serverTimezone = timezone)
        updateConfig(newConfig)
        refreshMonthlyWindowInfo()
    }

    private fun recordTransaction(tx: WalletTransaction) {
        _transactions.value = listOf(tx) + _transactions.value
        try {
            firestore?.collection("transactions")?.document(tx.transactionId)?.set(tx)
        } catch (e: Exception) {
            Log.w(TAG, "Transaction firestore save fallback", e)
        }
    }

    // ==========================================
    // ADMIN ACTIONS
    // ==========================================

    fun updateRedemptionStatus(requestId: String, newStatus: String, adminNote: String) {
        val target = _allRedemptions.value.find { it.requestId == requestId } ?: return
        val updatedTarget = target.copy(
            status = newStatus,
            adminNote = adminNote,
            updatedAt = System.currentTimeMillis()
        )

        _allRedemptions.value = _allRedemptions.value.map {
            if (it.requestId == requestId) updatedTarget else it
        }
        _myRedemptions.value = _myRedemptions.value.map {
            if (it.requestId == requestId) updatedTarget else it
        }

        // If rejected, refund the points to the user safely!
        if (newStatus == "REJECTED") {
            val user = _allUsers.value.find { it.userId == target.userId } ?: _currentUser.value
            if (user != null) {
                val refundedUser = user.copy(
                    points = user.points + target.points,
                    updatedAt = System.currentTimeMillis()
                )
                if (_currentUser.value?.userId == target.userId) {
                    _currentUser.value = refundedUser
                }
                _allUsers.value = _allUsers.value.map {
                    if (it.userId == target.userId) refundedUser else it
                }
                saveUserProfile(refundedUser)

                // Record refund transaction
                val tx = WalletTransaction(
                    transactionId = "tx_ref_" + UUID.randomUUID().toString().take(8),
                    userId = target.userId,
                    type = "refund",
                    points = target.points,
                    source = "Refund for rejected request: ${target.rewardTitle}",
                    referenceId = target.requestId,
                    createdAt = System.currentTimeMillis()
                )
                recordTransaction(tx)
            }
        }

        try {
            firestore?.collection("redemptionRequests")?.document(requestId)?.set(updatedTarget)
        } catch (e: Exception) {
            Log.w(TAG, "Update redemption firestore error", e)
        }
    }

    fun addQuizQuestion(quiz: QuizQuestion) {
        val newQuiz = if (quiz.quizId.isBlank()) quiz.copy(quizId = "q_" + UUID.randomUUID().toString().take(6)) else quiz
        _quizzes.value = _quizzes.value + newQuiz
        try {
            firestore?.collection("quizzes")?.document(newQuiz.quizId)?.set(newQuiz)
        } catch (e: Exception) {
            Log.w(TAG, "Add quiz firestore error", e)
        }
    }

    fun updateQuizQuestion(quiz: QuizQuestion) {
        _quizzes.value = _quizzes.value.map { if (it.quizId == quiz.quizId) quiz else it }
        try {
            firestore?.collection("quizzes")?.document(quiz.quizId)?.set(quiz)
        } catch (e: Exception) {
            Log.w(TAG, "Update quiz firestore error", e)
        }
    }

    fun deleteQuizQuestion(quizId: String) {
        _quizzes.value = _quizzes.value.filterNot { it.quizId == quizId }
        try {
            firestore?.collection("quizzes")?.document(quizId)?.delete()
        } catch (e: Exception) {
            Log.w(TAG, "Delete quiz firestore error", e)
        }
    }

    fun addRewardItem(item: RewardItem) {
        val newItem = if (item.rewardId.isBlank()) item.copy(rewardId = "rew_" + UUID.randomUUID().toString().take(6)) else item
        _rewards.value = _rewards.value + newItem
        try {
            firestore?.collection("rewards")?.document(newItem.rewardId)?.set(newItem)
        } catch (e: Exception) {
            Log.w(TAG, "Add reward firestore error", e)
        }
    }

    fun updateRewardItem(item: RewardItem) {
        _rewards.value = _rewards.value.map { if (it.rewardId == item.rewardId) item else it }
        try {
            firestore?.collection("rewards")?.document(item.rewardId)?.set(item)
        } catch (e: Exception) {
            Log.w(TAG, "Update reward firestore error", e)
        }
    }

    fun deleteRewardItem(rewardId: String) {
        _rewards.value = _rewards.value.filterNot { it.rewardId == rewardId }
        try {
            firestore?.collection("rewards")?.document(rewardId)?.delete()
        } catch (e: Exception) {
            Log.w(TAG, "Delete reward firestore error", e)
        }
    }

    fun updateConfig(config: AppConfig) {
        _appConfig.value = config
        try {
            firestore?.collection("config")?.document("settings")?.set(config)
        } catch (e: Exception) {
            Log.w(TAG, "Update config firestore error", e)
        }
    }

    fun toggleUserStatus(userId: String) {
        _allUsers.value = _allUsers.value.map {
            if (it.userId == userId) {
                val newStatus = if (it.status == "active") "suspended" else "active"
                val updated = it.copy(status = newStatus, updatedAt = System.currentTimeMillis())
                if (_currentUser.value?.userId == userId) {
                    _currentUser.value = updated
                }
                saveUserProfile(updated)
                updated
            } else it
        }
    }
}

data class QuizAnswerResult(
    val isCorrect: Boolean,
    val pointsEarned: Int,
    val correctOptionIndex: Int = 0,
    val explanation: String = "",
    val errorMessage: String? = null
)

data class SpinRewardResult(
    val points: Long,
    val spinType: String,
    val freeSpinsLeft: Int,
    val extraSpinsLeft: Int,
    val adSpinsUsedToday: Int,
    val maxAdSpins: Int,
    val transactionId: String
)

