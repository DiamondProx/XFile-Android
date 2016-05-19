package com.huangjiang.view;

import android.app.Activity;
import android.content.Context;

import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.xfile.R;

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
    public static void confirmDel(Activity activity, TFileInfo tFileInfo, CustomDialog.DialogCallback onListener) {
        CustomDialog.ConfirmBuilder builder = new CustomDialog.ConfirmBuilder(activity);
        builder.setTFileInfo(tFileInfo);
        builder.setOnListener(onListener);
        builder.setTitle(activity.getString(R.string.sure_delete_file));
        builder.setContent(tFileInfo.getName());
        builder.create().show();
    }

    /**
     * 确认退出连接
     */
    public static void confirmClose(Activity activity, CustomDialog.DialogCallback onListener) {
        CustomDialog.ConfirmBuilder builder = new CustomDialog.ConfirmBuilder(activity);
        builder.setOnListener(onListener);
        builder.setTitle(activity.getString(R.string.progress_title));
        builder.setContent(activity.getString(R.string.confirm_exit_link));
        builder.create().show();

    }

}
