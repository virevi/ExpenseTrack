package com.example.expensetrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.expensetrack.data.database.AppDatabase
import com.example.expensetrack.data.repository.AppRepository
import com.example.expensetrack.export.ExportService
import com.example.expensetrack.ui.screens.DatabaseIntegratedApp
import com.example.expensetrack.ui.theme.ExpenseTrackTheme
import com.example.expensetrack.viewmodel.AccountViewModel
import com.example.expensetrack.viewmodel.AccountViewModelFactory
import com.example.expensetrack.viewmodel.TransactionViewModel
import com.example.expensetrack.viewmodel.TransactionViewModelFactory
import com.example.expensetrack.viewmodel.CategoryViewModel
import com.example.expensetrack.viewmodel.CategoryViewModelFactory
import com.example.expensetrack.data.SeedData
import kotlinx.coroutines.*
import androidx.lifecycle.lifecycleScope
import com.example.expensetrack.data.entity.Category

class MainActivity : ComponentActivity() {

    private val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "expense_track_database"
        )
            // .fallbackToDestructiveMigration() // dev only
            .build()
    }

    private val repository by lazy {
        AppRepository(database.accountDao(), database.transactionDao(), database.categoryDao())
    }

    private val exportService by lazy {
        ExportService(this, repository)
    }

    private val accountViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(repository)
    }

    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(repository, exportService)
    }

    private val categoryViewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
        if (!prefs.getBoolean("has_seeded", false)) {
            // Run on IO dispatcher since database is suspend
            lifecycleScope.launch(Dispatchers.IO) {
                // Insert parent categories and keep a map of name to ID
                val categoryDao = database.categoryDao()
                val parentIds = mutableMapOf<String, Long>()

                SeedData.expenseCategories.forEach { cat ->
                    val id = categoryDao.insert(cat)
                    parentIds[cat.name] = id
                }

                // Insert subcategories with correct parentId
                SeedData.expenseSubCategories.forEach { (parent, subcat) ->
                    val parentId = parentIds[parent]
                    if (parentId != null) {
                        categoryDao.insert(Category(name = subcat, type = "EXPENSE", parentId = parentId))
                    }
                }

                // Insert income categories
                SeedData.incomeCategories.forEach { cat ->
                    categoryDao.insert(cat)
                }

                // Insert accounts
                val accountDao = database.accountDao()
                SeedData.accounts.forEach { acc -> accountDao.insert(acc) }

                prefs.edit().putBoolean("has_seeded", true).apply()
            }
        }
        setContent {
            ExpenseTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DatabaseIntegratedApp(
                        accountViewModel = accountViewModel,
                        transactionViewModel = transactionViewModel,
                        categoryViewModel = categoryViewModel
                    )
                }
            }
        }
    }
}
