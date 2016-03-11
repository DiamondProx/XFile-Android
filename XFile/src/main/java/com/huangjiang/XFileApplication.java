package com.huangjiang;

import android.app.Application;

import com.huangjiang.message.UdpServer;

public class XFileApplication extends Application {
;
    @Override
    public void onCreate() {
        super.onCreate();
        UdpServer.start();
    }

}
