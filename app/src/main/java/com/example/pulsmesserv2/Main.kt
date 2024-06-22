package com.example.pulsmesserv2

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.pulsmesserv2.databinding.ActivityMainBinding
import com.example.pulsmesserv2.fragments.HomeFragment
import com.example.pulsmesserv2.fragments.ProtocolFragment
import com.example.pulsmesserv2.fragments.ResultFragment


class MainActivity : AppCompatActivity() {

    private val REQUEST_ENABLE_BT = 1

    private lateinit var bAdapter: BluetoothAdapter

    private lateinit var fragmentManager: FragmentManager
    private lateinit var binding: ActivityMainBinding

    private var homeFragment: HomeFragment? = null

    private val bluetoothReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)

                homeFragment?.onBluetoothStateChanged(state)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        onSectionAttached("Home")
        // Benutzer fragen, ob er Bluetooth anschalten möchte bei App Start
        initializeBluetooth()

//        homeFragment = HomeFragment()
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, homeFragment!!)
//            .commit()

        registerReceiver(bluetoothReceiver, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))

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

            "Result" -> {
                goToFragment(ResultFragment())
            }
        }
    }
    fun navigateToResult() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ResultFragment())
            .addToBackStack(null)  // Fügt die Transaktion zum Backstack hinzu, um zurück navigieren zu können
            .commit()
    }
    fun navigateToProtocol() {

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

    @RequiresApi(Build.VERSION_CODES.S)
    private fun initializeBluetooth() {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bAdapter = bluetoothManager.adapter

        if (!bAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                    REQUEST_ENABLE_BT
                )
                return
            }
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(bluetoothReceiver)
    }
}
