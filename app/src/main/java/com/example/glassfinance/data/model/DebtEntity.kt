package com.example.glassfinance.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Khoản cho vay / đi vay. */
@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    /** true = bạn đi vay (bạn nợ họ), false = bạn cho vay (họ nợ bạn). */
    val isBorrowed: Boolean,

    val name: String,

    /** Số tiền gốc (VND). */
    val principal: Long,

    /** Hạn trả (milliseconds, ngày địa phương). 0 = không có hạn. */
    val dueDate: Long = 0L,

    val note: String = "",

    val createdAt: Long = System.currentTimeMillis()
)

/** Một lần trả nợ của [DebtEntity]. */
@Entity(
    tableName = "debt_payments",
    indices = [Index(value = ["debtId"])]
)
data class DebtPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val debtId: Long,
    val amount: Long,
    val note: String = "",
    val paidAt: Long = System.currentTimeMillis()
)

/** Khoản nợ kèm lịch sử trả, đã tính sẵn số còn lại. */
data class DebtWithPayments(
    val debt: DebtEntity,
    val payments: List<DebtPaymentEntity>
) {
    val paid: Long get() = payments.sumOf { it.amount }
    val remaining: Long get() = (debt.principal - paid).coerceAtLeast(0L)
    val isSettled: Boolean get() = remaining == 0L
}
