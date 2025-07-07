package com.zb.demo.map.app

import android.app.Application
import com.baidu.mapapi.SDKInitializer

/**
 * application
 *
 * @author zhangbo
 * @version 1.0
 * @since 2025/7/6
 */
class MyApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        // 初始化百度地图SDK
        SDKInitializer.setAgreePrivacy(this, true)
        SDKInitializer.initialize(this)
    }
}