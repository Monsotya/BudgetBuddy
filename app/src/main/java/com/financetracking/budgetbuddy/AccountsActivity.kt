package com.financetracking.budgetbuddy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.room.Room
import com.financetracking.budgetbuddy.databinding.ActivityAccountsBinding
import com.financetracking.budgetbuddy.models.Transaction
import com.financetracking.budgetbuddy.models.TransactionAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AccountsActivity : AppCompatActivity() {
    private lateinit var transactions : List<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var db : AppDatabase
    private lateinit var binding: ActivityAccountsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAccountsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        transactions = arrayListOf()

        transactionAdapter = TransactionAdapter(transactions)

        db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        fetchAll()

        navView.selectedItemId = com.financetracking.budgetbuddy.R.id.navigation_accounts

        navView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                com.financetracking.budgetbuddy.R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                com.financetracking.budgetbuddy.R.id.navigation_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                com.financetracking.budgetbuddy.R.id.navigation_accounts -> {
                    true
                }else -> false
            }
        }

        val timeOptions = resources.getStringArray(R.array.time_options)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.timePeriodSpinner.adapter = adapter

        binding.timePeriodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                val selectedTimePeriod = timeOptions[position]
                fetchTransactionsForTimePeriod(selectedTimePeriod)
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun fetchTransactionsForTimePeriod(timePeriod: String) {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val timeOptions = resources.getStringArray(R.array.time_options)
        GlobalScope.launch {
            transactions = when (timePeriod) {
                timeOptions[0] -> db.transactionDao().getAll()
                timeOptions[1] -> {
                    val currentYear = calendar.get(Calendar.YEAR)
                    val startDate = dateFormat.parse("$currentYear-01-01")?.time ?: 0
                    val endDate = dateFormat.parse("$currentYear-12-31")?.time ?: Long.MAX_VALUE
                    db.transactionDao().getTransactionsForTimePeriod(startDate, endDate)
                }
                timeOptions[2] -> {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    val startDate = calendar.timeInMillis
                    calendar.add(Calendar.MONTH, 1)
                    calendar.add(Calendar.DATE, -1)
                    val endDate = calendar.timeInMillis
                    db.transactionDao().getTransactionsForTimePeriod(startDate, endDate)
                }
                timeOptions[3] -> {
                    val firstDayOfWeek = calendar.firstDayOfWeek
                    calendar.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
                    val startDate = calendar.timeInMillis
                    calendar.add(Calendar.DATE, 6)
                    val endDate = calendar.timeInMillis
                    db.transactionDao().getTransactionsForTimePeriod(startDate, endDate)
                }
                else -> emptyList()
            }
            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }


    private fun fetchAll(){
        GlobalScope.launch {
            transactions = db.transactionDao().getAll()

            runOnUiThread {
                updateDashboard()
                transactionAdapter.setData(transactions)
            }
        }
    }

    private fun updateDashboard(){
        val totalAmount: Double = transactions.map { it.amount }.sum()
        val incomeAmount: Double = transactions.filter { it.amount > 0 }.map{ it.amount }.sum()
        val expenseAmount: Double = transactions.filter { it.amount < 0 }.map{ it.amount }.sum()

        binding.balance.text = "$ %.2f".format(totalAmount)
        binding.income.text = "$ %.2f".format(incomeAmount)
        binding.expense.text = "$ %.2f".format(expenseAmount)
    }
}