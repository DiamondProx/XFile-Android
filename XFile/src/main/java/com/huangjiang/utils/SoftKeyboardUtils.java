package com.huangjiang.utils;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * 键盘工具
 */
public class SoftKeyboardUtils {

    /**
     * 隐藏键盘
     */
    public static void hiddenSoftKeyboard(Activity activity, EditText et) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()) {
            imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
        }
    }
}
