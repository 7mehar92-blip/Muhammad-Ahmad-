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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Output
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.LeaderboardEntry
import com.example.model.WithdrawalRequest
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

@Composable
fun LeaderboardWalletScreen(
    viewModel: TradingBotViewModel,
    modifier: Modifier = Modifier
) {
    val leaderboard by viewModel.leaderboard.collectAsState()
    val withdrawals by viewModel.withdrawals.collectAsState()
    val balance by viewModel.accountBalance.collectAsState()
    val compounding by viewModel.compounding.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Leaderboard, 1: Withdrawal
    var withdrawalAddress by remember { mutableStateOf("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t") }
    var withdrawalAmount by remember { mutableStateOf("3000") }
    var showOtpDialog by remember { mutableStateOf(false) }
    var enteredOtp by remember { mutableStateOf("") }
    var activeWithdrawalId by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tab Selector (Leaderboard vs Wallet Withdrawal)
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CyberSurface,
                contentColor = CyberCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CyberCyan
                    )
                },
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LEADERBOARD", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("USDT WITHDRAWAL", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // Leaderboard content (As seen in the video!)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("YOUR DAILY PROFIT", fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                            Text(
                                text = "+$${String.format("%.2f", compounding.sessionProfit)}",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = CandleGreen
                            )
                            Text("Rank #4 • Insha'Allah Top 3 soon", fontSize = 11.sp, color = CyberGold)
                        }
                        Surface(
                            color = CandleGreen.copy(alpha = 0.2f),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = CyberGold,
                                modifier = Modifier
                                    .size(44.dp)
                                    .padding(8.dp)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "GLOBAL TRADER LEADERBOARD",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }

            items(leaderboard) { entry ->
                LeaderboardRow(entry = entry)
            }
        } else {
            // Withdrawal Form & History (Matches the exact Quotex withdrawal UI shown in video!)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CyberSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AVAILABLE FOR WITHDRAWAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                        Text(
                            text = "$${String.format("%.2f", balance)}",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text("Payment Method", fontSize = 12.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
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
                                Text("Tether USDT (TRC20)", fontWeight = FontWeight.Bold, color = Color.White)
                                Surface(
                                    color = CandleGreen.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Instant Payout", color = CandleGreen, fontSize = 11.sp, modifier = Modifier.padding(4.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Address Input
                        OutlinedTextField(
                            value = withdrawalAddress,
                            onValueChange = { withdrawalAddress = it },
                            label = { Text("USDT TRC20 Wallet Address", color = TextSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdrawal_address_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberSurfaceBorder,
                                focusedContainerColor = CyberSurfaceVariant,
                                unfocusedContainerColor = CyberSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Amount Input
                        OutlinedTextField(
                            value = withdrawalAmount,
                            onValueChange = { withdrawalAmount = it },
                            label = { Text("Amount ($ USD)", color = TextSecondary) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("withdrawal_amount_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberSurfaceBorder,
                                focusedContainerColor = CyberSurfaceVariant,
                                unfocusedContainerColor = CyberSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val amt = withdrawalAmount.toDoubleOrNull() ?: 0.0
                                val success = viewModel.requestWithdrawal(amt, withdrawalAddress)
                                if (success) {
                                    activeWithdrawalId = viewModel.withdrawals.value.first().id
                                    showOtpDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("confirm_withdrawal_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Output, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CONFIRM WITHDRAWAL", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Withdrawal History (Status of latest requests)
            item {
                Text(
                    text = "STATUS OF YOUR LATEST REQUESTS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
            }

            items(withdrawals) { req ->
                WithdrawalHistoryCard(request = req)
            }
        }
    }

    // 2-Step Verification Modal (As seen in the video!)
    if (showOtpDialog) {
        Dialog(onDismissRequest = { showOtpDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = CyberSurface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, CyberPurple)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(CyberCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "2-Step Verification",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        text = "Please enter the verification code to approve USDT transfer of $$withdrawalAmount",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = enteredOtp,
                        onValueChange = { enteredOtp = it },
                        label = { Text("6-Digit Code (e.g. 582910)", color = TextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("otp_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberSurfaceBorder,
                            focusedContainerColor = CyberSurfaceVariant,
                            unfocusedContainerColor = CyberSurfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            viewModel.confirmWithdrawalOtp(activeWithdrawalId)
                            showOtpDialog = false
                            enteredOtp = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CandleGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_otp_btn")
                    ) {
                        Text("VERIFY & PAYOUT", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (entry.isUser) CyberSurfaceVariant else CyberSurface
        ),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (entry.isUser) CyberCyan else CyberSurfaceBorder
        )
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
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            when (entry.rank) {
                                1 -> CyberGold
                                2 -> Color(0xFFC0C0C0)
                                3 -> Color(0xFFCD7F32)
                                else -> CyberSurfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#${entry.rank}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (entry.rank <= 3) Color.Black else Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.username,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (entry.isUser) CyberCyan else Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = entry.countryFlag, fontSize = 12.sp)
                    }
                    Text(
                        text = "${entry.tradesCount} Trades • ${entry.winRate}% Win Rate",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Text(
                text = "$${String.format("%,.2f", entry.profitUsd)}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = CandleGreen
            )
        }
    }
}

@Composable
fun WithdrawalHistoryCard(request: WithdrawalRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CyberSurface),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberSurfaceBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${request.amountUsd.toInt()}.00 USD",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Surface(
                    color = if (request.status == "COMPLETED") CandleGreen.copy(alpha = 0.2f) else CyberGold.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = request.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (request.status == "COMPLETED") CandleGreen else CyberGold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Wallet: ${request.address}",
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1
            )
            Text(
                text = "${request.network} • ${request.timestamp} • TX: ${request.txHash}",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}
