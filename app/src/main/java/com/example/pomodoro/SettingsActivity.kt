package com.example.pomodoro

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import androidx.activity.viewModels
import com.example.pomodoro.databinding.ActivitySettingsBinding
import com.example.pomodoro.databinding.EditSessionBinding
import com.example.pomodoro.viewModel.MainViewModel
import com.google.android.material.snackbar.Snackbar
class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            WorkSessionLayout.setOnClickListener {
                showEditSessionDialog("WorkSession")
            }

            ShortBreakLayout.setOnClickListener {
                showEditSessionDialog("ShortBreak")
            }

            LongBreakLayout.setOnClickListener {
                showEditSessionDialog("LongBreak")
            }

            txtWorkMin.text = (MainHelper.getWorkSession() / (60 * 1000)).toString()
            txtShortMin.text = (MainHelper.getShortBreak() / (60 * 1000)).toString()
            txtLongMin.text = (MainHelper.getLongBreak() / (60 * 1000)).toString()
        }
    }

    val viewModel by viewModels<MainViewModel> {
        MainActivity.MainViewModelFactory(applicationContext) // Pass the application context here
    }
    private fun showEditSessionDialog(category: String) {
        val dialog = Dialog(this)
        val dialogBinding = EditSessionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val etTimeInput = dialogBinding.etTimeInput

        when (category) {
            "WorkSession" -> {
                etTimeInput.setText((MainHelper.getWorkSession() / (60 * 1000)).toString())
                dialogBinding.tvTimeCat.text = "Work Session"
            }
            "ShortBreak" -> {
                etTimeInput.setText((MainHelper.getShortBreak() / (60 * 1000)).toString())
                dialogBinding.tvTimeCat.text = "Short Break"
            }
            "LongBreak" -> {
                etTimeInput.setText((MainHelper.getLongBreak() / (60 * 1000)).toString())
                dialogBinding.tvTimeCat.text = "Long Break"
            }
        }

        dialogBinding.btnIncrease.setOnClickListener {
            val currentValue = etTimeInput.text.toString().toIntOrNull() ?: 0
            val newValue = currentValue + 1
            if (newValue <= 180) {
                etTimeInput.setText(newValue.toString())

                when (category) {
                    "WorkSession" -> MainHelper.setWorkSession(newValue)
                    "ShortBreak" -> MainHelper.setShortBreak(newValue)
                    "LongBreak" -> MainHelper.setLongBreak(newValue)
                }


                binding.txtWorkMin.text = (MainHelper.getWorkSession() / (60 * 1000)).toString()
                binding.txtShortMin.text = (MainHelper.getShortBreak() / (60 * 1000)).toString()
                binding.txtLongMin.text = (MainHelper.getLongBreak() / (60 * 1000)).toString()
            } else {
                Snackbar.make(dialogBinding.root, "Maximum value reached", Snackbar.LENGTH_SHORT).show()
            }
        }

        dialogBinding.btnDecrease.setOnClickListener {
            val currentValue = etTimeInput.text.toString().toIntOrNull() ?: 0
            val newValue = currentValue - 1
            if (newValue >= 1) { // Enforce minimum limit
                etTimeInput.setText(newValue.toString())

                when (category) {
                    "WorkSession" -> MainHelper.setWorkSession(newValue)
                    "ShortBreak" -> MainHelper.setShortBreak(newValue)
                    "LongBreak" -> MainHelper.setLongBreak(newValue)
                }

                binding.txtWorkMin.text = (MainHelper.getWorkSession() / (60 * 1000)).toString()
                binding.txtShortMin.text = (MainHelper.getShortBreak() / (60 * 1000)).toString()
                binding.txtLongMin.text = (MainHelper.getLongBreak() / (60 * 1000)).toString()
            }
        }

        etTimeInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newValue = etTimeInput.text.toString().toIntOrNull() ?: 0
                if (newValue in 1..180) {
                    etTimeInput.clearFocus() // Hide the keyboard
                    when (category) {
                        "WorkSession" -> MainHelper.setWorkSession(newValue)
                        "ShortBreak" -> MainHelper.setShortBreak(newValue)
                        "LongBreak" -> MainHelper.setLongBreak(newValue)
                    }

                    // Update the TextViews
                    binding.txtWorkMin.text = (MainHelper.getWorkSession() / (60 * 1000)).toString()
                    binding.txtShortMin.text = (MainHelper.getShortBreak() / (60 * 1000)).toString()
                    binding.txtLongMin.text = (MainHelper.getLongBreak() / (60 * 1000)).toString()
                    dialog.dismiss() // Close the dialog
                } else {
                    Snackbar.make(dialogBinding.root, "Value should be between 1 and 180", Snackbar.LENGTH_SHORT).show()
                    return@setOnEditorActionListener true
                }
            }
            false
        }

        dialog.show()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}