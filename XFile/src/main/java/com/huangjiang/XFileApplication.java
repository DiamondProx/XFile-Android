package com.huangjiang;

import android.app.Application;
import android.content.Context;

import com.huangjiang.message.DeviceClient;
import com.huangjiang.message.XFileDeviceServer;

import java.nio.ByteBuffer;

public class XFileApplication extends Application {

    public static Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        initMessageService();
    }

    void initMessageService() {
        XFileDeviceServer.start();
        DeviceClient.getInstance().initDeviceClient();
    }

}
