package com.example.expensetrack.data.dao

import kotlinx.coroutines.flow.Flow
//import com.example.expensetrack.data.entity.Transaction
import java.time.LocalDateTime
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction   // <-- this one is the Room annotation
import com.example.expensetrack.data.entity.Transaction as TxEntity

@Dao
interface TransactionDao {

    // ✅ Flow methods (reactive, no suspend needed)
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TxEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY date DESC")
    fun getTransactionsByAccount(accountId: Long): Flow<List<TxEntity>>

    // ✅ Suspend methods (for write operations)
    @Insert
    suspend fun insert(transaction: TxEntity): Long

    @Update
    suspend fun update(transaction: TxEntity)

    @Delete
    suspend fun delete(transaction: TxEntity)

    @Query("SELECT * FROM transactions WHERE id = :transactionId")
    suspend fun getTransactionById(transactionId: Long): TxEntity?

    // ✅ Keep only essential methods, remove unused ones
    @Query("SELECT COUNT(*) FROM transactions WHERE accountId = :accountId")
    suspend fun getTransactionCountByAccount(accountId: Long): Int

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Transaction
    suspend fun createTransfer(
        fromAccountId: Long,
        toAccountId: Long,
        amountAbs: java.math.BigDecimal,
        whenAt: java.time.LocalDateTime,
        memo: String?
    ) {
        // make a pair with a shared transferGroupId
        val gid = java.util.UUID.randomUUID().toString()

        // debit from source (negative)
        insert(
            com.example.expensetrack.data.entity.Transaction(
                accountId = fromAccountId,
                amount = amountAbs.negate(),
                payee = "Transfer to $toAccountId",
                category = "TRANSFER",
                memo = memo ?: "",
                date = whenAt,
                type = com.example.expensetrack.data.entity.TransactionType.TRANSFER,
                checkNumber = "",
                categoryId = null,
                transferGroupId = gid
            )
        )

        // credit to destination (positive)
        insert(
            com.example.expensetrack.data.entity.Transaction(
                accountId = toAccountId,
                amount = amountAbs,
                payee = "Transfer from $fromAccountId",
                category = "TRANSFER",
                memo = memo ?: "",
                date = whenAt,
                type = com.example.expensetrack.data.entity.TransactionType.TRANSFER,
                checkNumber = "",
                categoryId = null,
                transferGroupId = gid
            )
        )
    }
}
