package com.example.glassfinance.ui.calendar

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassfinance.data.model.Categories
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.ui.components.EmptyState
import com.example.glassfinance.ui.components.GlassCard
import com.example.glassfinance.ui.components.SectionTitle
import com.example.glassfinance.ui.components.TransactionRow
import com.example.glassfinance.ui.theme.ChartPalette
import com.example.glassfinance.ui.theme.FinanceTheme
import com.example.glassfinance.utils.BarPoint
import com.example.glassfinance.utils.Formatters
import com.example.glassfinance.utils.Period
import com.example.glassfinance.utils.PeriodStats
import com.example.glassfinance.utils.PeriodType
import java.time.LocalDate
import kotlin.math.abs

/** Báo cáo theo Tuần / Tháng / Quý / Năm, bấm ‹ › để xem kỳ trước / sau. */
@Composable
internal fun PeriodReportView(
    transactions: List<TransactionEntity>,
    hideAmounts: Boolean,
    onOpenTransaction: (TransactionEntity) -> Unit
) {
    var typeOrdinal by rememberSaveable { mutableIntStateOf(PeriodType.MONTH.ordinal) }
    var offset by rememberSaveable { mutableIntStateOf(0) } // 0 = kỳ hiện tại, -1 = kỳ trước…

    val today = LocalDate.now()
    val type = PeriodType.entries[typeOrdinal]
    val period = remember(type, offset, today) { Period.of(type, today).shift(offset) }
    val report = remember(transactions, period, today) { PeriodStats.report(transactions, period, today) }
    val canGoNext = offset < 0 // không cho xem kỳ tương lai

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 136.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodType.entries.forEach { t ->
                    FilterChip(
                        selected = t == type,
                        onClick = { typeOrdinal = t.ordinal; offset = 0 },
                        label = { Text(t.label) }
                    )
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NavArrow("‹", enabled = true) { offset -= 1 }
                        Text(
                            period.label(),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        NavArrow("›", enabled = canGoNext) { if (canGoNext) offset += 1 }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryBlock("Thu", Formatters.compact(report.income, hideAmounts), FinanceTheme.colors.income)
                        SummaryBlock("Chi", Formatters.compact(report.expense, hideAmounts), FinanceTheme.colors.expense)
                        SummaryBlock(
                            "Còn lại",
                            Formatters.compact(report.net, hideAmounts),
                            if (report.net >= 0) FinanceTheme.colors.income else FinanceTheme.colors.expense
                        )
                    }
                }
            }
        }

        if (report.count == 0) {
            item { EmptyState("📭", "Chưa có giao dịch", "Kỳ này chưa ghi nhận khoản thu chi nào.") }
            return@LazyColumn
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Thu chi theo ${if (type == PeriodType.WEEK || type == PeriodType.MONTH) "ngày" else "tháng"}", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    PeriodBarChart(report.bars, hideAmounts)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Legend("Thu", FinanceTheme.colors.income)
                        Legend("Chi", FinanceTheme.colors.expense)
                    }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    InsightRow("🧾", "Số giao dịch", "${report.count}")
                    InsightRow("📆", "Chi trung bình / ngày", Formatters.money(report.avgDailyExpense, hideAmounts))
                    InsightRow(
                        "💰", "Tỷ lệ tiết kiệm",
                        report.savingRate?.let { "$it%" } ?: "Chưa có thu nhập"
                    )
                    InsightRow(
                        "📈", "Chi so với kỳ trước",
                        report.expenseChangePct?.let { (if (it > 0) "+" else "") + "$it%" } ?: "Kỳ trước không có chi"
                    )
                    report.biggestExpense?.let {
                        InsightRow("🏷️", "Khoản chi lớn nhất", Formatters.money(it.amount, hideAmounts))
                    }
                }
            }
        }

        if (report.topCategories.isNotEmpty()) {
            item { SectionTitle("Chi theo danh mục") }
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        report.topCategories.forEachIndexed { i, c ->
                            val pct = if (report.expense > 0) (c.amount * 100 / report.expense).toInt() else 0
                            CategoryBar(
                                label = "${Categories.emojiOf(c.category)} ${c.category}",
                                value = Formatters.money(c.amount, hideAmounts),
                                pct = pct,
                                color = ChartPalette[i % ChartPalette.size]
                            )
                        }
                    }
                }
            }
        }

        report.biggestExpense?.let { big ->
            item { SectionTitle("Khoản chi lớn nhất kỳ này") }
            item {
                TransactionRow(transaction = big, hideAmount = hideAmounts, onClick = { onOpenTransaction(big) })
            }
        }
    }
}

@Composable
private fun PeriodBarChart(bars: List<BarPoint>, hide: Boolean) {
    val incomeColor = FinanceTheme.colors.income
    val expenseColor = FinanceTheme.colors.expense
    val axis = MaterialTheme.colorScheme.outlineVariant
    val maxValue = (bars.maxOfOrNull { maxOf(it.income, it.expense) } ?: 0L).coerceAtLeast(1L)
    val labelStep = if (bars.size > 14) 5 else 1

    Column {
        Canvas(modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)) {
            val n = bars.size.coerceAtLeast(1)
            val slot = size.width / n
            val barW = (slot * 0.36f).coerceAtMost(14.dp.toPx())
            val h = size.height
            val minH = 2.dp.toPx()
            val radius = CornerRadius(2.dp.toPx())

            drawLine(axis, Offset(0f, h), Offset(size.width, h), strokeWidth = 1.dp.toPx())
            bars.forEachIndexed { i, b ->
                val cx = slot * i + slot / 2f
                if (b.income > 0) {
                    val bh = (h * b.income / maxValue).coerceAtLeast(minH)
                    drawRoundRect(incomeColor, Offset(cx - barW, h - bh), Size(barW, bh), radius)
                }
                if (b.expense > 0) {
                    val bh = (h * b.expense / maxValue).coerceAtLeast(minH)
                    drawRoundRect(expenseColor, Offset(cx, h - bh), Size(barW, bh), radius)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            bars.forEachIndexed { i, b ->
                Text(
                    text = if (i % labelStep == 0) b.label else "",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontSize = 9.sp,
                    maxLines = 1,
                    softWrap = false,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (hide) {
            Text(
                "Số tiền đang được ẩn",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Legend(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.size(6.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InsightRow(emoji: String, title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.size(10.dp))
        Text(title, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CategoryBar(label: String, value: String, pct: Int, color: Color) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontWeight = FontWeight.Medium)
            Text("$value · $pct%", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(color.copy(alpha = 0.18f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(abs(pct).coerceIn(1, 100) / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(color)
            )
        }
    }
}
