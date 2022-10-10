package io.ballerine.kmp.bleandroidapp.screens

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.ballerine.kmp.bleandroidapp.utils.CustomMultiplePermissionView
import io.ballerine.kmp.bleandroidapp.utils.SimpleProgressBar
import io.ballerine.kmp.bleandroidapp.utils.necessaryPermissions

@Composable
fun BluetoothDevicesListScreen(
    listOfBleDevices: SnapshotStateList<ScanResult>,
    scanDevices: () -> Unit,
    gotoSettings: () -> Unit,
    onConnect: (BluetoothDevice) -> Unit,
    isBleDeviceIsConnecting: MutableState<Boolean>
) {

    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        if (isBleDeviceIsConnecting.value) {
            SimpleProgressBar()
        } else {
            CustomMultiplePermissionView(
                permission = necessaryPermissions(),
                permissionsNotAvailableContent = {

                    ButtonGoToSettings(gotoSettings = gotoSettings)
                }, permissionsNotGrantedContent = {
                    ButtonGoToSettings(gotoSettings = gotoSettings)
                }
            ) {

                Button(onClick = {
                    scanDevices()
                }) {
                    Text(text = "Scan devices")
                }

                LazyColumn {
                    itemsIndexed(listOfBleDevices) { index, item ->

                        Button(onClick = { onConnect(item.device) })
                        {
                            Text(text = "${item.device.name} ${listOfBleDevices.first().rssi}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ButtonGoToSettings(gotoSettings: () -> Unit) {
    Button(onClick = {
        gotoSettings()
    }) { Text(text = "Grant required permissions") }
}
