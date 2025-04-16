package com.example.summer;

import android.app.Application;
import android.content.Context;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;
import com.example.summer.datas.SpotData;

public class DemoApplication extends Application {
    private SpotData spotData; // 持有树数据的实例
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.setAgreePrivacy(getApplicationContext(),true);
        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
        LocationClient.setAgreePrivacy(true);
        // 应用启动时初始化树，仅创建一次
        spotData = new SpotData();

    }
    // 提供公共方法获取树数据
    public SpotData getSpotData() {
        return spotData;
    }

    // 输入一个地点的名称，返回该地点的人流量
    public int getCrowdByLocationName(String locationName) {
        return spotData.getCrowdByLocationName(locationName);
    }

    // 输入一个地点的名称，将该地点对应的searchtimes加1，若为类别，则将该分类下所有地点的searchtimes加1
    public void increaseSearchTimes(String locationName) {
        spotData.increaseSearchTimes(locationName);
    }
}
