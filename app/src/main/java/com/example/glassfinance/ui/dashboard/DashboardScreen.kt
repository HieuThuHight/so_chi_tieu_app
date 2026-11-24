package com.example.glassfinance.ui.dashboard

import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassfinance.data.model.DebtWithPayments
import com.example.glassfinance.data.model.SavingsGoalEntity
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.isIncome
import com.example.glassfinance.ui.components.EmptyState
import com.example.glassfinance.ui.components.GlassCard
import com.example.glassfinance.ui.components.SectionTitle
import com.example.glassfinance.ui.components.TransactionRow
import com.example.glassfinance.ui.savings.SavingsSection
import com.example.glassfinance.ui.theme.FinanceTheme
import com.example.glassfinance.utils.Formatters
import com.example.glassfinance.utils.StatsCalculator
import java.time.YearMonth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape

@Composable
fun DashboardScreen(
    transactions: List<TransactionEntity>,
    goals: List<SavingsGoalEntity>,
    hideBalance: Boolean,
    onToggleHideBalance: () -> Unit,
    onOpenTransaction: (TransactionEntity) -> Unit,
    onSeeAllTransactions: () -> Unit,
    onAddGoal: (String, Long) -> Unit,
    onUpdateGoal: (SavingsGoalEntity) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit,
    debts: List<DebtWithPayments>,
    onOpenDebts: () -> Unit,
    onOpenSettings: () -> Unit = {}
) {
    val colors = FinanceTheme.colors

    // Tính lại khi dữ liệu đổi (kể cả khi sang tháng mới).
    val month = remember(transactions) { YearMonth.now() }
    val balance = remember(transactions) {
        transactions.sumOf { if (it.isIncome) it.amount else -it.amount }
    }
    val totals = remember(transactions, month) { StatsCalculator.totalsFor(transactions, month) }
    val recent = remember(transactions) { transactions.take(5) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 136.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "GLASS FINANCE",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Tổng quan",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleHideBalance) {
                        Text(if (hideBalance) "🙈" else "👁️", fontSize = 22.sp)
                    }
                    IconButton(onClick = onOpenSettings) {
                        Text("⚙️", fontSize = 22.sp)
                    }
                }
            }
        }

        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(Brush.linearGradient(listOf(colors.heroStart, colors.heroEnd)))
                    // Vệt sáng phía trên + viền sáng để thẻ chính cũng có chất kính.
                    .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.16f), Color.Transparent), endY = 260f))
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.55f), Color.White.copy(alpha = 0.05f))),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(22.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(170.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 60.dp, y = (-60).dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                )
                Column {
                    Text("Tổng số dư", color = colors.onHeroMuted, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        Formatters.money(balance, hideBalance),
                        color = colors.onHero,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        HeroStat(
                            label = "↓ Thu tháng này",
                            value = Formatters.money(totals.income, hideBalance),
                            labelColor = Color(0xFF9BE5BD),
                            modifier = Modifier.weight(1f)
                        )
                        HeroStat(
                            label = "↑ Chi tháng này",
                            value = Formatters.money(totals.expense, hideBalance),
                            labelColor = Color(0xFFFFB2B2),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SectionTitle("Tình hình ${Formatters.monthLabel(month).lowercase()}")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "Thu nhập",
                        amount = Formatters.compact(totals.income, hideBalance),
                        color = colors.income,
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "Chi tiêu",
                        amount = Formatters.compact(totals.expense, hideBalance),
                        color = colors.expense,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            val lent = debts.filter { !it.debt.isBorrowed }.sumOf { it.remaining }
            val borrowed = debts.filter { it.debt.isBorrowed }.sumOf { it.remaining }
            GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onOpenDebts) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("🤝 Sổ vay / nợ", fontWeight = FontWeight.SemiBold)
                        Text(
                            if (debts.isEmpty()) "Ghi lại khoản cho vay, đi vay và theo dõi trả nợ"
                            else "Cho vay còn ${Formatters.compact(lent, hideBalance)} · Đi vay còn ${Formatters.compact(borrowed, hideBalance)}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text("›", fontSize = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            SavingsSection(
                goals = goals,
                hideAmounts = hideBalance,
                onAddGoal = onAddGoal,
                onUpdateGoal = onUpdateGoal,
                onDeleteGoal = onDeleteGoal
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionTitle("Giao dịch gần đây")
                if (transactions.isNotEmpty()) {
                    TextButton(onClick = onSeeAllTransactions) { Text("Xem tất cả") }
                }
            }
        }

        if (recent.isEmpty()) {
            item {
                EmptyState(
                    emoji = "✨",
                    title = "Chưa có giao dịch",
                    message = "Bấm nút ＋ để thêm giao dịch đầu tiên,\nhoặc tạo dữ liệu mẫu trong Cài đặt."
                )
            }
        } else {
            items(recent, key = { it.id }) { tx ->
                TransactionRow(
                    transaction = tx,
                    hideAmount = hideBalance,
                    onClick = { onOpenTransaction(tx) }
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    GlassCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Text(amount, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun HeroStat(
    label: String,
    value: String,
    labelColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(label, color = labelColor, fontSize = 12.sp)
        Spacer(Modifier.height(2.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, maxLines = 1)
    }
}