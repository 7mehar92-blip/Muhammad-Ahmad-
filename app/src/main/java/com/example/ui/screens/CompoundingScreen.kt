package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SignalType
import com.example.model.TradeResult
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
import com.example.viewmodel.TradingBotViewModel
import kotlin.math.roundToInt

@Composable
fun CompoundingScreen(
    viewModel: TradingBotViewModel,
    modifier: Modifier = Modifier
) {
    val compounding by viewModel.compounding.collectAsState()
    val history by viewModel.tradeHistory.collectAsState()

    var stakeInput by remember { mutableStateOf(compounding.initialStake.toInt().toString()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Calculate,
                        contentDescription = "Compounding",
                        tint = CyberGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "COMPOUNDING ENGINE & RISK",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Automatic stake scaling, session progression & loss shields",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Live Compounding Dashboard Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ACTIVE SESSION COMPOUNDING",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberCyan
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (compounding.isAutoTradingActive) "AUTO ON" else "PAUSED",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (compounding.isAutoTradingActive) CandleGreen else TextSecondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = compounding.isAutoTradingActive,
                                onCheckedChange = { viewModel.toggleAutoTrading() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CandleGreen,
                                    checkedTrackColor = CandleGreen.copy(alpha = 0.4f)
                                ),
                                modifier = Modifier.testTag("auto_trading_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step visualization indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (step in 1..compounding.totalSteps) {
                            val isCompleted = step < compounding.currentStep
                            val isCurrent = step == compounding.currentStep
                            Surface(
                                color = when {
                                    isCompleted -> CandleGreen.copy(alpha = 0.2f)
                                    isCurrent -> CyberPurple.copy(alpha = 0.4f)
                                    else -> CyberSurfaceVariant
                                },
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    when {
                                        isCompleted -> CandleGreen
                                        isCurrent -> CyberCyan
                                        else -> CyberSurfaceBorder
                                    }
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "STEP $step",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isCurrent) CyberCyan else TextSecondary
                                    )
                                    Text(
                                        text = if (isCompleted) "✓ WIN" else if (isCurrent) "ACTIVE" else "WAIT",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCompleted) CandleGreen else if (isCurrent) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Initial Stake Config
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = stakeInput,
                            onValueChange = {
                                stakeInput = it
                                it.toDoubleOrNull()?.let { amt -> viewModel.setInitialStake(amt) }
                            },
                            label = { Text("Base Stake ($)", color = TextSecondary, fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("stake_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberSurfaceBorder,
                                focusedContainerColor = CyberSurfaceVariant,
                                unfocusedContainerColor = CyberSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        listOf(10, 50, 100).forEach { quickVal ->
                            Button(
                                onClick = {
                                    stakeInput = quickVal.toString()
                                    viewModel.setInitialStake(quickVal.toDouble())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(52.dp)
                            ) {
                                Text("$$quickVal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section: Projected Compound Growth Table
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "PROJECTED STAKE CALCULATION MATRIX",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = CyberGold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val base = compounding.initialStake
                    val rate = 1.92
                    var runningStake = base

                    for (s in 1..compounding.totalSteps) {
                        val projectedPayout = runningStake * rate
                        val profit = projectedPayout - runningStake
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Step $s Stake: $${runningStake.roundToInt()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Returns $${projectedPayout.roundToInt()} (+$${profit.roundToInt()})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = CandleGreen
                            )
                        }
                        runningStake = projectedPayout
                    }
                }
            }
        }

        // Section: Trade Session Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SESSION EXECUTION LOGS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${history.size} Executed",
                    fontSize = 11.sp,
                    color = CyberCyan
                )
            }
        }

        if (history.isEmpty()) {
            item {
                Surface(
                    color = CyberSurface,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.HourglassTop, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No trades recorded yet in this session.", color = TextSecondary, fontSize = 12.sp)
                        Text("Start the AI scan from the floating ball to execute trades.", color = CyberCyan, fontSize = 11.sp)
                    }
                }
            }
        } else {
            items(history) { trade ->
                TradeHistoryCard(trade = trade)
            }
        }
    }
}

@Composable
fun TradeHistoryCard(trade: com.example.model.TradeExecution) {
    val isWin = trade.result == TradeResult.WIN
    val outcomeColor = if (isWin) CandleGreen else CandleRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(outcomeColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isWin) Icons.Default.CheckCircle else Icons.Default.RemoveCircle,
                        contentDescription = null,
                        tint = outcomeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${trade.assetSymbol} • ${trade.direction.name}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Step ${trade.step} • ${trade.durationSeconds}s Turbo",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isWin) "+$${(trade.payoutAmount - trade.stakeAmount).toInt()}.00" else "-$${trade.stakeAmount.toInt()}.00",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = outcomeColor
                )
                Text(
                    text = "Stake: $${trade.stakeAmount.toInt()}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
