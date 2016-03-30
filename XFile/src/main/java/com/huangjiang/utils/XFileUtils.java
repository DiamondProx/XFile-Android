package com.huangjiang.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.huangjiang.XFileApplication;

public class XFileUtils {

    /*
     * 是否存在SD存储卡
     */
    public static boolean ExistSDCard() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return true;
        } else
            return false;
    }

    /*
     * 后缀检查
     */
    public static boolean checkEndsWithInStringArray(String checkItsEnd, String[] fileEndings) {
        for (String aEnd : fileEndings) {
            if (checkItsEnd.endsWith(aEnd))
                return true;
        }
        return false;
    }

    /*
     * 获取存储卡路径
     */
    public static String getStorageCardPath() {

        return Environment.getExternalStorageDirectory().getPath();
    }

    /*
     * 时间转换
     */
    @SuppressLint("SimpleDateFormat")
    public static String paserTimeToYMD(long time) {
        System.setProperty("user.timezone", "Asia/Shanghai");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
        return format.format(new Date(time * 1000L));
    }

    /**
     * 根据输入的参数大小，进行单位换算，size单位为bit.视情况转换为KB,MB,GB
     *
     * @param size
     * @return
     */
    public static String getFolderSizeString(long size) {
        String sizeStr = null;
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size < kb) {
            sizeStr = size + "bit";
        } else if (size < mb) {
            size = size / kb;
            sizeStr = size + "KB";
        } else if (size < gb) {
            size = size / mb;
            sizeStr = size + "MB";
        } else {
            size = size / gb;
            sizeStr = size + "GB";
        }
        return sizeStr;
    }

    /**
     * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
     * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
     *
     * @param videoPath 视频的路径
     * @param width     指定输出视频缩略图的宽度
     * @param height    指定输出视频缩略图的高度度
     * @param kind      参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
     *                  其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return 指定大小的视频缩略图
     */
    public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        Bitmap bitmap = null;
        // 获取视频的缩略图
        bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        System.out.println("w" + bitmap.getWidth());
        System.out.println("h" + bitmap.getHeight());
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /*
     * 获取所有存储卡
     */
    public static List<String> getSdCardPaths() {

        List<String> paths = new ArrayList<String>();

        String extFileStatus = Environment.getExternalStorageState();
        File extFile = Environment.getExternalStorageDirectory();
        if (extFileStatus.endsWith(Environment.MEDIA_UNMOUNTED) && extFile.exists() && extFile.isDirectory() && extFile.canWrite()) {
            paths.add(extFile.getAbsolutePath());
        }

        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("mount");
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            int mountPathIndex = 1;
            while ((line = br.readLine()) != null) {
                // format of sdcard file system: vfat/fuse
                if ((!line.contains("fat") && !line.contains("fuse") && !line.contains("storage")) || line.contains("secure") || line.contains("asec") || line.contains("firmware") || line.contains("shell") || line.contains("obb")
                        || line.contains("legacy") || line.contains("data")) {
                    continue;
                }
                String[] parts = line.split(" ");
                int length = parts.length;
                if (mountPathIndex >= length) {
                    continue;
                }
                String mountPath = parts[mountPathIndex];
                if (!mountPath.contains("/") || mountPath.contains("data") || mountPath.contains("Data")) {
                    continue;
                }
                File mountRoot = new File(mountPath);
                if (!mountRoot.exists() || !mountRoot.isDirectory() || !mountRoot.canWrite()) {
                    continue;
                }
                boolean equalsToPrimarySD = mountPath.equals(extFile.getAbsolutePath());
                if (equalsToPrimarySD) {
                    continue;
                }
                paths.add(mountPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paths;
    }

    /**
     * 获取视频时长
     *
     * @param durationSeconds
     * @return
     */
    public static String getDuration(int durationSeconds) {
        int hours = durationSeconds / (60 * 60);
        int leftSeconds = durationSeconds % (60 * 60);
        int minutes = leftSeconds / 60;
        int seconds = leftSeconds % 60;

        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append(addZeroPrefix(hours));
        sBuffer.append(":");
        sBuffer.append(addZeroPrefix(minutes));
        sBuffer.append(":");
        sBuffer.append(addZeroPrefix(seconds));

        return sBuffer.toString();
    }

    public static String addZeroPrefix(int number) {
        if (number < 10) {
            return "0" + number;
        } else {
            return "" + number;
        }

    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    public static String getVersion(Context context) {
        String version = "";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            version = info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return version;
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int mScreenWidth = dm.widthPixels;// 获取屏幕分辨率宽度
        return mScreenWidth;
    }

    /**
     * 获取文件md5值
     *
     * @param file
     * @return
     * @throws FileNotFoundException
     */
    public static String getMd5ByFile(File file) throws FileNotFoundException {
        String value = null;
        FileInputStream in = new FileInputStream(file);
        try {
            MappedByteBuffer byteBuffer = in.getChannel().map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(byteBuffer);
            BigInteger bi = new BigInteger(1, md5.digest());
            value = bi.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }

    public static String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) XFileApplication.context.getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = tm.getDeviceId();
        return deviceId;
    }

    public static String getStoragePathByExtension(String extension) {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator);
        sb.append("XFile" + File.separator);
        switch (extension) {
            case "doc":
                sb.append("doc" + File.separator);
                break;
            default:
                sb.append("other" + File.separator);
                break;
        }
        return sb.toString();
    }


}
