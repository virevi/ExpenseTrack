package com.example.expensetrack.data.dao

import androidx.room.*
import com.example.expensetrack.data.entity.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts ORDER BY name ASC")
    fun getAllAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :accountId")
    suspend fun getAccountById(accountId: Long): Account?

    @Insert
    suspend fun insert(account: Account): Long

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("DELETE FROM accounts WHERE id = :id")
    suspend fun deleteAccountById(id: Long)

    @Query("UPDATE accounts SET isActive = 0 WHERE id = :id")
    suspend fun deactivateAccount(id: Long)

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun getAccountCount(): Int
}
