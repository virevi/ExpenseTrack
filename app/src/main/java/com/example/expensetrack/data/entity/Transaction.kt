package com.example.expensetrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["accountId"])]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val date: LocalDateTime,
    val amount: BigDecimal,
    val payee: String = "",
    val category: String = "",
    val subcategory: String = "",
    val memo: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val linkedTransferId: Long? = null,
    val clearedFlag: Boolean = false,
    val checkNumber: String = "",
    val categoryId: Long? = null,
    val transferGroupId: String? = null
)

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}
