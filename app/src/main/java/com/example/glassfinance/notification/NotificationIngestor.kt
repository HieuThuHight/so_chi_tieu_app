package com.example.glassfinance.notification

import android.content.Context
import com.example.glassfinance.data.database.AppDatabase
import com.example.glassfinance.data.preferences.SettingsStore
import com.example.glassfinance.data.repository.FinanceRepository
import com.example.glassfinance.parser.BankNotificationParsers

/** Nhận nội dung một thông báo, đọc ra giao dịch và lưu vào Room (có chống trùng). */
object NotificationIngestor {

    enum class Outcome { SAVED, DUPLICATE, BANK_DISABLED, UNRECOGNIZED, IGNORED }

    suspend fun handle(
        context: Context,
        sourcePackage: String,
        title: String,
        text: String,
        postTime: Long
    ): Outcome {
        val outcome = process(context, sourcePackage, title, text, postTime)
        // Ghi nhật ký chẩn đoán cho các tin có dáng ngân hàng (không lưu nội dung).
        if (outcome != Outcome.IGNORED) {
            NotificationActivityLog.record(context, sourcePackage, outcome.name)
        }
        return outcome
    }

    private suspend fun process(
        context: Context,
        sourcePackage: String,
        title: String,
        text: String,
        postTime: Long
    ): Outcome {
        val parsed = BankNotificationParsers.parse(title, text, postTime)
        if (parsed == null) {
            return if (UnrecognizedNotificationLog.looksLikeBankMessage(text)) {
                UnrecognizedNotificationLog.add(context, sourcePackage, title, text)
                Outcome.UNRECOGNIZED
            } else Outcome.IGNORED
        }

        val enabled = SettingsStore(context).settings.value.enabledBanks
        if (parsed.bank !in enabled) return Outcome.BANK_DISABLED

        val db = AppDatabase.getDatabase(context)
        val repository = FinanceRepository(db.transactionDao(), db.budgetDao(), db.savingsGoalDao(), db.debtDao())
        return if (repository.addFromNotification(parsed)) {
            // Báo cho người dùng biết giao dịch nào vừa được lưu (lỗi hiện thông báo không làm hỏng việc lưu).
            runCatching { TransactionSavedNotifier.notifySaved(context, parsed) }
            Outcome.SAVED
        } else Outcome.DUPLICATE
    }
}
