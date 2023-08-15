package com.example.pomodoro

import android.os.Bundle
import com.example.pomodoro.viewModel.TaskViewModel

data class Task(val name: String, var isCompleted: Boolean = false)
class TaskHelper {
    companion object {
        private val taskList: MutableList<TaskViewModel.Task> = mutableListOf()

        fun getTasks(): List<TaskViewModel.Task> {
            return taskList.toList()
        }

        fun setAddedTask(task: TaskViewModel.Task) {
            taskList.add(task)
        }

        fun setUpdatedTask(task: TaskViewModel.Task) {
            val existingTask = taskList.find { it.name == task.name }
            existingTask?.let {
                it.isCompleted = task.isCompleted
            }
        }

        fun setRemovedTask(task: TaskViewModel.Task) {
            taskList.remove(task)
        }

        fun setEmptyTask() {
            taskList.clear()
        }
    }
}
