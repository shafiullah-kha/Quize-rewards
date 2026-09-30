package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SpinTransaction
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
import com.example.util.findActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 9 server-validated points segments: 20, 30, 40, 50, 60, 70, 80, 90, 100
val SPIN_SEGMENTS = listOf(20L, 30L, 40L, 50L, 60L, 70L, 80L, 90L, 100L)
val SEGMENT_COLORS = listOf(
    Color(0xFFE5A93C), // Gold
    Color(0xFF26A69A), // Teal
    Color(0xFFE74C3C), // Coral
    Color(0xFF3498DB), // Blue
    Color(0xFF9B59B6), // Purple
    Color(0xFF2ECC71), // Emerald
    Color(0xFFF39C12), // Orange
    Color(0xFF1ABC9C), // Cyan
    Color(0xFFD4AF37)  // Bright Gold
)

@Composable
fun SpinWheelDialog(
    viewModel: QuizRewardsViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val coroutineScope = rememberCoroutineScope()

    val spinDaily by viewModel.spinDaily.collectAsState()
    val spinHistory by viewModel.spinHistory.collectAsState()
    val isSpinning by viewModel.isSpinning.collectAsState()
    val appConfig by viewModel.appConfig.collectAsState()
    val spinFeedback by viewModel.spinFeedback.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var winPointsDialog by remember { mutableStateOf<Long?>(null) }

    // Wheel rotation animation state
    val rotationAngle = remember { Animatable(0f) }

    val freeSpinsLeft = spinDaily.freeSpins
    val extraSpinsAvailable = spinDaily.extraSpinsAvailable
    val totalAvailableSpins = freeSpinsLeft + extraSpinsAvailable
    val adSpinsUsedToday = spinDaily.adSpins
    val maxAdSpins = appConfig.dailyAdSpinLimit
    val canWatchAd = adSpinsUsedToday < maxAdSpins

    Dialog(
        onDismissRequest = { if (!isSpinning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("spin_wheel_dialog"),
            colors = CardDefaults.cardColors(containerColor = DarkBackground),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🎡",
                            fontSize = 24.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Spin & Earn",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = GoldLight
                                )
                            )
                            Text(
                                text = "Win 20–100 Points every spin!",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                            )
                        }
                    }

                    IconButton(
                        onClick = { if (!isSpinning) onDismiss() },
                        enabled = !isSpinning
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tabs: Wheel vs History
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = DarkSurface,
                    contentColor = GoldPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GoldPrimary,
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Lucky Wheel", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Spin History (${spinHistory.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTab == 0) {
                    // Wheel View
                    WheelView(
                        viewModel = viewModel,
                        activity = activity,
                        rotationAngle = rotationAngle.value,
                        isSpinning = isSpinning,
                        freeSpinsLeft = freeSpinsLeft,
                        extraSpinsAvailable = extraSpinsAvailable,
                        adSpinsUsedToday = adSpinsUsedToday,
                        maxAdSpins = maxAdSpins,
                        canWatchAd = canWatchAd,
                        spinFeedback = spinFeedback,
                        onSpinClick = {
                            viewModel.spinWheel(
                                onResult = { result ->
                                    coroutineScope.launch {
                                        // Find segment index
                                        val segmentIndex = SPIN_SEGMENTS.indexOf(result.points).coerceAtLeast(0)
                                        val sweep = 360f / SPIN_SEGMENTS.size
                                        // Target angle: pointer is at top (270 degrees)
                                        val targetAngleOffset = 270f - (segmentIndex * sweep + sweep / 2f)
                                        val fullSpins = 5 * 360f
                                        val currentBase = (rotationAngle.value % 360f)
                                        val finalAngle = rotationAngle.value + fullSpins + (targetAngleOffset - currentBase)

                                        rotationAngle.animateTo(
                                            targetValue = finalAngle,
                                            animationSpec = tween(
                                                durationMillis = 3500,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                        winPointsDialog = result.points
                                    }
                                },
                                onError = {
                                    // Error handled in viewModel state
                                }
                            )
                        }
                    )
                } else {
                    // History View
                    SpinHistoryView(spinHistory = spinHistory)
                }
            }
        }
    }

    // Win Celebration Popup
    if (winPointsDialog != null) {
        val pts = winPointsDialog!!
        Dialog(onDismissRequest = { winPointsDialog = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                border = androidx.compose.foundation.BorderStroke(2.dp, GoldPrimary),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🎉", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Congratulations!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = GoldLight
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "+$pts Points 🎁",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "The points have been securely credited to your wallet balance.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = { winPointsDialog = null },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Text("Claim & Continue", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WheelView(
    viewModel: QuizRewardsViewModel,
    activity: Activity?,
    rotationAngle: Float,
    isSpinning: Boolean,
    freeSpinsLeft: Int,
    extraSpinsAvailable: Int,
    adSpinsUsedToday: Int,
    maxAdSpins: Int,
    canWatchAd: Boolean,
    spinFeedback: String?,
    onSpinClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Wheel Canvas with pointer
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            // Rotating Wheel
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotationAngle)
            ) {
                val radius = size.minDimension / 2f
                val center = Offset(size.width / 2f, size.height / 2f)
                val sweep = 360f / SPIN_SEGMENTS.size

                SPIN_SEGMENTS.forEachIndexed { i, pts ->
                    val startAngle = i * sweep
                    val color = SEGMENT_COLORS[i % SEGMENT_COLORS.size]
                    drawArc(
                        color = color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(center.x - radius, center.y - radius)
                    )
                    drawArc(
                        color = Color.White.copy(alpha = 0.3f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        style = Stroke(width = 1.5f),
                        size = Size(radius * 2, radius * 2),
                        topLeft = Offset(center.x - radius, center.y - radius)
                    )

                    // Draw text inside segment using native canvas
                    val midAngleRad = Math.toRadians((startAngle + sweep / 2.0))
                    val textRadius = radius * 0.65f
                    val textX = (center.x + textRadius * Math.cos(midAngleRad)).toFloat()
                    val textY = (center.y + textRadius * Math.sin(midAngleRad)).toFloat()

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            this.color = android.graphics.Color.WHITE
                            this.textSize = 32f
                            this.typeface = android.graphics.Typeface.DEFAULT_BOLD
                            this.textAlign = android.graphics.Paint.Align.CENTER
                            this.isAntiAlias = true
                            setShadowLayer(4f, 0f, 2f, android.graphics.Color.BLACK)
                        }
                        save()
                        rotate((startAngle + sweep / 2f + 90f), textX, textY)
                        drawText("$pts", textX, textY + 10f, paint)
                        restore()
                    }
                }

                // Outer border ring
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = radius,
                    style = Stroke(width = 6f)
                )

                // Center hub
                drawCircle(
                    color = Color(0xFF1E2024),
                    radius = radius * 0.22f
                )
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = radius * 0.22f,
                    style = Stroke(width = 3f)
                )
            }

            // Top Pointer arrow (fixed at top pointing down)
            Canvas(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.TopCenter)
            ) {
                val path = Path().apply {
                    moveTo(size.width / 2f, size.height)
                    lineTo(0f, 0f)
                    lineTo(size.width, 0f)
                    close()
                }
                drawPath(path, color = Color(0xFFFFD700))
                drawPath(path, color = Color.White, style = Stroke(width = 2f))
            }

            // Center Pin icon
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Spin Status info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DarkCard,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Free Spins Left: $freeSpinsLeft/3",
                        color = if (freeSpinsLeft > 0) EmeraldGreen else TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Next reset: Daily (Server Time)",
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }

                if (extraSpinsAvailable > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DiamondCyan.copy(alpha = 0.2f))
                            .border(1.dp, DiamondCyan, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+$extraSpinsAvailable Extra Ready",
                            color = DiamondCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Extra Today: $adSpinsUsedToday/$maxAdSpins",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        if (spinFeedback != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = spinFeedback,
                color = GoldPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // State A/B: Free or Extra Spins Available
        if (freeSpinsLeft > 0 || extraSpinsAvailable > 0) {
            Button(
                onClick = onSpinClick,
                enabled = !isSpinning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = DarkBackground,
                    disabledContainerColor = DarkSurface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("spin_wheel_button")
            ) {
                if (isSpinning) {
                    CircularProgressIndicator(
                        color = DarkBackground,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Spinning...", fontWeight = FontWeight.Black, fontSize = 15.sp)
                } else {
                    Text(
                        text = if (freeSpinsLeft > 0) "SPIN 🎡" else "SPIN (Extra Spin) 🎡",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    )
                }
            }
        } else if (canWatchAd) {
            // State C: Free spins finished, Watch Ad & Get More Spin
            Button(
                onClick = {
                    if (activity != null) {
                        viewModel.watchAdForSpin(activity) {}
                    }
                },
                enabled = !isSpinning,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DiamondCyan,
                    contentColor = DarkBackground
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("watch_ad_for_spin_button")
            ) {
                Icon(Icons.Default.SmartDisplay, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "WATCH AD +1 SPIN 🎬",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
            }
            Text(
                text = "Extra Spins Today: $adSpinsUsedToday/$maxAdSpins",
                color = TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        } else {
            // State D: Extra daily limit reached
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Daily extra spins finished",
                        color = CoralAccent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Come back tomorrow for more spins.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SpinHistoryView(spinHistory: List<SpinTransaction>) {
    if (spinHistory.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No spins recorded yet. Spin the wheel to start earning!", color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            items(spinHistory) { tx ->
                val dateStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(tx.createdAt))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🎡", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (tx.spinType == "FREE_SPIN") "Free Spin & Earn" else "Rewarded Ad Spin",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$dateStr • ${tx.status}",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Text(
                            text = "+${tx.rewardPoints} pts",
                            color = EmeraldGreen,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
