package com.example.pulsmesserv2.bluetooth

interface BluetoothDataListener {
    fun onCurrentValueReceived(value: Int)
    fun onAverageValueReceived(value: Int)
    fun onCurrentMeasuring(isMeasuring: Boolean)

    fun onActiveDevice(name: String)
    fun onImageDeviceStatus(imageResources: Int)
    fun onImageDeviceStatusColor(color: Int)
    fun onCurrentBluetoothConnectionStatus(status: Boolean)

}