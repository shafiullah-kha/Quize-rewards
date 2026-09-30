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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RedemptionRequest
import com.example.data.model.RewardItem
import com.example.ui.components.GamingTopBar
import com.example.ui.components.RedemptionConfirmationDialog
import com.example.ui.components.SpinEarnCard
import com.example.ui.components.SpinWheelDialog
import com.example.ui.components.StatusChip
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.DiamondDark
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
fun RewardsScreen(
    viewModel: QuizRewardsViewModel,
    onNavigateToWallet: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val rewards by viewModel.rewards.collectAsState()
    val myRedemptions by viewModel.myRedemptions.collectAsState()
    val currentPoints = user?.points ?: 0L

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Reward Store", "My Requests (${myRedemptions.size})")

    val selectedReward by viewModel.selectedRewardForRedemption.collectAsState()
    val recipientInput by viewModel.recipientInput.collectAsState()
    val feedbackMessage by viewModel.redemptionFeedback.collectAsState()
    val windowInfo by viewModel.monthlyWindowInfo.collectAsState()
    var showSpinWheelDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        GamingTopBar(
            title = "Reward Center",
            points = currentPoints,
            onPointsClick = onNavigateToWallet,
            showAdminBadge = user?.isAdmin == true
        )

        // Policy & Transparency Notice Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkCard)
                .border(1.dp, DiamondCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = DiamondCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Official Voucher Delivery Process",
                        color = DiamondCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real Free Fire rewards are processed via authorized top-ups or gift vouchers by administrators. Diamonds are never generated automatically.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }
        }

        // Monthly Diamond Redemption Schedule Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (windowInfo.isOpen) EmeraldGreen.copy(alpha = 0.12f) else DarkCardElevated
                )
                .border(
                    1.dp,
                    if (windowInfo.isOpen) EmeraldGreen.copy(alpha = 0.5f) else GoldPrimary.copy(alpha = 0.3f),
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Diamond,
                        contentDescription = null,
                        tint = if (windowInfo.isOpen) EmeraldGreen else GoldLight,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (windowInfo.isOpen) {
                                "Monthly Redemption is OPEN (5th–10th)"
                            } else {
                                "Monthly Redemption: 5th–10th of Month"
                            },
                            color = if (windowInfo.isOpen) EmeraldGreen else GoldLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (windowInfo.isOpen) {
                                "${windowInfo.countdownFormatted} • Points never expire"
                            } else {
                                "Next: ${windowInfo.nextWindowStartDate} (${windowInfo.countdownFormatted})"
                            },
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Button(
                    onClick = onNavigateToWallet,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (windowInfo.isOpen) EmeraldGreen else DarkCard,
                        contentColor = if (windowInfo.isOpen) DarkBackground else GoldLight
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = if (windowInfo.isOpen) "Redeem Now" else "View Wallet",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Tabs
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = DarkSurface,
            contentColor = GoldPrimary,
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

        if (feedbackMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = if (feedbackMessage!!.startsWith("Success")) EmeraldGreen.copy(alpha = 0.15f) else CoralAccent.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (feedbackMessage!!.startsWith("Success")) EmeraldGreen else CoralAccent
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (feedbackMessage!!.startsWith("Success")) Icons.Default.CheckCircle else Icons.Default.Info,
                        contentDescription = null,
                        tint = if (feedbackMessage!!.startsWith("Success")) EmeraldGreen else CoralAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = feedbackMessage ?: "",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (selectedTabIndex == 0) {
            // Reward Store List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Spin & Earn horizontal reward banner
                item {
                    SpinEarnCard(
                        viewModel = viewModel,
                        onOpenSpinDialog = { showSpinWheelDialog = true },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                items(rewards.filter { it.enabled }) { item ->
                    val canAfford = currentPoints >= item.requiredPoints
                    val progress = (currentPoints.toFloat() / item.requiredPoints.toFloat()).coerceIn(0f, 1f)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("reward_item_${item.rewardId}"),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (canAfford) GoldPrimary.copy(alpha = 0.5f) else BorderSubtle
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (item.bannerIcon == "diamond") DiamondDark else DarkCardElevated
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (item.bannerIcon == "diamond") Icons.Default.Diamond else Icons.Default.CardGiftcard,
                                            contentDescription = null,
                                            tint = if (item.bannerIcon == "diamond") DiamondCyan else GoldLight,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                        Text(
                                            text = item.category,
                                            style = MaterialTheme.typography.labelSmall.copy(color = DiamondCyan)
                                        )
                                    }
                                }

                                Surface(
                                    color = DarkCardElevated,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.3f))
                                ) {
                                    Text(
                                        text = "${item.requiredPoints} pts",
                                        color = GoldLight,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Points requirement progress bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (canAfford) "Ready to Redeem!" else "Progress: $currentPoints / ${item.requiredPoints} pts",
                                    color = if (canAfford) EmeraldGreen else TextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${(progress * 100).toInt()}%",
                                    color = if (canAfford) EmeraldGreen else GoldLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (canAfford) EmeraldGreen else GoldPrimary,
                                trackColor = DarkCardElevated
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { viewModel.openRedemptionDialog(item) },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GoldPrimary,
                                    contentColor = DarkBackground,
                                    disabledContainerColor = DarkCardElevated,
                                    disabledContentColor = TextMuted
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .testTag("redeem_button_${item.rewardId}")
                            ) {
                                if (canAfford) {
                                    Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Redeem Reward", fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Need ${item.requiredPoints - currentPoints} More Points", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        } else {
            // My Redemption Requests Tab
            if (myRedemptions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No redemption requests yet",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Earn points through quizzes and rewarded ads to redeem your first reward.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(myRedemptions) { req ->
                        val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                            .format(Date(req.createdAt))

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .testTag("redemption_card_${req.requestId}"),
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
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = DiamondCyan,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        )
                                        Text(
                                            text = req.rewardTitle,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        )
                                    }
                                    StatusChip(status = req.status)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Points used: ${req.points} pts",
                                        color = GoldLight,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = dateStr,
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }

                                if (req.recipientDetails.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Target: ${req.recipientDetails}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }

                                if (req.adminNote.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(DarkCardElevated)
                                            .border(1.dp, GoldPrimary.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                            .padding(10.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = "Admin Note / Voucher Details:",
                                                color = GoldLight,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = req.adminNote,
                                                color = TextPrimary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    // Confirmation Modal
    if (selectedReward != null) {
        RedemptionConfirmationDialog(
            reward = selectedReward!!,
            userPoints = currentPoints,
            recipientInput = recipientInput,
            onRecipientChange = { viewModel.setRecipientInput(it) },
            onConfirm = { viewModel.confirmRedemption() },
            onDismiss = { viewModel.closeRedemptionDialog() }
        )
    }

    if (showSpinWheelDialog) {
        SpinWheelDialog(
            viewModel = viewModel,
            onDismiss = { showSpinWheelDialog = false }
        )
    }
}
