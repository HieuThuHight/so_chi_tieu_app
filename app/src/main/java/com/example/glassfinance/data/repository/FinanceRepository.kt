package com.example.glassfinance.data.repository

import com.example.glassfinance.data.database.BudgetDao
import com.example.glassfinance.data.database.DebtDao
import com.example.glassfinance.data.database.SavingsGoalDao
import com.example.glassfinance.data.database.TransactionDao
import com.example.glassfinance.data.model.BudgetEntity
import com.example.glassfinance.data.model.DebtEntity
import com.example.glassfinance.data.model.DebtPaymentEntity
import com.example.glassfinance.data.model.DebtWithPayments
import com.example.glassfinance.data.model.SavingsGoalEntity
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.model.TransactionSource
import com.example.glassfinance.parser.CategoryGuesser
import com.example.glassfinance.parser.ParsedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/** Toàn bộ dữ liệu của app tại một thời điểm (dùng để xuất / nhập). */
data class BackupData(
    val transactions: List<TransactionEntity>,
    val budgets: List<BudgetEntity>,
    val goals: List<SavingsGoalEntity>,
    val debts: List<DebtWithPayments>
)

data class ImportReport(
    val transactions: Int,
    val budgets: Int,
    val goals: Int,
    val debts: Int
) {
    fun summary(): String =
        "Đã nhập $transactions giao dịch, $budgets ngân sách, $goals mục tiêu tiết kiệm, $debts khoản vay/nợ. " +
            "Dữ liệu trùng được bỏ qua."
}

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val savingsGoalDao: SavingsGoalDao,
    private val debtDao: DebtDao
) {

    val transactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()
    val budgets: Flow<List<BudgetEntity>> = budgetDao.observeAll()
    val savingsGoals: Flow<List<SavingsGoalEntity>> = savingsGoalDao.observeAll()

    val debts: Flow<List<DebtWithPayments>> =
        combine(debtDao.observeDebts(), debtDao.observePayments()) { debts, payments ->
            val byDebt = payments.groupBy { it.debtId }
            debts.map { DebtWithPayments(it, byDebt[it.id].orEmpty()) }
        }

    // ----- Giao dịch -----
    suspend fun updateTransactionCategory(transactionId: Long, newCategory: String): Int =
        transactionDao.updateCategoryOnly(transactionId, newCategory)

    suspend fun getTransactionById(id: Long): TransactionEntity? =
        transactionDao.getTransactionById(id)

    suspend fun addTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    suspend fun addTransactions(list: List<TransactionEntity>) =
        transactionDao.insertAll(list)

    suspend fun updateTransaction(transaction: TransactionEntity) =
        transactionDao.updateTransaction(transaction)

    suspend fun deleteTransaction(transaction: TransactionEntity) =
        transactionDao.deleteTransaction(transaction)

    /**
     * Lưu giao dịch đọc từ thông báo ngân hàng.
     * Trả về false nếu là bản trùng: cùng thông báo bị báo lại (fingerprint trùng) hoặc
     * giao dịch này đã có bản Gmail/OCR (từ tệp nhập vào) trong khoảng [DEDUPE_WINDOW_MS].
     */
    suspend fun addFromNotification(p: ParsedTransaction): Boolean {
        val official = transactionDao.countOfficialMatches(
            type = p.type,
            amount = p.amount,
            from = p.timeMillis - DEDUPE_WINDOW_MS,
            to = p.timeMillis + DEDUPE_WINDOW_MS
        )
        if (official > 0) return false

        val id = transactionDao.insertTransaction(
            TransactionEntity(
                bankName = p.bank,
                type = p.type,
                category = CategoryGuesser.guess(p.isIncome, p.content),
                amount = p.amount,
                content = p.content,
                transactionTime = p.timeMillis,
                source = TransactionSource.NOTIFICATION,
                fingerprint = p.fingerprint
            )
        )
        return id != -1L // -1: IGNORE vì fingerprint đã tồn tại
    }

    // ----- Ngân sách -----
    suspend fun upsertBudget(budget: BudgetEntity) = budgetDao.upsert(budget)
    suspend fun deleteBudget(budget: BudgetEntity) = budgetDao.delete(budget)

    // ----- Tiết kiệm -----
    suspend fun addSavingsGoal(goal: SavingsGoalEntity) = savingsGoalDao.insert(goal)
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity) = savingsGoalDao.update(goal)
    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) = savingsGoalDao.delete(goal)

    // ----- Vay / nợ -----
    suspend fun addDebt(debt: DebtEntity): Long = debtDao.insertDebt(debt)

    suspend fun addDebtPayment(payment: DebtPaymentEntity) {
        debtDao.insertPayment(payment)
    }

    suspend fun deleteDebtPayment(payment: DebtPaymentEntity) = debtDao.deletePayment(payment)

    suspend fun deleteDebt(debt: DebtEntity) {
        debtDao.deletePaymentsOf(debt.id)
        debtDao.deleteDebt(debt.id)
    }

    // ----- Toàn bộ dữ liệu -----
    suspend fun clearAll() {
        transactionDao.deleteAllTransactions()
        budgetDao.deleteAll()
        savingsGoalDao.deleteAll()
        debtDao.deleteAllPayments()
        debtDao.deleteAllDebts()
    }

    suspend fun snapshot(): BackupData {
        val payments = debtDao.getAllPaymentsOnce().groupBy { it.debtId }
        return BackupData(
            transactions = transactionDao.getAllOnce(),
            budgets = budgetDao.getAllOnce(),
            goals = savingsGoalDao.getAllOnce(),
            debts = debtDao.getAllDebtsOnce().map { DebtWithPayments(it, payments[it.id].orEmpty()) }
        )
    }

    /**
     * Nhập dữ liệu từ bản sao lưu theo kiểu GỘP, không xóa dữ liệu đang có:
     * giao dịch / mục tiêu / khoản nợ đã tồn tại thì bỏ qua; ngân sách cùng danh mục thì ghi đè.
     */
    suspend fun importData(data: BackupData): ImportReport {
        // Giao dịch
        val seen = transactionDao.getAllOnce().map { txKey(it) }.toMutableSet()
        val freshTx = data.transactions.filter { seen.add(txKey(it)) }.map { it.copy(id = 0) }
        val before = transactionDao.count()
        transactionDao.insertAll(freshTx) // fingerprint trùng sẽ bị bỏ qua
        val addedTx = transactionDao.count() - before

        // Ngân sách
        data.budgets.forEach { budgetDao.upsert(it) }

        // Mục tiêu tiết kiệm
        val goalKeys = savingsGoalDao.getAllOnce().map { "${it.name}|${it.targetAmount}" }.toMutableSet()
        var addedGoals = 0
        data.goals.forEach { g ->
            if (goalKeys.add("${g.name}|${g.targetAmount}")) {
                savingsGoalDao.insert(g.copy(id = 0))
                addedGoals++
            }
        }

        // Vay / nợ (kèm lịch sử trả)
        val debtKeys = debtDao.getAllDebtsOnce()
            .map { "${it.isBorrowed}|${it.name}|${it.principal}|${it.createdAt}" }.toMutableSet()
        var addedDebts = 0
        data.debts.forEach { item ->
            val d = item.debt
            if (debtKeys.add("${d.isBorrowed}|${d.name}|${d.principal}|${d.createdAt}")) {
                val newId = debtDao.insertDebt(d.copy(id = 0))
                item.payments.forEach { debtDao.insertPayment(it.copy(id = 0, debtId = newId)) }
                addedDebts++
            }
        }

        return ImportReport(addedTx, data.budgets.size, addedGoals, addedDebts)
    }

    private fun txKey(t: TransactionEntity) =
        "${t.type}|${t.amount}|${t.transactionTime}|${t.content}|${t.bankName}"

    private companion object {
        /** Hai bản ghi của cùng một giao dịch lệch nhau tối đa bằng này thì coi là một. */
        const val DEDUPE_WINDOW_MS = 15 * 60 * 1000L
    }
}
