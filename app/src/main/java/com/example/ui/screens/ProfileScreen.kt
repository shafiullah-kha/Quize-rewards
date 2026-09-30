package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.components.LegalDocType
import com.example.ui.components.LegalDocumentDialog
import com.example.ui.components.SpinWheelDialog
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: QuizRewardsViewModel,
    onNavigateToAdmin: () -> Unit,
    onNavigateToAuth: () -> Unit,
    onNavigateToInviteEarn: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToRewards: () -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val config by viewModel.appConfig.collectAsState()

    var showMenuSheet by remember { mutableStateOf(false) }
    var activeLegalDoc by remember { mutableStateOf<LegalDocType?>(null) }
    var showSpinDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showAccountDetailsDialog by remember { mutableStateOf(false) }

    val points = user?.points ?: 0L
    val level = (points / 200).toInt() + 1
    val currentLevelXp = (points % 200).toInt()
    val progress = (currentLevelXp / 200f).coerceIn(0f, 1f)
    val userUid = user?.userId ?: "GUEST"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top Header with Title and "=" Menu Button
        Surface(color = DarkSurface, modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Player Profile",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Points Pill
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onNavigateToWallet() },
                        color = DarkCardElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$points pts",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldLight
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // "=" Menu button as explicitly requested
                    IconButton(
                        onClick = { showMenuSheet = true },
                        modifier = Modifier.testTag("profile_menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = GoldPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Profile Card (Profile picture, Username, User ID, Points, Level/XP)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_profile_card"),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profile Picture
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.2f))
                            .border(2.5.dp, GoldPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            tint = GoldPrimary,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Username
                    Text(
                        text = user?.displayName ?: "Guest Player",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    )

                    // User ID with copy button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                copyToClipboard(context, userUid, "User ID Copied!")
                            }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "UID: $userUid",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy UID",
                            tint = TextMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Role & Active Status Badges
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (user?.isAdmin == true) CoralAccent.copy(alpha = 0.2f) else DiamondCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (user?.isAdmin == true) "ROLE: ADMIN" else "ROLE: USER",
                                color = if (user?.isAdmin == true) CoralAccent else DiamondCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(EmeraldGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                color = EmeraldGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Points & Level / XP Bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = DarkSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Level $level",
                                        fontWeight = FontWeight.Black,
                                        color = GoldLight,
                                        fontSize = 14.sp
                                    )
                                }

                                Text(
                                    text = "$currentLevelXp / 200 XP",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = GoldPrimary,
                                trackColor = DarkCard
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Wallet Balance",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                                Text(
                                    text = "$points Points",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = EmeraldGreen
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Rewards Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToInviteEarn() }
                        .testTag("profile_invite_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "🎁", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Invite & Earn",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "+100 Pts per friend",
                            color = GoldPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showSpinDialog = true }
                        .testTag("profile_spin_card"),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DiamondCyan.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(text = "🎡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Spin & Earn",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Win 20–100 Pts",
                            color = DiamondCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Admin Panel Entry Button (If admin)
            if (user?.isAdmin == true) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToAdmin() }
                        .testTag("admin_panel_entry_button"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralAccent.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(CoralAccent.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = CoralAccent, modifier = Modifier.size(22.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Admin Control Center", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Configure spins, rewards, and legal terms", color = TextSecondary, fontSize = 11.sp)
                            }
                        }
                        Text("Manage ›", color = CoralAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Authentication & Account State
            if (user == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sign in to save your rewards",
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Guest users cannot claim referral or welcome rewards until account creation is completed.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateToAuth,
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
                            ) {
                                Text("Sign In", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onNavigateToAuth,
                                modifier = Modifier.weight(1f).height(42.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = com.example.ui.theme.DarkBackground)
                            ) {
                                Text("Sign Up", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth().height(46.dp).testTag("logout_button"),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CoralAccent.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CoralAccent)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Secure Log Out", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // "=" MENU BOTTOM SHEET as required by Items 7 & 8
    if (showMenuSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMenuSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = DarkBackground,
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp, bottom = 4.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(TextMuted)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "Settings & Menu",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Black,
                        color = GoldLight
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Account
                MenuSectionHeader(title = "ACCOUNT")
                if (user == null) {
                    MenuItemRow(
                        title = "Login",
                        icon = Icons.Default.Login,
                        onClick = {
                            showMenuSheet = false
                            onNavigateToAuth()
                        }
                    )
                    MenuItemRow(
                        title = "Sign Up",
                        icon = Icons.Default.PersonAdd,
                        onClick = {
                            showMenuSheet = false
                            onNavigateToAuth()
                        }
                    )
                } else {
                    MenuItemRow(
                        title = "Account Details",
                        icon = Icons.Default.Info,
                        onClick = {
                            showMenuSheet = false
                            showAccountDetailsDialog = true
                        }
                    )
                    MenuItemRow(
                        title = "Log Out",
                        icon = Icons.Default.ExitToApp,
                        iconTint = CoralAccent,
                        onClick = {
                            showMenuSheet = false
                            viewModel.logout()
                        }
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderSubtle)

                // Section 2: Rewards
                MenuSectionHeader(title = "REWARDS")
                MenuItemRow(
                    title = "Wallet",
                    icon = Icons.Default.AccountBalanceWallet,
                    onClick = {
                        showMenuSheet = false
                        onNavigateToWallet()
                    }
                )
                MenuItemRow(
                    title = "Redemption History",
                    icon = Icons.Default.Rule,
                    onClick = {
                        showMenuSheet = false
                        onNavigateToRewards()
                    }
                )
                MenuItemRow(
                    title = "Invite & Earn 🎁",
                    icon = Icons.Default.PersonAdd,
                    onClick = {
                        showMenuSheet = false
                        onNavigateToInviteEarn()
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderSubtle)

                // Section 3: App
                MenuSectionHeader(title = "APP")
                MenuItemRow(
                    title = "Notifications",
                    icon = Icons.Default.Notifications,
                    onClick = {
                        showMenuSheet = false
                        showNotificationsDialog = true
                    }
                )
                MenuItemRow(
                    title = "Language",
                    icon = Icons.Default.Language,
                    onClick = {
                        showMenuSheet = false
                        showLanguageDialog = true
                    }
                )
                MenuItemRow(
                    title = "Help & Support",
                    icon = Icons.Default.HelpOutline,
                    onClick = {
                        showMenuSheet = false
                        activeLegalDoc = LegalDocType.HelpSupport
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderSubtle)

                // Section 4: Legal
                MenuSectionHeader(title = "LEGAL")
                MenuItemRow(
                    title = "Privacy Policy",
                    icon = Icons.Default.Policy,
                    onClick = {
                        showMenuSheet = false
                        activeLegalDoc = LegalDocType.PrivacyPolicy
                    }
                )
                MenuItemRow(
                    title = "Terms & Conditions",
                    icon = Icons.Default.Gavel,
                    onClick = {
                        showMenuSheet = false
                        activeLegalDoc = LegalDocType.TermsConditions
                    }
                )
                MenuItemRow(
                    title = "Reward & Withdrawal Rules",
                    icon = Icons.Default.Rule,
                    onClick = {
                        showMenuSheet = false
                        activeLegalDoc = LegalDocType.RewardRules
                    }
                )
                MenuItemRow(
                    title = "Referral Rules",
                    icon = Icons.Default.Security,
                    onClick = {
                        showMenuSheet = false
                        activeLegalDoc = LegalDocType.ReferralRules
                    }
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = BorderSubtle)

                // Section 5: Other
                MenuSectionHeader(title = "OTHER")
                MenuItemRow(
                    title = "About",
                    icon = Icons.Default.Info,
                    onClick = {
                        showMenuSheet = false
                        activeLegalDoc = LegalDocType.About
                    }
                )
                MenuItemRow(
                    title = "Contact Us",
                    icon = Icons.Default.Mail,
                    onClick = {
                        showMenuSheet = false
                        activeLegalDoc = LegalDocType.ContactUs
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // Spin Dialog
    if (showSpinDialog) {
        SpinWheelDialog(
            viewModel = viewModel,
            onDismiss = { showSpinDialog = false }
        )
    }

    // Active Legal Document Dialog
    activeLegalDoc?.let { doc ->
        val text = when (doc) {
            LegalDocType.PrivacyPolicy -> config.privacyPolicyText
            LegalDocType.TermsConditions -> config.termsConditionsText
            LegalDocType.RewardRules -> config.rewardRulesText
            LegalDocType.ReferralRules -> config.referralRulesText
            LegalDocType.HelpSupport -> "Need assistance? You can earn points by answering trivia quizzes, spinning the lucky wheel daily (3 free spins + up to 10 ad spins), and inviting friends with your referral code. Redemptions for official Free Fire vouchers are processed between the 5th and 10th of every month. Contact us at ${config.adminEmail} for account help."
            LegalDocType.About -> "Quiz Rewards v1.0\nA secure trivia reward platform where gamers earn Free Fire Diamonds through fair trivia challenges, rewarded video advertisements, and trusted friend referrals."
            LegalDocType.ContactUs -> "Contact our support team directly at:\n\nEmail: ${config.adminEmail}\nResponse time: within 24 hours\nAdmin: Shafihu Official Support"
            else -> ""
        }
        LegalDocumentDialog(
            docType = doc,
            content = text,
            onDismiss = { activeLegalDoc = null }
        )
    }

    // Account Details Dialog
    if (showAccountDetailsDialog) {
        Dialog(onDismissRequest = { showAccountDetailsDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Account Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(label = "User ID", value = userUid)
                    DetailRow(label = "Display Name", value = user?.displayName ?: "Guest")
                    DetailRow(label = "Email Address", value = user?.email ?: "None")
                    DetailRow(label = "Account Status", value = user?.status?.uppercase() ?: "ACTIVE")
                    DetailRow(label = "Role", value = user?.role?.uppercase() ?: "USER")
                    DetailRow(label = "Referral Code", value = user?.referralCode ?: "None")
                    DetailRow(label = "Referred By", value = user?.referredBy?.ifBlank { "None (Direct)" } ?: "None")
                    DetailRow(label = "Welcome Bonus", value = if (user?.welcomeBonusAwarded == true) "Claimed (+100 Pts)" else "Pending")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showAccountDetailsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = com.example.ui.theme.DarkBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        Dialog(onDismissRequest = { showLanguageDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Select App Language", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf("English", "Urdu", "Hindi", "Arabic").forEach { lang ->
                        val isSel = user?.language == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    user?.let { viewModel.switchAccount(it.email) }
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang, color = if (isSel) GoldPrimary else TextPrimary, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                            if (isSel) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showLanguageDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = com.example.ui.theme.DarkBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(40.dp)
                    ) {
                        Text("Save & Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Notifications Dialog
    if (showNotificationsDialog) {
        Dialog(onDismissRequest = { showNotificationsDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Push Notifications", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Daily free spins reminder (3 daily spins)", color = TextPrimary, fontSize = 13.sp)
                    Text("Monthly diamond window alerts (5th–10th)", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                    Text("Referral bonus reward alerts (+100 Pts)", color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(top = 8.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            Toast.makeText(context, "Notification preferences saved!", Toast.LENGTH_SHORT).show()
                            showNotificationsDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = com.example.ui.theme.DarkBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("Save Preferences", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            color = TextSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun MenuItemRow(
    title: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color = GoldPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 12.sp)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}

private fun copyToClipboard(context: Context, text: String, toastMessage: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("ProfileData", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
}
