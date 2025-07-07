package com.zb.demo.map.listener

import android.R
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.route.BikingRouteResult
import com.baidu.mapapi.search.route.DrivingRouteResult
import com.baidu.mapapi.search.route.IndoorRouteResult
import com.baidu.mapapi.search.route.IntegralRouteResult
import com.baidu.mapapi.search.route.MassTransitRouteResult
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener
import com.baidu.mapapi.search.route.TransitRouteResult
import com.baidu.mapapi.search.route.WalkingRouteResult


/**
 * 路线规划结果监听
 *
 * @author zhangbo
 * @version 1.0
 * @since 2025/7/6
 */
class RoutePlanResultListener : OnGetRoutePlanResultListener {
    override fun onGetWalkingRouteResult(p0: WalkingRouteResult?) {
    }

    override fun onGetTransitRouteResult(p0: TransitRouteResult?) {
    }

    override fun onGetMassTransitRouteResult(p0: MassTransitRouteResult?) {
    }

    override fun onGetDrivingRouteResult(result: DrivingRouteResult?) {
        if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
            return
        }


        // 获取第一条路线
        val route = result.getRouteLines().get(0)


//        // 绘制路线
//        val overlay: DrivingRouteOverlay = DrivingRouteOverlay(mBaiduMap)
//        overlay.setData(route)
//        overlay.addToMap()
//        overlay.zoomToSpan()
    } // 其他交通方式结果处理...

    override fun onGetIndoorRouteResult(p0: IndoorRouteResult?) {
    }

    override fun onGetBikingRouteResult(p0: BikingRouteResult?) {
    }

    override fun onGetIntegralRouteResult(p0: IntegralRouteResult?) {

    }


}
