package com.example.summer.ui.dashboard;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;
import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.example.summer.R;
import com.example.summer.utils.WenXin;
import org.json.JSONException;

import java.io.IOException;

public class DashboardFragment extends Fragment {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private EditText addressSearchEditText;
    private Button searchButton;
    public LocationClient mLocationClient;
    private MyLocationListener myListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mMapView = root.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);

        // 初始化地图中心点
        LatLng chengdeSummerResort = new LatLng(40.9978, 117.9413);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(chengdeSummerResort, 15.8f));

        // 初始化搜索组件
        addressSearchEditText = root.findViewById(R.id.address_search_edit_text);
        searchButton = root.findViewById(R.id.search_button);

        searchButton.setOnClickListener(v -> {
            String address = addressSearchEditText.getText().toString().trim();
            if (!address.isEmpty()) {
                new WenXinTask().execute(address);
            } else {
                Toast.makeText(requireContext(), "请输入地址", Toast.LENGTH_SHORT).show();
            }
        });

        // 初始化定位
        initLocation();

        return root;
    }

    private void showIntroductionDialog(String introduction) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_location_introduction, null);
        builder.setView(view);

        TextView locationIntroductionText = view.findViewById(R.id.location_introduction_text);
        if (introduction != null &&!introduction.isEmpty()) {
            locationIntroductionText.setText(introduction);
        } else {
            locationIntroductionText.setText("无介绍内容");
        }

        builder.setPositiveButton("确定", (dialog, which) -> dialog.dismiss());
        builder.setTitle("简介");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void initLocation() {
        try {
            mLocationClient = new LocationClient(getActivity().getApplicationContext());
            myListener = new MyLocationListener();
            mLocationClient.registerLocationListener(myListener);

            LocationClientOption option = new LocationClientOption();
            option.setIsNeedAddress(true);
            mLocationClient.setLocOption(option);

            if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                mLocationClient.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 其他生命周期方法保持不变
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null) return;
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(location.getDirection())
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(locData);
        }
    }

    private class WenXinTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String address = params[0];
            WenXin wenXin = new WenXin();
            try {
                return wenXin.getLocationIntroduction(address);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                showIntroductionDialog(result);
            } else {
                Log.e("WenXin", "请求失败");
                Toast.makeText(requireContext(), "请求失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}