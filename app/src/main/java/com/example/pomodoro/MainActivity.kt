
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
            taskViewBinding.txtTask.text = Editable.Factory.getInstance().newEditable(task.name)
            taskViewBinding.checkboxCompleted.isChecked = task.isCompleted

            val taskView = taskViewBinding.root
            taskView.tag = index

            toggleCheckbox(
                taskViewBinding.checkboxCompleted,
                taskViewBinding.txtTask,
                task.isCompleted
            )
            deleteTask(taskViewBinding.btnDeleteTask)

            binding.taskList.addView(taskView)
        }
    }


    private fun addNewTaskView(isChecked: Boolean = false) {
        val newTaskViewBinding = ItemTaskBinding.inflate(layoutInflater)
        val newTaskView = newTaskViewBinding.root
        newTaskView.tag = viewModel.listOfTasks.value?.size ?: 0

        toggleCheckbox(newTaskViewBinding.checkboxCompleted, newTaskViewBinding.txtTask, isChecked)
        deleteTask(newTaskViewBinding.btnDeleteTask)

        binding.taskList.addView(newTaskView)

        val taskEditText = newTaskViewBinding.txtTask
        var taskAddDelayJob: Job? = null

        taskEditText.addTextChangedListener { editable ->
            val taskName = editable.toString().trim()
            taskAddDelayJob?.cancel()
            taskAddDelayJob = CoroutineScope(Dispatchers.Main).launch {
                delay(1000)
                if (taskName.isNotEmpty()) {
                    val task = MainViewModel.Task(taskName, isChecked)
                    viewModel.addTask(task)
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
