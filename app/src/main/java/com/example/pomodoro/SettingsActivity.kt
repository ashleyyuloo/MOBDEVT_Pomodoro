package com.example.pomodoro

import android.app.Dialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.pomodoro.databinding.ActivitySettingsBinding
import com.example.pomodoro.databinding.EditSessionBinding
import com.example.pomodoro.viewModel.MainViewModel
import com.google.android.material.snackbar.Snackbar


class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private val colorButtonsMap = mutableMapOf<Button, Boolean>()

    private val viewModel by viewModels<MainViewModel> {
        MainActivity.MainViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding){
            WorkSessionLayout.setOnClickListener {
                showEditSessionDialog("WorkSession")
            }

            ShortBreakLayout.setOnClickListener {
                showEditSessionDialog("ShortBreak")
            }

            LongBreakLayout.setOnClickListener {
                showEditSessionDialog("LongBreak")
            }
        }
        setupColorButtons()
        setupTextViews()

    }

    private fun setupColorButtons() {
        val defaultTheme = R.drawable.baseline_check_box_24
        val whiteButtons = arrayOf(
            binding.btnColor1, binding.btnColor2, binding.btnColor7, binding.btnColor9,
            binding.btnColor12, binding.btnColor15, binding.btnColor16, binding.btnColor3, binding.btnColor4, binding.btnColor5,
            binding.btnColor6, binding.btnColor8, binding.btnColor10, binding.btnColor11,
            binding.btnColor13, binding.btnColor14
        )

        for (button in whiteButtons) {

            button.setOnClickListener {
                handleColorButtonClick(button)

                when(button){
                    binding.btnColor2 -> {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    }
                    else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
            if(isDarkModeOn()){
                binding.btnColor1.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                handleColorButtonClick(binding.btnColor2)
            }
            else{
                binding.btnColor1.setCompoundDrawablesRelativeWithIntrinsicBounds(defaultTheme, 0, 0, 0)
            }
        }

    }

    private fun isDarkModeOn(): Boolean {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == Configuration.UI_MODE_NIGHT_YES
    }

    private fun handleColorButtonClick(button: Button) {

        colorButtonsMap.forEach { (btn, isSelected) ->
            if (isSelected) {
                btn.tag = false
                btn.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                colorButtonsMap[btn] = false
            }
        }

        val currentState = colorButtonsMap[button] ?: false
        button.tag = !currentState

        val checkBoxDrawable = if (button in arrayOf(
                binding.btnColor1, binding.btnColor2, binding.btnColor7, binding.btnColor9,
                binding.btnColor12, binding.btnColor15, binding.btnColor16, binding.btnColor3, binding.btnColor4, binding.btnColor5,
                binding.btnColor6, binding.btnColor8, binding.btnColor10, binding.btnColor11,
                binding.btnColor13, binding.btnColor14
            )) {
                binding.btnColor1.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, 0)
                R.drawable.baseline_check_box_24
        }else{
            null
        }


//        val checkBoxDrawable = if (button in arrayOf(
//                binding.btnColor2, binding.btnColor7, binding.btnColor9,
//                binding.btnColor12, binding.btnColor15, binding.btnColor1
//                )) {
//            R.drawable.baseline_check_box_24
//        } else if (button in arrayOf(
//                binding.btnColor1,binding.btnColor2,binding.btnColor3, binding.btnColor4, binding.btnColor5
//            )) {
//            R.drawable.white_check_box_24
//        }else{
//            null
//        }

        button.setCompoundDrawablesRelativeWithIntrinsicBounds(checkBoxDrawable!!, 0, 0, 0)
        colorButtonsMap[button] = true
    }


    private fun setupTextViews() {
        binding.txtWorkMin.text = convertMillisecondsToMinutes(MainHelper.getWorkSession()).toString()
        binding.txtShortMin.text = convertMillisecondsToMinutes(MainHelper.getShortBreak()).toString()
        binding.txtLongMin.text = convertMillisecondsToMinutes(MainHelper.getLongBreak()).toString()
    }

    private fun convertMillisecondsToMinutes(milliseconds: Long): Long {
        return milliseconds / (60 * 1000)
    }

    private fun showEditSessionDialog(category: String) {
        val dialog = Dialog(this)
        val dialogBinding = EditSessionBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val etTimeInput = dialogBinding.etTimeInput

        val timeInMinutes: Long = when (category) {
            "WorkSession" -> convertMillisecondsToMinutes(MainHelper.getWorkSession())
            "ShortBreak" -> convertMillisecondsToMinutes(MainHelper.getShortBreak())
            "LongBreak" -> convertMillisecondsToMinutes(MainHelper.getLongBreak())
            else -> 0
        }

    etTimeInput.setText(timeInMinutes.toString())
        dialogBinding.tvTimeCat.text = when (category) {
            "WorkSession" -> getString(R.string.workSessionTitle)
            "ShortBreak" -> getString(R.string.shrtBreakTitle)
            "LongBreak" -> getString(R.string.lngBreakTitle)
            else -> ""
        }

        dialogBinding.btnIncrease.setOnClickListener {
            updateSessionTime(etTimeInput, category, 1)
        }

        dialogBinding.btnDecrease.setOnClickListener {
            updateSessionTime(etTimeInput, category, -1)
        }

        etTimeInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val newValue = etTimeInput.text.toString().toIntOrNull() ?: 0
                if (newValue in 1..180) {
                    etTimeInput.clearFocus() // Hide the keyboard
                    updateMainHelperAndTextViews(category, newValue)
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

    private fun updateSessionTime(etTimeInput: EditText, category: String, change: Int) {
        val currentValue = etTimeInput.text.toString().toIntOrNull() ?: 0
        val newValue = currentValue + change
        if (newValue in 1..180) {
            etTimeInput.setText(newValue.toString())
            updateMainHelperAndTextViews(category, newValue)
            Log.d("Testing Settings", "SA Category: $category, Value: $newValue")
        } else {
            Snackbar.make(etTimeInput.rootView, "Value should be between 1 and 180", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun updateMainHelperAndTextViews(category: String, newValue: Int) {
        when (category) {
            "WorkSession" -> {
                viewModel.updateWorkSessionDuration(newValue.toLong())
            }
            "ShortBreak" -> {
                viewModel.updateShortBreakDuration(newValue.toLong())
            }
            "LongBreak" -> {
                viewModel.updateLongBreakDuration(newValue.toLong())
            }
        }
        setupTextViews()
    }

    //go back to home
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

}