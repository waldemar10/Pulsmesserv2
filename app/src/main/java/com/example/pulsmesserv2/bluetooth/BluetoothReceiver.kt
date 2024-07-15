package com.example.pulsmesserv2.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BluetoothReceiver : BroadcastReceiver() {

    private lateinit var listener: BluetoothStatusListener

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action

        if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            // Handle Bluetooth state changes
            listener.onBluetoothStateChanged(state)
        }
    }


}