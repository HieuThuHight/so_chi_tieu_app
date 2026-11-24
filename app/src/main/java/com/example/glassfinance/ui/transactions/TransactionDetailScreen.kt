package com.example.glassfinance.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassfinance.data.model.Categories
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.TransactionSource
import com.example.glassfinance.data.model.isIncome
import com.example.glassfinance.ui.components.GlassCard
import com.example.glassfinance.ui.theme.FinanceTheme
import com.example.glassfinance.utils.Formatters

@Composable
fun TransactionDetailScreen(
    transaction: TransactionEntity,
    hideAmounts: Boolean,
    onBack: () -> Unit,
    onEdit: () -> Unit
) {
    val tint = if (transaction.isIncome) FinanceTheme.colors.income else FinanceTheme.colors.expense
    val category = transaction.category.ifBlank { "Chưa phân loại" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("‹  Giao dịch", style = MaterialTheme.typography.titleMedium)
        }

        Spacer(Modifier.height(4.dp))

        // Phần đầu: danh mục + số tiền là thứ người dùng cần thấy đầu tiên.
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(Categories.emojiOf(transaction.category), fontSize = 32.sp)
                }

                Spacer(Modifier.height(12.dp))

                Text(
                    text = category,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = if (hideAmounts) Formatters.HIDDEN
                    else Formatters.signedMoney(transaction.amount, transaction.isIncome),
                    style = MaterialTheme.typography.headlineMedium,
                    fontSize = 34.sp,
                    color = tint
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "${Formatters.fullDate(transaction.transactionTime)} · ${Formatters.time(transaction.transactionTime)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                DetailRow("Loại", if (transaction.isIncome) "Thu tiền" else "Chi tiền")
                RowDivider()
                DetailRow("Ngân hàng", transaction.bankName)
                RowDivider()
                DetailRow("Nội dung", transaction.content.ifBlank { "—" })
                RowDivider()
                DetailRow("Nguồn", sourceLabel(transaction.source))
                if (!transaction.externalId.isNullOrBlank()) {
                    RowDivider()
                    DetailRow("Mã giao dịch", transaction.externalId)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Sửa giao dịch", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

private fun sourceLabel(source: String): String = when (source) {
    TransactionSource.NOTIFICATION -> "Thông báo ngân hàng"
    TransactionSource.GMAIL -> "Gmail"
    TransactionSource.SHORTCUT -> "Phím tắt"
    TransactionSource.MANUAL -> "Nhập tay"
    else -> source.ifBlank { "—" }
}
