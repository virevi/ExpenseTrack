package com.example.expensetrack.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import com.example.expensetrack.data.repository.AppRepository
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class ExportService(
    private val context: Context,
    private val repository: AppRepository
) {

    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    suspend fun exportToQif(accountIds: List<Long>) {
        withContext(Dispatchers.IO) {
            try {
                val qifContent = generateQifContent(accountIds)
                val file = saveQifFile(qifContent)
                shareQifFile(file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun generateQifContent(accountIds: List<Long>): String {
        val stringBuilder = StringBuilder()

        stringBuilder.append("!Type:Bank\n")

        for (accountId in accountIds) {
            val account = repository.getAccountById(accountId)
            // ✅ Use first() to get current snapshot from Flow
            val transactions = repository.getTransactionsByAccount(accountId).first()

            if (account != null) {
                stringBuilder.append("!Account\n")
                stringBuilder.append("N${account.name}\n")
                stringBuilder.append("TBank\n")
                stringBuilder.append("^\n")
                stringBuilder.append("!Type:Bank\n")

                for (transaction in transactions) {
                    stringBuilder.append("D${dateFormat.format(Date())}\n")
                    stringBuilder.append("T${transaction.amount}\n")
                    stringBuilder.append("P${transaction.payee}\n")
                    stringBuilder.append("L${transaction.category}\n")
                    if (transaction.memo.isNotBlank()) {
                        stringBuilder.append("M${transaction.memo}\n")
                    }
                    if (transaction.checkNumber.isNotBlank()) {
                        stringBuilder.append("N${transaction.checkNumber}\n")
                    }
                    stringBuilder.append("^\n")
                }
            }
        }

        return stringBuilder.toString()
    }

    private fun saveQifFile(content: String): File {
        val fileName = "ExpenseTrack_Export_${System.currentTimeMillis()}.qif"
        val file = File(context.getExternalFilesDir(null), fileName)

        FileWriter(file).use { writer ->
            writer.write(content)
        }

        return file
    }

    private fun shareQifFile(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/qif"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "ExpenseTrack Export")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooserIntent = Intent.createChooser(shareIntent, "Export QIF File")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
}
