package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.dp
import com.example.model.Candlestick
import com.example.ui.theme.CandleGreen
import com.example.ui.theme.CandleRed
import com.example.ui.theme.CyberCyan

@Composable
fun CandlestickChart(
    candles: List<Candlestick>,
    currentPrice: Double,
    digits: Int = 5,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color(0xFF090D17))
            .padding(vertical = 12.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (candles.isEmpty()) return@Canvas

            val width = size.width
            val height = size.height

            val minPrice = candles.minOf { it.low } * 0.9998
            val maxPrice = candles.maxOf { it.high } * 1.0002
            val priceRange = if (maxPrice - minPrice > 0) maxPrice - minPrice else 0.001

            fun getY(price: Double): Float {
                val ratio = ((maxPrice - price) / priceRange).toFloat()
                return (ratio * height).coerceIn(4f, height - 4f)
            }

            // Draw horizontal price grid lines
            val gridSteps = 5
            for (i in 0..gridSteps) {
                val gridY = (height / gridSteps) * i
                drawLine(
                    color = Color(0xFF1E293B).copy(alpha = 0.5f),
                    start = Offset(0f, gridY),
                    end = Offset(width, gridY),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            }

            val candleCount = candles.size
            val candleSpacing = width / candleCount.coerceAtLeast(1)
            val candleBodyWidth = (candleSpacing * 0.65f).coerceIn(4f, 24f)

            // Draw candlesticks
            candles.forEachIndexed { index, candle ->
                val centerX = index * candleSpacing + (candleSpacing / 2f)
                val isBullish = candle.close >= candle.open
                val candleColor = if (isBullish) CandleGreen else CandleRed

                val openY = getY(candle.open)
                val closeY = getY(candle.close)
                val highY = getY(candle.high)
                val lowY = getY(candle.low)

                // High-low wick
                drawLine(
                    color = candleColor,
                    start = Offset(centerX, highY),
                    end = Offset(centerX, lowY),
                    strokeWidth = 2f
                )

                // Candle body
                val bodyTop = minOf(openY, closeY)
                val bodyHeight = maxOf(Math.abs(closeY - openY), 3f)

                drawRect(
                    color = candleColor,
                    topLeft = Offset(centerX - (candleBodyWidth / 2f), bodyTop),
                    size = Size(candleBodyWidth, bodyHeight)
                )

                // Draw FVG (Fair Value Gap) highlight if present
                if (candle.isFvgBullish) {
                    drawRect(
                        color = CandleGreen.copy(alpha = 0.15f),
                        topLeft = Offset(centerX - (candleBodyWidth / 2f), bodyTop - 8f),
                        size = Size(candleBodyWidth * 2f, 16f)
                    )
                } else if (candle.isFvgBearish) {
                    drawRect(
                        color = CandleRed.copy(alpha = 0.15f),
                        topLeft = Offset(centerX - (candleBodyWidth / 2f), bodyTop - 8f),
                        size = Size(candleBodyWidth * 2f, 16f)
                    )
                }
            }

            // Draw current price line
            val currentY = getY(currentPrice)
            drawLine(
                color = CyberCyan,
                start = Offset(0f, currentY),
                end = Offset(width, currentY),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
            )

            // Current price pulse dot
            drawCircle(
                color = CyberCyan,
                radius = 5f,
                center = Offset(width - 12f, currentY)
            )
        }
    }
}
