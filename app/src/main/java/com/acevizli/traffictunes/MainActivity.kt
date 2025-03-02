package com.acevizli.traffictunes

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.acevizli.traffictunes.ble.BLEAdvertiser
import com.acevizli.traffictunes.ble.BLEScanner
import com.acevizli.traffictunes.ble.BLEHelper

class MainActivity : AppCompatActivity() {

    private lateinit var scanner: BLEScanner
    private lateinit var advertiser: BLEAdvertiser
    private lateinit var txtSongInfo: TextView
    private lateinit var switchScan: SwitchCompat
    private lateinit var switchAdvertise: SwitchCompat
    private val detectedDevices = mutableMapOf<String, String>() // ✅ Store device -> song info

    // ✅ Register permission launcher before onCreate
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            Toast.makeText(this, "Permissions granted!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Permissions denied!", Toast.LENGTH_SHORT).show()
        }
    }
    private fun addDevice(deviceName: String, songInfo: String) {
        detectedDevices[deviceName] = songInfo // ✅ Store device -> song mapping
        updateUI()
    }

    // ✅ Update UI to display all detected devices and their music
    private fun updateUI() {
        val displayText = detectedDevices.entries.joinToString("\n") { (device, song) ->
            "🎶 $device: $song"
        }
        txtSongInfo.text = displayText // ✅ Show all devices and their music in UI
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtSongInfo = findViewById(R.id.txtSongInfo)
        switchScan = findViewById(R.id.switchScan)
        switchAdvertise = findViewById(R.id.switchAdvertise)

        if (!BLEHelper.isBluetoothSupported(this)) {
            Toast.makeText(this, "Bluetooth LE not supported!", Toast.LENGTH_LONG).show()
            return
        }

        scanner = BLEScanner(this) { deviceName, songInfo ->
            runOnUiThread {
                addDevice(deviceName, songInfo) // ✅ Store the new device & song
            }
        }
        advertiser = BLEAdvertiser(this)

        // ✅ Request permissions on startup
        BLEHelper.requestBluetoothPermissions(permissionLauncher)

        switchScan.setOnClickListener {
            if (!BLEHelper.isBluetoothEnabled(this)) {
                BLEHelper.requestBluetoothEnable(this)  // ✅ Ask user to enable Bluetooth
                switchScan.isChecked = false
                return@setOnClickListener
            }
            if(!BLEHelper.isLocationEnabled(this)) {
                BLEHelper.requestLocationEnable(this)
                switchScan.isChecked = false
                return@setOnClickListener
            }
            if(switchScan.isChecked) {
                if (BLEHelper.hasBluetoothPermissions(this)) {
                    scanner.startScanning()
                } else {
                    BLEHelper.requestBluetoothPermissions(permissionLauncher)
                    switchScan.isChecked = false
                }
            }
            else {
                scanner.stopScanning()
            }
        }

        switchAdvertise.setOnClickListener {
            if (!BLEHelper.isBluetoothEnabled(this)) {
                BLEHelper.requestBluetoothEnable(this)  // ✅ Ask user to enable Bluetooth
                switchAdvertise.isChecked = false
                return@setOnClickListener
            }
            if(!BLEHelper.isLocationEnabled(this)) {
                BLEHelper.requestLocationEnable(this)
                switchAdvertise.isChecked = false
                return@setOnClickListener
            }

            if(switchAdvertise.isChecked) {
                if (BLEHelper.hasBluetoothPermissions(this)) {
                    advertiser.startAdvertising("Lose Yourself - Eminem")
                } else {
                    switchAdvertise.isChecked = false
                    BLEHelper.requestBluetoothPermissions(permissionLauncher)
                }
            }
            else {
                advertiser.stopAdvertising()
            }
        }
    }


}
