package io.ballerine.kmp.bleandroidapp.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionsRequired
import com.google.accompanist.permissions.rememberMultiplePermissionsState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CustomMultiplePermissionView(
    permission: List<String>,
    permissionsNotAvailableContent: @Composable () -> Unit,
    permissionsNotGrantedContent: @Composable () -> Unit,
    content: @Composable () -> Unit) {
    val permissionState = rememberMultiplePermissionsState(permission)
    PermissionsRequired(
        multiplePermissionsState = permissionState,
        permissionsNotGrantedContent = {
            LaunchedEffect(key1 = "permission", block = {
                permissionState.launchMultiplePermissionRequest()
            })
            permissionsNotGrantedContent()
        },
        permissionsNotAvailableContent = {
            permissionsNotAvailableContent()
        },
        content = content
    )
}
