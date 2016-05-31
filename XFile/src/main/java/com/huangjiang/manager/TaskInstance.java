package com.huangjiang.manager;

import com.google.protobuf.ByteString;
import com.huangjiang.XFileApp;
import com.huangjiang.business.model.LinkType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.config.SysConstant;
import com.huangjiang.core.ThreadPoolManager;
import com.huangjiang.dao.DaoMaster;
import com.huangjiang.dao.TFileDao;
import com.huangjiang.dao.TransferDetailDao;
import com.huangjiang.manager.callback.Packetlistener;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.StringUtils;
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
    private TFileDao fileDao;

    /**
     * 累计传输
     */
    private TransferDetailDao transferDetailDao;


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


    public TaskInstance(TFileInfo task) {
        fileDao = DaoMaster.getInstance().newSession().getFileDao();
        transferDetailDao = DaoMaster.getInstance().newSession().getTransferDetailDao();
        createTime = System.currentTimeMillis();
        currentTask = task.newInstance();
    }

    /**
     * 传输/续传
     */
    public void transmit() {
        final XFileProtocol.File reqFile = XFileUtils.parseProtocol(currentTask);
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
        try {

            byte[] readBytes;
            long remain = currentTask.getLength() - reqFile.getPosition();
            // 判断传回来的position是否等于文件length，相等的情况下，当作已经传输完成
            if (remain == 0) {
                currentTask.setFileEvent(FileEvent.SET_FILE_SUCCESS);
                triggerEvent(currentTask);
                fileDao.updateTransmit(currentTask.getTaskId(), reqFile.getPosition(), 1);
                transferDetailDao.addTotalSize(currentTask.getLength());
                final String taskId = getTaskId();
                ThreadPoolManager.getInstance(TaskInstance.class.getName()).startTaskThread(new Runnable() {
                    @Override
                    public void run() {
                        IMFileManager.getInstance().removeTask(taskId);
                        IMFileManager.getInstance().checkUndone();
                    }
                });
                reset();
                return;
            }
            // 继续传输未完成任务
            if (remain >= SysConstant.FILE_SEGMENT_SIZE) {
                readBytes = new byte[SysConstant.FILE_SEGMENT_SIZE];
            } else {
                readBytes = new byte[(int) remain];
            }
            RandomAccessFile randomAccessFile = new RandomAccessFile(currentTask.getPath(), "r");
            randomAccessFile.seek(reqFile.getPosition());
            randomAccessFile.read(readBytes);
            ByteString byteString = ByteString.copyFrom(readBytes);
            XFileProtocol.File.Builder builder = XFileProtocol.File.newBuilder();
            builder.setTaskId(currentTask.getTaskId());
            builder.setData(byteString);
            builder.setPosition(reqFile.getPosition());
            XFileProtocol.File responseFile = builder.build();


            Packetlistener packetlistener = new Packetlistener() {
                @Override
                public void onSuccess(short serviceId, Object response) {
                    if (response == null) {
                        currentTask.setFileEvent(FileEvent.SET_FILE_FAILED);
                        triggerEvent(currentTask);
                        return;
                    }

                    try {
                        byte[] rsp = (byte[]) response;
                        XFileProtocol.File rspFile = XFileProtocol.File.parseFrom(rsp);
                        cancelTimeout();
                        fileDao.updateTransmit(currentTask.getTaskId(), rspFile.getPosition(), 0);
                        currentTask.setFileEvent(FileEvent.SET_FILE);
                        readIndex = rspFile.getPosition();
                        currentTask.setPosition(readIndex);
                        long temPercent = readIndex * 100 / currentTask.getLength();
                        if (readPercent < temPercent) {
                            readPercent = temPercent;
                            triggerEvent(currentTask);
                        }
                        // 收到暂停标记
                        if (serviceId == SysConstant.SERVICE_FILE_SET_STOP) {
                            currentTask.setFileEvent(FileEvent.SET_FILE_STOP);
                            triggerEvent(currentTask);
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
                        currentTask.setFileEvent(FileEvent.SET_FILE_FAILED);
                        triggerEvent(currentTask);
                        fileDao.updateTransmit(reqFile.getTaskId(), reqFile.getPosition(), 2);
                    }
                }

                @Override
                public void onTimeout() {
                    logger.e("****transferFileOnTimeout");
                    if (!isCancel) {
                        currentTask.setFileEvent(FileEvent.SET_FILE_FAILED);
                        triggerEvent(currentTask);
                        fileDao.updateTransmit(reqFile.getTaskId(), reqFile.getPosition(), 2);
                    }
                }
            };

            if (XFileApp.mLinkType == LinkType.CLIENT) {
                IMClientFileManager.getInstance().sendMessage(sid, cid, responseFile, packetlistener, (short) 0);
            } else if (XFileApp.mLinkType == LinkType.SERVER) {
                IMServerFileManager.getInstance().sendMessage(sid, cid, responseFile, packetlistener, (short) 0);
            }

        } catch (Exception e) {
            currentTask.setFileEvent(FileEvent.SET_FILE_FAILED);
            triggerEvent(currentTask);
            e.printStackTrace();
            logger.e("****transferFile Exception:" + e.getMessage());
        }

    }


    /**
     * 接收保存文件
     */
    public void dispatchReceiveData(Header header, byte[] bodyData) {
        try {

            final XFileProtocol.File reqFile = XFileProtocol.File.parseFrom(bodyData);
            byte[] fileData = reqFile.getData().toByteArray();
            RandomAccessFile randomAccessFile = new RandomAccessFile(currentTask.getPath(), "rw");
            randomAccessFile.seek(reqFile.getPosition());
            randomAccessFile.write(fileData);
            randomAccessFile.close();
            cancelTimeout();

            if (reqFile.getPosition() + fileData.length <= currentTask.getLength()) {
                XFileProtocol.File.Builder builder = XFileProtocol.File.newBuilder();
                builder.setTaskId(currentTask.getTaskId());
                builder.setPosition(reqFile.getPosition() + ((long) fileData.length));
                XFileProtocol.File responseFile = builder.build();

                currentTask.setFileEvent(FileEvent.SET_FILE);
                writeIndex = responseFile.getPosition();
                currentTask.setPosition(responseFile.getPosition());
                fileDao.updateTransmit(currentTask.getTaskId(), currentTask.getPosition(), 0);
                long tempPercent = writeIndex * 100 / currentTask.getLength();

                if (writePercent < tempPercent) {
                    writePercent = tempPercent;
                    triggerEvent(currentTask);
                }

                short sid;
                if (reqFile.getPosition() + fileData.length == currentTask.getLength()) {
                    // 文件发送完成,提醒接收端结束状态
                    writePercent = 0;
                    writeIndex = 0;
                    isTransmit = false;
                    isCancel = false;
                    currentTask.setFileEvent(FileEvent.SET_FILE_SUCCESS);
                    if (!StringUtils.isEmpty(currentTask.getExtension())) {
                        String newFilePath = XFileUtils.rename(currentTask.getPath(), currentTask.getExtension());
                        if (!StringUtils.isEmpty(newFilePath)) {
                            currentTask.setPath(newFilePath);
                            fileDao.updatePath(currentTask.getTaskId(), currentTask.getPath());
                        }
                    }
                    fileDao.updateTransmit(currentTask.getTaskId(), currentTask.getPosition(), 1);
                    transferDetailDao.addTotalSize(currentTask.getLength());
                    triggerEvent(currentTask);
                    final String taskId = getTaskId();
                    ThreadPoolManager.getInstance(TaskInstance.class.getName()).startTaskThread(new Runnable() {
                        @Override
                        public void run() {
                            IMFileManager.getInstance().removeTask(taskId);
                        }
                    });
                    reset();
                }

                if (isCancel && (reqFile.getPosition() + fileData.length < currentTask.getLength())) {
                    // 暂停操作
                    sid = SysConstant.SERVICE_FILE_SET_STOP;
                    currentTask.setFileEvent(FileEvent.SET_FILE_STOP);
                    isCancel = false;
                    isTransmit = false;
                    writePercent = 0;
                    writeIndex = 0;
                    triggerEvent(currentTask);
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

                Packetlistener packetlistener = new Packetlistener() {
                    @Override
                    public void onSuccess(short service, Object response) {

                    }

                    @Override
                    public void onFaild() {
                        logger.e("*****ReceiveFileFailed");
                        if (!isCancel) {
                            currentTask.setFileEvent(FileEvent.SET_FILE_FAILED);
                            triggerEvent(currentTask);
                            fileDao.updateTransmit(currentTask.getTaskId(), currentTask.getPosition(), 2);
                        }
                    }

                    @Override
                    public void onTimeout() {
                        logger.e("*****ReceiveFileOnTimeout");
                        if (!isCancel) {
                            currentTask.setFileEvent(FileEvent.SET_FILE_FAILED);
                            triggerEvent(currentTask);
                            fileDao.updateTransmit(currentTask.getTaskId(), currentTask.getPosition(), 2);
                        }
                    }
                };

                if (XFileApp.mLinkType == LinkType.CLIENT) {
                    IMClientFileManager.getInstance().sendMessage(sid, cid, responseFile, packetlistener, header.getSeqnum());
                } else if (XFileApp.mLinkType == LinkType.SERVER) {
                    IMServerFileManager.getInstance().sendMessage(sid, cid, responseFile, packetlistener, header.getSeqnum());
                }
            }

        } catch (Exception e) {
            currentTask.setFileEvent(FileEvent.SET_FILE_FAILED);
            triggerEvent(currentTask);
            e.printStackTrace();
            logger.e("****dispatchReceiveData Exception:" + e.getMessage());
        }
    }


    /**
     * 停止
     */
    public void stop() {
        isCancel = true;
    }

    public void cancel() {
        XFileProtocol.File.Builder builder = XFileProtocol.File.newBuilder();
        builder.setTaskId(currentTask.getTaskId());
        final XFileProtocol.File reqFile = builder.build();
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
        timer.schedule(timerTask, 1000 * 16);

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

    public long getCreateTime() {
        return createTime;
    }


}
