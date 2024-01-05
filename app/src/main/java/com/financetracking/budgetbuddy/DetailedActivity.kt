package com.financetracking.budgetbuddy

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.room.Room
import com.financetracking.budgetbuddy.databinding.ActivityDetailedBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import android.view.inputmethod.InputMethodManager
import android.content.Context
import com.financetracking.budgetbuddy.models.Transaction
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailedActivity : AppCompatActivity() {
    private lateinit var transaction : Transaction
    private lateinit var selectedDate: Calendar
    private lateinit var binding: ActivityDetailedBinding
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed)

        binding = ActivityDetailedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transaction = intent.getSerializableExtra("transaction") as Transaction

        binding.labelInput.setText(transaction.label)

        binding.descriptionInput.setText(transaction.description)

        if (transaction.amount < 0){
            binding.switcher.isChecked = false
            binding.amountInput.setText((transaction.amount * (-1)).toString())
            binding.tvText.visibility = View.GONE
            binding.tvTextTaken.visibility = View.VISIBLE
        } else{
            binding.amountInput.setText(transaction.amount.toString())
            binding.tvText.visibility = View.VISIBLE
            binding.tvTextTaken.visibility = View.GONE
        }

        binding.switcher.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.tvText.visibility = View.VISIBLE
                binding.tvTextTaken.visibility = View.GONE
            } else {
                binding.tvText.visibility = View.GONE
                binding.tvTextTaken.visibility = View.VISIBLE
            }
            binding.updateBtn.visibility = View.VISIBLE
        }

        selectedDate = Calendar.getInstance()
        selectedDate.timeInMillis = transaction.date

        binding.dateInput.setOnClickListener {
            showDatePickerDialog()
        }

        updateDate(selectedDate)

        binding.rootView.setOnClickListener {
            this.window.decorView.clearFocus()

            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }

        binding.labelInput.addTextChangedListener {
            binding.updateBtn.visibility = View.VISIBLE
            if(it!!.count() > 0)
                binding.labelLayout.error = null
        }

        binding.amountInput.addTextChangedListener {
            binding.updateBtn.visibility = View.VISIBLE
            if(it!!.count() > 0)
                binding.amountLayout.error = null
        }

        binding.descriptionInput.addTextChangedListener {
            binding.updateBtn.visibility = View.VISIBLE
        }
        binding.dateInput.addTextChangedListener {
            binding.updateBtn.visibility = View.VISIBLE
        }
        binding.tvText.addTextChangedListener {
            binding.updateBtn.visibility = View.VISIBLE
        }


            binding.updateBtn.setOnClickListener {
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
                val transaction  = Transaction(transaction.id, label, amount, description, convertDateToNumericValue(selectedDate))
                update(transaction)
            }
        }

        binding.closeBtn.setOnClickListener {
            finish()
        }
    }

    private fun update(transaction: Transaction){
        val db = Room.databaseBuilder(this,
            AppDatabase::class.java,
            "transactions").build()

        GlobalScope.launch {
            db.transactionDao().update(transaction)
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
    }

    private fun convertDateToNumericValue(calendar: Calendar): Long {
        return calendar.timeInMillis
    }
}