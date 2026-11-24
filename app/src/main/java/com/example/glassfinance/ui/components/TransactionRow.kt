package com.example.glassfinance.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassfinance.data.model.Categories
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.isIncome
import com.example.glassfinance.ui.theme.FinanceTheme
import com.example.glassfinance.utils.Formatters


/** Dòng giao dịch: danh mục · ngân hàng ở trên, nội dung (cắt bằng "…" nếu dài) ở dưới, số tiền + ngày giờ bên phải. */
@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    modifier: Modifier = Modifier,
    hideAmount: Boolean = false,
    // false: chỉ hiện giờ (dùng khi danh sách đã gom theo ngày, ngày nằm ở tiêu đề nhóm).
    showDate: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val tint = if (transaction.isIncome) FinanceTheme.colors.income else FinanceTheme.colors.expense
    // Nội dung lấy từ thông báo ngân hàng; chỉ hiện khi có và khác tên danh mục.
    val note = transaction.content.takeIf { it.isNotBlank() && it != transaction.category }

    GlassCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Text(Categories.emojiOf(transaction.category), fontSize = 22.sp)
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${transaction.category} · ${transaction.bankName}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (note != null) {
                    Text(
                        text = note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (hideAmount) Formatters.HIDDEN
                    else Formatters.signedMoney(transaction.amount, transaction.isIncome),
                    color = tint,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = if (showDate) {
                        "${Formatters.fullDate(transaction.transactionTime)} · ${Formatters.time(transaction.transactionTime)}"
                    } else {
                        Formatters.time(transaction.transactionTime)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}