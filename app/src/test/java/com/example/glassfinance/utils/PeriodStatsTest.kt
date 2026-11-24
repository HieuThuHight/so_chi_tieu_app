package com.example.glassfinance.utils

import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.TransactionSource
import com.example.glassfinance.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class PeriodStatsTest {

    private fun tx(date: String, type: String, amount: Long, category: String = "Khác") = TransactionEntity(
        bankName = "VCB", type = type, category = category, amount = amount, content = "",
        transactionTime = LocalDate.parse(date).atTime(12, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        source = TransactionSource.MANUAL
    )

    private val list = listOf(
        tx("2026-11-02", TransactionType.EXPENSE, 100_000, "Ăn uống"),
        tx("2026-11-19", TransactionType.EXPENSE, 300_000, "Di chuyển"),
        tx("2026-11-05", TransactionType.INCOME, 10_000_000, "Lương"),
        tx("2026-10-10", TransactionType.EXPENSE, 200_000, "Ăn uống")
    )

    @Test fun baoCaoThang() {
        val p = Period.of(PeriodType.MONTH, LocalDate.of(2026, 11, 19))
        val r = PeriodStats.report(list, p, LocalDate.of(2026, 11, 19))
        assertEquals(10_000_000L, r.income)
        assertEquals(400_000L, r.expense)
        assertEquals(400_000L / 19, r.avgDailyExpense)
        assertEquals(100, r.expenseChangePct)
        assertEquals(30, r.bars.size)
    }

    @Test fun tuanBatDauThuHai() {
        val w = Period.of(PeriodType.WEEK, LocalDate.of(2026, 11, 19))
        assertEquals(LocalDate.of(2026, 11, 16), w.start)
        assertEquals(LocalDate.of(2026, 11, 22), w.end)
    }

    @Test fun quyVaNam() {
        val q = Period.of(PeriodType.QUARTER, LocalDate.of(2026, 11, 19))
        assertEquals(LocalDate.of(2026, 10, 1), q.start)
        assertEquals(LocalDate.of(2026, 12, 31), q.end)
        assertEquals(12, PeriodStats.report(list, Period.of(PeriodType.YEAR, LocalDate.of(2026, 1, 1))).bars.size)
    }
}
