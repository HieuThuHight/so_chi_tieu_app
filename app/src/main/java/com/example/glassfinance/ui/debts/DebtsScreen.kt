package com.example.glassfinance.ui.debts

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassfinance.data.model.DebtEntity
import com.example.glassfinance.data.model.DebtPaymentEntity
import com.example.glassfinance.data.model.DebtWithPayments
import com.example.glassfinance.ui.components.EmptyState
import com.example.glassfinance.ui.components.GlassCard
import com.example.glassfinance.ui.components.SectionTitle
import com.example.glassfinance.ui.theme.FinanceTheme
import com.example.glassfinance.utils.Formatters
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/** Sổ vay / nợ lưu cục bộ trong SQLite. */
@Composable
fun DebtsScreen(
    debts: List<DebtWithPayments>,
    hideAmounts: Boolean,
    onBack: () -> Unit,
    onAddDebt: (isBorrowed: Boolean, name: String, amount: Long, dueDate: Long, note: String) -> Unit,
    onPay: (debtId: Long, amount: Long, note: String) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
    onDeletePayment: (DebtPaymentEntity) -> Unit
) {
    val colors = FinanceTheme.colors
    var showAdd by remember { mutableStateOf(false) }
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }
    val selected = debts.firstOrNull { it.debt.id == selectedId }

    val lent = remember(debts) { debts.filter { !it.debt.isBorrowed }.sumOf { it.remaining } }
    val borrowed = remember(debts) { debts.filter { it.debt.isBorrowed }.sumOf { it.remaining } }
    val open = remember(debts) {
        debts.filter { !it.isSettled }
            .sortedBy { if (it.debt.dueDate == 0L) Long.MAX_VALUE else it.debt.dueDate }
    }
    val settled = remember(debts) { debts.filter { it.isSettled } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onBack) { Text("‹ Quay lại") }
                TextButton(onClick = { showAdd = true }) { Text("＋ Thêm khoản") }
            }
            Text(
                "Sổ vay / nợ",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Họ nợ bạn", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            Formatters.money(lent, hideAmounts),
                            color = colors.income,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Bạn nợ họ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            Formatters.money(borrowed, hideAmounts),
                            color = colors.expense,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }

        if (debts.isEmpty()) {
            item {
                EmptyState(
                    emoji = "🤝",
                    title = "Chưa có khoản vay / nợ",
                    message = "Bấm \"＋ Thêm khoản\" để ghi lại ai đang nợ bạn hoặc bạn đang nợ ai."
                )
            }
        }

        if (open.isNotEmpty()) {
            item { SectionTitle("Đang còn nợ") }
            items(open, key = { "open-${it.debt.id}" }) { item ->
                DebtCard(item, hideAmounts) { selectedId = item.debt.id }
            }
        }

        if (settled.isNotEmpty()) {
            item { SectionTitle("Đã tất toán") }
            items(settled, key = { "done-${it.debt.id}" }) { item ->
                DebtCard(item, hideAmounts) { selectedId = item.debt.id }
            }
        }
    }

    if (showAdd) {
        AddDebtDialog(
            onDismiss = { showAdd = false },
            onConfirm = onAddDebt
        )
    }

    selected?.let { item ->
        DebtDetailDialog(
            item = item,
            hideAmounts = hideAmounts,
            onDismiss = { selectedId = null },
            onPay = onPay,
            onDeleteDebt = onDeleteDebt,
            onDeletePayment = onDeletePayment
        )
    }
}

private fun isOverdue(item: DebtWithPayments): Boolean {
    val due = item.debt.dueDate
    return due > 0L && !item.isSettled && LocalDate.now().isAfter(Formatters.toLocalDate(due))
}

@Composable
private fun DebtCard(item: DebtWithPayments, hideAmounts: Boolean, onClick: () -> Unit) {
    val colors = FinanceTheme.colors
    val debt = item.debt
    val tint = if (debt.isBorrowed) colors.expense else colors.income
    val fraction = if (debt.principal > 0) (item.paid.toFloat() / debt.principal.toFloat()).coerceIn(0f, 1f) else 0f
    val overdue = isOverdue(item)

    GlassCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(debt.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (debt.isBorrowed) "Bạn đi vay" else "Bạn cho vay",
                        fontSize = 12.sp,
                        color = tint
                    )
                }
                Text(
                    if (item.isSettled) "✅ Đã xong" else Formatters.money(item.remaining, hideAmounts),
                    fontWeight = FontWeight.Bold,
                    color = if (item.isSettled) colors.income else tint
                )
            }
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = tint,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                buildString {
                    append("Đã trả ${Formatters.money(item.paid, hideAmounts)} / ${Formatters.money(debt.principal, hideAmounts)}")
                    if (debt.dueDate > 0L) {
                        append("  ·  Hạn ${Formatters.fullDate(debt.dueDate)}")
                        if (overdue) append(" (quá hạn)")
                    }
                },
                fontSize = 12.sp,
                color = if (overdue) colors.expense else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddDebtDialog(
    onDismiss: () -> Unit,
    onConfirm: (Boolean, String, Long, Long, String) -> Unit
) {
    var isBorrowed by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dueMillis by remember { mutableLongStateOf(0L) }
    var showPicker by remember { mutableStateOf(false) }
    val amount = amountText.toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm khoản vay / nợ") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isBorrowed,
                        onClick = { isBorrowed = false },
                        label = { Text("Tôi cho vay") }
                    )
                    FilterChip(
                        selected = isBorrowed,
                        onClick = { isBorrowed = true },
                        label = { Text("Tôi đi vay") }
                    )
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(40) },
                    label = { Text(if (isBorrowed) "Vay của ai?" else "Cho ai vay?") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() }.take(12) },
                    label = { Text("Số tiền (₫)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { if (amount > 0) Text(Formatters.money(amount)) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(80) },
                    label = { Text("Ghi chú (tùy chọn)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { showPicker = true }) {
                        Text(if (dueMillis > 0L) "📅 Hạn ${Formatters.fullDate(dueMillis)}" else "📅 Chọn hạn trả")
                    }
                    if (dueMillis > 0L) {
                        TextButton(onClick = { dueMillis = 0L }) { Text("Bỏ hạn") }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && amount > 0,
                onClick = {
                    onConfirm(isBorrowed, name.trim(), amount, dueMillis, note.trim())
                    onDismiss()
                }
            ) { Text("Lưu") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } }
    )

    if (showPicker) {
        val zone = ZoneId.systemDefault()
        val initial = if (dueMillis > 0L) Formatters.toLocalDate(dueMillis) else LocalDate.now()
        val state = rememberDatePickerState(
            initialSelectedDateMillis = initial.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { utc ->
                        val picked = Instant.ofEpochMilli(utc).atZone(ZoneOffset.UTC).toLocalDate()
                        dueMillis = picked.atStartOfDay(zone).toInstant().toEpochMilli()
                    }
                    showPicker = false
                }) { Text("Chọn") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Hủy") } }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun DebtDetailDialog(
    item: DebtWithPayments,
    hideAmounts: Boolean,
    onDismiss: () -> Unit,
    onPay: (Long, Long, String) -> Unit,
    onDeleteDebt: (DebtEntity) -> Unit,
    onDeletePayment: (DebtPaymentEntity) -> Unit
) {
    val debt = item.debt
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    val amount = amountText.toLongOrNull() ?: 0L
    val canPay = amount in 1..item.remaining

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(debt.name) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    (if (debt.isBorrowed) "Bạn đi vay" else "Bạn cho vay") +
                        " · gốc ${Formatters.money(debt.principal, hideAmounts)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Còn lại: ${Formatters.money(item.remaining, hideAmounts)}",
                    fontWeight = FontWeight.Bold
                )
                if (debt.dueDate > 0L) {
                    Text(
                        "Hạn trả: ${Formatters.fullDate(debt.dueDate)}" + if (isOverdue(item)) " (quá hạn)" else "",
                        color = if (isOverdue(item)) FinanceTheme.colors.expense else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (debt.note.isNotBlank()) {
                    Text("Ghi chú: ${debt.note}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (item.payments.isNotEmpty()) {
                    Text("Lịch sử trả", fontWeight = FontWeight.SemiBold)
                    item.payments.forEach { p ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${Formatters.money(p.amount, hideAmounts)} · ${Formatters.fullDate(p.paidAt)}",
                                    fontSize = 13.sp
                                )
                                if (p.note.isNotBlank()) {
                                    Text(p.note, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = { onDeletePayment(p) }) { Text("✕") }
                        }
                    }
                }

                if (!item.isSettled) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it.filter { c -> c.isDigit() }.take(12) },
                        label = { Text("Số tiền trả lần này (₫)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = amount > item.remaining,
                        supportingText = {
                            Text(
                                if (amount > item.remaining) "Tối đa ${Formatters.money(item.remaining)}"
                                else if (amount > 0) Formatters.money(amount) else ""
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it.take(60) },
                        label = { Text("Ghi chú (tùy chọn)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (!item.isSettled) {
                TextButton(
                    enabled = canPay,
                    onClick = {
                        onPay(debt.id, amount, note.trim())
                        onDismiss()
                    }
                ) { Text("Ghi trả") }
            } else {
                TextButton(onClick = onDismiss) { Text("Đóng") }
            }
        },
        dismissButton = {
            Row {
                TextButton(
                    onClick = {
                        if (confirmDelete) {
                            onDeleteDebt(debt)
                            onDismiss()
                        } else {
                            confirmDelete = true
                        }
                    }
                ) {
                    Text(
                        if (confirmDelete) "Chắc chắn xóa?" else "Xóa khoản",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                if (!item.isSettled) {
                    TextButton(onClick = onDismiss) { Text("Đóng") }
                }
            }
        }
    )
}
