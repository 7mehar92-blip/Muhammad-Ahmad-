package com.example

import com.example.model.Candlestick
import com.example.model.CustomStrategyRule
import com.example.model.MarketType
import com.example.model.SignalType
import com.example.model.StrategyMode
import com.example.model.Timeframe
import com.example.model.TradingAsset
import com.example.service.StrategyEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    private val testAsset = TradingAsset(
        id = "eur_usd",
        symbol = "EUR/USD (OTC)",
        name = "Euro OTC",
        marketType = MarketType.BINARY,
        currentPrice = 1.0850,
        changePercent = 0.5
    )

    private val sampleCandles = listOf(
        Candlestick(1000L, 1.0800, 1.0820, 1.0790, 1.0810),
        Candlestick(2000L, 1.0810, 1.0830, 1.0805, 1.0825),
        Candlestick(3000L, 1.0825, 1.0840, 1.0820, 1.0835),
        Candlestick(4000L, 1.0835, 1.0855, 1.0830, 1.0850),
        Candlestick(5000L, 1.0850, 1.0870, 1.0845, 1.0865, isFvgBullish = true)
    )

    @Test
    fun testStrategyEngine_generatesSmcSignal() {
        val engine = StrategyEngine()
        val signal = engine.analyzeMarket(
            asset = testAsset,
            candles = sampleCandles,
            mode = StrategyMode.BUILT_IN_SMC,
            customRule = CustomStrategyRule(),
            timeframe = Timeframe.SEC_5
        )

        assertNotNull(signal)
        assertTrue(signal.strength in 80..100)
        assertEquals(Timeframe.SEC_5, signal.timeframe)
        assertTrue(signal.indicators.isNotEmpty())
    }

    @Test
    fun testCustomRuleParser_evaluatesRsiAndFvg() {
        val engine = StrategyEngine()
        val customRule = "BUY when FVG == BULLISH and RSI < 35; SELL when FVG == BEARISH and RSI > 65"

        val buySignal = engine.evaluateCustomRule(
            asset = testAsset,
            timeframe = Timeframe.SEC_15,
            ruleText = customRule,
            rsi = 32.0,
            hasBullishFvg = true,
            hasBearishFvg = false,
            trend = "BULLISH"
        )
        assertEquals(SignalType.BUY, buySignal.type)

        val sellSignal = engine.evaluateCustomRule(
            asset = testAsset,
            timeframe = Timeframe.SEC_15,
            ruleText = customRule,
            rsi = 68.0,
            hasBullishFvg = false,
            hasBearishFvg = true,
            trend = "BEARISH"
        )
        assertEquals(SignalType.SELL, sellSignal.type)
    }

    @Test
    fun testCompoundingCalculation() {
        var stake = 50.0
        val payoutRate = 1.92
        for (i in 2..3) {
            stake = kotlin.math.round(stake * payoutRate)
        }
        assertTrue(stake > 150.0)
    }
}
