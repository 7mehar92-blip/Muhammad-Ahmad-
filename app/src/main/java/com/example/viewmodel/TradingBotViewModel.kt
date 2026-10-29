package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.Candlestick
import com.example.model.CompoundingConfig
import com.example.model.CustomStrategyRule
import com.example.model.LeaderboardEntry
import com.example.model.MarketType
import com.example.model.SignalType
import com.example.model.StrategyMode
import com.example.model.Timeframe
import com.example.model.TradeExecution
import com.example.model.TradeResult
import com.example.model.TradeSignal
import com.example.model.TradingAsset
import com.example.model.WebhookMessage
import com.example.model.WithdrawalRequest
import com.example.service.MarketFeedSimulator
import com.example.service.StrategyEngine
import com.example.service.VoiceAgentManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.random.Random

class TradingBotViewModel(application: Application) : AndroidViewModel(application) {

    val marketFeed = MarketFeedSimulator()
    val strategyEngine = StrategyEngine()
    val voiceAgent = VoiceAgentManager(application)

    // Current Market & Timeframe
    val selectedAsset: StateFlow<TradingAsset> = marketFeed.selectedAsset
    val candles: StateFlow<List<Candlestick>> = marketFeed.candles
    val currentPrice: StateFlow<Double> = marketFeed.currentLivePrice

    private val _selectedTimeframe = MutableStateFlow(Timeframe.SEC_5)
    val selectedTimeframe: StateFlow<Timeframe> = _selectedTimeframe.asStateFlow()

    private val _strategyMode = MutableStateFlow(StrategyMode.AI_AUTO_SELECT)
    val strategyMode: StateFlow<StrategyMode> = _strategyMode.asStateFlow()

    private val _customRule = MutableStateFlow(CustomStrategyRule())
    val customRule: StateFlow<CustomStrategyRule> = _customRule.asStateFlow()

    // Compounding & Account
    private val _compounding = MutableStateFlow(
        CompoundingConfig(
            totalSteps = 3,
            currentStep = 1,
            initialStake = 50.0,
            currentStake = 50.0,
            payoutPercent = 92,
            isAutoTradingActive = true,
            sessionProfit = 5479.20
        )
    )
    val compounding: StateFlow<CompoundingConfig> = _compounding.asStateFlow()

    private val _accountBalance = MutableStateFlow(6479.00)
    val accountBalance: StateFlow<Double> = _accountBalance.asStateFlow()

    // Overlay & HUD state
    private val _isControlModalOpen = MutableStateFlow(false)
    val isControlModalOpen: StateFlow<Boolean> = _isControlModalOpen.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _currentSignal = MutableStateFlow<TradeSignal?>(null)
    val currentSignal: StateFlow<TradeSignal?> = _currentSignal.asStateFlow()

    private val _activeTrade = MutableStateFlow<TradeExecution?>(null)
    val activeTrade: StateFlow<TradeExecution?> = _activeTrade.asStateFlow()

    private val _tradeCountdownSeconds = MutableStateFlow(0)
    val tradeCountdownSeconds: StateFlow<Int> = _tradeCountdownSeconds.asStateFlow()

    private val _completedTradeDialog = MutableStateFlow<TradeExecution?>(null)
    val completedTradeDialog: StateFlow<TradeExecution?> = _completedTradeDialog.asStateFlow()

    // Clicker crosshair coordinates (normalized 0f..1f for preview canvas)
    private val _buyTargetNormalizedY = MutableStateFlow(0.85f)
    val buyTargetNormalizedY: StateFlow<Float> = _buyTargetNormalizedY.asStateFlow()

    private val _sellTargetNormalizedY = MutableStateFlow(0.92f)
    val sellTargetNormalizedY: StateFlow<Float> = _sellTargetNormalizedY.asStateFlow()

    private val _isSimulatedClickFiring = MutableStateFlow<SignalType?>(null)
    val isSimulatedClickFiring: StateFlow<SignalType?> = _isSimulatedClickFiring.asStateFlow()

    // Trade History & Session Logs
    private val _tradeHistory = MutableStateFlow<List<TradeExecution>>(emptyList())
    val tradeHistory: StateFlow<List<TradeExecution>> = _tradeHistory.asStateFlow()

    // Webhooks
    private val _webhooks = MutableStateFlow<List<WebhookMessage>>(emptyList())
    val webhooks: StateFlow<List<WebhookMessage>> = _webhooks.asStateFlow()

    // Leaderboard
    private val _leaderboard = MutableStateFlow(
        listOf(
            LeaderboardEntry(1, "Sktrader (VIP)", 54870.00, 97, 184, isUser = false, "🇵🇰"),
            LeaderboardEntry(2, "AlgoPro_Official", 42120.00, 95, 142, isUser = false, "🇦🇪"),
            LeaderboardEntry(3, "CryptoScalper_X", 38450.00, 93, 118, isUser = false, "🇹🇷"),
            LeaderboardEntry(4, "You (AI Compounding)", 5479.20, 96, 21, isUser = true, "🇵🇰"),
            LeaderboardEntry(5, "QuantMatrix", 4820.00, 91, 35, isUser = false, "🇬🇧"),
            LeaderboardEntry(6, "TurtleKing", 3910.00, 88, 29, isUser = false, "🇸🇦")
        )
    )
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()

    // Withdrawals
    private val _withdrawals = MutableStateFlow(
        listOf(
            WithdrawalRequest(
                id = "WTH-98421",
                amountUsd = 1420.00,
                address = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
                network = "TRC20 (USDT)",
                timestamp = "Today 02:45 PM",
                status = "COMPLETED",
                txHash = "6c4e0b51...a8f2"
            )
        )
    )
    val withdrawals: StateFlow<List<WithdrawalRequest>> = _withdrawals.asStateFlow()

    private var activeTradeJob: Job? = null

    init {
        // Voice activation announcement on app launch
        viewModelScope.launch {
            delay(800)
            voiceAgent.announceActivation()
        }
        initSampleWebhooks()
    }

    private fun initSampleWebhooks() {
        val sample = listOf(
            WebhookMessage(
                id = "WBK_01",
                source = "TradingView (PineScript Webhook)",
                rawPayload = """{"ticker": "EURUSD_OTC", "action": "BUY", "fvg": "BULLISH", "rsi": 31.4, "timeframe": 5}""",
                parsedSignal = SignalType.BUY,
                timestamp = System.currentTimeMillis() - 120000
            ),
            WebhookMessage(
                id = "WBK_02",
                source = "GoCharting SMC Orderflow",
                rawPayload = """{"event": "LIQUIDITY_SWEEP", "direction": "SELL", "delta": -420, "score": 94}""",
                parsedSignal = SignalType.SELL,
                timestamp = System.currentTimeMillis() - 360000
            )
        )
        _webhooks.value = sample
    }

    fun selectAsset(asset: TradingAsset) {
        marketFeed.selectAsset(asset)
        calculateStakeForCurrentStep()
    }

    fun setTimeframe(tf: Timeframe) {
        _selectedTimeframe.value = tf
        voiceAgent.speakUrdu(
            textUrdu = "Timeframe ${tf.label} select kar liya gaya hai.",
            phoneticFallback = "Timeframe ${tf.label} select kar liya gaya hai."
        )
    }

    fun setCompoundingSteps(steps: Int) {
        _compounding.value = _compounding.value.copy(totalSteps = steps)
        calculateStakeForCurrentStep()
        voiceAgent.speakUrdu(
            textUrdu = "$steps steps compounding tayyar hai.",
            phoneticFallback = "$steps steps compounding tayyar hai."
        )
    }

    fun setInitialStake(amount: Double) {
        _compounding.value = _compounding.value.copy(initialStake = amount)
        calculateStakeForCurrentStep()
    }

    fun setStrategyMode(mode: StrategyMode) {
        _strategyMode.value = mode
        voiceAgent.speakUrdu(
            textUrdu = "Strategy badal kar ${mode.title} set kar di gayi hai.",
            phoneticFallback = "Strategy badal kar ${mode.title} set kar di gayi hai."
        )
    }

    fun updateCustomRule(newRuleText: String) {
        _customRule.value = _customRule.value.copy(
            ruleText = newRuleText,
            isValid = true
        )
        voiceAgent.announceRuleSaved()
    }

    fun openControlModal() {
        _isControlModalOpen.value = true
        voiceAgent.speakUrdu(
            textUrdu = "Control panel open ho gaya hai.",
            phoneticFallback = "Control panel open ho gaya hai."
        )
    }

    fun closeControlModal() {
        _isControlModalOpen.value = false
    }

    fun toggleAutoTrading() {
        val current = _compounding.value.isAutoTradingActive
        _compounding.value = _compounding.value.copy(isAutoTradingActive = !current)
        val msg = if (!current) "Auto trading shuru kar di gayi hai." else "Auto trading pause ho gayi hai."
        voiceAgent.speakUrdu(msg, msg)
    }

    // Trigger AI Scan & Signal Generation (As seen in video)
    fun requestAiScan() {
        if (_isAnalyzing.value || _activeTrade.value != null) return

        viewModelScope.launch {
            _isAnalyzing.value = true
            voiceAgent.announceAnalyzing()

            // Realistic AI Scan time (approx 2.5 seconds with radar wave)
            delay(2400)

            val sig = strategyEngine.analyzeMarket(
                asset = selectedAsset.value,
                candles = candles.value,
                mode = _strategyMode.value,
                customRule = _customRule.value,
                timeframe = _selectedTimeframe.value
            )

            _currentSignal.value = sig
            _isAnalyzing.value = false

            // Voice announcement of signal
            voiceAgent.announceSignal(sig.type.name)

            // Auto execute if enabled
            if (_compounding.value.isAutoTradingActive) {
                delay(1200)
                executeTradeWithSignal(sig)
            }
        }
    }

    fun executeTradeWithSignal(signal: TradeSignal) {
        if (_activeTrade.value != null) return

        viewModelScope.launch {
            // Trigger visual auto-clicker pulse on target
            _isSimulatedClickFiring.value = signal.type
            delay(300)
            _isSimulatedClickFiring.value = null

            val stake = _compounding.value.currentStake
            val duration = signal.timeframe.seconds
            val entry = currentPrice.value

            val trade = TradeExecution(
                id = "TRD_${System.currentTimeMillis()}",
                assetSymbol = signal.asset.symbol,
                direction = signal.type,
                stakeAmount = stake,
                payoutAmount = stake + (stake * (signal.asset.payoutPercent / 100.0)),
                entryPrice = entry,
                exitPrice = entry,
                durationSeconds = duration,
                result = TradeResult.PENDING,
                step = _compounding.value.currentStep
            )

            _activeTrade.value = trade
            _tradeCountdownSeconds.value = duration

            // Start countdown timer
            activeTradeJob?.cancel()
            activeTradeJob = launch {
                for (s in duration downTo 1) {
                    _tradeCountdownSeconds.value = s
                    delay(1000)
                }
                finishActiveTrade(trade)
            }
        }
    }

    private fun finishActiveTrade(trade: TradeExecution) {
        val isWin = true // High-probability algorithmic signal outcome as in video demonstration
        val profitDelta = if (isWin) trade.payoutAmount - trade.stakeAmount else -trade.stakeAmount
        val exit = if (trade.direction == SignalType.BUY) trade.entryPrice + 0.00045 else trade.entryPrice - 0.00045

        val completed = trade.copy(
            exitPrice = exit,
            result = if (isWin) TradeResult.WIN else TradeResult.LOSS
        )

        _activeTrade.value = null
        _tradeCountdownSeconds.value = 0
        _completedTradeDialog.value = completed

        // Update trade history
        val updatedHistory = listOf(completed) + _tradeHistory.value
        _tradeHistory.value = updatedHistory

        // Update Balance and Compounding Progress
        val newProfit = _compounding.value.sessionProfit + profitDelta
        _accountBalance.value += profitDelta

        if (isWin) {
            val nextStep = _compounding.value.currentStep + 1
            if (nextStep > _compounding.value.totalSteps) {
                // Session complete!
                voiceAgent.announceSessionComplete()
                _compounding.value = _compounding.value.copy(
                    currentStep = 1,
                    sessionProfit = newProfit,
                    consecutiveWins = _compounding.value.consecutiveWins + 1
                )
            } else {
                voiceAgent.announceWin(trade.step)
                _compounding.value = _compounding.value.copy(
                    currentStep = nextStep,
                    sessionProfit = newProfit,
                    consecutiveWins = _compounding.value.consecutiveWins + 1
                )
            }
        } else {
            voiceAgent.announceLossReset()
            _compounding.value = _compounding.value.copy(
                currentStep = 1,
                sessionProfit = newProfit,
                consecutiveWins = 0
            )
        }

        calculateStakeForCurrentStep()
        updateLeaderboardUserProfit(newProfit)
    }

    fun dismissTradeResultDialog() {
        _completedTradeDialog.value = null
    }

    private fun calculateStakeForCurrentStep() {
        val cfg = _compounding.value
        val payoutMultiplier = 1.0 + (cfg.payoutPercent / 100.0)
        var stake = cfg.initialStake

        for (i in 2..cfg.currentStep) {
            stake = (stake * payoutMultiplier).roundToInt().toDouble()
        }
        _compounding.value = _compounding.value.copy(currentStake = stake)
    }

    private fun updateLeaderboardUserProfit(profit: Double) {
        val updated = _leaderboard.value.map { entry ->
            if (entry.isUser) {
                entry.copy(profitUsd = profit, tradesCount = entry.tradesCount + 1)
            } else {
                entry
            }
        }
        _leaderboard.value = updated
    }

    fun simulateIncomingWebhook(source: String, direction: SignalType) {
        val json = """{"source": "$source", "action": "${direction.name}", "symbol": "${selectedAsset.value.symbol}", "confidence": 96.5, "time": "${Date()}"}"""
        val msg = WebhookMessage(
            id = "WBK_${System.currentTimeMillis()}",
            source = source,
            rawPayload = json,
            parsedSignal = direction
        )
        _webhooks.value = listOf(msg) + _webhooks.value
        voiceAgent.announceWebhookReceived(source)

        // Generate signal from webhook
        val sig = TradeSignal(
            id = "SIG_WBK_${System.currentTimeMillis()}",
            asset = selectedAsset.value,
            type = direction,
            timeframe = _selectedTimeframe.value,
            strength = 96,
            strategyName = "$source Webhook Feed",
            rationaleUrdu = "$source se high probability $direction signal received hua.",
            rationaleEn = "External webhook received from $source. Order executed immediately.",
            indicators = listOf(source, "Webhook Signal", "Auto-Trigger")
        )
        _currentSignal.value = sig

        if (_compounding.value.isAutoTradingActive) {
            executeTradeWithSignal(sig)
        }
    }

    fun requestWithdrawal(amount: Double, address: String): Boolean {
        if (amount > _accountBalance.value || amount <= 0) return false

        _accountBalance.value -= amount
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val req = WithdrawalRequest(
            id = "WTH-${Random.nextInt(10000, 99999)}",
            amountUsd = amount,
            address = address,
            network = "TRC20 (USDT)",
            timestamp = sdf.format(Date()),
            status = "PENDING_VERIFICATION",
            txHash = "tx_0x" + Random.nextLong(1000000000, 9999999999L).toString(16)
        )
        _withdrawals.value = listOf(req) + _withdrawals.value
        voiceAgent.speakUrdu(
            textUrdu = "$amount Dollar ka withdrawal process ho chuka hai.",
            phoneticFallback = "$amount Dollar ka withdrawal process ho chuka hai."
        )
        return true
    }

    fun confirmWithdrawalOtp(id: String) {
        val updated = _withdrawals.value.map {
            if (it.id == id) it.copy(status = "COMPLETED") else it
        }
        _withdrawals.value = updated
        voiceAgent.speakUrdu(
            textUrdu = "Withdrawal kamyabi se confirm ho gaya hai.",
            phoneticFallback = "Withdrawal kamyabi se confirm ho gaya hai."
        )
    }

    override fun onCleared() {
        super.onCleared()
        voiceAgent.destroy()
        activeTradeJob?.cancel()
    }
}
