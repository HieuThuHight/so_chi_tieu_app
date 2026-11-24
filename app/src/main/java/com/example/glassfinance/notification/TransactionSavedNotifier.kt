package com.example.glassfinance.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.glassfinance.MainActivity
import com.example.glassfinance.R
import com.example.glassfinance.data.preferences.SettingsStore
import com.example.glassfinance.parser.CategoryGuesser
import com.example.glassfinance.parser.ParsedTransaction
import com.example.glassfinance.utils.Formatters

/**
 * Hiện thông báo "Đã lưu giao dịch" mỗi khi app vừa ghi một giao dịch từ thông báo ngân hàng,
 * kèm số tiền, ngân hàng, danh mục và nội dung để biết đó là giao dịch nào.
 *
 * Thông báo này do chính GlassFinance đăng, và listener bỏ qua thông báo của chính app
 * (xem BankNotificationListener.process) nên không bị đọc lại thành giao dịch.
 */
object TransactionSavedNotifier {

    private const val CHANNEL_ID = "transaction_saved"

    @SuppressLint("MissingPermission") // đã kiểm tra bằng areNotificationsEnabled()
    fun notifySaved(context: Context, p: ParsedTransaction) {
        val app = context.applicationContext
        val manager = NotificationManagerCompat.from(app)
        // Android 13+: false nếu người dùng chưa cấp quyền POST_NOTIFICATIONS.
        if (!manager.areNotificationsEnabled()) return
        ensureChannel(app)

        // Người dùng đang ẩn số tiền trong app thì thông báo cũng không lộ số tiền.
        val hide = SettingsStore(app).settings.value.hideBalance
        val money = if (hide) Formatters.HIDDEN else Formatters.signedMoney(p.amount, p.isIncome)
        val category = CategoryGuesser.guess(p.isIncome, p.content)
        val content = p.content.trim()

        val oneLine = buildString {
            append(money).append(" · ").append(p.bank)
            if (content.isNotEmpty()) append(" · ").append(content)
        }
        val detail = buildString {
            append(money).append(" · ").append(p.bank)
            append("\nDanh mục: ").append(category)
            if (content.isNotEmpty()) append("\n").append(content)
        }

        val open = PendingIntent.getActivity(
            app, 0,
            Intent(app, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(app, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_saved)
            .setContentTitle("Đã lưu giao dịch ✅")
            .setContentText(oneLine)
            .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
            .setContentIntent(open)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Mỗi giao dịch một id riêng để các thông báo không đè lên nhau.
        try {
            manager.notify(p.fingerprint.hashCode(), notification)
        } catch (_: SecurityException) {
            // Chưa có quyền: bỏ qua, giao dịch vẫn đã được lưu.
        }
    }

    private fun ensureChannel(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Giao dịch đã lưu", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Báo khi GlassFinance vừa tự ghi một giao dịch từ thông báo ngân hàng"
            }
        )
    }
}
