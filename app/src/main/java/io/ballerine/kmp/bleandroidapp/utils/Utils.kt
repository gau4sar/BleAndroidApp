package io.ballerine.kmp.bleandroidapp.utils

import android.Manifest


fun necessaryPermissions(): List<String> {
    return arrayListOf(
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}