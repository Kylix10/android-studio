package com.example.summer.ui.dashboard;

import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.Nullable;
import android.widget.TextView;
import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
//import androidx.appcompat.app.AlertDialog;
import com.example.summer.R;
import com.example.summer.utils.NetworkUtils;
import com.example.summer.utils.WenXin;
import com.example.summer.DemoApplication;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import com.example.summer.datas.SpotData;
import android.widget.TextView;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class DashboardFragment extends Fragment {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private EditText addressSearchEditText;
    private Button searchButton;
    public LocationClient mLocationClient;
    private MyLocationListener myListener;
    private Button routePlanningButton;

    private Button facilitySearchButton;
    private Button clearMarkersButton;
    private SpotData spotData;
    private List<MarkerOptions> markerPositions = new ArrayList<>();//存标记位置




    // 用于存储途径点
    private List<LatLng> midPoints = new ArrayList<>();
    private LatLng startPoint;
    private LatLng endPoint;

    //储存用户输入的地点
    private String startAddress;
    private String endAddress;
    private List<String> midPointAddresses = new ArrayList<>();

    private boolean flag1=false;//用于判断路径规划是否有起点

    private DemoApplication demoApplication; // 新增：持有应用实例

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        // 初始化应用实例（在视图创建时获取）
        demoApplication = (DemoApplication) requireActivity().getApplication();

        mMapView = root.findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);

        //初始化设施查找
        facilitySearchButton = root.findViewById(R.id.facility_search_button);
        clearMarkersButton = root.findViewById(R.id.clearMarkersButton);

        DemoApplication application = (DemoApplication) requireActivity().getApplication();
        spotData = application.getSpotData();

        facilitySearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFacilityCategoryDialog();
            }
        });

        clearMarkersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearMapMarkers();
            }
        });


        // 初始化地图中心点
        LatLng chengdeSummerResort = new LatLng(40.9978, 117.9413);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(chengdeSummerResort, 15.8f));

        // 初始化搜索组件
        addressSearchEditText = root.findViewById(R.id.address_search_edit_text);
        searchButton = root.findViewById(R.id.search_button);

        searchButton.setOnClickListener(v -> {
            String address = addressSearchEditText.getText().toString().trim();
            if (!address.isEmpty()) {
                // 新增：搜索前先更新搜索次数
                demoApplication.increaseSearchTimes(address); // 直接通过应用实例调用
                new WenXinTask().execute(address);

            } else {
                Toast.makeText(requireContext(), "请输入地址", Toast.LENGTH_SHORT).show();
            }
        });

        // 初始化定位
        initLocation();

        // 初始化路线规划按钮
        routePlanningButton = root.findViewById(R.id.route_planning_button);
        routePlanningButton.setOnClickListener(v -> {

            if (startPoint == null) {
                // 当 startPoint 为空时执行的代码块
                // 例如，可以在这里进行相应的提示或处理逻辑
                Toast.makeText(requireContext(), "确定起点", Toast.LENGTH_SHORT).show();
                showStartPointDialog();
            } else if (flag1&&endPoint==null){
                Toast.makeText(requireContext(), "确定终点", Toast.LENGTH_SHORT).show();
                showEndPointDialog();
            }else {
                Toast.makeText(requireContext(), "路线规划显示", Toast.LENGTH_SHORT).show();
                showRoutePlanningResultDialog();
            }

        });

        return root;
    }


    private void showStartPointDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout, null);
        EditText startEditText = view.findViewById(R.id.start_edit_text);
        Button confirmButton = view.findViewById(R.id.confirm_start_button);
        builder.setView(view);

        AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 透明背景

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = startEditText.getText().toString().trim();
                String fullAddress = "承德避暑山庄" + input;

                if (!TextUtils.isEmpty(fullAddress )) {
                    // 限制地址范围，仅允许承德避暑山庄内地点
                    if (!fullAddress .contains("承德") || !fullAddress .contains("避暑山庄")) {
                        Toast.makeText(requireContext(), "请输入承德避暑山庄内的地点", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    startAddress = input; // 保存地址
                    // 新增：起点搜索时更新搜索次数
                    demoApplication.increaseSearchTimes(fullAddress );
                    NetworkUtils.getLocationFromAddress(fullAddress , new Callback() {
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
                                parseJSONResponse(json, "start", null);
                                dialog.dismiss();
                                //showMidPointDialog(); // 这里调用了 showMidPointDialog 方法

                                // 起点成功后，自动跳转到途经点输入
                                requireActivity().runOnUiThread(() -> showMidPointDialog());

                            } else {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "请求失败，状态码：" + response.code(), Toast.LENGTH_SHORT).show()
                                );
                                dialog.dismiss();
                            }
                        }
                    });

                } else {
                    Toast.makeText(requireContext(), "请输入起点地址", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }

            }
        });
        dialog.show();

    }

    private void showMidPointDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout_route, null);
        EditText midEditText = view.findViewById(R.id.midpoint_edit_text);
        Button addButton = view.findViewById(R.id.add_midpoint_button);
        Button confirmEndButton = view.findViewById(R.id.confirm_endpoint_button);

        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 透明背景

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String midAddress = midEditText.getText().toString().trim();
                //输入为空时，跳转重新输入
                if (TextUtils.isEmpty(midAddress)) {
                    dialog.dismiss();
                    showMidPointDialog();
                    return;
                }
                String fullAddress3 = "承德避暑山庄" + midAddress;
                if (!TextUtils.isEmpty(fullAddress3)) {
                    // 限制地址范围，仅允许承德避暑山庄内地点
                    if (!fullAddress3.contains("承德") || !fullAddress3.contains("避暑山庄")) {
                        Toast.makeText(requireContext(), "请输入承德避暑山庄内的地点", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    demoApplication.increaseSearchTimes(fullAddress3);
                    NetworkUtils.getLocationFromAddress(fullAddress3, new Callback() {
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
                                final String finalMidAddress = midAddress;

                                requireActivity().runOnUiThread(() -> {
                                    parseJSONResponse(json, "mid", finalMidAddress);
                                    midEditText.setText("");  //清空输入框，方便继续添加
                                    Toast.makeText(requireContext(), "已添加：" + finalMidAddress, Toast.LENGTH_SHORT).show();
                                });
                            } else {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "请求失败，状态码：" + response.code(), Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "请输入途径地点", Toast.LENGTH_SHORT).show();
                }
            }
        });

        confirmEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String midAddress = midEditText.getText().toString().trim();
                //输入为空时，直接跳转，不添加
                if (TextUtils.isEmpty(midAddress)) {
                    dialog.dismiss();
                    showEndPointDialog();
                    return;
                }
                String fullAddress4 = "承德避暑山庄" + midAddress;
                if (!TextUtils.isEmpty(fullAddress4)) {
                    // 限制地址范围，仅允许承德避暑山庄内地点
                    if (!fullAddress4.contains("承德") || !fullAddress4.contains("避暑山庄")) {
                        Toast.makeText(requireContext(), "请输入承德避暑山庄内的地点", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    demoApplication.increaseSearchTimes(fullAddress4);
                    NetworkUtils.getLocationFromAddress(fullAddress4, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "请求失败：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                            dialog.dismiss(); // 即使失败也跳转
                            showEndPointDialog();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()) {
                                String json = response.body().string();
                                requireActivity().runOnUiThread(() -> {
                                    parseJSONResponse(json, "mid", midAddress);
                                    Toast.makeText(requireContext(), "已添加：" + midAddress, Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    showEndPointDialog(); // 成功后跳转
                                });
                            } else {
                                requireActivity().runOnUiThread(() -> {
                                    Toast.makeText(requireContext(), "请求失败，状态码：" + response.code(), Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    showEndPointDialog();
                                });
                            }
                        }
                    });
                } else {
                    dialog.dismiss();
                    showEndPointDialog();
                }
            }
        });
        dialog.show();
    }

    private void showEndPointDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_layout_endpoint, null);
        EditText endEditText = view.findViewById(R.id.endpoint_edit_text);
        Button confirmButton = view.findViewById(R.id.confirm_endpoint_final_button);
        Button noEndButton = view.findViewById(R.id.confirm_noendpoint_final_button);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 透明背景

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input2 = endEditText.getText().toString().trim();
                String fullAddress2 = "承德避暑山庄" + input2;
                if (!TextUtils.isEmpty(fullAddress2)) {
                    // 限制地址范围，仅允许承德避暑山庄内地点
                    if (!fullAddress2.contains("承德") || !fullAddress2.contains("避暑山庄")) {
                        Toast.makeText(requireContext(), "请输入承德避暑山庄内的地点", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    endAddress = input2; // 保存地址
                    // 新增：终点搜索时更新搜索次数
                    demoApplication.increaseSearchTimes(fullAddress2);
                    // 获取经纬度
                    NetworkUtils.getLocationFromAddress(fullAddress2, new Callback() {
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
                                parseJSONResponse(json, "end", null);

                                // 终点输入成功后，自动显示路径规划结果
                                requireActivity().runOnUiThread(() -> showRoutePlanningResultDialog());

                            } else {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "请求失败，状态码：" + response.code(), Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    });
                } else {
                    Toast.makeText(requireContext(), "请输入终点地址", Toast.LENGTH_SHORT).show();
                }
                dialog.dismiss();
            }
        });
        // “无终点”按钮逻辑
        noEndButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endPoint = null;
                endAddress = null;
                // 直接展示路径结果
                showRoutePlanningResultDialog();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void drawRouteOnMap(List<LatLng> path) {
        if (path == null || path.size() < 2) return;

        mBaiduMap.clear(); // 清除旧图层

        int lineColor = 0xAAFF5733; // 路径颜色，可自定义

        OverlayOptions polylineOptions = new PolylineOptions()
                .points(path)
                .width(10)
                .color(lineColor)
                .dottedLine(false); // 实线

        mBaiduMap.addOverlay(polylineOptions);

        // 镜头移动到路径中点
        LatLng center = path.get(path.size() / 2);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(center, 15.0f));
    }



    private void showRoutePlanningResultDialog() {
        if (startPoint != null) {
            PathResult result;

            if (endPoint != null) {
                // 有终点
                result = optimizePathWithMST(startPoint, midPoints, endPoint,
                        startAddress, midPointAddresses, endAddress);
            } else {
                // 无终点
                result = optimizePathWithoutEnd(startPoint, midPoints,
                        startAddress, midPointAddresses);
            }

            if (result == null || result.path == null || result.path.isEmpty()) {
                Toast.makeText(requireContext(), "路径规划失败", Toast.LENGTH_SHORT).show();
                return;
            }

            List<LatLng> fullPath = result.path;
            List<String> addressList = result.addressList;
            double distance = calculateTotalDistance(fullPath);

            drawRouteOnMap(fullPath); // 地图路径绘制

            StringBuilder routeInfo = new StringBuilder();
            routeInfo.append("最优路径顺序：\n");

            for (int i = 0; i < addressList.size(); i++) {
                if (i == 0) {
                    routeInfo.append(addressList.get(i)); // 起点
                } else {
                    routeInfo.append(" -> ").append(addressList.get(i)); // 途经点或终点
                }
            }

            routeInfo.append("\n\n总距离：").append(String.format("%.2f", distance)).append(" 千米");

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            View view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_route_result_layout, null);
            TextView resultTextView = view.findViewById(R.id.result_text_view);
            Button cleanButton = view.findViewById(R.id.clean_button);
            Button okButton = view.findViewById(R.id.ok_button);

            resultTextView.setText(routeInfo.toString());
            builder.setView(view);

            AlertDialog dialog = builder.create();
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent); // 透明背景
            okButton.setOnClickListener(v -> dialog.dismiss());
            cleanButton.setOnClickListener(v -> {
                startPoint = null;
                endPoint = null;
                midPoints.clear();
                startAddress = null;
                endAddress = null;
                midPointAddresses.clear();
                mBaiduMap.clear();
                flag1 = false;
                mMapView.setVisibility(View.GONE);
                mMapView.setVisibility(View.VISIBLE);
                dialog.dismiss();
            });
            dialog.show();
        }
    }





    public double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 地球半径（单位：km）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    //储存输出的最短路径
    public class PathResult {
        public List<LatLng> path;
        public List<String> addressList;

        public PathResult(List<LatLng> path, List<String> addressList) {
            this.path = path;
            this.addressList = addressList;
        }
    }

    //无终点的时候
    public PathResult optimizePathWithoutEnd(LatLng start, List<LatLng> waypoints,
                                             String startAddr, List<String> waypointAddrs) {
        List<LatLng> allPoints = new ArrayList<>();
        List<String> allAddrs = new ArrayList<>();
        allPoints.add(start);
        allAddrs.add(startAddr);
        allPoints.addAll(waypoints);
        allAddrs.addAll(waypointAddrs);

        int n = allPoints.size();

        // 构建距离矩阵
        double[][] dist = new double[n][n];
        for (int i = 0; i < n; i++) {
            LatLng pi = allPoints.get(i);
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                LatLng pj = allPoints.get(j);
                dist[i][j] = haversine(pi.latitude, pi.longitude, pj.latitude, pj.longitude);
            }
        }

        // 构建最小生成树（Prim）
        boolean[] visited = new boolean[n];
        List<List<Integer>> tree = new ArrayList<>();
        for (int i = 0; i < n; i++) tree.add(new ArrayList<>());

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[2]));
        visited[0] = true;
        for (int i = 1; i < n; i++) {
            pq.offer(new int[]{0, i, (int)(dist[0][i] * 1e6)});
        }

        while (!pq.isEmpty()) {
            int[] edge = pq.poll();
            int u = edge[0], v = edge[1];
            if (visited[v]) continue;
            visited[v] = true;
            tree.get(u).add(v);
            tree.get(v).add(u);

            for (int i = 0; i < n; i++) {
                if (!visited[i]) {
                    pq.offer(new int[]{v, i, (int)(dist[v][i] * 1e6)});
                }
            }
        }

        // DFS 遍历
        List<Integer> visitOrder = new ArrayList<>();
        boolean[] dfsVisited = new boolean[n];
        dfsMST(0, dfsVisited, tree, visitOrder);

        List<LatLng> path = new ArrayList<>();
        List<String> addressList = new ArrayList<>();
        for (int idx : visitOrder) {
            path.add(allPoints.get(idx));
            addressList.add(allAddrs.get(idx));
        }

        return new PathResult(path, addressList);
    }

    //有终点的时候
    public PathResult optimizePathWithMST(LatLng start, List<LatLng> waypoints, LatLng end,
                                          String startAddr, List<String> waypointAddrs, String endAddr) {

        // 合并所有点和地址
        List<LatLng> allPoints = new ArrayList<>();
        List<String> allAddrs = new ArrayList<>();
        allPoints.add(start);
        allAddrs.add(startAddr);
        allPoints.addAll(waypoints);
        allAddrs.addAll(waypointAddrs);
        allPoints.add(end);
        allAddrs.add(endAddr);

        int n = allPoints.size();

        // 构建距离矩阵
        double[][] dist = new double[n][n];
        for (int i = 0; i < n; i++) {
            LatLng pi = allPoints.get(i);
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                LatLng pj = allPoints.get(j);
                dist[i][j] = haversine(pi.latitude, pi.longitude, pj.latitude, pj.longitude);
            }
        }

        // 构建最小生成树（Prim算法）
        boolean[] visited = new boolean[n];
        List<List<Integer>> tree = new ArrayList<>();
        for (int i = 0; i < n; i++) tree.add(new ArrayList<>());

        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingDouble(a -> a[2]));
        visited[0] = true;
        for (int i = 1; i < n; i++) {
            pq.offer(new int[]{0, i, (int)(dist[0][i] * 1e6)});
        }

        while (!pq.isEmpty()) {
            int[] edge = pq.poll();
            int u = edge[0], v = edge[1];
            if (visited[v]) continue;
            visited[v] = true;
            tree.get(u).add(v);
            tree.get(v).add(u);

            for (int i = 0; i < n; i++) {
                if (!visited[i]) {
                    pq.offer(new int[]{v, i, (int)(dist[v][i] * 1e6)});
                }
            }
        }

        // DFS 遍历 MST 得到路径
        List<Integer> visitOrder = new ArrayList<>();
        boolean[] dfsVisited = new boolean[n];

        dfsMST(0, dfsVisited, tree, visitOrder);
        // 把终点放到 visitOrder 的最后
        int endIdx = n - 1;
        if (visitOrder.contains(endIdx)) {
            visitOrder.remove((Integer) endIdx);
            visitOrder.add(endIdx);
        }


        // 构建返回结果
        List<LatLng> path = new ArrayList<>();
        List<String> addressList = new ArrayList<>();
        for (int idx : visitOrder) {
            path.add(allPoints.get(idx));
            addressList.add(allAddrs.get(idx));
        }
        return new PathResult(path, addressList);
    }

    private void dfsMST(int node, boolean[] visited, List<List<Integer>> tree, List<Integer> order) {
        visited[node] = true;
        order.add(node);
        for (int neighbor : tree.get(node)) {
            if (!visited[neighbor]) {
                dfsMST(neighbor, visited, tree, order);
            }
        }
    }


    public double calculateTotalDistance(List<LatLng> path) {
        double total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            total += haversine(path.get(i).latitude, path.get(i).longitude,
                    path.get(i + 1).latitude, path.get(i + 1).longitude);
        }
        return total;
    }

    private void parseJSONResponse(String json, String type, @Nullable String address) {
        try {
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

                switch (type) {
                    case "start":
                        startPoint = new LatLng(lat, lng);
                        break;

                    case "end":
                        endPoint = latLng;
                        break;
                    case "mid":
                        midPoints.add(latLng);
                        if (address != null) {
                            midPointAddresses.add(address);
                        }
                        break;
                }

                requireActivity().runOnUiThread(() -> {
                    Toast.makeText(requireContext(), "定位成功：" + latLng.toString(), Toast.LENGTH_SHORT).show();
                    mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(latLng, 15.8f));

                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(android.R.drawable.ic_dialog_map);
                    OverlayOptions option = new MarkerOptions()
                            .position(latLng)
                            .icon(bitmap);
                    mBaiduMap.addOverlay(option);
                });

            } else {
                String message = jsonObject.getString("message");
                showToast("请求失败：" + message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            showToast("解析失败：" + e.getMessage());
        }
    }

    private void showToast(String msg) {
        requireActivity().runOnUiThread(() ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        );
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
    private void showFacilityCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("景区分类");

        // 定义类别按钮的文本
        String[] categories = {"自然风光", "历史文化", "景区设施"};

        builder.setItems(categories, (dialog, which) -> {
            String selectedCategory = categories[which];
            dialog.dismiss();

            // 获取指定类别下所有叶节点的名称数组
            List<String> leafNodeNames = spotData.getAllLeafNodeNames(selectedCategory);

            // 存储标记相关信息，这里使用List存储MarkerOptions，方便后续清除标记
            List<MarkerOptions> markerOptionsList = new ArrayList<>();

            // 遍历叶节点名称，获取经纬度并在地图上标记
            for (String nodeName : leafNodeNames) {
                if (!TextUtils.isEmpty(nodeName)) {
                    // 新增：更新搜索次数
                    demoApplication.increaseSearchTimes(nodeName);
                    NetworkUtils.getLocationFromAddress(nodeName, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            requireActivity().runOnUiThread(() ->
                                    Toast.makeText(requireContext(), "请求失败：" + e.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful() && response.body() != null) {
                                String responseData = response.body().string();
                                LatLng latLng = parseLocationResponse(responseData);
                                if (latLng != null) {
                                    requireActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), "定位成功：" + latLng.toString(), Toast.LENGTH_SHORT).show();
                                        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newLatLngZoom(latLng, 15.8f));

                                        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.ic_tag);
                                        MarkerOptions option = new MarkerOptions()
                                                .position(latLng)
                                                .icon(bitmap);
                                        mBaiduMap.addOverlay(option);
                                        // 将MarkerOptions添加到列表中，方便后续清除标记
                                        markerOptionsList.add(option);
                                    });
                                }
                            } else {
                                requireActivity().runOnUiThread(() ->
                                        Toast.makeText(requireContext(), "响应失败：" + response.message(), Toast.LENGTH_SHORT).show()
                                );
                            }
                        }
                    });
                }
            }
            // 保存标记列表，便于后续清除标记操作
            markerPositions.addAll(markerOptionsList);
        });

        builder.show();
    }

    private LatLng parseLocationResponse(String responseData) {
        try {
            // 这里按照你之前类似解析JSON获取经纬度的逻辑进行解析
            int start = responseData.indexOf("{");
            int end = responseData.lastIndexOf("}");
            if (start != -1 && end != -1) {
                responseData = responseData.substring(start, end + 1);
            }

            JSONObject jsonObject = new JSONObject(responseData);
            int status = jsonObject.getInt("status");
            if (status == 0) {
                JSONObject result = jsonObject.getJSONObject("result");
                JSONObject location = result.getJSONObject("location");
                double lat = location.getDouble("lat");
                double lng = location.getDouble("lng");
                return new LatLng(lat, lng);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }



    private void clearMapMarkers() {
        mBaiduMap.clear();
        markerPositions.clear();
        Toast.makeText(requireContext(), "地图标记已清空", Toast.LENGTH_SHORT).show();
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