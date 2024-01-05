package com.financetracking.budgetbuddy

import android.app.DatePickerDialog
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.financetracking.budgetbuddy.databinding.ActivityAddTransactionBinding
import com.financetracking.budgetbuddy.models.Transaction
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var selectedDate: Calendar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedDate = Calendar.getInstance()

        binding.dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        updateDate(selectedDate)

        binding.labelInput.addTextChangedListener {
            if(it!!.count() > 0)
                binding.labelLayout.error = null
        }

        binding.amountInput.addTextChangedListener {
            if(it!!.count() > 0)
                binding.amountLayout.error = null
        }

        binding.addTransactionBtn.setOnClickListener {
            val label = binding.labelInput.text.toString()
            val description = binding.descriptionInput.text.toString()
            var amount = binding.amountInput.text.toString().toDoubleOrNull()

            if (!binding.switcher.isChecked){
                if (amount != null) {
                    amount = amount * -1
                }
            }

            if(label.isEmpty())
                binding.labelLayout.error = getString(R.string.error)

            else if(amount == null)
                binding.amountLayout.error = getString(R.string.error)
            else {
                val transaction = Transaction(0, label, amount, description, convertDateToNumericValue(selectedDate))
                insert(transaction)
            }
        }

        binding.closeBtn.setOnClickListener {
            finish()
        }

        binding.switcher.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvText.visibility = View.VISIBLE
                binding.tvTextTaken.visibility = View.GONE
            } else {
                binding.tvText.visibility = View.GONE
                binding.tvTextTaken.visibility = View.VISIBLE
            }
        }

        binding.rootView.setOnClickListener {
            this.window.decorView.clearFocus()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    private fun insert(transaction: Transaction){
        val db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        GlobalScope.launch {
            db.transactionDao().insertAll(transaction)
            finish()
        }
    }

    private fun showDatePickerDialog() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                // Update the selectedDate when a new date is selected
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
                updateDate(selectedDate)
            },
            year, month, day
        )

        datePickerDialog.show()
    }

    private fun updateDate(calendar: Calendar) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formattedDate = dateFormat.format(calendar.time)
        binding.dateInput.setText(formattedDate)
        binding.dateLayout.error = null

        val numericValue = convertDateToNumericValue(calendar)
         }

    private fun convertDateToNumericValue(calendar: Calendar): Long {
        return calendar.timeInMillis
    }
}