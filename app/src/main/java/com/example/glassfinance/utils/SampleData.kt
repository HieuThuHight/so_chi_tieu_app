package com.example.glassfinance.utils

import com.example.glassfinance.data.model.BudgetEntity
import com.example.glassfinance.data.model.SavingsGoalEntity
import com.example.glassfinance.data.model.SyncStatus
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.TransactionSource
import com.example.glassfinance.data.model.TransactionType

/** Dữ liệu mẫu để xem thử giao diện (nút trong Cài đặt → Dữ liệu). */
object SampleData {

    private const val DAY = 24L * 60 * 60 * 1000

    fun transactions(now: Long = System.currentTimeMillis()): List<TransactionEntity> {
        fun tx(
            daysAgo: Int,
            minute: Int,
            bank: String,
            income: Boolean,
            category: String,
            amount: Long,
            content: String
        ) = TransactionEntity(
            bankName = bank,
            type = if (income) TransactionType.INCOME else TransactionType.EXPENSE,
            category = category,
            amount = amount,
            content = content,
            transactionTime = now - daysAgo * DAY - minute * 60_000L,
            source = TransactionSource.MANUAL,
            syncStatus = SyncStatus.PENDING
        )

        return listOf(
            tx(0, 5, "VCB", false, "Ăn uống", 45_000, "Ăn trưa"),
            tx(0, 240, "MB Bank", true, "Lương", 8_000_000, "Lương tháng"),
            tx(1, 30, "VCB", false, "Học tập", 120_000, "Mua sách"),
            tx(2, 90, "MB Bank", true, "Làm thêm", 500_000, "Tiền làm thêm"),
            tx(3, 15, "VCB", false, "Hóa đơn", 250_000, "Tiền điện"),
            tx(4, 60, "VCB", false, "Mua sắm", 180_000, "Mua đồ cá nhân"),
            tx(5, 45, "MB Bank", false, "Di chuyển", 60_000, "Đổ xăng"),
            tx(6, 20, "VCB", false, "Ăn uống", 85_000, "Cà phê và bánh"),
            tx(8, 10, "MB Bank", false, "Giải trí", 150_000, "Xem phim"),
            tx(10, 50, "VCB", false, "Sức khỏe", 220_000, "Thuốc cảm"),
            tx(12, 25, "VCB", false, "Ăn uống", 320_000, "Đi ăn cùng bạn"),
            tx(15, 40, "MB Bank", false, "Hóa đơn", 180_000, "Internet"),
            tx(20, 70, "MB Bank", true, "Thưởng", 1_000_000, "Thưởng dự án"),
            // Các tháng trước để biểu đồ có dữ liệu
            tx(35, 10, "MB Bank", true, "Lương", 7_800_000, "Lương tháng trước"),
            tx(37, 20, "VCB", false, "Ăn uống", 1_250_000, "Ăn uống cả tháng"),
            tx(41, 15, "VCB", false, "Mua sắm", 900_000, "Quần áo"),
            tx(46, 30, "VCB", false, "Hóa đơn", 430_000, "Điện nước"),
            tx(65, 10, "MB Bank", true, "Lương", 7_500_000, "Lương"),
            tx(68, 40, "VCB", false, "Ăn uống", 1_450_000, "Ăn uống"),
            tx(72, 20, "VCB", false, "Giải trí", 600_000, "Du lịch ngắn ngày"),
            tx(96, 10, "MB Bank", true, "Lương", 7_500_000, "Lương"),
            tx(99, 35, "VCB", false, "Ăn uống", 1_100_000, "Ăn uống"),
            tx(103, 55, "VCB", false, "Học tập", 800_000, "Khóa học online"),
            tx(128, 10, "MB Bank", true, "Lương", 7_200_000, "Lương"),
            tx(132, 30, "VCB", false, "Mua sắm", 1_300_000, "Mua sắm")
        )
    }

    fun budgets(): List<BudgetEntity> = listOf(
        BudgetEntity("Ăn uống", 1_500_000),
        BudgetEntity("Mua sắm", 800_000),
        BudgetEntity("Giải trí", 400_000)
    )

    fun savingsGoal(): SavingsGoalEntity = SavingsGoalEntity(
        name = "Mua laptop mới 💻",
        targetAmount = 20_000_000,
        savedAmount = 6_500_000
    )
}
