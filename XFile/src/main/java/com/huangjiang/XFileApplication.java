package com.huangjiang;

import android.app.Application;
import android.content.Context;

import com.huangjiang.message.DeviceClient;
import com.huangjiang.message.DeviceServer;
import com.huangjiang.message.FileServer;
import com.huangjiang.message.MessageServer;

public class XFileApplication extends Application {

    public static Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        initMessageService();
    }

    void initMessageService() {
        // 启动文件服务器
//        FileServer.getInstance().start();
        // 启动发现设备服务器
        DeviceServer.start();
        // 启动发现设备客户端
        DeviceClient.getInstance().initDeviceClient();
        // 启动消息服务器
//        MessageServer.getInstance().start();
    }

}
