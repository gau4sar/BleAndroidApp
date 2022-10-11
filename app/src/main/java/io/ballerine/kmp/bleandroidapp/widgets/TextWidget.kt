package io.ballerine.kmp.bleandroidapp.widgets

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import io.ballerine.kmp.bleandroidapp.ui.theme.TextColorGrey

@Composable
fun MediumGreyTextStyle(text: String) {
    Text(text = text, color = TextColorGrey, fontSize = 11.sp)
}

@Composable
fun NormalBlackTextStyle(text: String) {
    Text(text = text, color = Color.Black, fontSize = 14.sp)
}

@Composable
fun NormalBlackBoldTextStyle(text: String) {
    Text(text = text, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun NormalWhiteTextStyle(text: String) {
    Text(text = text, color = Color.White, fontSize = 14.sp)
}

@Composable
fun BigBlackTextStyle(text: String) {

    Text(text = text, color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
fun BigWhiteTextStyle(text: String) {

    Text(text = text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
fun VeryBigWhiteTextStyle(text: String) {

    Text(text = text, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
}