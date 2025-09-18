package com.example.expensetrack.export

import android.content.Context
import com.example.expensetrack.data.entity.Account
import com.example.expensetrack.data.entity.AccountType
import com.example.expensetrack.data.entity.Transaction
import com.example.expensetrack.data.entity.TransactionType
import java.io.File
import java.io.FileWriter
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class QifExporter(private val context: Context) {

    private val dateFormats = mapOf(
        "MM/DD/YYYY" to DateTimeFormatter.ofPattern("MM/dd/yyyy"),
        "DD/MM/YYYY" to DateTimeFormatter.ofPattern("dd/MM/yyyy")
    )

    data class ExportSettings(
        val dateFormat: String = "MM/DD/YYYY",
        val separateFilePerAccount: Boolean = true,
        val includeCleared: Boolean = true,
        val startDate: LocalDateTime? = null,
        val endDate: LocalDateTime? = null
    )

    /**
     * Export account(s) to QIF format
     */
    suspend fun exportToQif(
        accounts: List<Account>,
        allTransactions: Map<Long, List<Transaction>>,
        settings: ExportSettings = ExportSettings()
    ): List<File> {
        val exportedFiles = mutableListOf<File>()

        if (settings.separateFilePerAccount) {
            // Export each account to separate QIF file
            accounts.forEach { account ->
                val transactions = allTransactions[account.id] ?: emptyList()
                val filteredTransactions = filterTransactionsByDate(transactions, settings)

                if (filteredTransactions.isNotEmpty() || account.openingBalance != BigDecimal.ZERO) {
                    val file = createQifFile(account, filteredTransactions, settings)
                    exportedFiles.add(file)
                }
            }
        } else {
            // Export all accounts to single QIF file
            val file = createMultiAccountQifFile(accounts, allTransactions, settings)
            exportedFiles.add(file)
        }

        return exportedFiles
    }

    /**
     * Create QIF file for single account
     */
    private fun createQifFile(
        account: Account,
        transactions: List<Transaction>,
        settings: ExportSettings
    ): File {
        val fileName = "${sanitizeFileName(account.name)}.qif"
        val file = File(context.getExternalFilesDir("exports"), fileName)

        FileWriter(file).use { writer ->
            // Write account header
            writer.write(getAccountHeader(account.type))
            writer.write("\n")

            // Write opening balance transaction if non-zero
            if (account.openingBalance != BigDecimal.ZERO) {
                writeOpeningBalanceTransaction(writer, account, settings)
            }

            // Write transactions
            transactions.forEach { transaction ->
                writeTransaction(writer, transaction, settings)
            }
        }

        return file
    }

    /**
     * Create QIF file with multiple accounts
     */
    private fun createMultiAccountQifFile(
        accounts: List<Account>,
        allTransactions: Map<Long, List<Transaction>>,
        settings: ExportSettings
    ): File {
        val fileName = "ExpenseTrack_Export.qif"
        val file = File(context.getExternalFilesDir("exports"), fileName)

        FileWriter(file).use { writer ->
            accounts.forEach { account ->
                val transactions = allTransactions[account.id] ?: emptyList()
                val filteredTransactions = filterTransactionsByDate(transactions, settings)

                // Write account section header
                writer.write(getAccountHeader(account.type))
                writer.write("\n")

                // Write opening balance if non-zero
                if (account.openingBalance != BigDecimal.ZERO) {
                    writeOpeningBalanceTransaction(writer, account, settings)
                }

                // Write transactions
                filteredTransactions.forEach { transaction ->
                    writeTransaction(writer, transaction, settings)
                }

                writer.write("\n") // Separator between accounts
            }
        }

        return file
    }

    /**
     * Get QIF account type header
     */
    private fun getAccountHeader(accountType: AccountType): String {
        return when (accountType) {
            AccountType.CASH -> "!Type:Cash"
            AccountType.BANK -> "!Type:Bank"
            AccountType.CREDIT_CARD -> "!Type:CCard"
        }
    }

    /**
     * Write opening balance transaction
     */
    private fun writeOpeningBalanceTransaction(
        writer: FileWriter,
        account: Account,
        settings: ExportSettings
    ) {
        val formatter = dateFormats[settings.dateFormat]
            ?: DateTimeFormatter.ofPattern("MM/dd/yyyy")

        // Use account creation date or current date
        val openingDate = LocalDateTime.now().format(formatter)

        writer.write("D$openingDate\n")
        writer.write("T${formatAmount(account.openingBalance)}\n")
        writer.write("POpening Balance\n")
        writer.write("L[${account.name}]\n")
        writer.write("MOpening Balance\n")
        writer.write("^\n")
    }

    /**
     * Write individual transaction to QIF format
     */
    private fun writeTransaction(
        writer: FileWriter,
        transaction: Transaction,
        settings: ExportSettings
    ) {
        val formatter = dateFormats[settings.dateFormat]
            ?: DateTimeFormatter.ofPattern("MM/dd/yyyy")

        // D - Date
        writer.write("D${transaction.date.format(formatter)}\n")

        // T - Amount (negative for payments/expenses, positive for deposits/income)
        writer.write("T${formatAmount(transaction.amount)}\n")

        // P - Payee
        if (transaction.payee.isNotEmpty()) {
            writer.write("P${transaction.payee}\n")
        }

        // M - Memo
        if (transaction.memo.isNotEmpty()) {
            writer.write("M${transaction.memo}\n")
        }

        // L - Category or Account (for transfers)
        if (transaction.type == TransactionType.TRANSFER) {
            // For transfers, use [Account Name] format
            writer.write("L[Transfer]\n") // Simplified for now
        } else if (transaction.category.isNotEmpty()) {
            writer.write("L${transaction.category}\n")
        }

        // C - Cleared status
        if (transaction.clearedFlag) {
            writer.write("C*\n") // * means reconciled, X means cleared
        }

        // N - Check/Reference Number
        if (transaction.checkNumber.isNotEmpty()) {
            writer.write("N${transaction.checkNumber}\n")
        }

        // ^ - End of record
        writer.write("^\n")
    }

    /**
     * Format amount for QIF (no currency symbol, proper decimal places)
     */
    private fun formatAmount(amount: BigDecimal): String {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString()
    }

    /**
     * Filter transactions by date range
     */
    private fun filterTransactionsByDate(
        transactions: List<Transaction>,
        settings: ExportSettings
    ): List<Transaction> {
        var filtered = transactions

        settings.startDate?.let { start ->
            filtered = filtered.filter { it.date >= start }
        }

        settings.endDate?.let { end ->
            filtered = filtered.filter { it.date <= end }
        }

        return filtered.sortedBy { it.date }
    }

    /**
     * Sanitize filename to remove invalid characters
     */
    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^a-zA-Z0-9\\s]"), "")
            .replace(Regex("\\s+"), "_")
            .take(50) // Limit filename length
    }
}
