package com.financetracking.budgetbuddy

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import com.financetracking.budgetbuddy.databinding.ActivitySettingsBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Locale

class SettingsActivity : AppCompatActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        navView.selectedItemId = R.id.navigation_settings

        navView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.navigation_settings -> {
                    true
                }
                R.id.navigation_accounts -> {
                    startActivity(Intent(this, AccountsActivity::class.java))
                    true
                }else -> false
            }
        }

        val languageOptions = resources.getStringArray(R.array.language_options)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.language_options,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        binding.spinner.adapter = adapter
        val currentLanguage = Locale.getDefault().language

        if (currentLanguage == "uk") {
            binding.spinner.setSelection(1)
        }
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                when (selectedItem) {
                    languageOptions[1] -> {
                        setLocale("uk")
                    }
                    languageOptions[0] -> {
                        setLocale("en")
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        sharedPreferences = getPreferences(MODE_PRIVATE)

        val currentTheme = AppCompatDelegate.getDefaultNightMode()

        val isDarkMode = currentTheme == AppCompatDelegate.MODE_NIGHT_YES

        val darkModeSwitch = binding.darkModeSwitch
        darkModeSwitch.isChecked = isDarkMode

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            setDarkMode(isChecked)
        }
    }

    private fun setDarkMode(enabled: Boolean) {
        val mode = if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)

        // Save the dark mode state
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()

        // Recreate the activity only if the dark mode state has changed
        if (isDarkModeEnabled() != enabled) {
            recreate()
        }
    }

    private fun isDarkModeEnabled(): Boolean {
        // Retrieve the dark mode state from SharedPreferences
        return sharedPreferences.getBoolean("dark_mode", false)
    }


    private fun setLocale(languageCode: String) {
        val locale = Locale(languageCode)
        val config = Configuration(resources.configuration)

        // Check if the current locale is different from the desired locale
        if (config.locale.language != locale.language) {
            // Set the new locale
            config.setLocale(locale)
            resources.updateConfiguration(config, resources.displayMetrics)

            // Restart the activity to apply changes
            recreate()
        }
    }
}