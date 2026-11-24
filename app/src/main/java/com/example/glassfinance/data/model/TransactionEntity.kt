package com.example.glassfinance.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object TransactionType {
    const val INCOME = "INCOME"
    const val EXPENSE = "EXPENSE"
}

object TransactionSource {
    const val NOTIFICATION = "NOTIFICATION"
    const val GMAIL = "GMAIL"
    const val SHORTCUT = "SHORTCUT"
    const val MANUAL = "MANUAL"
}

/** Còn giữ để không đổi cấu trúc bảng; app không còn đồng bộ với dịch vụ bên ngoài nên không dùng nữa. */
object SyncStatus {
    const val PENDING = "PENDING"
    const val SYNCED = "SYNCED"
}

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["transactionTime"]),
        // Chống trùng giao dịch khi sau này tự nhận diện từ thông báo/Gmail.
        // Với fingerprint = null thì SQLite vẫn cho phép nhiều dòng.
        Index(value = ["fingerprint"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val bankName: String,

    // TransactionType.INCOME hoặc TransactionType.EXPENSE
    val type: String,

    val category: String,

    // Số tiền tính bằng VND (luôn dương, dấu thu/chi nằm ở `type`)
    val amount: Long,

    val content: String,

    // Thời gian giao dịch (milliseconds)
    val transactionTime: Long,

    // TransactionSource.*
    val source: String,

    // Chỉ còn để tương thích dữ liệu cũ (mã giao dịch từ nguồn ngoài). Không dùng nữa.
    val externalId: String? = null,

    val fingerprint: String? = null,

    // Không dùng nữa (trước đây dành cho đồng bộ Google Sheet).
    val syncStatus: String = SyncStatus.PENDING,

    val createdAt: Long = System.currentTimeMillis()
)

val TransactionEntity.isIncome: Boolean
    get() = type == TransactionType.INCOME
