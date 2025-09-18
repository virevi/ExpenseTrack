package com.example.expensetrack.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.example.expensetrack.data.entity.Account
import com.example.expensetrack.data.entity.Transaction
import com.example.expensetrack.data.entity.Converters
import com.example.expensetrack.data.entity.DateTimeConverters
import com.example.expensetrack.data.entity.AccountType
import com.example.expensetrack.data.dao.AccountDao
import com.example.expensetrack.data.dao.TransactionDao
import com.example.expensetrack.data.dao.CategoryDao
import com.example.expensetrack.data.entity.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import com.example.expensetrack.data.SeedData


@Database(
    entities = [
        com.example.expensetrack.data.entity.Account::class,
        com.example.expensetrack.data.entity.Transaction::class,
        com.example.expensetrack.data.entity.Category::class
    ],
    version = 2, // bump from previous
    exportSchema = true
)
@TypeConverters(Converters::class, DateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): com.example.expensetrack.data.dao.AccountDao
    abstract fun transactionDao(): com.example.expensetrack.data.dao.TransactionDao
    abstract fun categoryDao(): com.example.expensetrack.data.dao.CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "expense_track_database"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate database with default accounts
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    prepopulateDatabase(database)
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun prepopulateDatabase(database: AppDatabase) {
            val categoryDao = database.categoryDao()
            val accountDao = database.accountDao()

            // --- Insert Expense Categories & Subcategories
            // Insert parent categories and track their IDs
            val parentIds = mutableMapOf<String, Long>()
            val parentNames = SeedData.expenseCategories.map { it.name }.distinct()
            parentNames.forEach { name ->
                val id = categoryDao.insert(Category(name = name, type = "EXPENSE", parentId = null))
                parentIds[name] = id
            }
            // Insert subcategories
            SeedData.expenseSubCategories.forEach { (parent, subcat) ->
                val parentId = parentIds[parent]
                if (parentId != null) {
                    categoryDao.insert(Category(name = subcat, type = "EXPENSE", parentId = parentId))
                }
            }

            // --- Insert Income Categories
            SeedData.incomeCategories.forEach { categoryDao.insert(it) }

            // --- Insert Accounts
            SeedData.accounts.forEach { accountDao.insert(it) }
        }

    }
}
