package com.huangjiang.utils;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.Display;
import android.view.Window;

/**
 * 屏幕工具
 */
public class DisplayUtils {

    public static Dimension getAreaOne(Activity activity) {
        Dimension dimen = new Dimension();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Display disp = activity.getWindowManager().getDefaultDisplay();
            Point outP = new Point();
            disp.getSize(outP);
            dimen.mWidth = outP.x;
            dimen.mHeight = outP.y;
        } else {
            Rect rect = new Rect();
            Window win = activity.getWindow();
            win.getDecorView().getWindowVisibleDisplayFrame(rect);
            dimen.mWidth = rect.width();
            dimen.mHeight = rect.height();
        }
        return dimen;
    }

    public static Dimension getAreaTwo(Activity activity) {
        Dimension dimen = new Dimension();
        Rect outRect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);
        System.out.println("top:" + outRect.top + " ; left: " + outRect.left);
        dimen.mWidth = outRect.width();
        dimen.mHeight = outRect.height();
        return dimen;
    }

    public static Dimension getAreaThree(Activity activity) {
        Dimension dimen = new Dimension();
        // 用户绘制区域
        Rect outRect = new Rect();
        activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(outRect);
        dimen.mWidth = outRect.width();
        dimen.mHeight = outRect.height();
        // end
        return dimen;
    }

    public static int getAndroidTitleBar(Activity activity) {
        return getAreaOne(activity).mHeight - getAreaTwo(activity).mHeight;
    }

    public static class Dimension {
        public int mWidth;
        public int mHeight;

        public Dimension() {
        }
    }

}
