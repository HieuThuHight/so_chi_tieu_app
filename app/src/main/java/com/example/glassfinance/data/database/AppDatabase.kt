package com.example.glassfinance.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.glassfinance.data.model.BudgetEntity
import com.example.glassfinance.data.model.DebtEntity
import com.example.glassfinance.data.model.DebtPaymentEntity
import com.example.glassfinance.data.model.SavingsGoalEntity
import com.example.glassfinance.data.model.TransactionEntity

/**
 * Toàn bộ dữ liệu của app nằm trong MỘT cơ sở dữ liệu SQLite này (qua Room),
 * lưu cục bộ trên máy. Không có kết nối mạng nào.
 */
@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun debtDao(): DebtDao

    companion object {

        const val DB_NAME = "glass_finance_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** v2 → v3: thêm sổ vay / nợ. Giữ nguyên toàn bộ dữ liệu cũ. */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `debts` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`isBorrowed` INTEGER NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`principal` INTEGER NOT NULL, " +
                        "`dueDate` INTEGER NOT NULL, " +
                        "`note` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `debt_payments` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`debtId` INTEGER NOT NULL, " +
                        "`amount` INTEGER NOT NULL, " +
                        "`note` TEXT NOT NULL, " +
                        "`paidAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_debt_payments_debtId` ON `debt_payments` (`debtId`)"
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_2_3)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
