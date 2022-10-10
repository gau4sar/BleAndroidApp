package io.ballerine.kmp.bleandroidapp.utils

import android.Manifest
import java.util.*


fun necessaryPermissions(): List<String> {
    return arrayListOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}

val HEART_RATE_SERVICE_UUID = convertFromInteger(0x180D)
val HEART_RATE_MEASUREMENT_CHAR_UUID = convertFromInteger(0x2A37)
val HEART_RATE_CONTROL_POINT_CHAR_UUID = convertFromInteger(0x2A39)

fun convertFromInteger(i: Int): UUID? {
    val MSB = 0x0000000000001000L
    val LSB = -0x7fffff7fa064cb05L
    val value = (i and -0x1).toLong()
    return UUID(MSB or (value shl 32), LSB)
}