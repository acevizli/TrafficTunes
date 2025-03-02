package com.acevizli.traffictunes.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log


class BLEScanner(private val context: Context, private val callback: (String, String) -> Unit) {

    private val bluetoothAdapter: BluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val bleScanner: BluetoothLeScanner?
        get() = bluetoothAdapter.bluetoothLeScanner
    private val scanHandler = Handler(Looper.getMainLooper())
    private var isScanning = false


    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onBatchScanResults(results: List<ScanResult>) {
            Log.d("BLEScanner", "📦 Batch Scan Results: ${results.size} devices found")

            for (result in results) {
                val scanRecord = result.scanRecord
                val alldata = scanRecord?.serviceData
                alldata?.forEach { (uuid, data) -> Log.d("BLEScanner", "UUID: $uuid, Data: ${String(data)}") }
                if (scanRecord != null) {
                    val mainData = scanRecord.getServiceData(BLEConstants.SERVICE_UUID)
                    if (mainData != null) {
                        val deviceName = scanRecord.deviceName?:"Unknown Device"
                        val songInfoMain = String(mainData)  // Decode first part
                        val fullSongInfo =  songInfoMain
                        Log.d("BLEScanner", "🎶 Found: [$deviceName] is playing: $fullSongInfo")
                        callback(deviceName,fullSongInfo) // Send to UI
                    }
                }
            }
        }


        override fun onScanFailed(errorCode: Int) {
            val errorMessage = when (errorCode) {
                ScanCallback.SCAN_FAILED_ALREADY_STARTED -> "Scan already started"
                ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "App registration failed"
                ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> "Internal error"
                ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> "Feature unsupported"
                else -> "Unknown error"
            }
            Log.e("BLEScanner", "Scan failed: $errorMessage (Error Code: $errorCode)")
        }
    }


    @SuppressLint("MissingPermission")
    fun startScanning() {
        if (!BLEHelper.isBluetoothSupported(context)) {
            Log.e("BLEAdvertiser", "Bluetooth is OFF, requesting user to enable it.")
            return
        }

        if (bleScanner == null) {
            Log.e("BLEScanner", "BLE Scanner not available")
            return
        }

        if (isScanning) {
            Log.w("BLEScanner", "⚠️ Scanning already in progress")
            return
        }

        val scanFilters = listOf(
            ScanFilter.Builder()
                .build()
        )


        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(5000) // ✅ Collect results for 5 seconds before delivering them
            .build()

        isScanning = true
        bleScanner?.startScan(scanFilters, scanSettings, scanCallback)
        Log.d("BLEScanner", "🔍 Started BLE scanning with batch reporting")
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        if (isScanning) {
            bleScanner?.stopScan(scanCallback)
            isScanning = false
            Log.d("BLEScanner", "🛑 Stopped BLE scanning")
        }
    }

}
