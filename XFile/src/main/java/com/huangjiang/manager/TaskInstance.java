package com.huangjiang.manager;

import android.os.Build;

import com.google.protobuf.ByteString;
import com.huangjiang.XFileApp;
import com.huangjiang.business.model.LinkType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.config.SysConstant;
import com.huangjiang.core.ThreadPoolManager;
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

import java.io.RandomAccessFile;
import java.util.Timer;
import java.util.TimerTask;

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


    /**
     * 超时处理
     */
    Timer timer = null;

    /**
     * 超时任务
     */
    TimerTask timerTask = null;

    /**
     * 创建时间
     */
    private long createTime;


    public TaskInstance() {
        fileDao = DaoMaster.getInstance().newSession().getDFileDao();
        transferDetailDao = DaoMaster.getInstance().newSession().getDTransferDetailDao();
        createTime = System.currentTimeMillis();
    }

    /**
     * 传输/续传
     */
    public void transmit() {
        final XFileProtocol.File reqFile = XFileUtils.buildSFile(currentTask);
        if (currentTask.isSend()) {
            isTransmit = true;
            isCancel = false;
            transferFile(reqFile);
        } else {
            isCancel = false;
            waitReceive();
            short sid = SysConstant.SERVICE_DEFAULT;
            short cid = SysConstant.CMD_FILE_RESUME;
            if (XFileApp.mLinkType == LinkType.CLIENT) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, reqFile, null, (short) 0);
            } else if (XFileApp.mLinkType == LinkType.SERVER) {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, reqFile, null, (short) 0);
            }
        }
    }

    /**
     * 发送文件
     */
    private void transferFile(final XFileProtocol.File reqFile) {

        short cid = SysConstant.CMD_FILE_SET;
        short sid = SysConstant.SERVICE_DEFAULT;
        final TFileInfo reqTFile = XFileUtils.buildTFile(reqFile);
        reqTFile.setIsSend(true);
        try {

            byte[] readBytes;
            long remain = reqFile.getLength() - reqFile.getPosition();
            // 判断传回来的position是否等于文件length，相等的情况下，当作已经传输完成
            if (remain == 0) {
                reset();
                reqTFile.setFileEvent(FileEvent.SET_FILE_SUCCESS);
                triggerEvent(reqTFile);
                fileDao.completeTransmit(reqFile.getTaskId(), reqFile.getPosition(), 1);
                transferDetailDao.addTotalSize(reqFile.getLength());
                ThreadPoolManager.getInstance(TaskInstance.class.getName()).startTaskThread(new Runnable() {
                    @Override
                    public void run() {
                        IMFileManager.getInstance().removeTask(getTaskId());
                        IMFileManager.getInstance().checkUndone();
                    }
                });
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
            responseFile.setIsSend(true);

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
                        rspTFile.setIsSend(true);
                        cancelTimeout();
                        fileDao.completeTransmit(rspFile.getTaskId(), rspFile.getPosition(), 0);
                        rspTFile.setFileEvent(FileEvent.SET_FILE);
                        readIndex = rspFile.getPosition();
                        currentTask.setPosition(readIndex);
                        long temPercent = readIndex * 100 / rspFile.getLength();
                        if (readPercent < temPercent) {
                            readPercent = temPercent;
                            triggerEvent(rspTFile);
                        }
                        // 收到暂停标记
                        if (serviceId == SysConstant.SERVICE_FILE_SET_STOP) {
                            rspTFile.setFileEvent(FileEvent.SET_FILE_STOP);
                            triggerEvent(rspTFile);
                            // 对方暂停接收文件
                            readIndex = 0;
                            readPercent = 0;
                            isCancel = true;
                            isTransmit = false;
                            ThreadPoolManager.getInstance(TaskInstance.class.getName()).startTaskThread(new Runnable() {
                                @Override
                                public void run() {
                                    IMFileManager.getInstance().removeTask(getTaskId());
                                    IMFileManager.getInstance().checkUndone();
                                }
                            });
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
                    logger.e("****transferFileOnFailed");
                    if (!isCancel) {
                        reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                        triggerEvent(reqTFile);
                        fileDao.completeTransmit(reqFile.getTaskId(), reqFile.getPosition(), 2);
                    }
                }

                @Override
                public void onTimeout() {
                    logger.e("****transferFileOnTimeout");
                    if (!isCancel) {
                        reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                        triggerEvent(reqTFile);
                        fileDao.completeTransmit(reqFile.getTaskId(), reqFile.getPosition(), 2);
                    }
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
    public void dispatchReceiveData(Header header, byte[] bodyData) {
        try {

            final XFileProtocol.File reqFile = XFileProtocol.File.parseFrom(bodyData);
            String fullPath = currentTask.getPath();
            byte[] fileData = reqFile.getData().toByteArray();
            RandomAccessFile randomAccessFile = new RandomAccessFile(fullPath, "rw");
            randomAccessFile.seek(reqFile.getPosition());
            randomAccessFile.write(fileData);
            randomAccessFile.close();
            cancelTimeout();

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
                rspTFile.setPath(currentTask.getPath());
                Packetlistener packetlistener = new Packetlistener() {
                    @Override
                    public void onSuccess(short service, Object response) {

                    }

                    @Override
                    public void onFaild() {
                        logger.e("*****ReceiveFileFailed");
                        if (!isCancel) {
                            rspTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                            triggerEvent(rspTFile);
                            fileDao.completeTransmit(rspTFile.getTaskId(), rspTFile.getPosition(), 2);
                        }
                    }

                    @Override
                    public void onTimeout() {
                        logger.e("*****ReceiveFileOnTimeout");
                        if (!isCancel) {
                            rspTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                            triggerEvent(rspTFile);
                            fileDao.completeTransmit(rspTFile.getTaskId(), rspTFile.getPosition(), 2);
                        }
                    }
                };

                rspTFile.setFileEvent(FileEvent.SET_FILE);
                writeIndex = responseFile.getPosition();
                currentTask.setPosition(responseFile.getPosition());
                fileDao.completeTransmit(responseFile.getTaskId(), responseFile.getPosition(), 0);
                long tempPercent = writeIndex * 100 / reqFile.getLength();

                if (writePercent < tempPercent) {
                    writePercent = tempPercent;
                    triggerEvent(rspTFile);
                }

                short sid;
                if (reqFile.getPosition() + fileData.length == reqFile.getLength()) {
                    // 文件发送完成,提醒接收端结束状态
                    writePercent = 0;
                    writeIndex = 0;
                    isTransmit = false;
                    isCancel = false;
                    rspTFile.setFileEvent(FileEvent.SET_FILE_SUCCESS);
                    fileDao.completeTransmit(responseFile.getTaskId(), responseFile.getPosition(), 1);
                    transferDetailDao.addTotalSize(responseFile.getLength());
                    triggerEvent(rspTFile);
                    reset();
                    ThreadPoolManager.getInstance(TaskInstance.class.getName()).startTaskThread(new Runnable() {
                        @Override
                        public void run() {
                            IMFileManager.getInstance().removeTask(getTaskId());
                        }
                    });
                }

                if (isCancel && (reqFile.getPosition() + fileData.length < reqFile.getLength())) {
                    // 暂停操作
                    sid = SysConstant.SERVICE_FILE_SET_STOP;
                    rspTFile.setFileEvent(FileEvent.SET_FILE_STOP);
                    isCancel = false;
                    isTransmit = false;
                    writePercent = 0;
                    writeIndex = 0;
                    triggerEvent(rspTFile);
                    ThreadPoolManager.getInstance(TaskInstance.class.getName()).startTaskThread(new Runnable() {
                        @Override
                        public void run() {
                            IMFileManager.getInstance().removeTask(getTaskId());
                        }
                    });
                } else {
                    sid = SysConstant.SERVICE_FILE_SET_SUCCESS;
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


    /**
     * 停止
     */
    public void stop() {
        isCancel = true;
    }

    public void cancel() {
        final XFileProtocol.File reqFile = XFileUtils.buildSFile(currentTask);
        currentTask.setFileEvent(FileEvent.CANCEL_FILE);
        triggerEvent(currentTask);
        reset();
        short sid = SysConstant.SERVICE_DEFAULT;
        short cid = SysConstant.CMD_FILE_CANCEL;
        if (XFileApp.mLinkType == LinkType.CLIENT) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, reqFile);
        } else if (XFileApp.mLinkType == LinkType.SERVER) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, reqFile);
        }
    }

    public void dispatchCancel() {
        currentTask.setFileEvent(FileEvent.CANCEL_FILE);
        triggerEvent(currentTask);
        reset();
    }


    /**
     * 重置发送任务
     */
    public void reset() {
        readIndex = 0;
        readPercent = 0;
        writeIndex = 0;
        writePercent = 0;
        currentTask = null;
        isTransmit = false;
        isCancel = false;
    }

    /**
     * 等待接收
     */
    public void waitReceive() {
        isTransmit = true;
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                isTransmit = false;
                currentTask.setFileEvent(FileEvent.SET_FILE_FAILED);
                triggerEvent(currentTask);
            }
        };
        timer.schedule(timerTask, 8000);

    }

    /**
     * 取消等待
     */
    public void cancelTimeout() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }


    /**
     * 通知界面
     */
    public void triggerEvent(TFileInfo tFileInfo) {
        EventBus.getDefault().post(tFileInfo);
    }

    /**
     * 是否正在传输
     */
    public boolean isTransmit() {
        return isTransmit;
    }

    public String getTaskId() {
        return currentTask == null ? "" : currentTask.getTaskId();
    }

    public void setCurrentTask(TFileInfo currentTask) {
        this.currentTask = currentTask;
    }

    public long getCreateTime() {
        return createTime;
    }


}
