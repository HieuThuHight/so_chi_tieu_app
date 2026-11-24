package com.example.glassfinance.notification

import android.app.Notification
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Chạy nền khi người dùng bật "Quyền truy cập thông báo" cho GlassFinance.
 * Mỗi thông báo được chuyển cho [NotificationIngestor]; thông báo không phải của ngân hàng bị bỏ qua.
 *
 * Lưu ý: hệ thống chỉ báo cho listener những thông báo ĐẾN SAU khi nó kết nối. Vì vậy khi vừa kết nối
 * ([onListenerConnected]) ta đọc luôn các thông báo đang nằm trong khay; cùng giao dịch bị đọc lại
 * thì được chống trùng bằng fingerprint nên không bị đôi.
 */
class BankNotificationListener : NotificationListenerService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onListenerConnected() {
        super.onListenerConnected()
        instance = this
        NotificationActivityLog.markConnected(applicationContext)
        scanActive()
    }

    /** Đọc lại mọi thông báo đang nằm trong khay. Trả về số thông báo đã đưa đi xử lý. */
    private fun scanActive(): Int {
        val active = runCatching { activeNotifications }.getOrNull().orEmpty()
        active.forEach { process(it) }
        return active.size
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn != null) process(sbn)
    }

    private fun process(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        val extras = sbn.notification?.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        // BIG_TEXT đầy đủ hơn TEXT (TEXT bị cắt khi thông báo dài như tin VCB / MB).
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        if (text.isBlank()) return

        val pkg = sbn.packageName
        val postTime = sbn.postTime
        scope.launch {
            runCatching {
                NotificationIngestor.handle(applicationContext, pkg, title, text, postTime)
            }.onFailure {
                Log.e(TAG, "Không xử lý được thông báo từ $pkg", it)
                NotificationActivityLog.record(
                    applicationContext, pkg, "ERROR:${it.javaClass.simpleName}: ${it.message ?: ""}"
                )
            }
        }
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        if (instance === this) instance = null
        // Hệ thống thỉnh thoảng ngắt listener (tiết kiệm pin) → xin kết nối lại.
        runCatching { NotificationListenerService.requestRebind(ComponentName(this, BankNotificationListener::class.java)) }
    }

    override fun onDestroy() {
        if (instance === this) instance = null
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "BankNotifListener"

        @Volatile
        private var instance: BankNotificationListener? = null

        /**
         * Quét lại khay thông báo ngay (dùng cho nút "Quét lại" / "Làm mới").
         * Trả về số thông báo đã quét, hoặc -1 nếu listener đang chưa kết nối.
         * Lưu ý: requestRebind() KHÔNG làm gì khi listener đã kết nối, nên không thể dựa vào nó để quét lại.
         */
        fun rescanActive(): Int = instance?.scanActive() ?: -1
    }
}
