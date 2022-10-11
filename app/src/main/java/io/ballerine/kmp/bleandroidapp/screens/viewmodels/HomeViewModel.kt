package io.ballerine.kmp.bleandroidapp.screens.viewmodels

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    var isBleDeviceConnecting = mutableStateOf(false)
    var isScanningInProgress = mutableStateOf(false)
    var selectedBleDevice: BluetoothDevice? = null

    var listOfBleDevices = mutableStateListOf<ScanResult>()
    var listOfCharacteristics = mutableStateListOf<BluetoothGattCharacteristic>()

    val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()


}