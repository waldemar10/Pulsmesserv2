package com.example.pulsmesserv2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class BluetoothViewModel: ViewModel()  {

    private val _scanCompleted = MutableLiveData<Boolean>()
    val scanCompleted: LiveData<Boolean> get() = _scanCompleted

    fun notifyScanCompleted() {
        _scanCompleted.value = true
    }
}