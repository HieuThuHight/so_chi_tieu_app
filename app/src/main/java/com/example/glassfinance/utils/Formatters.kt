package com.example.glassfinance.utils

import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs

object Formatters {

    const val HIDDEN = "••••••"

    private val vi: Locale = Locale.forLanguageTag("vi-VN")
    private val dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")

    /** 25680000 -> "25.680.000 ₫" */
    fun money(amount: Long): String = "${NumberFormat.getInstance(vi).format(amount)} ₫"

    fun money(amount: Long, hidden: Boolean): String = if (hidden) HIDDEN else money(amount)

    fun signedMoney(amount: Long, isIncome: Boolean): String =
        (if (isIncome) "+" else "-") + money(amount)

    /** 8500000 -> "8,5 triệu" */
    fun compact(amount: Long): String {
        val sign = if (amount < 0) "-" else ""
        val a = abs(amount).toDouble()
        return when {
            a >= 1_000_000_000 -> "$sign${oneDecimal(a / 1_000_000_000)} tỷ"
            a >= 1_000_000 -> "$sign${oneDecimal(a / 1_000_000)} triệu"
            a >= 1_000 -> "$sign${oneDecimal(a / 1_000)} nghìn"
            else -> "$sign${a.toLong()} ₫"
        }
    }

    fun compact(amount: Long, hidden: Boolean): String = if (hidden) HIDDEN else compact(amount)

    private fun oneDecimal(value: Double): String =
        String.format(vi, "%.1f", value).removeSuffix(",0")

    fun toLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

    /** "Hôm nay", "Hôm qua" hoặc dd/MM/yyyy */
    fun dateLabel(date: LocalDate): String {
        val today = LocalDate.now()
        return when (date) {
            today -> "Hôm nay"
            today.minusDays(1) -> "Hôm qua"
            else -> date.format(dateFormat)
        }
    }

    /** "Hôm nay", "Hôm qua" hoặc tên thứ ("Thứ Hai", "Chủ Nhật") – dùng làm tiêu đề nhóm ngày. */
    fun weekdayLabel(date: LocalDate): String {
        val today = LocalDate.now()
        return when (date) {
            today -> "Hôm nay"
            today.minusDays(1) -> "Hôm qua"
            else -> date.dayOfWeek.getDisplayName(TextStyle.FULL, vi)
                .replaceFirstChar { it.uppercase(vi) }
        }
    }

    fun date(date: LocalDate): String = date.format(dateFormat)

    fun fullDate(millis: Long): String = toLocalDate(millis).format(dateFormat)

    fun time(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(timeFormat)

    fun monthLabel(month: YearMonth): String = "Tháng ${month.monthValue}/${month.year}"
}
