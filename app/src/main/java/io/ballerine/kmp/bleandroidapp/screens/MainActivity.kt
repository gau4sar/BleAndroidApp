package io.ballerine.kmp.bleandroidapp.screens

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
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
import com.beepiz.bluetooth.gattcoroutines.GattConnection
import io.ballerine.kmp.bleandroidapp.ui.theme.BleAndroidAppTheme
import io.ballerine.kmp.bleandroidapp.utils.SimpleProgressBar
import io.ballerine.kmp.bleandroidapp.utils.hasPermission
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

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

    private var listOfCharacteristics = mutableStateListOf<BluetoothGattCharacteristic>()
    private var isBleDeviceIsConnecting = mutableStateOf(false)


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

                        if (listOfCharacteristics.isEmpty()) {
                            BluetoothDevicesListScreen(
                                listOfBleDevices = listOfBleDevices,
                                gotoSettings = {

                                    val intent =
                                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                    val uri: Uri =
                                        Uri.fromParts(
                                            "package",
                                            this@MainActivity.packageName,
                                            null
                                        )
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
                                },
                                onConnect = { bluetoothDevice ->
                                    GlobalScope.launch {
                                        bluetoothDevice.logGattServices()

                                        /*connectBleDevice(bluetoothDevice)*/
                                    }
                                },
                                isBleDeviceIsConnecting = isBleDeviceIsConnecting
                            )
                        } else {
                            BluetoothDeviceCharacteristicsScreen(listOfCharacteristics)
                        }
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
                /*GlobalScope.launch {
                    listOfBleDevices.first().device.logGattServices()
                }*/
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

    suspend fun BluetoothDevice.logGattServices(tag: String = "BleGattCoroutines") {

        isBleDeviceIsConnecting.value = true
        val deviceConnection = GattConnection(bluetoothDevice = this@logGattServices)

        Timber.d("deviceConnection $deviceConnection")

        try {
            deviceConnection.connect() // Suspends until connection is established
            Timber.d("deviceConnection.connect()")

            val gattServices = deviceConnection.discoverServices() // Suspends until completed

            Timber.d("deviceConnection.discoverServices()")

            gattServices.forEach {

                Timber.d("forEach ${it.characteristics}")

                it.characteristics.forEach {
                    try {
                        deviceConnection.readCharacteristic(it) // Suspends until characteristic is

                        /*if (it.uuid == HEART_RATE_SERVICE_UUID || it.uuid == HEART_RATE_MEASUREMENT_CHAR_UUID || it.uuid == HEART_RATE_CONTROL_POINT_CHAR_UUID) {

                            Log.d(
                                "logGattServices",
                                "HEART_RATE_SERVICE_UUID"
                            )
                        }*/


                        val messageBytes: ByteArray = it.getValue()

                        Timber.d("messageBytes -> $messageBytes")

                        listOfCharacteristics.add(it)
                        listOfCharacteristics.distinctBy {
                            it.uuid
                        }.let {
                            listOfCharacteristics.clear()
                            listOfCharacteristics.addAll(it)
                        }

                    } catch (e: Exception) {
                        Timber.e("Couldn't read characteristic with uuid: ${it.uuid}")
                    }
                }
            }
        } finally {
            deviceConnection.close() // Close when no longer used. Also triggers disconnect by default.
            Timber.d("logGattServices close")
            isBleDeviceIsConnecting.value = false
        }
    }

    /*val nameCharacteristic = "name"
    val uuidCharacteristic = "uuid"

    var mGattCharacteristics: MutableList<BluetoothGattCharacteristic> = mutableListOf()
    private fun displayGattServices(gattServices: List<BluetoothGattService>?) {
        if (gattServices == null) return
        var uuid: String?
        val unknownServiceString = "UnKnown Service"
        val unknownCharaString = "UnKnown Characteristics"
        val gattServiceData: MutableList<HashMap<String, String>> = mutableListOf()
        val gattCharacteristicData: MutableList<ArrayList<HashMap<String, String>>> =
            mutableListOf()

        mGattCharacteristics = mutableListOf()

        // Loops through available GATT Services.
        gattServices.forEach { gattService ->
            val currentServiceData = HashMap<String, String>()
            uuid = gattService.uuid.toString()
            currentServiceData[nameCharacteristic] =
                SampleGattAttributes.lookup(uuid, unknownServiceString)
            currentServiceData[uuidCharacteristic] = uuid!!
            gattServiceData += currentServiceData

            Log.d(
                "currentServiceData",
                "currentServiceData nameCharacteristic -> ${currentServiceData[nameCharacteristic]}"
            )
            Log.d(
                "currentServiceData",
                "currentServiceData uuidCharacteristic -> ${currentServiceData[uuidCharacteristic]}"
            )

            val gattCharacteristicGroupData: ArrayList<HashMap<String, String>> = arrayListOf()
            val gattCharacteristics = gattService.characteristics
            val charas: MutableList<BluetoothGattCharacteristic> = mutableListOf()

            // Loops through available Characteristics.
            gattCharacteristics.forEach { gattCharacteristic ->
                charas += gattCharacteristic
                val currentCharaData: HashMap<String, String> = hashMapOf()
                uuid = gattCharacteristic.uuid.toString()
                currentCharaData[nameCharacteristic] =
                    SampleGattAttributes.lookup(uuid, unknownCharaString)
                currentCharaData[uuidCharacteristic] = uuid!!
                gattCharacteristicGroupData += currentCharaData

                Log.d(
                    "currentServiceData",
                    "currentCharaData nameCharacteristic -> ${currentCharaData[nameCharacteristic]}"
                )
                Log.d(
                    "currentServiceData",
                    "currentCharaData uuidCharacteristic -> ${currentCharaData[uuidCharacteristic]}"
                )
            }
            mGattCharacteristics += charas
            gattCharacteristicData += gattCharacteristicGroupData
        }
    }*/

}

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/*
var HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb"
var CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"

*/
/**
 * This class includes a small subset of standard GATT attributes for demonstration purposes.
 *//*

object SampleGattAttributes {
    private val attributes: java.util.HashMap<String?, String?> =
        java.util.HashMap<String?, String?>()
    */
/*var HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb"
    var CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb"*//*


    init {
        // Sample Services.
        attributes["0000180d-0000-1000-8000-00805f9b34fb"] = "Heart Rate Service"
        attributes["0000180a-0000-1000-8000-00805f9b34fb"] = "Device Information Service"
        // Sample Characteristics.
        attributes[HEART_RATE_MEASUREMENT] = "Heart Rate Measurement"
        attributes["00002a29-0000-1000-8000-00805f9b34fb"] = "Manufacturer Name String"
    }

    fun lookup(uuid: String?, defaultName: String): String {
        val name = attributes[uuid]
        return name ?: defaultName
    }
}*/
