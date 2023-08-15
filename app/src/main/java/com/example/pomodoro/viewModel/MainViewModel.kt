package com.example.pomodoro.viewModel

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private var job: Job? = null // to get the coroutine job (used for timer; ASYNCH JOB)
    private var remainingTime: Long = 0
    private var isWorkSession = false
    var pausedTime: Long = 0

    private val _workSessionDuration = MutableLiveData<Long>(MainHelper.getWorkSession())

    private val _shortBreakDuration = MutableLiveData<Long>(MainHelper.getShortBreak())

    private val _longBreakDuration = MutableLiveData<Long>(MainHelper.getLongBreak())

    fun updateWorkSessionDuration(newDuration: Long) {
        MainHelper.setWorkSession(newDuration.toInt())
        _workSessionDuration.value = newDuration
    }

    fun updateShortBreakDuration(newDuration: Long) {
        MainHelper.setShortBreak(newDuration.toInt())
        _shortBreakDuration.value = newDuration
    }

    fun updateLongBreakDuration(newDuration: Long) {
        MainHelper.setLongBreak(newDuration.toInt())
        _longBreakDuration.value = newDuration
    }

    init {
        updateTimerDisplay(_workSessionDuration.value!!)
    }

    private fun playSound(){
        mediaPlayer = MediaPlayer.create(applicationContext, R.raw.triangle_open)
        mediaPlayer!!.setOnCompletionListener {
            it.release()
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
        startTimer(_shortBreakDuration.value!!)
    }

    private fun startLongBreak() {
        isWorkSession = false
        startTimer(_longBreakDuration.value!!)
    }

    // STAN: starts the timer
    fun startTimer(duration: Long) {
        job?.cancel()
        job = viewModelScope.launch {// starts the count
            remainingTime = duration //duration is the session values in millis

            while (remainingTime > 0) {
                updateTimerDisplay(remainingTime)
                delay(1000) //after 1 second
                remainingTime -= 1000 // subtract 1 second from the remaining time
            }
            timerFinished() //reamining time is zero
        }
    }

    private fun timerFinished() {
        if (isWorkSession) {
            _workSessionCounter.value = _workSessionCounter.value!! + 1

            playSound()
            if (_workSessionCounter.value == 4) {
                startLongBreak()
            } else {
                startShortBreak()
            }
        } else {
            startWorkSession()
            if (_workSessionCounter.value == 4) {
                _workSessionCounter.value = 0
            }
        }
    }

    //STAN: Pasues the timer
    fun pauseTimer() {
        job?.cancel()
        pausedTime = remainingTime
    }

    // STAN: Stops the timer
    fun stopTimer() {
        job?.cancel()
        _workSessionCounter.value = 0
        updateTimerDisplay(_workSessionDuration.value!!)
        Log.d("Testing Settings", "work duration: $_workSessionDuration")
    }

    private fun updateTimerDisplay(millis: Long) {
        val formattedTime = formatTime(millis)
        _session.value = formattedTime
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
}
