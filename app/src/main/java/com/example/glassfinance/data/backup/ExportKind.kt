package com.example.glassfinance.data.backup

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Các định dạng xuất dữ liệu ra khỏi app. */
enum class ExportKind(val label: String, val mime: String, val extension: String) {
    CSV("CSV (mở bằng Excel / Google Sheets)", "text/csv", "csv"),
    JSON("Sao lưu đầy đủ (JSON, nhập lại được)", "application/json", "json"),
    SQL("Tệp SQL (INSERT, mở bằng DB Browser)", "application/octet-stream", "sql"),
    DATABASE("Tệp cơ sở dữ liệu SQLite (.db)", "application/octet-stream", "db");

    fun suggestedFileName(): String {
        val stamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US).format(Date())
        return "glassfinance_$stamp.$extension"
    }
}
