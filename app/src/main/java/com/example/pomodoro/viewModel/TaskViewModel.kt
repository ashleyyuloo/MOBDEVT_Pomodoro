package com.example.pomodoro.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TaskViewModel: ViewModel() {

    data class Task(var name: String, val isCompleted: Boolean = false)

    val listOfTasks: MutableLiveData<MutableList<Task>> = MutableLiveData(mutableListOf())

    fun addTask(task: Task) {
        val updatedList = listOfTasks.value ?: mutableListOf()
        updatedList.add(task)
        listOfTasks.value = updatedList
    }

    fun updateTask(task: Task) {
        val updatedList = listOfTasks.value ?: mutableListOf()
        val existingTaskIndex = updatedList.indexOfFirst { it.name == task.name }
        if (existingTaskIndex >= 0) {
            updatedList[existingTaskIndex] = task
            listOfTasks.value = updatedList
        }
    }

    fun removeTask(task: Task) {
        val updatedList = listOfTasks.value ?: mutableListOf()
        updatedList.remove(task)
        listOfTasks.value = updatedList
    }

    fun clearTasks() {
        listOfTasks.value?.clear()
    }

}