package com.example.expensetrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val openingBalance: BigDecimal = BigDecimal.ZERO,
    val currency: String = "INR",
    val notes: String = "",
    val isActive: Boolean = true
)

enum class AccountType {
    CASH,
    BANK,
    CREDIT_CARD
}
