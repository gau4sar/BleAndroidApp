package io.ballerine.kmp.bleandroidapp.screens

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import io.ballerine.kmp.bleandroidapp.ui.theme.BleAndroidAppTheme
import io.ballerine.kmp.bleandroidapp.utils.*

class MainActivity : ComponentActivity() {

    private var isScanning = mutableStateOf(false)

    private var listOfBleDevices = mutableStateListOf<ScanResult>()

    //bluetoothAdapter.isEnabled returns false when bluetooth is off
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BleAndroidAppTheme {

                val startForResult =
                    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            scanBleDevices()
                        }
                    }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isScanning.value) {

                        SimpleProgressBar()
                    } else {

                        BluetoothDevicesListScreen(
                            listOfBleDevices = listOfBleDevices,
                            gotoSettings = {

                                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri: Uri =
                                    Uri.fromParts("package", this@MainActivity.packageName, null)
                                intent.data = uri
                                ContextCompat.startActivity(this@MainActivity, intent, null)
                            },
                            scanDevices = {

                                if (!bluetoothAdapter.isEnabled) {

                                    val enableBtIntent =
                                        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)

                                    startForResult.launch(enableBtIntent)
                                } else {
                                    scanBleDevices()
                                }
                            })
                    }
                }
            }
        }
    }

    fun scanBleDevices() {
        if (isScanning.value) {
            stopBleScan()
        } else {
            startBleScan()
        }
    }

    private fun startBleScan() {
        listOfBleDevices.clear()

        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning.value = true
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {

            val scanResult = listOfBleDevices.find { it.device.address == result.device.address }

            if (scanResult != null) {
                // A scan result already exists with the same address

                listOfBleDevices[listOfBleDevices.indexOf(scanResult)] = result
            } else {
                with(result.device) {
                    Log.d(
                        "onScanResult",
                        "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address"
                    )
                }

                listOfBleDevices.add(result)
            }

            isScanning.value = false
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("onScanFailed", ": code $errorCode")
            isScanning.value = false

            stopBleScan()
            scanBleDevices()
        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning.value = false
    }
}