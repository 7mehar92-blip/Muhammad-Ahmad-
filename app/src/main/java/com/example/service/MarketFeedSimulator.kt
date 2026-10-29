package com.example.service

import com.example.model.Candlestick
import com.example.model.MarketType
import com.example.model.TradingAsset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

class MarketFeedSimulator {

    private val scope = CoroutineScope(Dispatchers.Default + Job())

    val defaultAssets = listOf(
        TradingAsset("eur_usd_otc", "EUR/USD (OTC)", "Euro / US Dollar OTC", MarketType.BINARY, 1.08450, +0.34, 92, 5),
        TradingAsset("gbp_usd_otc", "GBP/USD (OTC)", "British Pound / US Dollar OTC", MarketType.BINARY, 1.29820, -0.15, 93, 5),
        TradingAsset("usd_jpy_otc", "USD/JPY (OTC)", "US Dollar / Japanese Yen OTC", MarketType.BINARY, 153.420, +0.48, 91, 3),
        TradingAsset("xau_usd_otc", "Gold (OTC)", "Gold / US Dollar Binary", MarketType.BINARY, 2748.50, +1.12, 94, 2),
        TradingAsset("btc_usdt_otc", "BTC/USDT (OTC)", "Bitcoin / Tether OTC", MarketType.BINARY, 71250.00, +2.85, 90, 2),
        // Forex & Crypto
        TradingAsset("eur_usd_fx", "EUR/USD", "Euro Forex Major", MarketType.FOREX, 1.08420, +0.22, 85, 5),
        TradingAsset("xau_usd_fx", "XAU/USD", "Spot Gold Bullion", MarketType.FOREX, 2749.10, +1.15, 88, 2),
        TradingAsset("btc_usdt_crypto", "BTC/USDT", "Bitcoin Perpetual", MarketType.FOREX, 71280.00, +2.90, 89, 2),
        TradingAsset("us100_index", "NASDAQ (US100)", "Tech 100 Index", MarketType.FOREX, 20450.00, +0.75, 87, 2)
    )

    private val _selectedAsset = MutableStateFlow(defaultAssets.first())
    val selectedAsset: StateFlow<TradingAsset> = _selectedAsset.asStateFlow()

    private val _candles = MutableStateFlow<List<Candlestick>>(emptyList())
    val candles: StateFlow<List<Candlestick>> = _candles.asStateFlow()

    private val _currentLivePrice = MutableStateFlow(defaultAssets.first().currentPrice)
    val currentLivePrice: StateFlow<Double> = _currentLivePrice.asStateFlow()

    init {
        initHistory(defaultAssets.first())
        startFeedLoop()
    }

    fun selectAsset(asset: TradingAsset) {
        _selectedAsset.value = asset
        _currentLivePrice.value = asset.currentPrice
        initHistory(asset)
    }

    private fun initHistory(asset: TradingAsset) {
        val list = mutableListOf<Candlestick>()
        var price = asset.currentPrice
        val now = System.currentTimeMillis()
        val stepMs = 5000L

        for (i in 40 downTo 1) {
            val delta = (Random.nextDouble() - 0.49) * (price * 0.0008)
            val open = price
            val close = price + delta
            val high = maxOf(open, close) + Random.nextDouble() * (price * 0.0004)
            val low = minOf(open, close) - Random.nextDouble() * (price * 0.0004)
            price = close
            list.add(
                Candlestick(
                    timestamp = now - (i * stepMs),
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = Random.nextDouble(10.0, 150.0),
                    isFvgBullish = i % 7 == 0,
                    isFvgBearish = i % 11 == 0
                )
            )
        }
        _candles.value = list
        _currentLivePrice.value = list.last().close
    }

    private fun startFeedLoop() {
        scope.launch {
            var tickCount = 0
            while (isActive) {
                delay(600)
                tickCount++
                val current = _candles.value.toMutableList()
                if (current.isEmpty()) continue

                val last = current.removeAt(current.size - 1)
                val pipSize = _selectedAsset.value.currentPrice * 0.00015
                val delta = (Random.nextDouble() - 0.485) * pipSize
                val newPrice = last.close + delta

                _currentLivePrice.value = newPrice

                if (tickCount % 8 == 0) {
                    // Close current candle and start new one
                    val completed = last.copy(
                        high = maxOf(last.high, newPrice),
                        low = minOf(last.low, newPrice),
                        close = newPrice
                    )
                    current.add(completed)
                    // Keep max 45 candles in rolling buffer
                    if (current.size > 45) {
                        current.removeAt(0)
                    }
                    val newCandle = Candlestick(
                        timestamp = System.currentTimeMillis(),
                        open = newPrice,
                        high = newPrice,
                        low = newPrice,
                        close = newPrice,
                        volume = 15.0,
                        isFvgBullish = Random.nextInt(10) == 0,
                        isFvgBearish = Random.nextInt(12) == 0
                    )
                    current.add(newCandle)
                } else {
                    // Update active candle in-place
                    val updated = last.copy(
                        high = maxOf(last.high, newPrice),
                        low = minOf(last.low, newPrice),
                        close = newPrice,
                        volume = last.volume + Random.nextDouble(2.0, 8.0)
                    )
                    current.add(updated)
                }
                _candles.value = current
            }
        }
    }
}
