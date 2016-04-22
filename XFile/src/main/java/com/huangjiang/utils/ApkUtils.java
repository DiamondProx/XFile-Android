package com.huangjiang.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.huangjiang.business.model.TFileInfo;

/**
 * 程序帮助助手
 */
public class ApkUtils {

    /**
     * 卸载程序
     */
    public static void unInstall(Context context, TFileInfo tFileInfo) {
        if (tFileInfo != null && !StringUtils.isEmpty(tFileInfo.getPackageName())) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + tFileInfo.getPackageName()));
            context.startActivity(intent);
        }
    }

    /**
     * 文件备份
     */
    public static void backUp(Context context, TFileInfo tFileInfo) {
        String sourcePath = "";
        String targetPath = "";
    }
}
