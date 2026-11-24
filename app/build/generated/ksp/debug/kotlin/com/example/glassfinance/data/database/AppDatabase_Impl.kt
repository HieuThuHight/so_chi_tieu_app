package com.example.glassfinance.`data`.database

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _transactionDao: Lazy<TransactionDao> = lazy {
    TransactionDao_Impl(this)
  }

  private val _budgetDao: Lazy<BudgetDao> = lazy {
    BudgetDao_Impl(this)
  }

  private val _savingsGoalDao: Lazy<SavingsGoalDao> = lazy {
    SavingsGoalDao_Impl(this)
  }

  private val _debtDao: Lazy<DebtDao> = lazy {
    DebtDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3, "3822033fbbd6dc8df685d3453afc64a7", "a2e1369e4d53748432c2bb029eace4ce") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `bankName` TEXT NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `amount` INTEGER NOT NULL, `content` TEXT NOT NULL, `transactionTime` INTEGER NOT NULL, `source` TEXT NOT NULL, `externalId` TEXT, `fingerprint` TEXT, `syncStatus` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_transactionTime` ON `transactions` (`transactionTime`)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_transactions_fingerprint` ON `transactions` (`fingerprint`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `budgets` (`category` TEXT NOT NULL, `monthlyLimit` INTEGER NOT NULL, PRIMARY KEY(`category`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `savings_goals` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `targetAmount` INTEGER NOT NULL, `savedAmount` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `debts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `isBorrowed` INTEGER NOT NULL, `name` TEXT NOT NULL, `principal` INTEGER NOT NULL, `dueDate` INTEGER NOT NULL, `note` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `debt_payments` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `debtId` INTEGER NOT NULL, `amount` INTEGER NOT NULL, `note` TEXT NOT NULL, `paidAt` INTEGER NOT NULL)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_debt_payments_debtId` ON `debt_payments` (`debtId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '3822033fbbd6dc8df685d3453afc64a7')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `transactions`")
        connection.execSQL("DROP TABLE IF EXISTS `budgets`")
        connection.execSQL("DROP TABLE IF EXISTS `savings_goals`")
        connection.execSQL("DROP TABLE IF EXISTS `debts`")
        connection.execSQL("DROP TABLE IF EXISTS `debt_payments`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsTransactions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsTransactions.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("bankName", TableInfo.Column("bankName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("amount", TableInfo.Column("amount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("content", TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("transactionTime", TableInfo.Column("transactionTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("source", TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("externalId", TableInfo.Column("externalId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("fingerprint", TableInfo.Column("fingerprint", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("syncStatus", TableInfo.Column("syncStatus", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsTransactions.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysTransactions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesTransactions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesTransactions.add(TableInfo.Index("index_transactions_transactionTime", false, listOf("transactionTime"), listOf("ASC")))
        _indicesTransactions.add(TableInfo.Index("index_transactions_fingerprint", true, listOf("fingerprint"), listOf("ASC")))
        val _infoTransactions: TableInfo = TableInfo("transactions", _columnsTransactions, _foreignKeysTransactions, _indicesTransactions)
        val _existingTransactions: TableInfo = read(connection, "transactions")
        if (!_infoTransactions.equals(_existingTransactions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |transactions(com.example.glassfinance.data.model.TransactionEntity).
              | Expected:
              |""".trimMargin() + _infoTransactions + """
              |
              | Found:
              |""".trimMargin() + _existingTransactions)
        }
        val _columnsBudgets: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsBudgets.put("category", TableInfo.Column("category", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsBudgets.put("monthlyLimit", TableInfo.Column("monthlyLimit", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysBudgets: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesBudgets: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoBudgets: TableInfo = TableInfo("budgets", _columnsBudgets, _foreignKeysBudgets, _indicesBudgets)
        val _existingBudgets: TableInfo = read(connection, "budgets")
        if (!_infoBudgets.equals(_existingBudgets)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |budgets(com.example.glassfinance.data.model.BudgetEntity).
              | Expected:
              |""".trimMargin() + _infoBudgets + """
              |
              | Found:
              |""".trimMargin() + _existingBudgets)
        }
        val _columnsSavingsGoals: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSavingsGoals.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavingsGoals.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavingsGoals.put("targetAmount", TableInfo.Column("targetAmount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavingsGoals.put("savedAmount", TableInfo.Column("savedAmount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSavingsGoals.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSavingsGoals: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSavingsGoals: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSavingsGoals: TableInfo = TableInfo("savings_goals", _columnsSavingsGoals, _foreignKeysSavingsGoals, _indicesSavingsGoals)
        val _existingSavingsGoals: TableInfo = read(connection, "savings_goals")
        if (!_infoSavingsGoals.equals(_existingSavingsGoals)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |savings_goals(com.example.glassfinance.data.model.SavingsGoalEntity).
              | Expected:
              |""".trimMargin() + _infoSavingsGoals + """
              |
              | Found:
              |""".trimMargin() + _existingSavingsGoals)
        }
        val _columnsDebts: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDebts.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDebts.put("isBorrowed", TableInfo.Column("isBorrowed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDebts.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDebts.put("principal", TableInfo.Column("principal", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDebts.put("dueDate", TableInfo.Column("dueDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDebts.put("note", TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDebts.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDebts: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDebts: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDebts: TableInfo = TableInfo("debts", _columnsDebts, _foreignKeysDebts, _indicesDebts)
        val _existingDebts: TableInfo = read(connection, "debts")
        if (!_infoDebts.equals(_existingDebts)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |debts(com.example.glassfinance.data.model.DebtEntity).
              | Expected:
              |""".trimMargin() + _infoDebts + """
              |
              | Found:
              |""".trimMargin() + _existingDebts)
        }
        val _columnsDebtPayments: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDebtPayments.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDebtPayments.put("debtId", TableInfo.Column("debtId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDebtPayments.put("amount", TableInfo.Column("amount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDebtPayments.put("note", TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDebtPayments.put("paidAt", TableInfo.Column("paidAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDebtPayments: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDebtPayments: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesDebtPayments.add(TableInfo.Index("index_debt_payments_debtId", false, listOf("debtId"), listOf("ASC")))
        val _infoDebtPayments: TableInfo = TableInfo("debt_payments", _columnsDebtPayments, _foreignKeysDebtPayments, _indicesDebtPayments)
        val _existingDebtPayments: TableInfo = read(connection, "debt_payments")
        if (!_infoDebtPayments.equals(_existingDebtPayments)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |debt_payments(com.example.glassfinance.data.model.DebtPaymentEntity).
              | Expected:
              |""".trimMargin() + _infoDebtPayments + """
              |
              | Found:
              |""".trimMargin() + _existingDebtPayments)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "transactions", "budgets", "savings_goals", "debts", "debt_payments")
  }

  public override fun clearAllTables() {
    super.performClear(false, "transactions", "budgets", "savings_goals", "debts", "debt_payments")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(TransactionDao::class, TransactionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(BudgetDao::class, BudgetDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SavingsGoalDao::class, SavingsGoalDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(DebtDao::class, DebtDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun transactionDao(): TransactionDao = _transactionDao.value

  public override fun budgetDao(): BudgetDao = _budgetDao.value

  public override fun savingsGoalDao(): SavingsGoalDao = _savingsGoalDao.value

  public override fun debtDao(): DebtDao = _debtDao.value
}
