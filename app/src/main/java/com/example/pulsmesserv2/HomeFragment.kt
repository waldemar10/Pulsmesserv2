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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer

import com.example.pulsmesserv2.databinding.FragmentHomeBinding
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

private val SERVICE_UUID: UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")
private val  CHARACTERISTIC_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
private val  AVG_CHARACTERISTIC_UUID: UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
private val  CURR_CHARACTERISTIC_UUID: UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
private val  RESET_CHARACTERISTIC_UUID:UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")

private const val REQUEST_ENABLE_BT = 1
private const val REQUEST_CODE_BLUETOOTH_SCAN = 3
private const val REQUEST_BLUETOOTH_PERMISSION = 4
private const val REQUEST_LOCATION_PERMISSION = 5

class HomeFragment : Fragment(), ProgressDialogCallback  {


    private var bpmModelList: ArrayList<BPMModel> = ArrayList()

    private val myViewModel: MyViewModel by viewModels { MyViewModelFactory(requireContext()) }

    private val bluetoothViewModel: BluetoothViewModel by activityViewModels()

    private lateinit var bAdapter: BluetoothAdapter

    private var bluetoothGatt: BluetoothGatt? = null

    lateinit var num: TextView
    lateinit var avgNum: TextView
    private var progressDialog: Dialog? = null
    lateinit var ivStatusConnection: ImageView
    lateinit var tvBluetoothStatus: TextView
    private lateinit var cvWarning: CardView
    private lateinit var tvWarningText: TextView
    private lateinit var btnConnection: Button
    private lateinit var btnStart: Button
    private lateinit var btnSave: Button
    private lateinit var btnProtocol: Button


    var _binding: FragmentHomeBinding? = null
    val binding get() = _binding!!


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        bluetoothViewModel.scanCompleted.observe(viewLifecycleOwner, Observer {
            if (it) {
                closeProgressDialog()
            }
        })

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


                setupInsets(view.findViewById(R.id.homescreen))
                initializeUI(view)
                setupButtonListeners()


    }

    private fun setupInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun initializeUI(it: View) {

        progressDialog = Dialog(requireContext())

        bAdapter = BluetoothAdapter.getDefaultAdapter()

        tvBluetoothStatus = it.findViewById(R.id.tvBluetoothStatus)
        ivStatusConnection = it.findViewById(R.id.ivStatusConnection)
        btnConnection = it.findViewById(R.id.btnConnection)
        btnStart = it.findViewById(R.id.btnStart)
        btnSave = it.findViewById(R.id.btnSave)
        btnProtocol = it.findViewById(R.id.btnProtocol)
        num = it.findViewById(R.id.txtPulse)
        avgNum = it.findViewById(R.id.twNum)
        cvWarning = it.findViewById(R.id.cvWarning)
        tvWarningText = it.findViewById(R.id.tvWarningText)
        bpmModelList = setFile("bpm_data.txt")!!

        setupWarningMessage()
        setupConnectionStatus()
    }
    @SuppressLint("SetTextI18n")
    fun setupWarningMessage() {

        val pulse = num.text.toString().dropLast(4).toInt()

        when {
            pulse > 100 -> {
                cvWarning.visibility = CardView.VISIBLE
                tvWarningText.text =  "Ihre Herzfrequenz ist ungewöhnlich hoch." +
                        " Bitte setzen Sie sich hin, entspannen Sie sich und messen Sie Ihren Puls nach ein paar Minuten erneut. " +
                        "Wenn Ihre Herzfrequenz weiterhin erhöht bleibt, suchen Sie bitte einen Arzt auf."
            }
            pulse < 50 -> {
                cvWarning.visibility = CardView.VISIBLE
                tvWarningText.text = "Ihre Herzfrequenz ist ungewöhnlich niedrig. " +
                        "Bitte setzen Sie sich hin und ruhen Sie sich aus. Wenn Ihre Herzfrequenz weiterhin niedrig bleibt oder " +
                        "Sie sich unwohl fühlen, suchen Sie bitte umgehend einen Arzt auf."
            }
            else -> cvWarning.visibility = CardView.GONE
        }
    }

    private fun setupConnectionStatus() {
        ivStatusConnection.setImageResource(R.drawable.ic_no_connection)
        ivStatusConnection.setBackgroundColor(Color.RED)
        tvBluetoothStatus.text = "Keine Verbindung "
    }



    @RequiresApi(Build.VERSION_CODES.S)
    private fun setupButtonListeners() {

            btnConnection.setOnClickListener {
                if (bAdapter.isEnabled) {
                    showProgressDialog()
                    checkLocationPermission()
                }
            }

            btnSave.setOnClickListener {
                saveData()
            }

            btnStart.setOnClickListener {
                if (bAdapter.isEnabled) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        resetMeasurement()
                    }
                }
            }

            btnProtocol.setOnClickListener {
                ((requireActivity() as MainActivity)).navigateToProtocol()
            }


    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth enabled successfully
                showToast("Bluetooth activation completed")
            } else {
                // Bluetooth activation failed or canceled
                showToast("Bluetooth activation failed or canceled")
            }
        }
    }

    private fun resetMeasurement() {
        val service = bluetoothGatt?.getService(SERVICE_UUID)
        val resetCharacteristic = service?.getCharacteristic(RESET_CHARACTERISTIC_UUID)
        if (resetCharacteristic != null) {
            resetCharacteristic.value = byteArrayOf(0x01) // Beispielwert für Reset, abhängig von deinem spezifischen Gerät
            println("Reset Characteristic Value: ${resetCharacteristic.value.contentToString()}")

            if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
            }

            val success = bluetoothGatt?.writeCharacteristic(resetCharacteristic)
            if(success == true){
                showToast("Reset command sent successfully")
            } else {
                showToast("Failed to send reset command")
            }
        } else {
            showToast("Reset characteristic not found")
        }
    }


    fun setFile(filename: String): ArrayList<BPMModel>? {
        val model = myViewModel.readFromFile(filename)
        println("inhalt der Datei:" + model)
        return model
    }











    @Deprecated("Deprecated in Java")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_BLUETOOTH_SCAN) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                BluetoothHandler().startScan(bAdapter,requireContext(), MainActivity(), this)
            } else {
                showToast("Permission to scan for Bluetooth devices is not granted")
            }
        }
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                bluetoothGatt?.device?.let { device ->
                    BluetoothHandler().connectToDevice(device,requireContext(), MainActivity())
                }
            } else {
                showToast("Permission not granted")
            }
        }
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Die Standortberechtigung wurde erteilt, starten Sie den Bluetooth-Scan.
                BluetoothHandler().startScan(bAdapter,requireContext(), MainActivity(), this)
            } else {
                showToast("Permission to access location is not granted")
            }
        }
    }

    // Standort wichtig für Bluetooth-Scan BLE
    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {

            BluetoothHandler().startScan(bAdapter,requireContext(), MainActivity(), this)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showProgressDialog() {
        progressDialog = Dialog(requireContext())
        progressDialog?.setContentView(R.layout.progress_dialog)
        progressDialog?.setCancelable(true) // Benutzer kann nicht abbrechen
        progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparenter Hintergrund
        progressDialog?.show()
    }


    fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun closeProgressDialog() {
        progressDialog?.dismiss()
    }

}