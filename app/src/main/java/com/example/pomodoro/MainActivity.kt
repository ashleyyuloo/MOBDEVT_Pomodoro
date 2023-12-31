package com.example.pomodoro

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
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
import com.example.pomodoro.viewModel.TaskViewModel
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var circleImageViews: List<ImageView>

    private val viewModel by viewModels<MainViewModel> {
        MainViewModelFactory(applicationContext)
    }

    private val taskViewModel by viewModels<TaskViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        circleImageViews = listOf(binding.imageView, binding.imageView2, binding.imageView3, binding.imageView4)

        viewModel.session.observe(this) { newSession ->
            binding.txtTimer.text = newSession.toString()
        }

        viewModel.workSessionCounter.observe(this) { completedWorkSessions ->
            updateCircleIndicators(completedWorkSessions)
        }

        taskViewModel.listOfTasks.observe(this) { tasks ->
            setupTaskViews(tasks)
        }

        with(binding){
            if (savedInstanceState != null) {
                binding.btnPlay.visibility = savedInstanceState.getInt("btnPlayVisibility", View.VISIBLE)
                binding.btnPause.visibility = savedInstanceState.getInt("btnPauseVisibility", View.GONE)
                binding.btnStop.visibility = savedInstanceState.getInt("btnStopVisibility", View.GONE)
            }
            else{
                setButtonVisibility(View.VISIBLE, View.GONE, View.GONE)
            }

            btnPlay.setOnClickListener {
                setButtonVisibility(View.GONE, View.VISIBLE, View.VISIBLE)

                if (viewModel.pausedTime > 0) {
                    viewModel.startTimer(viewModel.pausedTime)
                    viewModel.pausedTime = 0
                } else {
                    viewModel.startWorkSession()
                }
            }

            btnPause.setOnClickListener {
                setButtonVisibility(View.VISIBLE, View.GONE, View.VISIBLE)
                viewModel.pauseTimer()
            }

            btnStop.setOnClickListener {
                setButtonVisibility(View.VISIBLE, View.GONE, View.GONE)
                viewModel.stopTimer()
            }

            btnAddTasks.setOnClickListener {
                addNewTaskView()
            }

            btnClearTasks.setOnClickListener{
                taskViewModel.clearTasks()
            }

        }
    }

    private fun setButtonVisibility(play: Int, pause: Int, stop: Int) {
        binding.btnPlay.visibility = play
        binding.btnPause.visibility = pause
        binding.btnStop.visibility = stop
    }

    private var editingTaskIndex: Int = -1 //-1 means no task is being edited

    // STAN: allows the task views to be viewed, selected and edited
    @SuppressLint("ClickableViewAccessibility")
    private fun setupTaskViews(tasks: List<TaskViewModel.Task>) {
        binding.taskList.removeAllViews()

        //loops through the list of tasks
        tasks.forEachIndexed { index, task ->
            val taskViewBinding = ItemTaskBinding.inflate(layoutInflater)
            val taskView = taskViewBinding.root
            taskView.tag = index

            with(taskViewBinding) {
                checkboxCompleted.isChecked = task.isCompleted
                txtTask.text = Editable.Factory.getInstance().newEditable(task.name)
                //sets the txttask with the taskname and allows it to be editable

                toggleCheckbox(checkboxCompleted, txtTask, task.isCompleted)

                txtTask.isEnabled = !task.isCompleted //allow edits to to not completed tasks
                txtTask.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        val clickedTaskIndex = taskView.tag as Int

                        //if user is trying to edit another task than the one that was previously selected
                        if (editingTaskIndex != clickedTaskIndex) {
                            editingTaskIndex.takeIf { it != -1 }?.let { previousTaskIndex -> //checks for ongoing task edits
                                tasks[previousTaskIndex].name = txtTask.text.toString()
                            } //save the edit task to their original clicked tasks

                            editingTaskIndex = clickedTaskIndex
                            txtTask.isEnabled = true
                            txtTask.requestFocus()

                            setButtonsEnabledState(false)
                            taskViewBinding.btnDeleteTask.isEnabled = false
                        }
                    }
                    false
                }

                //checks for the done and enter key press
                txtTask.setOnEditorActionListener { _, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE || (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                        editingTaskIndex.takeIf { it != -1 }?.let { editedTaskIndex ->
                            tasks[editedTaskIndex].name = txtTask.text.toString()

                            //hide the keyboard
                            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputMethodManager.hideSoftInputFromWindow(txtTask.windowToken, 0)

                            //not editing
                            editingTaskIndex = -1
                            txtTask.clearFocus()

                            setButtonsEnabledState(true)
                            taskViewBinding.btnDeleteTask.isEnabled = true
                        }
                        true
                    } else {
                        false
                    }
                }

                deleteTask(btnDeleteTask)
            }

            binding.taskList.addView(taskView)
        }
    }

    private fun addNewTaskView(isChecked: Boolean = false) {
        val newTaskViewBinding = ItemTaskBinding.inflate(layoutInflater)
        val newTaskView = newTaskViewBinding.root

        toggleCheckbox(newTaskViewBinding.checkboxCompleted, newTaskViewBinding.txtTask, isChecked)
        deleteTask(newTaskViewBinding.btnDeleteTask)

        val taskEditText = newTaskViewBinding.txtTask

        setButtonsEnabledState(false)
        newTaskViewBinding.btnDeleteTask.isEnabled = false

        taskEditText.setOnEditorActionListener { _, actionId, _ ->
            //done is pressed
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val taskName = taskEditText.text.toString().trim() //gets the taskname without the whitespaces
                if (taskName.isNotEmpty()) {
                    val existingTask = taskViewModel.listOfTasks.value?.find { it.name == taskName }?.name
                    //checks if theres an existing task by finding the name of that task in the list

                    if (existingTask != null) { //it exists
                        Snackbar.make(binding.root, "Task with the same name already exists", Snackbar.LENGTH_SHORT).show()
                    } else {
                        //hides the keyboard
                        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(taskEditText.windowToken, 0)

                        val task = TaskViewModel.Task(taskName, isChecked)
                        taskViewModel.addTask(task)

                        setButtonsEnabledState(true)
                        newTaskViewBinding.btnDeleteTask.isEnabled = true
                    }
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }

        binding.taskList.addView(newTaskView)
        taskEditText.requestFocus()

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(taskEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun setButtonsEnabledState(enabled: Boolean) {
        binding.btnAddTasks.isEnabled = enabled
        binding.btnClearTasks.isEnabled = enabled
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
            val task = TaskViewModel.Task(taskName, isChecked)
            taskViewModel.updateTask(task)
        }
    }


    private fun deleteTask(deleteButton: Button) {
        deleteButton.setOnClickListener {
            val taskView = deleteButton.parent as View
            val taskId = taskView.tag as Int
            val task = taskViewModel.listOfTasks.value?.get(taskId)
            task?.let {
                taskViewModel.removeTask(it)
            }
            binding.taskList.removeView(taskView)
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

        val tasks = taskViewModel.listOfTasks.value ?: return //gets the list of tasks, if it is empty then u do nothing
        val taskStates = tasks.associate { it.name to it.isCompleted } //stores it like a map

        val bundle = Bundle()
        for ((key, value) in taskStates) {
            bundle.putBoolean(key, value)
        } //iterates through the takssaktes

        outState.putBundle("taskStatesBundle", bundle)
        outState.putInt("btnPlayVisibility", binding.btnPlay.visibility)
        outState.putInt("btnPauseVisibility", binding.btnPause.visibility)
        outState.putInt("btnStopVisibility", binding.btnStop.visibility)
    }

    /////////// TO EXPLAIN ////////////
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val bundle = savedInstanceState.getBundle("taskStatesBundle")
        val taskStates = mutableMapOf<String, Boolean>()

        bundle?.keySet()?.forEach { key ->
            val value = bundle.getBoolean(key)
            taskStates[key] = value
        }

        taskStates.forEach { (taskName, isCompleted) ->
            val task = TaskViewModel.Task(taskName, isCompleted)
            taskViewModel.updateTask(task)
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
            viewModel.stopTimer()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

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
