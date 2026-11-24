package com.example.glassfinance.ui.transactions

import androidx.compose.ui.unit.Dp
import com.example.glassfinance.ui.components.LocalGlassOpacity
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.isIncome
import com.example.glassfinance.ui.components.EmptyState
import com.example.glassfinance.ui.components.TransactionRow
import com.example.glassfinance.utils.Formatters
import java.time.LocalDate

private val filters = listOf("Tất cả", "Thu", "Chi")

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionsScreen(
    transactions: List<TransactionEntity>,
    hideAmounts: Boolean,
    onOpenTransaction: (TransactionEntity) -> Unit
) {
    var filterIndex by rememberSaveable { mutableIntStateOf(0) }

    val filtered = remember(transactions, filterIndex) {
        transactions.filter { tx ->
            when (filterIndex) {
                1 -> tx.isIncome
                2 -> !tx.isIncome
                else -> true
            }
        }
    }

    // Gom theo ngày (danh sách đã sắp mới → cũ nên thứ tự nhóm cũng mới → cũ).
    val groups = remember(filtered) {
        filtered.groupBy { Formatters.toLocalDate(it.transactionTime) }.toList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = "Giao dịch",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "${filtered.size} giao dịch",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.forEachIndexed { index, label ->
                FilterChip(
                    selected = filterIndex == index,
                    onClick = { filterIndex = index },
                    label = { Text(label) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        if (groups.isEmpty()) {
            Spacer(Modifier.height(24.dp))
            EmptyState(
                emoji = "🧾",
                title = "Chưa có giao dịch",
                message = "Giao dịch từ thông báo ngân hàng hoặc bấm ＋ sẽ hiện ở đây."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // chừa chỗ cho nút ＋ để không che số tiền của dòng cuối
                contentPadding = PaddingValues(bottom = 136.dp),
                // Một khoảng cách duy nhất giữa mọi phần tử nên tiêu đề ngày và các dòng luôn cách đều nhau.
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groups.forEachIndexed { index, (date, dayTransactions) ->
                    stickyHeader(key = "day-$date") {
                        DayHeader(
                            date = date,
                            transactions = dayTransactions,
                            hideAmounts = hideAmounts,
                            // Lùi tiêu đề xuống, cách giao dịch của ngày trước; nhóm đầu không cần.
                            topGap = if (index == 0) 0.dp else 16.dp
                        )
                    }
                    items(dayTransactions, key = { it.id }) { tx ->
                        TransactionRow(
                            transaction = tx,
                            hideAmount = hideAmounts,
                            showDate = false,
                            onClick = { onOpenTransaction(tx) }
                        )
                    }
                }
            }
        }
    }
}

/** Tiêu đề nhóm ngày: "Hôm nay  19/11/2026" bên trái, tổng thu - chi trong ngày bên phải. */
@Composable
private fun DayHeader(
    date: LocalDate,
    transactions: List<TransactionEntity>,
    hideAmounts: Boolean,
    topGap: Dp = 0.dp
) {
    val net = transactions.sumOf { if (it.isIncome) it.amount else -it.amount }

    // Viên kính trong suốt thay cho dải nền đặc: nền phía sau (quầng màu, dòng giao dịch đang cuộn) vẫn lờ mờ hiện ra.
    val shape = RoundedCornerShape(20.dp)
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    // Tiêu đề ngày dính trên đầu khi cuộn: nền đậm và gần như đặc để chữ không bị dòng giao dịch phía sau làm rối.
    val glass = (LocalGlassOpacity.current + 0.1f).coerceIn(0.90f, 0.97f)
    val rim = Brush.linearGradient(
        listOf(
            Color.White.copy(alpha = if (dark) 0.25f else 0.8f),
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topGap)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = glass))
            .border(1.dp, rim, shape)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = Formatters.weekdayLabel(date),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = Formatters.date(date),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = if (hideAmounts) Formatters.HIDDEN
            else Formatters.signedMoney(kotlin.math.abs(net), net >= 0),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
