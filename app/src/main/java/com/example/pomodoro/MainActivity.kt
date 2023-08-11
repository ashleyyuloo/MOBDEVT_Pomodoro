package com.example.pomodoro

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.viewModel.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var circleImageViews: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        circleImageViews = listOf(binding.imageView, binding.imageView2, binding.imageView3, binding.imageView4)

        val viewModel by viewModels<MainViewModel> {
            MainViewModelFactory(applicationContext) // Pass the application context here
        }
        viewModel.session.observe(this) {
            binding.txtTimer.text = it.toString()
        }
        viewModel.workSessionCounter.observe(this) { completedWorkSessions ->
            updateCircleIndicators(completedWorkSessions)
        }

        with(binding){
            btnPlay.setOnClickListener {
                if (viewModel.pausedTime > 0) {
                    viewModel.startTimer(viewModel.pausedTime)
                    viewModel.pausedTime = 0
                } else {
                    viewModel.startWorkSession()
                }
            }

            btnPause.setOnClickListener {
                viewModel.pauseTimer()
            }

            btnStop.setOnClickListener {
                viewModel.stopTimer()
            }

            btnAddTasks.setOnClickListener {
                val newTaskView = layoutInflater.inflate(R.layout.item_task, null)
                val checkboxCompleted = newTaskView.findViewById<CheckBox>(R.id.checkboxCompleted)
                val txtTask = newTaskView.findViewById<EditText>(R.id.txtTask)
                val btnDeleteTask = newTaskView.findViewById<Button>(R.id.btnDeleteTask)

                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

                checkboxCompleted.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        txtTask.paintFlags = txtTask.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    } else {
                        txtTask.paintFlags = txtTask.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    }
                }

                // Set a focus change listener to show the keyboard when EditText gains focus
                txtTask.setOnFocusChangeListener { _, hasFocus ->
                    if (hasFocus) {
                        inputMethodManager.showSoftInput(txtTask, InputMethodManager.SHOW_IMPLICIT)
                    }
                }

                btnDeleteTask.setOnClickListener {
                    taskList.removeView(newTaskView) // Remove the task when the delete button is clicked
                }

                taskList.addView(newTaskView) // Add the new task view to the LinearLayout
                txtTask.requestFocus() // Set focus on the EditText
            }




            btnClearTasks.setOnClickListener{
                binding.taskList.removeAllViews()
            }

        }
    }

    private fun updateCircleIndicators(completedWorkSessions: Int) {
        for (i in 0 until circleImageViews.size) {
            if (i < completedWorkSessions) {
                circleImageViews[i].setImageResource(R.drawable.circle_filled)
            } else {
                circleImageViews[i].setImageResource(R.drawable.circle_outline)
            }
        }
    }

    // to go to settings
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_pomodoro, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.toSettings){
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    // °˖✧✿✧˖° TO EXPLAIN °˖✧✿✧˖°
    class MainViewModelFactory(private val applicationContext: Context) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(applicationContext) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


}