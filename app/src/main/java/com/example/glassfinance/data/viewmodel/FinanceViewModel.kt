package com.example.glassfinance.data.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.glassfinance.data.backup.BackupManager
import com.example.glassfinance.data.backup.ExportKind
import com.example.glassfinance.data.model.BudgetEntity
import com.example.glassfinance.data.model.DebtEntity
import com.example.glassfinance.data.model.DebtPaymentEntity
import com.example.glassfinance.data.model.DebtWithPayments
import com.example.glassfinance.data.model.SavingsGoalEntity
import com.example.glassfinance.data.model.TransactionEntity
import com.example.glassfinance.data.preferences.AppSettings
import com.example.glassfinance.data.preferences.SettingsStore
import com.example.glassfinance.data.preferences.ThemeMode
import com.example.glassfinance.data.repository.FinanceRepository
import com.example.glassfinance.utils.SampleData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FinanceViewModel(
    private val repository: FinanceRepository,
    private val settingsStore: SettingsStore,
    private val backupManager: BackupManager
) : ViewModel() {

    val transactions: StateFlow<List<TransactionEntity>> =
        repository.transactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val budgets: StateFlow<List<BudgetEntity>> =
        repository.budgets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val savingsGoals: StateFlow<List<SavingsGoalEntity>> =
        repository.savingsGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val debts: StateFlow<List<DebtWithPayments>> =
        repository.debts.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val settings: StateFlow<AppSettings> = settingsStore.settings

    // ----- Giao dịch -----

    fun updateTransactionCategory(transactionId: Long, newCategory: String) {
        viewModelScope.launch { repository.updateTransactionCategory(transactionId, newCategory) }
    }

    /** id == 0 là giao dịch mới, ngược lại là cập nhật. */
    fun saveTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            if (transaction.id == 0L) {
                repository.addTransaction(transaction)
            } else {
                repository.updateTransaction(transaction)
            }
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch { repository.deleteTransaction(transaction) }
    }

    // ----- Ngân sách -----

    fun setBudget(category: String, monthlyLimit: Long) {
        viewModelScope.launch { repository.upsertBudget(BudgetEntity(category, monthlyLimit)) }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch { repository.deleteBudget(budget) }
    }

    // ----- Tiết kiệm -----

    fun addSavingsGoal(name: String, target: Long) {
        viewModelScope.launch {
            repository.addSavingsGoal(SavingsGoalEntity(name = name, targetAmount = target))
        }
    }

    fun updateSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch { repository.updateSavingsGoal(goal) }
    }

    fun deleteSavingsGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch { repository.deleteSavingsGoal(goal) }
    }

    // ----- Vay / nợ (lưu cục bộ trong SQLite) -----

    fun addDebt(isBorrowed: Boolean, name: String, amount: Long, dueDate: Long, note: String) {
        viewModelScope.launch {
            repository.addDebt(
                DebtEntity(
                    isBorrowed = isBorrowed,
                    name = name,
                    principal = amount,
                    dueDate = dueDate,
                    note = note
                )
            )
        }
    }

    fun payDebt(debtId: Long, amount: Long, note: String) {
        viewModelScope.launch {
            repository.addDebtPayment(DebtPaymentEntity(debtId = debtId, amount = amount, note = note))
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch { repository.deleteDebt(debt) }
    }

    fun deleteDebtPayment(payment: DebtPaymentEntity) {
        viewModelScope.launch { repository.deleteDebtPayment(payment) }
    }

    // ----- Cài đặt -----

    fun setThemeMode(mode: ThemeMode) = settingsStore.setThemeMode(mode)

    fun toggleHideBalance() = settingsStore.setHideBalance(!settings.value.hideBalance)

    fun setHideBalance(hide: Boolean) = settingsStore.setHideBalance(hide)

    fun setBankEnabled(bank: String, enabled: Boolean) = settingsStore.setBankEnabled(bank, enabled)

    fun setGlassOpacity(value: Float) = settingsStore.setGlassOpacity(value)

    fun setNavBarOpacity(value: Float) = settingsStore.setNavBarOpacity(value)

    // ----- Xuất / nhập dữ liệu -----

    private val _dataMessage = MutableStateFlow("")
    val dataMessage: StateFlow<String> = _dataMessage.asStateFlow()

    fun clearDataMessage() {
        _dataMessage.value = ""
    }

    fun export(kind: ExportKind, uri: Uri) {
        viewModelScope.launch {
            _dataMessage.value = try {
                val message = backupManager.export(kind, uri)
                settingsStore.setLastBackup(System.currentTimeMillis())
                message
            } catch (e: Exception) {
                "Xuất không thành công: ${e.message ?: "lỗi không rõ"}"
            }
        }
    }

    fun importBackup(uri: Uri) {
        viewModelScope.launch {
            _dataMessage.value = try {
                backupManager.importJson(uri).summary()
            } catch (e: Exception) {
                "Nhập không thành công: ${e.message ?: "lỗi không rõ"}"
            }
        }
    }

    fun addSampleData() {
        viewModelScope.launch {
            repository.addTransactions(SampleData.transactions())
            SampleData.budgets().forEach { repository.upsertBudget(it) }
            repository.addSavingsGoal(SampleData.savingsGoal())
        }
    }

    fun clearAllData() {
        viewModelScope.launch { repository.clearAll() }
    }
}
