package com.huangjiang.manager;

import android.os.Build;

import com.google.protobuf.ByteString;
import com.huangjiang.XFileApplication;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.config.SysConstant;
import com.huangjiang.dao.DFile;
import com.huangjiang.dao.DFileDao;
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
import java.util.LinkedList;

/**
 * 文件管理
 */
public class IMFileManager extends IMBaseManager {

    private Logger logger = Logger.getLogger(IMFileManager.class);

    // 发送端记录位置
    private long readIndex;
    private long readPercent;

    // 接收端记录位置
    private long writeIndex;
    private long writePercent;
    // 是否正在传输
    private boolean isTransmit = false;
    // 取消/暂停发送
    private boolean isCancel = false;

    /**
     * 任务列表,记录尚未发送的任务
     */
    private LinkedList<TFileInfo> taskQueue = new LinkedList<>();

    private DFileDao fileDao;

    private TFileInfo currentTask = null;

    private static IMFileManager inst = null;

    public static IMFileManager getInstance() {
        if (inst == null) {
            inst = new IMFileManager();
        }
        return inst;
    }

    IMFileManager() {
        fileDao = DaoMaster.getInstance().newSession().getDFileDao();
    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    /**
     * 消息分发
     */
    public void dispatchMessage(Header header, byte[] bodyData) {
        switch (header.getCommandId()) {
            case SysConstant.CMD_FILE_NEW:
                // 新建接收文件任务
                dispatchCreateTask(header, bodyData);
                break;
            case SysConstant.CMD_TASK_CHECK:
                // 检查任务是否存在/任务存在进入文件传输
                dispatchCheckTask(header, bodyData);
                break;
            case SysConstant.CMD_FILE_SET:
                // 保存文件/断点续传
                dispatchReceiveData(header, bodyData);
                break;
            case SysConstant.CMD_FILE_RESUME:
                // 断点续传
                dispatchResume(bodyData);
                break;
            case SysConstant.CMD_FILE_CANCEL:
                dispatchCancel(bodyData);
                break;
        }
    }

    /**
     * 创建发送文件任务
     */
    public void createTask(final TFileInfo createNewFile) {
        short cid = SysConstant.CMD_FILE_NEW;
        short sid = SysConstant.SERVICE_DEFAULT;
        createNewFile.setTaskId(XFileUtils.buildTaskId());
        createNewFile.setFrom(Build.MODEL);
        createNewFile.setIsSend(true);
        XFileProtocol.File reqFile = XFileUtils.buildSFile(createNewFile);
        Packetlistener packetlistener = new Packetlistener() {
            @Override
            public void onSuccess(short serviceId, Object response) {

                if (serviceId != SysConstant.SERVICE_FILE_NEW_SUCCESS || response == null) {
                    // 创建任务失败
                    createNewFile.setFileEvent(FileEvent.CREATE_FILE_FAILED);
                    triggerEvent(createNewFile);
                    return;
                }
                try {
                    byte[] rsp = (byte[]) response;
                    XFileProtocol.File rspFile = XFileProtocol.File.parseFrom(rsp);
                    createNewFile.setFileEvent(FileEvent.CREATE_FILE_SUCCESS);
                    createNewFile.setFrom(rspFile.getFrom());

                    // 保存数据库,记录传输状态
                    DFile dbFile = XFileUtils.buildDFile(createNewFile);
                    fileDao.insert(dbFile);

                    triggerEvent(createNewFile);
                    logger.e("****createTaskSuccess");

                    checkTask(createNewFile);


                } catch (Exception e) {
                    e.printStackTrace();
                    logger.e(e.getMessage());
                }

            }

            @Override
            public void onFaild() {
                createNewFile.setFileEvent(FileEvent.CREATE_FILE_FAILED);
                triggerEvent(createNewFile);
            }

            @Override
            public void onTimeout() {
                createNewFile.setFileEvent(FileEvent.CREATE_FILE_FAILED);
                triggerEvent(createNewFile);
            }
        };
        if (XFileApplication.connect_type == 1) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, reqFile, packetlistener, (short) 0);
        } else if (XFileApplication.connect_type == 2) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, reqFile, packetlistener, (short) 0);
        }
        logger.e("****createTaskSend");

    }

    /**
     * 处理创建任务
     */
    private void dispatchCreateTask(Header header, byte[] bodyData) {
        try {
            XFileProtocol.File reqFile = XFileProtocol.File.parseFrom(bodyData);
            // 判断是否存在文件,如果不存在则创建
            String fullPath = XFileUtils.getSavePathByExtension(reqFile.getExtension()) + reqFile.getFullName();
            File saveFile = new File(fullPath);
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            TFileInfo reqTFile = XFileUtils.buildTFile(reqFile);
            // 设置为接收文件
            reqTFile.setIsSend(false);
            // 创建成功
            reqTFile.setFileEvent(FileEvent.CREATE_FILE_SUCCESS);
            // 保存数据库
            DFile dbFile = XFileUtils.buildDFile(reqTFile);
            // 本地保存文件路径
            dbFile.setSavePath(fullPath);
            fileDao.insert(dbFile);
            // 答复-本机机型
            XFileProtocol.File.Builder rspFile = reqFile.toBuilder();
            rspFile.setFrom(Build.MODEL);
            rspFile.setIsSend(false);

            short sid = SysConstant.SERVICE_FILE_NEW_SUCCESS;
            short cid = SysConstant.CMD_FILE_NEW_RSP;

            if (XFileApplication.connect_type == 1) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, rspFile.build(), header.getSeqnum());
            } else {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, rspFile.build(), null, header.getSeqnum());
            }

            triggerEvent(reqTFile);


        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    /**
     * 检查接收方任务是否存在
     */
    public void checkTask(final TFileInfo checkFile) {
        short cid = SysConstant.CMD_TASK_CHECK;
        short sid = SysConstant.SERVICE_DEFAULT;
        final XFileProtocol.File reqFile = XFileUtils.buildSFile(checkFile);
        final TFileInfo reqTFile = XFileUtils.buildTFile(reqFile);
        Packetlistener packetlistener = new Packetlistener() {
            @Override
            public void onSuccess(short serviceId, Object response) {
                // 传输任务不合法
                if (serviceId != SysConstant.SERVICE_TASK_CHECK_SUCCESS || response == null) {
                    checkFile.setFileEvent(FileEvent.CHECK_TASK_FAILED);
                    triggerEvent(checkFile);
                    return;
                }
                try {
                    checkFile.setFileEvent(FileEvent.CHECK_TASK_SUCCESS);
                    triggerEvent(checkFile);
                    logger.e("***checkTaskSuccess");
                    if (!isTransmit) {
                        // 开始发送
                        isTransmit = true;
                        isCancel = false;
                        readIndex = 0;
                        readPercent = 0;
                        currentTask = reqTFile;
                        transferFile(reqFile);

                    } else {
                        // 创建文件成功,检查是否有传输任务,如果正在传输,添加缓存列表
                        taskQueue.addLast(checkFile);
                        // 任务等待发送
                        checkFile.setFileEvent(FileEvent.WAITING);
                        triggerEvent(checkFile);
                        logger.e("****createTaskSuccess");
                    }


                } catch (Exception e) {
                    checkFile.setFileEvent(FileEvent.CHECK_TASK_FAILED);
                    triggerEvent(checkFile);
                    e.printStackTrace();
                    logger.e(e.getMessage());
                }

            }

            @Override
            public void onFaild() {
                checkFile.setFileEvent(FileEvent.CHECK_TASK_FAILED);
                triggerEvent(checkFile);
            }

            @Override
            public void onTimeout() {
                checkFile.setFileEvent(FileEvent.CHECK_TASK_FAILED);
                triggerEvent(checkFile);
            }
        };
        if (XFileApplication.connect_type == 1) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, reqFile, packetlistener, (short) 0);
        } else if (XFileApplication.connect_type == 2) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, reqFile, packetlistener, (short) 0);
        }
        logger.e("****checkTaskSend");
    }

    /**
     * 处理检查任务合法性
     */
    private void dispatchCheckTask(Header header, byte[] bodyData) {
        try {
            XFileProtocol.File requestFile = XFileProtocol.File.parseFrom(bodyData);
            TFileInfo reqTFile = XFileUtils.buildTFile(requestFile);
            short sid;
            short cid = SysConstant.CMD_TASK_CHECK_RSP;

            // 判断本地是否有这个taskId,找不到这个taskId返回失败信息,停止传送/续传
            if (fileDao.getDFileByTaskId(requestFile.getTaskId()) != null) {
                sid = SysConstant.SERVICE_TASK_CHECK_SUCCESS;
                if (!isTransmit) {
                    isTransmit = true;
                    currentTask = reqTFile;
                } else {
                    // 正在传送,等待
                    reqTFile.setFileEvent(FileEvent.WAITING);
                    // 加入任务列表
                    taskQueue.addLast(reqTFile);
                    triggerEvent(reqTFile);
                }
            } else {
                sid = SysConstant.SERVICE_TASK_CHECK_FAILED;
            }

            // 答复发送端创建成功
            if (XFileApplication.connect_type == 1) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, requestFile, header.getSeqnum());
            } else {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, requestFile, null, header.getSeqnum());
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
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
                // 移除发送任务
                removeTask(reqTFile);
                // 保存数据库,传送完成
                DFile dFile = XFileUtils.buildDFile(reqTFile);
                fileDao.updateTransferStatus(dFile);
                // 重置标记
                readPercent = 0;
                readIndex = 0;
                isTransmit = false;
                triggerEvent(reqTFile);
                // 检查当前是否还有等待发送的任务
                checkUndone();
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
                        // 保存数据库传输位置
                        DFile dbFile = XFileUtils.buildDFile(rspTFile);
                        fileDao.updateTransferStatus(dbFile);
                        // 收到暂停标记
                        if (serviceId == SysConstant.SERVICE_FILE_SET_STOP) {
                            // 移除发送任务
                            removeTask(reqTFile);
                            rspTFile.setFileEvent(FileEvent.SET_FILE_STOP);
                            rspTFile.setPercent(readPercent);
                            triggerEvent(rspTFile);
                            // 对方暂停接收文件
                            readIndex = 0;
                            readPercent = 0;
                            isCancel = true;
                            isTransmit = false;
                            currentTask = null;
                            // 检查当前是否还有等待发送的任务
                            checkUndone();

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
            if (XFileApplication.connect_type == 1) {
                IMClientFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, (short) 0);
            } else if (XFileApplication.connect_type == 2) {
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
            String fullPath = XFileUtils.getSavePathByExtension(reqFile.getExtension()) + File.separator + reqFile.getFullName();
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
                writeIndex = reqFile.getPosition();
                long tempPercent = writeIndex * 100 / reqFile.getLength();

                logger.e("****writePercent:" + writePercent + ",tempPercent" + tempPercent);
                if (writePercent < tempPercent) {
                    writePercent = tempPercent;
                    // 更新接收状态
                    rspTFile.setFileEvent(FileEvent.SET_FILE);
                    rspTFile.setPercent(writePercent);
                    // 保存数据库，记录发送到哪了
                    DFile dbFile = XFileUtils.buildDFile(rspTFile);
                    fileDao.updateTransferStatus(dbFile);
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
                    // 保存数据库，记录发送到哪了
                    DFile dbFile = XFileUtils.buildDFile(rspTFile);
                    fileDao.updateTransferStatus(dbFile);
                    // 移除接收任务
                    removeTask(rspTFile);
                    triggerEvent(rspTFile);

                }
                if (isCancel) {
                    sid = SysConstant.SERVICE_FILE_SET_STOP;
                    // 暂停操作
                    rspTFile.setFileEvent(FileEvent.SET_FILE_STOP);
                    rspTFile.setPercent(writePercent);
                    isCancel = false;
                    isTransmit = false;
                    currentTask = null;
                    writePercent = 0;
                    writeIndex = 0;
                    // 移除接收任务
                    removeTask(rspTFile);
                    triggerEvent(rspTFile);
                } else {
                    sid = SysConstant.SERVICE_FILE_SET_SUCCESS;
                }

                short cid = SysConstant.CMD_FILE_SET_RSP;
                if (XFileApplication.connect_type == 1) {
                    IMClientFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, header.getSeqnum());
                } else if (XFileApplication.connect_type == 2) {
                    IMServerFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, header.getSeqnum());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }


    /**
     * 处理断点续传
     */
    private void resume(final XFileProtocol.File requestFile) {
        // 重新定位写入文件标记
        writeIndex = requestFile.getPosition();
        writePercent = writeIndex * 100 / requestFile.getLength();
        short sid = SysConstant.SERVICE_DEFAULT;
        short cid = SysConstant.CMD_FILE_RESUME;
        if (XFileApplication.connect_type == 1) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, requestFile, null, (short) 0);
        } else if (XFileApplication.connect_type == 2) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, requestFile, null, (short) 0);
        }

    }

    /**
     * 处理断点续传
     */
    private void dispatchResume(byte[] bodyData) {
        try {
            final XFileProtocol.File requestFile = XFileProtocol.File.parseFrom(bodyData);
            final TFileInfo reqTFile = XFileUtils.buildTFile(requestFile);
            checkTask(reqTFile);

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    /**
     * 取消任务1取消当前任务，2取消等待任务
     */
    public void cancelTask(final TFileInfo tFileInfo) {

        final XFileProtocol.File requestFile = XFileUtils.buildSFile(tFileInfo);

        if (isTransmit && currentTask != null && currentTask.getTaskId().equals(tFileInfo.getTaskId())) {
            //
            if (!tFileInfo.isSend()) {
                // 如果是接收方
                resetRead();
            }
        } else {
            // 移除任务列表
            removeTask(tFileInfo);
        }
        // 数据库删除
        if (tFileInfo != null) {
            fileDao.deleteByTaskId(tFileInfo.getTaskId());
            tFileInfo.setFileEvent(FileEvent.CANCEL_FILE);
            triggerEvent(tFileInfo);
        }
        // 删除的是当前正在传送的任务
        short sid = SysConstant.SERVICE_DEFAULT;
        short cid = SysConstant.CMD_FILE_CANCEL;
        if (XFileApplication.connect_type == 1) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, requestFile, null, (short) 0);
        } else if (XFileApplication.connect_type == 2) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, requestFile, null, (short) 0);
        }


    }

    /**
     * 处理取消命令,1取消当前发送的任务,2取消等待发送的任务
     */
    private void dispatchCancel(byte[] bodyData) {
        try {

            final XFileProtocol.File requestFile = XFileProtocol.File.parseFrom(bodyData);
            final TFileInfo reqTFile = XFileUtils.buildTFile(requestFile);
            reqTFile.setIsSend(!reqTFile.isSend());
            if (isTransmit && currentTask != null && reqTFile.getTaskId().equals(currentTask.getTaskId())) {
                // 删除当前任务
                if (reqTFile.isSend()) {
                    // 发送方收到,取消任务
                    resetWrite();
                    // 检查是否还有任务尚未完成
                    checkUndone();
                } else {
                    // 接收方收到取消标记,等待下一次传输过来再执行暂停任务
                    isCancel = true;
                }
                logger.e("****dispatchCancel");

            } else {
                // 删除等待的任务
                removeTask(reqTFile);
            }
            // 删除数据库保存记录
            fileDao.deleteByTaskId(reqTFile.getTaskId());
            reqTFile.setFileEvent(FileEvent.CANCEL_FILE);
            triggerEvent(reqTFile);

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }


    /**
     * 暂停接收数据
     */
    public void stopReceive() {
        if (isTransmit && !isCancel) {
            isCancel = true;
        }
    }

    /**
     * 继续接收数据/短点续传
     */
    public void resumeReceive(TFileInfo tFileInfo) {
        if (!isTransmit) {
            XFileProtocol.File reqFile = XFileUtils.buildSFile(tFileInfo);
            resume(reqFile);
        } else {
            checkTask(tFileInfo);
        }
    }

    /**
     * 通知界面
     */
    private void triggerEvent(TFileInfo tFileInfo) {
        EventBus.getDefault().post(tFileInfo);
    }


    /**
     * 移除等待任务
     */
    private void removeTask(TFileInfo task) {
        for (int i = 0; i < taskQueue.size(); i++) {
            TFileInfo currentTask = taskQueue.get(i);
            if (currentTask.getTaskId().equals(task.getTaskId())) {
                taskQueue.remove(i);
                break;
            }
        }
    }


    /**
     * 检查是否还有未完成的任务
     */
    private void checkUndone() {
        if (taskQueue != null && taskQueue.size() > 0) {
            TFileInfo task = taskQueue.pop();
            checkTask(task);
        }
    }

    private void resetRead() {
        readIndex = 0;
        readPercent = 0;
        currentTask = null;
        isTransmit = false;
        isCancel = true;
    }

    private void resetWrite() {
        writeIndex = 0;
        writePercent = 0;
        currentTask = null;
        isTransmit = false;
        isCancel = true;

    }


}
