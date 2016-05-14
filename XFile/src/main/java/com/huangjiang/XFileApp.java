package com.huangjiang;

import android.app.Application;
import android.content.Context;

import com.huangjiang.business.model.LinkType;
import com.huangjiang.config.Config;
import com.huangjiang.utils.CrashHandler;
import com.huangjiang.utils.SoundHelper;
import com.huangjiang.utils.XFileUtils;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.PushAgent;

public class XFileApp extends Application {

    public static Context context;

    public static String device_id;

    public static LinkType mLinkType = LinkType.NONE;// 0 未连接,1 客户端连接,2 服务端连接


    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        device_id = XFileUtils.getDeviceId();
        initUMeg();
        initErrorHandler();
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
     * 友盟
     */
    void initUMeg() {
        // Push
        PushAgent mPushAgent = PushAgent.getInstance(context);
        mPushAgent.setDebugMode(true);
        mPushAgent.onAppStart();
        if (Config.getUpdate()) {
            mPushAgent.enable();
        } else {
            mPushAgent.disable();
        }
        // Analytics
//        MobclickAgent.setDebugMode(true);
        MobclickAgent.openActivityDurationTrack(false);
    }


}
