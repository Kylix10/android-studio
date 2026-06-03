package com.example.summer.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.summer.R;
import com.example.summer.datas.LocationConfig;
import com.example.summer.utils.LocationStateManager;

import android.widget.EditText;
import com.example.summer.utils.NetworkUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;

public class LocationSwitchDialogFragment extends DialogFragment {
    private View rootView;
    private RecyclerView rvLocations;
    private Button btnCancel;
    private List<LocationConfig> list;
    private LocationConfig currentActive;

    // 当前GPS定位卡片控件
    private LinearLayout layoutCurrentLocation;
    private TextView tvCurrentLocationStatus;
    private ImageView ivCurrentCheck;
    private LocationClient mLocationClient;
    
    // 手动添加自定义位置控件
    private EditText etCustomLocationName;
    private Button btnSearchAdd;

    // 超时处理器，防止在无定位信号或初始化失败的设备上挂起
    private final android.os.Handler timeoutHandler = new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable timeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (mLocationClient != null) {
                mLocationClient.stop();
            }
            if (getContext() != null) {
                Toast.makeText(getContext(), "GPS定位超时，已开启备用高精度模拟定位 🎯", Toast.LENGTH_LONG).show();
            }
            
            // 优雅降级：若真实定位超时（如在虚拟机/室内无GPS设备中运行），自动采用高精度模拟定位坐标（以北京天安门为例）
            double mockLat = 39.9042;
            double mockLon = 116.4074;
            String mockAddr = "当前位置(模拟)";
            
            LocationConfig config = new LocationConfig(
                    mockAddr,
                    mockLat,
                    mockLon,
                    new int[]{R.drawable.banner1, R.drawable.banner2, R.drawable.banner3, R.drawable.banner4},
                    "101010100",
                    "010-62881144",
                    "\n医疗救助点：您当前位置附近的社区医院或游客中心\n\n警务服务站：您当前位置辖区派出所\n\n周边设有公共厕所、母婴室及残疾人无障碍通道。"
            );
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    LocationStateManager.getInstance().setCurrentLocation(config);
                    dismiss();
                });
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        rootView = inflater.inflate(R.layout.dialog_location_switch, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = LocationStateManager.getInstance().getPresetLocations();
        currentActive = LocationStateManager.getInstance().getCurrentLocation();

        rvLocations = rootView.findViewById(R.id.rvLocations);
        btnCancel = rootView.findViewById(R.id.btnCancel);
        layoutCurrentLocation = rootView.findViewById(R.id.layoutCurrentLocation);
        tvCurrentLocationStatus = rootView.findViewById(R.id.tvCurrentLocationStatus);
        ivCurrentCheck = rootView.findViewById(R.id.ivCurrentCheck);

        rvLocations.setLayoutManager(new LinearLayoutManager(getContext()));
        LocationAdapter adapter = new LocationAdapter(list);
        rvLocations.setAdapter(adapter);

        btnCancel.setOnClickListener(v -> dismiss());

        // 判断当前位置是否已经是激活的自定义GPS位置
        boolean isCurrentActiveCustom = true;
        for (LocationConfig preset : list) {
            if (preset.getName().equals(currentActive.getName())) {
                isCurrentActiveCustom = false;
                break;
            }
        }

        if (isCurrentActiveCustom) {
            ivCurrentCheck.setVisibility(View.VISIBLE);
            layoutCurrentLocation.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow));
            tvCurrentLocationStatus.setText("当前位置已激活：" + currentActive.getName());
        } else {
            ivCurrentCheck.setVisibility(View.GONE);
            layoutCurrentLocation.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.nocolor));
            tvCurrentLocationStatus.setText("点击获取 GPS 精定导引");
        }

        layoutCurrentLocation.setOnClickListener(v -> {
            tvCurrentLocationStatus.setText("正在尝试获取 GPS 定位...");
            startLocationSearch();
        });

        // 绑定手动输入自定义景点的控件并实现点击事件
        etCustomLocationName = rootView.findViewById(R.id.etCustomLocationName);
        btnSearchAdd = rootView.findViewById(R.id.btnSearchAdd);

        btnSearchAdd.setOnClickListener(v -> {
            String input = etCustomLocationName.getText().toString().trim();
            if (input.isEmpty()) {
                Toast.makeText(getContext(), "请输入景区或地点名称", Toast.LENGTH_SHORT).show();
                return;
            }
            btnSearchAdd.setEnabled(false);
            btnSearchAdd.setText("添加中...");
            NetworkUtils.getLocationFromAddress(input, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            btnSearchAdd.setEnabled(true);
                            btnSearchAdd.setText("添加并切换");
                            Toast.makeText(getContext(), "获取位置失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        String json = response.body().string();
                        try {
                            int startIdx = json.indexOf("{");
                            int endIdx = json.lastIndexOf("}");
                            if (startIdx != -1 && endIdx != -1) {
                                json = json.substring(startIdx, endIdx + 1);
                            }
                            JSONObject jsonObject = new JSONObject(json);
                            int status = jsonObject.getInt("status");
                            if (status == 0) {
                                JSONObject result = jsonObject.getJSONObject("result");
                                JSONObject location = result.getJSONObject("location");
                                double lat = location.getDouble("lat");
                                double lng = location.getDouble("lng");

                                LocationConfig config = new LocationConfig(
                                        input,
                                        lat,
                                        lng,
                                        new int[]{R.drawable.banner1, R.drawable.banner2, R.drawable.banner3, R.drawable.banner4},
                                        "101010100", // 默认北京城市代码，天气 API 直接通过经纬度抓取
                                        "400-123-4567",
                                        "\n医疗救助点：您当前位置附近的社区服务点\n\n警务服务站：您当前辖区警务室\n\n周边提供母婴室、残疾人无障碍通道及公共卫生设施。"
                                );

                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        LocationStateManager.getInstance().getPresetLocations().add(config);
                                        LocationStateManager.getInstance().setCurrentLocation(config);
                                        Toast.makeText(getContext(), "已成功添加并切换景区：" + input, Toast.LENGTH_SHORT).show();
                                        dismiss();
                                    });
                                }
                            } else {
                                String msg = jsonObject.optString("message", "未知错误");
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        btnSearchAdd.setEnabled(true);
                                        btnSearchAdd.setText("添加并切换");
                                        Toast.makeText(getContext(), "定位失败：" + msg, Toast.LENGTH_SHORT).show();
                                    });
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    btnSearchAdd.setEnabled(true);
                                    btnSearchAdd.setText("添加并切换");
                                    Toast.makeText(getContext(), "解析位置失败", Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    } else {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                btnSearchAdd.setEnabled(true);
                                btnSearchAdd.setText("添加并切换");
                                Toast.makeText(getContext(), "网络请求错误", Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }
            });
        });
    }

    private void startLocationSearch() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        } else {
            requestSingleLocation();
        }
    }

    private void requestSingleLocation() {
        try {
            mLocationClient = new LocationClient(requireContext().getApplicationContext());
            LocationClientOption option = new LocationClientOption();
            option.setIsNeedAddress(true);
            //option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy); // 设置高精度定位模式
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            //option.setIsNeedWifi(true); // 强制启用WIFI扫描，关键！
            //option.setScanSpan(2000);  // 2s轮询，缩短首次定位等待
            option.setCoorType("bd09ll"); // 设置坐标类型为百度经纬度
            option.setScanSpan(1000); // 每一秒定位一次，直到获取成功，防止单次定位初始化失败挂起！
            option.setOpenGps(true);
            mLocationClient.setLocOption(option);
            
            // 开启10秒超时保护，防止在无定位信号的设备上无限挂起
            timeoutHandler.removeCallbacks(timeoutRunnable);
            timeoutHandler.postDelayed(timeoutRunnable, 10000);

            mLocationClient.registerLocationListener(new BDAbstractLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation bdLocation) {
                    if (bdLocation != null) {
                        int type = bdLocation.getLocType();
                        // 61: GPS, 161: Network, 66: Offline (定位成功状态码)
                        if (type == BDLocation.TypeGpsLocation 
                                || type == BDLocation.TypeNetWorkLocation 
                                || type == BDLocation.TypeOffLineLocation) {
                            
                            // 成功获取定位，取消超时保护
                            timeoutHandler.removeCallbacks(timeoutRunnable);

                            double lat = bdLocation.getLatitude();
                            double lon = bdLocation.getLongitude();
                            
                            String addr = bdLocation.getAddrStr();
                            if (addr == null || addr.isEmpty()) {
                                addr = bdLocation.getDistrict();
                            }
                            if (addr == null || addr.isEmpty()) {
                                addr = bdLocation.getCity();
                            }
                            if (addr == null || addr.isEmpty()) {
                                addr = "我的当前位置";
                            } else {
                                addr = "当前：" + addr;
                            }

                            final String finalAddr = addr;
                            LocationConfig config = new LocationConfig(
                                    finalAddr,
                                    lat,
                                    lon,
                                    new int[]{R.drawable.banner1, R.drawable.banner2, R.drawable.banner3, R.drawable.banner4},
                                    "101010100", // 默认北京城市代码，天气 API 直接通过经纬度抓取
                                    "400-123-4567",
                                    "\n医疗救助点：您当前位置附近的社区服务点\n\n警务服务站：您当前辖区警务室\n\n周边提供母婴室、残疾人无障碍通道及公共卫生设施。"
                            );
                            
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    LocationStateManager.getInstance().setCurrentLocation(config);
                                    Toast.makeText(getContext(), "获取定位成功：" + finalAddr, Toast.LENGTH_SHORT).show();
                                    dismiss();
                                });
                            }
                            
                            if (mLocationClient != null) {
                                mLocationClient.stop();
                            }
                        }
                    }
                }
            });
            mLocationClient.start();
        } catch (Exception e) {
            e.printStackTrace();
            timeoutHandler.removeCallbacks(timeoutRunnable);
            Toast.makeText(getContext(), "启动定位失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
            tvCurrentLocationStatus.setText("点击获取 GPS 精定导引");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestSingleLocation();
            } else {
                Toast.makeText(getContext(), "位置权限已被拒绝，无法使用当前位置导览", Toast.LENGTH_SHORT).show();
                tvCurrentLocationStatus.setText("未获得定位权限");
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            Window window = getDialog().getWindow();
            window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            window.setGravity(android.view.Gravity.BOTTOM);
            window.setWindowAnimations(android.R.style.Animation_InputMethod);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (timeoutHandler != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        if (mLocationClient != null) {
            mLocationClient.stop();
        }
    }

    private class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {
        private final List<LocationConfig> items;

        LocationAdapter(List<LocationConfig> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_location_select, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            LocationConfig item = items.get(position);
            holder.tvLocationName.setText(item.getName());
            holder.tvCoordinates.setText(String.format(
                    "北纬 %.4f, 东经 %.4f",
                    item.getLatitude(),
                    item.getLongitude()
            ));

            if (item.getName().contains("避暑山庄")) {
                holder.tvLocationIcon.setText("🌲");
            } else if (item.getName().contains("颐和园")) {
                holder.tvLocationIcon.setText("🏯");
            } else if (item.getName().contains("西湖")) {
                holder.tvLocationIcon.setText("🛶");
            } else if (item.getName().contains("泰山")) {
                holder.tvLocationIcon.setText("⛰️");
            } else {
                holder.tvLocationIcon.setText("📍");
            }

            boolean isActive = item.getName().equals(currentActive.getName());
            if (isActive) {
                holder.ivCheck.setVisibility(View.VISIBLE);
                holder.tvLocationName.setTextColor(ContextCompat.getColor(requireContext(), R.color.text));
                holder.layoutContainer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.yellow));
            } else {
                holder.ivCheck.setVisibility(View.GONE);
                holder.tvLocationName.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                holder.layoutContainer.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.nocolor));
            }

            holder.layoutContainer.setOnClickListener(v -> {
                if (!isActive) {
                    LocationStateManager.getInstance().setCurrentLocation(item);
                    Toast.makeText(getContext(), "已切换景区定位至：" + item.getName(), Toast.LENGTH_SHORT).show();
                }
                dismiss();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layoutContainer;
            TextView tvLocationIcon;
            TextView tvLocationName;
            TextView tvCoordinates;
            ImageView ivCheck;

            ViewHolder(View itemView) {
                super(itemView);
                layoutContainer = itemView.findViewById(R.id.layoutContainer);
                tvLocationIcon = itemView.findViewById(R.id.tvLocationIcon);
                tvLocationName = itemView.findViewById(R.id.tvLocationName);
                tvCoordinates = itemView.findViewById(R.id.tvCoordinates);
                ivCheck = itemView.findViewById(R.id.ivCheck);
            }
        }
    }
}
