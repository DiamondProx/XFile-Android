package com.huangjiang;

import android.app.Application;
import android.content.Context;

import com.huangjiang.utils.SoundHelper;
import com.huangjiang.utils.XFileUtils;

public class XFileApplication extends Application {

    public static Context context;

    public static String device_id;

    public static int connect_type = 0;//0 未连接,1 客户端连接,2 服务端连接


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        device_id = XFileUtils.getDeviceId();
        SoundHelper.init();
    }

}
