package com.huangjiang.wfs;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.huangjiang.utils.XFileUtils;

public class WebService extends Service {

    public static final int PORT = 7766;
    public static final String WEBROOT = "/";

    private WebServer webServer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        String apkPath = XFileUtils.getProgramPath(this);
        webServer = new WebServer(PORT, WEBROOT, apkPath);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        webServer.setDaemon(true);
        webServer.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        webServer.close();
        super.onDestroy();
    }

}
