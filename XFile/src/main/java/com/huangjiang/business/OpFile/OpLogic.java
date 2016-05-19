package com.huangjiang.business.opfile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.huangjiang.XFileApp;
import com.huangjiang.business.BaseLogic;
import com.huangjiang.business.event.OpFileEvent;
import com.huangjiang.business.model.FileType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.core.ThreadPoolManager;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.MediaStoreUtils;
import com.huangjiang.utils.StringUtils;
import com.huangjiang.utils.XFileUtils;
import com.huangjiang.xfile.R;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 文件操作
 */
public class OpLogic extends BaseLogic {

    Logger logger = Logger.getLogger(OpLogic.class);

    private Context context;

    public OpLogic(Context context) {
        this.context = context;
    }

    /**
     * 重命名
     */
    public void renameFile(final TFileInfo tFileInfo, final String newName, final OpFileEvent.Target target) {
        ThreadPoolManager.getInstance(OpLogic.class.getName()).startTaskThread(new Runnable() {
            @Override
            public void run() {
                OpFileEvent event = new OpFileEvent(OpFileEvent.OpType.RENAME, tFileInfo);
                File file = new File(tFileInfo.getPath());
                String savePath = file.getParent();
                savePath += File.separator;
                savePath += newName;
                if (!StringUtils.isEmpty(tFileInfo.getExtension())) {
                    savePath += "." + tFileInfo.getExtension();
                }
                File saveFile = new File(savePath);
                if (!saveFile.exists()) {
                    if (file.renameTo(saveFile)) {
                        String originalPath = tFileInfo.getPath();
                        FileType originalType = tFileInfo.getFileType();
                        String fileName = saveFile.getName();
                        if (!StringUtils.isEmpty(fileName) && fileName.lastIndexOf(".") > 0) {
                            int extensionIndex = fileName.lastIndexOf(".");
                            tFileInfo.setExtension(fileName.substring(extensionIndex + 1));
                            tFileInfo.setName(fileName.substring(0, extensionIndex));
                        } else {
                            tFileInfo.setExtension("");
                            tFileInfo.setName(fileName);
                        }
                        tFileInfo.setFullName(saveFile.getName());
                        tFileInfo.setPath(saveFile.getAbsolutePath());
                        if (XFileUtils.checkEndsWithInStringArray(fileName, context.getResources().getStringArray(R.array.fileEndingImage))) {
                            tFileInfo.setFileType(FileType.Image);
                        } else if (XFileUtils.checkEndsWithInStringArray(fileName, context.getResources().getStringArray(R.array.fileEndingAudio))) {
                            tFileInfo.setFileType(FileType.Audio);
                        } else if (XFileUtils.checkEndsWithInStringArray(fileName, context.getResources().getStringArray(R.array.fileEndingVideo))) {
                            tFileInfo.setFileType(FileType.Video);
                        } else if (XFileUtils.checkEndsWithInStringArray(fileName, context.getResources().getStringArray(R.array.fileEndingApk))) {
                            tFileInfo.setFileType(FileType.Apk);
                        } else {
                            tFileInfo.setFileType(FileType.Normal);
                        }
                        // 刷新媒体库
                        if (tFileInfo.getFileType() == FileType.Image || tFileInfo.getFileType() == FileType.Audio || tFileInfo.getFileType() == FileType.Video) {
                            MediaStoreUtils.resetMediaStore(XFileApp.context, originalPath);
                            MediaStoreUtils.resetMediaStore(XFileApp.context, saveFile.getAbsolutePath());
                        }
                        if (tFileInfo.getFileType() == originalType) {
                            OpFileEvent changeEvent = new OpFileEvent(OpFileEvent.OpType.CHANGE, null);
                            changeEvent.setFileType(tFileInfo.getFileType());
                            changeEvent.setSuccess(true);
                            triggerEvent(changeEvent);
                        } else {
                            OpFileEvent changeEvent = new OpFileEvent(OpFileEvent.OpType.CHANGE, null);
                            changeEvent.setFileType(originalType);
                            changeEvent.setSuccess(true);
                            triggerEvent(changeEvent);
                            changeEvent.setFileType(tFileInfo.getFileType());
                            triggerEvent(changeEvent);
                        }
                        event.setSuccess(true);
                    } else {
                        event.setMessage(context.getString(R.string.rename_failed));
                        event.setSuccess(false);
                    }
                } else {
                    event.setMessage(context.getString(R.string.repeat_name));
                    event.setSuccess(false);
                }
                event.setTarget(target);
                triggerEvent(event);
            }
        });


    }

    /**
     * 删除文件
     */
    public void deleteFile(final TFileInfo tFileInfo) {
        ThreadPoolManager.getInstance(OpLogic.class.getName()).startTaskThread(new Runnable() {
            @Override
            public void run() {
                OpFileEvent event = new OpFileEvent(OpFileEvent.OpType.DELETE, tFileInfo);
                try {
                    File file = new File(tFileInfo.getPath());
                    if (file.exists()) {
                        boolean b = file.delete();
                        if (b) {
                            event.setSuccess(true);
                            if (tFileInfo.getFileType() == FileType.Image || tFileInfo.getFileType() == FileType.Audio || tFileInfo.getFileType() == FileType.Video) {
                                MediaStoreUtils.resetMediaStore(XFileApp.context, file.getAbsolutePath());
                            }
                        } else {
                            event.setSuccess(false);
                        }

                    } else {
                        event.setSuccess(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.e(e.getMessage());
                    event.setSuccess(false);
                }
                triggerEvent(event);
            }
        });
    }

    /**
     * 程序卸载
     */
    public void unInstall(TFileInfo tFileInfo) {
        if (tFileInfo != null && !StringUtils.isEmpty(tFileInfo.getPackageName())) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_DELETE);
            intent.setData(Uri.parse("package:" + tFileInfo.getPackageName()));
            context.startActivity(intent);
        }
    }

    /**
     * 备份安装包
     */
    public void backUpApk(final TFileInfo tFileInfo) {
        ThreadPoolManager.getInstance(OpLogic.class.getName()).startTaskThread(new Runnable() {
            @Override
            public void run() {
                OpFileEvent event = new OpFileEvent(OpFileEvent.OpType.BACKUP, tFileInfo);
                try {
                    File fromFile = new File(tFileInfo.getPath());
                    if (!fromFile.exists() || !fromFile.isFile()) {
                        return;
                    }
                    String prePath = "";
                    prePath += Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator;
                    prePath += "XFile" + File.separator;
                    prePath += "apk" + File.separator;
                    String tempSavePath = prePath + tFileInfo.getFullName();
                    File toFile = new File(tempSavePath);
                    if (!toFile.getParentFile().exists()) {
                        if (!toFile.getParentFile().mkdirs()) {
                            event.setSuccess(false);
                            triggerEvent(event);
                            return;
                        }
                    }
                    int i = 0;
                    while (toFile.exists()) {
                        i++;
                        toFile = new File(prePath + tFileInfo.getName() + "-" + i + tFileInfo.getExtension());
                    }

                    java.io.FileInputStream fosfrom = new java.io.FileInputStream(fromFile);
                    java.io.FileOutputStream fosto = new FileOutputStream(toFile);
                    byte bt[] = new byte[1024];
                    int c;
                    while ((c = fosfrom.read(bt)) > 0) {
                        fosto.write(bt, 0, c); //将内容写到新文件当中
                    }
                    fosfrom.close();
                    fosto.close();
                    event.setSuccess(true);

                } catch (Exception e) {
                    e.printStackTrace();
                    logger.e(e.getMessage());
                    event.setSuccess(false);
                }
                triggerEvent(event);

            }
        });
    }
}
