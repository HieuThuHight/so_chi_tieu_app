package com.example.glassfinance.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.isIncome
import com.example.glassfinance.ui.components.EmptyState
import com.example.glassfinance.ui.components.GlassCard
import com.example.glassfinance.ui.components.ScreenHeader
import com.example.glassfinance.ui.components.SectionTitle
import com.example.glassfinance.ui.components.TransactionRow
import com.example.glassfinance.ui.theme.FinanceTheme
import com.example.glassfinance.utils.DayTotals
import com.example.glassfinance.utils.Formatters
import com.example.glassfinance.utils.PeriodStats
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/** Tab "Lịch": chuyển giữa lịch tháng và báo cáo theo khoảng thời gian. */
@Composable
fun CalendarScreen(
    transactions: List<TransactionEntity>,
    hideAmounts: Boolean,
    onOpenTransaction: (TransactionEntity) -> Unit
) {
    var mode by rememberSaveable { mutableIntStateOf(0) } // 0 = Lịch, 1 = Báo cáo

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(start = 18.dp, end = 18.dp, top = 16.dp)) {
            ScreenHeader("Lịch", "Thu chi theo ngày và báo cáo theo khoảng thời gian")
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = mode == 0, onClick = { mode = 0 }, label = { Text("📅 Lịch tháng") })
                FilterChip(selected = mode == 1, onClick = { mode = 1 }, label = { Text("📊 Báo cáo") })
            }
        }
        if (mode == 0) {
            MonthCalendarView(transactions, hideAmounts, onOpenTransaction)
        } else {
            PeriodReportView(transactions, hideAmounts, onOpenTransaction)
        }
    }
}

private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val WEEKDAYS = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

private fun dayName(d: DayOfWeek): String = when (d) {
    DayOfWeek.MONDAY -> "Thứ Hai"
    DayOfWeek.TUESDAY -> "Thứ Ba"
    DayOfWeek.WEDNESDAY -> "Thứ Tư"
    DayOfWeek.THURSDAY -> "Thứ Năm"
    DayOfWeek.FRIDAY -> "Thứ Sáu"
    DayOfWeek.SATURDAY -> "Thứ Bảy"
    DayOfWeek.SUNDAY -> "Chủ nhật"
}

/** 1.250.000 -> "1,2tr", 85.000 -> "85k". Ngắn để vừa ô lịch. */
internal fun shortMoney(a: Long): String = when {
    a >= 1_000_000 -> {
        val tenths = a / 100_000
        val whole = tenths / 10
        val frac = tenths % 10
        "$whole${if (frac != 0L) ",$frac" else ""}tr"
    }
    a >= 1_000 -> "${a / 1_000}k"
    else -> a.toString()
}

@Composable
private fun MonthCalendarView(
    transactions: List<TransactionEntity>,
    hideAmounts: Boolean,
    onOpenTransaction: (TransactionEntity) -> Unit
) {
    val today = LocalDate.now()
    // YearMonth / LocalDate không lưu thẳng được → lưu dạng số.
    var monthIndex by rememberSaveable { mutableIntStateOf(today.year * 12 + today.monthValue - 1) }
    var selectedEpochDay by rememberSaveable { mutableLongStateOf(today.toEpochDay()) }

    val month = YearMonth.of(monthIndex / 12, monthIndex % 12 + 1)
    val selected = LocalDate.ofEpochDay(selectedEpochDay)

    val totals = remember(transactions, month) { PeriodStats.dayTotals(transactions, month) }
    val monthIncome = totals.values.sumOf { it.income }
    val monthExpense = totals.values.sumOf { it.expense }
    val dayTxs = remember(transactions, selected) { PeriodStats.transactionsOn(transactions, selected) }
    val dayIncome = dayTxs.filter { it.isIncome }.sumOf { it.amount }
    val dayExpense = dayTxs.filter { !it.isIncome }.sumOf { it.amount }

    fun goToMonth(delta: Int) {
        monthIndex += delta
        val m = YearMonth.of(monthIndex / 12, monthIndex % 12 + 1)
        // Chọn sẵn ngày hôm nay nếu tháng này có, nếu không thì mùng 1.
        selectedEpochDay = (if (m == YearMonth.from(today)) today else m.atDay(1)).toEpochDay()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 136.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NavArrow("‹", enabled = true) { goToMonth(-1) }
                        Text(
                            Formatters.monthLabel(month),
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        NavArrow("›", enabled = true) { goToMonth(1) }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        WEEKDAYS.forEach {
                            Text(
                                it,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    MonthGrid(month, today, selected, totals, hideAmounts) { selectedEpochDay = it.toEpochDay() }
                }
            }
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryBlock("Thu tháng", Formatters.compact(monthIncome, hideAmounts), FinanceTheme.colors.income)
                    SummaryBlock("Chi tháng", Formatters.compact(monthExpense, hideAmounts), FinanceTheme.colors.expense)
                    SummaryBlock(
                        "Chênh lệch",
                        Formatters.compact(monthIncome - monthExpense, hideAmounts),
                        MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                SectionTitle("${dayName(selected.dayOfWeek)}, ${selected.format(DAY_FORMAT)}")
                if (dayTxs.isNotEmpty()) {
                    Text(
                        "Thu ${Formatters.money(dayIncome, hideAmounts)} · Chi ${Formatters.money(dayExpense, hideAmounts)}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (dayTxs.isEmpty()) {
            item { EmptyState("🗓️", "Không có giao dịch", "Ngày này chưa ghi nhận khoản thu chi nào.") }
        } else {
            items(dayTxs, key = { it.id }) { tx ->
                TransactionRow(
                    transaction = tx,
                    hideAmount = hideAmounts,
                    onClick = { onOpenTransaction(tx) }
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate,
    totals: Map<LocalDate, DayTotals>,
    hide: Boolean,
    onSelect: (LocalDate) -> Unit
) {
    val offset = month.atDay(1).dayOfWeek.value - 1 // Thứ Hai = 0
    val rows = (offset + month.lengthOfMonth() + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (r in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                for (c in 0 until 7) {
                    val dayNumber = r * 7 + c - offset + 1
                    if (dayNumber < 1 || dayNumber > month.lengthOfMonth()) {
                        Spacer(Modifier.weight(1f))
                    } else {
                        val date = month.atDay(dayNumber)
                        DayCell(
                            date = date,
                            totals = totals[date],
                            isToday = date == today,
                            isSelected = date == selected,
                            hide = hide,
                            modifier = Modifier.weight(1f),
                            onClick = { onSelect(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    totals: DayTotals?,
    isToday: Boolean,
    isSelected: Boolean,
    hide: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(12.dp)
    val bg = when {
        isSelected -> primary.copy(alpha = 0.16f)
        else -> androidx.compose.ui.graphics.Color.Transparent
    }
    Column(
        modifier = modifier
            .height(58.dp)
            .clip(shape)
            .background(bg)
            .then(if (isToday) Modifier.border(1.5.dp, primary, shape) else Modifier)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(
            date.dayOfMonth.toString(),
            fontSize = 14.sp,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
        )
        if (totals != null) {
            if (hide) {
                Box(
                    Modifier
                        .padding(top = 6.dp)
                        .size(6.dp)
                        .background(primary, CircleShape)
                )
            } else {
                if (totals.expense > 0) {
                    Text("-${shortMoney(totals.expense)}", fontSize = 9.sp, maxLines = 1, color = FinanceTheme.colors.expense)
                }
                if (totals.income > 0) {
                    Text("+${shortMoney(totals.income)}", fontSize = 9.sp, maxLines = 1, color = FinanceTheme.colors.income)
                }
            }
        }
    }
}

@Composable
internal fun NavArrow(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            symbol,
            fontSize = 26.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.25f)
        )
    }
}

@Composable
internal fun SummaryBlock(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold, color = color)
    }
}
