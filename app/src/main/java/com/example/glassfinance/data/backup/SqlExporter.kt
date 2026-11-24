package com.example.glassfinance.data.backup

import com.example.glassfinance.data.repository.BackupData
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Xuất toàn bộ dữ liệu thành tệp .sql (CREATE TABLE + INSERT) chuẩn SQLite.
 * Mở bằng DB Browser for SQLite, hoặc: sqlite3 glassfinance.db < glassfinance_xxx.sql
 */
object SqlExporter {

    private val timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun build(data: BackupData): String {
        val sb = StringBuilder()
        sb.appendLine("-- GlassFinance: xuất dữ liệu dạng SQL (SQLite)")
        sb.appendLine("-- Mở bằng DB Browser for SQLite hoặc chạy: sqlite3 glassfinance.db < tệp_này.sql")
        sb.appendLine("PRAGMA foreign_keys = OFF;")
        sb.appendLine("BEGIN TRANSACTION;")
        sb.appendLine()

        sb.appendLine(
            """
            CREATE TABLE IF NOT EXISTS transactions (
              id INTEGER PRIMARY KEY,
              bank_name TEXT NOT NULL,
              type TEXT NOT NULL,            -- INCOME | EXPENSE
              category TEXT NOT NULL,
              amount INTEGER NOT NULL,       -- VND, luôn dương
              content TEXT NOT NULL,
              transaction_time INTEGER NOT NULL,  -- mili-giây UTC
              transaction_time_local TEXT NOT NULL,
              source TEXT NOT NULL,          -- NOTIFICATION | MANUAL | ...
              created_at INTEGER NOT NULL
            );
            CREATE TABLE IF NOT EXISTS budgets (
              category TEXT PRIMARY KEY,
              monthly_limit INTEGER NOT NULL
            );
            CREATE TABLE IF NOT EXISTS savings_goals (
              id INTEGER PRIMARY KEY,
              name TEXT NOT NULL,
              target_amount INTEGER NOT NULL,
              saved_amount INTEGER NOT NULL,
              created_at INTEGER NOT NULL
            );
            CREATE TABLE IF NOT EXISTS debts (
              id INTEGER PRIMARY KEY,
              is_borrowed INTEGER NOT NULL,  -- 1 = bạn đi vay, 0 = bạn cho vay
              name TEXT NOT NULL,
              principal INTEGER NOT NULL,
              due_date INTEGER NOT NULL,     -- 0 = không có hạn
              note TEXT NOT NULL,
              created_at INTEGER NOT NULL
            );
            CREATE TABLE IF NOT EXISTS debt_payments (
              id INTEGER PRIMARY KEY,
              debt_id INTEGER NOT NULL,
              amount INTEGER NOT NULL,
              note TEXT NOT NULL,
              paid_at INTEGER NOT NULL
            );
            CREATE VIEW IF NOT EXISTS v_monthly_summary AS
              SELECT strftime('%Y-%m', transaction_time / 1000, 'unixepoch', 'localtime') AS month,
                     SUM(CASE WHEN type = 'INCOME' THEN amount ELSE 0 END) AS income,
                     SUM(CASE WHEN type = 'EXPENSE' THEN amount ELSE 0 END) AS expense
              FROM transactions
              GROUP BY month
              ORDER BY month DESC;
            """.trimIndent()
        )
        sb.appendLine()

        data.transactions.forEach { t ->
            val local = Instant.ofEpochMilli(t.transactionTime).atZone(ZoneId.systemDefault()).format(timeFormat)
            sb.appendLine(
                "INSERT INTO transactions (id, bank_name, type, category, amount, content, transaction_time, " +
                    "transaction_time_local, source, created_at) VALUES (" +
                    "${t.id}, ${q(t.bankName)}, ${q(t.type)}, ${q(t.category)}, ${t.amount}, ${q(t.content)}, " +
                    "${t.transactionTime}, ${q(local)}, ${q(t.source)}, ${t.createdAt});"
            )
        }
        data.budgets.forEach { b ->
            sb.appendLine("INSERT INTO budgets (category, monthly_limit) VALUES (${q(b.category)}, ${b.monthlyLimit});")
        }
        data.goals.forEach { g ->
            sb.appendLine(
                "INSERT INTO savings_goals (id, name, target_amount, saved_amount, created_at) VALUES (" +
                    "${g.id}, ${q(g.name)}, ${g.targetAmount}, ${g.savedAmount}, ${g.createdAt});"
            )
        }
        data.debts.forEach { item ->
            val d = item.debt
            sb.appendLine(
                "INSERT INTO debts (id, is_borrowed, name, principal, due_date, note, created_at) VALUES (" +
                    "${d.id}, ${if (d.isBorrowed) 1 else 0}, ${q(d.name)}, ${d.principal}, ${d.dueDate}, " +
                    "${q(d.note)}, ${d.createdAt});"
            )
            item.payments.forEach { p ->
                sb.appendLine(
                    "INSERT INTO debt_payments (id, debt_id, amount, note, paid_at) VALUES (" +
                        "${p.id}, ${p.debtId}, ${p.amount}, ${q(p.note)}, ${p.paidAt});"
                )
            }
        }

        sb.appendLine()
        sb.appendLine("COMMIT;")
        return sb.toString()
    }

    /** Chuỗi SQL: bao bằng nháy đơn, nháy đơn bên trong được nhân đôi. */
    private fun q(value: String): String = "'" + value.replace("'", "''") + "'"
}
