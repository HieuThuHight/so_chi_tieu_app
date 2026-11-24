package com.example.glassfinance.ui.settings

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.text.style.TextOverflow
import com.example.glassfinance.notification.BankNotificationListener
import com.example.glassfinance.notification.NotificationActivityLog
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material3.Slider
import android.content.ClipData
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.glassfinance.data.backup.ExportKind
import com.example.glassfinance.data.database.AppDatabase
import android.content.ClipboardManager
import android.provider.Settings
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.glassfinance.notification.UnrecognizedNotificationLog
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.preferences.AppSettings
import com.example.glassfinance.data.preferences.SupportedBanks
import com.example.glassfinance.data.preferences.ThemeMode
import com.example.glassfinance.ui.components.GlassCard
import com.example.glassfinance.ui.components.ScreenHeader
import com.example.glassfinance.ui.components.SectionTitle

@Composable
fun SettingsScreen(
    settings: AppSettings,
    transactions: List<TransactionEntity>,
    debtCount: Int,
    onThemeModeChange: (ThemeMode) -> Unit,
    onHideBalanceChange: (Boolean) -> Unit,
    onBankEnabledChange: (String, Boolean) -> Unit,
    onExport: (ExportKind, Uri) -> Unit,
    onImport: (Uri) -> Unit,
    dataMessage: String,
    onDismissMessage: () -> Unit,
    onAddSampleData: () -> Unit,
    onClearAll: () -> Unit,
    onGlassOpacityChange: (Float) -> Unit = {},
    onNavBarOpacityChange: (Float) -> Unit = {}
) {
    val context = LocalContext.current
    var confirmClear by remember { mutableStateOf(false) }

    // Chỉ còn sao lưu JSON: người dùng tự chọn nơi lưu (không cần cấp quyền bộ nhớ).
    val jsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(ExportKind.JSON.mime)
    ) { uri -> if (uri != null) onExport(ExportKind.JSON, uri) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> if (uri != null) onImport(uri) }

    fun launchExport() {
        jsonLauncher.launch(ExportKind.JSON.suggestedFileName())
    }

    val dbSize = remember(transactions, debtCount) { databaseSizeText(context) }
    val countByBank = transactions.groupingBy { it.bankName }.eachCount()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 136.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ScreenHeader("Cài đặt", "Ngân hàng, thiết bị, dữ liệu và bảo mật") }

        // ----- Giao diện -----
        item {
            SettingsGroup("Giao diện") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        ThemeMode.SYSTEM to "Hệ thống",
                        ThemeMode.LIGHT to "Sáng",
                        ThemeMode.DARK to "Tối"
                    ).forEach { (mode, label) ->
                        FilterChip(
                            selected = settings.themeMode == mode,
                            onClick = { onThemeModeChange(mode) },
                            modifier = Modifier.weight(1f),
                            label = {
                                Text(
                                    label,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 14.dp)
                ) {
                    Text("Độ trong suốt của kính", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Chạm hoặc kéo để chỉnh. Thấp = kính trong hơn, cao = thẻ đặc hơn (${(settings.glassOpacity * 100).toInt()}%)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // Giữ giá trị riêng để con trượt chạy ngay theo ngón tay (chạm hoặc kéo đều được),
                    // đồng thời áp dụng cho toàn app ngay khi đang kéo.
                    var sliderValue by remember { mutableFloatStateOf(settings.glassOpacity) }
                    LaunchedEffect(settings.glassOpacity) { sliderValue = settings.glassOpacity }
                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            sliderValue = it
                            onGlassOpacityChange(it)
                        },
                        valueRange = 0.3f..0.95f,
                        steps = 12,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Trong", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Đặc", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    GlassPreview((settings.glassOpacity * 100).toInt())

                    Spacer(Modifier.height(18.dp))
                    Text("Độ mờ thanh điều hướng", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Thanh Tổng quan / Giao dịch / Lịch / Thống kê ở dưới cùng. Thấp = trong suốt, nhìn xuyên thấy nội dung phía sau; cao = đặc hơn (${(settings.navBarOpacity * 100).toInt()}%). Nếu chữ trên thanh khó đọc thì kéo sang phải.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    var navSlider by remember { mutableFloatStateOf(settings.navBarOpacity) }
                    LaunchedEffect(settings.navBarOpacity) { navSlider = settings.navBarOpacity }
                    Slider(
                        value = navSlider,
                        onValueChange = {
                            navSlider = it
                            onNavBarOpacityChange(it)
                        },
                        valueRange = 0.0f..0.9f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Trong", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Đặc", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // ----- Đọc thông báo ngân hàng -----
        item {
            SettingsGroup("Tự động đọc thông báo ngân hàng") {
                NotificationAccessSection()
            }
        }

        // ----- Ngân hàng -----
        item {
            SettingsGroup("Ngân hàng") {
                Text(
                    "Chạm để bật / tắt ngân hàng bạn dùng. Danh sách này hiện trong form thêm giao dịch, số bên cạnh là số giao dịch.",
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BankChips(
                    banks = SupportedBanks.all,
                    enabled = settings.enabledBanks,
                    counts = countByBank,
                    onChange = onBankEnabledChange
                )
            }
        }

        // ----- Dữ liệu & sao lưu -----
        item {
            SettingsGroup("Dữ liệu & sao lưu") {
                InfoRow("Nơi lưu", "SQLite trên máy này")
                InfoRow("Giao dịch", "${transactions.size}")
                InfoRow("Vay / nợ", "$debtCount khoản")
                InfoRow("Dung lượng", dbSize)
                InfoRow("Sao lưu gần nhất", lastBackupText(settings.lastBackupAt))
                Text(
                    "Toàn bộ dữ liệu chỉ nằm trong cơ sở dữ liệu SQLite của app trên máy này, không gửi đi đâu. " +
                        "Gỡ app là mất dữ liệu, nên hãy xuất ra tệp định kỳ rồi cất ở nơi bạn muốn (Tải xuống, Drive, USB…).",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = { launchExport() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) { Text("📤 ${ExportKind.JSON.label}") }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("*/*")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
                ) { Text("📥 Nhập từ bản sao lưu JSON") }

                if (dataMessage.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            dataMessage,
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = onDismissMessage) { Text("Đóng") }
                    }
                }
            }
        }

        // ----- Bảo mật -----
        item {
            SettingsGroup("Bảo mật & riêng tư") {
                SwitchRow(
                    title = "Ẩn số dư",
                    subtitle = "Che toàn bộ số tiền ở Tổng quan, Giao dịch và Thống kê",
                    checked = settings.hideBalance,
                    onCheckedChange = onHideBalanceChange
                )
            }
        }

        // ----- Thiết bị -----
        item {
            SettingsGroup("Thiết bị") {
                InfoRow("Thiết bị", "${Build.MANUFACTURER} ${Build.MODEL}")
                InfoRow("Android", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                InfoRow("Phiên bản app", appVersion(context))
            }
        }

        // ----- Dữ liệu -----
        item {
            SettingsGroup("Dữ liệu") {
                OutlinedButton(
                    onClick = onAddSampleData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                ) { Text("✨ Thêm dữ liệu mẫu") }

                OutlinedButton(
                    onClick = { confirmClear = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) { Text("🗑️ Xóa toàn bộ dữ liệu", color = MaterialTheme.colorScheme.error) }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Xóa toàn bộ dữ liệu?") },
            text = { Text("Giao dịch, ngân sách, mục tiêu tiết kiệm và sổ vay/nợ sẽ bị xóa vĩnh viễn và không thể khôi phục.") },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    confirmClear = false
                }) { Text("Xóa hết", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Hủy") } }
        )
    }
}

/** Xem trước thẻ kính trên nền nhiều màu: kéo thanh trượt là thấy ngay thẻ trong / đặc hơn. */
@Composable
private fun GlassPreview(percent: Int) {
    Box(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .height(92.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF3DBE8B), Color(0xFFF2B35E), Color(0xFF6FA8DC)))),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(modifier = Modifier.padding(horizontal = 28.dp).fillMaxWidth()) {
            Text(
                "Thẻ kính · $percent%",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** Danh sách ngân hàng dạng chip xuống dòng: chạm để bật / tắt, kèm số giao dịch. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BankChips(
    banks: List<String>,
    enabled: Set<String>,
    counts: Map<String, Int>,
    onChange: (String, Boolean) -> Unit
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        banks.forEach { bank ->
            val on = bank in enabled
            FilterChip(
                selected = on,
                onClick = { onChange(bank, !on) },
                label = { Text("$bank · ${counts[bank] ?: 0}") }
            )
        }
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionTitle(title)
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun appVersion(context: Context): String = runCatching {
    context.packageManager.getPackageInfo(context.packageName, 0).versionName
}.getOrNull() ?: "—"

private fun lastBackupText(time: Long): String =
    if (time <= 0L) "Chưa lần nào"
    else java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        .format(java.util.Date(time))

private fun databaseSizeText(context: Context): String {
    val bytes = context.getDatabasePath(AppDatabase.DB_NAME).length()
    return if (bytes >= 1024 * 1024) String.format(java.util.Locale.US, "%.1f MB", bytes / 1048576.0)
    else String.format(java.util.Locale.US, "%.1f KB", bytes / 1024.0)
}

private fun notificationResultText(result: String): String = when {
    result == "SAVED" -> "✅ Đã lưu"
    result == "DUPLICATE" -> "↺ Trùng, bỏ qua"
    result == "BANK_DISABLED" -> "🚫 Ngân hàng đang tắt"
    result == "UNRECOGNIZED" -> "❓ Chưa nhận diện"
    result.startsWith("ERROR") -> "⚠️ " + result.removePrefix("ERROR:").trim()
    else -> result
}

private fun isListenerEnabled(context: Context): Boolean =
    NotificationManagerCompat.getEnabledListenerPackages(context).contains(context.packageName)

@Composable
private fun NotificationAccessSection() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var enabled by remember { mutableStateOf(isListenerEnabled(context)) }
    var samples by remember { mutableStateOf(UnrecognizedNotificationLog.all(context)) }
    var copied by remember { mutableStateOf(false) }
    var activity by remember { mutableStateOf(NotificationActivityLog.snapshot(context)) }
    var scanInfo by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Quét khay thông báo ngay, đợi xử lý nền xong rồi mới đọc lại nhật ký.
    fun scanNow() {
        val n = BankNotificationListener.rescanActive()
        if (n < 0) {
            // Listener chưa kết nối: xin hệ thống kết nối lại (khi kết nối sẽ tự quét khay).
            runCatching {
                NotificationListenerService.requestRebind(
                    ComponentName(context, BankNotificationListener::class.java)
                )
            }
            scanInfo = "Chưa kết nối với hệ thống, đã xin kết nối lại. Đợi vài giây rồi bấm Làm mới."
        } else {
            scanInfo = "Đã quét $n thông báo trong khay."
        }
        scope.launch {
            delay(800)
            activity = NotificationActivityLog.snapshot(context)
        }
    }

    // Quay lại từ màn hình cấp quyền của hệ thống → cập nhật trạng thái.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = isListenerEnabled(context)
                samples = UnrecognizedNotificationLog.all(context)
                activity = NotificationActivityLog.snapshot(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    InfoRow("Trạng thái", if (enabled) "✅ Đang bật" else "⛔ Chưa bật")

    // ----- Chẩn đoán (không đếm thông báo, không lưu nội dung) -----
    InfoRow(
        "Kết nối gần nhất",
        if (activity.connectedAt > 0L) lastBackupText(activity.connectedAt) else "Chưa kết nối"
    )
    if (enabled && activity.connectedAt == 0L) {
        Text(
            "Quyền đã bật nhưng hệ thống chưa kết nối app. Hãy đặt pin GlassFinance ở \"Không hạn chế\", " +
                "bật tự khởi chạy, khóa app trong màn hình đa nhiệm, rồi bấm \"Quét lại\" bên dưới.",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.error
        )
    }
    // Chỉ hiện vài tin gần nhất cho gọn; bấm "Xem thêm" để mở hết.
    var showAllEntries by remember { mutableStateOf(false) }
    val shownEntries = if (showAllEntries) activity.entries else activity.entries.take(3)
    shownEntries.forEach { e ->
        InfoRow(
            lastBackupText(e.time).takeLast(5) + " · " + e.pkg.substringAfterLast('.'),
            notificationResultText(e.result)
        )
    }
    if (activity.entries.size > 3) {
        TextButton(
            onClick = { showAllEntries = !showAllEntries },
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(if (showAllEntries) "Thu gọn" else "Xem thêm (${activity.entries.size - 3})")
        }
    }
    Row(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = { scanNow() },
            enabled = enabled
        ) { Text("🔄 Quét lại") }
        TextButton(onClick = { scanNow() }) { Text("Làm mới") }
    }
    scanInfo?.let {
        Text(
            it,
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Text(
        "Khi ngân hàng báo biến động số dư, app tự ghi giao dịch (hiện hỗ trợ: VCB, MB Bank, Techcombank). " +
                "Chỉ các ngân hàng đang bật ở mục trên mới được ghi. Nếu sau đó có email/ảnh chụp của cùng giao dịch " +
                "thì app tự gộp, không bị đôi.",
        modifier = Modifier.padding(horizontal = 16.dp),
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    OutlinedButton(
        onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) { Text(if (enabled) "Mở cài đặt quyền thông báo" else "🔔 Cấp quyền đọc thông báo") }

    if (!enabled) {
        Text(
            "Nếu máy báo \"Cài đặt bị hạn chế\" (Android 13+, app cài từ file APK): Cài đặt → Ứng dụng → GlassFinance → " +
                    "⋮ → Cho phép cài đặt bị hạn chế, rồi quay lại bật quyền.",
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Text(
        "Máy Xiaomi / Oppo / Vivo / Samsung: hãy đặt GlassFinance ở chế độ pin \"Không hạn chế\" và bật tự khởi chạy, " +
                "nếu không hệ thống có thể tắt nền làm app lỡ thông báo.",
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    if (samples.isNotEmpty()) {
        InfoRow("Mẫu chưa nhận diện", "${samples.size} tin")
        Text(
            "Các thông báo giống tin biến động số dư nhưng app chưa đọc được. Sao chép rồi gửi (nhớ che số tài khoản) để thêm hỗ trợ ngân hàng đó.",
            modifier = Modifier.padding(horizontal = 16.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(onClick = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("notification-samples", samples.joinToString("\n\n")))
                copied = true
            }) { Text(if (copied) "Đã sao chép ✓" else "📋 Sao chép") }
            TextButton(onClick = {
                UnrecognizedNotificationLog.clear(context)
                samples = emptyList()
                copied = false
            }) { Text("Xóa mẫu") }
        }
    }
}
