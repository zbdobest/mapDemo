package com.zb.demo.map.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * 权限处理工具
 *
 * @author zhangbo
 * @version 1.0
 * @since 2025/7/7
 */
object PermissionUtils {

    // 位置权限组
    val LOCATION_PERMISSIONS = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // 文件权限组（Android 10以下）
    val LEGACY_FILE_PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    // 媒体位置权限（Android 10+）
    val MEDIA_LOCATION_PERMISSION = Manifest.permission.ACCESS_MEDIA_LOCATION

    /**
     * 检查权限是否已授予
     */
    fun hasPermissions(context: Context, permissions: Array<String>): Boolean {
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * 请求权限
     */
    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    /**
     * 检查是否需要显示权限说明
     */
    fun shouldShowRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    /**
     * 处理权限请求结果
     */
    fun handlePermissionResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit,
        onPermanentlyDenied: () -> Unit
    ) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                handlePermissionResult(
                    permissions,
                    grantResults,
                    onGranted,
                    onDenied,
                    onPermanentlyDenied
                )
            }
            REQUEST_FILE_PERMISSION ->{
                handlePermissionResult(
                    permissions,
                    grantResults,
                    onGranted,
                    onDenied,
                    onPermanentlyDenied
                )
            }

        }
    }

    private fun handlePermissionResult(
        permissions: Array<out String>,
        grantResults: IntArray,
        onGranted: () -> Unit,
        onDenied: () -> Unit,
        onPermanentlyDenied: () -> Unit
    ) {
        val allGranted = permissions.indices.all {
            grantResults[it] == PackageManager.PERMISSION_GRANTED
        }

        if (allGranted) {
            onGranted()
        } else {
            val shouldShowRationale = permissions.any {
//                ActivityCompat.shouldShowRequestPermissionRationale(activity, it)
                true
            }

            if (shouldShowRationale) {
                onDenied()
            } else {
                onPermanentlyDenied()
            }
        }
    }

    // 权限请求码常量
    val REQUEST_LOCATION_PERMISSION = 1001
    val REQUEST_FILE_PERMISSION = 1002
}