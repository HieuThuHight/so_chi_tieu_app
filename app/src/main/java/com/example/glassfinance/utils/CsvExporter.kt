package com.example.glassfinance.utils

import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.isIncome
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object CsvExporter {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    /** Có BOM UTF-8 ở đầu để Excel hiển thị đúng tiếng Việt. */
    fun build(list: List<TransactionEntity>): String {
        val sb = StringBuilder("\uFEFFThời gian,Loại,Danh mục,Số tiền,Nội dung,Ngân hàng,Nguồn\r\n")
        list.forEach { tx ->
            val time = Instant.ofEpochMilli(tx.transactionTime)
                .atZone(ZoneId.systemDefault())
                .format(formatter)
            sb.append(time).append(',')
                .append(if (tx.isIncome) "Thu" else "Chi").append(',')
                .append(escape(tx.category)).append(',')
                .append(tx.amount).append(',')
                .append(escape(tx.content)).append(',')
                .append(escape(tx.bankName)).append(',')
                .append(escape(tx.source)).append("\r\n")
        }
        return sb.toString()
    }

    private fun escape(value: String): String = "\"" + value.replace("\"", "\"\"") + "\""
}
