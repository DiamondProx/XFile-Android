package com.huangjiang;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.huangjiang.service.IMService;

public class XFileApplication extends Application {

    public static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        startService(new Intent(this, IMService.class));
    }


    void initMessageService() {
        // 启动文件服务器
//        FileServer.getInstance().start();
        // 启动发现设备服务器
//        DeviceServerThread.start();
        // 启动发现设备客户端
//        DeviceClient.getInstance().initDeviceClient();
        // 启动消息服务器
//        MessageServerThread.getInstance().start();


    }

}
