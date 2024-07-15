package com.example.pulsmesserv2.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BpmViewModel : ViewModel() {

    var bpm = 0
    var avgBpm = 0
    var deviceName = ""

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> get() = _isConnected

    fun setBPM(value: Int) {
        bpm = value
    }
    fun setAvgBPM(value: Int) {
        avgBpm = value
    }
    fun setConnectionStatus(status: Boolean) {
        _isConnected.value = status
    }
    fun setDeviceStatus(name: String) {
        deviceName = name
    }
}