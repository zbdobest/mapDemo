# mapDemo
地图demo
## 应用概要

一款基于百度地图API的简洁导航应用，主要功能包括：
- 在地图上选择目的地
- 实时导航路线显示
- 行程结束显示摘要（路线、耗时、距离）

## 开发步骤

### 1. 环境准备
- Android Studio
- 百度地图API Key（从[百度地图开放平台](https://lbsyun.baidu.com/)申请）

### 2. 项目配置
1. 在`AndroidManifest.xml`中添加：
```xml
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" /> <!-- 这个权限用于访问GPS定位 -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" /> <!-- 用于访问wifi网络信息，wifi信息会用于进行网络定位 -->
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" /> <!-- 获取网络状态，根据网络状态切换进行数据请求网络转换 -->
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" /> <!-- 写外置存储。如果开发者使用了离线地图，并且数据写在外置存储区域，则需要申请该权限 -->
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" /> <!-- 读取外置存储。如果开发者使用了so动态加载功能并且把so文件放在了外置存储区域，则需要申请该权限，否则不需要 -->
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" /> <!-- 访问网络，进行地图相关业务数据请求，包括地图数据，路线规划，POI检索等 -->
    <uses-permission android:name="android.permission.INTERNET" />
<!-- 百度地图AK -->
  <meta-data
      android:name="com.baidu.lbsapi.API_KEY"
      android:value="YGTGNVBvXyqekwxdINpUevzo" />
  <service android:name="com.baidu.location.f"
      android:enabled="true"
      android:process=":remote">
  </service>
```

2. 在`build.gradle`中添加依赖：
```gradle
  // 百度地图SDK
  implementation 'com.baidu.lbsyun:BaiduMapSDK_Map:7.6.4'
  implementation 'com.baidu.lbsyun:BaiduMapSDK_Search:7.6.4'
  implementation 'com.baidu.lbsyun:BaiduMapSDK_Location:9.1.8'
  implementation 'com.baidu.lbsyun:BaiduMapSDK_Util:7.6.4'
```

### 3. 核心功能实现
1. **地图初始化**：
```kotlin
mMapView = findViewById(R.id.map_view)
mBaiduMap = mMapView.map
mBaiduMap.isMyLocationEnabled = true
```

2. **目的地选择**：
```kotlin
mBaiduMap.setOnMapClickListener { latLng ->
    // 添加标记
    val marker = mBaiduMap.addOverlay(
        MarkerOptions().position(latLng)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
}
```

3. **开始导航**：
```kotlin
btnStart.setOnClickListener {
    routePlanSearch.drivingSearch(
        DrivingRoutePlanOption()
            .from(PlanNode.withLocation(currentPos))
            .to(PlanNode.withLocation(destinationPos)))
}
```

## 注意事项
- 需要Android 6.0+设备
- 首次使用需授予位置权限
- 确保网络连接正常

> 提示：完整代码请参考项目中的MainActivity.kt
