package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldLight
import com.example.ui.theme.TextPrimary
import com.example.ui.components.TestAdSimulationDialog
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InviteEarnScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.WalletScreen
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.QuizRewardsTheme
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.QuizRewardsViewModel

object AppRoutes {
    const val HOME = "home"
    const val QUIZ = "quiz"
    const val REWARDS = "rewards"
    const val WALLET = "wallet"
    const val PROFILE = "profile"
    const val ADMIN = "admin"
    const val AUTH = "auth"
    const val INVITE_EARN = "invite_earn"
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomNavItem(AppRoutes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Quiz : BottomNavItem(AppRoutes.QUIZ, "Quiz", Icons.Filled.Quiz, Icons.Outlined.Quiz)
    object Rewards : BottomNavItem(AppRoutes.REWARDS, "Rewards", Icons.Filled.Diamond, Icons.Outlined.Diamond)
    object Wallet : BottomNavItem(AppRoutes.WALLET, "Wallet", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet)
    object Profile : BottomNavItem(AppRoutes.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizRewardsTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val viewModel: QuizRewardsViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val isShowingSimulation by viewModel.isShowingAdSimulation.collectAsState()
    val adCountdown by viewModel.adCountdownSeconds.collectAsState()
    val adProgress by viewModel.adSimulationProgress.collectAsState()
    val welcomeBonusMessage by viewModel.welcomeBonusMessage.collectAsState()

    val navItems = listOf(
        BottomNavItem.Home,
        BottomNavItem.Quiz,
        BottomNavItem.Rewards,
        BottomNavItem.Wallet,
        BottomNavItem.Profile
    )

    val showBottomBar = currentRoute in listOf(
        AppRoutes.HOME,
        AppRoutes.QUIZ,
        AppRoutes.REWARDS,
        AppRoutes.WALLET,
        AppRoutes.PROFILE
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = DarkSurface,
                    modifier = Modifier.navigationBarsPadding().testTag("bottom_nav_bar")
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentRoute == item.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GoldPrimary,
                                selectedTextColor = GoldPrimary,
                                indicatorColor = DarkCard,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextSecondary
                            ),
                            modifier = Modifier.testTag("nav_item_${item.route}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppRoutes.HOME) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToQuiz = { navController.navigate(AppRoutes.QUIZ) },
                    onNavigateToRewards = { navController.navigate(AppRoutes.REWARDS) },
                    onNavigateToWallet = { navController.navigate(AppRoutes.WALLET) },
                    onNavigateToProfile = { navController.navigate(AppRoutes.PROFILE) }
                )
            }
            composable(AppRoutes.QUIZ) {
                QuizScreen(
                    viewModel = viewModel,
                    onNavigateToWallet = { navController.navigate(AppRoutes.WALLET) }
                )
            }
            composable(AppRoutes.REWARDS) {
                RewardsScreen(
                    viewModel = viewModel,
                    onNavigateToWallet = { navController.navigate(AppRoutes.WALLET) }
                )
            }
            composable(AppRoutes.WALLET) {
                WalletScreen(viewModel = viewModel)
            }
            composable(AppRoutes.PROFILE) {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToAdmin = { navController.navigate(AppRoutes.ADMIN) },
                    onNavigateToAuth = { navController.navigate(AppRoutes.AUTH) },
                    onNavigateToInviteEarn = { navController.navigate(AppRoutes.INVITE_EARN) },
                    onNavigateToWallet = { navController.navigate(AppRoutes.WALLET) },
                    onNavigateToRewards = { navController.navigate(AppRoutes.REWARDS) }
                )
            }
            composable(AppRoutes.INVITE_EARN) {
                InviteEarnScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.ADMIN) {
                AdminScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoutes.AUTH) {
                AuthScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onAuthSuccess = { navController.popBackStack() }
                )
            }
        }
    }

    // Rewarded Video Playback Modal
    if (isShowingSimulation) {
        TestAdSimulationDialog(
            countdownSeconds = adCountdown,
            progress = adProgress,
            onCancel = { viewModel.cancelAdSimulation() }
        )
    }

    // Welcome Gift In-App Celebratory Notification Modal
    if (welcomeBonusMessage != null) {
        Dialog(onDismissRequest = { viewModel.dismissWelcomeBonusMessage() }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .testTag("welcome_bonus_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🎁", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Welcome Gift Added!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = GoldLight
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = welcomeBonusMessage ?: "Welcome Gift 🎁 +100 Points added!",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Your new player welcome bonus has been safely authorized and added to your balance.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.dismissWelcomeBonusMessage() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("claim_welcome_bonus_button")
                    ) {
                        Text("Claim Points 🎁", fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
