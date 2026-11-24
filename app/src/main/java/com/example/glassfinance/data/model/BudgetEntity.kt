package com.example.glassfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Ngân sách chi tiêu theo tháng cho một danh mục. */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val category: String,
    val monthlyLimit: Long
)
