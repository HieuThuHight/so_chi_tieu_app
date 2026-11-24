package com.example.glassfinance.ui.savings

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
import com.example.glassfinance.data.model.SavingsGoalEntity
import com.example.glassfinance.ui.components.GlassCard
import com.example.glassfinance.ui.components.SectionTitle
import com.example.glassfinance.ui.theme.FinanceTheme
import com.example.glassfinance.utils.Formatters

/** Chức năng "Tiết kiệm": các mục tiêu, nạp / rút tiền, thêm / xóa mục tiêu. */
@Composable
fun SavingsSection(
    goals: List<SavingsGoalEntity>,
    hideAmounts: Boolean,
    onAddGoal: (name: String, target: Long) -> Unit,
    onUpdateGoal: (SavingsGoalEntity) -> Unit,
    onDeleteGoal: (SavingsGoalEntity) -> Unit
) {
    var showAdd by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<SavingsGoalEntity?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("Mục tiêu tiết kiệm")
            TextButton(onClick = { showAdd = true }) { Text("＋ Thêm") }
        }

        if (goals.isEmpty()) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "🎯 Đặt một mục tiêu để theo dõi tiền tiết kiệm của bạn.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
        } else {
            goals.forEach { goal ->
                GoalCard(goal = goal, hideAmounts = hideAmounts, onClick = { selected = goal })
            }
        }
    }

    if (showAdd) {
        AddGoalDialog(
            onDismiss = { showAdd = false },
            onConfirm = { name, target -> onAddGoal(name, target) }
        )
    }

    selected?.let { goal ->
        GoalDialog(
            goal = goal,
            onDismiss = { selected = null },
            onUpdate = onUpdateGoal,
            onDelete = onDeleteGoal
        )
    }
}

@Composable
private fun GoalCard(
    goal: SavingsGoalEntity,
    hideAmounts: Boolean,
    onClick: () -> Unit
) {
    val fraction = if (goal.targetAmount > 0) {
        (goal.savedAmount.toFloat() / goal.targetAmount.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val reached = goal.savedAmount >= goal.targetAmount
    val barColor = if (reached) FinanceTheme.colors.income else MaterialTheme.colorScheme.primary

    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(goal.name, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text(
                    if (reached) "🎉 Đạt mục tiêu" else "${(fraction * 100).toInt()}%",
                    fontWeight = FontWeight.Bold,
                    color = barColor,
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = barColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "${Formatters.money(goal.savedAmount, hideAmounts)} / ${Formatters.money(goal.targetAmount, hideAmounts)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddGoalDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var targetText by remember { mutableStateOf("") }
    val target = targetText.toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Mục tiêu mới") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text("Tên mục tiêu") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it.filter { c -> c.isDigit() }.take(12) },
                    label = { Text("Số tiền cần đạt (₫)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { if (target > 0) Text(Formatters.money(target)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && target > 0,
                onClick = {
                    onConfirm(name.trim(), target)
                    onDismiss()
                }
            ) { Text("Tạo") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )
}

@Composable
private fun GoalDialog(
    goal: SavingsGoalEntity,
    onDismiss: () -> Unit,
    onUpdate: (SavingsGoalEntity) -> Unit,
    onDelete: (SavingsGoalEntity) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    val amount = amountText.toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(goal.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Đã có ${Formatters.money(goal.savedAmount)} / ${Formatters.money(goal.targetAmount)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() }.take(12) },
                    label = { Text("Số tiền nạp / rút (₫)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { if (amount > 0) Text(Formatters.money(amount)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = amount > 0,
                onClick = {
                    onUpdate(goal.copy(savedAmount = goal.savedAmount + amount))
                    onDismiss()
                }
            ) { Text("Nạp") }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        if (confirmDelete) {
                            onDelete(goal)
                            onDismiss()
                        } else {
                            confirmDelete = true
                        }
                    }
                ) {
                    Text(
                        if (confirmDelete) "Chắc chắn xóa?" else "Xóa",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                TextButton(
                    enabled = amount > 0,
                    onClick = {
                        onUpdate(goal.copy(savedAmount = (goal.savedAmount - amount).coerceAtLeast(0L)))
                        onDismiss()
                    }
                ) { Text("Rút") }
            }
        }
    )
}
