package com.mn.mncompanion.bluetooth

import android.Manifest.permission.BLUETOOTH_CONNECT
import android.Manifest.permission.BLUETOOTH_SCAN
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

fun Context.hasBluetoothPermissions(): Boolean {
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) return true

    val hasScanPermission = ContextCompat.checkSelfPermission(this, BLUETOOTH_SCAN) == PERMISSION_GRANTED
    val hasConnectPermission = ContextCompat.checkSelfPermission(this, BLUETOOTH_CONNECT) == PERMISSION_GRANTED

    return hasScanPermission && hasConnectPermission
}

@RequiresApi(Build.VERSION_CODES.S)
fun Activity.shouldShowBluetoothRationale() =
    ActivityCompat.shouldShowRequestPermissionRationale(this, BLUETOOTH_SCAN) ||
            ActivityCompat.shouldShowRequestPermissionRationale(this, BLUETOOTH_CONNECT)

@RequiresApi(Build.VERSION_CODES.S)
fun ComponentActivity.requestBluetoothPermissions(onGranted: () -> Unit, onDenied: () -> Unit) {
    val requestLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions())
    { grantResult: Map<String, Boolean> ->
        val allGranted = !grantResult.values.isEmpty() && !grantResult.values.contains(false)
        if (allGranted)
            onGranted()
        else
            onDenied()
    }

    requestLauncher.launch(arrayOf(BLUETOOTH_SCAN, BLUETOOTH_CONNECT))
}