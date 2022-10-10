package io.ballerine.kmp.bleandroidapp.screens

import android.bluetooth.BluetoothGattCharacteristic
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BluetoothDeviceCharacteristicsScreen(listOfCharacteristics: SnapshotStateList<BluetoothGattCharacteristic>) {

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {

        Text(text = "Characteristics ->")

        LazyColumn {

            itemsIndexed(listOfCharacteristics) { index: Int, item: BluetoothGattCharacteristic ->
                
                Text(text = "${item.uuid} ${item.properties} ${item.permissions} ${item.writeType}")
            }
        }
    }
}