package com.example.pulsmesserv2.bluetooth

import android.Manifest
import android.annotation.SuppressLint
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
import com.example.pulsmesserv2.MainActivity
import com.example.pulsmesserv2.utils.ProgressDialogCallback
import com.example.pulsmesserv2.R
import com.example.pulsmesserv2.utils.ToastUtil

import java.util.UUID

object BluetoothHandler {

    private lateinit var listener: BluetoothDataListener

    private val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
    private val AVG_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
    private val CURR_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
    private val START_CHARACTERISTIC_UUID: UUID =
        UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")

    private var scanCallback: ScanCallback? = null
    private val foundDevices = HashSet<String>()

    private var connectedDeviceName: String? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val SCAN_PERIOD: Long = 5000
    private const val REQUEST_BLUETOOTH_PERMISSION = 4
    fun initialize(listener: BluetoothDataListener) {
        this.listener = listener
    }



    fun startMeasurement(context: Context) {

        if (bluetoothGatt == null) {
            ToastUtil.showToast(context,"Keine Verbindung mit einem Gerät")
            Log.e("ResetMeasurement", "bluetoothGatt NULL")
            return
        }

        val service = bluetoothGatt?.getService(SERVICE_UUID)
        if (service == null) {
            Log.e("ResetMeasurement", "Service not found")
            return
        }

        val resetCharacteristic = service.getCharacteristic(START_CHARACTERISTIC_UUID)
        if (resetCharacteristic == null) {
            Log.e("ResetMeasurement", "Reset characteristic not found")
            return
        }

        resetCharacteristic.value = byteArrayOf(0x01)
        Log.d("ResetCharacteristic", "Value: ${resetCharacteristic.value.contentToString()}")

        // Überprüfen der Bluetooth-Berechtigung
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("ResetCharacteristic", "Bluetooth permission not granted")
            return
        }


        val success = bluetoothGatt?.writeCharacteristic(resetCharacteristic)
        if (success == true) {
            println("Reset successful")
            listener.onCurrentMeasuring(true)

            Handler(Looper.getMainLooper()).postDelayed({

                listener.onCurrentMeasuring(false)
            }, 20000) // 20 Sekunden Timer
        } else {
            println("Reset not successful")
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun startScan(bAdapter: BluetoothAdapter, context: Context, activity: MainActivity, progressDialogCallback: ProgressDialogCallback) {
        val bluetoothLeScanner = bAdapter.bluetoothLeScanner

        val scanResults: MutableList<ScanResult> = mutableListOf()
        foundDevices.clear() // Leeren des Sets bei jedem neuen Scan

        scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)

                val device = result.device
                if(device.name !== null) {
                    if(device.name.contains("BPM")) {
                        val deviceAddress = device.address

                        // Prüfen, ob das Gerät bereits gefunden wurde
                        if (foundDevices.add(deviceAddress)) {
                            // Wenn Gerät ist neu, zur Liste hinzufügen
                            scanResults.add(result)
                            val deviceName = if (ActivityCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                return
                            } else {
                                device.name ?: "Unknown Device"
                            }
                            val scanResultText = "$deviceName - $deviceAddress"
                            Log.d("BluetoothScan", "Scan result: $scanResultText")
                        }

                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                Log.e("BluetoothScan", "Scan failed with error code $errorCode")
            }
        }

        if (ActivityCompat.checkSelfPermission(context,
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
                        connectToDevice(selectedDevice, context, activity)
                        ToastUtil.showToast(context,"Connecting to ${selectedDevice.name}")
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else {
                ToastUtil.showToast(context,"No devices found")
            }
        }, SCAN_PERIOD)
    }




    fun disconnectGatt(context: Context) {
        bluetoothGatt?.let { gatt ->
            if (ActivityCompat.checkSelfPermission(context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            gatt.disconnect()
            gatt.close()

            listener.onActiveDevice("Keine Verbindung")
            listener.onImageDeviceStatus(R.drawable.ic_no_connection)
            listener.onImageDeviceStatusColor(Color.RED)

            bluetoothGatt = null
        }
    }


    @RequiresApi(Build.VERSION_CODES.S)
    fun connectToDevice(device: BluetoothDevice, context: Context, activity: MainActivity) {
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_BLUETOOTH_PERMISSION
            )
            return
        }

        bluetoothGatt = device.connectGatt(context, false, object : BluetoothGattCallback() {
            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("BluetoothGattCallback", "Successfully connected to ${gatt.device.name}")
                    bluetoothGatt = gatt

                    if (ActivityCompat.checkSelfPermission(context,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        gatt.discoverServices()
                    } else {
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                            REQUEST_BLUETOOTH_PERMISSION
                        )
                    }
                    connectedDeviceName = gatt.device.name
                    activity.runOnUiThread {
                        listener.onCurrentBluetoothConnectionStatus(true)
                        listener.onActiveDevice("Gerät ${connectedDeviceName ?: "Unbekannt"} verbunden ")
                        listener.onImageDeviceStatus(R.drawable.ic_yes_connection)
                        listener.onImageDeviceStatusColor(Color.GREEN)
                    }
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("BluetoothGattCallback", "Disconnected from ${gatt.device.name}")
                    bluetoothGatt = null
                    activity.runOnUiThread {
                        listener.onCurrentBluetoothConnectionStatus(false)
                        listener.onActiveDevice("Keine Verbindung ")
                        listener.onImageDeviceStatus(R.drawable.ic_no_connection)
                        listener.onImageDeviceStatusColor(Color.RED)
                    }
                }
            }

            override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(SERVICE_UUID)
                    if (service != null && descriptor.characteristic.uuid == CURR_CHARACTERISTIC_UUID) {
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
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                gatt.writeDescriptor(descriptor)
                gatt.setCharacteristicNotification(characteristic, true)
            }

            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    val service = gatt.getService(SERVICE_UUID)
                    if (service == null) {
                        Log.e("onServicesDiscovered", "Service not found")
                        return
                    }
                    Log.d("onServicesDiscovered", "Service found: ${service.uuid}")

                    val currCharacteristic = service.getCharacteristic(CURR_CHARACTERISTIC_UUID)
                    if (currCharacteristic == null) {
                        Log.e("onServicesDiscovered", "Current characteristic not found")
                    } else {
                        enableNotification(gatt, currCharacteristic)
                    }

                    val avgCharacteristic = service.getCharacteristic(AVG_CHARACTERISTIC_UUID)
                    if (avgCharacteristic == null) {
                        Log.e("onServicesDiscovered", "Average characteristic not found")
                    } else {
                        enableNotification(gatt, avgCharacteristic)
                    }

                }
            }

            override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (characteristic.uuid == START_CHARACTERISTIC_UUID) {
                        activity.runOnUiThread {
                            ToastUtil.showToast(context,"Measurement started")
                        }
                    }

                } else {
                    activity.runOnUiThread {

                        ToastUtil.showToast(context,"Failed to write characteristic")
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            @SuppressLint("SetTextI18n")
            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
                val value = characteristic.getStringValue(0).toFloatOrNull()
                val intValue = value?.toInt()
                Log.d("Received value", intValue.toString())

                if (characteristic.uuid == CURR_CHARACTERISTIC_UUID) {
                    listener.onCurrentValueReceived(intValue ?: 0)
                } else if (characteristic.uuid == AVG_CHARACTERISTIC_UUID) {
                    listener.onAverageValueReceived(intValue ?: 0)
                }
            }
        })
    }
}