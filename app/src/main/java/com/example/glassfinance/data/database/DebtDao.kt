package com.example.glassfinance.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.glassfinance.data.model.DebtEntity
import com.example.glassfinance.data.model.DebtPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {

    @Query("SELECT * FROM debts ORDER BY createdAt DESC")
    fun observeDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debt_payments ORDER BY paidAt DESC, id DESC")
    fun observePayments(): Flow<List<DebtPaymentEntity>>

    @Query("SELECT * FROM debts")
    suspend fun getAllDebtsOnce(): List<DebtEntity>

    @Query("SELECT * FROM debt_payments")
    suspend fun getAllPaymentsOnce(): List<DebtPaymentEntity>

    @Insert
    suspend fun insertDebt(debt: DebtEntity): Long

    @Insert
    suspend fun insertPayment(payment: DebtPaymentEntity): Long

    @Delete
    suspend fun deletePayment(payment: DebtPaymentEntity)

    @Query("DELETE FROM debts WHERE id = :debtId")
    suspend fun deleteDebt(debtId: Long)

    @Query("DELETE FROM debt_payments WHERE debtId = :debtId")
    suspend fun deletePaymentsOf(debtId: Long)

    @Query("DELETE FROM debts")
    suspend fun deleteAllDebts()

    @Query("DELETE FROM debt_payments")
    suspend fun deleteAllPayments()
}
