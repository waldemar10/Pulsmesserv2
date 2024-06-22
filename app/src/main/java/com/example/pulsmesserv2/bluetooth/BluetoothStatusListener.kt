package com.example.pulsmesserv2.bluetooth

interface BluetoothStatusListener {
    fun onBluetoothStateChanged(state: Int)
}