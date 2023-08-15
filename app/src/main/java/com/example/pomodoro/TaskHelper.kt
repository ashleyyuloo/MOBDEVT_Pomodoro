package com.example.pomodoro

data class Task(val name: String, val isCompleted: Boolean = false)
class TaskHelper {
    companion object{
        private val taskList: MutableList<Task> = mutableListOf()

        fun getTasks(): List<Task> {
            return taskList.toList()
        }

        fun setAddTask(task: Task){
            taskList.add(task)
        }

        fun setUpdateTask(task: Task){
            taskList.add(task)
        }

    }
}