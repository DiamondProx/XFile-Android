package com.huangjiang;

import android.app.Application;

import com.huangjiang.message.XFileServer;

public class XFileApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        XFileServer server=new XFileServer();
        server.startServer();
    }

}
