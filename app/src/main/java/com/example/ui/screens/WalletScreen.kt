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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.example.data.model.MonthlyRedemption
import com.example.data.model.MonthlyRedemptionWindowInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.components.GamingTopBar
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DiamondCyan
import com.example.ui.theme.EmeraldGreen
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
fun WalletScreen(
    viewModel: QuizRewardsViewModel
) {
    val user by viewModel.currentUser.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val breakdown by viewModel.walletBreakdown.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Quizzes", "Rewarded Ads", "Redemptions")

    val filteredTransactions = when (selectedFilter) {
        "Quizzes" -> transactions.filter { it.type == "QUIZ_EARN" }
        "Rewarded Ads" -> transactions.filter { it.type == "AD_EARN" }
        "Redemptions" -> transactions.filter { it.type.startsWith("REDEMPTION") }
        else -> transactions
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        GamingTopBar(
            title = "My Wallet",
            points = breakdown.totalPoints,
            showAdminBadge = user?.isAdmin == true
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))

                // Hero Balance Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wallet_balance_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AVAILABLE POINTS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${breakdown.totalPoints}",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = GoldLight
                                )
                            )
                            Text(
                                text = " pts",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Text(
                            text = "💎 1,000 Points = 60 Free Fire Diamonds (Min. Redemption: 1,000 pts)",
                            style = MaterialTheme.typography.bodySmall.copy(color = DiamondCyan, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // 6 Points Categories Breakdown Grid
            item {
                // Row 1: Total Points & Available Points
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WalletStatCard(
                        title = "Total Points",
                        value = "${breakdown.earnedFromQuizzes + breakdown.earnedFromAds} pts",
                        color = GoldLight,
                        icon = Icons.Default.MonetizationOn,
                        modifier = Modifier.weight(1f)
                    )

                    WalletStatCard(
                        title = "Available Points",
                        value = "${breakdown.totalPoints} pts",
                        color = DiamondCyan,
                        icon = Icons.Default.MonetizationOn,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 2: Quiz Points & Advertisement Points
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WalletStatCard(
                        title = "Quiz Points",
                        value = "+${breakdown.earnedFromQuizzes} pts",
                        color = GoldPrimary,
                        icon = Icons.Default.Quiz,
                        modifier = Modifier.weight(1f)
                    )

                    WalletStatCard(
                        title = "Advertisement Points",
                        value = "+${breakdown.earnedFromAds} pts",
                        color = DiamondCyan,
                        icon = Icons.Default.SmartDisplay,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 3: Redeemed Points & Pending Points
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    WalletStatCard(
                        title = "Redeemed Points",
                        value = "-${breakdown.redeemedPoints} pts",
                        color = EmeraldGreen,
                        icon = Icons.Default.ShoppingBag,
                        modifier = Modifier.weight(1f)
                    )

                    WalletStatCard(
                        title = "Pending Points",
                        value = "${breakdown.pendingPoints} pts",
                        color = CoralAccent,
                        icon = Icons.Default.HourglassTop,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Monthly Diamond Redemption Section
            item {
                MonthlyDiamondRedemptionSection(viewModel)
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Transaction Filter Chips
            item {
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { f ->
                        val isSelected = selectedFilter == f
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedFilter = f }
                                .testTag("filter_$f"),
                            color = if (isSelected) GoldPrimary else DarkCard,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GoldLight else BorderSubtle
                            )
                        ) {
                            Text(
                                text = f,
                                color = if (isSelected) DarkBackground else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Transactions list
            if (filteredTransactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No transactions found.", color = TextSecondary)
                    }
                }
            } else {
                items(filteredTransactions) { tx ->
                    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                        .format(Date(tx.createdAt))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .testTag("tx_card_${tx.transactionId}"),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                tx.points > 0 -> EmeraldGreen.copy(alpha = 0.2f)
                                                else -> CoralAccent.copy(alpha = 0.2f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when {
                                            tx.type == "QUIZ_EARN" -> Icons.Default.Quiz
                                            tx.type == "AD_EARN" -> Icons.Default.SmartDisplay
                                            else -> Icons.Default.ShoppingBag
                                        },
                                        contentDescription = null,
                                        tint = if (tx.points > 0) EmeraldGreen else CoralAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = tx.source,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "ID: ${tx.transactionId} • Type: ${tx.type} • Ref: ${tx.referenceId.take(12)}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = DiamondCyan, fontSize = 10.sp)
                                    )
                                    Text(
                                        text = dateStr,
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                                    )
                                }
                            }

                            Text(
                                text = if (tx.points > 0) "+${tx.points}" else "${tx.points}",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (tx.points > 0) EmeraldGreen else CoralAccent
                                )
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun WalletStatCard(
    title: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = color
                )
            )
        }
    }
}

@Composable
fun MonthlyDiamondRedemptionSection(viewModel: QuizRewardsViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val windowInfo by viewModel.monthlyWindowInfo.collectAsState()
    val config by viewModel.appConfig.collectAsState()
    val feedbackMessage by viewModel.monthlyRedemptionFeedback.collectAsState()
    val isSubmitting by viewModel.monthlyRedemptionIsSubmitting.collectAsState()

    val currentPoints = user?.points ?: 0L
    var playerIdInput by remember { mutableStateOf("") }
    var selectedPoints by remember { mutableStateOf(1000L) }

    val diamondsPer1k = config.diamondsPerThousandPoints
    val selectedDiamonds = ((selectedPoints / 1000L) * diamondsPer1k).toInt()

    val availableDiamonds = ((currentPoints / 1000L) * diamondsPer1k).toInt()
    val hasEnoughPoints = currentPoints >= selectedPoints
    val canRedeem = windowInfo.isOpen && hasEnoughPoints && playerIdInput.trim().length >= 5 && !windowInfo.hasSubmittedThisMonth

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("monthly_redemption_card"),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (windowInfo.isOpen) EmeraldGreen.copy(alpha = 0.6f) else DiamondCyan.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header: Title & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (windowInfo.isOpen) EmeraldGreen.copy(alpha = 0.2f) else DiamondCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = if (windowInfo.isOpen) EmeraldGreen else DiamondCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Monthly Diamond Redemption",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Free Fire Diamond Rewards",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                }

                // Window Status Pill Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (windowInfo.isOpen) EmeraldGreen.copy(alpha = 0.2f) else CoralAccent.copy(alpha = 0.15f)
                        )
                        .border(
                            1.dp,
                            if (windowInfo.isOpen) EmeraldGreen else CoralAccent.copy(alpha = 0.6f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (windowInfo.isOpen) "OPEN (5th–10th)" else "CLOSED",
                        color = if (windowInfo.isOpen) EmeraldGreen else CoralAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Window Status & Countdown Box
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (windowInfo.isOpen) Icons.Default.Timer else Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = if (windowInfo.isOpen) EmeraldGreen else GoldLight,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (windowInfo.isOpen) {
                                "Redemption is OPEN — redeem your Diamonds before the 10th."
                            } else {
                                "Redemption is currently closed."
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = if (windowInfo.isOpen) EmeraldGreen else TextPrimary
                            )
                        )
                    }

                    if (!windowInfo.isOpen) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Redemption opens on the 5th and remains available until the 10th.",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Next Redemption: ${windowInfo.nextWindowStartDate}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = GoldLight,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Live Countdown Tag
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkCard)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (windowInfo.isOpen) "Window Countdown" else "Opens Countdown",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                        Text(
                            text = windowInfo.countdownFormatted.ifBlank { "Synchronizing server time..." },
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = if (windowInfo.isOpen) EmeraldGreen else GoldPrimary,
                                fontWeight = FontWeight.Black
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "🌐 Server Time: ${windowInfo.serverTimezone} (Day ${windowInfo.currentServerDay})",
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                        Text(
                            text = "🛡️ Points never expire",
                            fontSize = 10.sp,
                            color = DiamondCyan,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Check if user already submitted for this monthly cycle
            val existing = windowInfo.existingMonthlyRedemption
            if (windowInfo.hasSubmittedThisMonth && existing != null) {
                // Show Existing Submission Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monthly Request (${existing.month})",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        when (existing.status) {
                                            MonthlyRedemption.STATUS_COMPLETED -> EmeraldGreen.copy(alpha = 0.2f)
                                            MonthlyRedemption.STATUS_PROCESSING -> GoldPrimary.copy(alpha = 0.2f)
                                            MonthlyRedemption.STATUS_REJECTED, MonthlyRedemption.STATUS_FAILED -> CoralAccent.copy(alpha = 0.2f)
                                            else -> DiamondCyan.copy(alpha = 0.2f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = existing.status,
                                    color = when (existing.status) {
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
                        Text(
                            text = "💎 ${existing.diamondAmount} Free Fire Diamonds • ${existing.pointsRedeemed} pts used",
                            style = MaterialTheme.typography.bodyMedium.copy(color = GoldLight, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Player ID: ${existing.playerId} • Request ID: ${existing.id}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                        if (!existing.providerTransactionId.isNullOrBlank()) {
                            Text(
                                text = "Voucher/Tx ID: ${existing.providerTransactionId}",
                                style = MaterialTheme.typography.labelSmall.copy(color = EmeraldGreen, fontWeight = FontWeight.SemiBold)
                            )
                        }
                        if (!existing.adminNote.isNullOrBlank()) {
                            Text(
                                text = "Note: ${existing.adminNote}",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Only one redemption request is permitted per monthly cycle. Remaining points carry forward to the next month automatically.",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 10.sp)
                        )
                    }
                }
            } else {
                // User has NOT yet submitted for this month: Show redemption controls

                // Point conversion & value banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurface)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "YOUR AVAILABLE POINTS",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$currentPoints pts",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldLight
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "DIAMOND VALUE",
                            fontSize = 10.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "💎 $availableDiamonds Diamonds",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = DiamondCyan
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tier Selector
                Text(
                    text = "Select Diamond Amount (1,000 pts = $diamondsPer1k 💎):",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                val tierOptions = listOf(1000L, 2000L, 3000L, 4000L, 5000L)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tierOptions) { pts ->
                        val diamonds = ((pts / 1000L) * diamondsPer1k).toInt()
                        val isSelected = selectedPoints == pts
                        val isAffordable = currentPoints >= pts

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(enabled = isAffordable) {
                                    selectedPoints = pts
                                }
                                .testTag("monthly_tier_$pts"),
                            color = if (isSelected) GoldPrimary else if (isAffordable) DarkSurface else DarkCardElevated.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) GoldLight else if (isAffordable) BorderSubtle else BorderSubtle.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "💎 $diamonds",
                                    color = if (isSelected) DarkBackground else if (isAffordable) DiamondCyan else TextMuted,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$pts pts",
                                    color = if (isSelected) DarkBackground else if (isAffordable) TextSecondary else TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Free Fire Player ID Input
                OutlinedTextField(
                    value = playerIdInput,
                    onValueChange = { playerIdInput = it },
                    label = { Text("Free Fire Player ID") },
                    placeholder = { Text("e.g. 104829104") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = DiamondCyan)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ff_player_id_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DiamondCyan,
                        unfocusedBorderColor = BorderSubtle,
                        focusedLabelColor = DiamondCyan,
                        cursorColor = DiamondCyan
                    ),
                    singleLine = true
                )

                Text(
                    text = "Official Player ID only. We never ask for your Free Fire password.",
                    fontSize = 10.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Feedback Message (if any)
                if (feedbackMessage != null) {
                    val isSuccess = feedbackMessage!!.startsWith("Success")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSuccess) EmeraldGreen.copy(alpha = 0.15f) else CoralAccent.copy(alpha = 0.15f)
                            )
                            .border(
                                1.dp,
                                if (isSuccess) EmeraldGreen else CoralAccent,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Info,
                                contentDescription = null,
                                tint = if (isSuccess) EmeraldGreen else CoralAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = feedbackMessage ?: "",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Action Button
                Button(
                    onClick = {
                        viewModel.submitMonthlyDiamondRedemption(
                            points = selectedPoints,
                            playerId = playerIdInput.trim()
                        )
                    },
                    enabled = canRedeem && !isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = DarkBackground,
                        disabledContainerColor = DarkSurface,
                        disabledContentColor = TextMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("submit_monthly_redemption_btn")
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            color = DarkBackground,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Validating with Server...", fontWeight = FontWeight.Bold)
                    } else if (!windowInfo.isOpen) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Redemption Closed (Opens 5th)",
                            fontWeight = FontWeight.Bold
                        )
                    } else if (!hasEnoughPoints) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Insufficient Points (Need $selectedPoints pts)",
                            fontWeight = FontWeight.Bold
                        )
                    } else if (playerIdInput.trim().length < 5) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Enter Free Fire Player ID to Redeem",
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(Icons.Default.Diamond, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Redeem $selectedDiamonds Diamonds ($selectedPoints pts)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Deducts only $selectedPoints points. Remainder remains in wallet and never expires.",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

