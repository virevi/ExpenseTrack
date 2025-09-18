package com.example.expensetrack.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index("parentId")]
)
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    // "INCOME" or "EXPENSE"
    val type: String,
    // null means top-level category
    val parentId: Long? = null
)
