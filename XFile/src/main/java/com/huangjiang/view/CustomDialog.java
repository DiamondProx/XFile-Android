package com.huangjiang.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.filetransfer.R;


/**
 * 文件属性
 */
public class CustomDialog extends Dialog {

    public CustomDialog(Context context) {
        super(context);
    }

    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    protected CustomDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    public static class PropertyBuilder {
        private Context context;
        private TFileInfo tFileInfo;
        private DialogInterface.OnClickListener positiveButtonClickListener;

        public PropertyBuilder(Context context) {
            this.context = context;
        }

        public void setTFileInfo(TFileInfo tFileInfo) {
            this.tFileInfo = tFileInfo;
        }

        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            final CustomDialog dialog = new CustomDialog(context, R.style.Dialog);
            View layout = inflater.inflate(R.layout.dialog_file_property, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (positiveButtonClickListener != null) {
                layout.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        positiveButtonClickListener.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                    }
                });
            }

            dialog.setContentView(layout);
            return dialog;
        }
    }


    public static class MoreBuilder {

        private Activity activity;
        private TFileInfo tFileInfo;
        private DialogOnItemClick onClickListener;

        public MoreBuilder(Activity activity) {
            this.activity = activity;
        }

        public void setTFileInfo(TFileInfo tFileInfo) {
            this.tFileInfo = tFileInfo;
        }

        public void setOnClickListener(DialogOnItemClick onClickListener) {
            this.onClickListener = onClickListener;
        }

        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final CustomDialog dialog = new CustomDialog(activity, R.style.Dialog);
            View layout = inflater.inflate(R.layout.dialog_file_more, null);



            dialog.addContentView(layout, new ViewGroup.LayoutParams(500, ViewGroup.LayoutParams.WRAP_CONTENT));
            layout.findViewById(R.id.more_uninstall).setOnClickListener(listener);
            layout.findViewById(R.id.more_del).setOnClickListener(listener);
            layout.findViewById(R.id.more_rename).setOnClickListener(listener);
            layout.findViewById(R.id.more_back).setOnClickListener(listener);
            dialog.setContentView(layout);
            return dialog;
        }

        private View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClickItem(v.getId(), tFileInfo);
                }
            }
        };
    }

    public interface DialogOnItemClick {
        void onClickItem(int id, TFileInfo tFileInfo);
    }

}
