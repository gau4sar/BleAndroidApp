package io.ballerine.kmp.bleandroidapp.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CustomOutlinedButton(
    text: String,
    onClick: () -> Unit,
    isConnected: Boolean
) {
    OutlinedButton(
        onClick = { onClick() },
        border = BorderStroke(1.dp, if (isConnected) Color.Green else Color.Black),
        shape = RoundedCornerShape(16), // = 50% percent
        // or shape = CircleShape
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isConnected) Color.Green else Color.Black,
            backgroundColor = Color.Transparent
        )
    ) {
        Text(text = text)
    }
}