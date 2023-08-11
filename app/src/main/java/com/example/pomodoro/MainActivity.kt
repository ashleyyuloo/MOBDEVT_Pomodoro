
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.databinding.ItemTaskBinding
import com.example.pomodoro.viewModel.MainViewModel
import com.example.pomodoro.viewModel.TaskItem

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var circleImageViews: List<ImageView>

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Save your task list data into the bundle
        val taskItems = mutableListOf<TaskItem>()
        for (i in 0 until binding.taskList.childCount) {
            val taskView = binding.taskList.getChildAt(i)
            val taskText = taskView.findViewById<TextView>(R.id.txtTask).text.toString()
            val isChecked = taskView.findViewById<CheckBox>(R.id.checkboxCompleted).isChecked
            taskItems.add(TaskItem(taskText, isChecked))
        }
        outState.putParcelableArrayList("taskList", ArrayList(taskItems))
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        // Restore the task list data from the bundle
        val taskItems = savedInstanceState.getParcelableArrayList<TaskItem>("taskList")
        if (taskItems != null) {
            for (taskItem in taskItems) {
                val newTaskView = ItemTaskBinding.inflate(layoutInflater)
                newTaskView.txtTask.text = taskItem.task?.toEditable() // Convert String to Editable
                newTaskView.checkboxCompleted.isChecked = taskItem.isChecked

                toggleCheckbox(newTaskView.checkboxCompleted, newTaskView.txtTask, taskItem.isChecked)
                deleteTask(newTaskView.btnDeleteTask)
                changeFocus(newTaskView.txtTask)

                binding.taskList.addView(newTaskView.root)
            }
        }
    }

    // Extension function to convert String to Editable
    fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)


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
                addNewTaskView()
            }

            btnClearTasks.setOnClickListener{
                binding.taskList.removeAllViews()
            }

        }
    }

    private fun addNewTaskView(isChecked: Boolean = false) {
        val newTaskView = ItemTaskBinding.inflate(layoutInflater)
        toggleCheckbox(newTaskView.checkboxCompleted, newTaskView.txtTask, isChecked)
        deleteTask(newTaskView.btnDeleteTask)
        changeFocus(newTaskView.txtTask)
        binding.taskList.addView(newTaskView.root)
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
        }
    }

    private fun deleteTask(deleteButton: Button) {
        deleteButton.setOnClickListener {
            binding.taskList.removeView(deleteButton.parent as View)
        }
    }

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
