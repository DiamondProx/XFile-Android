package com.huangjiang;

import android.app.Application;
import android.content.Context;

import com.huangjiang.config.Config;
import com.huangjiang.utils.CrashHandler;
import com.huangjiang.utils.SoundHelper;
import com.huangjiang.utils.XFileUtils;
import com.umeng.message.PushAgent;

public class XFileApplication extends Application {

    public static Context context;

    public static String device_id;

    public static int connect_type = 0;// 0 未连接,1 客户端连接,2 服务端连接


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        device_id = XFileUtils.getDeviceId();
        initErrorHandler();
        initMessageHandler();
        SoundHelper.init();
    }

    /**
     * 程序异常处理
     */
    private void initErrorHandler() {
        CrashHandler handler = CrashHandler.getInstance();
        handler.init(this);
    }

    /**
     * 消息推送
     */
    private void initMessageHandler() {
        PushAgent mPushAgent = PushAgent.getInstance(context);
        mPushAgent.setDebugMode(false);
        mPushAgent.onAppStart();
        if (Config.getUpdate()) {
            mPushAgent.enable();
        }
    }


}
