package com.huangjiang.view;

import android.app.Activity;
import android.content.Context;

import com.huangjiang.business.model.TFileInfo;

/**
 * 对话框帮助
 */
public class DialogHelper {

    public static void showProperty(Context context, TFileInfo tFileInfo) {
        CustomDialog.PropertyBuilder builder = new CustomDialog.PropertyBuilder(context);
        builder.setTFileInfo(tFileInfo);
        builder.create().show();
    }

    public static void showMore(Activity activity, TFileInfo tFileInfo) {
        CustomDialog.MoreBuilder builder = new CustomDialog.MoreBuilder(activity);
        builder.setTFileInfo(tFileInfo);
        builder.create().show();
    }

}
