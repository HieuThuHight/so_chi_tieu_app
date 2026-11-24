package com.example.glassfinance.ui.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassfinance.data.model.BudgetEntity
import com.example.glassfinance.data.model.Categories
import com.example.glassfinance.ui.categories.CategoryPicker
import com.example.glassfinance.ui.components.GlassCard
import com.example.glassfinance.ui.components.SectionTitle
import com.example.glassfinance.ui.theme.FinanceTheme
import com.example.glassfinance.utils.Formatters

/**
 * Chức năng "Ngân sách": hạn mức chi theo tháng cho từng danh mục,
 * so với số đã chi của tháng đang xem ([spentByCategory]).
 */
@Composable
fun BudgetSection(
    budgets: List<BudgetEntity>,
    spentByCategory: Map<String, Long>,
    hideAmounts: Boolean,
    onSetBudget: (category: String, limit: Long) -> Unit,
    onDeleteBudget: (BudgetEntity) -> Unit
) {
    var showEditor by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<BudgetEntity?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("Ngân sách tháng")
            TextButton(onClick = {
                editing = null
                showEditor = true
            }) { Text("＋ Đặt ngân sách") }
        }

        if (budgets.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "💡 Đặt hạn mức cho từng danh mục để app cảnh báo khi bạn sắp chi vượt.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            budgets.forEach { budget ->
                BudgetRow(
                    budget = budget,
                    spent = spentByCategory[budget.category] ?: 0L,
                    hideAmounts = hideAmounts,
                    onClick = {
                        editing = budget
                        showEditor = true
                    }
                )
            }
        }
    }

    if (showEditor) {
        BudgetDialog(
            existing = editing,
            onDismiss = { showEditor = false },
            onSave = onSetBudget,
            onDelete = onDeleteBudget
        )
    }
}

@Composable
private fun BudgetRow(
    budget: BudgetEntity,
    spent: Long,
    hideAmounts: Boolean,
    onClick: () -> Unit
) {
    val colors = FinanceTheme.colors
    val ratio = if (budget.monthlyLimit > 0) spent.toFloat() / budget.monthlyLimit.toFloat() else 0f
    val barColor = when {
        ratio > 1f -> colors.expense
        ratio >= 0.8f -> colors.warning
        else -> colors.income
    }
    val statusText = if (spent > budget.monthlyLimit) {
        "Vượt ${Formatters.money(spent - budget.monthlyLimit, hideAmounts)}"
    } else {
        "Còn ${Formatters.money(budget.monthlyLimit - spent, hideAmounts)}"
    }

    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${Categories.emojiOf(budget.category)} ${budget.category}",
                    fontWeight = FontWeight.SemiBold
                )
                Text(statusText, color = barColor, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { ratio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${Formatters.money(spent, hideAmounts)} / ${Formatters.money(budget.monthlyLimit, hideAmounts)}  •  ${(ratio * 100).toInt()}%",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BudgetDialog(
    existing: BudgetEntity?,
    onDismiss: () -> Unit,
    onSave: (String, Long) -> Unit,
    onDelete: (BudgetEntity) -> Unit
) {
    var category by remember { mutableStateOf(existing?.category ?: Categories.expense.first().name) }
    var limitText by remember { mutableStateOf(existing?.monthlyLimit?.toString() ?: "") }
    val limit = limitText.toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Đặt ngân sách" else "Sửa ngân sách") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Danh mục", style = MaterialTheme.typography.labelLarge)
                CategoryPicker(
                    categories = Categories.expense.map { it.name },
                    selected = category,
                    onSelect = { if (existing == null) category = it }
                )
                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it.filter { c -> c.isDigit() }.take(12) },
                    label = { Text("Hạn mức mỗi tháng (₫)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { if (limit > 0) Text(Formatters.money(limit)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = limit > 0,
                onClick = {
                    onSave(category, limit)
                    onDismiss()
                }
            ) { Text("Lưu") }
        },
        dismissButton = {
            Row {
                if (existing != null) {
                    TextButton(onClick = {
                        onDelete(existing)
                        onDismiss()
                    }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
                }
                TextButton(onClick = onDismiss) { Text("Hủy") }
            }
        }
    )
}
