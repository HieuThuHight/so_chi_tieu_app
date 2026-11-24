package com.example.glassfinance.utils

import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.isIncome
import java.time.LocalDate
import java.time.YearMonth

data class MonthTotals(
    val month: YearMonth,
    val income: Long,
    val expense: Long
) {
    val net: Long get() = income - expense
}

data class CategorySpend(
    val category: String,
    val amount: Long
)

data class MonthReport(
    val totals: MonthTotals,
    val previous: MonthTotals,
    val avgDailyExpense: Long,
    /** % tiết kiệm = (thu - chi) / thu, null nếu chưa có thu nhập */
    val savingRate: Int?,
    /** % thay đổi chi tiêu so với tháng trước, null nếu tháng trước không có chi */
    val expenseChangePct: Int?,
    val topCategory: CategorySpend?,
    val biggestExpense: TransactionEntity?
)

fun TransactionEntity.localDate(): LocalDate = Formatters.toLocalDate(transactionTime)

fun TransactionEntity.yearMonth(): YearMonth = YearMonth.from(localDate())

object StatsCalculator {

    fun totalsFor(list: List<TransactionEntity>, month: YearMonth): MonthTotals {
        var income = 0L
        var expense = 0L
        list.forEach { tx ->
            if (tx.yearMonth() == month) {
                if (tx.isIncome) income += tx.amount else expense += tx.amount
            }
        }
        return MonthTotals(month, income, expense)
    }

    /** [count] tháng kết thúc tại [end], tháng cũ nhất đứng trước. */
    fun lastMonths(list: List<TransactionEntity>, end: YearMonth, count: Int = 6): List<MonthTotals> {
        val grouped = list.groupBy { it.yearMonth() }
        return (count - 1 downTo 0).map { offset ->
            val month = end.minusMonths(offset.toLong())
            val txs = grouped[month].orEmpty()
            MonthTotals(
                month = month,
                income = txs.filter { it.isIncome }.sumOf { it.amount },
                expense = txs.filter { !it.isIncome }.sumOf { it.amount }
            )
        }
    }

    fun expenseByCategory(list: List<TransactionEntity>, month: YearMonth): List<CategorySpend> =
        list.filter { !it.isIncome && it.yearMonth() == month }
            .groupBy { it.category }
            .map { (category, txs) -> CategorySpend(category, txs.sumOf { it.amount }) }
            .sortedByDescending { it.amount }

    fun report(list: List<TransactionEntity>, month: YearMonth): MonthReport {
        val totals = totalsFor(list, month)
        val previous = totalsFor(list, month.minusMonths(1))
        val byCategory = expenseByCategory(list, month)

        val daysCounted = if (month == YearMonth.now()) LocalDate.now().dayOfMonth else month.lengthOfMonth()
        val avgDaily = if (daysCounted > 0) totals.expense / daysCounted else 0L

        val savingRate = if (totals.income > 0) (((totals.income - totals.expense) * 100) / totals.income).toInt() else null
        val change = if (previous.expense > 0) (((totals.expense - previous.expense) * 100) / previous.expense).toInt() else null

        val biggest = list
            .filter { !it.isIncome && it.yearMonth() == month }
            .maxByOrNull { it.amount }

        return MonthReport(
            totals = totals,
            previous = previous,
            avgDailyExpense = avgDaily,
            savingRate = savingRate,
            expenseChangePct = change,
            topCategory = byCategory.firstOrNull(),
            biggestExpense = biggest
        )
    }
}
