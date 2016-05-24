package com.huangjiang.manager;

import android.os.Build;

import com.google.protobuf.ByteString;
import com.huangjiang.XFileApp;
import com.huangjiang.business.model.LinkType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.config.SysConstant;
import com.huangjiang.dao.DFile;
import com.huangjiang.dao.DFileDao;
import com.huangjiang.dao.DTransferDetailDao;
import com.huangjiang.dao.DaoMaster;
import com.huangjiang.manager.callback.Packetlistener;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.XFileUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.RandomAccessFile;

/**
 * 传输任务实例
 */
public class TaskInstance {

    private Logger logger = Logger.getLogger(TaskInstance.class);

    /**
     * 读位置
     */
    private long readIndex;
    /**
     * 读百分比
     */
    private long readPercent;

    /**
     * 写位置
     */
    private long writeIndex;
    /**
     * 写百分比
     */
    private long writePercent;

    /**
     * 是否正在传输
     */
    private boolean isTransmit = false;
    /**
     * 取消/暂停发送
     */
    private boolean isCancel = false;
    /**
     * 传输文件
     */
    private TFileInfo currentTask = null;

    /**
     * 传输记录
     */
    private DFileDao fileDao;

    /**
     * 累计传输
     */
    private DTransferDetailDao transferDetailDao;


    public void TaskInstance() {
        fileDao = DaoMaster.getInstance().newSession().getDFileDao();
        transferDetailDao = DaoMaster.getInstance().newSession().getDTransferDetailDao();
    }


    /**
     * 发送文件
     */
    private void transferFile(final XFileProtocol.File reqFile) {

        short cid = SysConstant.CMD_FILE_SET;
        short sid = SysConstant.SERVICE_DEFAULT;
        final TFileInfo reqTFile = XFileUtils.buildTFile(reqFile);
        try {

            byte[] readBytes;
            long remain = reqFile.getLength() - reqFile.getPosition();

            // 判断传回来的position是否等于文件length，相等的情况下，当作已经传输完成
            if (remain == 0) {
                reqTFile.setFileEvent(FileEvent.SET_FILE_SUCCESS);
                reqTFile.setPercent(100);
                // 重置标记
                readPercent = 0;
                readIndex = 0;
                isTransmit = false;
                triggerEvent(reqTFile);
                return;
            }
            // 继续传输未完成任务
            if (remain >= SysConstant.FILE_SEGMENT_SIZE) {
                readBytes = new byte[SysConstant.FILE_SEGMENT_SIZE];
            } else {
                readBytes = new byte[(int) remain];
            }
            RandomAccessFile randomAccessFile = new RandomAccessFile(reqFile.getPath(), "r");
            randomAccessFile.seek(reqFile.getPosition());
            randomAccessFile.read(readBytes);
            ByteString byteString = ByteString.copyFrom(readBytes);
            XFileProtocol.File.Builder responseFile = reqFile.toBuilder();
            responseFile.setData(byteString);

            Packetlistener packetlistener = new Packetlistener() {
                @Override
                public void onSuccess(short serviceId, Object response) {
                    if (response == null) {
                        reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                        triggerEvent(reqTFile);
                        return;
                    }

                    try {
                        byte[] rsp = (byte[]) response;
                        XFileProtocol.File rspFile = XFileProtocol.File.parseFrom(rsp);
                        TFileInfo rspTFile = XFileUtils.buildTFile(rspFile);
                        rspTFile.setFileEvent(FileEvent.SET_FILE);
                        // 通知界面进度
                        readIndex = rspFile.getPosition();
                        long temPercent = readIndex * 100 / rspFile.getLength();
                        if (readPercent < temPercent) {
                            readPercent = temPercent;
                            // 更新界面
                            rspTFile.setPercent(readPercent);
                            triggerEvent(rspTFile);
                        }
                        // 收到暂停标记
                        if (serviceId == SysConstant.SERVICE_FILE_SET_STOP) {
                            rspTFile.setFileEvent(FileEvent.SET_FILE_STOP);
                            rspTFile.setPercent(readPercent);
                            triggerEvent(rspTFile);
                            // 对方暂停接收文件
                            readIndex = 0;
                            readPercent = 0;
                            isCancel = true;
                            isTransmit = false;
                        }

                        if (!isCancel) {
                            // 正常收到答复继续传送文件
                            transferFile(rspFile);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.e(e.getMessage());
                    }

                }

                @Override
                public void onFaild() {
                    logger.e("****onFaild222222222222222");
                    reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                    triggerEvent(reqTFile);
                }

                @Override
                public void onTimeout() {
                    logger.e("****onFaild444444444444444444");
                    reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                    triggerEvent(reqTFile);
                }
            };
            if (XFileApp.mLinkType == LinkType.CLIENT) {
                IMClientFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, (short) 0);
            } else if (XFileApp.mLinkType == LinkType.SERVER) {
                IMServerFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, (short) 0);
            }

        } catch (Exception e) {
            reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
            triggerEvent(reqTFile);
            e.printStackTrace();
            logger.e(e.getMessage());
        }

    }


    /**
     * 接收保存文件
     */
    private void dispatchReceiveData(Header header, byte[] bodyData) {
        try {
            final XFileProtocol.File reqFile = XFileProtocol.File.parseFrom(bodyData);
            TFileInfo reqTFile = XFileUtils.buildTFile(reqFile);
            String fullPath = reqTFile.getPath();
            byte[] fileData = reqFile.getData().toByteArray();
            RandomAccessFile randomAccessFile = new RandomAccessFile(fullPath, "rw");
            randomAccessFile.seek(reqFile.getPosition());
            randomAccessFile.write(fileData);
            randomAccessFile.close();

            if (reqFile.getPosition() + fileData.length <= reqFile.getLength()) {
                XFileProtocol.File.Builder responseFile = XFileProtocol.File.newBuilder();
                responseFile.setName(reqFile.getName());
                responseFile.setPosition(reqFile.getPosition() + ((long) fileData.length));
                responseFile.setLength(reqFile.getLength());
                responseFile.setPath(reqFile.getPath());
                responseFile.setExtension(reqFile.getExtension());
                responseFile.setFullName(reqFile.getFullName());
                responseFile.setTaskId(reqFile.getTaskId());
                responseFile.setFrom(Build.MODEL);
                responseFile.setIsSend(false);
                final TFileInfo rspTFile = XFileUtils.buildTFile(responseFile.build());

                Packetlistener packetlistener = new Packetlistener() {
                    @Override
                    public void onSuccess(short service, Object response) {

                    }

                    @Override
                    public void onFaild() {
                        logger.e("*****Faild5555555555555");
                        rspTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                        triggerEvent(rspTFile);
                    }

                    @Override
                    public void onTimeout() {
                        logger.e("*****Faild666666666666666666");
                        rspTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                        triggerEvent(rspTFile);
                    }
                };
                writeIndex = responseFile.getPosition();
                long tempPercent = writeIndex * 100 / reqFile.getLength();

                //logger.e("****writePercent111:" + writePercent + ",tempPercent" + tempPercent);


                if (writePercent < tempPercent) {
                    writePercent = tempPercent;
                    // 更新接收状态
                    rspTFile.setPercent(writePercent);
                    triggerEvent(rspTFile);
//                    logger.e("****writePercent222:" + writePercent + ",tempPercent" + tempPercent);
                }
                short sid;
                if (reqFile.getPosition() + fileData.length == reqFile.getLength()) {
//                    logger.e("****writePercent333:" + writePercent + ",tempPercent" + tempPercent);
                    // 文件发送完成,提醒接收端结束状态
                    writePercent = 0;
                    writeIndex = 0;
                    isTransmit = false;
                    isCancel = false;
                    rspTFile.setFileEvent(FileEvent.SET_FILE_SUCCESS);

                }
                if (isCancel) {
//                    logger.e("****dispatchReceive-isCancel=true1111");
                    sid = SysConstant.SERVICE_FILE_SET_STOP;
                    // 暂停操作
                    rspTFile.setFileEvent(FileEvent.SET_FILE_STOP);
                    rspTFile.setPercent(writePercent);
                    isCancel = false;
                    isTransmit = false;
                    writePercent = 0;
                    writeIndex = 0;
                    triggerEvent(rspTFile);
                } else {
                    sid = SysConstant.SERVICE_FILE_SET_SUCCESS;
//                    logger.e("****dispatchReceive-isCancel=false2222");
                }

                short cid = SysConstant.CMD_FILE_SET_RSP;
                if (XFileApp.mLinkType == LinkType.CLIENT) {
                    IMClientFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, header.getSeqnum());
                } else if (XFileApp.mLinkType == LinkType.SERVER) {
                    IMServerFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, header.getSeqnum());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }


    private void resume() {
        final XFileProtocol.File reqFile = XFileUtils.buildSFile(currentTask);
        if (currentTask.isSend()) {
            transferFile(reqFile);
        } else {
            short sid = SysConstant.SERVICE_DEFAULT;
            short cid = SysConstant.CMD_FILE_RESUME;
            if (XFileApp.mLinkType == LinkType.CLIENT) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, reqFile, null, (short) 0);
            } else if (XFileApp.mLinkType == LinkType.SERVER) {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, reqFile, null, (short) 0);
            }
        }
    }

    private void dispatchResume(XFileProtocol.File reqFile) {
        // 删除数据库记录
        transferFile(reqFile);
    }

    private void cancel() {
        final XFileProtocol.File requestFile = XFileUtils.buildSFile(currentTask);
        // 删除数据库记录
        DFile dFile = fileDao.getDFileByTaskId(currentTask.getTaskId());
        fileDao.deleteByTaskId(currentTask.getTaskId());
        // 删除缓存文件
        if (!currentTask.isSend()) {
            delCacheFile(dFile);
        }
        // 通知界面
        currentTask.setFileEvent(FileEvent.CANCEL_FILE);
        triggerEvent(currentTask);
        reset();
        // 删除的是当前正在传送的任务
        short sid = SysConstant.SERVICE_DEFAULT;
        short cid = SysConstant.CMD_FILE_CANCEL;
        if (XFileApp.mLinkType == LinkType.CLIENT) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, requestFile, null, (short) 0);
        } else if (XFileApp.mLinkType == LinkType.SERVER) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, requestFile, null, (short) 0);
        }
    }

    private void dispatchCancel(XFileProtocol.File reqFile) {
        final TFileInfo reqTFile = XFileUtils.buildTFile(reqFile);
        // 删除数据库记录
        DFile dFile = fileDao.getDFileByTaskId(currentTask.getTaskId());
        fileDao.deleteByTaskId(currentTask.getTaskId());
        // 删除缓存文件
        if (!currentTask.isSend()) {
            delCacheFile(dFile);
        }
        // 通知界面
        currentTask.setFileEvent(FileEvent.CANCEL_FILE);
        triggerEvent(currentTask);
        reset();
    }

    /**
     * 重置发送任务
     */
    private void reset() {
        readIndex = 0;
        readPercent = 0;
        writeIndex = 0;
        writePercent = 0;
        currentTask = null;
        isTransmit = false;
        isCancel = false;
    }


    /**
     * 通知界面
     */
    private void triggerEvent(TFileInfo tFileInfo) {
        EventBus.getDefault().post(tFileInfo);
    }

    /**
     * 删除本地缓存文件
     */
    private boolean delCacheFile(DFile dFile) {
        File cacheFile = new File(dFile.getSavePath());
        return cacheFile.delete();
    }

    public boolean isTransmit() {
        return isTransmit;
    }

}
