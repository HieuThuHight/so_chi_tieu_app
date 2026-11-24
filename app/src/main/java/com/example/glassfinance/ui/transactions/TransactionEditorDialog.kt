package com.example.glassfinance.ui.transactions

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassfinance.data.model.Categories
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.TransactionSource
import com.example.glassfinance.data.model.TransactionType
import com.example.glassfinance.data.model.isIncome
import com.example.glassfinance.data.preferences.SupportedBanks
import com.example.glassfinance.ui.theme.FinanceTheme
import com.example.glassfinance.utils.Formatters
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.launch

/**
 * Bảng trượt từ dưới lên để thêm / sửa giao dịch.
 * [initial] = null → thêm mới. Có [initial] → sửa (chỉ đổi danh mục), kèm nút Xóa nếu truyền [onDelete].
 * (Giữ tên cũ để các nơi gọi không phải đổi.)
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionEditorDialog(
    initial: TransactionEntity?,
    banks: List<String>,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onDelete: ((TransactionEntity) -> Unit)? = null
) {
    var isIncome by remember { mutableStateOf(initial?.isIncome ?: false) }
    var amountText by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(initial?.category ?: Categories.expense.first().name) }
    // Bấm "Khác" → phải tự gõ tên danh mục; tên đã gõ nằm trong customText.
    var pickedOther by remember { mutableStateOf(false) }
    var customText by remember { mutableStateOf("") }
    var bank by remember { mutableStateOf(initial?.bankName ?: banks.firstOrNull() ?: SupportedBanks.CASH) }
    var timeMillis by remember { mutableLongStateOf(initial?.transactionTime ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val amount = amountText.toLongOrNull() ?: 0L
    val finalCategory = if (pickedOther) customText.trim() else category
    val categoryValid = !pickedOther || customText.isNotBlank()

    val categoryOptions = remember(isIncome, initial) {
        val base = Categories.forType(isIncome).map { it.name }
        val legacy = initial?.takeIf { it.isIncome == isIncome }?.category
        if (legacy != null && legacy !in base) base + legacy else base
    }
    val bankOptions = remember(banks, initial) {
        (banks + SupportedBanks.CASH + listOfNotNull(initial?.bankName)).distinct()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    // Chạy [action] rồi đóng bảng có hiệu ứng trượt xuống.
    fun closeAfter(action: () -> Unit = {}) {
        action()
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (initial != null) {
                // ---------- SỬA: chỉ đổi danh mục, còn lại lấy từ thông báo ngân hàng ----------
                val tint = if (initial.isIncome) FinanceTheme.colors.income else FinanceTheme.colors.expense

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(tint.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(Categories.emojiOf(category), fontSize = 28.sp)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            "Sửa giao dịch",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            Formatters.signedMoney(initial.amount, initial.isIncome),
                            style = MaterialTheme.typography.titleLarge,
                            color = tint
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        InfoRow("Ngân hàng", initial.bankName)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        InfoRow(
                            "Thời gian",
                            "${Formatters.fullDate(initial.transactionTime)} · ${Formatters.time(initial.transactionTime)}"
                        )
                        if (initial.content.isNotBlank()) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                            InfoRow("Nội dung", initial.content, maxLines = 2)
                        }
                    }
                }

                SectionLabel("Danh mục")
                CategoryGrid(categoryOptions, if (pickedOther) Categories.OTHER else category) { picked ->
                    if (picked == Categories.OTHER) {
                        pickedOther = true
                    } else {
                        pickedOther = false
                        category = picked
                    }
                }
                if (pickedOther) {
                    CustomCategoryField(customText) { customText = it }
                }

                Text(
                    "Số tiền, thời gian và nội dung lấy từ thông báo ngân hàng nên không sửa ở đây.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    enabled = categoryValid,
                    onClick = {
                        closeAfter {
                            onSave(initial.copy(category = finalCategory))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Lưu thay đổi", fontWeight = FontWeight.SemiBold) }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (onDelete != null) {
                        TextButton(onClick = { confirmDelete = true }) {
                            Text("Xóa giao dịch", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    TextButton(onClick = { closeAfter() }) { Text("Hủy") }
                }
            } else {
                // ---------- THÊM MỚI ----------
                Text("Thêm giao dịch", style = MaterialTheme.typography.titleLarge)

                TypeToggle(
                    isIncome = isIncome,
                    onChange = { income ->
                        isIncome = income
                        pickedOther = false
                        val names = Categories.forType(income).map { it.name }
                        if (category !in names) category = names.first()
                    }
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { c -> c.isDigit() }.take(12) },
                    label = { Text("Số tiền") },
                    suffix = { Text("₫") },
                    textStyle = MaterialTheme.typography.titleLarge,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { if (amount > 0) Text(Formatters.money(amount)) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it.take(80) },
                    label = { Text("Nội dung") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                SectionLabel("Danh mục")
                CategoryGrid(categoryOptions, if (pickedOther) Categories.OTHER else category) { picked ->
                    if (picked == Categories.OTHER) {
                        pickedOther = true
                    } else {
                        pickedOther = false
                        category = picked
                    }
                }
                if (pickedOther) {
                    CustomCategoryField(customText) { customText = it }
                }

                SectionLabel("Ngân hàng / nguồn tiền")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bankOptions.forEach { name ->
                        FilterChip(
                            selected = bank == name,
                            onClick = { bank = name },
                            label = { Text(name) }
                        )
                    }
                }

                SectionLabel("Ngày giao dịch")
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    shape = RoundedCornerShape(14.dp)
                ) { Text("📅  ${Formatters.fullDate(timeMillis)}") }

                Button(
                    enabled = amount > 0 && categoryValid,
                    onClick = {
                        closeAfter {
                            onSave(
                                TransactionEntity(
                                    bankName = bank,
                                    type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                                    category = finalCategory,
                                    amount = amount,
                                    content = content.trim().ifBlank { finalCategory },
                                    transactionTime = timeMillis,
                                    source = TransactionSource.MANUAL
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) { Text("Lưu giao dịch", fontWeight = FontWeight.SemiBold) }
            }
        }
    }

    if (confirmDelete && initial != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Xóa giao dịch?") },
            text = { Text("Giao dịch \"${initial.content.ifBlank { initial.category }}\" sẽ bị xóa vĩnh viễn.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    closeAfter { onDelete(initial) }
                }) { Text("Xóa", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Giữ lại") }
            }
        )
    }

    if (showDatePicker) {
        val zone = ZoneId.systemDefault()
        val current = Instant.ofEpochMilli(timeMillis).atZone(zone)
        // DatePicker làm việc theo UTC → đổi qua lại cho đúng ngày địa phương.
        val todayUtc = LocalDate.now(zone).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = current.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= todayUtc
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { utc ->
                        val picked = Instant.ofEpochMilli(utc).atZone(ZoneOffset.UTC).toLocalDate()
                        timeMillis = picked.atTime(current.toLocalTime()).atZone(zone).toInstant().toEpochMilli()
                    }
                    showDatePicker = false
                }) { Text("Chọn") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** Ô gõ tên danh mục riêng, hiện ra khi người dùng bấm chip "Khác". */
@Composable
private fun CustomCategoryField(value: String, onChange: (String) -> Unit) {
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { focus.requestFocus() }

    OutlinedTextField(
        value = value,
        onValueChange = { onChange(it.take(30)) },
        label = { Text("Tên danh mục") },
        placeholder = { Text("Ví dụ: Cắt tóc, Đổ xăng…") },
        singleLine = true,
        supportingText = { if (value.isBlank()) Text("Nhập tên danh mục để lưu") },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focus)
    )
}

/** Danh mục xếp thành lưới tự xuống dòng – thấy hết một lượt, không phải vuốt ngang. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryGrid(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { name ->
            FilterChip(
                selected = selected == name,
                onClick = { onSelect(name) },
                label = { Text("${Categories.emojiOf(name)} $name") }
            )
        }
    }
}

/** Công tắc Chi tiêu / Thu nhập: hai ô lớn, ô đang chọn đổi sang màu thu hoặc chi. */
@Composable
private fun TypeToggle(isIncome: Boolean, onChange: (Boolean) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TypeOption("Chi tiêu", !isIncome, FinanceTheme.colors.expense, Modifier.weight(1f)) { onChange(false) }
        TypeOption("Thu nhập", isIncome, FinanceTheme.colors.income, Modifier.weight(1f)) { onChange(true) }
    }
}

@Composable
private fun TypeOption(
    label: String,
    selected: Boolean,
    tint: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) tint.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceContainer
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(vertical = 14.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            color = if (selected) tint else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String, maxLines: Int = 1) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(16.dp))
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}
