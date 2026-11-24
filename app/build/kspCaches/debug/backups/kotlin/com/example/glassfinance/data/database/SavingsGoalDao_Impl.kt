package com.example.glassfinance.`data`.database

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.example.glassfinance.`data`.model.SavingsGoalEntity
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
public class SavingsGoalDao_Impl(
  __db: RoomDatabase,
) : SavingsGoalDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSavingsGoalEntity: EntityInsertAdapter<SavingsGoalEntity>

  private val __deleteAdapterOfSavingsGoalEntity: EntityDeleteOrUpdateAdapter<SavingsGoalEntity>

  private val __updateAdapterOfSavingsGoalEntity: EntityDeleteOrUpdateAdapter<SavingsGoalEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSavingsGoalEntity = object : EntityInsertAdapter<SavingsGoalEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `savings_goals` (`id`,`name`,`targetAmount`,`savedAmount`,`createdAt`) VALUES (nullif(?, 0),?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SavingsGoalEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.targetAmount)
        statement.bindLong(4, entity.savedAmount)
        statement.bindLong(5, entity.createdAt)
      }
    }
    this.__deleteAdapterOfSavingsGoalEntity = object : EntityDeleteOrUpdateAdapter<SavingsGoalEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `savings_goals` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SavingsGoalEntity) {
        statement.bindLong(1, entity.id)
      }
    }
    this.__updateAdapterOfSavingsGoalEntity = object : EntityDeleteOrUpdateAdapter<SavingsGoalEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `savings_goals` SET `id` = ?,`name` = ?,`targetAmount` = ?,`savedAmount` = ?,`createdAt` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SavingsGoalEntity) {
        statement.bindLong(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindLong(3, entity.targetAmount)
        statement.bindLong(4, entity.savedAmount)
        statement.bindLong(5, entity.createdAt)
        statement.bindLong(6, entity.id)
      }
    }
  }

  public override suspend fun insert(goal: SavingsGoalEntity): Long = performSuspending(__db, false, true) { _connection ->
    val _result: Long = __insertAdapterOfSavingsGoalEntity.insertAndReturnId(_connection, goal)
    _result
  }

  public override suspend fun delete(goal: SavingsGoalEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfSavingsGoalEntity.handle(_connection, goal)
  }

  public override suspend fun update(goal: SavingsGoalEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfSavingsGoalEntity.handle(_connection, goal)
  }

  public override fun observeAll(): Flow<List<SavingsGoalEntity>> {
    val _sql: String = "SELECT * FROM savings_goals ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("savings_goals")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfTargetAmount: Int = getColumnIndexOrThrow(_stmt, "targetAmount")
        val _columnIndexOfSavedAmount: Int = getColumnIndexOrThrow(_stmt, "savedAmount")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<SavingsGoalEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavingsGoalEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpTargetAmount: Long
          _tmpTargetAmount = _stmt.getLong(_columnIndexOfTargetAmount)
          val _tmpSavedAmount: Long
          _tmpSavedAmount = _stmt.getLong(_columnIndexOfSavedAmount)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = SavingsGoalEntity(_tmpId,_tmpName,_tmpTargetAmount,_tmpSavedAmount,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllOnce(): List<SavingsGoalEntity> {
    val _sql: String = "SELECT * FROM savings_goals"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfTargetAmount: Int = getColumnIndexOrThrow(_stmt, "targetAmount")
        val _columnIndexOfSavedAmount: Int = getColumnIndexOrThrow(_stmt, "savedAmount")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<SavingsGoalEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SavingsGoalEntity
          val _tmpId: Long
          _tmpId = _stmt.getLong(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpTargetAmount: Long
          _tmpTargetAmount = _stmt.getLong(_columnIndexOfTargetAmount)
          val _tmpSavedAmount: Long
          _tmpSavedAmount = _stmt.getLong(_columnIndexOfSavedAmount)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = SavingsGoalEntity(_tmpId,_tmpName,_tmpTargetAmount,_tmpSavedAmount,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteAll() {
    val _sql: String = "DELETE FROM savings_goals"
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
