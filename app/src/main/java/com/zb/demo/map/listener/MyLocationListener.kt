package com.zb.demo.map.listener

import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation


/**
 * GPS信号检测
 *
 * @author zhangbo
 * @version 1.0
 * @since 2025/7/6
 */
private class MyLocationListener : BDAbstractLocationListener() {
    override fun onReceiveLocation(location: BDLocation) {
        // 检查定位精度和卫星数量
        val isGoodSignal = checkGpsSignalQuality(location)

        if (isGoodSignal) {
//            hideGpsWarning()
        } else {
//            showGpsWarning()
        }
    }

    fun checkGpsSignalQuality(location: BDLocation): Boolean {
        // 1. 检查定位精度
        val accuracy = location.getRadius()
        if (accuracy > 50) {  // 精度大于50米认为信号差
            return false
        }


        // 2. 检查卫星数量（仅当GPS定位时有效）
        if (location.getLocType() == BDLocation.TypeGpsLocation) {
            val satellites = location.getSatelliteNumber()
            if (satellites < 4) {  // 少于4颗卫星认为信号差
                return false
            }
        }


        // 3. 检查定位类型
        val locType = location.getLocType()
        if (locType == BDLocation.TypeOffLineLocation || locType == BDLocation.TypeNetWorkException || locType == BDLocation.TypeCriteriaException) {
            return false
        }

        return true
    }
}
