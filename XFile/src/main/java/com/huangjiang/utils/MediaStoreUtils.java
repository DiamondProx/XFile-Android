package com.huangjiang.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.File;

/**
 * 多媒体工具
 */
public class MediaStoreUtils {

    public static void resetMediaStore(Context context, String refreshFile) {
        try {
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(Uri.fromFile(new File(refreshFile)));
            context.sendBroadcast(scanIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
