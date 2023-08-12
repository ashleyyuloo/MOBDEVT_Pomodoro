
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
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.databinding.ItemTaskBinding
import com.example.pomodoro.viewModel.MainViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var circleImageViews: List<ImageView>

    val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory(applicationContext) // Pass the application context here
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        circleImageViews = listOf(binding.imageView, binding.imageView2, binding.imageView3, binding.imageView4)


        viewModel.session.observe(this) {
            binding.txtTimer.text = it.toString()
        }
        viewModel.workSessionCounter.observe(this) { completedWorkSessions ->
            updateCircleIndicators(completedWorkSessions)
        }
        viewModel.listOfTasks.observe(this) { tasks ->
            setupTaskViews(tasks)
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
                viewModel.clearTasks()
                binding.taskList.removeAllViews()
            }

        }
    }

    private fun setupTaskViews(tasks: List<MainViewModel.Task>) {
        binding.taskList.removeAllViews()

        tasks.forEachIndexed { index, task ->
            val taskViewBinding = ItemTaskBinding.inflate(layoutInflater)
            taskViewBinding.checkboxCompleted.isChecked = task.isCompleted

            val taskView = taskViewBinding.root
            taskView.tag = index

            toggleCheckbox(
                taskViewBinding.checkboxCompleted,
                taskViewBinding.txtTask,
                task.isCompleted
            )

            taskViewBinding.txtTask.text = Editable.Factory.getInstance().newEditable(task.name)

            if (task.isCompleted) {
                taskViewBinding.txtTask.isEnabled = false
            }

            deleteTask(taskViewBinding.btnDeleteTask)

            binding.taskList.addView(taskView)
        }
    }

    private var isNewTaskViewAdded = false
    private fun addNewTaskView(isChecked: Boolean = false) {
        val newTaskViewBinding = ItemTaskBinding.inflate(layoutInflater)
        val newTaskView = newTaskViewBinding.root

        toggleCheckbox(newTaskViewBinding.checkboxCompleted, newTaskViewBinding.txtTask, isChecked)
        deleteTask(newTaskViewBinding.btnDeleteTask)

        val taskEditText = newTaskViewBinding.txtTask

        binding.btnClearTasks.isEnabled = false
        newTaskViewBinding.btnDeleteTask.isEnabled = false
        binding.btnAddTasks.isEnabled = false

        taskEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val taskName = taskEditText.text.toString().trim()
                if (taskName.isNotEmpty()) {
                    val existingTask = viewModel.listOfTasks.value?.find { it.name == taskName }

                    if (existingTask != null) {
                        Snackbar.make(binding.root, "Task with the same name already exists", Snackbar.LENGTH_SHORT).show()
                    } else {
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(taskEditText.windowToken, 0)
                        val task = MainViewModel.Task(taskName, isChecked)
                        viewModel.addTask(task)

                        isNewTaskViewAdded = false // Reset the flag
                        binding.btnAddTasks.requestFocus()
                        binding.btnClearTasks.isEnabled = true
                        newTaskViewBinding.btnDeleteTask.isEnabled = true
                        binding.btnAddTasks.isEnabled = true
                    }

                    true
                } else {
                    isNewTaskViewAdded = true // Set the flag
                    false
                }
            } else {
                false
            }
        }

        binding.taskList.addView(newTaskView)

        // Request focus on the EditText
        taskEditText.requestFocus()

        // Show the keyboard
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(taskEditText, InputMethodManager.SHOW_IMPLICIT)
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
            val task = MainViewModel.Task(taskName, isChecked)
            viewModel.updateTask(task) // Update the task in the ViewModel
        }
    }


    private fun deleteTask(deleteButton: Button) {
        deleteButton.setOnClickListener {
            val taskView = deleteButton.parent as View
            val taskId = taskView.tag as Int
            val task = viewModel.listOfTasks.value?.get(taskId)
            task?.let {
                viewModel.removeTask(it) // Remove the task from the ViewModel
            }
            binding.taskList.removeView(taskView)
        }
    }

    private fun printTaskList() {
        val tasks = viewModel.listOfTasks.value
        tasks?.forEach { task ->
            Log.d("TaskList", "Task: ${task.name}, Completed: ${task.isCompleted}")
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        val tasks = viewModel.listOfTasks.value ?: return
        val taskStates = tasks.associate { it.name to it.isCompleted }

        val bundle = Bundle()
        for ((key, value) in taskStates) {
            bundle.putBoolean(key, value)
        }

        outState.putBundle("taskStatesBundle", bundle)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val bundle = savedInstanceState.getBundle("taskStatesBundle")
        val taskStates = mutableMapOf<String, Boolean>()

        bundle?.keySet()?.forEach { key ->
            val value = bundle.getBoolean(key)
            taskStates[key] = value
        }

        taskStates.forEach { (taskName, isCompleted) ->
            val task = MainViewModel.Task(taskName, isCompleted)
            viewModel.updateTask(task)
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
