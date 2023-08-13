package com.example.pomodoro

class MainHelper {
    companion object{

        private const val workSession = 60
        private const val shortBreak = 5
        private const val longBreak = 15

        fun getWorkSession(): Long {
            return convertToMillis(workSession)
        }

        fun getShortBreak(): Long {
            return convertToMillis(shortBreak)
        }

        fun getLongBreak(): Long {
            return convertToMillis(longBreak)
        }

        private fun convertToMillis(minutes:Int): Long{
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