package com.huangjiang.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;

/**
 * 静默安装
 */
public class ApkInstall {

    static Logger logger = Logger.getLogger(ApkInstall.class);


    /**
     * 请求root权限，用这个请求root权限，等待授权管理返回
     */
    public static boolean upgradeRootPermission2(String pkgCodePath) {
        String cmd = "chmod 777 " + pkgCodePath;
        Process process = null;
        DataOutputStream os = null;
        BufferedReader br = null;
        StringBuilder sb;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("echo return\n");
            os.writeBytes("exit\n");
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            sb = new StringBuilder();
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
                sb.append("\n");
                logger.e("TMS", "temp==" + temp);
                if ("return".equalsIgnoreCase(temp)) {
                    logger.e("TMS", "----------" + sb.toString());
                    return true;
                }
            }
            process.waitFor();
        } catch (Exception e) {
            logger.e("TMS", "异常：" + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
                if (br != null) {
                    br.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    /**
     * 2      * 请求ROOT权限后执行命令（最好开启一个线程）
     * 3      * @param cmd    (pm install -r *.apk)
     * 4      * @return
     * 5
     */
    public static boolean installApkInRoot(File path) {
        String cmd = "pm install -r " + path + "\n";
        Process process = null;
        DataOutputStream os = null;
        BufferedReader br = null;
        StringBuilder sb;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            br = new BufferedReader(new InputStreamReader(process.getInputStream()));

            sb = new StringBuilder();
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
                sb.append("\n");
                logger.e("TMS", "temp==" + temp);
                if ("Success".equalsIgnoreCase(temp)) {
                    logger.e("TMS", "----------" + sb.toString());
                    return true;
                }
            }
            process.waitFor();
        } catch (Exception e) {
            logger.e("TMS", "异常：" + e.getMessage());
        } finally {
            try {
                if (os != null) {
                    os.flush();
                    os.close();
                }
                if (br != null) {
                    br.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }


}
