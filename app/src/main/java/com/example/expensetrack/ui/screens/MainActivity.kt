package com.example.expensetrack.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.expensetrack.data.database.AppDatabase
import com.example.expensetrack.data.repository.AppRepository
import com.example.expensetrack.export.ExportService
import com.example.expensetrack.ui.theme.ExpenseTrackTheme
import com.example.expensetrack.viewmodel.AccountViewModel
import com.example.expensetrack.viewmodel.AccountViewModelFactory
import com.example.expensetrack.viewmodel.TransactionViewModel
import com.example.expensetrack.viewmodel.TransactionViewModelFactory
import com.example.expensetrack.viewmodel.CategoryViewModel
import com.example.expensetrack.viewmodel.CategoryViewModelFactory

class MainActivity : ComponentActivity() {

    private val database by lazy { AppDatabase.getDatabase(this) }

    // Pass all three DAOs to the repository (account, transaction, category)
    private val repository by lazy {
        AppRepository(
            database.accountDao(),
            database.transactionDao(),
            database.categoryDao()           // NEW
        )
    }

    private val exportService by lazy { ExportService(this, repository) }

    private val accountViewModel: AccountViewModel by viewModels {
        AccountViewModelFactory(repository)
    }

    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(repository, exportService)
    }

    // NEW: Provide CategoryViewModel
    private val categoryViewModel: CategoryViewModel by viewModels {
        CategoryViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExpenseTrackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DatabaseIntegratedApp(
                        accountViewModel = accountViewModel,
                        transactionViewModel = transactionViewModel,
                        categoryViewModel = categoryViewModel      // NEW
                    )
                }
            }
        }
    }
}
