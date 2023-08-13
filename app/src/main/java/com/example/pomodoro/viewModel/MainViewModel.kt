package com.example.pomodoro.viewModel

import android.content.Context
import android.media.MediaPlayer
import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pomodoro.MainActivity
import com.example.pomodoro.MainHelper
import com.example.pomodoro.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//gets the application context from the MainActivity
class MainViewModel(private val applicationContext: Context) : ViewModel() {
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


    private lateinit var mediaPlayer: MediaPlayer

    private val _session = MutableLiveData<String>()
    val session: LiveData<String> = _session

    private val _workSessionCounter = MutableLiveData<Int>(0)
    val workSessionCounter: LiveData<Int> = _workSessionCounter

    private val _workSession = MainHelper.getWorkSession()
    private val _shortBreak = MainHelper.getShortBreak()
    private val _longBreak = MainHelper.getLongBreak()

    private var job: Job? = null // to get the coroutine job (used for timer)
    private var remainingTime: Long = 0
    private var isWorkSession = false
    var pausedTime: Long = 0

    init {
        updateTimerDisplay(_workSession)
    }

    private fun playSound(){
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.triangle_open)
        mediaPlayer!!.setOnCompletionListener {
            it.release() // Release the MediaPlayer resource after the sound is played
        }
        mediaPlayer!!.start()
    }

    fun startWorkSession() {
        isWorkSession = true
        if (pausedTime > 0) { // if there is a paused value, start the session from the pausedTime
            startTimer(pausedTime)
            pausedTime = 0
        } else {
            startTimer(_workSession) // play it from the start
        }
    }

    private fun startShortBreak() {
        isWorkSession = false
        playSound()
        startTimer(_shortBreak)
    }

    private fun startLongBreak() {
        isWorkSession = false
        playSound()
        startTimer(_longBreak)
    }

    fun startTimer(duration: Long) {
        job?.cancel() // cancel or stop any running timer
        job = viewModelScope.launch {
            remainingTime = duration

            while (remainingTime > 0) {
                updateTimerDisplay(remainingTime)
                delay(1000) // wait for 1 second
                remainingTime -= 1000 // subtract 1 second from the time (countdown)
            }
            timerFinished()
        }
    }

    // Actions to be performed when the timer finishes
    private fun timerFinished() {
        if (isWorkSession) {
            _workSessionCounter.value = _workSessionCounter.value!! + 1

            if (_workSessionCounter.value == 4) {
                startLongBreak() // after 4 work sessions = longbreak
            } else {
                startShortBreak() // short break after ever sessions
            }
        } else {
            playSound()
            startWorkSession()
            if (_workSessionCounter.value == 4) {
                _workSessionCounter.value = 0 // reset work session after 4 session
                //placed here so that the circles reset after the long break
            }
        }
    }

    fun pauseTimer() {
        job?.cancel()
        pausedTime = remainingTime // store the current time (remaining) in the paused time
    }

    fun stopTimer() {
        job?.cancel()
        _workSessionCounter.value = 0 // restart the progress
        updateTimerDisplay(_workSession)
    }

    private fun updateTimerDisplay(millis: Long) {
        _session.value = formatTime(millis)
    }

    private fun formatTime(millis: Long): String {
        val minutes = millis / 60000
        val seconds = (millis % 60000) / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }
}
