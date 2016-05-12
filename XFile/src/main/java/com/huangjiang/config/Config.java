package com.huangjiang.config;

import com.huangjiang.XFileApp;
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
        return SharedPrefsUtil.getValue(XFileApp.context, SOUND, true);
    }

    public static void setSound(boolean soundSetting) {
        SharedPrefsUtil.putValue(XFileApp.context, SOUND, soundSetting);
    }


    public static boolean getVibration() {
        return SharedPrefsUtil.getValue(XFileApp.context, VIBRATION, true);
    }

    public static void setVibration(boolean vibrationSetting) {
        SharedPrefsUtil.putValue(XFileApp.context, VIBRATION, vibrationSetting);
    }

    public static boolean getUpdate() {
        return SharedPrefsUtil.getValue(XFileApp.context, UPDATE, true);
    }

    public static void setUpdate(boolean updateSetting) {
        SharedPrefsUtil.putValue(XFileApp.context, UPDATE, updateSetting);
    }

    public static boolean getMobileData() {
        return SharedPrefsUtil.getValue(XFileApp.context, MOBILE_DATA, false);
    }

    public static void setMobileData(boolean mobileDataSetting) {
        SharedPrefsUtil.putValue(XFileApp.context, MOBILE_DATA, mobileDataSetting);
    }

    public static boolean getHidden() {
        return SharedPrefsUtil.getValue(XFileApp.context, HIDDEN, false);
    }

    public static void setHidden(boolean hiddenSetting) {
        SharedPrefsUtil.putValue(XFileApp.context, HIDDEN, hiddenSetting);
    }

    public static boolean getNeedPWD() {
        return SharedPrefsUtil.getValue(XFileApp.context, NEED_PWD, false);
    }

    public static void setNeedPWD(boolean needPWDSetting) {
        SharedPrefsUtil.putValue(XFileApp.context, NEED_PWD, needPWDSetting);
    }

    public static boolean getInstallSilent() {
        return SharedPrefsUtil.getValue(XFileApp.context, INSTALL_SILENT, false);
    }

    public static void setInstallSilent(boolean installSilentSetting) {
        SharedPrefsUtil.putValue(XFileApp.context, INSTALL_SILENT, installSilentSetting);
    }

}
