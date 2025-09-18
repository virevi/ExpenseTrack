package com.example.expensetrack.data

import com.example.expensetrack.data.entity.Account
import com.example.expensetrack.data.entity.AccountType
import com.example.expensetrack.data.entity.Category

object SeedData {
    // Accounts (from accounts_list.csv)
    val accounts = listOf(
        Account(name = "Cash INR", type = AccountType.CASH),
        Account(name = "HDFC BANK", type = AccountType.BANK),
        Account(name = "HDFC Rekha", type = AccountType.BANK),
        Account(name = "ICICI Rekha", type = AccountType.BANK),
        Account(name = "ICICI Wealth", type = AccountType.BANK),
        Account(name = "Rekha's Cash", type = AccountType.CASH),
        Account(name = "SBI Resident", type = AccountType.BANK),
        Account(name = "Soft Loans INR", type = AccountType.BANK),
        Account(name = "UBI Resident", type = AccountType.BANK)
        // ...add others if present in your csv
    )

    // Expense categories & subcategories (from expense_categories.csv)
    val expenseCategories = listOf(
        // Category(name, type, parentId) -- parentId will be linked after parent is inserted
        // List of parent categories -- only unique category names:
        "Auto", "Clothing", "Education", "Groceries", "House", "Investment", "Irregular Expenses",
        "Loans", "Medical", "Money Transfers", "Other Exp", "Reimbursable Expenses", "Utilities", "Vacation"
    ).map {
        Category(name = it, type = "EXPENSE", parentId = null)
    }

    val expenseSubCategories = listOf(
        // parentCategory, subcategory
        Pair("Auto", "Accessories"),
        Pair("Auto", "Fuel"),
        Pair("Auto", "Insurance"),
        Pair("Auto", "NewCar"),
        Pair("Auto", "Service"),
        Pair("Auto", "Taxi"),
        Pair("Clothing", "Clothes"),
        Pair("Clothing", "Laundry"),
        Pair("Education", "Fees"),
        Pair("Education", "Other Exp"),
        Pair("Education", "Stationery"),
        Pair("Education", "Transport"),
        Pair("Groceries", "Fruits"),
        Pair("Groceries", "Meat"),
        Pair("Groceries", "Milk"),
        Pair("Groceries", "Provisions"),
        Pair("Groceries", "Toileteries"),
        Pair("Groceries", "Vegetables"),
        Pair("House", "Furnishing"),
        Pair("House", "House Expense"),
        Pair("House", "Maintenance"),
        Pair("House", "Rent"),
        Pair("Investment", "Learning"),
        Pair("Investment", "Other"),
        Pair("Irregular Expenses", "Adjust"),
        Pair("Irregular Expenses", "Bank Charges"),
        Pair("Irregular Expenses", "Digital Household"),
        Pair("Irregular Expenses", "Electronics"),
        Pair("Irregular Expenses", "Gifts Given"),
        Pair("Irregular Expenses", "IncomeTAX"),
        Pair("Irregular Expenses", "Misc"),
        Pair("Irregular Expenses", "Other Books"),
        Pair("Irregular Expenses", "Postage"),
        Pair("Irregular Expenses", "Toys"),
        Pair("Loans", "Interest"),
        Pair("Loans", "Principal"),
        Pair("Medical", "Insurance"),
        Pair("Medical", "Medicine"),
        Pair("Medical", "Others"),
        Pair("Other Exp", "Books @ Mag"),
        Pair("Other Exp", "Cosmetics"),
        Pair("Other Exp", "Dining"),
        Pair("Other Exp", "Household"),
        Pair("Other Exp", "Insurance"),
        Pair("Other Exp", "Kitchen"),
        Pair("Other Exp", "Other Misc"),
        Pair("Other Exp", "Recreation"),
        Pair("Reimbursable Expenses", "Official"),
        Pair("Reimbursable Expenses", "Personal"),
        Pair("Utilities", "Drinking Water"),
        Pair("Utilities", "Electricity"),
        Pair("Utilities", "Gas"),
        Pair("Utilities", "Internet"),
        Pair("Utilities", "Telephone"),
        Pair("Utilities", "Water"),
        Pair("Vacation", "GiveAways"),
        Pair("Vacation", "Lodging"),
        Pair("Vacation", "Other Exp"),
        Pair("Vacation", "Travel")
    )

    // Income categories (from income_categories.csv)
    val incomeCategories = listOf(
        "Bonus", "Dividend", "Interest Inc", "Other Inc", "Over Time", "Salary"
    ).map {
        Category(name = it, type = "INCOME", parentId = null)
    }
}
