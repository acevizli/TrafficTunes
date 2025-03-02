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
import androidx.core.content.ContextCompat
import com.acevizli.traffictunes.ble.BLEAdvertiser
import com.acevizli.traffictunes.ble.BLEScanner
import com.acevizli.traffictunes.ble.BLEHelper

class MainActivity : AppCompatActivity() {

    private lateinit var scanner: BLEScanner
    private lateinit var advertiser: BLEAdvertiser
    private lateinit var txtSongInfo: TextView
    private lateinit var btnStartScan: Button
    private lateinit var btnStartAdvertise: Button

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        txtSongInfo = findViewById(R.id.txtSongInfo)
        btnStartScan = findViewById(R.id.btnStartScan)
        btnStartAdvertise = findViewById(R.id.btnStartAdvertise)

        if (!BLEHelper.isBluetoothSupported(this)) {
            Toast.makeText(this, "Bluetooth LE not supported!", Toast.LENGTH_LONG).show()
            return
        }

        scanner = BLEScanner(this) { songInfo ->
            runOnUiThread {
                txtSongInfo.text = "Nearby: $songInfo"
            }
        }
        advertiser = BLEAdvertiser(this)

        // ✅ Request permissions on startup
        requestBluetoothPermissions()

        btnStartScan.setOnClickListener {
            if (hasPermissions()) {
                scanner.startScanning()
            } else {
                requestBluetoothPermissions()
            }
        }

        btnStartAdvertise.setOnClickListener {
            if (hasPermissions()) {
                advertiser.startAdvertising("Song: Lose Yourself - Eminem")
            } else {
                requestBluetoothPermissions()
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED
        } else { // API 30 and below
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31+
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_ADVERTISE
                )
            )
        } else { // API 30 and below
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN
                )
            )
        }
    }
}
