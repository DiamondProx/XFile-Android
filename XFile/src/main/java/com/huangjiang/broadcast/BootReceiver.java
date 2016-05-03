package com.huangjiang.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.huangjiang.business.event.OpFileEvent;
import com.huangjiang.business.model.FileType;
import com.huangjiang.business.model.TFileInfo;

import org.greenrobot.eventbus.EventBus;

/**
 * 应用卸载广播
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            packageName = packageName.replace("package:", "");
            TFileInfo tFileInfo = new TFileInfo();
            tFileInfo.setFileType(FileType.Apk);
            tFileInfo.setPackageName(packageName);
            OpFileEvent unInstallEvent = new OpFileEvent(OpFileEvent.OpType.UNINSTALL, tFileInfo);
            unInstallEvent.setSuccess(true);
            EventBus.getDefault().post(unInstallEvent);
        }
    }

}
