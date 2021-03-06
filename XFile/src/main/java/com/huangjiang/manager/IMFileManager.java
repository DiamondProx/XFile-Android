package com.huangjiang.manager;

import android.os.Build;

import com.huangjiang.XFileApp;
import com.huangjiang.business.event.DiskEvent;
import com.huangjiang.business.model.LinkType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.config.Config;
import com.huangjiang.config.SysConstant;
import com.huangjiang.dao.DaoMaster;
import com.huangjiang.dao.TFileDao;
import com.huangjiang.manager.callback.Packetlistener;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.SoundHelper;
import com.huangjiang.utils.StringUtils;
import com.huangjiang.utils.VibratorUtils;
import com.huangjiang.utils.XFileUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Hashtable;
import java.util.Map;

/**
 * 文件管理
 */
public class IMFileManager extends IMBaseManager {

    private Logger logger = Logger.getLogger(IMFileManager.class);

    /**
     * 任务列表,记录尚未发送的任务
     */
    private Hashtable<String, TaskInstance> taskTable = new Hashtable<>();

    private TFileDao fileDao;

    private static IMFileManager inst = null;

    public static IMFileManager getInstance() {
        if (inst == null) {
            inst = new IMFileManager();
        }
        return inst;
    }

    IMFileManager() {
        fileDao = DaoMaster.getInstance().newSession().getFileDao();
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
                // 取消操作
                dispatchCancel(bodyData);
                break;
        }
    }

    /**
     * 创建发送文件任务
     */
    public void createTask(final TFileInfo task) {
        short cid = SysConstant.CMD_FILE_NEW;
        short sid = SysConstant.SERVICE_DEFAULT;
        final TFileInfo createNewFile = task.newInstance();
        createNewFile.setTaskId(XFileUtils.buildTaskId());
        createNewFile.setFrom(Build.MODEL);
        createNewFile.setIsSend(true);
        XFileProtocol.File reqFile = XFileUtils.parseProtocol(createNewFile);
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
                    fileDao.insert(createNewFile);

                    triggerEvent(createNewFile);

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
        if (XFileApp.mLinkType == LinkType.CLIENT) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, reqFile, packetlistener, (short) 0);
        } else if (XFileApp.mLinkType == LinkType.SERVER) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, reqFile, packetlistener, (short) 0);
        }
        logger.e("****createTask");

    }

    /**
     * 处理创建任务
     */
    private void dispatchCreateTask(Header header, byte[] bodyData) {
        try {
            XFileProtocol.File reqFile = XFileProtocol.File.parseFrom(bodyData);
            boolean createFile = true;
            File saveFile = null;
            // 判断磁盘空间是否足够
            long remainSpace = XFileUtils.getSDFreeSize() - reqFile.getLength();
            // 保留20M磁盘空间
            if (remainSpace <= 1024 * 1024 * 20) {
                // 磁盘空间不足
                createFile = false;
                DiskEvent diskEvent = new DiskEvent(DiskEvent.DiskState.ENOUGH);
                EventBus.getDefault().post(diskEvent);
            } else {
                // 根据后缀名生成路径
                String prePath = XFileUtils.getSavePathByExtension(reqFile.getExtension());
                String fullPath = prePath + reqFile.getName();
                saveFile = new File(fullPath);
                if (!saveFile.getParentFile().exists()) {
                    createFile = saveFile.getParentFile().mkdirs();
                }

                int i = 0;
                while (saveFile.exists()) {
                    i++;
                    saveFile = new File(prePath + reqFile.getName() + "-" + i);
                }
                if (createFile && !saveFile.exists()) {
                    createFile = saveFile.createNewFile();
                }
            }

            TFileInfo reqTFile = XFileUtils.parseTFile(reqFile);
            // 设置为接收文件
            reqTFile.setIsSend(false);

            if (createFile) {
                // 创建成功
                reqTFile.setFileEvent(FileEvent.CREATE_FILE_SUCCESS);
                // 本地保存文件路径-临时文件
                reqTFile.setPath(saveFile.getAbsolutePath());
                fileDao.insert(reqTFile);
            }

            // 答复-本机机型
            XFileProtocol.File.Builder rspFile = XFileProtocol.File.newBuilder();
            rspFile.setTaskId(reqFile.getTaskId());
            rspFile.setFrom(Build.MODEL);

            short sid = createFile ? SysConstant.SERVICE_FILE_NEW_SUCCESS : SysConstant.SERVICE_FILE_NEW_FAILED;
            short cid = SysConstant.CMD_FILE_NEW_RSP;

            if (createFile) {
                if (Config.getSound()) {
                    SoundHelper.playReceiveFile();
                }
                if (Config.getVibration()) {
                    VibratorUtils.Vibrate();
                }
                triggerEvent(reqTFile);
            }

            if (XFileApp.mLinkType == LinkType.CLIENT) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, rspFile.build(), header.getSeqnum());
            } else if (XFileApp.mLinkType == LinkType.SERVER) {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, rspFile.build(), null, header.getSeqnum());
            }


        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
        logger.e("****dispatchCreateTask");
    }

    /**
     * 检查接收方任务是否存在
     */
    public void checkTask(final TFileInfo task) {
        short cid = SysConstant.CMD_TASK_CHECK;
        short sid = SysConstant.SERVICE_DEFAULT;
        final TFileInfo checkFile = task.newInstance();
        final XFileProtocol.File reqFile = XFileUtils.parseProtocol(checkFile);
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

                    TaskInstance taskInstance = taskTable.get(checkFile.getTaskId());
                    if (taskInstance == null) {
                        taskInstance = new TaskInstance(checkFile);
                        TFileInfo dbFile = fileDao.getTFileByTaskId(checkFile.getTaskId());
                        if (dbFile != null) {
                            checkFile.setPath(dbFile.getPath());
                        }
                        putTask(taskInstance);
                    }
                    if (!isTransmit()) {
                        taskInstance.transmit();
                    } else {
                        checkFile.setFileEvent(FileEvent.WAITING);
                        triggerEvent(checkFile);
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
        if (XFileApp.mLinkType == LinkType.CLIENT) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, reqFile, packetlistener, (short) 0);
        } else if (XFileApp.mLinkType == LinkType.SERVER) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, reqFile, packetlistener, (short) 0);
        }
        logger.e("****checkTask");
    }

    /**
     * 处理检查任务合法性
     */
    private void dispatchCheckTask(Header header, byte[] bodyData) {
        try {
            XFileProtocol.File reqFile = XFileProtocol.File.parseFrom(bodyData);
            short sid;
            short cid = SysConstant.CMD_TASK_CHECK_RSP;
            TFileInfo dbFile = fileDao.getTFileByTaskId(reqFile.getTaskId());
            // 判断本地是否有这个taskId,找不到这个taskId返回失败信息,停止传送/续传
            if (dbFile != null) {
                dbFile.setFileEvent(FileEvent.CHECK_TASK_SUCCESS);
                triggerEvent(dbFile);

                TaskInstance taskInstance = taskTable.get(dbFile.getTaskId());
                if (taskInstance == null) {
                    taskInstance = new TaskInstance(dbFile);
                    putTask(taskInstance);
                }
                if (!isTransmit()) {
                    taskInstance.waitReceive();
                } else {
                    dbFile.setFileEvent(FileEvent.WAITING);
                    triggerEvent(dbFile);
                }

                sid = SysConstant.SERVICE_TASK_CHECK_SUCCESS;
            } else {
                sid = SysConstant.SERVICE_TASK_CHECK_FAILED;
            }

            XFileProtocol.File rspFile = XFileProtocol.File.newBuilder().setTaskId(reqFile.getTaskId()).build();
            // 答复发送端创建成功
            if (XFileApp.mLinkType == LinkType.CLIENT) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, rspFile, header.getSeqnum());
            } else if (XFileApp.mLinkType == LinkType.SERVER) {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, rspFile, null, header.getSeqnum());
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
        logger.e("****dispatchCheckTask");

    }

    /**
     * 接收保存文件
     */
    private void dispatchReceiveData(Header header, byte[] bodyData) {
        try {
            final XFileProtocol.File reqFile = XFileProtocol.File.parseFrom(bodyData);
            TaskInstance taskInstance = taskTable.get(reqFile.getTaskId());
            if (taskInstance != null) {
                taskInstance.dispatchReceiveData(header, bodyData);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }


    /**
     * 继续接收数据/短点续传
     */
    public void resumeReceive(TFileInfo tFileInfo) {
        TaskInstance taskInstance = taskTable.get(tFileInfo.getTaskId());
        if (taskInstance != null) {
            if (!isTransmit()) {
                // 校验成功
                taskInstance.transmit();
            } else {
                // 等待发送
                tFileInfo.setFileEvent(FileEvent.WAITING);
                triggerEvent(tFileInfo);
            }
        } else {
            // 尚未校验
            checkTask(tFileInfo);
        }
    }

    /**
     * 暂停
     */
    public void stopReceive(TFileInfo tFileInfo) {
        TaskInstance taskInstance = taskTable.get(tFileInfo.getTaskId());
        if (taskInstance != null) {
            taskInstance.stop();
        }
    }


    /**
     * 处理断点续传
     */
    private void dispatchResume(byte[] bodyData) {
        try {
            final XFileProtocol.File requestFile = XFileProtocol.File.parseFrom(bodyData);
            TaskInstance taskInstance = taskTable.get(requestFile.getTaskId());
            if (taskInstance != null) {
                taskInstance.transmit();
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    /**
     * 取消任务1取消当前任务，2取消等待任务
     */
    public void cancelTask(final TFileInfo tFileInfo) {
        TaskInstance taskInstance = taskTable.get(tFileInfo.getTaskId());
        if (taskInstance != null) {
            taskInstance.cancel();
            removeTask(tFileInfo.getTaskId());
        }
        // 删除数据库记录
        TFileInfo dFile = fileDao.getTFileByTaskId(tFileInfo.getTaskId());
        fileDao.deleteByTaskId(tFileInfo.getTaskId());
        // 删除缓存文件
        if (!dFile.isSend() && dFile.getStatus() != 1) {
            delCacheFile(dFile);
        }
    }

    /**
     * 处理取消命令,1取消当前发送的任务,2取消等待发送的任务
     */
    private void dispatchCancel(byte[] bodyData) {
        try {
            final XFileProtocol.File reqFile = XFileProtocol.File.parseFrom(bodyData);
            TaskInstance taskInstance = taskTable.get(reqFile.getTaskId());
            if (taskInstance != null) {
                taskInstance.dispatchCancel();
                removeTask(taskInstance.getTaskId());
            }
            // 删除数据库记录
            TFileInfo dFile = fileDao.getTFileByTaskId(reqFile.getTaskId());
            fileDao.deleteByTaskId(reqFile.getTaskId());
            // 删除缓存文件
            if (!dFile.isSend() && dFile.getStatus() != 1) {
                delCacheFile(dFile);
            }
            checkUndone();

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }


    /**
     * 移除等待任务
     */
    public void removeTask(String taskId) {
        if (taskTable.containsKey(taskId)) {
            taskTable.remove(taskId);
        }
    }


    /**
     * 检查是否还有未完成的任务
     */
    public void checkUndone() {
        if (!isTransmit()) {
            long createTime = 0;
            TaskInstance taskInstance = null;
            for (Map.Entry entry : taskTable.entrySet()) {
                TaskInstance val = (TaskInstance) entry.getValue();
                if ((createTime == 0 || createTime > val.getCreateTime()) && !StringUtils.isEmpty(val.getTaskId())) {
                    createTime = val.getCreateTime();
                    taskInstance = val;
                }
            }
            if (taskInstance != null) {
                taskInstance.transmit();
            }
        }
    }


    /**
     * 通知界面
     */
    private void triggerEvent(TFileInfo tFileInfo) {
        EventBus.getDefault().post(tFileInfo);
    }

    public boolean isTransmit() {
        for (Map.Entry entry : taskTable.entrySet()) {
            TaskInstance val = (TaskInstance) entry.getValue();
            if (val.isTransmit()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除本地缓存文件
     */
    public boolean delCacheFile(TFileInfo tFileInfo) {
        File cacheFile = new File(tFileInfo.getPath());
        return cacheFile.delete();
    }

    /**
     * 添加任务
     */
    private void putTask(TaskInstance taskInstance) {
        if (!taskTable.containsKey(taskInstance.getTaskId())) {
            taskTable.put(taskInstance.getTaskId(), taskInstance);
        }
    }


}
