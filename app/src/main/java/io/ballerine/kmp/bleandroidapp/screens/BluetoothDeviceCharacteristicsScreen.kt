package io.ballerine.kmp.bleandroidapp.screens

import android.bluetooth.BluetoothGattCharacteristic
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.ballerine.kmp.bleandroidapp.R
import io.ballerine.kmp.bleandroidapp.ui.theme.*
import io.ballerine.kmp.bleandroidapp.widgets.*

@Composable
fun BluetoothDeviceCharacteristicsScreen(
    navController: NavController,
    deviceName: String,
    listOfCharacteristics: SnapshotStateList<BluetoothGattCharacteristic>,
    isConnected: Boolean,
    onConnectClick: () -> Unit,
    onDisconnect: () -> Unit,
    isBleDeviceConnecting: Boolean
) {

    val mCheckedState = remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        VeryLightGrey,
                        UltraLightGrey
                    )
                )
            )
    ) {


        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color.White)
                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_arrow_back_24),
                contentDescription = null,
                modifier = Modifier.clickable {
                    navController.navigateUp()
                }
            )

            Spacer16Dp()

            BigBlackTextStyle(text = deviceName)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Image(
                    painter = painterResource(id = R.drawable.img_smart_watch2),
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                )

                Spacer16Dp()

                if (isBleDeviceConnecting) {
                    SimpleProgressBar()
                } else {
                    CustomOutlinedButton(
                        text = if (!isConnected) {
                            "Connect"
                        } else {
                            "Disconnect"
                        }, onClick = {
                            if (!isConnected) {
                                onConnectClick()
                            } else {
                                onDisconnect()
                            }
                        },
                        isConnected = isConnected
                    )
                }
            }

            Spacer16Dp()
            Spacer16Dp()

            Column(modifier = Modifier) {
                Card(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.White)
                            .padding(16.dp)
                    ) {

                        NormalBlackBoldTextStyle(text = "Notification")

                        Spacer8Dp()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NormalBlackTextStyle(text = "Continuous notification")

                            Switch(/*modifier = Modifier.border(
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = Color.LightGray,
                                ),
                                shape = RoundedCornerShape(16.dp)*/
                                checked = mCheckedState.value,
                                onCheckedChange = { mCheckedState.value = it })
                        }
                    }
                }

                Spacer16Dp()

                Card(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(14.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = Color.White)
                            .padding(16.dp)
                    ) {

                        NormalBlackBoldTextStyle(text = "Other")

                        Spacer8Dp()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            NormalBlackTextStyle(text = "Battery Settings")

                            Icon(
                                painter = painterResource(id = R.drawable.ic_baseline_arrow_forward_ios_24),
                                contentDescription = null,
                                modifier = Modifier.clickable {

                                },
                                tint = Color.Black
                            )
                        }
                    }
                }
            }

            Spacer16Dp()
            Spacer16Dp()
            Spacer16Dp()

            Text(text = "Characteristics ->")

            LazyColumn {

                itemsIndexed(listOfCharacteristics) { index: Int, item: BluetoothGattCharacteristic ->

                    Text(text = "${item.uuid} ${item.properties} ${item.permissions} ${item.writeType}")
                }
            }
        }
    }
}