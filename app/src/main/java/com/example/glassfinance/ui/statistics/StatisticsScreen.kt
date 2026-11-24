package com.example.glassfinance.ui.statistics

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassfinance.data.model.BudgetEntity
import com.example.glassfinance.data.model.Categories
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.ui.budgets.BudgetSection
import com.example.glassfinance.ui.components.EmptyState
import com.example.glassfinance.ui.components.GlassCard
import com.example.glassfinance.ui.components.ScreenHeader
import com.example.glassfinance.ui.components.SectionTitle
import com.example.glassfinance.ui.theme.ChartPalette
import com.example.glassfinance.ui.theme.FinanceTheme
import com.example.glassfinance.utils.Formatters
import com.example.glassfinance.utils.StatsCalculator
import java.time.YearMonth
import kotlin.math.abs
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.lazy.items
import com.example.glassfinance.ui.components.TransactionRow
import com.example.glassfinance.utils.yearMonth
import java.time.temporal.ChronoUnit

@Composable
fun StatisticsScreen(
    transactions: List<TransactionEntity>,
    budgets: List<BudgetEntity>,
    hideAmounts: Boolean,
    onSetBudget: (String, Long) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit,
    onOpenTransaction: ((TransactionEntity) -> Unit)? = null
) {
    // 0 = tháng hiện tại, -1 = tháng trước, ...
    var monthOffset by rememberSaveable { mutableIntStateOf(0) }
    val month = remember(monthOffset) { YearMonth.now().plusMonths(monthOffset.toLong()) }

    val report = remember(transactions, month) { StatsCalculator.report(transactions, month) }
    // 12 tháng gần nhất cố định; chạm vào cột để đổi tháng đang xem.
    val history = remember(transactions) { StatsCalculator.lastMonths(transactions, YearMonth.now(), 12) }
    val monthTx = remember(transactions, month) {
        transactions.filter { it.yearMonth() == month }.sortedByDescending { it.transactionTime }
    }
    // Mặc định chỉ hiện 4 giao dịch; bấm "Xem đầy đủ" để mở hết. Đổi tháng thì thu gọn lại.
    var showAllTx by rememberSaveable(monthOffset) { mutableStateOf(false) }
    val visibleTx = if (showAllTx) monthTx else monthTx.take(4)
    val byCategory = remember(transactions, month) { StatsCalculator.expenseByCategory(transactions, month) }
    val spentMap = remember(byCategory) { byCategory.associate { it.category to it.amount } }

    val colors = FinanceTheme.colors
    val totals = report.totals

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 136.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenHeader("Thống kê", "Biểu đồ thu chi và báo cáo theo tháng") }

        // ----- Chọn tháng -----
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { monthOffset -= 1 }) { Text("‹", fontSize = 26.sp) }
                    Text(Formatters.monthLabel(month), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = { monthOffset += 1 }, enabled = monthOffset < 0) {
                        Text("›", fontSize = 26.sp)
                    }
                }
            }
        }

        // ----- Tổng thu / chi / chênh lệch -----
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBlock("Thu", Formatters.compact(totals.income, hideAmounts), colors.income)
                    StatBlock("Chi", Formatters.compact(totals.expense, hideAmounts), colors.expense)
                    StatBlock(
                        "Chênh lệch",
                        Formatters.compact(totals.net, hideAmounts),
                        if (totals.net >= 0) colors.income else colors.expense
                    )
                }
            }
        }

        // ----- Biểu đồ 12 tháng -----
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Thu chi 12 tháng gần nhất", fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LegendDot(colors.income)
                        Text("  Thu", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(14.dp))
                        LegendDot(colors.expense)
                        Text("  Chi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Chạm vào cột tháng để xem giao dịch của tháng đó",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    MonthlyBarChart(
                        data = history,
                        selected = month,
                        hideAmounts = hideAmounts,
                        onSelect = { picked ->
                            monthOffset = YearMonth.now().until(picked, ChronoUnit.MONTHS).toInt()
                        }
                    )
                }
            }
        }

        // ----- Giao dịch của tháng đang chọn -----
        item { SectionTitle("Giao dịch ${Formatters.monthLabel(month).lowercase()}") }
        if (monthTx.isEmpty()) {
            item {
                EmptyState(
                    emoji = "🗓️",
                    title = "Chưa có giao dịch",
                    message = "Tháng này chưa có giao dịch nào."
                )
            }
        } else {
            items(visibleTx, key = { it.id }) { tx ->
                TransactionRow(
                    transaction = tx,
                    hideAmount = hideAmounts,
                    onClick = onOpenTransaction?.let { open -> { open(tx) } }
                )
            }
            if (monthTx.size > 4) {
                item {
                    TextButton(
                        onClick = { showAllTx = !showAllTx },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (showAllTx) "Thu gọn" else "Xem đầy đủ (${monthTx.size} giao dịch)")
                    }
                }
            }
        }

        // ----- Chi tiêu theo danh mục -----
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Chi tiêu theo danh mục")
                if (byCategory.isEmpty()) {
                    EmptyState(
                        emoji = "📊",
                        title = "Chưa có chi tiêu",
                        message = "Tháng này chưa có khoản chi nào để thống kê."
                    )
                } else {
                    val total = totals.expense.coerceAtLeast(1L)
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                DonutChart(
                                    slices = byCategory.mapIndexed { index, spend ->
                                        (spend.amount.toFloat() / total.toFloat()) to ChartPalette[index % ChartPalette.size]
                                    },
                                    modifier = Modifier.size(170.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Tổng chi", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text(
                                            Formatters.compact(totals.expense, hideAmounts),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            byCategory.forEachIndexed { index, spend ->
                                val percent = (spend.amount * 100f / total).toInt()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LegendDot(ChartPalette[index % ChartPalette.size])
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "${Categories.emojiOf(spend.category)} ${spend.category}",
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        "${Formatters.money(spend.amount, hideAmounts)}  ·  $percent%",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ----- Ngân sách (chức năng bên trong Thống kê) -----
        item {
            BudgetSection(
                budgets = budgets,
                spentByCategory = spentMap,
                hideAmounts = hideAmounts,
                onSetBudget = onSetBudget,
                onDeleteBudget = onDeleteBudget
            )
        }

        // ----- Báo cáo nhanh -----
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Báo cáo nhanh")
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InsightRow("💸", "Chi trung bình mỗi ngày", Formatters.money(report.avgDailyExpense, hideAmounts))

                        report.savingRate?.let { rate ->
                            InsightRow("💰", "Tỷ lệ tiết kiệm", "$rate%")
                        }

                        report.expenseChangePct?.let { pct ->
                            val text = when {
                                pct > 0 -> "Tăng $pct% so với tháng trước"
                                pct < 0 -> "Giảm ${abs(pct)}% so với tháng trước"
                                else -> "Không đổi so với tháng trước"
                            }
                            InsightRow(if (pct > 0) "📈" else "📉", "Chi tiêu", text)
                        }

                        report.topCategory?.let { top ->
                            val share = if (totals.expense > 0) (top.amount * 100 / totals.expense).toInt() else 0
                            InsightRow("🏆", "Danh mục chi nhiều nhất", "${top.category} ($share%)")
                        }

                        report.biggestExpense?.let { tx ->
                            InsightRow(
                                "🧾",
                                "Khoản chi lớn nhất",
                                "${tx.content.ifBlank { tx.category }} — ${Formatters.money(tx.amount, hideAmounts)}"
                            )
                        }

                        if (totals.income == 0L && totals.expense == 0L) {
                            Text(
                                "Tháng này chưa có dữ liệu để lập báo cáo.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun InsightRow(emoji: String, title: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}
