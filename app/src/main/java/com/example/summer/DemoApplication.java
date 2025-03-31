package com.example.summer;

import android.app.Application;
import android.content.Context;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.CoordType;
import com.baidu.mapapi.SDKInitializer;

public class DemoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SDKInitializer.setAgreePrivacy(getApplicationContext(),true);
        SDKInitializer.initialize(getApplicationContext());
        SDKInitializer.setCoordType(CoordType.BD09LL);
        LocationClient.setAgreePrivacy(true);

    }
}
