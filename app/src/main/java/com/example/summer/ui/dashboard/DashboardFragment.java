package com.example.summer.ui.dashboard;

import android.Manifest;
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
import com.example.summer.utils.NetworkUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;

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
                //System.out.println("请求URL：" + address);
                NetworkUtils.getLocationFromAddress(address, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        requireActivity().runOnUiThread(() ->
                                Toast.makeText(requireContext(), "请求失败：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            String json = response.body().string();
                            parseJSONResponse(json);
                        } else {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "请求失败，状态码：" + response.code(), Toast.LENGTH_SHORT).show()
                            );
                        }
                    }
                });
            } else {
                Toast.makeText(requireContext(), "请输入地址", Toast.LENGTH_SHORT).show();
            }
        });

        // 初始化定位
        initLocation();

        return root;
    }

    private void parseJSONResponse(String json) {
        try {
            // 处理JSONP格式响应
            int start = json.indexOf("{");
            int end = json.lastIndexOf("}");
            if (start != -1 && end != -1) {
                json = json.substring(start, end + 1);
            }

            JSONObject jsonObject = new JSONObject(json);
            int status = jsonObject.getInt("status");
            if (status == 0) {
                JSONObject result = jsonObject.getJSONObject("result");
                JSONObject location = result.getJSONObject("location");
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");
                LatLng latLng = new LatLng(lat, lng);

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "定位成功：" + latLng.toString(), Toast.LENGTH_SHORT).show();
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(latLng, 15.8f));
                });
            } else {
                String message = jsonObject.getString("message");
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(requireContext(), "请求失败：" + message, Toast.LENGTH_SHORT).show()
                );
            }
        } catch (JSONException e) {
            e.printStackTrace();
            requireActivity().runOnUiThread(() ->
                    Toast.makeText(requireContext(), "解析失败：" + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
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
}