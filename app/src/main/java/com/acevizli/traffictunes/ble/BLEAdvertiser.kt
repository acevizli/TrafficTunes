package com.acevizli.traffictunes.ble

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log

class BLEAdvertiser(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter =
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter
    private val advertiser: BluetoothLeAdvertiser? = bluetoothAdapter.bluetoothLeAdvertiser

    @SuppressLint("MissingPermission")
    fun startAdvertising(songInfo: String) {
        if (advertiser == null) {
            Log.e("BLEAdvertiser", "BLE Advertiser not available")
            return
        }

        val advertiseData = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB")) // Example UUID
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addManufacturerData(0xFF, songInfo.toByteArray()) // Encode song info
            .build()

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(false)
            .setTimeout(0)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .build()

        advertiser.startAdvertising(settings, advertiseData, object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                Log.d("BLEAdvertiser", "Advertising started")
            }

            override fun onStartFailure(errorCode: Int) {
                Log.e("BLEAdvertiser", "Advertising failed: $errorCode")
            }
        })
    }

    @SuppressLint("MissingPermission")
    fun stopAdvertising() {
        advertiser?.stopAdvertising(object : AdvertiseCallback() {})
        Log.d("BLEAdvertiser", "Stopped BLE advertising")
    }
}
