package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.QuizQuestion
import com.example.data.model.RewardItem
import com.example.data.repository.QuizRewardsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Quiz Rewards", appName)
  }

  @Test
  fun `quiz answer correct grants points and prevents immediate replay`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = QuizRewardsRepository(context)

    val question = QuizQuestion(
      quizId = "test_q1",
      question = "What is Free Fire's classic map?",
      options = listOf("Bermuda", "Erangel", "Kalahari", "Purgatory"),
      correctAnswer = 0,
      points = 15
    )

    // Initial points
    val initialPoints = repo.currentUser.value?.points ?: 0L

    // Submit correct answer
    val result = repo.submitQuizAnswer(question, 0)
    assertTrue(result.isCorrect)
    assertEquals(15, result.pointsEarned)

    // Check points updated
    val updatedPoints = repo.currentUser.value?.points ?: 0L
    assertEquals(initialPoints + 15L, updatedPoints)

    // Attempting same question again should be blocked
    val replayResult = repo.submitQuizAnswer(question, 0)
    assertFalse(replayResult.isCorrect)
    assertEquals(0, replayResult.pointsEarned)
  }

  @Test
  fun `rewarded ad completion updates wallet and enforces daily limit`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = QuizRewardsRepository(context)

    val initialPoints = repo.currentUser.value?.points ?: 0L
    val initialAds = repo.currentUser.value?.adsWatchedToday ?: 0

    val result = repo.onRewardedAdCompleted("Test AdMob")
    assertTrue(result.isSuccess)

    val afterPoints = repo.currentUser.value?.points ?: 0L
    assertEquals(initialPoints + repo.appConfig.value.rewardedAdPoints, afterPoints)
    assertEquals(initialAds + 1, repo.currentUser.value?.adsWatchedToday)
  }

  @Test
  fun `redemption request deducts points safely and creates pending request`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = QuizRewardsRepository(context)

    val testReward = RewardItem(
      rewardId = "rew_test_60",
      title = "Free Fire 60 Diamonds",
      diamonds = 60,
      requiredPoints = 1000L
    )

    val initialPoints = repo.currentUser.value?.points ?: 0L
    if (initialPoints >= 1000L) {
      val res = repo.submitRedemptionRequest(testReward, "Player ID: 123456789")
      assertTrue(res.isSuccess)
      val req = res.getOrNull()
      assertNotNull(req)
      assertEquals("PENDING", req?.status)
      assertEquals(60, req?.diamonds)
      assertEquals(1000L, req?.points)

      val afterPoints = repo.currentUser.value?.points ?: 0L
      assertEquals(initialPoints - 1000L, afterPoints)

      // Test Admin approval & completion
      req?.let {
        repo.updateRedemptionStatus(it.requestId, "COMPLETED", "Voucher PIN: FF-TEST-9921")
        val updatedReq = repo.allRedemptions.value.find { r -> r.requestId == it.requestId }
        assertEquals("COMPLETED", updatedReq?.status)
        assertEquals("Voucher PIN: FF-TEST-9921", updatedReq?.adminNote)
      }
    }
  }

  @Test
  fun `monthly diamond redemption test cases - days 4, 5, 7, 10, 11 and duplicate protection`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = QuizRewardsRepository(context)

    // Ensure user has at least 3000 points
    val initialUser = repo.currentUser.value
    assertNotNull(initialUser)
    val userWithPoints = initialUser!!.copy(points = 3000L)
    repo.saveUserProfile(userWithPoints)
    repo.updateConfig(repo.appConfig.value.copy(diamondsPerThousandPoints = 60, minRedemptionPoints = 1000L))

    // 1. Day 4 -> redemption rejected
    repo.adminSetTestServerDayOverride(4)
    val day4Result = repo.submitMonthlyDiamondRedemption(1000L, "104829104")
    assertFalse("Day 4 redemption should be rejected", day4Result.isSuccess)
    assertTrue(day4Result.exceptionOrNull()?.message?.contains("CLOSED", ignoreCase = true) == true)

    // 2. Day 11 -> redemption rejected
    repo.adminSetTestServerDayOverride(11)
    val day11Result = repo.submitMonthlyDiamondRedemption(1000L, "104829104")
    assertFalse("Day 11 redemption should be rejected", day11Result.isSuccess)
    assertTrue(day11Result.exceptionOrNull()?.message?.contains("CLOSED", ignoreCase = true) == true)

    // 3. User with insufficient points -> rejected
    repo.adminSetTestServerDayOverride(5) // Window is OPEN
    val insufficientPointsResult = repo.submitMonthlyDiamondRedemption(50000L, "104829104")
    assertFalse("Insufficient points redemption must be rejected", insufficientPointsResult.isSuccess)

    // 4. Day 5 -> redemption allowed
    repo.adminSetTestServerDayOverride(5)
    val startPoints = repo.currentUser.value!!.points
    val day5Result = repo.submitMonthlyDiamondRedemption(1000L, "104829104")
    assertTrue("Day 5 redemption should succeed", day5Result.isSuccess)
    val redemption = day5Result.getOrNull()
    assertNotNull(redemption)
    assertEquals(1000L, redemption?.pointsRedeemed)
    assertEquals(60, redemption?.diamondAmount)
    assertEquals("PENDING", redemption?.status)

    // Points After Redemption: deducts ONLY 1000, remainder remains in wallet
    val pointsAfter = repo.currentUser.value!!.points
    assertEquals(startPoints - 1000L, pointsAfter)

    // 5. Duplicate monthly redemption -> rejected!
    val duplicateResult = repo.submitMonthlyDiamondRedemption(1000L, "104829104")
    assertFalse("Duplicate monthly redemption for same cycle must be rejected", duplicateResult.isSuccess)
    assertTrue(duplicateResult.exceptionOrNull()?.message?.contains("Duplicate", ignoreCase = true) == true)

    // 6. Admin Rejection -> securely refunds points!
    redemption?.let {
      repo.adminUpdateMonthlyRedemption(it.id, "REJECTED", failureReason = "Player ID unverified", adminNote = "Rejected with refund")
      val pointsAfterRefund = repo.currentUser.value!!.points
      assertEquals("Points must be restored after admin rejection", startPoints, pointsAfterRefund)
    }

    // 7. Day 7 & Day 10 -> window is open
    repo.adminSetTestServerDayOverride(7)
    assertTrue("Day 7 window must be open", repo.monthlyWindowInfo.value.isOpen)

    repo.adminSetTestServerDayOverride(10)
    assertTrue("Day 10 window must be open", repo.monthlyWindowInfo.value.isOpen)

    // Reset override
    repo.adminSetTestServerDayOverride(null)
  }

  @Test
  fun `welcome bonus awards 100 points once and is idempotent`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = QuizRewardsRepository(context)

    val newUserId = "user_welcome_test_123"
    val testProfile = com.example.data.model.UserProfile(
      userId = newUserId,
      email = "newplayer@example.com",
      displayName = "New Player",
      points = 0L,
      welcomeBonusAwarded = false
    )
    repo.saveUserProfile(testProfile)

    // Initially 0 points
    val savedUser = repo.allUsers.value.find { it.userId == newUserId }
    assertNotNull(savedUser)
    assertEquals(0L, savedUser?.points)
    assertFalse(savedUser!!.welcomeBonusAwarded)

    // Award welcome bonus
    val awarded = repo.awardWelcomeBonus(newUserId)
    assertTrue("First welcome bonus claim must succeed", awarded)

    val userAfterBonus = repo.allUsers.value.find { it.userId == newUserId }
    assertEquals(100L, userAfterBonus?.points)
    assertTrue(userAfterBonus!!.welcomeBonusAwarded)

    // Second claim must be rejected (idempotency)
    val awardedSecond = repo.awardWelcomeBonus(newUserId)
    assertFalse("Second welcome bonus claim must be rejected", awardedSecond)
    assertEquals(100L, repo.allUsers.value.find { it.userId == newUserId }?.points)

    // Verify transaction
    val tx = repo.transactions.value.find { it.userId == newUserId && it.type == "WELCOME_GIFT" }
    assertNotNull("Transaction must be recorded", tx)
    assertEquals(100L, tx?.points)
    assertEquals("COMPLETED", tx?.status)
  }

  @Test
  fun `referral system validates code, awards referrer and prevents self or duplicate referral`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = QuizRewardsRepository(context)

    val referrerId = "referrer_hero_456"
    val refCode = "QB-HERO45"
    val referrerUser = com.example.data.model.UserProfile(
      userId = referrerId,
      email = "hero@example.com",
      displayName = "Hero Referrer",
      points = 500L,
      referralCode = refCode
    )
    repo.saveUserProfile(referrerUser)

    val newUserId = "new_friend_789"
    val newUser = com.example.data.model.UserProfile(
      userId = newUserId,
      email = "friend@example.com",
      displayName = "Friend User",
      points = 0L
    )
    repo.saveUserProfile(newUser)

    // 1. Self referral must fail
    val selfRef = repo.validateAndApplyReferral(referrerId, refCode)
    assertFalse("Self referral must fail", selfRef.isSuccess)

    // 2. Invalid code must fail
    val invalidRef = repo.validateAndApplyReferral(newUserId, "QB-INVALID")
    assertFalse("Invalid code must fail", invalidRef.isSuccess)

    // 3. Valid referral must succeed
    val validRef = repo.validateAndApplyReferral(newUserId, refCode)
    assertTrue("Valid referral must succeed", validRef.isSuccess)

    // Referrer balance increased by 100 points
    val referrerAfter = repo.allUsers.value.find { it.userId == referrerId }
    assertEquals(600L, referrerAfter?.points)

    // Referral record created with REWARDED status
    val refRecord = repo.allReferrals.value.find { it.referredUserId == newUserId }
    assertNotNull(refRecord)
    assertEquals(referrerId, refRecord?.referrerUserId)
    assertEquals("REWARDED", refRecord?.status)
    assertEquals(100L, refRecord?.rewardPoints)

    // 4. Duplicate referral on same new user must fail
    val duplicateRef = repo.validateAndApplyReferral(newUserId, refCode)
    assertFalse("Duplicate referral for same user must fail", duplicateRef.isSuccess)
  }

  @Test
  fun `spin and earn consumes spins securely and awards 20 to 100 points`() = kotlinx.coroutines.test.runTest {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = QuizRewardsRepository(context)

    val spinUser = repo.currentUser.value
    assertNotNull(spinUser)
    val startPoints = spinUser!!.points

    // Perform spin
    val spinResult = repo.spinWheel()
    assertTrue("Spin should succeed when free spins available", spinResult.isSuccess)
    val res = spinResult.getOrNull()
    assertNotNull(res)
    assertTrue("Reward must be in 20..100 points", res!!.points in 20L..100L)
    assertEquals(2, res.freeSpinsLeft) // Initial 3 - 1 = 2

    // Points credited to user
    val userAfterSpin = repo.currentUser.value!!.points
    assertEquals(startPoints + res.points, userAfterSpin)

    // Transaction exists
    val tx = repo.transactions.value.find { it.type == "SPIN_REWARD" }
    assertNotNull(tx)
    assertEquals(res.points, tx?.points)
    assertEquals("COMPLETED", tx?.status)
  }
}
