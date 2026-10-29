package com.example.model

enum class MarketType(val displayName: String) {
    BINARY("Binary Options (Quotex / Pocket)"),
    FOREX("Forex & Crypto (MT5 / TradingView)")
}

data class TradingAsset(
    val id: String,
    val symbol: String,
    val name: String,
    val marketType: MarketType,
    val currentPrice: Double,
    val changePercent: Double,
    val payoutPercent: Int = 92, // e.g., 92% on Quotex OTC
    val digits: Int = 5
)

enum class Timeframe(val label: String, val seconds: Int) {
    SEC_5("5s (Turbo)", 5),
    SEC_15("15s", 15),
    SEC_30("30s", 30),
    MIN_1("1 Min", 60),
    MIN_2("2 Min", 120),
    MIN_5("5 Min", 300)
}

enum class SignalType {
    BUY,
    SELL,
    WAIT
}

data class Candlestick(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 1.0,
    val isFvgBullish: Boolean = false,
    val isFvgBearish: Boolean = false
)

data class TradeSignal(
    val id: String,
    val asset: TradingAsset,
    val type: SignalType,
    val timeframe: Timeframe,
    val strength: Int, // e.g. 96%
    val strategyName: String,
    val rationaleUrdu: String,
    val rationaleEn: String,
    val indicators: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

enum class StrategyMode(val title: String, val description: String, val winRate: Int) {
    BUILT_IN_SMC("SMC (Smart Money Concepts)", "Order Blocks, Liquidity Sweeps, BOS & CHoCH", 94),
    CANDLE_RANGE_THEORY("CRT (Candle Range Theory)", "Opening range expansion, high-timeframe wick fills", 91),
    TURTLE_SOUP("Turtle Soup & Liquidity Hunt", "20-bar extreme false breakouts and sharp reversals", 89),
    HEATMAP_VOLUME("Orderbook Heatmap & Flow", "Cluster liquidity & order imbalance confluences", 93),
    CUSTOM_USER_RULE("Custom Strategy (PineScript / Rules)", "User-defined logic parser & indicator expressions", 90),
    AI_AUTO_SELECT("AI Auto-Optimizer (Adaptive)", "Dynamically picks highest win-rate algorithm for current OTC feed", 96)
}

data class CustomStrategyRule(
    val ruleText: String = "BUY when FVG == BULLISH and RSI < 35; SELL when FVG == BEARISH and RSI > 65",
    val isEnabled: Boolean = true,
    val isValid: Boolean = true,
    val description: String = "SMC Fair Value Gap + RSI Oversold/Overbought confirmation"
)

data class CompoundingConfig(
    val totalSteps: Int = 3,
    val currentStep: Int = 1,
    val initialStake: Double = 10.0,
    val currentStake: Double = 10.0,
    val payoutPercent: Int = 92,
    val isAutoTradingActive: Boolean = true,
    val consecutiveWins: Int = 0,
    val maxLossAllowed: Double = 200.0,
    val targetProfit: Double = 5000.0,
    val sessionProfit: Double = 5479.20
)

data class TradeExecution(
    val id: String,
    val assetSymbol: String,
    val direction: SignalType,
    val stakeAmount: Double,
    val payoutAmount: Double,
    val entryPrice: Double,
    val exitPrice: Double,
    val durationSeconds: Int,
    val result: TradeResult,
    val step: Int,
    val timestamp: Long = System.currentTimeMillis()
)

enum class TradeResult {
    WIN,
    LOSS,
    PENDING
}

data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val profitUsd: Double,
    val winRate: Int,
    val tradesCount: Int,
    val isUser: Boolean = false,
    val countryFlag: String = "🇵🇰"
)

data class WithdrawalRequest(
    val id: String,
    val amountUsd: Double,
    val address: String,
    val network: String = "TRC20 (USDT)",
    val timestamp: String,
    val status: String = "COMPLETED",
    val txHash: String
)

data class WebhookMessage(
    val id: String,
    val source: String,
    val rawPayload: String,
    val parsedSignal: SignalType,
    val timestamp: Long = System.currentTimeMillis()
)
