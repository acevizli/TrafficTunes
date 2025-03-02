package com.acevizli.traffictunes.ble

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.os.ParcelUuid
import android.util.Log
import java.nio.charset.StandardCharsets

class BLEAdvertiser(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter

    private val advertiser: BluetoothLeAdvertiser?
        get() = bluetoothAdapter.bluetoothLeAdvertiser





    @SuppressLint("MissingPermission")
    fun startAdvertising(songInfo: String) {
        if (!BLEHelper.isBluetoothSupported(context)) {
            Log.e("BLEAdvertiser", "Bluetooth is OFF, requesting user to enable it.")
            return
        }

        if (advertiser == null) {
            Log.e("BLEAdvertiser", "BLE Advertiser not available")
            return
        }
        val songBytes = songInfo.toByteArray(StandardCharsets.UTF_8)


        val advertiseData = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(false) // ✅ Use addServiceData instead of addManufacturerData
            .build()

        val scanResponseData = AdvertiseData.Builder()
            .addServiceData(BLEConstants.SERVICE_UUID, songBytes) // ✅ Send remaining song info
            .build()

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(false)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        val callback = object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.d("BLEAdvertiser", "Advertising started successfully with settings: $settingsInEffect")
            }

            override fun onStartFailure(errorCode: Int) {
                val errorMessage = when (errorCode) {
                    AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED -> "Advertising already started"
                    AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE -> "Data size too large"
                    AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED -> "Feature unsupported on this device"
                    AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR -> "Internal error"
                    AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS -> "Too many advertisers"
                    else -> "Unknown error"
                }
                Log.e("BLEAdvertiser", "Advertising failed: $errorMessage (Error Code: $errorCode)")
            }
        }

        advertiser?.startAdvertising(settings, advertiseData,scanResponseData, callback)
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        advertiser?.stopAdvertising(object : AdvertiseCallback() {})
        Log.d("BLEAdvertiser", "Stopped BLE advertising")
    }

    companion object {
        const val REQUEST_ENABLE_BT = 1
    }
}
