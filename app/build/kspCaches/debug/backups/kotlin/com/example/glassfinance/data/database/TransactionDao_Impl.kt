package com.example.glassfinance.`data`.database

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.getTotalChangedRows
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.glassfinance.`data`.model.TransactionEntity
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class TransactionDao_Impl(
  __db: RoomDatabase,
) : TransactionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfTransactionEntity: EntityInsertAdapter<TransactionEntity>

  private val __deleteAdapterOfTransactionEntity: EntityDeleteOrUpdateAdapter<TransactionEntity>

  private val __updateAdapterOfTransactionEntity: EntityDeleteOrUpdateAdapter<TransactionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfTransactionEntity = object : EntityInsertAdapter<TransactionEntity>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `transactions` (`id`,`bankName`,`type`,`category`,`amount`,`content`,`transactionTime`,`source`,`externalId`,`fingerprint`,`syncStatus`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: TransactionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.bankName)
        statement.bindText(3, entity.type)
        statement.bindText(4, entity.category)
        statement.bindLong(5, entity.amount)
        statement.bindText(6, entity.content)
        statement.bindLong(7, entity.transactionTime)
        statement.bindText(8, entity.source)
        val _tmpExternalId: String? = entity.externalId
        if (_tmpExternalId == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpExternalId)
        }
        val _tmpFingerprint: String? = entity.fingerprint
        if (_tmpFingerprint == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpFingerprint)
        }
        statement.bindText(11, entity.syncStatus)
        statement.bindLong(12, entity.createdAt)
      }
    }
    this.__deleteAdapterOfTransactionEntity = object : EntityDeleteOrUpdateAdapter<TransactionEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `transactions` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TransactionEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfTransactionEntity = object : EntityDeleteOrUpdateAdapter<TransactionEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `transactions` SET `id` = ?,`bankName` = ?,`type` = ?,`category` = ?,`amount` = ?,`content` = ?,`transactionTime` = ?,`source` = ?,`externalId` = ?,`fingerprint` = ?,`syncStatus` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: TransactionEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.bankName)
        statement.bindText(3, entity.type)
        statement.bindText(4, entity.category)
        statement.bindLong(5, entity.amount)
        statement.bindText(6, entity.content)
        statement.bindLong(7, entity.transactionTime)
        statement.bindText(8, entity.source)
        val _tmpExternalId: String? = entity.externalId
        if (_tmpExternalId == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpExternalId)
        }
        val _tmpFingerprint: String? = entity.fingerprint
        if (_tmpFingerprint == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpFingerprint)
        }
        statement.bindText(11, entity.syncStatus)
        statement.bindLong(12, entity.createdAt)
        statement.bindLong(13, entity.id)
      }
    }
  }

  public override suspend fun insertTransaction(transaction: TransactionEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfTransactionEntity.insertAndReturnId(_connection, transaction)
    _result
  }

  public override suspend fun insertAll(transactions: List<TransactionEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfTransactionEntity.insert(_connection, transactions)
  }

  public override suspend fun deleteTransaction(transaction: TransactionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfTransactionEntity.handle(_connection, transaction)
  }

  public override suspend fun updateTransaction(transaction: TransactionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfTransactionEntity.handle(_connection, transaction)
  }

  public override fun getAllTransactions(): Flow<List<TransactionEntity>> {
    val _sql: String = "SELECT * FROM transactions ORDER BY transactionTime DESC, id DESC"
    return createFlow(__db, false, arrayOf("transactions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfBankName: Int = getColumnIndexOrThrow(_stmt, "bankName")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfTransactionTime: Int = getColumnIndexOrThrow(_stmt, "transactionTime")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfExternalId: Int = getColumnIndexOrThrow(_stmt, "externalId")
        val _columnIndexOfFingerprint: Int = getColumnIndexOrThrow(_stmt, "fingerprint")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<TransactionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TransactionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpBankName: String
          _tmpBankName = _stmt.getText(_columnIndexOfBankName)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpAmount: Long
          _tmpAmount = _stmt.getLong(_columnIndexOfAmount)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpTransactionTime: Long
          _tmpTransactionTime = _stmt.getLong(_columnIndexOfTransactionTime)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpExternalId: String?
          if (_stmt.isNull(_columnIndexOfExternalId)) {
            _tmpExternalId = null
          } else {
            _tmpExternalId = _stmt.getText(_columnIndexOfExternalId)
          }
          val _tmpFingerprint: String?
          if (_stmt.isNull(_columnIndexOfFingerprint)) {
            _tmpFingerprint = null
          } else {
            _tmpFingerprint = _stmt.getText(_columnIndexOfFingerprint)
          }
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = TransactionEntity(_tmpId,_tmpBankName,_tmpType,_tmpCategory,_tmpAmount,_tmpContent,_tmpTransactionTime,_tmpSource,_tmpExternalId,_tmpFingerprint,_tmpSyncStatus,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllOnce(): List<TransactionEntity> {
    val _sql: String = "SELECT * FROM transactions ORDER BY transactionTime DESC, id DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfBankName: Int = getColumnIndexOrThrow(_stmt, "bankName")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfTransactionTime: Int = getColumnIndexOrThrow(_stmt, "transactionTime")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfExternalId: Int = getColumnIndexOrThrow(_stmt, "externalId")
        val _columnIndexOfFingerprint: Int = getColumnIndexOrThrow(_stmt, "fingerprint")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<TransactionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: TransactionEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpBankName: String
          _tmpBankName = _stmt.getText(_columnIndexOfBankName)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpAmount: Long
          _tmpAmount = _stmt.getLong(_columnIndexOfAmount)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpTransactionTime: Long
          _tmpTransactionTime = _stmt.getLong(_columnIndexOfTransactionTime)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpExternalId: String?
          if (_stmt.isNull(_columnIndexOfExternalId)) {
            _tmpExternalId = null
          } else {
            _tmpExternalId = _stmt.getText(_columnIndexOfExternalId)
          }
          val _tmpFingerprint: String?
          if (_stmt.isNull(_columnIndexOfFingerprint)) {
            _tmpFingerprint = null
          } else {
            _tmpFingerprint = _stmt.getText(_columnIndexOfFingerprint)
          }
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = TransactionEntity(_tmpId,_tmpBankName,_tmpType,_tmpCategory,_tmpAmount,_tmpContent,_tmpTransactionTime,_tmpSource,_tmpExternalId,_tmpFingerprint,_tmpSyncStatus,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM transactions"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTransactionById(id: Long): TransactionEntity? {
    val _sql: String = "SELECT * FROM transactions WHERE id = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfBankName: Int = getColumnIndexOrThrow(_stmt, "bankName")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfTransactionTime: Int = getColumnIndexOrThrow(_stmt, "transactionTime")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfExternalId: Int = getColumnIndexOrThrow(_stmt, "externalId")
        val _columnIndexOfFingerprint: Int = getColumnIndexOrThrow(_stmt, "fingerprint")
        val _columnIndexOfSyncStatus: Int = getColumnIndexOrThrow(_stmt, "syncStatus")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: TransactionEntity?
        if (_stmt.step()) {
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpBankName: String
          _tmpBankName = _stmt.getText(_columnIndexOfBankName)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpAmount: Long
          _tmpAmount = _stmt.getLong(_columnIndexOfAmount)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpTransactionTime: Long
          _tmpTransactionTime = _stmt.getLong(_columnIndexOfTransactionTime)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpExternalId: String?
          if (_stmt.isNull(_columnIndexOfExternalId)) {
            _tmpExternalId = null
          } else {
            _tmpExternalId = _stmt.getText(_columnIndexOfExternalId)
          }
          val _tmpFingerprint: String?
          if (_stmt.isNull(_columnIndexOfFingerprint)) {
            _tmpFingerprint = null
          } else {
            _tmpFingerprint = _stmt.getText(_columnIndexOfFingerprint)
          }
          val _tmpSyncStatus: String
          _tmpSyncStatus = _stmt.getText(_columnIndexOfSyncStatus)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _result = TransactionEntity(_tmpId,_tmpBankName,_tmpType,_tmpCategory,_tmpAmount,_tmpContent,_tmpTransactionTime,_tmpSource,_tmpExternalId,_tmpFingerprint,_tmpSyncStatus,_tmpCreatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun countOfficialMatches(
    type: String,
    amount: Long,
    from: Long,
    to: Long,
  ): Int {
    val _sql: String = """
        |
        |        SELECT COUNT(*) FROM transactions
        |        WHERE source IN ('GMAIL', 'SHORTCUT')
        |          AND type = ? AND amount = ?
        |          AND transactionTime BETWEEN ? AND ?
        |        
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, type)
        _argIndex = 2
        _stmt.bindLong(_argIndex, amount)
        _argIndex = 3
        _stmt.bindLong(_argIndex, from)
        _argIndex = 4
        _stmt.bindLong(_argIndex, to)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateCategoryOnly(transactionId: Long, newCategory: String): Int {
    val _sql: String = "UPDATE transactions SET category = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, newCategory)
        _argIndex = 2
        _stmt.bindLong(_argIndex, transactionId)
        _stmt.step()
        getTotalChangedRows(_connection)
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllTransactions() {
    val _sql: String = "DELETE FROM transactions"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
