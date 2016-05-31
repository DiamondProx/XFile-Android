package com.huangjiang.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.huangjiang.XFileApp;
import com.huangjiang.business.model.FileType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.xfile.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

public class XFileUtils {

    /*
     * 是否存在SD存储卡
     */
    public static boolean ExistSDCard() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
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
    public static String parseTimeToYMD(long time) {
        System.setProperty("user.timezone", "Asia/Shanghai");
        TimeZone tz = TimeZone.getTimeZone("Asia/Shanghai");
        TimeZone.setDefault(tz);
        SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
        if (Long.toString(time).length() == 10) {
            return format.format(new Date(time * 1000L));
        } else {
            return format.format(new Date(time));
        }
    }

    /**
     * 根据输入的参数大小，进行单位换算，size单位为bit.视情况转换为KB,MB,GB
     */
    public static String parseSize(long size) {
        String sizeStr;
        float kb = 1024;
        float mb = kb * 1024;
        float gb = mb * 1024;
        DecimalFormat fnum = new DecimalFormat("##0.00");
        if (size < kb) {
            sizeStr = size + "bit";
        } else if (size < mb) {
            float showSize = (float) size / kb;
            fnum.format(showSize);
            sizeStr = fnum.format(showSize) + "KB";
        } else if (size < gb) {
            float showSize = (float) size / mb;
            fnum.format(showSize);
            sizeStr = fnum.format(showSize) + "MB";
        } else {
            float showSize = (float) size / gb;
            fnum.format(showSize);
            sizeStr = fnum.format(showSize) + "GB";
        }
        return sizeStr;
    }


    /*
     * 获取外置存储卡路径
     */
    public static List<String> getSdCardPaths() {

        List<String> paths = new ArrayList<>();
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
            String line;
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
                if (!mountPath.contains("/") || mountPath.contains("data") || mountPath.contains("Data") || mountPath.contains("usbotg") || mountPath.contains("uicc")) {
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
     */
    public static String getDuration(int durationSeconds) {
        int hours = durationSeconds / (60 * 60);
        int leftSeconds = durationSeconds % (60 * 60);
        int minutes = leftSeconds / 60;
        int seconds = leftSeconds % 60;
        String duration;
        duration = addZeroPrefix(hours);
        duration += ":";
        duration += addZeroPrefix(minutes);
        duration += ":";
        duration += addZeroPrefix(seconds);
        return duration;
    }

    static String addZeroPrefix(int number) {
        if (number < 10) {
            return "0" + number;
        } else {
            return "" + number;
        }
    }

    /**
     * 获取版本号
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

    /**
     * 获取屏幕宽度
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        // 获取屏幕分辨率宽度
        return dm.widthPixels;
    }


    /**
     * 读取设备号
     */
    public static String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) XFileApp.context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    /**
     * 根据后缀生成保存路径
     */
    public static String getSavePathByExtension(String extension) {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        sb.append("XFile");
        sb.append(File.separator);
        if (checkEndsWithInStringArray(extension, XFileApp.context.getResources().getStringArray(R.array.fileEndingImage))) {
            sb.append("image");
            sb.append(File.separator);
        } else if (XFileUtils.checkEndsWithInStringArray(extension, XFileApp.context.getResources().getStringArray(R.array.fileEndingAudio))) {
            sb.append("music");
            sb.append(File.separator);
        } else if (XFileUtils.checkEndsWithInStringArray(extension, XFileApp.context.getResources().getStringArray(R.array.fileEndingVideo))) {
            sb.append("video");
            sb.append(File.separator);
        } else if (XFileUtils.checkEndsWithInStringArray(extension, XFileApp.context.getResources().getStringArray(R.array.fileEndingApk))) {
            sb.append("apk");
            sb.append(File.separator);
        } else {
            sb.append("other");
            sb.append(File.separator);
        }
        return sb.toString();
    }

    public static String buildTaskId() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static FileType getFileType(Context context, String extension) {
        FileType fileType;
        if (XFileUtils.checkEndsWithInStringArray(extension, context.getResources().getStringArray(R.array.fileEndingImage))) {
            fileType = FileType.Image;
        } else if (XFileUtils.checkEndsWithInStringArray(extension, context.getResources().getStringArray(R.array.fileEndingAudio))) {
            fileType = FileType.Audio;
        } else if (XFileUtils.checkEndsWithInStringArray(extension, context.getResources().getStringArray(R.array.fileEndingVideo))) {
            fileType = FileType.Video;
        } else if (XFileUtils.checkEndsWithInStringArray(extension, context.getResources().getStringArray(R.array.fileEndingApk))) {
            fileType = FileType.Apk;
        } else {
            fileType = FileType.Normal;
        }
        return fileType;
    }

    public static XFileProtocol.File parseProtocol(TFileInfo TFile) {
        XFileProtocol.File.Builder protocol = XFileProtocol.File.newBuilder();
        protocol.setName(TFile.getName());
        protocol.setPosition(TFile.getPosition());
        protocol.setLength(TFile.getLength());
        protocol.setPath(TFile.getPath());
        protocol.setExtension(TFile.getExtension());
        protocol.setFullName(TFile.getFullName());
        protocol.setTaskId(TFile.getTaskId());
        protocol.setIsSend(TFile.isSend());
        protocol.setFrom(TFile.getFrom());
        return protocol.build();
    }

    public static TFileInfo parseTFile(XFileProtocol.File protocol) {
        TFileInfo TFile = new TFileInfo();
        TFile.setName(protocol.getName());
        TFile.setPosition(protocol.getPosition());
        TFile.setLength(protocol.getLength());
        TFile.setPath(protocol.getPath());
        TFile.setExtension(protocol.getExtension());
        TFile.setFullName(protocol.getFullName());
        TFile.setTaskId(protocol.getTaskId());
        TFile.setFrom(protocol.getFrom());
        TFile.setIsSend(protocol.getIsSend());
        return TFile;
    }


    /**
     * 获取当前程序安装路径
     */
    public static String getProgramPath(Context context) {
        PackageManager pm = context.getPackageManager();
        String name = null;
        try {
            PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
            name = packageInfo.applicationInfo.publicSourceDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return name;
    }

    /**
     * SD卡的剩余空间
     */
    public static long getSDFreeSize() {
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //空闲的数据块的数量
        long freeBlocks = sf.getAvailableBlocks();
        //返回SD卡空闲大小
        return freeBlocks * blockSize;  //单位Byte
        //return (freeBlocks * blockSize)/1024;   //单位KB
        //return (freeBlocks * blockSize)/1024 /1024; //单位MB
    }

    /**
     * SD卡总容量
     */
    public static long getSDAllSize() {
        //取得SD卡文件路径
        File path = Environment.getExternalStorageDirectory();
        StatFs sf = new StatFs(path.getPath());
        //获取单个数据块的大小(Byte)
        long blockSize = sf.getBlockSize();
        //获取所有数据块数
        long allBlocks = sf.getBlockCount();
        //返回SD卡大小
        return allBlocks * blockSize; //单位Byte
        //return (allBlocks * blockSize)/1024; //单位KB
        //return (allBlocks * blockSize)/1024/1024; //单位MB
    }

    /**
     * 缓存文件重命名
     */
    public static String rename(String filePath, String extension) {
        File originalFile = new File(filePath);
        String fileName = originalFile.getName();
        String prePath;
        if (!originalFile.getParentFile().toString().equals(File.separator)) {
            if (!originalFile.getParentFile().exists()) {
                if (!originalFile.getParentFile().mkdirs())
                    return "";
            }
            prePath = originalFile.getParentFile().getPath();
        } else {
            prePath = File.separator;
        }
        File reSaveFile = new File(prePath + File.separator + fileName + extension);
        int i = 0;
        while (reSaveFile.exists()) {
            i++;
            reSaveFile = new File(prePath + File.separator + fileName + "-" + i + extension);
        }
        if (!reSaveFile.exists()) {
            if (!originalFile.renameTo(reSaveFile)) {
                return "";
            }
        }
        return reSaveFile.getAbsolutePath();
    }


}
