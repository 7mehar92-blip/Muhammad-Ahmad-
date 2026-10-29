package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.model.MarketType
import com.example.model.SignalType
import com.example.model.Timeframe
import com.example.model.TradingAsset
import com.example.service.FloatingOverlayService
import com.example.ui.components.CandlestickChart
import com.example.ui.components.FloatingControlBall
import com.example.ui.components.SignalRadarDialog
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

@Composable
fun LiveBrokerScreen(
    viewModel: TradingBotViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val selectedAsset by viewModel.selectedAsset.collectAsState()
    val candles by viewModel.candles.collectAsState()
    val livePrice by viewModel.currentPrice.collectAsState()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsState()
    val compounding by viewModel.compounding.collectAsState()
    val balance by viewModel.accountBalance.collectAsState()

    val isControlModalOpen by viewModel.isControlModalOpen.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val currentSignal by viewModel.currentSignal.collectAsState()
    val activeTrade by viewModel.activeTrade.collectAsState()
    val countdownSeconds by viewModel.tradeCountdownSeconds.collectAsState()
    val completedTrade by viewModel.completedTradeDialog.collectAsState()
    val simulatedClickFiring by viewModel.isSimulatedClickFiring.collectAsState()

    val currentSpokenText by viewModel.voiceAgent.currentPhrase.collectAsState()
    val isVoiceEnabled by viewModel.voiceAgent.isVoiceEnabled.collectAsState()

    var showAssetMenu by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF070A11))) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top Broker Bar (Quotex / Pocket Option Style Header)
            BrokerHeaderBar(
                selectedAsset = selectedAsset,
                balance = balance,
                payout = selectedAsset.payoutPercent,
                isVoiceEnabled = isVoiceEnabled,
                onAssetClick = { showAssetMenu = true },
                onToggleVoice = { viewModel.voiceAgent.toggleVoice() },
                onLaunchExternalOverlay = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (Settings.canDrawOverlays(context)) {
                            val svcIntent = Intent(context, FloatingOverlayService::class.java)
                            ContextCompat.startForegroundService(context, svcIntent)
                        } else {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:${context.packageName}")
                            )
                            context.startActivity(intent)
                        }
                    }
                }
            )

            // Asset Dropdown Selector
            DropdownMenu(
                expanded = showAssetMenu,
                onDismissRequest = { showAssetMenu = false },
                modifier = Modifier.background(CyberSurface)
            ) {
                viewModel.marketFeed.defaultAssets.forEach { asset ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(asset.symbol, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("${asset.payoutPercent}%", color = CandleGreen, fontWeight = FontWeight.Bold)
                            }
                        },
                        onClick = {
                            viewModel.selectAsset(asset)
                            showAssetMenu = false
                        }
                    )
                }
            }

            // Urdu Voice Realtime HUD banner (as heard in video)
            UrduVoiceBanner(spokenText = currentSpokenText)

            // Candlestick Chart Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                CandlestickChart(
                    candles = candles,
                    currentPrice = livePrice,
                    digits = selectedAsset.digits,
                    modifier = Modifier.fillMaxSize()
                )

                // Current Price overlay badge
                Surface(
                    color = CyberSurface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text("LIVE OTC", fontSize = 10.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                        Text(
                            text = String.format("%.${selectedAsset.digits}f", livePrice),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }

                // Strategy tag badge
                Surface(
                    color = CyberPurple.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberPurple.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = "SMC v4 • FVG Sweeper",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CyberPurple,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Bottom Trading Execution Console (Quotex style BUY / SELL Buttons & Stake)
            BrokerBottomTradingPanel(
                timeframe = selectedTimeframe,
                stake = compounding.currentStake,
                step = compounding.currentStep,
                payout = selectedAsset.payoutPercent,
                isClickFiring = simulatedClickFiring,
                onBuyClick = {
                    val sig = com.example.model.TradeSignal(
                        id = "MANUAL_BUY",
                        asset = selectedAsset,
                        type = SignalType.BUY,
                        timeframe = selectedTimeframe,
                        strength = 95,
                        strategyName = "Manual Algo Flow",
                        rationaleUrdu = "BUY trade execute ho rahi hai.",
                        rationaleEn = "BUY trade executed.",
                        indicators = listOf("Manual", "92% Payout")
                    )
                    viewModel.executeTradeWithSignal(sig)
                },
                onSellClick = {
                    val sig = com.example.model.TradeSignal(
                        id = "MANUAL_SELL",
                        asset = selectedAsset,
                        type = SignalType.SELL,
                        timeframe = selectedTimeframe,
                        strength = 95,
                        strategyName = "Manual Algo Flow",
                        rationaleUrdu = "SELL trade execute ho rahi hai.",
                        rationaleEn = "SELL trade executed.",
                        indicators = listOf("Manual", "92% Payout")
                    )
                    viewModel.executeTradeWithSignal(sig)
                },
                onOpenControl = { viewModel.openControlModal() }
            )
        }

        // Draggable Floating Circular Control Ball (Widget)
        FloatingControlBall(
            isScanning = isAnalyzing,
            isAutoActive = compounding.isAutoTradingActive,
            onTap = { viewModel.openControlModal() },
            onLongPress = { viewModel.toggleAutoTrading() },
            modifier = Modifier.align(Alignment.Center)
        )

        // Modal HUD dialog (Timeframe, Compounding, Radar Scan, Win Popup)
        SignalRadarDialog(
            isOpen = isControlModalOpen,
            isAnalyzing = isAnalyzing,
            activeSignal = currentSignal,
            activeTrade = activeTrade,
            countdownSeconds = countdownSeconds,
            completedTrade = completedTrade,
            selectedTimeframe = selectedTimeframe,
            compoundingConfig = compounding,
            currentSpokenText = currentSpokenText,
            onSelectTimeframe = { viewModel.setTimeframe(it) },
            onSelectCompoundingSteps = { viewModel.setCompoundingSteps(it) },
            onStartAiScan = { viewModel.requestAiScan() },
            onDismissResult = { viewModel.dismissTradeResultDialog() },
            onClose = { viewModel.closeControlModal() }
        )
    }
}

@Composable
fun BrokerHeaderBar(
    selectedAsset: TradingAsset,
    balance: Double,
    payout: Int,
    isVoiceEnabled: Boolean,
    onAssetClick: () -> Unit,
    onToggleVoice: () -> Unit,
    onLaunchExternalOverlay: () -> Unit
) {
    Surface(
        color = CyberSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Asset Selector chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberSurfaceVariant)
                    .clickable { onAssetClick() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("asset_selector_btn")
            ) {
                Text(
                    text = selectedAsset.symbol,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Surface(
                    color = CandleGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "$payout%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = CandleGreen,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Realtime Balance ($6,479.00 as in video)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LIVE ACCOUNT",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = "$${String.format("%.2f", balance)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            // System Overlay & Voice Toggles
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleVoice,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isVoiceEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                        contentDescription = "Voice Toggle",
                        tint = if (isVoiceEnabled) CyberCyan else TextSecondary
                    )
                }
                IconButton(
                    onClick = onLaunchExternalOverlay,
                    modifier = Modifier.size(36.dp),
                    content = {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "System Floating Ball",
                            tint = CyberPurple
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun UrduVoiceBanner(spokenText: String) {
    Surface(
        color = Color(0xFF0F172A),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(CyberCyan)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "URDU AGENT: $spokenText",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = CyberCyan,
                maxLines = 1
            )
        }
    }
}

@Composable
fun BrokerBottomTradingPanel(
    timeframe: Timeframe,
    stake: Double,
    step: Int,
    payout: Int,
    isClickFiring: SignalType?,
    onBuyClick: () -> Unit,
    onSellClick: () -> Unit,
    onOpenControl: () -> Unit
) {
    Surface(
        color = CyberSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Time and Investment row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Timeframe Card
                Surface(
                    color = CyberSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenControl() }
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                        Text("TIMEFRAME", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                        Text(timeframe.label, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                    }
                }

                // Stake & Step Card
                Surface(
                    color = CyberSurfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .clickable { onOpenControl() }
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("INVESTMENT", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text("$${stake.toInt()}.00", fontSize = 14.sp, fontWeight = FontWeight.Black, color = CyberGold)
                        }
                        Surface(
                            color = CyberPurple.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Step $step",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberPurple,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Giant BUY / SELL Auto-Execution buttons with click target crosshairs!
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // BUY Button (Green)
                Button(
                    onClick = onBuyClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CandleGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .scale(if (isClickFiring == SignalType.BUY) 0.94f else 1f)
                        .testTag("quotex_buy_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("BUY / CALL", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.Black)
                            Text("+$payout% Profit", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF003816))
                        }
                    }
                }

                // SELL Button (Red)
                Button(
                    onClick = onSellClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CandleRed),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .scale(if (isClickFiring == SignalType.SELL) 0.94f else 1f)
                        .testTag("quotex_sell_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text("SELL / PUT", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White)
                            Text("+$payout% Profit", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD2D9))
                        }
                    }
                }
            }
        }
    }
}
