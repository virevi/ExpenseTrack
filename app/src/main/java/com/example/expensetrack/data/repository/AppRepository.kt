package com.example.expensetrack.data.repository

import kotlinx.coroutines.flow.Flow
import com.example.expensetrack.data.dao.AccountDao
import com.example.expensetrack.data.dao.TransactionDao
import com.example.expensetrack.data.dao.CategoryDao
import com.example.expensetrack.data.entity.Account
import com.example.expensetrack.data.entity.Transaction
import com.example.expensetrack.data.entity.Category
import java.math.BigDecimal
import java.time.LocalDateTime

class AppRepository(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {

    // Accounts
    fun getAllAccounts(): Flow<List<Account>> = accountDao.getAllAccounts()
    suspend fun insertAccount(account: Account): Long = accountDao.insert(account)
    suspend fun updateAccount(account: Account) = accountDao.update(account)
    suspend fun deleteAccount(account: Account) = accountDao.delete(account)
    suspend fun getAccountById(accountId: Long): Account? = accountDao.getAccountById(accountId)

    // Transactions
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()
    fun getTransactionsByAccount(accountId: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByAccount(accountId)
    suspend fun insertTransaction(transaction: Transaction): Long = transactionDao.insert(transaction)
    suspend fun updateTransaction(transaction: Transaction) = transactionDao.update(transaction)
    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.delete(transaction)
    suspend fun deleteTransactionById(id: Long) = transactionDao.deleteById(id)

    // Categories
    fun getAllCategories() = categoryDao.getAll()
    fun getTopLevelCategories() = categoryDao.getTopLevel()
    fun getChildrenCategories(parentId: Long) = categoryDao.getChildren(parentId)
    suspend fun insertCategory(c: Category) = categoryDao.insert(c)
    suspend fun updateCategory(c: Category) = categoryDao.update(c)
    suspend fun deleteCategory(c: Category) = categoryDao.delete(c)

    // Transfers
    suspend fun createTransfer(
        fromAccountId: Long,
        toAccountId: Long,
        amountAbs: BigDecimal,
        whenAt: LocalDateTime,
        memo: String?
    ) = transactionDao.createTransfer(fromAccountId, toAccountId, amountAbs, whenAt, memo)
}
