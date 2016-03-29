package com.huangjiang.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.huangjiang.manager.IMClientMessageManager;
import com.huangjiang.manager.IMDeviceServerManager;
import com.huangjiang.manager.IMClientFileManager;
import com.huangjiang.manager.IMServerFileManager;
import com.huangjiang.manager.IMServerMessageManager;
import com.huangjiang.utils.Logger;

/**
 *
 */
public class IMService extends Service {

    private Logger logger = Logger.getLogger(IMClientMessageManager.class);

    private IMDeviceServerManager deviceServerMgr = IMDeviceServerManager.getInstance();
    private IMServerMessageManager messageServerManager = IMServerMessageManager.getInstance();
    private IMClientMessageManager messageClientManager = IMClientMessageManager.getInstance();
    private IMServerFileManager fileServerManager = IMServerFileManager.getInstance();
    private IMClientFileManager fileClientManager = IMClientFileManager.getInstance();


    /**
     * binder
     */
    private IMServiceBinder binder = new IMServiceBinder();

    public class IMServiceBinder extends Binder {
        public IMService getService() {
            return IMService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        deviceServerMgr.start();
        messageServerManager.start();
        fileServerManager.start();
        logger.e("****IMServiceStart");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        deviceServerMgr.stop();
        messageServerManager.stop();
        messageClientManager.stop();
        fileServerManager.stop();
        fileClientManager.stop();
        logger.e("****IMServiceStop");

    }
}
