package com.example.glassfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Mục tiêu tiết kiệm. */
@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val targetAmount: Long,
    val savedAmount: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
)
