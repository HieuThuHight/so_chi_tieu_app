package com.example.glassfinance.data.backup

import android.content.Context
import android.net.Uri
import com.example.glassfinance.data.database.AppDatabase
import com.example.glassfinance.data.repository.FinanceRepository
import com.example.glassfinance.data.repository.ImportReport
import com.example.glassfinance.utils.CsvExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Xuất / nhập dữ liệu qua Storage Access Framework: người dùng tự chọn nơi lưu
 * (Tải xuống, Google Drive, USB…) nên app không cần quyền truy cập bộ nhớ.
 */
class BackupManager(
    private val context: Context,
    private val repository: FinanceRepository,
    private val database: AppDatabase
) {

    /** Ghi dữ liệu ra [uri] theo [kind]. Trả về thông báo kết quả cho người dùng. */
    suspend fun export(kind: ExportKind, uri: Uri): String = withContext(Dispatchers.IO) {
        when (kind) {
            ExportKind.CSV -> {
                val data = repository.snapshot()
                writeText(uri, CsvExporter.build(data.transactions))
                "Đã xuất ${data.transactions.size} giao dịch ra tệp CSV."
            }

            ExportKind.JSON -> {
                val data = repository.snapshot()
                writeText(uri, BackupJson.build(data))
                "Đã sao lưu ${data.transactions.size} giao dịch, ${data.debts.size} khoản vay/nợ (JSON)."
            }

            ExportKind.SQL -> {
                val data = repository.snapshot()
                writeText(uri, SqlExporter.build(data))
                "Đã xuất tệp SQL (${data.transactions.size} giao dịch)."
            }

            ExportKind.DATABASE -> {
                copyDatabaseTo(uri)
                "Đã xuất tệp cơ sở dữ liệu SQLite."
            }
        }
    }

    /** Nhập (gộp) từ bản sao lưu JSON do chính app này xuất ra. */
    suspend fun importJson(uri: Uri): ImportReport = withContext(Dispatchers.IO) {
        val text = context.contentResolver.openInputStream(uri)?.use {
            it.readBytes().toString(Charsets.UTF_8)
        } ?: throw IOException("Không đọc được tệp đã chọn.")
        repository.importData(BackupJson.parse(text))
    }

    private fun writeText(uri: Uri, text: String) {
        val out = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw IOException("Không mở được tệp để ghi.")
        out.use { it.write(text.toByteArray(Charsets.UTF_8)) }
    }

    private fun copyDatabaseTo(uri: Uri) {
        // Ghi hết dữ liệu trong file WAL vào file .db chính trước khi sao chép.
        database.openHelper.writableDatabase.query("PRAGMA wal_checkpoint(FULL)").use { it.moveToFirst() }

        val dbFile = context.getDatabasePath(AppDatabase.DB_NAME)
        val out = context.contentResolver.openOutputStream(uri, "wt")
            ?: throw IOException("Không mở được tệp để ghi.")
        out.use { output -> dbFile.inputStream().use { it.copyTo(output) } }
    }
}
