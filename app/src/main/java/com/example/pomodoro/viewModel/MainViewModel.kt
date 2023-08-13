package com.example.pomodoro.viewModel

import android.content.Context
import android.media.MediaPlayer
import android.os.Parcel
import android.os.Parcelable
import android.widget.Button
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
    private lateinit var mediaPlayer: MediaPlayer

    private val _session = MutableLiveData<String>()
    val session: LiveData<String> = _session

    private val _workSessionCounter = MutableLiveData<Int>(0)
    val workSessionCounter: LiveData<Int> = _workSessionCounter

    private val _workSessionDuration = MutableLiveData<Long>()
    val workSessionDuration: LiveData<Long> = _workSessionDuration

    private val _shortBreakDuration = MutableLiveData<Long>()
    val shortBreakDuration: LiveData<Long> = _shortBreakDuration

    private val _longBreakDuration = MutableLiveData<Long>()
    val longBreakDuration: LiveData<Long> = _longBreakDuration

    private var job: Job? = null // to get the coroutine job (used for timer)
    private var remainingTime: Long = 0
    private var isWorkSession = false
    var pausedTime: Long = 0

    fun updateDurations() {
        _workSessionDuration.value = MainHelper.getWorkSession()
        _shortBreakDuration.value = MainHelper.getShortBreak()
        _longBreakDuration.value = MainHelper.getLongBreak()
    }


    init {
        _workSessionDuration.value = MainHelper.getWorkSession()
        _shortBreakDuration.value = MainHelper.getShortBreak()
        _longBreakDuration.value = MainHelper.getLongBreak()
        updateTimerDisplay(_workSessionDuration.value!!)
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
        if (pausedTime > 0) {
            startTimer(pausedTime)
            pausedTime = 0
        } else {
            startTimer(_workSessionDuration.value!!)
        }
    }

    private fun startShortBreak() {
        isWorkSession = false
        playSound()
        startTimer(_shortBreakDuration.value!!)
    }

    private fun startLongBreak() {
        isWorkSession = false
        playSound()
        startTimer(_longBreakDuration.value!!)
    }

    fun startTimer(duration: Long) {
        job?.cancel()
        job = viewModelScope.launch {
            remainingTime = duration

            while (remainingTime > 0) {
                updateTimerDisplay(remainingTime)
                delay(1000)
                remainingTime -= 1000
            }
            timerFinished()
        }
    }

    private fun timerFinished() {
        if (isWorkSession) {
            _workSessionCounter.value = _workSessionCounter.value!! + 1

            if (_workSessionCounter.value == 4) {
                startLongBreak()
            } else {
                startShortBreak()
            }
        } else {
            playSound()
            startWorkSession()
            if (_workSessionCounter.value == 4) {
                _workSessionCounter.value = 0
            }
        }
    }

    fun pauseTimer() {
        job?.cancel()
        pausedTime = remainingTime
    }

    fun stopTimer() {
        job?.cancel()
        _workSessionCounter.value = 0
        updateTimerDisplay(_workSessionDuration.value!!)
    }

    private fun updateTimerDisplay(millis: Long) {
        _session.value = formatTime(millis)
    }

    private fun formatTime(millis: Long): String {
        val minutes = (millis / 60000) % 60
        val seconds = (millis % 60000) / 1000
        return if (millis < 3600000) {
            String.format("%02d:%02d", minutes, seconds)
        } else {
            val hours = millis / 3600000
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }
    }


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
