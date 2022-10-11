package io.ballerine.kmp.bleandroidapp.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.mutableStateListOf
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.beepiz.bluetooth.gattcoroutines.GattConnection
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import io.ballerine.kmp.bleandroidapp.screens.navigations.HomeScreenItems
import io.ballerine.kmp.bleandroidapp.screens.viewmodels.HomeViewModel
import io.ballerine.kmp.bleandroidapp.ui.theme.BleAndroidAppTheme
import io.ballerine.kmp.bleandroidapp.utils.customAnimatedComposable
import io.ballerine.kmp.bleandroidapp.utils.hasPermission
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : ComponentActivity() {

    lateinit var homeViewModel: HomeViewModel

    //bluetoothAdapter.isEnabled returns false when bluetooth is off
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        setContent {
            BleAndroidAppTheme {

                val startForResult =
                    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                        if (result.resultCode == Activity.RESULT_OK) {
                            scanBleDevices()
                        }
                    }

                val navController = rememberAnimatedNavController()

                AnimatedNavHost(
                    navController,
                    startDestination = HomeScreenItems.HomeScreen.route
                ) {

                    customAnimatedComposable(HomeScreenItems.HomeScreen.route) {

                        BluetoothDevicesListScreen(
                            isScanning = homeViewModel.isScanningInProgress.value,
                            listOfBleDevices = homeViewModel.listOfBleDevices,
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
                                    stopBleScan()
                                    scanBleDevices()
                                }
                            },
                            onBleDeviceClicked = { bluetoothDevice ->
                                homeViewModel.selectedBleDevice = bluetoothDevice

                                navController.navigate(HomeScreenItems.BluetoothDetailsScreen.route)
                            }
                        )
                    }

                    customAnimatedComposable(HomeScreenItems.BluetoothDetailsScreen.route) {

                        if (homeViewModel.selectedBleDevice != null) {
                            BluetoothDeviceCharacteristicsScreen(
                                navController = navController,
                                deviceName = homeViewModel.selectedBleDevice!!.name,
                                homeViewModel.listOfCharacteristics,
                                onConnectClick = {
                                    GlobalScope.launch {
                                        homeViewModel.selectedBleDevice!!.logGattServices()
                                    }
                                },
                                onDisconnect = {
                                    deviceConnection!!.close()
                                    listOfConnectedDevices.remove(homeViewModel.selectedBleDevice!!)
                                },
                                isConnected = listOfConnectedDevices.contains(homeViewModel.selectedBleDevice!!),
                                isBleDeviceConnecting = homeViewModel.isBleDeviceConnecting.value
                            )
                        }
                    }
                }
            }
        }
    }


    fun scanBleDevices() {
        if (homeViewModel.isScanningInProgress.value) {
            stopBleScan()
        } else {
            startBleScan()
        }
    }


    private fun startBleScan() {
        isAlreadyStopScanningIsCalled = false
        homeViewModel.listOfBleDevices.clear()

        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            bleScanner.startScan(null, homeViewModel.scanSettings, scanCallback)
            homeViewModel.isScanningInProgress.value = true
        }
    }


    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {

            val scanResult =
                homeViewModel.listOfBleDevices.find { it.device.address == result.device.address }

            if (scanResult != null) {
                // A scan result already exists with the same address

                homeViewModel.listOfBleDevices[homeViewModel.listOfBleDevices.indexOf(scanResult)] =
                    result
                stopScanning(10000L)
            } else {
                with(result.device) {
                    Timber.d("Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }

                homeViewModel.listOfBleDevices.add(result)
                /*homeViewModel.listOfBleDevices.add(result)
                homeViewModel.listOfBleDevices.add(result)
                homeViewModel.listOfBleDevices.add(result)
                homeViewModel.listOfBleDevices.add(result)*/

                if (homeViewModel.listOfBleDevices.isNotEmpty()) {
                    stopScanning()
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("onScanFailed : code $errorCode")
            stopBleScan()
            scanBleDevices()
        }
    }

    var isAlreadyStopScanningIsCalled = false
    private fun stopScanning(delayMillis: Long = 5000) {
        if (homeViewModel.isScanningInProgress.value && !isAlreadyStopScanningIsCalled) {
            Handler().postDelayed({
                homeViewModel.isScanningInProgress.value = false
            }, delayMillis)//delayMillis)
        }

        isAlreadyStopScanningIsCalled = true
    }


    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        stopScanning()
    }


    var listOfConnectedDevices = mutableStateListOf<BluetoothDevice>()
    var deviceConnection: GattConnection? = null

    //Connect the ble devices and load the characteristics
    private suspend fun BluetoothDevice.logGattServices() {

        homeViewModel.isBleDeviceConnecting.value = true
        deviceConnection = GattConnection(
            bluetoothDevice = this@logGattServices,
            connectionSettings = GattConnection.ConnectionSettings(
                autoConnect = true,
                allowAutoConnect = true,
                disconnectOnClose = false
            )
        )

        Timber.d("deviceConnection $deviceConnection")

        try {
            deviceConnection!!.connect() // Suspends until connection is established
            Timber.d("deviceConnection.connect()")

            val gattServices = deviceConnection!!.discoverServices() // Suspends until completed

            homeViewModel.isBleDeviceConnecting.value = false

            Timber.d("deviceConnection.discoverServices()")

            gattServices.forEach { bluetoothGattService ->

                Timber.d("forEach ${bluetoothGattService.characteristics}")

                bluetoothGattService.characteristics.forEach { bluetoothGattCharacteristic ->
                    try {
                        deviceConnection!!.readCharacteristic(bluetoothGattCharacteristic) // Suspends until characteristic is

                        /*if (it.uuid == HEART_RATE_SERVICE_UUID || it.uuid == HEART_RATE_MEASUREMENT_CHAR_UUID || it.uuid == HEART_RATE_CONTROL_POINT_CHAR_UUID) {

                            Log.d(
                                "logGattServices",
                                "HEART_RATE_SERVICE_UUID"
                            )
                        }*/

                        val messageBytes: ByteArray = bluetoothGattCharacteristic.value

                        Timber.d("messageBytes -> $messageBytes")

                        homeViewModel.listOfCharacteristics.add(bluetoothGattCharacteristic)
                        homeViewModel.listOfCharacteristics.distinctBy {
                            it.uuid
                        }.let {
                            homeViewModel.listOfCharacteristics.clear()
                            homeViewModel.listOfCharacteristics.addAll(it)
                        }

                    } catch (e: Exception) {
                        Timber.e("Couldn't read characteristic with uuid: ${bluetoothGattCharacteristic.uuid}")
                        listOfConnectedDevices.remove(this)
                    }
                }
            }

            listOfConnectedDevices.add(homeViewModel.selectedBleDevice!!)
        } catch (e: Exception) {

            homeViewModel.isBleDeviceConnecting.value = false
        } finally {

            /*deviceConnection.close() // Close when no longer used. Also triggers disconnect by default.
            Timber.d("logGattServices close")
            isBleDeviceConnecting.value = false*/
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
            val chars: MutableList<BluetoothGattCharacteristic> = mutableListOf()

            // Loops through available Characteristics.
            gattCharacteristics.forEach { gattCharacteristic ->
                chars += gattCharacteristic
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
            mGattCharacteristics += chars
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
