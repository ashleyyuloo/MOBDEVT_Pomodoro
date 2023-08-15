package com.example.pomodoro.viewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.pomodoro.TaskHelper

class TaskViewModel : ViewModel() {
    data class Task(var name: String, var isCompleted: Boolean = false)

    private val _listOfTasks: MutableLiveData<List<Task>> = MutableLiveData(TaskHelper.getTasks())
    val listOfTasks: LiveData<List<Task>> get() = _listOfTasks

    fun addTask(task: Task) {
        val updatedList = _listOfTasks.value?.toMutableList() ?: mutableListOf()
        updatedList.add(task)
        _listOfTasks.value = updatedList
        TaskHelper.setAddedTask(task)
    }

    fun updateTask(task: Task) {
        val updatedList = _listOfTasks.value?.toMutableList() ?: mutableListOf()
        val existingTaskIndex = updatedList.indexOfFirst { it.name == task.name }
        if (existingTaskIndex >= 0) {
            updatedList[existingTaskIndex] = task
            _listOfTasks.value = updatedList
        }
        TaskHelper.setUpdatedTask(task)
    }

    fun removeTask(task: Task) {
        val updatedList = _listOfTasks.value?.toMutableList() ?: mutableListOf()
        updatedList.remove(task)
        _listOfTasks.value = updatedList
        TaskHelper.setRemovedTask(task)
    }

    fun clearTasks() {
        _listOfTasks.value = emptyList()
        TaskHelper.setEmptyTask()
    }
}
