package com.example.expensetrack.util

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import java.math.RoundingMode

object CurrencyUtils {

    private val indianFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun formatCurrency(amount: BigDecimal, currencyCode: String = "INR"): String {
        return when (currencyCode) {
            "INR" -> indianFormat.format(amount)
            else -> "$currencyCode ${amount.setScale(2, RoundingMode.HALF_UP)}"
        }
    }

    fun formatAmount(amount: BigDecimal): String {
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString()
    }

    fun parseCurrency(amountString: String): BigDecimal? {
        return try {
            // Remove currency symbols and parse
            val cleanAmount = amountString.replace(Regex("[^\\d.-]"), "")
            BigDecimal(cleanAmount)
        } catch (e: NumberFormatException) {
            null
        }
    }

    fun isPositiveAmount(amount: BigDecimal): Boolean {
        return amount > BigDecimal.ZERO
    }

    fun isNegativeAmount(amount: BigDecimal): Boolean {
        return amount < BigDecimal.ZERO
    }
}
