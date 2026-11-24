package com.example.glassfinance.`data`.database

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.glassfinance.`data`.model.DebtEntity
import com.example.glassfinance.`data`.model.DebtPaymentEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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
public class DebtDao_Impl(
  __db: RoomDatabase,
) : DebtDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDebtEntity: EntityInsertAdapter<DebtEntity>

  private val __insertAdapterOfDebtPaymentEntity: EntityInsertAdapter<DebtPaymentEntity>

  private val __deleteAdapterOfDebtPaymentEntity: EntityDeleteOrUpdateAdapter<DebtPaymentEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfDebtEntity = object : EntityInsertAdapter<DebtEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `debts` (`id`,`isBorrowed`,`name`,`principal`,`dueDate`,`note`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DebtEntity) {
        statement.bindLong(1, entity.id)
        val _tmp: Int = if (entity.isBorrowed) 1 else 0
        statement.bindLong(2, _tmp.toLong())
        statement.bindText(3, entity.name)
        statement.bindLong(4, entity.principal)
        statement.bindLong(5, entity.dueDate)
        statement.bindText(6, entity.note)
        statement.bindLong(7, entity.createdAt)
      }
    }
    this.__insertAdapterOfDebtPaymentEntity = object : EntityInsertAdapter<DebtPaymentEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `debt_payments` (`id`,`debtId`,`amount`,`note`,`paidAt`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DebtPaymentEntity) {
        statement.bindLong(1, entity.id)
        statement.bindLong(2, entity.debtId)
        statement.bindLong(3, entity.amount)
        statement.bindText(4, entity.note)
        statement.bindLong(5, entity.paidAt)
      }
    }
    this.__deleteAdapterOfDebtPaymentEntity = object : EntityDeleteOrUpdateAdapter<DebtPaymentEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `debt_payments` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: DebtPaymentEntity) {
        statement.bindLong(1, entity.id)
      }
    }
  }

  public override suspend fun insertDebt(debt: DebtEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfDebtEntity.insertAndReturnId(_connection, debt)
    _result
  }

  public override suspend fun insertPayment(payment: DebtPaymentEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfDebtPaymentEntity.insertAndReturnId(_connection, payment)
    _result
  }

  public override suspend fun deletePayment(payment: DebtPaymentEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfDebtPaymentEntity.handle(_connection, payment)
  }

  public override fun observeDebts(): Flow<List<DebtEntity>> {
    val _sql: String = "SELECT * FROM debts ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("debts")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfIsBorrowed: Int = getColumnIndexOrThrow(_stmt, "isBorrowed")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPrincipal: Int = getColumnIndexOrThrow(_stmt, "principal")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<DebtEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DebtEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpIsBorrowed: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsBorrowed).toInt()
          _tmpIsBorrowed = _tmp != 0
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPrincipal: Long
          _tmpPrincipal = _stmt.getLong(_columnIndexOfPrincipal)
          val _tmpDueDate: Long
          _tmpDueDate = _stmt.getLong(_columnIndexOfDueDate)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = DebtEntity(_tmpId,_tmpIsBorrowed,_tmpName,_tmpPrincipal,_tmpDueDate,_tmpNote,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observePayments(): Flow<List<DebtPaymentEntity>> {
    val _sql: String = "SELECT * FROM debt_payments ORDER BY paidAt DESC, id DESC"
    return createFlow(__db, false, arrayOf("debt_payments")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDebtId: Int = getColumnIndexOrThrow(_stmt, "debtId")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfPaidAt: Int = getColumnIndexOrThrow(_stmt, "paidAt")
        val _result: MutableList<DebtPaymentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DebtPaymentEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDebtId: Long
          _tmpDebtId = _stmt.getLong(_columnIndexOfDebtId)
          val _tmpAmount: Long
          _tmpAmount = _stmt.getLong(_columnIndexOfAmount)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpPaidAt: Long
          _tmpPaidAt = _stmt.getLong(_columnIndexOfPaidAt)
          _item = DebtPaymentEntity(_tmpId,_tmpDebtId,_tmpAmount,_tmpNote,_tmpPaidAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllDebtsOnce(): List<DebtEntity> {
    val _sql: String = "SELECT * FROM debts"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfIsBorrowed: Int = getColumnIndexOrThrow(_stmt, "isBorrowed")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfPrincipal: Int = getColumnIndexOrThrow(_stmt, "principal")
        val _columnIndexOfDueDate: Int = getColumnIndexOrThrow(_stmt, "dueDate")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<DebtEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DebtEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpIsBorrowed: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsBorrowed).toInt()
          _tmpIsBorrowed = _tmp != 0
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpPrincipal: Long
          _tmpPrincipal = _stmt.getLong(_columnIndexOfPrincipal)
          val _tmpDueDate: Long
          _tmpDueDate = _stmt.getLong(_columnIndexOfDueDate)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = DebtEntity(_tmpId,_tmpIsBorrowed,_tmpName,_tmpPrincipal,_tmpDueDate,_tmpNote,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllPaymentsOnce(): List<DebtPaymentEntity> {
    val _sql: String = "SELECT * FROM debt_payments"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDebtId: Int = getColumnIndexOrThrow(_stmt, "debtId")
        val _columnIndexOfAmount: Int = getColumnIndexOrThrow(_stmt, "amount")
        val _columnIndexOfNote: Int = getColumnIndexOrThrow(_stmt, "note")
        val _columnIndexOfPaidAt: Int = getColumnIndexOrThrow(_stmt, "paidAt")
        val _result: MutableList<DebtPaymentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DebtPaymentEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpDebtId: Long
          _tmpDebtId = _stmt.getLong(_columnIndexOfDebtId)
          val _tmpAmount: Long
          _tmpAmount = _stmt.getLong(_columnIndexOfAmount)
          val _tmpNote: String
          _tmpNote = _stmt.getText(_columnIndexOfNote)
          val _tmpPaidAt: Long
          _tmpPaidAt = _stmt.getLong(_columnIndexOfPaidAt)
          _item = DebtPaymentEntity(_tmpId,_tmpDebtId,_tmpAmount,_tmpNote,_tmpPaidAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDebt(debtId: Long) {
    val _sql: String = "DELETE FROM debts WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, debtId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deletePaymentsOf(debtId: Long) {
    val _sql: String = "DELETE FROM debt_payments WHERE debtId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, debtId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllDebts() {
    val _sql: String = "DELETE FROM debts"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAllPayments() {
    val _sql: String = "DELETE FROM debt_payments"
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
