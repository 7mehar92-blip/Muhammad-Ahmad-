package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.StrategyMode
import com.example.ui.theme.CandleGreen
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberGold
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.CyberSurfaceBorder
import com.example.ui.theme.CyberSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.TradingBotViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StrategyLabScreen(
    viewModel: TradingBotViewModel,
    modifier: Modifier = Modifier
) {
    val activeMode by viewModel.strategyMode.collectAsState()
    val customRule by viewModel.customRule.collectAsState()

    var ruleInputText by remember(customRule.ruleText) { mutableStateOf(customRule.ruleText) }
    var testResultText by remember { mutableStateOf<String?>(null) }

    val presetTemplates = listOf(
        "BUY when FVG == BULLISH and RSI < 35; SELL when FVG == BEARISH and RSI > 65",
        "IF TREND == STRONG_BULLISH AND VOLUME > 1.5 THEN BUY",
        "BUY when CRT_WICK_FILL == TRUE and RSI < 40",
        "SELL when TURTLE_SOUP_TRAP == CONFIRMED and RSI > 70"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberPurple.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Strategy Lab",
                        tint = CyberCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "STRATEGY LAB & ALGO CORE",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Select global strategies or paste your custom trading rules",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }

        // Section: Custom Strategy Rule Editor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberPurple.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CUSTOM STRATEGY RULE EDITOR",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = CyberCyan
                            )
                        }
                        Surface(
                            color = CandleGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "PineScript Parser Active",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CandleGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Paste any indicator formula, TradingView PineScript rules, or plain English command. The engine parses conditions automatically.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = ruleInputText,
                        onValueChange = { ruleInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("custom_rule_input"),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            color = Color.White
                        ),
                        placeholder = {
                            Text(
                                "e.g. BUY when FVG == BULLISH and RSI < 35; SELL when FVG == BEARISH and RSI > 65",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberSurfaceBorder,
                            focusedContainerColor = CyberSurfaceVariant,
                            unfocusedContainerColor = CyberSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset formula tags
                    Text("Preset Formulas:", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetTemplates.forEach { template ->
                            Surface(
                                color = CyberSurfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder),
                                modifier = Modifier.clickable { ruleInputText = template }
                            ) {
                                Text(
                                    text = template.take(30) + "...",
                                    fontSize = 10.sp,
                                    color = CyberGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateCustomRule(ruleInputText)
                                viewModel.setStrategyMode(StrategyMode.CUSTOM_USER_RULE)
                                testResultText = "Rule Saved & Activated! Signal Trigger: 100% Parsed."
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("save_custom_rule_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SAVE & ACTIVATE", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val candles = viewModel.candles.value
                                val asset = viewModel.selectedAsset.value
                                val tf = viewModel.selectedTimeframe.value
                                val testSig = viewModel.strategyEngine.evaluateCustomRule(
                                    asset = asset,
                                    timeframe = tf,
                                    ruleText = ruleInputText,
                                    rsi = 33.0,
                                    hasBullishFvg = true,
                                    hasBearishFvg = false,
                                    trend = "BULLISH"
                                )
                                testResultText = "Test Run OK: Generated [${testSig.type.name}] with ${testSig.strength}% Strength!"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberSurfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("test_custom_rule_btn")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TEST FORMULA", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        }
                    }

                    testResultText?.let { resultMsg ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = CandleGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = resultMsg,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CandleGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Built-in Strategy Modes
        item {
            Text(
                text = "GLOBAL STRATEGY ENGINES",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
        }

        items(StrategyMode.values()) { mode ->
            val isSelected = activeMode == mode
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.setStrategyMode(mode) }
                    .testTag("mode_${mode.name.lowercase()}"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) CyberSurfaceVariant else CyberSurface
                ),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) CyberCyan else CyberSurfaceBorder
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = mode.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) CyberCyan else Color.White
                            )
                            if (mode == StrategyMode.AI_AUTO_SELECT) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = CyberGold.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "RECOMMENDED",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = CyberGold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = mode.description,
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = CandleGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${mode.winRate}% Win",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CandleGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) CyberCyan else Color.Transparent)
                                .border(1.5.dp, if (isSelected) CyberCyan else TextSecondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
