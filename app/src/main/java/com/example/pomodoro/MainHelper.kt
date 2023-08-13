package com.example.pomodoro

class MainHelper {
    companion object{

        private var workSession: Int = 25
        private var shortBreak: Int = 5
        private var longBreak: Int = 15

        fun getWorkSession(): Long {
            return convertToMillis(workSession)
        }

        fun setWorkSession(minutes: Int) {
            workSession = minutes
        }

        fun getShortBreak(): Long {
            return convertToMillis(shortBreak)
        }

        fun setShortBreak(minutes: Int) {
            shortBreak = minutes
        }

        fun getLongBreak(): Long {
            return convertToMillis(longBreak)
        }

        fun setLongBreak(minutes: Int) {
            longBreak = minutes
        }

        private fun convertToMillis(minutes: Int): Long {
            return minutes * 60 * 1000L
        }
//
//
//        // for testing purposes
//        fun getWorkSession(): Long {
//            return 10 * 1000L // 10 seconds
//        }
//
//        fun getShortBreak(): Long {
//            return 3 * 1000L // 3 seconds
//        }
//
//        fun getLongBreak(): Long {
//            return 5 * 1000L // 5 seconds
//        }
    }
}