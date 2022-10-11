package io.ballerine.kmp.bleandroidapp.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ballerine.kmp.bleandroidapp.widgets.BigBlackTextStyle
import io.ballerine.kmp.bleandroidapp.widgets.LoadingAnimation

@Composable
fun ScanningScreen() {

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.padding(top = 200.dp)) {
            LoadingAnimation()
        }

        BigBlackTextStyle("Scanning for devices...")
    }
}