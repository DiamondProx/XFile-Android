package com.huangjiang.view;

import android.app.Activity;
import android.content.Context;

import com.huangjiang.business.model.TFileInfo;

/**
 * 对话框帮助
 */
public class DialogHelper {

    /**
     * 属性
     */
    public static void showProperty(Context context, TFileInfo tFileInfo) {
        CustomDialog.PropertyBuilder builder = new CustomDialog.PropertyBuilder(context);
        builder.setTFileInfo(tFileInfo);
        builder.create().show();
    }

    /**
     * 更多
     */
    public static void showMore(Activity activity, TFileInfo tFileInfo, CustomDialog.DialogCallback onListener) {
        CustomDialog.MoreBuilder builder = new CustomDialog.MoreBuilder(activity);
        builder.setTFileInfo(tFileInfo);
        builder.setOnClickListener(onListener);
        builder.create().show();
    }

    /**
     * 重命名
     */
    public static void showRename(Activity activity, TFileInfo tFileInfo, CustomDialog.DialogCallback onListener) {
        CustomDialog.RenameBuilder builder = new CustomDialog.RenameBuilder(activity);
        builder.setTFileInfo(tFileInfo);
        builder.setOnListener(onListener);
        builder.create().show();
    }

    /**
     * 删除确认
     */
    public static void showDel(Activity activity, TFileInfo tFileInfo, CustomDialog.DialogCallback onListener) {
        CustomDialog.DelBuilder builder = new CustomDialog.DelBuilder(activity);
        builder.setTFileInfo(tFileInfo);
        builder.setOnListener(onListener);
        builder.create().show();
    }

}
