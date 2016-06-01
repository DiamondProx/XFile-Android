package com.huangjiang.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;

import com.huangjiang.XFileApp;
import com.huangjiang.activity.XFileActivityManager;
import com.huangjiang.core.ThreadPoolManager;
import com.huangjiang.service.IMService;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 全局异常
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    Logger logger = Logger.getLogger(CrashHandler.class);

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
        logger.e("****uncaughtException");
    }


    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成. 开发者可以根据自己的情况来自定义异常处理逻辑
     *
     * @param ex 异常
     * @return true:如果处理了该异常信息;否则返回false
     */
    private boolean handleException(final Throwable ex) {
        if (ex == null || mContext == null) {
            return true;
        }
        try {
            String log = getLogInfo(ex);
            saveLog(log);
        } catch (Exception e) {
            e.printStackTrace();
        }
        MobclickAgent.reportError(mContext, ex);
        logger.e("****handleException:" + ex.getMessage());
        return true;
    }

    /**
     * 上报异常时需要做的对象处理
     */
    private void exceptionOption() {
        ThreadPoolManager.release();
        mContext.stopService(new Intent(mContext, IMService.class));
        XFileActivityManager.create().finishAllActivity();
        MobclickAgent.onKillProcess(mContext);
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public String getLogInfo(Throwable throwable) throws PackageManager.NameNotFoundException {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        PackageManager packageManager = XFileApp.context.getPackageManager();
        PackageInfo info = packageManager.getPackageInfo(XFileApp.context.getPackageName(), PackageManager.GET_ACTIVITIES);
        StringBuilder builder = new StringBuilder();
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd,HH-mm-ss");
        builder.append(String.format("Devices Model: %s\n", Build.MODEL));
        builder.append(String.format("Devices SDK Version: %s\n", Build.VERSION.SDK_INT));
        builder.append(String.format("Software Version Name: %s\n", info.versionName));
        builder.append(String.format("Software Version Code: %s\n", info.versionCode));
        //start(使用标准版的应用名作为标识 )
        builder.append(String.format("Software Type: %s\n", "golo"));
        //end
        builder.append(String.format("Crash Time: %s\n", format.format(date)));
        builder.append(writer.toString());
        return builder.toString();
    }

    public void saveLog(String input) throws Exception {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd,HH-mm-ss");
        File folder = new File(Environment.getExternalStorageDirectory(), "crash");
        if (folder.mkdirs() || folder.isDirectory()) {
            File log = new File(folder, format.format(date));
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(log);
                out.write(input.getBytes());
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }
    }
}
