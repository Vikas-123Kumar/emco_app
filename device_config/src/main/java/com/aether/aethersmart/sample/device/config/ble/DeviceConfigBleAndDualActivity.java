package com.aether.aethersmart.sample.device.config.ble;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aether.aethersmart.sample.resource.HomeModel;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.tuya.appsdk.sample.device.config.R;
import com.thingclips.smart.android.ble.api.BleConfigType;
import com.thingclips.smart.android.ble.api.LeScanSetting;
import com.thingclips.smart.android.ble.api.ScanDeviceBean;
import com.thingclips.smart.android.ble.api.ScanType;
import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.home.sdk.bean.ConfigProductInfoBean;
import com.thingclips.smart.sdk.api.IBleActivator;
import com.thingclips.smart.sdk.api.IBleActivatorListener;
import com.thingclips.smart.sdk.api.IMultiModeActivator;
import com.thingclips.smart.sdk.api.IMultiModeActivatorListener;
import com.thingclips.smart.sdk.api.IThingActivator;
import com.thingclips.smart.sdk.api.IThingActivatorGetToken;
import com.thingclips.smart.sdk.api.IThingDataCallback;
import com.thingclips.smart.sdk.bean.BleActivatorBean;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.thingclips.smart.sdk.bean.MultiModeActivatorBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author AoBing
 * <p>
 * BLE device activation, here you can activate Single and Dual devices.
 * <p>
 * First, you need to make the device enter the active state,
 * and then scan the surrounding devices through the mobile APP.
 * The scanned device can obtain the name and icon of the device by request.
 * <p>
 * Perform different activation methods according to the scanned device type:
 * <p>
 * If it is a single device, proceed directly to the activation step.
 * <p>
 * If it is a dual device, such as a gateway,
 * you need to obtain the Token from the cloud first,
 * and then pass in the Wi-Fi SSID and password to the gateway to perform activation.
 */

public class DeviceConfigBleAndDualActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "BLE";
    private Button mBtnScan, mBtnStop;
    private CircularProgressIndicator cpiLoading;
    private static IBleActivator mBleActivator = ThingHomeSdk.getActivator().newBleActivator();
    private static IMultiModeActivator mMultiModeActivator = ThingHomeSdk.getActivator().newMultiModeActivator();

    private final List<ScanDeviceBean> scanDeviceBeanList = new ArrayList<>();
    private final List<ConfigProductInfoBean> infoBeanList = new ArrayList<>();
    private BleDeviceListAdapter adapter;
    BleActivatorBean bleActivatorBean;
    MultiModeActivatorBean multiModeActivatorBean;
    ImageView imgBack2, img_gif, img_pump;
    TextView txt_time_remain;
    LinearLayout ll_connect;
    Button btn_add_device;
    String role_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.device_config_ble_and_dual_activity);
        checkPermission();
        role_id = getSharedPreferences("user_login", 0).getString("current_role", "end_user");

        initView();
        if (!role_id.equals("end_user")) {
            showDeviceSearch();
        }
    }

    public void showDeviceSearch() {
        final View v = LayoutInflater.from(this).inflate(R.layout.search_devices, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ImageView img_close = v.findViewById(R.id.img_close);
        builder.setView(v);
        AlertDialog dialog = builder.create();
        img_close.setOnClickListener(view -> {
            finish();
            dialog.dismiss();
        });
        Button btnstartsearch = v.findViewById(R.id.btnstartsearch);
        btnstartsearch.setOnClickListener(view -> {
            dialog.dismiss();
            startScan();
            showDeviceInfo();

        });
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    RecyclerView rv_device_details;

    BleDeviceListAdapter_installer bleDeviceListAdapterInstaller;
    AlertDialog dialog_installer;

    public void showDeviceInfo() {
        final View v = LayoutInflater.from(this).inflate(R.layout.search_devices_2, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ImageView img_close = v.findViewById(R.id.img_close);
        builder.setView(v);
        dialog_installer = builder.create();
        img_close.setOnClickListener(view -> {
            finish();
            dialog_installer.dismiss();
        });
        Button btnstartsearch = v.findViewById(R.id.btnstartsearch);
        btnstartsearch.setOnClickListener(view -> {
                    stopScan();
                    startScan();
                }

        );
        bleDeviceListAdapterInstaller = new BleDeviceListAdapter_installer(this);
        rv_device_details = v.findViewById(R.id.rv_device_details);
        rv_device_details.setLayoutManager(new LinearLayoutManager(this));
        rv_device_details.setAdapter(bleDeviceListAdapterInstaller);
        dialog_installer.setCancelable(false);
        dialog_installer.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog_installer.setCanceledOnTouchOutside(false);
        dialog_installer.show();

    }


    int totalDuration = 120;
    SeekBar seekBar;

    public void startTime() {
        seekBar.setEnabled(false);
        ll_connect.setVisibility(View.VISIBLE);
        mRvl.setVisibility(View.GONE);
        img_pump.setVisibility(View.GONE);
        btn_add_device.setVisibility(View.GONE);
        new CountDownTimer(totalDuration * 1000, 1000) {
            int elapsed = 0;

            public void onTick(long millisUntilFinished) {
                elapsed++;
                int secondsLeft = (int) (millisUntilFinished / 1000);
                int minutes = secondsLeft / 60;
                int seconds = secondsLeft % 60;

                String time = String.format("%d:%02d", minutes, seconds);
                txt_time_remain.setText(time + " remaining");

                // Update progress (in reverse, since it's counting down)
                seekBar.setProgress(elapsed);
            }

            public void onFinish() {
                txt_time_remain.setText("02:00" + " remaining");
                seekBar.setProgress(0);
            }
        }.start();
    }

    RecyclerView mRvl;

    private void initView() {
        mBtnScan = findViewById(R.id.bt_search);
        ll_connect = findViewById(R.id.ll_connect);
        btn_add_device = findViewById(R.id.btn_add_device);
        mBtnStop = findViewById(R.id.bt_stop);
        img_pump = findViewById(R.id.img_pump);
        seekBar = findViewById(R.id.seekBar);
        mRvl = findViewById(R.id.rvList);
        cpiLoading = findViewById(R.id.cpiLoading);
        imgBack2 = findViewById(R.id.imgBack2);
        img_gif = findViewById(R.id.img_gif);
        txt_time_remain = findViewById(R.id.txt_time_remain);
        imgBack2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btn_add_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopScan();
                startActivator(0);
            }
        });
        mBtnScan.setOnClickListener(this);
        mBtnStop.setOnClickListener(this);
        try {
            Glide.with(this)
                    .load(getResources().getDrawable(R.drawable.connect_gif))
                    .into(img_gif);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        adapter = new BleDeviceListAdapter(this);
        mRvl.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mRvl.setAdapter(adapter);
        setViewVisible(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.bt_search) {
            setViewVisible(true);
            startScan();
        } else if (v.getId() == R.id.bt_stop) {
            setViewVisible(false);
            stopScan();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (role_id.equals("end_user")) {
            startScan();
        }

    }

    private void startScan() {
        infoBeanList.clear();
        scanDeviceBeanList.clear();

        // Scan Single Ble Device
        LeScanSetting scanSetting = new LeScanSetting.Builder()
                .setTimeout(60 * 1000) // Timeout：ms
                .addScanType(ScanType.SINGLE) // If you need to scan for BLE devices, you only need to add ScanType.SINGLE
                .build();

        // start scan
        ThingHomeSdk.getBleOperator().startLeScan(scanSetting, bean -> {
            Log.d(TAG, "扫描结果:" + bean.getUuid());
            scanDeviceBeanList.add(bean);
            getDeviceInfo(bean);
        });
    }

    private void stopScan() {
        ThingHomeSdk.getBleOperator().stopLeScan();
    }

    private void getDeviceInfo(ScanDeviceBean scanDeviceBean) {
        ThingHomeSdk.getActivatorInstance().getActivatorDeviceInfo(scanDeviceBean.getProductId(),
                scanDeviceBean.getUuid(),
                scanDeviceBean.getMac(),
                new IThingDataCallback<ConfigProductInfoBean>() {
                    @Override
                    public void onSuccess(ConfigProductInfoBean result) {
                        infoBeanList.add(result);
                        adapter.notifyDataSetChanged();
                        if (bleDeviceListAdapterInstaller != null) {
                            bleDeviceListAdapterInstaller.notifyDataSetChanged();
                        }
                        Log.d(TAG, "getDeviceInfo:" + result.getName());
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Log.d(TAG, "getDeviceInfoError:" + errorMessage);
                        Toast.makeText(DeviceConfigBleAndDualActivity.this,
                                "getDeviceInfoError:" + errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void addWifiDetailsInstaller(int postion) {
        final View v = LayoutInflater.from(this).inflate(R.layout.connect_installer_device, null);
        EditText etSSID = v.findViewById(R.id.et_ssid);
        EditText etPwd = v.findViewById(R.id.et_pwd);
        TextView txt_device_name = v.findViewById(R.id.txt_device_name);
        TextView txt_device_model = v.findViewById(R.id.txt_device_model);
        txt_device_name.setText(infoBeanList.get(postion).getName());
        Button btn_cancel = v.findViewById(R.id.btnclose);
        Button btn_ok = v.findViewById(R.id.btn_save_schedule);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        AlertDialog dialog = builder.create();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            if (ssid != null) {
                // Remove quotes around SSID name if necessary
                ssid = ssid.replace("\"", "");
                etSSID.setText(ssid);

                Log.d("Connected_Wifi", "Connected to Wi-Fi: " + ssid);
            } else {
                Log.d("Connected_Wifi", "Not connected to any Wi-Fi");
            }
        }
        btn_ok.setOnClickListener(view -> {
            String ssid = Objects.requireNonNull(etSSID.getText()).toString();
            if (TextUtils.isEmpty(ssid)) {
                Toast.makeText(DeviceConfigBleAndDualActivity.this, "SSID is Null", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "SSID is Null");
            }
            // Wi-Fi password can be null
            String pwd = Objects.requireNonNull(etPwd.getText()).toString();
            dialog.cancel();
            startDualActivator(postion, ssid, pwd);
        });
        btn_cancel.setOnClickListener(view -> {
                    showDeviceInfo();
                    dialog.cancel();

                }
        );
        dialog.show();
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCanceledOnTouchOutside(false);
    }

    private void startActivator(int pos) {
        try {
            String type = scanDeviceBeanList.get(pos).getConfigType();
            if (BleConfigType.CONFIG_TYPE_SINGLE.getType().equals(type)) {
                singleActivator(pos);
            } else if (BleConfigType.CONFIG_TYPE_WIFI.getType().equals(type)) {
                if (role_id.equals("end_user")) {
                    dualActivatorDialog(pos);
                } else {
                    addWifiDetailsInstaller(pos);
                }

            } else {
                Toast.makeText(DeviceConfigBleAndDualActivity.this, "Device Type not support", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception exception) {
            Toast.makeText(DeviceConfigBleAndDualActivity.this, "2 " + exception.toString(), Toast.LENGTH_SHORT).show();

        }

    }

    private void singleActivator(int pos) {
        cpiLoading.setVisibility(View.VISIBLE);

        bleActivatorBean = new BleActivatorBean();

        bleActivatorBean.homeId = HomeModel.getCurrentHome(this); // homeId
        bleActivatorBean.address = scanDeviceBeanList.get(pos).getAddress();
        bleActivatorBean.deviceType = scanDeviceBeanList.get(pos).getDeviceType();
        bleActivatorBean.uuid = scanDeviceBeanList.get(pos).getUuid(); // UUID
        bleActivatorBean.productId = scanDeviceBeanList.get(pos).getProductId();

        mBleActivator.startActivator(bleActivatorBean, new IBleActivatorListener() {
            @Override
            public void onSuccess(DeviceBean deviceBean) {
                cpiLoading.setVisibility(View.GONE);
                bleActivatorBean = null;
                Log.d(TAG, "activator success:" + deviceBean.getName());
                Toast.makeText(DeviceConfigBleAndDualActivity.this, "success:" + deviceBean.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int code, String msg, Object handle) {
                cpiLoading.setVisibility(View.GONE);
                bleActivatorBean = null;
                Log.d(TAG, "activator error:" + msg);
                Toast.makeText(DeviceConfigBleAndDualActivity.this, "error:" + msg, Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void stopActivator() {
        if (bleActivatorBean != null) {
            mBleActivator.stopActivator(bleActivatorBean.uuid);
        }
        if (multiModeActivatorBean != null) {
            mMultiModeActivator.stopActivator(multiModeActivatorBean.uuid);
        }
    }

    private void dualActivatorDialog(int pos) {
        final View v = LayoutInflater.from(this).inflate(R.layout.ble_dual_activator_dialog, null);
        EditText etSSID = v.findViewById(R.id.et_ssid);
        EditText etPwd = v.findViewById(R.id.et_pwd);
        TextView text_id = v.findViewById(R.id.text_id);
        Button btn_cancel = v.findViewById(R.id.btn_cancel);
        Button btn_ok = v.findViewById(R.id.btn_ok);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(v);
        AlertDialog dialog = builder.create();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();

            if (ssid != null) {
                // Remove quotes around SSID name if necessary
                ssid = ssid.replace("\"", "");
                etSSID.setText(ssid);

                Log.d("Connected_Wifi", "Connected to Wi-Fi: " + ssid);
            } else {
                Log.d("Connected_Wifi", "Not connected to any Wi-Fi");
            }
        }
        btn_ok.setOnClickListener(view -> {
            String ssid = Objects.requireNonNull(etSSID.getText()).toString();
            if (TextUtils.isEmpty(ssid)) {
                Toast.makeText(DeviceConfigBleAndDualActivity.this, "SSID is Null", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "SSID is Null");
            }
            // Wi-Fi password can be null
            String pwd = Objects.requireNonNull(etPwd.getText()).toString();
            dialog.cancel();
            startDualActivator(pos, ssid, pwd);
        });
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();

    }

    private void startDualActivator(int pos, String ssid, String pwd) {
        long homeId = HomeModel.getCurrentHome(this);
        ThingHomeSdk.getActivatorInstance().getActivatorToken(homeId,
                new IThingActivatorGetToken() {

                    // get Token
                    @Override
                    public void onSuccess(String token) {
                        Log.d(TAG, "getToken success, token :" + token);
                        multiModeActivatorBean = new MultiModeActivatorBean();
                        multiModeActivatorBean.deviceType = scanDeviceBeanList.get(pos).getDeviceType();
                        multiModeActivatorBean.uuid = scanDeviceBeanList.get(pos).getUuid();
                        multiModeActivatorBean.address = scanDeviceBeanList.get(pos).getAddress();
                        multiModeActivatorBean.mac = scanDeviceBeanList.get(pos).getMac();
                        multiModeActivatorBean.ssid = ssid;
                        multiModeActivatorBean.pwd = pwd;
                        multiModeActivatorBean.token = token;
                        multiModeActivatorBean.homeId = homeId;
                        multiModeActivatorBean.timeout = 120 * 1000;
                        startTime();

                        // start activator
                        mMultiModeActivator.startActivator(multiModeActivatorBean, new IMultiModeActivatorListener() {
                            @Override
                            public void onSuccess(DeviceBean deviceBean) {
                                if (deviceBean != null) {
                                    Toast.makeText(DeviceConfigBleAndDualActivity.this, "Connected Successfully", Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "Success:" + deviceBean.getName());
                                    SharedPreferences sharedPreferences = getSharedPreferences("device", 0);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("device_id", deviceBean.getDevId());
                                    editor.apply();
                                    finish();

                                }
                                multiModeActivatorBean = null;
                            }

                            @Override
                            public void onFailure(int code, String msg, Object handle) {
                                Log.d(TAG, "error:" + msg);
                                Toast.makeText(DeviceConfigBleAndDualActivity.this, "error:" + msg, Toast.LENGTH_SHORT).show();
                                cpiLoading.setVisibility(View.GONE);
                                multiModeActivatorBean = null;
                            }
                        });
                    }

                    @Override
                    public void onFailure(String code, String msg) {
                        Log.e(TAG, "getToken failed:" + msg);
                    }
                });


    }

    private static class VH extends RecyclerView.ViewHolder {
        TextView mTvDeviceName;
        LinearLayout mLlItemRoot;
        Button mBtnItemStartActivator;

        public VH(@NonNull View itemView) {
            super(itemView);
            mTvDeviceName = itemView.findViewById(R.id.tv_ble_device_item_name);
            mLlItemRoot = itemView.findViewById(R.id.ll_ble_device_root);
            mBtnItemStartActivator = itemView.findViewById(R.id.bt_item_start_activator);
        }
    }


    private static class BleDeviceListAdapter extends RecyclerView.Adapter<DeviceConfigBleAndDualActivity.VH> {
        private final DeviceConfigBleAndDualActivity activity;

        public BleDeviceListAdapter(DeviceConfigBleAndDualActivity activity) {
            this.activity = activity;
        }

        @NonNull
        @Override
        public DeviceConfigBleAndDualActivity.VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DeviceConfigBleAndDualActivity.VH(LayoutInflater.from(parent.getContext()).inflate(R.layout.device_ble_device_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DeviceConfigBleAndDualActivity.VH holder, int position) {
            if (activity != null && activity.infoBeanList.size() > position) {
                holder.mTvDeviceName.setText(activity.infoBeanList.get(position).getName());
                activity.btn_add_device.setVisibility(View.VISIBLE);
                holder.mBtnItemStartActivator.setOnClickListener(v -> {
                    Log.d(TAG, "点击：" + position);
                    activity.setViewVisible(false);
                    activity.stopScan();
                    activity.startActivator(position);
                });
            }
        }

        @Override
        public int getItemCount() {
            return 1;
        }
    }

    private static class VH_installer extends RecyclerView.ViewHolder {
        TextView txt_device_name, txt_model_number, txt_strength;
        LinearLayout ll_device_details;

        public VH_installer(@NonNull View itemView) {
            super(itemView);
            txt_device_name = itemView.findViewById(R.id.txt_device_name);
            txt_model_number = itemView.findViewById(R.id.txt_model_number);
            ll_device_details = itemView.findViewById(R.id.ll_device_details);
            txt_strength = itemView.findViewById(R.id.txt_strength);
        }
    }


    private static class BleDeviceListAdapter_installer extends RecyclerView.Adapter<DeviceConfigBleAndDualActivity.VH_installer> {
        private final DeviceConfigBleAndDualActivity activity;

        public BleDeviceListAdapter_installer(DeviceConfigBleAndDualActivity activity) {
            this.activity = activity;
        }

        @NonNull
        @Override
        public DeviceConfigBleAndDualActivity.VH_installer onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new DeviceConfigBleAndDualActivity.VH_installer(LayoutInflater.from(parent.getContext()).inflate(R.layout.searching_devices, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull DeviceConfigBleAndDualActivity.VH_installer holder, int position) {
            if (activity != null && activity.scanDeviceBeanList.size() > position) {
                holder.txt_device_name.setText(activity.infoBeanList.get(position).getName());
                holder.txt_strength.setText(activity.scanDeviceBeanList.get(position).getRssi() + "");
                try {
                    holder.ll_device_details.setOnClickListener(v -> {
                        Log.d(TAG, "点击：" + position);
                        Toast.makeText(activity, "Selected", Toast.LENGTH_SHORT).show();
                        activity.stopScan();
                        activity.startActivator(position);
                        if (activity.dialog_installer != null) {
                            activity.dialog_installer.dismiss();
                        }
                    });
                } catch (Exception ex) {
                    Toast.makeText(activity, ex.toString(), Toast.LENGTH_SHORT).show();
                }

            }
        }

        @Override
        public int getItemCount() {
            return activity.scanDeviceBeanList.size();
        }
    }

    // You need to check permissions before using Bluetooth devices
    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_COARSE_LOCATION") != 0 || ContextCompat.checkSelfPermission(this, "android.permission.ACCESS_FINE_LOCATION") != 0) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"}, 1001);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001) {
            if (grantResults.length != 0 && grantResults[0] == 0) {
                Log.i("DeviceConfigBleActivity", "onRequestPermissionsResult: agree");
            } else {
                this.finish();
                Log.e("DeviceConfigBleActivity", "onRequestPermissionsResult: denied");
            }
        } else {
            throw new IllegalStateException("Unexpected value: " + requestCode);
        }
    }

    private void setViewVisible(boolean visible) {
        cpiLoading.setVisibility(visible ? View.VISIBLE : View.GONE);
        mBtnScan.setEnabled(!visible);
        mBtnStop.setEnabled(visible);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopScan();
        stopActivator();
    }
}