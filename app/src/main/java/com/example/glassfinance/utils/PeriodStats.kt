package com.example.glassfinance.utils

import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.isIncome
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

enum class PeriodType(val label: String) {
    WEEK("Tuần"), MONTH("Tháng"), QUARTER("Quý"), YEAR("Năm")
}

/** Khoảng thời gian [start, end] (tính cả hai đầu), tuần bắt đầu từ thứ Hai. */
data class Period(val type: PeriodType, val start: LocalDate, val end: LocalDate) {

    val days: Int get() = (ChronoUnit.DAYS.between(start, end) + 1).toInt()

    fun contains(date: LocalDate) = !date.isBefore(start) && !date.isAfter(end)

    fun shift(n: Int): Period = of(
        type,
        when (type) {
            PeriodType.WEEK -> start.plusWeeks(n.toLong())
            PeriodType.MONTH -> start.plusMonths(n.toLong())
            PeriodType.QUARTER -> start.plusMonths(3L * n)
            PeriodType.YEAR -> start.plusYears(n.toLong())
        }
    )

    fun label(): String = when (type) {
        PeriodType.WEEK -> "${start.dayOfMonth}/${start.monthValue} – ${end.dayOfMonth}/${end.monthValue}/${end.year}"
        PeriodType.MONTH -> "Tháng ${start.monthValue}/${start.year}"
        PeriodType.QUARTER -> "Quý ${(start.monthValue - 1) / 3 + 1}/${start.year}"
        PeriodType.YEAR -> "Năm ${start.year}"
    }

    companion object {
        fun of(type: PeriodType, date: LocalDate): Period = when (type) {
            PeriodType.WEEK -> {
                val s = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                Period(type, s, s.plusDays(6))
            }
            PeriodType.MONTH -> {
                val ym = YearMonth.from(date)
                Period(type, ym.atDay(1), ym.atEndOfMonth())
            }
            PeriodType.QUARTER -> {
                val firstMonth = (date.monthValue - 1) / 3 * 3 + 1
                val s = LocalDate.of(date.year, firstMonth, 1)
                Period(type, s, s.plusMonths(2).let { YearMonth.from(it).atEndOfMonth() })
            }
            PeriodType.YEAR -> Period(type, LocalDate.of(date.year, 1, 1), LocalDate.of(date.year, 12, 31))
        }
    }
}

/** Một cột biểu đồ: một ngày (tuần/tháng) hoặc một tháng (quý/năm). */
data class BarPoint(val label: String, val income: Long, val expense: Long)

data class PeriodReport(
    val period: Period,
    val income: Long,
    val expense: Long,
    val count: Int,
    val avgDailyExpense: Long,
    val savingRate: Int?,
    val previousExpense: Long,
    val expenseChangePct: Int?,
    val topCategories: List<CategorySpend>,
    val biggestExpense: TransactionEntity?,
    val bars: List<BarPoint>
) {
    val net: Long get() = income - expense
}

data class DayTotals(val income: Long, val expense: Long) {
    val net: Long get() = income - expense
}

object PeriodStats {

    private fun inPeriod(list: List<TransactionEntity>, p: Period) =
        list.filter { p.contains(it.localDate()) }

    fun report(all: List<TransactionEntity>, period: Period, today: LocalDate = LocalDate.now()): PeriodReport {
        val txs = inPeriod(all, period)
        val income = txs.filter { it.isIncome }.sumOf { it.amount }
        val expense = txs.filter { !it.isIncome }.sumOf { it.amount }

        // Kỳ đang diễn ra thì chia theo số ngày đã qua, kỳ đã xong thì chia theo cả kỳ.
        val daysCounted = when {
            period.contains(today) -> (ChronoUnit.DAYS.between(period.start, today) + 1).toInt()
            today.isBefore(period.start) -> 0
            else -> period.days
        }
        val avg = if (daysCounted > 0) expense / daysCounted else 0L

        val prevExpense = inPeriod(all, period.shift(-1)).filter { !it.isIncome }.sumOf { it.amount }
        val change = if (prevExpense > 0) (((expense - prevExpense) * 100) / prevExpense).toInt() else null
        val saving = if (income > 0) (((income - expense) * 100) / income).toInt() else null

        val top = txs.filter { !it.isIncome }
            .groupBy { it.category }
            .map { (c, l) -> CategorySpend(c, l.sumOf { it.amount }) }
            .sortedByDescending { it.amount }

        return PeriodReport(
            period = period,
            income = income,
            expense = expense,
            count = txs.size,
            avgDailyExpense = avg,
            savingRate = saving,
            previousExpense = prevExpense,
            expenseChangePct = change,
            topCategories = top,
            biggestExpense = txs.filter { !it.isIncome }.maxByOrNull { it.amount },
            bars = bars(txs, period)
        )
    }

    private fun bars(txs: List<TransactionEntity>, period: Period): List<BarPoint> =
        when (period.type) {
            PeriodType.WEEK, PeriodType.MONTH -> {
                val byDay = txs.groupBy { it.localDate() }
                (0 until period.days).map { i ->
                    val d = period.start.plusDays(i.toLong())
                    val l = byDay[d].orEmpty()
                    BarPoint(
                        label = d.dayOfMonth.toString(),
                        income = l.filter { it.isIncome }.sumOf { it.amount },
                        expense = l.filter { !it.isIncome }.sumOf { it.amount }
                    )
                }
            }
            PeriodType.QUARTER, PeriodType.YEAR -> {
                val byMonth = txs.groupBy { it.yearMonth() }
                val months = if (period.type == PeriodType.QUARTER) 3 else 12
                (0 until months).map { i ->
                    val ym = YearMonth.from(period.start).plusMonths(i.toLong())
                    val l = byMonth[ym].orEmpty()
                    BarPoint(
                        label = "T${ym.monthValue}",
                        income = l.filter { it.isIncome }.sumOf { it.amount },
                        expense = l.filter { !it.isIncome }.sumOf { it.amount }
                    )
                }
            }
        }

    /** Tổng thu/chi theo từng ngày của [month], dùng để tô lịch. */
    fun dayTotals(all: List<TransactionEntity>, month: YearMonth): Map<LocalDate, DayTotals> =
        all.filter { it.yearMonth() == month }
            .groupBy { it.localDate() }
            .mapValues { (_, l) ->
                DayTotals(
                    income = l.filter { it.isIncome }.sumOf { it.amount },
                    expense = l.filter { !it.isIncome }.sumOf { it.amount }
                )
            }

    fun transactionsOn(all: List<TransactionEntity>, day: LocalDate): List<TransactionEntity> =
        all.filter { it.localDate() == day }.sortedByDescending { it.transactionTime }
}
