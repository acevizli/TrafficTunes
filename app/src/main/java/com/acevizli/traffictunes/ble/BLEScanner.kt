package com.acevizli.traffictunes.ble


import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BLEScanner(private val context: Context, private val callback: (String) -> Unit) {

    private val bluetoothAdapter: BluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val bleScanner: BluetoothLeScanner? = bluetoothAdapter.bluetoothLeScanner
    private val scanHandler = Handler(Looper.getMainLooper())

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            result.scanRecord?.bytes?.let { data ->
                val songInfo = String(data)  // Decode song info
                Log.d("BLEScanner", "Found device playing: $songInfo")
                callback(songInfo) // Send result to UI
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLEScanner", "Scan failed: $errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (!hasBLEPermissions()) {
            Log.e("BLEScanner", "Missing Bluetooth permissions")
            return
        }

        if (bleScanner == null) {
            Log.e("BLEScanner", "BLE Scanner not available")
            return
        }

        val scanFilters = listOf<ScanFilter>() // No filter, detect all devices
        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bleScanner.startScan(scanFilters, scanSettings, scanCallback)
        Log.d("BLEScanner", "Started BLE scanning")
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (!hasBLEPermissions()) return
        bleScanner?.stopScan(scanCallback)
        Log.d("BLEScanner", "Stopped BLE scanning")
    }

    private fun hasBLEPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}
