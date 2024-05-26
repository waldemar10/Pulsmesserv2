package com.example.pulsmesserv2

import MyViewModelFactory
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.pulsmesserv2.databinding.ActivityMainBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID


//val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
//val CHARACTERISTIC_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
//val AVG_CHARACTERISTIC_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
//val CURR_CHARACTERISTIC_UUID: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
//val RESET_CHARACTERISTIC_UUID:UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")

class MainActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_CODE_BLUETOOTH_SCAN = 3
    private val REQUEST_BLUETOOTH_PERMISSION = 4
    private val REQUEST_LOCATION_PERMISSION = 5
    private var bpmModelList: ArrayList<BPMModel> = ArrayList()
    private val myViewModel: MyViewModel by viewModels { MyViewModelFactory(applicationContext) }
    private lateinit var bAdapter: BluetoothAdapter
    private var connectedDeviceName: String? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private val SCAN_PERIOD: Long = 10000
    private var scanCallback: ScanCallback? = null
    private val foundDevices = HashSet<String>()

    private lateinit var num: TextView
    private lateinit var avgNum: TextView
    private var progressDialog: Dialog? = null

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

//        binding.btnData.setOnClickListener{
//            goToFragment(DataFragment())
//        }
//        binding.btnHome.setOnClickListener {
//            goToFragment(HomeFragment())
//        }
        onSectionAttached("Home")
        // Benutzer fragen ob er Bluetooth anschalten möchte bei App Start
        initializeBluetooth()

//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        setupInsets()
//        initializeBluetooth()
//        initializeUI()
//        setupButtonListeners()
//        if (savedInstanceState == null) {
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.fragment_container, StartFragment())
//                .commit()
//        }

    }

    fun goToFragment(fragment: Fragment){
        fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    fun onSectionAttached(fragmentName: String){
        when(fragmentName) {
            "Protocol" -> {
                goToFragment(ProtocolFragment())
            }

            "Home" -> {
                goToFragment(HomeFragment())
            }
        }
    }
    fun navigateToProtocol() {
        println("test")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ProtocolFragment())
            .addToBackStack(null)  // Fügt die Transaktion zum Backstack hinzu, um zurück navigieren zu können
            .commit()
    }
    fun navigateToHome(){
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .addToBackStack(null)  // Fügt die Transaktion zum Backstack hinzu, um zurück navigieren zu können
            .commit()
    }
//    private fun setupInsets() {
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//    }
//    private fun initializeUI() {
//        bpmModelList = setFile("bpm_data.txt")!!
//        num = findViewById(R.id.txtPulse)
//        avgNum = findViewById(R.id.twNum)
//
//        // Initial UI Setup
//        setupWarningMessage()
//        setupConnectionStatus()
//    }

//    @SuppressLint("SetTextI18n")
//    private fun setupWarningMessage() {
//        val showWarning = findViewById<CardView>(R.id.cvWarning)
//        val textWarning = findViewById<TextView>(R.id.tvWarningText)
//        val pulse = num.text.toString().dropLast(4).toInt()
//
//        when {
//            pulse > 100 -> {
//                showWarning.visibility = CardView.VISIBLE
//                textWarning.text =  "Ihre Herzfrequenz ist ungewöhnlich hoch." +
//                        " Bitte setzen Sie sich hin, entspannen Sie sich und messen Sie Ihren Puls nach ein paar Minuten erneut. " +
//                        "Wenn Ihre Herzfrequenz weiterhin erhöht bleibt, suchen Sie bitte einen Arzt auf."
//            }
//            pulse < 50 -> {
//                showWarning.visibility = CardView.VISIBLE
//                textWarning.text = "Ihre Herzfrequenz ist ungewöhnlich niedrig. " +
//                        "Bitte setzen Sie sich hin und ruhen Sie sich aus. Wenn Ihre Herzfrequenz weiterhin niedrig bleibt oder " +
//                        "Sie sich unwohl fühlen, suchen Sie bitte umgehend einen Arzt auf."
//            }
//            else -> showWarning.visibility = CardView.GONE
//        }
//    }
//
//    private fun setupConnectionStatus() {
//        // Standard keine Verbindung
//        val ivStatusConnection = findViewById<ImageView>(R.id.ivStatusConnection)
//        ivStatusConnection.setImageResource(R.drawable.ic_no_connection)
//        ivStatusConnection.setBackgroundColor(Color.RED)
//
//        val tvBluetoothStatus = findViewById<TextView>(R.id.tvBluetoothStatus)
//        tvBluetoothStatus.text = "Keine Verbindung "
//    }
    private fun initializeBluetooth() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bAdapter = bluetoothManager.adapter

        if (!bAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }


//    @RequiresApi(Build.VERSION_CODES.S)
//    private fun setupButtonListeners() {
//        findViewById<Button>(R.id.btnConnection).setOnClickListener {
//            if (bAdapter.isEnabled) {
//                showProgressDialog()
//                checkLocationPermission()
//            }
//        }
//
//        findViewById<Button>(R.id.btnSave).setOnClickListener {
//            saveData()
//        }
//
//        findViewById<Button>(R.id.btnStart).setOnClickListener {
//            if (bAdapter.isEnabled) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//                    resetMeasurement()
//                }
//            }
//        }
//
//    }

    private fun saveData() {
        val filename = "bpm_data.txt"
        val date = LocalDateTime.now(ZoneId.of("Europe/Berlin"))
        val formattedDate = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        val bpm = num.text.toString().dropLast(4).toInt()
        val bpmModel = BPMModel(bpm, formattedDate, false)

        bpmModelList.add(bpmModel)
        myViewModel.saveToFile(filename, bpmModelList)
        showToast("Datei gespeichert")
    }

    private fun navigateToProtocolPage() {
        val intent = Intent(this, ProtocolActivity::class.java)
        startActivity(intent)
    }
//    @RequiresApi(Build.VERSION_CODES.S)
//    private fun startScan() {
//        val bluetoothLeScanner = bAdapter.bluetoothLeScanner
//
//        val scanResults: MutableList<ScanResult> = mutableListOf()
//        foundDevices.clear() // Leeren des Sets bei jedem neuen Scan
//
//        scanCallback = object : ScanCallback() {
//            override fun onScanResult(callbackType: Int, result: ScanResult) {
//                super.onScanResult(callbackType, result)
//                val device = result.device
//                val deviceAddress = device.address
//
//                // Prüfen, ob das Gerät bereits gefunden wurde
//                if (foundDevices.add(deviceAddress)) {
//                    //Wenn Gerät ist neu, zur Liste hinzufügen
//                    scanResults.add(result)
//                    val deviceName = if (ActivityCompat.checkSelfPermission(
//                            this@MainActivity,
//                            Manifest.permission.BLUETOOTH_CONNECT
//                        ) != PackageManager.PERMISSION_GRANTED
//                    ) {
//                        return
//                    }else{
//                        device.name ?: "Unknown Device"
//                    }
//                    val scanResultText = "$deviceName - $deviceAddress"
//                    Log.d("BluetoothScan", "Scan result: $scanResultText")
//                }
//            }
//
//            override fun onScanFailed(errorCode: Int) {
//                super.onScanFailed(errorCode)
//                Log.e("BluetoothScan", "Scan failed with error code $errorCode")
//            }
//        }
//
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.BLUETOOTH_SCAN
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            return
//        }
//
//        bluetoothLeScanner.startScan(scanCallback)
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            bluetoothLeScanner.stopScan(scanCallback)
//
//            progressDialog?.dismiss() // Dialog schließen
//
//            // Überprüfen, ob mindestens ein Gerät gefunden wurde
//            val deviceNames = scanResults.mapNotNull { it.device.name }.toTypedArray()
//            if (deviceNames.isNotEmpty()) {
//                AlertDialog.Builder(this@MainActivity)
//                    .setTitle("Select a device")
//                    .setItems(deviceNames) { _, which ->
//                        val selectedDevice = scanResults[which].device
//                        connectToDevice(selectedDevice)
//                        showToast("Connecting to ${selectedDevice.name}")
//                    }
//                    .setNegativeButton("Cancel") { dialog, _ ->
//                        dialog.dismiss()
//                    }
//                    .show()
//            } else {
//                showToast("No devices found")
//            }
//        }, SCAN_PERIOD)
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == REQUEST_ENABLE_BT) {
//            if (resultCode == Activity.RESULT_OK) {
//                // Bluetooth enabled successfully
//                showToast("Bluetooth activation completed")
//            } else {
//                // Bluetooth activation failed or canceled
//                showToast("Bluetooth activation failed or canceled")
//            }
//        }
//    }

//    private fun resetMeasurement() {
//        val service = bluetoothGatt?.getService(SERVICE_UUID)
//        val resetCharacteristic = service?.getCharacteristic(RESET_CHARACTERISTIC_UUID)
//        if (resetCharacteristic != null) {
//            resetCharacteristic.value = byteArrayOf(0x01) // Beispielwert für Reset, abhängig von deinem spezifischen Gerät
//            println("Reset Characteristic Value: ${resetCharacteristic.value.contentToString()}")
//
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//                return
//            }
//
//            val success = bluetoothGatt?.writeCharacteristic(resetCharacteristic)
//            if(success == true){
//                showToast("Reset command sent successfully")
//            } else {
//                showToast("Failed to send reset command")
//            }
//        } else {
//            showToast("Reset characteristic not found")
//        }
//    }
//    private fun enableNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
//        val descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
//        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            return
//        }
//        gatt.writeDescriptor(descriptor)
//        gatt.setCharacteristicNotification(characteristic, true)
//    }
//    override fun onDestroy() {
//        super.onDestroy()
//        println("onDestroy")
//        disconnectGatt()
//    }
//    override fun onPause() {
//        super.onPause()
//        println("onPause")
//        disconnectGatt()
//    }
//    private fun disconnectGatt() {
//        bluetoothGatt?.let { gatt ->
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.BLUETOOTH_CONNECT
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//
//                return
//            }
//            gatt.disconnect()
//            gatt.close()
//            val ivStatusConnection = findViewById<ImageView>(R.id.ivStatusConnection)
//            val tvBluetoothStatus = findViewById<TextView>(R.id.tvBluetoothStatus)
//            tvBluetoothStatus.text = "Keine Verbindung "
//            ivStatusConnection.setImageResource(R.drawable.ic_no_connection)
//            ivStatusConnection.setBackgroundColor(Color.RED)
//            bluetoothGatt = null
//        }
//    }
//    @RequiresApi(Build.VERSION_CODES.S)
//    private fun connectToDevice(device: BluetoothDevice) {
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH_PERMISSION)
//            return
//        }
//
//        bluetoothGatt = device.connectGatt(this, false, object : BluetoothGattCallback() {
//            override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
//                val ivStatusConnection = findViewById<ImageView>(R.id.ivStatusConnection)
//                val tvBluetoothStatus = findViewById<TextView>(R.id.tvBluetoothStatus)
//                if (newState == BluetoothProfile.STATE_CONNECTED) {
//                    if (ActivityCompat.checkSelfPermission(this@MainActivity, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
//                        gatt.discoverServices()
//                    } else {
//                        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), REQUEST_BLUETOOTH_PERMISSION)
//                    }
//                    connectedDeviceName = gatt.device.name
//                    runOnUiThread {
//                        tvBluetoothStatus.text = "Gerät ${connectedDeviceName ?: "Unbekannt"} verbunden "
//                        ivStatusConnection.setImageResource(R.drawable.ic_yes_connection)
//                        ivStatusConnection.setBackgroundColor(Color.GREEN)
//                    }
//                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
//                    runOnUiThread {
//                        showToast("Disconnected from device")
//                        tvBluetoothStatus.text = "Keine Verbindung "
//                        ivStatusConnection.setImageResource(R.drawable.ic_no_connection)
//                        ivStatusConnection.setBackgroundColor(Color.RED)
//                    }
//                }
//            }
//            override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    val service = gatt.getService(SERVICE_UUID)
//                    if (descriptor.characteristic.uuid == CURR_CHARACTERISTIC_UUID) {
//                        val avgCharacteristic = service.getCharacteristic(AVG_CHARACTERISTIC_UUID)
//                        if (avgCharacteristic != null) {
//                            enableNotification(gatt, avgCharacteristic)
//                        }
//                    }
//                }
//            }
//            @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//            override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    val service = gatt.getService(SERVICE_UUID)
//                    val currCharacteristic = service.getCharacteristic(CURR_CHARACTERISTIC_UUID)
//                    val avgCharacteristic = service.getCharacteristic(AVG_CHARACTERISTIC_UUID)
//
//                    if (currCharacteristic != null) {
//                        enableNotification(gatt, currCharacteristic)
//                    }
//
//                    if (currCharacteristic != null) {
//                        val descriptor = currCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
//                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                        if (ActivityCompat.checkSelfPermission(
//                                this@MainActivity,
//                                Manifest.permission.BLUETOOTH_CONNECT
//                            ) != PackageManager.PERMISSION_GRANTED
//                        ) {
//                            return
//                        }
//
//                        gatt.writeDescriptor(descriptor, descriptor.value)
//                        gatt.setCharacteristicNotification(currCharacteristic, true)
//                    }
//
//                    if (avgCharacteristic != null) {
//                        val descriptor = avgCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
//                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                        if (ActivityCompat.checkSelfPermission(
//                                this@MainActivity,
//                                Manifest.permission.BLUETOOTH_CONNECT
//                            ) != PackageManager.PERMISSION_GRANTED
//                        ) {
//                            return
//                        }
//
//                        gatt.writeDescriptor(descriptor, descriptor.value)
//                        gatt.setCharacteristicNotification(avgCharacteristic, true)
//                    }
//                }
//            }
//            override fun onCharacteristicWrite(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
//                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    if (characteristic.uuid == RESET_CHARACTERISTIC_UUID) {
//                        runOnUiThread {
//                            showToast("Measurement reset successful")
//                        }
//                    }
//                } else {
//                    runOnUiThread {
//                        showToast("Failed to write characteristic")
//                    }
//                }
//            }
//            @SuppressLint("SetTextI18n")
//            override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
//                val value = characteristic.getStringValue(0).toFloatOrNull()
//                val intValue = value?.toInt()
//                runOnUiThread {
//                    if (characteristic.uuid == CURR_CHARACTERISTIC_UUID) {
//                        num.text = "$intValue BPM"
//                        setupWarningMessage() // Aktualisiere Warntext basierend auf neuem Wert
//                    } else if (characteristic.uuid == AVG_CHARACTERISTIC_UUID) {
//                        avgNum.text = "Avg: $intValue BPM" // Setze durchschnittliche BPM-Werte
//                    }
//                }
//            }
//        })
//    }



//    private fun updateWarningText() {
//        val bpmValue = num.text.toString().dropLast(4).toIntOrNull()
//        val showWarning = findViewById<CardView>(R.id.cvWarning)
//        val textWarning = findViewById<TextView>(R.id.tvWarningText)
//        if (bpmValue != null) {
//            when {
//                bpmValue > 100 -> {
//                    showWarning.visibility = CardView.VISIBLE
//                    textWarning.text = "Herzfrequenz hoch. Bitte kontrollieren Sie Ihre Herzfrequenz. Schwach"
//                }
//                bpmValue < 50 -> {
//                    showWarning.visibility = CardView.VISIBLE
//                    textWarning.text = "Herzfrequenz niedrig. Bitte kontrollieren Sie Ihre Herzfrequenz. Gefahr"
//                }
//                else -> {
//                    showWarning.visibility = CardView.GONE
//                }
//            }
//        }
//    }






//    fun setFile(filename: String): ArrayList<BPMModel>? {
//        val model = myViewModel.readFromFile(filename)
//        println("inhalt der Datei:" + model)
//        println("CALL")
//        return model
//    }











//    @RequiresApi(Build.VERSION_CODES.S)
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_CODE_BLUETOOTH_SCAN) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                startScan()
//            } else {
//                showToast("Permission to scan for Bluetooth devices is not granted")
//            }
//        }
//        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
//            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                bluetoothGatt?.device?.let { device ->
//                    connectToDevice(device)
//                }
//            } else {
//                showToast("Permission not granted")
//            }
//        }
//        if (requestCode == REQUEST_LOCATION_PERMISSION) {
//            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Die Standortberechtigung wurde erteilt, starten Sie den Bluetooth-Scan.
//                startScan()
//            } else {
//                showToast("Permission to access location is not granted")
//            }
//        }
//    }




    // Standort wichtig für Bluetooth-Scan BLE
//    @RequiresApi(Build.VERSION_CODES.S)
//    private fun checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
//        } else {
//
//            startScan()
//        }
//    }








//    private fun showProgressDialog() {
//        progressDialog = Dialog(this)
//        progressDialog?.setContentView(R.layout.progress_dialog)
//        progressDialog?.setCancelable(false) // Benutzer kann nicht abbrechen
//        progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparenter Hintergrund
//        progressDialog?.show()
//    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
