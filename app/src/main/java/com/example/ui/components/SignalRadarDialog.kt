package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CompoundingConfig
import com.example.model.SignalType
import com.example.model.Timeframe
import com.example.model.TradeExecution
import com.example.model.TradeResult
import com.example.model.TradeSignal
import com.example.ui.theme.CandleGreen
import com.example.ui.theme.CandleRed
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignalRadarDialog(
    isOpen: Boolean,
    isAnalyzing: Boolean,
    activeSignal: TradeSignal?,
    activeTrade: TradeExecution?,
    countdownSeconds: Int,
    completedTrade: TradeExecution?,
    selectedTimeframe: Timeframe,
    compoundingConfig: CompoundingConfig,
    currentSpokenText: String,
    onSelectTimeframe: (Timeframe) -> Unit,
    onSelectCompoundingSteps: (Int) -> Unit,
    onStartAiScan: () -> Unit,
    onDismissResult: () -> Unit,
    onClose: () -> Unit
) {
    if (!isOpen && completedTrade == null) return

    Dialog(onDismissRequest = {
        if (!isAnalyzing && activeTrade == null) {
            if (completedTrade != null) onDismissResult() else onClose()
        }
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(1.5.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                .testTag("signal_radar_dialog"),
            colors = CardDefaults.cardColors(containerColor = CyberSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top close button & voice status indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Urdu Voice Agent",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "URDU AI VOICE AGENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan
                        )
                    }
                    IconButton(
                        onClick = {
                            if (completedTrade != null) onDismissResult() else onClose()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // SUBTITLE VOICE BANNER (Matches Video voice feedback)
                Surface(
                    color = CyberSurfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"$currentSpokenText\"",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content branch: Completed Trade Dialog -> Active Trade Countdown -> Analyzing Radar -> Setup & Scan Form
                when {
                    completedTrade != null -> {
                        TradeClosedResultContent(
                            trade = completedTrade,
                            onDismiss = onDismissResult
                        )
                    }
                    isAnalyzing -> {
                        AnalyzingRadarContent()
                    }
                    activeTrade != null -> {
                        ActiveTradeCountdownContent(
                            trade = activeTrade,
                            countdownSeconds = countdownSeconds
                        )
                    }
                    activeSignal != null -> {
                        SignalConfirmationContent(
                            signal = activeSignal,
                            onStartTrade = { onStartAiScan() }
                        )
                    }
                    else -> {
                        SetupAndTriggerContent(
                            selectedTimeframe = selectedTimeframe,
                            compounding = compoundingConfig,
                            onSelectTimeframe = onSelectTimeframe,
                            onSelectSteps = onSelectCompoundingSteps,
                            onStartScan = onStartAiScan
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnalyzingRadarContent() {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(
                            CyberCyan.copy(alpha = 0.45f),
                            CyberPurple.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CyberPurple)
                    .border(2.dp, CyberCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Radar,
                    contentDescription = "Radar scanning",
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "ANALYZING MARKET",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 1.5.sp
        )
        Text(
            text = "Reading AI Algorithmic Signal & FVG...",
            fontSize = 12.sp,
            color = CyberCyan,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        LinearProgressIndicator(
            color = CyberCyan,
            trackColor = CyberSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(6.dp)
                .clip(CircleShape)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignalConfirmationContent(
    signal: TradeSignal,
    onStartTrade: () -> Unit
) {
    val isBuy = signal.type == SignalType.BUY
    val signalColor = if (isBuy) CandleGreen else CandleRed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Big Direction Circle (Directly from the video!)
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(signalColor.copy(alpha = 0.2f))
                .border(3.dp, signalColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (isBuy) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = signal.type.name,
                    tint = signalColor,
                    modifier = Modifier.size(42.dp)
                )
                Text(
                    text = signal.type.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = signalColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "SIGNAL 96% CONFIRM",
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = CyberGold
        )

        Text(
            text = "${signal.asset.symbol} • ${signal.timeframe.label}",
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Indicator confluence chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            signal.indicators.forEach { indicator ->
                Surface(
                    color = CyberSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = indicator,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onStartTrade,
            colors = ButtonDefaults.buttonColors(containerColor = signalColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("execute_signal_btn")
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "EXECUTE ${signal.type.name} TRADE",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun ActiveTradeCountdownContent(
    trade: TradeExecution,
    countdownSeconds: Int
) {
    val isBuy = trade.direction == SignalType.BUY
    val dirColor = if (isBuy) CandleGreen else CandleRed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(CyberSurfaceVariant)
                .border(3.dp, CyberCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$countdownSeconds",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = CyberCyan
                )
                Text(
                    text = "SECONDS",
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "AUTO-EXECUTED: ${trade.direction.name}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Black,
            color = dirColor
        )

        Text(
            text = "Stake: $${trade.stakeAmount.toInt()} • Payout: $${trade.payoutAmount.toInt()}",
            fontSize = 13.sp,
            color = Color.White,
            modifier = Modifier.padding(top = 4.dp)
        )

        Text(
            text = "Step ${trade.step} Active",
            fontSize = 12.sp,
            color = CyberGold,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun TradeClosedResultContent(
    trade: TradeExecution,
    onDismiss: () -> Unit
) {
    val isWin = trade.result == TradeResult.WIN
    val outcomeColor = if (isWin) CandleGreen else CandleRed

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(outcomeColor.copy(alpha = 0.2f))
                .border(2.dp, outcomeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Trade Closed",
                tint = outcomeColor,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "TRADE CLOSED",
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Text(
            text = if (isWin) "Win" else "Loss",
            fontSize = 32.sp,
            fontWeight = FontWeight.Black,
            color = outcomeColor
        )

        Text(
            text = if (isWin) "+$${(trade.payoutAmount - trade.stakeAmount).toInt()}.00" else "-$${trade.stakeAmount.toInt()}.00",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        Text(
            text = "PROFIT RECEIVED",
            fontSize = 11.sp,
            color = CyberCyan,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onDismiss,
            colors = ButtonDefaults.buttonColors(containerColor = outcomeColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("dismiss_result_btn")
        ) {
            Text(
                text = "CONTINUE SESSION",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupAndTriggerContent(
    selectedTimeframe: Timeframe,
    compounding: CompoundingConfig,
    onSelectTimeframe: (Timeframe) -> Unit,
    onSelectSteps: (Int) -> Unit,
    onStartScan: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Timeframe",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Timeframe choose karein aur compounding steps set karein",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Timeframe chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Timeframe.values().forEach { tf ->
                val selected = tf == selectedTimeframe
                FilterChip(
                    selected = selected,
                    onClick = { onSelectTimeframe(tf) },
                    label = {
                        Text(
                            text = tf.label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberPurple,
                        selectedLabelColor = Color.White,
                        containerColor = CyberSurfaceVariant,
                        labelColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("tf_${tf.name.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Compounding Steps",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Kitne steps compounding karni hai? (Automatic stake recalculation)",
            fontSize = 11.sp,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Compounding steps selector (1, 2, 3, 5)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(1, 2, 3, 5).forEach { stepCount ->
                val isSelected = compounding.totalSteps == stepCount
                Button(
                    onClick = { onSelectSteps(stepCount) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) CyberPurple else CyberSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(42.dp)
                        .testTag("step_$stepCount")
                ) {
                    Text(
                        text = "$stepCount",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Active Stake Info
        Surface(
            color = CyberSurfaceVariant,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
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
                        text = "Current Stake (Step ${compounding.currentStep})",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "$${compounding.currentStake.toInt()}.00",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberGold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Session Profit",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "+$${compounding.sessionProfit.toInt()}.00",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = CandleGreen
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Start Auto Trading / Scan Button
        Button(
            onClick = onStartScan,
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("start_ai_scan_btn")
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SCAN & AUTO-EXECUTE (AI)",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}
