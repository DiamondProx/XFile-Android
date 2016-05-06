package com.huangjiang.config;

import com.huangjiang.XFileApplication;
import com.huangjiang.utils.SharedPrefsUtil;

/**
 * 配置
 */
public class Config {

    /**
     * 是否开启热点
     */
    public static boolean is_ap = false;


    /**
     * 声音
     */
    final static String SOUND = "sound";
    /**
     * 震动
     */
    final static String VIBRATION = "vibration";
    /**
     * 检测更新
     */
    final static String UPDATE = "update";
    /**
     * 移动数据
     */
    final static String MOBILE_DATA = "mobile_data";
    /**
     * 显示隐藏文件
     */
    final static String HIDDEN = "hidden";
    /**
     * 需要密码连接
     */
    final static String NEED_PWD = "need_pwd";
    /**
     * 静默安装
     */
    final static String INSTALL_SILENT = "install_silent";


    public static boolean getSound() {
        return SharedPrefsUtil.getValue(XFileApplication.context, SOUND, true);
    }

    public static void setSound(boolean soundSetting) {
        SharedPrefsUtil.putValue(XFileApplication.context, SOUND, soundSetting);
    }


    public static boolean getVibration() {
        return SharedPrefsUtil.getValue(XFileApplication.context, VIBRATION, true);
    }

    public static void setVibration(boolean vibrationSetting) {
        SharedPrefsUtil.putValue(XFileApplication.context, VIBRATION, vibrationSetting);
    }

    public static boolean getUpdate() {
        return SharedPrefsUtil.getValue(XFileApplication.context, UPDATE, true);
    }

    public static void setUpdate(boolean updateSetting) {
        SharedPrefsUtil.putValue(XFileApplication.context, UPDATE, updateSetting);
    }

    public static boolean getMobileData() {
        return SharedPrefsUtil.getValue(XFileApplication.context, MOBILE_DATA, false);
    }

    public static void setMobileData(boolean mobileDataSetting) {
        SharedPrefsUtil.putValue(XFileApplication.context, MOBILE_DATA, mobileDataSetting);
    }

    public static boolean getHidden() {
        return SharedPrefsUtil.getValue(XFileApplication.context, HIDDEN, false);
    }

    public static void setHidden(boolean hiddenSetting) {
        SharedPrefsUtil.putValue(XFileApplication.context, HIDDEN, hiddenSetting);
    }

    public static boolean getNeedPWD() {
        return SharedPrefsUtil.getValue(XFileApplication.context, NEED_PWD, false);
    }

    public static void setNeedPWD(boolean needPWDSetting) {
        SharedPrefsUtil.putValue(XFileApplication.context, NEED_PWD, needPWDSetting);
    }

    public static boolean getInstallSilent() {
        return SharedPrefsUtil.getValue(XFileApplication.context, INSTALL_SILENT, false);
    }

    public static void setInstallSilent(boolean installSilentSetting) {
        SharedPrefsUtil.putValue(XFileApplication.context, INSTALL_SILENT, installSilentSetting);
    }

}
