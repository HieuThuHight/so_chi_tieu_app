package com.example.glassfinance.data.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.glassfinance.data.backup.BackupManager
import com.example.glassfinance.data.database.AppDatabase
import com.example.glassfinance.data.preferences.SettingsStore
import com.example.glassfinance.data.repository.FinanceRepository

class FinanceViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {

    private val appContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            val db = AppDatabase.getDatabase(appContext)
            val repository = FinanceRepository(
                transactionDao = db.transactionDao(),
                budgetDao = db.budgetDao(),
                savingsGoalDao = db.savingsGoalDao(),
                debtDao = db.debtDao()
            )
            return FinanceViewModel(
                repository = repository,
                settingsStore = SettingsStore(appContext),
                backupManager = BackupManager(appContext, repository, db)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
