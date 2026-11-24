package com.example.glassfinance.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.glassfinance.data.model.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("UPDATE transactions SET category = :newCategory WHERE id = :transactionId")
    suspend fun updateCategoryOnly(
        transactionId: Long,
        newCategory: String
    ): Int

    // Lấy toàn bộ giao dịch, mới nhất lên đầu
    @Query("SELECT * FROM transactions ORDER BY transactionTime DESC, id DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    /** Đọc một lần (dùng khi xuất / nhập dữ liệu). */
    @Query("SELECT * FROM transactions ORDER BY transactionTime DESC, id DESC")
    suspend fun getAllOnce(): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun count(): Int

    @Query("SELECT * FROM transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()

    // ----- Chống trùng khi đọc thông báo -----

    /** Đã có bản "chính thức" (Gmail / OCR – ví dụ từ tệp sao lưu cũ) của cùng giao dịch chưa? */
    @Query(
        """
        SELECT COUNT(*) FROM transactions
        WHERE source IN ('GMAIL', 'SHORTCUT')
          AND type = :type AND amount = :amount
          AND transactionTime BETWEEN :from AND :to
        """
    )
    suspend fun countOfficialMatches(type: String, amount: Long, from: Long, to: Long): Int
}
