package com.zb.demo.map.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapPoi
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.Marker
import com.baidu.mapapi.map.MarkerOptions
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.map.Overlay
import com.baidu.mapapi.map.PolylineOptions
import com.baidu.mapapi.model.LatLng
import com.baidu.mapapi.search.core.SearchResult
import com.baidu.mapapi.search.route.BikingRouteResult
import com.baidu.mapapi.search.route.DrivingRoutePlanOption
import com.baidu.mapapi.search.route.DrivingRouteResult
import com.baidu.mapapi.search.route.IndoorRouteResult
import com.baidu.mapapi.search.route.IntegralRouteResult
import com.baidu.mapapi.search.route.MassTransitRouteResult
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener
import com.baidu.mapapi.search.route.PlanNode
import com.baidu.mapapi.search.route.RoutePlanSearch
import com.baidu.mapapi.search.route.TransitRouteResult
import com.baidu.mapapi.search.route.WalkingRouteResult
import com.baidu.mapapi.utils.DistanceUtil
import com.zb.demo.map.R
import com.zb.demo.map.utils.PermissionUtils

/**
 *
 *
 * @author zhangbo
 * @version 1.0
 * @since 2025/7/6
 */
class MainActivity : AppCompatActivity(), BaiduMap.OnMapLoadedCallback, BaiduMap.OnMapClickListener {

    private lateinit var mBaiduMap: BaiduMap
    private lateinit var mMapView: MapView
    private lateinit var btnStart: Button
    private lateinit var btnReset: Button

    // 位置相关变量
    private lateinit var locationClient: LocationClient
    private var selectedPoint: LatLng? = null
    private var currentLocation: LatLng? = null
    private var destinationMarker: Marker? = null
    private var routeOverlay: Overlay? = null

    // 导航数据
    private var startTime: Long = 0
    private var totalDistance = 0f
    private var isNavigating = false

    // 路线规划
    private lateinit var routePlanSearch: RoutePlanSearch

    // 权限请求启动器（替代 onRequestPermissionsResult）
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }

        if (allGranted) {
            onPermissionsGranted()
        } else {
            val shouldShowRationale = permissions.keys.any {
                ActivityCompat.shouldShowRequestPermissionRationale(this, it)
            }

            if (shouldShowRationale) {
                showPermissionDeniedDialog()
            } else {
                showPermissionPermanentlyDeniedDialog()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // 初始化地图
        mMapView = findViewById(R.id.map_view)
        mBaiduMap = mMapView.map
        mBaiduMap.isMyLocationEnabled = true
        mBaiduMap.setOnMapLoadedCallback(this)
        mBaiduMap.setOnMapClickListener(this)
        mBaiduMap.mapType = BaiduMap.MAP_TYPE_NORMAL

        // 初始化UI控件
        btnStart = findViewById(R.id.btn_start)
        btnReset = findViewById(R.id.btn_reset)

        // 设置按钮点击事件
        btnStart.setOnClickListener {
            if (selectedPoint == null) {
                Toast.makeText(this, "请先选择目的地", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startNavigation()
        }

        //重置按钮
        btnReset.setOnClickListener {
            resetNavigation()
        }
        // 检查并请求权限
        checkAndRequestPermissions()

        // 初始化路线规划
        routePlanSearch = RoutePlanSearch.newInstance()
        routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener)
    }

    private fun initLocation() {
        locationClient = LocationClient(applicationContext)
        locationClient.registerLocationListener(object : BDAbstractLocationListener() {
            override fun onReceiveLocation(location: BDLocation) {
                if (location == null || mMapView == null) return

                // 获取当前位置
                currentLocation = LatLng(location.latitude, location.longitude)

                // 更新地图位置
                val locData = MyLocationData.Builder()
                    .accuracy(location.radius) // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.direction)
                    .latitude(location.latitude)
                    .longitude(location.longitude)
                    .build()
                mBaiduMap.setMyLocationData(locData)


                // 如果是导航中，更新路线
                if (isNavigating) {
                    updateNavigation(location)
                }
            }
        })

        // 配置定位参数
        val option = LocationClientOption().apply {
            locationMode = LocationClientOption.LocationMode.Hight_Accuracy
            isOpenGps = true
            setCoorType("bd09ll")
            setScanSpan(3000) // 3秒更新一次
            setIsNeedAddress(true)
            setIsNeedLocationDescribe(true)
        }
        locationClient.locOption = option
        locationClient.start()

        // 配置定位图层
        mBaiduMap.isMyLocationEnabled = true
        val myLocationConfig = MyLocationConfiguration(
            MyLocationConfiguration.LocationMode.FOLLOWING,
            true,
            BitmapDescriptorFactory.fromResource(R.drawable.my_location_24dp_ff00ff)
        )
        mBaiduMap.setMyLocationConfiguration(myLocationConfig)
    }

    override fun onMapLoaded() {
        // 地图加载完成后的操作
        Toast.makeText(this, "地图加载完成", Toast.LENGTH_SHORT).show()
    }

    override fun onMapClick(latLng: LatLng?) {
        latLng?.let {
            // 清除之前的选择
            destinationMarker?.remove()

            // 添加新的目的地标记
            selectedPoint = it
            destinationMarker = addDestinationMarker(it)
            // 移动地图视角
            val update = MapStatusUpdateFactory.newLatLng(it)
            mBaiduMap.animateMapStatus(update)

            Toast.makeText(this, "已选择目的地", Toast.LENGTH_SHORT).show()
        }
    }

    // 创建目的地标记的正确方式
    fun addDestinationMarker(position: LatLng): Marker {
        // 创建 MarkerOptions
        val options = MarkerOptions()
            .position(position)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.fmd_good_0000ff))
            .anchor(0.5f, 1.0f) // 锚点在图标底部中心
            .zIndex(5)

        // 添加覆盖物并转换为 Marker
        return mBaiduMap.addOverlay(options) as Marker
    }

    override fun onMapPoiClick(p0: MapPoi?) {
    }

    private fun startNavigation() {
        if (currentLocation == null || selectedPoint == null) return

        // 重置导航数据
        isNavigating = true
        startTime = System.currentTimeMillis()
        totalDistance = 0f

        // 清除旧路线
        routeOverlay?.remove()

        // 设置按钮状态
        btnStart.isEnabled = false
        btnReset.isEnabled = true

        // 开始路线规划（驾车模式）
        val start = PlanNode.withLocation(currentLocation)
        val end = PlanNode.withLocation(selectedPoint)
        routePlanSearch.drivingSearch(DrivingRoutePlanOption().from(start).to(end))

        Toast.makeText(this, "导航开始", Toast.LENGTH_SHORT).show()
    }

    private fun updateNavigation(location: BDLocation) {
        if (selectedPoint == null || currentLocation == null) return

        // 检查是否到达目的地
        val distanceToTarget = DistanceUtil.getDistance(currentLocation, selectedPoint)
        if (distanceToTarget < 30) { // 30米内视为到达
            showTripSummary()
            return
        }

        // 更新当前位置
        currentLocation = LatLng(location.latitude, location.longitude)

        // 更新地图中心点
        val update = MapStatusUpdateFactory.newLatLng(currentLocation)
        mBaiduMap.animateMapStatus(update)
    }

    private fun showTripSummary() {
        isNavigating = false
        locationClient.stop()

        // 计算行程时间（分钟）
        val duration = (System.currentTimeMillis() - startTime) / 60 / 1000

        // 显示结果对话框
        AlertDialog.Builder(this).apply {
            setTitle("行程摘要")
            setMessage("行程耗时: $duration 分钟\n总距离: ${"%.2f".format(totalDistance/1000)} 公里")
            setPositiveButton("确定") { _, _ -> resetNavigation() }
            setCancelable(false)
            show()
        }
    }

    private fun resetNavigation() {
        isNavigating = false

        // 清除地图标记和路线
        destinationMarker?.remove()
        routeOverlay?.remove()
        mBaiduMap.clear()

        // 重置位置服务
        locationClient.stop()
        locationClient.start()

        // 重置变量
        selectedPoint = null
        totalDistance = 0f

        // 更新按钮状态
        btnStart.isEnabled = true
        btnReset.isEnabled = false

        Toast.makeText(this, "导航已重置", Toast.LENGTH_SHORT).show()
    }

    // 路线规划结果监听器
    private val routePlanResultListener = object : OnGetRoutePlanResultListener {
        //步行导航
        override fun onGetWalkingRouteResult(result: WalkingRouteResult?) = Unit
        //室内公交
        override fun onGetTransitRouteResult(result: TransitRouteResult?) = Unit
        //跨城公交
        override fun onGetMassTransitRouteResult(result: MassTransitRouteResult?) = Unit
        //驾驶导航
        override fun onGetDrivingRouteResult(result: DrivingRouteResult?) = handleRouteResult(result)
        //室内路线
        override fun onGetIndoorRouteResult(result: IndoorRouteResult?) = Unit
        //骑行导航
        override fun onGetBikingRouteResult(result: BikingRouteResult?) = Unit
        //
        override fun onGetIntegralRouteResult(result: IntegralRouteResult?)  = Unit

        private fun handleRouteResult(result: DrivingRouteResult?) {
            if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(this@MainActivity, "路线规划失败", Toast.LENGTH_SHORT).show()
                return
            }

            // 获取最佳路线
            val routeLine = result.routeLines[0]
            totalDistance = routeLine.distance.toFloat()

            // 绘制路线
            val points = ArrayList<LatLng>()
            for (step in routeLine.allStep) {
                points.addAll(step.wayPoints)
            }

            routeOverlay = mBaiduMap.addOverlay(PolylineOptions()
                .points(points)
                .width(10)
                .color(Color.argb(178, 0, 78, 255)) // 半透明蓝色
                .zIndex(10))

            // 移动到路线起点
            val startPoint = points.first()
            val update = MapStatusUpdateFactory.newLatLngZoom(startPoint, 16f)
            mBaiduMap.animateMapStatus(update)
        }
    }

    override fun onResume() {
        super.onResume()
        // 当用户从设置页面返回时检查权限
        if (wasPermissionDenied) {
            checkAndRequestPermissions()
            wasPermissionDenied = false
        }
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationClient.stop()
        mMapView.onDestroy()
        routePlanSearch.destroy()
    }

    /**
     * 检查并请求所需权限
     */
    private fun checkAndRequestPermissions() {
        // 检查位置权限
        if (!PermissionUtils.hasPermissions(this, PermissionUtils.LOCATION_PERMISSIONS)) {
            requestLocationPermission()
            return
        }

        // 检查文件权限（如果需要）
        if (needsFilePermission() && !hasFilePermissions()) {
            requestFilePermission()
            return
        }

        // 所有权限已授予，启动应用
        onAllPermissionsGranted()
    }

    /**
     * 请求位置权限
     */
    private fun requestLocationPermission() {
        if (PermissionUtils.shouldShowRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            showLocationPermissionRationale()
        } else {
            requestPermissionLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
        }
    }

    /**
     * 显示位置权限说明
     */
    private fun showLocationPermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("需要位置权限")
            .setMessage("导航功能需要访问您的位置信息，以提供准确的路线规划和实时导航服务。")
            .setPositiveButton("确定") { _, _ ->
                requestPermissionLauncher.launch(PermissionUtils.LOCATION_PERMISSIONS)
            }
            .setNegativeButton("取消") { _, _ ->
                Toast.makeText(this, "位置权限被拒绝，导航功能无法使用", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    /**
     * 检查是否需要文件权限
     */
    private fun needsFilePermission(): Boolean {
        // 根据应用功能决定是否需要文件权限
        // 例如：如果应用需要保存导航历史或截图
        return true
    }

    /**
     * 检查文件权限状态
     */
    private fun hasFilePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用作用域存储，不需要传统文件权限
            true
        } else {
            PermissionUtils.hasPermissions(this, PermissionUtils.LEGACY_FILE_PERMISSIONS)
        }
    }

    /**
     * 请求文件权限
     */
    private fun requestFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 不需要请求传统文件权限
            return
        }

        val permissionsToRequest = PermissionUtils.LEGACY_FILE_PERMISSIONS.filter {
            ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            if (permissionsToRequest.any {
                    PermissionUtils.shouldShowRationale(this, it)
                }) {
                showFilePermissionRationale()
            } else {
                requestPermissionLauncher.launch(permissionsToRequest)
            }
        }
    }

    /**
     * 显示文件权限说明
     */
    private fun showFilePermissionRationale() {
        AlertDialog.Builder(this)
            .setTitle("需要存储权限")
            .setMessage("应用需要访问存储空间来保存导航历史和行程摘要。")
            .setPositiveButton("确定") { _, _ ->
                requestPermissionLauncher.launch(PermissionUtils.LEGACY_FILE_PERMISSIONS)
            }
            .setNegativeButton("取消") { _, _ ->
                Toast.makeText(this, "存储权限被拒绝，部分功能可能受限", Toast.LENGTH_LONG).show()
                onAllPermissionsGranted()
            }
            .show()
    }

    /**
     * 权限被拒绝时的处理
     */
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("权限被拒绝")
            .setMessage("您拒绝了必要的权限，相关功能将无法使用。")
            .setPositiveButton("重试") { _, _ -> checkAndRequestPermissions() }
            .setNegativeButton("取消") { _, _ -> finish() }
            .show()
    }

    /**
     * 权限被永久拒绝时的处理
     */
    private fun showPermissionPermanentlyDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("权限被永久拒绝")
            .setMessage("您已永久拒绝必要的权限。请在系统设置中手动授予权限。")
            .setPositiveButton("打开设置") { _, _ -> openAppSettings() }
            .setNegativeButton("取消") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    /**
     * 打开应用设置页面
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    /**
     * 位置权限已授予
     */
    private fun onPermissionsGranted() {
        // 继续检查其他权限
        checkAndRequestPermissions()
    }

    /**
     * 所有权限已授予
     */
    private fun onAllPermissionsGranted() {
        // 启动应用主功能
        initializeMapAndLocation()
    }

    /**
     * 初始化地图和位置服务
     */
    private fun initializeMapAndLocation() {
        // 初始化定位服务
        initLocation()
    }


    companion object {
        // 标记权限是否被拒绝
        var wasPermissionDenied = false
    }
}