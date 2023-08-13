package com.example.pomodoro.viewModel

import android.content.Context
import android.media.MediaPlayer
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
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

    private var job: Job? = null // to get the coroutine job (used for timer)
    private var remainingTime: Long = 0
    private var isWorkSession = false
    var pausedTime: Long = 0

    private val _workSessionDuration = MutableLiveData<Long>(MainHelper.getWorkSession())
    val workSessionDuration: LiveData<Long> = _workSessionDuration

    private val _shortBreakDuration = MutableLiveData<Long>(MainHelper.getShortBreak())
    val shortBreakDuration: LiveData<Long> = _shortBreakDuration

    private val _longBreakDuration = MutableLiveData<Long>(MainHelper.getLongBreak())
    val longBreakDuration: LiveData<Long> = _longBreakDuration

    fun updateWorkSessionDuration(newDuration: Long) {
        _workSessionDuration.value = newDuration
        Log.d("Testing", "MVM Work Time: ${_workSessionDuration.value}")
    }

    fun updateShortBreakDuration(newDuration: Long) {
        _shortBreakDuration.value = newDuration
        Log.d("Testing", "MVM Short Time: ${_shortBreakDuration.value}")
    }

    fun updateLongBreakDuration(newDuration: Long) {
        _longBreakDuration.value = newDuration
        Log.d("Testing", "MVM Long Time: ${_longBreakDuration.value}")
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
