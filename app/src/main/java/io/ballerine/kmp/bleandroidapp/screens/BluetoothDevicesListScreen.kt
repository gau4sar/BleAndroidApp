package io.ballerine.kmp.bleandroidapp.screens

import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.ballerine.kmp.bleandroidapp.R
import io.ballerine.kmp.bleandroidapp.ui.theme.Blue
import io.ballerine.kmp.bleandroidapp.ui.theme.LightBlue
import io.ballerine.kmp.bleandroidapp.ui.theme.LightGrey
import io.ballerine.kmp.bleandroidapp.utils.CustomMultiplePermissionView
import io.ballerine.kmp.bleandroidapp.utils.necessaryPermissions
import io.ballerine.kmp.bleandroidapp.widgets.*

@Composable
fun BluetoothDevicesListScreen(
    listOfBleDevices: SnapshotStateList<ScanResult>,
    scanDevices: () -> Unit,
    gotoSettings: () -> Unit,
    onBleDeviceClicked: (BluetoothDevice) -> Unit,
    isScanning: Boolean
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = LightGrey),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {

        if (isScanning) {
            ScanningScreen()
        } else {
            CustomMultiplePermissionView(
                permission = necessaryPermissions(),
                permissionsNotAvailableContent = {

                    ButtonGoToSettings(gotoSettings = gotoSettings)
                }, permissionsNotGrantedContent = {
                    ButtonGoToSettings(gotoSettings = gotoSettings)
                }
            ) {

                Column(
                    modifier = Modifier
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                ) {
                    Card(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(14.dp)
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Blue,
                                            LightBlue
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                VeryBigWhiteTextStyle(text = "Dining Room")

                                Spacer16Dp()

                                Icon(
                                    painter = painterResource(id = R.drawable.ic_baseline_replay_24),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.clickable {
                                        scanDevices()
                                    }
                                )
                            }

                            Spacer8Dp()

                            NormalWhiteTextStyle(text = "${listOfBleDevices.size} devices")

                            Spacer8Dp()
                        }
                    }
                }

                LazyVerticalGrid(
                    modifier = Modifier.padding(8.dp),
                    columns = GridCells.Fixed(2)
                ) {
                    items(listOfBleDevices.size) {
                        BluetoothDeviceItem(
                            deviceName = listOfBleDevices[it].device.name,
                            isConnected = false
                        ) {
                            onBleDeviceClicked(
                                listOfBleDevices[it].device
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BluetoothDeviceItem(deviceName: String, isConnected: Boolean, onClick: () -> Unit) {

    Column(
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 8.dp)
            .clickable {
                onClick()
            }
    ) {
        Card(
            elevation = 4.dp,
            shape = RoundedCornerShape(14.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.White)
                    .padding(10.dp)
            ) {

                NormalBlackTextStyle(text = deviceName)

                Spacer4Dp()

                MediumGreyTextStyle(if (isConnected) "Connected" else "Not connected")

                Spacer16Dp()

                Image(
                    painter = painterResource(id = R.drawable.img_smart_watch2),
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                )

                Spacer8Dp()
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
