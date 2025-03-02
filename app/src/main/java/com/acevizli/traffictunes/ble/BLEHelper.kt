package com.acevizli.traffictunes.ble

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.pm.PackageManager

object BLEHelper {

    fun isBluetoothSupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)
    }

    fun isBluetoothEnabled(): Boolean {
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        return bluetoothAdapter?.isEnabled == true
    }
}
