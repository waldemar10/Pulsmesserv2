package com.example.pulsmesserv2

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

import java.util.UUID

private const val REQUEST_ENABLE_BT = 1
private const val REQUEST_CODE_BLUETOOTH_SCAN = 3
private const val REQUEST_BLUETOOTH_PERMISSION = 4
private const val REQUEST_LOCATION_PERMISSION = 5

class BluetoothHandler() {


    private val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val  CHARACTERISTIC_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
    private val  AVG_CHARACTERISTIC_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
    private val  CURR_CHARACTERISTIC_UUID: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val  RESET_CHARACTERISTIC_UUID:UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")

    private var scanCallback: ScanCallback? = null
    private val foundDevices = HashSet<String>()

    private var connectedDeviceName: String? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val SCAN_PERIOD: Long = 10000

    @RequiresApi(Build.VERSION_CODES.S)
    fun startScan(bAdapter: BluetoothAdapter,context: Context,activity: MainActivity,progressDialogCallback: ProgressDialogCallback) {
        val bluetoothLeScanner = bAdapter.bluetoothLeScanner

        val scanResults: MutableList<ScanResult> = mutableListOf()
        foundDevices.clear() // Leeren des Sets bei jedem neuen Scan

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val device = result.device
                val deviceAddress = device.address

                // Prüfen, ob das Gerät bereits gefunden wurde
                if (foundDevices.add(deviceAddress)) {
                    //Wenn Gerät ist neu, zur Liste hinzufügen
                    scanResults.add(result)
                    val deviceName = if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }else{
                        device.name ?: "Unknown Device"
                    }
                    val scanResultText = "$deviceName - $deviceAddress"
                    Log.d("BluetoothScan", "Scan result: $scanResultText")
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e("BluetoothScan", "Scan failed with error code $errorCode")
            }
        }

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        bluetoothLeScanner.startScan(scanCallback)

        Handler(Looper.getMainLooper()).postDelayed({
            bluetoothLeScanner.stopScan(scanCallback)

            progressDialogCallback.closeProgressDialog()

            // Überprüfen, ob mindestens ein Gerät gefunden wurde
            val deviceNames = scanResults.mapNotNull { it.device.name }.toTypedArray()
            if (deviceNames.isNotEmpty()) {
                AlertDialog.Builder(context)
                    .setTitle("Select a device")
                    .setItems(deviceNames) { _, which ->
                        val selectedDevice = scanResults[which].device
                        connectToDevice(selectedDevice,context,activity)
                        HomeFragment().showToast("Connecting to ${selectedDevice.name}")
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                HomeFragment().showToast("No devices found")
            }
        }, SCAN_PERIOD)
    }

    private fun disconnectGatt(context: Context) {
        bluetoothGatt?.let { gatt ->
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            gatt.disconnect()
            gatt.close()

            HomeFragment().tvBluetoothStatus.text = "Keine Verbindung "
            HomeFragment().ivStatusConnection.setImageResource(R.drawable.ic_no_connection)
            HomeFragment().ivStatusConnection.setBackgroundColor(Color.RED)
            bluetoothGatt = null
        }
    }
    @RequiresApi(Build.VERSION_CODES.S)
    fun connectToDevice(device: BluetoothDevice, context: Context, activity: MainActivity) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH_PERMISSION)
            return
        }

        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {

                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                        gatt.discoverServices()
                    } else {
                        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH_PERMISSION)
                    }
                    connectedDeviceName = gatt.device.name
                    activity.runOnUiThread {
                        HomeFragment().tvBluetoothStatus.text = "Gerät ${connectedDeviceName ?: "Unbekannt"} verbunden "
                        HomeFragment().ivStatusConnection.setImageResource(R.drawable.ic_yes_connection)
                        HomeFragment().ivStatusConnection.setBackgroundColor(Color.GREEN)
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    activity.runOnUiThread {
                        HomeFragment().showToast("Disconnected from device")
                        HomeFragment().tvBluetoothStatus.text = "Keine Verbindung "
                        HomeFragment().ivStatusConnection.setImageResource(R.drawable.ic_no_connection)
                        HomeFragment().ivStatusConnection.setBackgroundColor(Color.RED)
                    }
                }
            }
            override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(SERVICE_UUID)
                    if (descriptor.characteristic.uuid == CURR_CHARACTERISTIC_UUID) {
                        val avgCharacteristic = service.getCharacteristic(AVG_CHARACTERISTIC_UUID)
                        if (avgCharacteristic != null) {
                            enableNotification(gatt, avgCharacteristic)
                        }
                    }
                }
            }

            private fun enableNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
                gatt.writeDescriptor(descriptor)
                gatt.setCharacteristicNotification(characteristic, true)
            }

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(SERVICE_UUID)
                    val currCharacteristic = service.getCharacteristic(CURR_CHARACTERISTIC_UUID)
                    val avgCharacteristic = service.getCharacteristic(AVG_CHARACTERISTIC_UUID)

                    if (currCharacteristic != null) {
                        enableNotification(gatt, currCharacteristic)
                    }

                    if (currCharacteristic != null) {
                        val descriptor = currCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }

                        gatt.writeDescriptor(descriptor, descriptor.value)
                        gatt.setCharacteristicNotification(currCharacteristic, true)
                    }

                    if (avgCharacteristic != null) {
                        val descriptor = avgCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }

                        gatt.writeDescriptor(descriptor, descriptor.value)
                        gatt.setCharacteristicNotification(avgCharacteristic, true)
                    }
                }
            }
            override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (characteristic.uuid == RESET_CHARACTERISTIC_UUID) {
                        activity.runOnUiThread {
                            HomeFragment().showToast("Measurement reset successful")
                        }
                    }
                } else {
                    activity.runOnUiThread {
                        HomeFragment().showToast("Failed to write characteristic")
                    }
                }
            }
            @SuppressLint("SetTextI18n")
            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                val value = characteristic.getStringValue(0).toFloatOrNull()
                val intValue = value?.toInt()
                activity.runOnUiThread {
                    if (characteristic.uuid == CURR_CHARACTERISTIC_UUID) {
                        HomeFragment().num.text = "$intValue BPM"
                        HomeFragment().setupWarningMessage()
                    } else if (characteristic.uuid == AVG_CHARACTERISTIC_UUID) {
                        HomeFragment().avgNum.text = "Avg: $intValue BPM"
                    }
                }
            }
        })
    }


}