package com.huangjiang.business.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import com.huangjiang.business.BaseLogic;
import com.huangjiang.business.event.FindResEvent;
import com.huangjiang.business.event.InstallEvent;
import com.huangjiang.business.event.RootEvent;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.core.ThreadPoolManager;
import com.huangjiang.utils.ApkInstall;

import java.io.File;
import java.util.List;

/**
 * 查找本地安装程序
 */
public class AppLogic extends BaseLogic {

    private AppInterface appInterface;

    public AppLogic(Context context) {
        appInterface = new AppInterface(context);
    }

    public void searchApp() {
        ThreadPoolManager.getInstance(AppLogic.class.getName()).startTaskThread(new Runnable() {
            @Override
            public void run() {
                List<TFileInfo> appList = appInterface.searchApp();
                triggerEvent(FindResEvent.MimeType.APK, appList);
            }
        });
    }

    /**
     * 获取root权限
     */
    public void upgradeRootPermission(final String pkgCodePath) {
        ThreadPoolManager.getInstance(AppLogic.class.getName()).startTaskThread(new Runnable() {
            @Override
            public void run() {
                boolean result = ApkInstall.upgradeRootPermission2(pkgCodePath);
                RootEvent rootEvent = new RootEvent();
                rootEvent.setResult(result);
                triggerEvent(rootEvent);
            }
        });
    }

    /**
     * 静默安装
     */
    public void installApkInRoot(final TFileInfo tFileInfo) {
        ThreadPoolManager.getInstance(AppLogic.class.getName()).startTaskThread(new Runnable() {
            @Override
            public void run() {
                InstallEvent installEvent = new InstallEvent();
                File file = new File(tFileInfo.getPath());
                if (file.exists()) {
                    boolean b = ApkInstall.installApkInRoot(file);
                    installEvent.setSuccess(b);
                    if (b) {
                        // TODO save database
                    }
                } else {
                    installEvent.setSuccess(false);
                }
                installEvent.settFileInfo(tFileInfo);
                triggerEvent(installEvent);
            }
        });
    }

}


