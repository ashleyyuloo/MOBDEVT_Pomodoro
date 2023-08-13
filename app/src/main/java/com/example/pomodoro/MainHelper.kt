package com.example.pomodoro

import android.util.Log

class MainHelper {
    companion object {
        private var workSession: Int = 2
        private var shortBreak: Int = 1
        private var longBreak: Int = 1

        fun getWorkSession(): Long {
            val workSessionInMillis = convertToMillis(workSession)
            Log.d("MainHelper", "getWorkSession: $workSessionInMillis")
            return workSessionInMillis
        }

        fun setWorkSession(minutes: Int) {
            workSession = minutes
            Log.d("MainHelper", "setWorkSession: $workSession")
        }

        fun getShortBreak(): Long {
            val shortBreakInMillis = convertToMillis(shortBreak)
            Log.d("MainHelper", "getShortBreak: $shortBreakInMillis")
            return shortBreakInMillis
        }

        fun setShortBreak(minutes: Int) {
            shortBreak = minutes
            Log.d("TestingMainHelper", "setShortBreak: $shortBreak")
        }

        fun getLongBreak(): Long {
            val longBreakInMillis = convertToMillis(longBreak)
            Log.d("TestingMainHelper", "getLongBreak: $longBreakInMillis")
            return longBreakInMillis
        }

        fun setLongBreak(minutes: Int) {
            longBreak = minutes
            Log.d("TestingMainHelper", "setLongBreak: $longBreak")
        }

        private fun convertToMillis(minutes: Int): Long {
            return minutes * 60 * 1000L
        }
    }
}
