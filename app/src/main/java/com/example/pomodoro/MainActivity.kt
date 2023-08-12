
package com.example.pomodoro

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.databinding.ItemTaskBinding
import com.example.pomodoro.viewModel.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    data class Task(val name: String, val isCompleted: Boolean = false)
    private val ListTask = mutableListOf<Task>()

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
                //viewModel.pauseTimer()
                printTaskList()
            }

            btnStop.setOnClickListener {
                viewModel.stopTimer()
            }

            btnAddTasks.setOnClickListener {
                addNewTaskView()
            }

            btnClearTasks.setOnClickListener{
                binding.taskList.removeAllViews()
                ListTask.clear()
            }

        }
    }

    private fun addNewTaskView(isChecked: Boolean = false) {
        val newTaskViewBinding = ItemTaskBinding.inflate(layoutInflater)
        val newTaskView = newTaskViewBinding.root
        newTaskView.tag = ListTask.size // Assign a unique identifier to the view

        toggleCheckbox(newTaskViewBinding.checkboxCompleted, newTaskViewBinding.txtTask, isChecked)
        deleteTask(newTaskViewBinding.btnDeleteTask)

        binding.taskList.addView(newTaskView)

        val taskEditText = newTaskViewBinding.txtTask // Store a reference to the EditText
        var taskAddDelayJob: Job? = null

        taskEditText.addTextChangedListener { editable ->
            val taskName = editable.toString().trim()
            taskAddDelayJob?.cancel() // Cancel the previous job to prevent adding task prematurely
            taskAddDelayJob = CoroutineScope(Dispatchers.Main).launch {
                delay(1000) // Wait for 1 second of inactivity in typing
                if (taskName.isNotEmpty()) {
                    val task = Task(taskName, isChecked)
                    ListTask.add(task)
                }
            }
        }
    }

    private fun toggleCheckbox(checkbox: CheckBox, textView: TextView, isChecked: Boolean) {
        if (isChecked) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }

        checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }

            val taskName = textView.text.toString().trim()
            val task = Task(taskName, isChecked)
            val existingTaskIndex = ListTask.indexOfFirst { it.name == taskName }

            if (existingTaskIndex >= 0) {
                ListTask[existingTaskIndex] = task // Update existing task
            } else {
                ListTask.add(task) // Add new task
            }
        }
    }

    private fun deleteTask(deleteButton: Button) {
        deleteButton.setOnClickListener {
            val taskView = deleteButton.parent as View
            val taskId = taskView.tag as Int // Retrieve the unique identifier
            ListTask.removeAt(taskId) // Remove the corresponding task from the list
            binding.taskList.removeView(taskView)
        }
    }

    private fun printTaskList() {
        for (task in ListTask) {
            Log.d("TaskList", "Task: ${task.name}, Completed: ${task.isCompleted}")
        }
    }


    /*
    private fun changeFocus(editText: EditText) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        editText.requestFocus()
        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        editText.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
                editText.clearFocus()
                binding.btnAddTasks.requestFocus()
                true
            } else {
                false
            }
        }

        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
                editText.clearFocus()
                binding.btnAddTasks.requestFocus()
                true
            } else {
                false
            }
        }
    }
*/

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
