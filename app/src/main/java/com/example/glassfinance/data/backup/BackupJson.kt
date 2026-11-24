package com.example.glassfinance.data.backup

import com.example.glassfinance.data.model.BudgetEntity
import com.example.glassfinance.data.model.DebtEntity
import com.example.glassfinance.data.model.DebtPaymentEntity
import com.example.glassfinance.data.model.DebtWithPayments
import com.example.glassfinance.data.model.SavingsGoalEntity
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.TransactionSource
import com.example.glassfinance.data.model.TransactionType
import com.example.glassfinance.data.repository.BackupData
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Đọc / ghi bản sao lưu đầy đủ dạng JSON (dùng org.json có sẵn của Android). */
object BackupJson {

    private const val APP = "GlassFinance"
    private const val VERSION = 1

    fun build(data: BackupData): String {
        val root = JSONObject()
        root.put("app", APP)
        root.put("version", VERSION)
        root.put("exportedAt", System.currentTimeMillis())

        root.put("transactions", JSONArray().also { arr ->
            data.transactions.forEach { arr.put(transactionToJson(it)) }
        })
        root.put("budgets", JSONArray().also { arr ->
            data.budgets.forEach {
                arr.put(JSONObject().put("category", it.category).put("monthlyLimit", it.monthlyLimit))
            }
        })
        root.put("savingsGoals", JSONArray().also { arr ->
            data.goals.forEach {
                arr.put(
                    JSONObject()
                        .put("name", it.name)
                        .put("targetAmount", it.targetAmount)
                        .put("savedAmount", it.savedAmount)
                        .put("createdAt", it.createdAt)
                )
            }
        })
        root.put("debts", JSONArray().also { arr ->
            data.debts.forEach { arr.put(debtToJson(it)) }
        })
        return root.toString(2)
    }

    /** @throws IllegalArgumentException nếu tệp không phải bản sao lưu GlassFinance hợp lệ. */
    fun parse(text: String): BackupData {
        val root = try {
            JSONObject(text.trim().removePrefix("\uFEFF"))
        } catch (e: JSONException) {
            throw IllegalArgumentException("Tệp không phải bản sao lưu GlassFinance hợp lệ.")
        }
        require(root.optString("app") == APP) { "Tệp này không phải bản sao lưu của GlassFinance." }

        return try {
            BackupData(
                transactions = root.optJSONArray("transactions").objects().map(::transactionFromJson),
                budgets = root.optJSONArray("budgets").objects().map {
                    BudgetEntity(it.getString("category"), it.getLong("monthlyLimit"))
                },
                goals = root.optJSONArray("savingsGoals").objects().map {
                    SavingsGoalEntity(
                        name = it.getString("name"),
                        targetAmount = it.getLong("targetAmount"),
                        savedAmount = it.optLong("savedAmount", 0L),
                        createdAt = it.optLong("createdAt", System.currentTimeMillis())
                    )
                },
                debts = root.optJSONArray("debts").objects().map(::debtFromJson)
            )
        } catch (e: JSONException) {
            throw IllegalArgumentException("Bản sao lưu bị lỗi hoặc thiếu dữ liệu (${e.message}).")
        }
    }

    // ---------- giao dịch ----------

    private fun transactionToJson(t: TransactionEntity) = JSONObject()
        .put("bankName", t.bankName)
        .put("type", t.type)
        .put("category", t.category)
        .put("amount", t.amount)
        .put("content", t.content)
        .put("transactionTime", t.transactionTime)
        .put("source", t.source)
        .put("externalId", t.externalId ?: JSONObject.NULL)
        .put("fingerprint", t.fingerprint ?: JSONObject.NULL)
        .put("createdAt", t.createdAt)

    private fun transactionFromJson(o: JSONObject): TransactionEntity {
        val type = o.getString("type")
        require(type == TransactionType.INCOME || type == TransactionType.EXPENSE) {
            "Loại giao dịch không hợp lệ: $type"
        }
        return TransactionEntity(
            bankName = o.getString("bankName"),
            type = type,
            category = o.getString("category"),
            amount = o.getLong("amount"),
            content = o.optString("content", ""),
            transactionTime = o.getLong("transactionTime"),
            source = o.optString("source", TransactionSource.MANUAL),
            externalId = o.stringOrNull("externalId"),
            fingerprint = o.stringOrNull("fingerprint"),
            createdAt = o.optLong("createdAt", System.currentTimeMillis())
        )
    }

    // ---------- vay / nợ ----------

    private fun debtToJson(item: DebtWithPayments): JSONObject {
        val d = item.debt
        return JSONObject()
            .put("isBorrowed", d.isBorrowed)
            .put("name", d.name)
            .put("principal", d.principal)
            .put("dueDate", d.dueDate)
            .put("note", d.note)
            .put("createdAt", d.createdAt)
            .put("payments", JSONArray().also { arr ->
                item.payments.forEach {
                    arr.put(
                        JSONObject()
                            .put("amount", it.amount)
                            .put("note", it.note)
                            .put("paidAt", it.paidAt)
                    )
                }
            })
    }

    private fun debtFromJson(o: JSONObject): DebtWithPayments {
        val debt = DebtEntity(
            isBorrowed = o.getBoolean("isBorrowed"),
            name = o.getString("name"),
            principal = o.getLong("principal"),
            dueDate = o.optLong("dueDate", 0L),
            note = o.optString("note", ""),
            createdAt = o.optLong("createdAt", System.currentTimeMillis())
        )
        val payments = o.optJSONArray("payments").objects().map {
            DebtPaymentEntity(
                debtId = 0,
                amount = it.getLong("amount"),
                note = it.optString("note", ""),
                paidAt = it.optLong("paidAt", System.currentTimeMillis())
            )
        }
        return DebtWithPayments(debt, payments)
    }

    // ---------- tiện ích ----------

    private fun JSONArray?.objects(): List<JSONObject> =
        if (this == null) emptyList() else (0 until length()).map { getJSONObject(it) }

    private fun JSONObject.stringOrNull(key: String): String? =
        if (isNull(key)) null else getString(key)
}
