package com.huangjiang;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.huangjiang.service.IMService;
import com.huangjiang.utils.XFileUtils;

public class XFileApplication extends Application {

    public static Context context;

    public static int broadcast_port;

    public static int message_port;

    public static int file_port;

    public static String device_id;



    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        device_id = XFileUtils.getDeviceId();
    }

}
