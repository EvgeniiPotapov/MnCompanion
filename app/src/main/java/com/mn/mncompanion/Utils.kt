package com.mn.mncompanion

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log

@SuppressLint("QueryPermissionsNeeded")
fun PackageManager.isApplicationInstalled(packageName: String): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                getPackageInfo(packageName, 0)
            }
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    } else {
        getInstalledApplications(0).firstOrNull { it.packageName == packageName } != null
    }
}

fun logd(message: String) {
    Log.d("MN_LOG", message)
}