//package com.zb.demo.map.ui
//
//import android.R
//import android.graphics.Color
//import android.os.Bundle
//import android.widget.TextView
//import androidx.appcompat.app.AppCompatActivity
//import com.baidu.mapapi.SDKInitializer
//import com.baidu.mapapi.map.BaiduMap
//import com.baidu.mapapi.map.BitmapDescriptorFactory
//import com.baidu.mapapi.map.MapStatusUpdateFactory
//import com.baidu.mapapi.map.MapView
//import com.baidu.mapapi.map.MarkerOptions
//import com.baidu.mapapi.map.OverlayOptions
//import com.baidu.mapapi.map.PolylineOptions
//import com.baidu.mapapi.model.LatLng
//import com.baidu.mapapi.model.LatLngBounds
//
//
///**
// * 形成数据
// *
// * @author zhangbo
// * @version 1.0
// * @since 2025/7/6
// */
//class SummaryActivity : AppCompatActivity() {
//    private var mMapView: MapView? = null
//    private var mBaiduMap: BaiduMap? = null
//    private var pathPoints: MutableList<LatLng?>? = ArrayList<LatLng?>()
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_summary)
//
//
//        // 获取行程数据
//        pathPoints = getIntent().getSerializableExtra("PATH_POINTS") as MutableList<LatLng?>?
//        val totalTime = getIntent().getLongExtra("TOTAL_TIME", 0)
//        val totalDistance = getIntent().getDoubleExtra("TOTAL_DISTANCE", 0.0)
//
//
//        // 显示行程数据
//        val tvTime = findViewById<TextView?>(R.id.tv_time)
//        val tvDistance = findViewById<TextView?>(R.id.tv_distance)
//        tvTime.setText(formatTime(totalTime))
//        tvDistance.setText(formatDistance(totalDistance))
//
//
//        // 初始化地图
//        SDKInitializer.initialize(getApplicationContext())
//        mMapView = findViewById<MapView?>(R.id.summary_map)
//        mBaiduMap = mMapView!!.getMap()
//
//
//        // 显示行程路径
//        showPathOnMap()
//    }
//
//    private fun showPathOnMap() {
//        if (pathPoints == null || pathPoints!!.size < 2) return
//
//
//        // 绘制路径
//        val options: OverlayOptions = PolylineOptions()
//            .points(pathPoints)
//            .color(Color.BLUE)
//            .width(8)
//        mBaiduMap!!.addOverlay(options)
//
//
//        // 添加起点和终点标记
//        addMarker(pathPoints!!.get(0)!!, R.drawable.ic_start, "起点")
//        addMarker(pathPoints!!.get(pathPoints!!.size - 1)!!, R.drawable.ic_destination, "终点")
//
//
//        // 调整地图显示范围
//        val builder = LatLngBounds.Builder()
//        for (point in pathPoints) {
//            builder.include(point)
//        }
//        val u = MapStatusUpdateFactory.newLatLngBounds(builder.build())
//        mBaiduMap!!.setMapStatus(u)
//    }
//
//    private fun addMarker(point: LatLng, iconRes: Int, title: String?) {
//        val options = MarkerOptions()
//            .position(point)
//            .icon(BitmapDescriptorFactory.fromResource(iconRes))
//            .title(title)
//        mBaiduMap!!.addOverlay(options)
//    }
//
//    private fun formatTime(millis: Long): String {
//        val seconds = millis / 1000
//        val minutes = seconds / 60
//        val hours = minutes / 60
//        return String.format("%02d:%02d:%02d", hours, minutes % 60, seconds % 60)
//    }
//
//    private fun formatDistance(meters: Double): String {
//        if (meters > 1000) {
//            return String.format("%.2f 公里", meters / 1000)
//        } else {
//            return String.format("%.0f 米", meters)
//        }
//    }
//}