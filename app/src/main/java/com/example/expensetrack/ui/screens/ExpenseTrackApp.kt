package com.example.expensetrack.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.expensetrack.viewmodel.AccountViewModel
import com.example.expensetrack.viewmodel.TransactionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackApp(
    accountViewModel: AccountViewModel,
    transactionViewModel: TransactionViewModel
) {
    val navController = rememberNavController()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ExpenseTrack") },
                actions = {
                    IconButton(
                        onClick = {
                            // Export functionality
                            val accountIds = accountViewModel.accounts.value.map { it.id }
                            transactionViewModel.exportQif(accountIds)
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Export")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                    label = { Text("Accounts") },
                    selected = currentDestination?.hierarchy?.any { it.route == "accounts" } == true,
                    onClick = {
                        navController.navigate("accounts") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = null) },
                    label = { Text("Transactions") },
                    selected = currentDestination?.hierarchy?.any { it.route == "transactions" } == true,
                    onClick = {
                        navController.navigate("transactions") {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "accounts",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("accounts") {
                AccountsScreen(
                    viewModel = accountViewModel,
                    onNavigateToTransactions = { accountId ->
                        transactionViewModel.selectAccount(accountId)
                        navController.navigate("transactions")
                    }
                )
            }
            composable("transactions") {
                TransactionsScreen(
                    viewModel = transactionViewModel
                )
            }
        }
    }
}
