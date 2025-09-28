/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2021 Tuya Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NO
 */

package com.aether.aethersmart.sample.device.config.main;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.aether.aethersmart.sample.device.config.ble.DeviceConfigBleAndDualActivity;
import com.tuya.appsdk.sample.device.config.R;
import com.aether.aethersmart.sample.device.config.ap.DeviceConfigAPActivity;
import com.aether.aethersmart.sample.device.config.ez.DeviceConfigEZActivity;
import com.aether.aethersmart.sample.device.config.mesh.DeviceConfigMeshActivity;
import com.aether.aethersmart.sample.device.config.scan.DeviceConfigQrCodeDeviceActivity;
import com.aether.aethersmart.sample.device.config.zigbee.gateway.DeviceConfigZbGatewayActivity;
import com.aether.aethersmart.sample.device.config.zigbee.sub.DeviceConfigZbSubDeviceActivity;
import com.aether.aethersmart.sample.resource.HomeModel;
import com.aether.aethersmart.sample.device.config.qrcode.QrCodeConfigActivity;

import kotlin.jvm.internal.Intrinsics;

/**
 * Device configuration func Widget
 *
 * @author chuanfeng <a href="mailto:developer@tuya.com"/>
 * @since 2021/2/18 1:49 PM
 */
public class DeviceConfigFuncWidget {
    String role_id = "";

    public final View render(Context context) {
        View rootView = LayoutInflater.from(context).inflate(R.layout.device_config_view_func, null, false);
        Intrinsics.checkExpressionValueIsNotNull(rootView, "rootView");
        role_id = context.getSharedPreferences("user_login", 0).getString("current_role", "end_user");

        initView(rootView);
        return rootView;
    }

    private void initView(View rootView) {
        // EZ Mode
        // AP Mode

        // Ble Low Energy
        if (role_id.equals("end_user")) {
            rootView.findViewById(R.id.ll_main_config).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.ll_connect_device).setVisibility(View.GONE);
        } else {
            rootView.findViewById(R.id.ll_main_config).setVisibility(View.GONE);
            rootView.findViewById(R.id.ll_connect_device).setVisibility(View.VISIBLE);
        }
        rootView.findViewById(R.id.ll_connect_device).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HomeModel.getCurrentHome(view.getContext()) == 0) {
                    Toast.makeText(
                            rootView.getContext(),
                            "Please select home",
                            Toast.LENGTH_LONG
                    ).show();
                } else {
                    view.getContext().startActivity(new Intent(view.getContext(), DeviceConfigBleAndDualActivity.class));
                }
            }
        });
        rootView.findViewById(R.id.tv_ble).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("fgh home", HomeModel.getCurrentHome(v.getContext()) + "");
                if (HomeModel.getCurrentHome(v.getContext()) == 0) {
                    Toast.makeText(
                            rootView.getContext(),
                            "Please select home",
                            Toast.LENGTH_LONG
                    ).show();
                    return;
                } else {
                    v.getContext().startActivity(new Intent(v.getContext(), DeviceConfigBleAndDualActivity.class));
                }

            }
        });


    }
}