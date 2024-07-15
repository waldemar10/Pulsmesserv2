package com.example.pulsmesserv2.fragments

import com.example.pulsmesserv2.viewmodel.MyViewModelFactory
import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout

import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels

import com.example.pulsmesserv2.BPMModel
import com.example.pulsmesserv2.viewmodel.BpmViewModel
import com.example.pulsmesserv2.MainActivity
import com.example.pulsmesserv2.viewmodel.MyViewModel
import com.example.pulsmesserv2.utils.ProgressDialogCallback
import com.example.pulsmesserv2.R
import com.example.pulsmesserv2.bluetooth.BluetoothDataListener
import com.example.pulsmesserv2.bluetooth.BluetoothHandler
import com.example.pulsmesserv2.bluetooth.BluetoothStatusListener
import com.example.pulsmesserv2.databinding.FragmentHomeBinding
import com.example.pulsmesserv2.utils.Animations
import com.example.pulsmesserv2.utils.ToastUtil
import com.example.pulsmesserv2.utils.applyFontToMenuItem
import com.google.android.material.bottomnavigation.BottomNavigationView


private const val REQUEST_LOCATION_PERMISSION = 5
const val FILENAME = "bpm_data.txt"
const val MEASURING_TIME:Long = 20000
class HomeFragment : Fragment(), ProgressDialogCallback, BluetoothDataListener,
    BluetoothStatusListener {

    private var bpmModelList: ArrayList<BPMModel> = ArrayList()

    private val myViewModel: MyViewModel by viewModels { MyViewModelFactory(requireContext()) }
    private val bpmViewModel: BpmViewModel by activityViewModels()

    private lateinit var bAdapter: BluetoothAdapter

    private lateinit var txtPulse: TextView
    private lateinit var avgNum: TextView
    private lateinit var circle: ConstraintLayout
    private lateinit var ivStatusConnection: ImageView
    private lateinit var tvBluetoothStatus: TextView
    private lateinit var tvMeasuringTime: TextView
    private lateinit var tvMeasuringText: TextView

    private var progressDialog: Dialog? = null


    private lateinit var itemData: MenuItem
    private lateinit var itemScan: MenuItem
    private lateinit var itemStart: MenuItem

    private var currentMeasuring: Boolean = true

    private var _binding: FragmentHomeBinding? = null

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        Log.d("HomeFragment", "myViewModel initialized: $myViewModel")

        return _binding!!.root
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInsets(view.findViewById(R.id.homescreen))
        initializeUI(view)
        setupButtonListeners()
        BluetoothHandler.initialize(this)
    }

@SuppressLint("SetTextI18n")
override fun onCurrentValueReceived(value: Int) {
    activity?.runOnUiThread {
        bpmViewModel.setBPM(value)
        txtPulse.text = value.toString()
    }
}

    override fun onCurrentMeasuring(isMeasuring: Boolean) {
        currentMeasuring = isMeasuring

        if(isMeasuring){
            //Show measuring text and remaining time
            tvMeasuringText.visibility = View.VISIBLE
            tvMeasuringTime.visibility = View.VISIBLE
            Animations(requireContext()).growAndShrink(circle)
            Animations(requireContext()).colorChange(circle)

            object : CountDownTimer(MEASURING_TIME, 1000) {
                override fun onTick(millisUntilFinished: Long) {

                    val secondsRemaining = millisUntilFinished / 1000
                    tvMeasuringTime.text = "$secondsRemaining Sekunden verbleiben"
                }

                override fun onFinish() {
                    // Wenn der Timer abgelaufen ist
                    tvMeasuringTime.text = "Fertig!"
                }
            }.start()
        }
        //If the measuring is finished, navigate to the result screen
        if(!currentMeasuring){
            ((requireActivity() as MainActivity)).navigateToResult()
        }
    }
    @SuppressLint("SetTextI18n")
    override fun onAverageValueReceived(value: Int) {
        activity?.runOnUiThread {
            avgNum.text = "Avg: $value"
            bpmViewModel.setAvgBPM(value)
        }
    }

    override fun onActiveDevice(name: String) {
        activity?.runOnUiThread {
            tvBluetoothStatus.text = name
            bpmViewModel.setDeviceStatus(name)
        }
    }

    override fun onImageDeviceStatus(imageResources: Int) {
        activity?.runOnUiThread {
            ivStatusConnection.setImageResource(imageResources)
        }
    }

    override fun onImageDeviceStatusColor(color: Int) {
        activity?.runOnUiThread {
            ivStatusConnection.setBackgroundColor(color)
        }
    }

    override fun onCurrentBluetoothConnectionStatus(status: Boolean) {
        if(status){
         bpmViewModel.setConnectionStatus(true)
        }else{
            bpmViewModel.setConnectionStatus(false)
        }
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
        txtPulse = it.findViewById(R.id.txtPulse)
        avgNum = it.findViewById(R.id.tvAvgBpm)
        circle = it.findViewById(R.id.circleGroup)
        tvMeasuringText = it.findViewById(R.id.tvMeasuringText)
        tvMeasuringTime = it.findViewById(R.id.tvMeasuringTime)

        val bottomNavigationView: BottomNavigationView = it.findViewById(R.id.bottomNavigationView)
        itemData = bottomNavigationView.menu.findItem(R.id.action_data)
        itemScan = bottomNavigationView.menu.findItem(R.id.action_scan)
        itemStart = bottomNavigationView.menu.findItem(R.id.action_start)
        applyFontToMenuItem(requireContext(), bottomNavigationView.menu.findItem(R.id.action_data), R.font.kodchasan)
        applyFontToMenuItem(requireContext(), bottomNavigationView.menu.findItem(R.id.action_start), R.font.kodchasan)
        applyFontToMenuItem(requireContext(), bottomNavigationView.menu.findItem(R.id.action_scan), R.font.kodchasan)

        tvMeasuringText.visibility = View.GONE
        tvMeasuringTime.visibility = View.GONE

        // Definiere die Liste
        bpmModelList = setFile()!!

        setupConnectionStatus()
    }


    private fun setupConnectionStatus() {

        if(bpmViewModel.isConnected.value == true){
            ivStatusConnection.setImageResource(R.drawable.ic_yes_connection)
            ivStatusConnection.setBackgroundColor(Color.GREEN)
            tvBluetoothStatus.text = bpmViewModel.deviceName
        }else{
            ivStatusConnection.setImageResource(R.drawable.ic_no_connection)
            ivStatusConnection.setBackgroundColor(Color.RED)
            tvBluetoothStatus.text = "Keine Verbindung "
        }


    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun setupButtonListeners() {

        itemScan.setOnMenuItemClickListener {
            if (bAdapter.isEnabled) {
                showProgressDialog()
                checkLocationPermission()
            }else{
                ToastUtil.showToast(requireContext(), "Bluetooth ist nicht aktiviert")
            }
            true
        }


        itemStart.setOnMenuItemClickListener {

            if (bAdapter.isEnabled) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    BluetoothHandler.startMeasurement(requireContext())
                }
            }else{
                ToastUtil.showToast(requireContext(), "Bluetooth ist nicht aktiviert")
            }
            true
        }

        itemData.setOnMenuItemClickListener{
            ((requireActivity() as MainActivity)).navigateToProtocol()

            true
        }
    }



    private fun setFile(): ArrayList<BPMModel>? {
        val model = myViewModel.readFromFile(FILENAME)
        return model
    }

    // Standort wichtig für Bluetooth-Scan BLE
    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {

            BluetoothHandler.startScan(
                bAdapter,
                requireContext(),
                requireActivity() as MainActivity,
                this
            )
        }
    }

    private fun showProgressDialog() {
        progressDialog = Dialog(requireContext())
        progressDialog?.setContentView(R.layout.progress_dialog)
        progressDialog?.setCancelable(true)
        progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparenter Hintergrund
        progressDialog?.show()
    }

    override fun closeProgressDialog() {
        progressDialog?.dismiss()
    }

    override fun onBluetoothStateChanged(state: Int) {
        activity?.runOnUiThread {

            when (state) {
                BluetoothAdapter.STATE_OFF -> {
                    bpmViewModel.setConnectionStatus(false)
                    tvBluetoothStatus.text = "Bluetooth ist ausgeschaltet"
                    ivStatusConnection.setImageResource(R.drawable.ic_no_connection)
                    ivStatusConnection.setBackgroundColor(Color.RED)
                    BluetoothHandler.disconnectGatt(requireContext())
                }
                BluetoothAdapter.STATE_TURNING_ON -> {
                    bpmViewModel.setConnectionStatus(true)
                    tvBluetoothStatus.text = "Keine Verbindung"
                    ivStatusConnection.setImageResource(R.drawable.ic_no_connection)
                    ivStatusConnection.setBackgroundColor(Color.RED)
                }
            }
        }
    }
}