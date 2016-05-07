package com.huangjiang.utils;

import android.content.Context;
import android.content.Intent;

import com.huangjiang.activity.XFileActivityManager;
import com.huangjiang.core.ThreadPoolManager;
import com.huangjiang.service.IMService;
import com.umeng.analytics.MobclickAgent;

/**
 * 全局异常
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    public static final String TAG = CrashHandler.class.getSimpleName();
    private static CrashHandler INSTANCE = new CrashHandler();
    private Context mContext;
    private Thread.UncaughtExceptionHandler mDefaultHandler;


    private CrashHandler() {

    }

    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    public void init(Context ctx) {
        mContext = ctx;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }


    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            mDefaultHandler.uncaughtException(thread, ex);
        } else {
            exceptionOption();
        }
        System.out.println("****uncaughtException");
    }


    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false
     */
    private boolean handleException(Throwable ex) {
        if (ex == null || mContext == null) {
            return true;
        }
        MobclickAgent.reportError(mContext, ex);
        System.out.println("****handleException");
        return true;
    }

    /**
     * 上报异常时需要做的对象处理
     */
    private void exceptionOption() {
        ThreadPoolManager.release();
        mContext.stopService(new Intent(mContext, IMService.class));
        XFileActivityManager.create().finishAllActivity();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
