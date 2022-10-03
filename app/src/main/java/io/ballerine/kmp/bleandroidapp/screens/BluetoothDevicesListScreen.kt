package io.ballerine.kmp.bleandroidapp.screens

import android.bluetooth.le.ScanResult
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.ballerine.kmp.bleandroidapp.utils.CustomMultiplePermissionView
import io.ballerine.kmp.bleandroidapp.utils.necessaryPermissions

@Composable
fun BluetoothDevicesListScreen(
    listOfBleDevices: SnapshotStateList<ScanResult>,
    scanDevices: () -> Unit,
    gotoSettings: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

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

            if (listOfBleDevices.isNotEmpty()) {
                Text(text = "${listOfBleDevices.first().device} ${listOfBleDevices.first().rssi}")
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
