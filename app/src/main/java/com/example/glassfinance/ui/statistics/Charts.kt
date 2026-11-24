package com.example.glassfinance.ui.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.glassfinance.ui.theme.FinanceTheme
import com.example.glassfinance.utils.MonthTotals
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassfinance.utils.Formatters
import java.time.YearMonth

/** Cột đôi thu / chi theo tháng, vẽ bằng Canvas (không cần thư viện ngoài). */
/**
 * Cột đôi thu / chi theo tháng. Cuộn ngang được, mỗi cột kèm số tiền thu / chi của tháng đó.
 * Chạm vào một tháng → [onSelect]; tháng đang chọn ([selected]) được tô nền.
 */
@Composable
fun MonthlyBarChart(
    data: List<MonthTotals>,
    modifier: Modifier = Modifier,
    selected: YearMonth? = null,
    hideAmounts: Boolean = false,
    onSelect: ((YearMonth) -> Unit)? = null
) {
    val colors = FinanceTheme.colors
    val maxValue = remember(data) {
        (data.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L).coerceAtLeast(1L).toFloat()
    }
    // Mở ra là thấy tháng mới nhất (cuối danh sách).
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = (data.size - 1).coerceAtLeast(0))
    val barArea = 120.dp

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(data, key = { it.month.toString() }) { m ->
            val isSelected = m.month == selected
            Column(
                modifier = Modifier
                    .width(66.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else Color.Transparent
                    )
                    .then(if (onSelect != null) Modifier.clickable { onSelect(m.month) } else Modifier)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.height(barArea),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    MonthBar(m.income / maxValue, barArea, colors.income)
                    MonthBar(m.expense / maxValue, barArea, colors.expense)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "T${m.month.monthValue}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(Formatters.compact(m.income, hideAmounts), fontSize = 10.sp, color = colors.income, maxLines = 1)
                Text(Formatters.compact(m.expense, hideAmounts), fontSize = 10.sp, color = colors.expense, maxLines = 1)
            }
        }
    }
}

@Composable
private fun MonthBar(fraction: Float, area: Dp, color: Color) {
    Box(
        modifier = Modifier
            .width(14.dp)
            .height(maxOf(area * fraction, 2.dp))
            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
            .background(color)
    )
}

/** Biểu đồ tròn rỗng giữa. [slices] = (tỉ lệ 0..1, màu). Nội dung [center] đặt ở giữa vòng. */
@Composable
fun DonutChart(
    slices: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier,
    center: @Composable () -> Unit = {}
) {
    val track = MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 20.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )

            var start = -90f
            slices.forEach { (fraction, color) ->
                val sweep = 360f * fraction
                if (sweep > 0f) {
                    drawArc(
                        color = color,
                        startAngle = start,
                        sweepAngle = (sweep - 2f).coerceAtLeast(0.5f),
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                }
                start += sweep
            }
        }
        center()
    }
}
