package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppConfig
import com.example.data.model.MonthlyRedemption
import com.example.data.model.MonthlyRedemptionWindowInfo
import com.example.data.model.QuizQuestion
import com.example.data.model.RedemptionRequest
import com.example.data.model.RewardItem
import com.example.ui.components.StatusChip
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.QuizRewardsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminScreen(
    viewModel: QuizRewardsViewModel,
    onBack: () -> Unit
) {
    val allRedemptions by viewModel.allRedemptions.collectAsState()
    val quizzes by viewModel.quizzes.collectAsState()
    val rewards by viewModel.rewards.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val appConfig by viewModel.appConfig.collectAsState()
    val monthlyRedemptions by viewModel.monthlyRedemptions.collectAsState()
    val monthlyWindowInfo by viewModel.monthlyWindowInfo.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Overview", "Monthly Redemptions", "Store Requests", "Quizzes", "Rewards", "Config", "Users")

    // Modals
    var editingQuiz by remember { mutableStateOf<QuizQuestion?>(null) }
    var isCreatingQuiz by remember { mutableStateOf(false) }

    var actionRedemption by remember { mutableStateOf<RedemptionRequest?>(null) }
    var actionType by remember { mutableStateOf<String?>(null) } // "APPROVE", "REJECT", "COMPLETE"
    var adminNoteInput by remember { mutableStateOf("") }

    // Monthly Redemption Dialog State
    var actionMonthlyRedemption by remember { mutableStateOf<MonthlyRedemption?>(null) }
    var monthlyActionType by remember { mutableStateOf<String?>(null) } // "PROCESSING", "COMPLETE", "REJECT", "FAIL"
    var monthlyAdminNoteInput by remember { mutableStateOf("") }
    var monthlyProviderTxIdInput by remember { mutableStateOf("") }
    var monthlyFailureReasonInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Admin Top Bar
        Surface(
            color = DarkSurface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Admin Management",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CoralAccent.copy(alpha = 0.2f))
                                .border(1.dp, CoralAccent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("ROOT", color = CoralAccent, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Text(
                        text = "Authorized Admin Dashboard",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                }
            }
        }

        // Scrollable Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = DarkSurface,
            contentColor = GoldPrimary,
            edgePadding = 16.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = GoldPrimary,
                    height = 3.dp
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (selectedTabIndex == index) GoldPrimary else TextSecondary
                        )
                    }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> AdminOverviewTab(allUsers, allRedemptions, quizzes, rewards)
            1 -> AdminMonthlyRedemptionsTab(monthlyRedemptions, monthlyWindowInfo, viewModel) { req, type ->
                actionMonthlyRedemption = req
                monthlyActionType = type
                monthlyAdminNoteInput = when (type) {
                    "REJECT", "FAIL" -> "Free Fire Player ID could not be verified."
                    "PROCESSING" -> "Verification with authorized top-up provider in progress."
                    else -> "Fulfilled with official Free Fire voucher."
                }
                monthlyFailureReasonInput = if (type == "REJECT" || type == "FAIL") "Player ID could not be verified or invalid." else ""
                monthlyProviderTxIdInput = if (type == "COMPLETE") "TOPUP-VOUCHER-FF-" + (100000..999999).random() else ""
            }
            2 -> AdminRedemptionsTab(allRedemptions) { req, type ->
                actionRedemption = req
                actionType = type
                adminNoteInput = when (type) {
                    "REJECT" -> "Invalid Free Fire Player ID or duplicate request."
                    "APPROVE" -> "Approved by admin. Top-up voucher queued for delivery."
                    else -> "Voucher PIN: FF" + (1000..9999).random() + "-TOPUP-SUCCESS"
                }
            }
            3 -> AdminQuizzesTab(
                quizzes = quizzes,
                onAddQuiz = { isCreatingQuiz = true },
                onEditQuiz = { editingQuiz = it },
                onDeleteQuiz = { viewModel.adminDeleteQuiz(it.quizId) }
            )
            4 -> AdminRewardsTab(rewards, viewModel)
            5 -> AdminConfigTab(appConfig, viewModel)
            6 -> AdminUsersTab(allUsers, viewModel)
        }
    }

    // Redemption Action Dialog
    if (actionRedemption != null && actionType != null) {
        Dialog(onDismissRequest = { actionRedemption = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "$actionType Redemption",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (actionType == "REJECT") CoralAccent else EmeraldGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "User: ${actionRedemption?.userEmail}\nReward: ${actionRedemption?.rewardTitle}\nDetails: ${actionRedemption?.recipientDetails}",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = adminNoteInput,
                        onValueChange = { adminNoteInput = it },
                        label = { Text(if (actionType == "REJECT") "Reason for Rejection" else "Voucher PIN / Note") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { actionRedemption = null },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val targetStatus = when (actionType) {
                                    "REJECT" -> "REJECTED"
                                    "APPROVE" -> "APPROVED"
                                    else -> "COMPLETED"
                                }
                                viewModel.adminUpdateRedemption(
                                    actionRedemption!!.requestId,
                                    targetStatus,
                                    adminNoteInput
                                )
                                actionRedemption = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (actionType == "REJECT") CoralAccent else EmeraldGreen
                            )
                        ) {
                            Text(actionType ?: "Confirm")
                        }
                    }
                }
            }
        }
    }

    // Monthly Redemption Action Dialog
    if (actionMonthlyRedemption != null && monthlyActionType != null) {
        Dialog(onDismissRequest = { actionMonthlyRedemption = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    val req = actionMonthlyRedemption!!
                    val isRejectOrFail = monthlyActionType == "REJECT" || monthlyActionType == "FAIL"

                    Text(
                        text = if (isRejectOrFail) "Reject / Fail Monthly Redemption" else if (monthlyActionType == "PROCESSING") "Mark In-Processing" else "Complete & Deliver Diamonds",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isRejectOrFail) CoralAccent else EmeraldGreen
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "User: ${req.userName} (${req.userEmail})\nPlayer ID: ${req.playerId}\nMonth: ${req.month}\nAmount: 💎 ${req.diamondAmount} Diamonds (${req.pointsRedeemed} pts)",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        lineHeight = 16.sp
                    )

                    if (isRejectOrFail) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "⚠️ Rejecting will automatically refund ${req.pointsRedeemed} points back to the user's wallet.",
                            style = MaterialTheme.typography.labelSmall.copy(color = CoralAccent, fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = monthlyFailureReasonInput,
                            onValueChange = { monthlyFailureReasonInput = it },
                            label = { Text("Reason for Failure / Rejection") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CoralAccent,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            maxLines = 2
                        )
                    }

                    if (monthlyActionType == "COMPLETE") {
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = monthlyProviderTxIdInput,
                            onValueChange = { monthlyProviderTxIdInput = it },
                            label = { Text("Provider Transaction ID / Voucher PIN") },
                            placeholder = { Text("e.g. TOPUP-VOUCHER-FF-882194") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldGreen,
                                unfocusedBorderColor = BorderSubtle,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = monthlyAdminNoteInput,
                        onValueChange = { monthlyAdminNoteInput = it },
                        label = { Text("Admin Note (Visible in User History)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { actionMonthlyRedemption = null },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                val targetStatus = when (monthlyActionType) {
                                    "PROCESSING" -> MonthlyRedemption.STATUS_PROCESSING
                                    "COMPLETE" -> MonthlyRedemption.STATUS_COMPLETED
                                    "FAIL" -> MonthlyRedemption.STATUS_FAILED
                                    "REJECT" -> MonthlyRedemption.STATUS_REJECTED
                                    else -> MonthlyRedemption.STATUS_PROCESSING
                                }
                                viewModel.adminUpdateMonthlyRedemption(
                                    id = req.id,
                                    newStatus = targetStatus,
                                    providerTxId = if (targetStatus == MonthlyRedemption.STATUS_COMPLETED) monthlyProviderTxIdInput.ifBlank { "MANUAL-ADMIN-" + System.currentTimeMillis() } else null,
                                    failureReason = if (isRejectOrFail) monthlyFailureReasonInput.ifBlank { "Verification failed." } else null,
                                    adminNote = monthlyAdminNoteInput.ifBlank { null }
                                )
                                actionMonthlyRedemption = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRejectOrFail) CoralAccent else EmeraldGreen
                            )
                        ) {
                            Text(
                                text = when (monthlyActionType) {
                                    "PROCESSING" -> "Mark Processing"
                                    "COMPLETE" -> "Complete Delivery"
                                    "FAIL" -> "Fail & Refund"
                                    "REJECT" -> "Reject & Refund"
                                    else -> "Confirm"
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Create / Edit Quiz Dialog
    if (isCreatingQuiz || editingQuiz != null) {
        val initial = editingQuiz ?: QuizQuestion()
        var questionText by remember { mutableStateOf(initial.question) }
        var optA by remember { mutableStateOf(initial.options.getOrElse(0) { "" }) }
        var optB by remember { mutableStateOf(initial.options.getOrElse(1) { "" }) }
        var optC by remember { mutableStateOf(initial.options.getOrElse(2) { "" }) }
        var optD by remember { mutableStateOf(initial.options.getOrElse(3) { "" }) }
        var correctIndex by remember { mutableIntStateOf(initial.correctAnswer) }
        var pointsText by remember { mutableStateOf(initial.points.toString()) }
        var categoryText by remember { mutableStateOf(initial.category) }
        var explanationText by remember { mutableStateOf(initial.explanation) }

        Dialog(onDismissRequest = {
            isCreatingQuiz = false
            editingQuiz = null
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = if (isCreatingQuiz) "Create Quiz Question" else "Edit Quiz Question",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        label = { Text("Question") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = optA, onValueChange = { optA = it }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = optB, onValueChange = { optB = it }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = optC, onValueChange = { optC = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = optD, onValueChange = { optD = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Correct Option: ${('A' + correctIndex)}", color = GoldLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (0..3).forEach { idx ->
                            Button(
                                onClick = { correctIndex = idx },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (correctIndex == idx) EmeraldGreen else DarkCard
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(('A' + idx).toString())
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = categoryText, onValueChange = { categoryText = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = pointsText, onValueChange = { pointsText = it }, label = { Text("Points") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = explanationText, onValueChange = { explanationText = it }, label = { Text("Explanation") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(onClick = {
                            isCreatingQuiz = false
                            editingQuiz = null
                        }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val q = QuizQuestion(
                                    quizId = initial.quizId,
                                    question = questionText,
                                    options = listOf(optA, optB, optC, optD),
                                    correctAnswer = correctIndex,
                                    points = pointsText.toIntOrNull() ?: 10,
                                    category = categoryText.ifBlank { "General" },
                                    explanation = explanationText
                                )
                                if (isCreatingQuiz) {
                                    viewModel.adminAddQuiz(q)
                                } else {
                                    viewModel.adminUpdateQuiz(q)
                                }
                                isCreatingQuiz = false
                                editingQuiz = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground)
                        ) {
                            Text("Save Question")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOverviewTab(
    users: List<com.example.data.model.UserProfile>,
    redemptions: List<RedemptionRequest>,
    quizzes: List<QuizQuestion>,
    rewards: List<RewardItem>
) {
    val pendingCount = redemptions.count { it.status == "PENDING" }
    val totalPoints = users.sumOf { it.points }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Key Metrics",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard("Total Users", "${users.size}", DiamondCyan, Modifier.weight(1f))
                AdminStatCard("Pending Requests", "$pendingCount", CoralAccent, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AdminStatCard("Active Quizzes", "${quizzes.size}", GoldPrimary, Modifier.weight(1f))
                AdminStatCard("Active Rewards", "${rewards.size}", EmeraldGreen, Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            AdminStatCard("Total Points in Circulation", "$totalPoints pts", GoldLight, Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Quick Verification Policy",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "1. Always verify the user's Free Fire Player ID against the official player database before issuing a voucher.\n" +
                                "2. Rejected redemptions automatically return points to the player's wallet balance with an audit record.\n" +
                                "3. Never share direct admin passwords or internal API keys.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 18.sp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminRedemptionsTab(
    redemptions: List<RedemptionRequest>,
    onAction: (RedemptionRequest, String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "Approved", "Rejected", "Completed")

    val filteredList = when (selectedFilter) {
        "All" -> redemptions
        else -> redemptions.filter { it.status.equals(selectedFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Status Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            items(filters) { f ->
                val isSelected = selectedFilter == f
                val count = if (f == "All") redemptions.size else redemptions.count { it.status.equals(f, ignoreCase = true) }
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { selectedFilter = f },
                    color = if (isSelected) GoldPrimary else DarkCard,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) GoldLight else BorderSubtle
                    )
                ) {
                    Text(
                        text = "$f ($count)",
                        color = if (isSelected) DarkBackground else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No $selectedFilter requests found.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredList) { req ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = req.requestId,
                                        color = DiamondCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = req.rewardTitle,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                                StatusChip(status = req.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "User: ${req.userName} (${req.userEmail})",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "Player ID / Target: ${req.recipientDetails}",
                                color = GoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Cost: ${req.points} pts • ${req.diamonds} Diamonds",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )

                            if (req.adminNote.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Admin Note: ${req.adminNote}",
                                    color = DiamondCyan,
                                    fontSize = 11.sp
                                )
                            }

                            if (req.status == "PENDING") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onAction(req, "APPROVE") },
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { onAction(req, "REJECT") },
                                        colors = ButtonDefaults.buttonColors(containerColor = CoralAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Reject & Refund", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { onAction(req, "COMPLETE") },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else if (req.status == "APPROVED") {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { onAction(req, "COMPLETE") },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Mark Completed (Voucher PIN)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { onAction(req, "REJECT") },
                                        colors = ButtonDefaults.buttonColors(containerColor = CoralAccent),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Reject & Refund", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminQuizzesTab(
    quizzes: List<QuizQuestion>,
    onAddQuiz: () -> Unit,
    onEditQuiz: (QuizQuestion) -> Unit,
    onDeleteQuiz: (QuizQuestion) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Quizzes (${quizzes.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Button(
                onClick = onAddQuiz,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Quiz")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(quizzes) { q ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = q.question,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                ),
                                maxLines = 2
                            )
                            Text(
                                text = "${q.category} • ${q.points} pts • Correct: Option ${('A' + q.correctAnswer)}",
                                style = MaterialTheme.typography.labelSmall.copy(color = GoldLight)
                            )
                        }

                        Row {
                            IconButton(onClick = { onEditQuiz(q) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = DiamondCyan)
                            }
                            IconButton(onClick = { onDeleteQuiz(q) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CoralAccent)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminRewardsTab(
    rewards: List<RewardItem>,
    viewModel: QuizRewardsViewModel
) {
    var isAddingReward by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newDiamonds by remember { mutableStateOf("60") }
    var newPoints by remember { mutableStateOf("1000") }
    var newCategory by remember { mutableStateOf("Free Fire") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Reward Packages (${rewards.size})",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Button(
                onClick = { isAddingReward = true },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Package", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(rewards) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${item.requiredPoints} pts = ${item.diamonds} 💎 • ${item.category}",
                                color = GoldLight,
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.adminUpdateReward(item.copy(enabled = !item.enabled))
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (item.enabled) EmeraldGreen else CoralAccent
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (item.enabled) "Active" else "Disabled", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (isAddingReward) {
        Dialog(onDismissRequest = { isAddingReward = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Add Reward Package",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newDiamonds, onValueChange = { newDiamonds = it }, label = { Text("Diamonds Count") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newPoints, onValueChange = { newPoints = it }, label = { Text("Required Points") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = newCategory, onValueChange = { newCategory = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        OutlinedButton(onClick = { isAddingReward = false }) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val rew = RewardItem(
                                    rewardId = "rew_custom_" + System.currentTimeMillis(),
                                    title = newTitle.ifBlank { "Free Fire Diamonds" },
                                    diamonds = newDiamonds.toIntOrNull() ?: 60,
                                    requiredPoints = newPoints.toLongOrNull() ?: 1000L,
                                    category = newCategory.ifBlank { "Free Fire" },
                                    bannerIcon = "diamond",
                                    enabled = true
                                )
                                viewModel.adminAddReward(rew)
                                isAddingReward = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground)
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminConfigTab(
    config: AppConfig,
    viewModel: QuizRewardsViewModel
) {
    var quizPoints by remember { mutableStateOf(config.quizPointsDefault.toString()) }
    var adPoints by remember { mutableStateOf(config.rewardedAdPoints.toString()) }
    var dailyAds by remember { mutableStateOf(config.dailyAdLimit.toString()) }
    var diamondsPer1k by remember { mutableStateOf(config.diamondsPerThousandPoints.toString()) }
    var minPoints by remember { mutableStateOf(config.minRedemptionPoints.toString()) }
    var serverTz by remember { mutableStateOf(config.serverTimezone) }
    var providerApiUrl by remember { mutableStateOf(config.topupProviderApiUrl) }
    var providerApiKey by remember { mutableStateOf(config.topupProviderApiKey) }
    var saveMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Global Points & Reward Conversion Rules",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Conversion Rule Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DiamondCyan.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "MAIN REWARD RULE (DEFAULT FIXED CONVERSION)",
                    style = MaterialTheme.typography.labelSmall.copy(color = DiamondCyan, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1000 Points = $diamondsPer1k Free Fire Diamonds",
                    color = GoldLight,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                Text(
                    text = "Minimum Redemption Threshold: $minPoints Points",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Monthly Window & Server Date Testing
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "MONTHLY REDEMPTION SCHEDULE (SERVER-CONTROLLED)",
                    style = MaterialTheme.typography.labelSmall.copy(color = GoldLight, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Rule: 5th through 10th day of every month = OPEN. Days 11–4 = CLOSED.",
                    color = TextPrimary,
                    fontSize = 12.sp
                )
                Text(
                    text = "Points never expire and automatically carry forward to next month.",
                    color = DiamondCyan,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "QA / Test Mode Simulation:",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Current State: ${config.testServerDayOverride?.let { "Simulating Day $it (${if (it in 5..10) "OPEN" else "CLOSED"})" } ?: "Real Server Date"}",
                    color = if (config.testServerDayOverride == null) TextPrimary else if (config.testServerDayOverride!! in 5..10) EmeraldGreen else CoralAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { viewModel.adminSetTestServerDayOverride(4) },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralAccent.copy(alpha = 0.2f), contentColor = CoralAccent),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                    ) {
                        Text("Day 4 (Closed)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.adminSetTestServerDayOverride(5) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.2f), contentColor = EmeraldGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                    ) {
                        Text("Day 5 (Open)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.adminSetTestServerDayOverride(7) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.2f), contentColor = EmeraldGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                    ) {
                        Text("Day 7 (Open)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { viewModel.adminSetTestServerDayOverride(10) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen.copy(alpha = 0.2f), contentColor = EmeraldGreen),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                    ) {
                        Text("Day 10 (Open)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.adminSetTestServerDayOverride(11) },
                        colors = ButtonDefaults.buttonColors(containerColor = CoralAccent.copy(alpha = 0.2f), contentColor = CoralAccent),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                    ) {
                        Text("Day 11 (Closed)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { viewModel.adminSetTestServerDayOverride(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = GoldLight),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.weight(1f).height(34.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                    ) {
                        Text("Reset Real", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = serverTz,
            onValueChange = { serverTz = it },
            label = { Text("Server Timezone (Default: UTC)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = diamondsPer1k,
            onValueChange = { diamondsPer1k = it },
            label = { Text("Diamonds Per 1,000 Points (Default: 60)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = minPoints,
            onValueChange = { minPoints = it },
            label = { Text("Minimum Redemption Points (Default: 1000)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = quizPoints,
            onValueChange = { quizPoints = it },
            label = { Text("Default Points Per Correct Quiz (+10 pts)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = adPoints,
            onValueChange = { adPoints = it },
            label = { Text("Rewarded Ad Bonus Points (+20 pts)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = dailyAds,
            onValueChange = { dailyAds = it },
            label = { Text("Max Rewarded Ads Per User Daily Limit (10)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Top-up Provider Credentials Card
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "AUTHORIZED TOP-UP PROVIDER CONFIG",
                    style = MaterialTheme.typography.labelSmall.copy(color = DiamondCyan, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Credentials sourced from environment variables (TOPUP_PROVIDER_API_URL / KEY). If empty, redemptions safely stay PENDING for authorized manual fulfillment.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = providerApiUrl,
                    onValueChange = { providerApiUrl = it },
                    label = { Text("Provider API URL (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = providerApiKey,
                    onValueChange = { providerApiKey = it },
                    label = { Text("Provider API Key (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (saveMessage != null) {
            Text(saveMessage!!, color = EmeraldGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val newConfig = config.copy(
                    quizPointsDefault = quizPoints.toIntOrNull() ?: 10,
                    rewardedAdPoints = adPoints.toIntOrNull() ?: 20,
                    dailyAdLimit = dailyAds.toIntOrNull() ?: 10,
                    diamondsPerThousandPoints = diamondsPer1k.toIntOrNull() ?: 60,
                    minRedemptionPoints = minPoints.toLongOrNull() ?: 1000L,
                    serverTimezone = serverTz.ifBlank { "UTC" },
                    topupProviderApiUrl = providerApiUrl,
                    topupProviderApiKey = providerApiKey
                )
                viewModel.adminUpdateConfig(newConfig)
                saveMessage = "Configuration saved successfully!"
            },
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Save Platform Config", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AdminUsersTab(
    users: List<com.example.data.model.UserProfile>,
    viewModel: QuizRewardsViewModel
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = if (searchQuery.isBlank()) {
        users
    } else {
        users.filter {
            it.displayName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Search users by name or email...") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GoldPrimary,
                unfocusedBorderColor = BorderSubtle
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Total Users: ${filteredUsers.size}",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredUsers) { u ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = u.displayName,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (u.isAdmin) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = GoldPrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "ADMIN",
                                            color = GoldPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${u.email} • ${u.points} pts • status: ${u.status}",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = { viewModel.adminToggleUserStatus(u.userId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (u.status == "active") CoralAccent else EmeraldGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (u.status == "active") "Suspend" else "Activate", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStatCard(title: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = TextSecondary, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun AdminMonthlyRedemptionsTab(
    redemptions: List<MonthlyRedemption>,
    windowInfo: MonthlyRedemptionWindowInfo,
    viewModel: QuizRewardsViewModel,
    onAction: (MonthlyRedemption, String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Current Month", "Pending", "Processing", "Completed", "Failed", "Rejected")

    val filteredList = when (selectedFilter) {
        "Current Month" -> redemptions.filter { it.month == windowInfo.currentMonthKey }
        "Pending" -> redemptions.filter { it.status == MonthlyRedemption.STATUS_PENDING }
        "Processing" -> redemptions.filter { it.status == MonthlyRedemption.STATUS_PROCESSING }
        "Completed" -> redemptions.filter { it.status == MonthlyRedemption.STATUS_COMPLETED }
        "Failed" -> redemptions.filter { it.status == MonthlyRedemption.STATUS_FAILED }
        "Rejected" -> redemptions.filter { it.status == MonthlyRedemption.STATUS_REJECTED }
        else -> redemptions
    }

    val pendingCount = redemptions.count { it.status == MonthlyRedemption.STATUS_PENDING }
    val processingCount = redemptions.count { it.status == MonthlyRedemption.STATUS_PROCESSING }
    val completedDiamonds = redemptions.filter { it.status == MonthlyRedemption.STATUS_COMPLETED }.sumOf { it.diamondAmount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            // Header stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdminStatCard(
                    title = "Pending Action",
                    value = "$pendingCount",
                    color = CoralAccent,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "In Processing",
                    value = "$processingCount",
                    color = GoldPrimary,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Diamonds Issued",
                    value = "$completedDiamonds 💎",
                    color = DiamondCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Window schedule status banner
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (windowInfo.isOpen) EmeraldGreen.copy(alpha = 0.5f) else BorderSubtle
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (windowInfo.isOpen) "Redemption Window is OPEN (5th–10th)" else "Redemption Window is CLOSED",
                            fontWeight = FontWeight.Bold,
                            color = if (windowInfo.isOpen) EmeraldGreen else CoralAccent,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "Server Time: ${windowInfo.serverTimezone} • ${windowInfo.countdownFormatted}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (windowInfo.isOpen) EmeraldGreen.copy(alpha = 0.2f) else DarkSurface)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (windowInfo.isOpen) "ACTIVE" else "CLOSED",
                            color = if (windowInfo.isOpen) EmeraldGreen else TextSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filters) { f ->
                    val isSelected = selectedFilter == f
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedFilter = f }
                            .testTag("admin_filter_$f"),
                        color = if (isSelected) GoldPrimary else DarkCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) GoldLight else BorderSubtle)
                    ) {
                        Text(
                            text = f,
                            color = if (isSelected) DarkBackground else TextSecondary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No monthly redemptions matching '$selectedFilter'.", color = TextSecondary)
                }
            }
        } else {
            items(filteredList) { req ->
                val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(req.createdAt))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("monthly_req_${req.id}"),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // User info and Status pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = req.userName.ifBlank { "User" },
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Text(
                                    text = "${req.userEmail} • ID: ${req.userId}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                    fontSize = 10.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (req.status) {
                                            MonthlyRedemption.STATUS_COMPLETED -> EmeraldGreen.copy(alpha = 0.2f)
                                            MonthlyRedemption.STATUS_PROCESSING -> GoldPrimary.copy(alpha = 0.2f)
                                            MonthlyRedemption.STATUS_REJECTED, MonthlyRedemption.STATUS_FAILED -> CoralAccent.copy(alpha = 0.2f)
                                            else -> DiamondCyan.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = req.status,
                                    color = when (req.status) {
                                        MonthlyRedemption.STATUS_COMPLETED -> EmeraldGreen
                                        MonthlyRedemption.STATUS_PROCESSING -> GoldLight
                                        MonthlyRedemption.STATUS_REJECTED, MonthlyRedemption.STATUS_FAILED -> CoralAccent
                                        else -> DiamondCyan
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Points & Diamonds highlight
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "💎 ${req.diamondAmount} Free Fire Diamonds",
                                color = GoldLight,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${req.pointsRedeemed} pts (${req.month})",
                                color = DiamondCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "🎮 Player ID: ${req.playerId}",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 12.sp
                        )

                        Text(
                            text = "Request ID: ${req.id} • Submitted: $dateStr",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )

                        if (!req.providerTransactionId.isNullOrBlank()) {
                            Text(
                                text = "Tx/Voucher: ${req.providerTransactionId}",
                                color = EmeraldGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        if (!req.failureReason.isNullOrBlank()) {
                            Text(
                                text = "Failure Reason: ${req.failureReason}",
                                color = CoralAccent,
                                fontSize = 11.sp
                            )
                        }

                        if (!req.adminNote.isNullOrBlank()) {
                            Text(
                                text = "Admin Note: ${req.adminNote}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }

                        // Action buttons
                        if (req.status == MonthlyRedemption.STATUS_PENDING || req.status == MonthlyRedemption.STATUS_PROCESSING) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (req.status == MonthlyRedemption.STATUS_PENDING) {
                                    Button(
                                        onClick = { onAction(req, "PROCESSING") },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GoldPrimary,
                                            contentColor = DarkBackground
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(34.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                                    ) {
                                        Text("Mark Processing", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = { onAction(req, "COMPLETE") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = EmeraldGreen,
                                        contentColor = DarkBackground
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                                ) {
                                    Text("Complete & Deliver", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { onAction(req, "REJECT") },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CoralAccent,
                                        contentColor = DarkBackground
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).height(34.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp)
                                ) {
                                    Text("Reject (Refund)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

