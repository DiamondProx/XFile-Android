package com.huangjiang.manager;

import android.os.Build;

import com.huangjiang.XFileApp;
import com.huangjiang.business.event.DiskEvent;
import com.huangjiang.business.model.LinkType;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.config.Config;
import com.huangjiang.config.SysConstant;
import com.huangjiang.dao.DFile;
import com.huangjiang.dao.DFileDao;
import com.huangjiang.dao.DaoMaster;
import com.huangjiang.manager.callback.Packetlistener;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.SoundHelper;
import com.huangjiang.utils.VibratorUtils;
import com.huangjiang.utils.XFileUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.LinkedList;

/**
 * 文件管理
 */
public class IMFileManager extends IMBaseManager {

    private Logger logger = Logger.getLogger(IMFileManager.class);

    /**
     * 任务列表,记录尚未发送的任务
     */
    private LinkedList<TaskInstance> taskQueue = new LinkedList<>();

    private DFileDao fileDao;

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
                // 取消操作
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
        if (XFileApp.mLinkType == LinkType.CLIENT) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, reqFile, packetlistener, (short) 0);
        } else if (XFileApp.mLinkType == LinkType.SERVER) {
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

            TFileInfo reqTFile = XFileUtils.buildTFile(reqFile);
            // 设置为接收文件
            reqTFile.setIsSend(false);

            if (createFile) {
                // 创建成功
                reqTFile.setFileEvent(FileEvent.CREATE_FILE_SUCCESS);
                // 保存数据库
                DFile dbFile = XFileUtils.buildDFile(reqTFile);
                // 本地保存文件路径-临时文件
                dbFile.setSavePath(saveFile.getAbsolutePath());
                fileDao.insert(dbFile);
            }

            // 答复-本机机型
            XFileProtocol.File.Builder rspFile = reqFile.toBuilder();
            rspFile.setFrom(Build.MODEL);
            rspFile.setIsSend(false);

            short sid = createFile ? SysConstant.SERVICE_FILE_NEW_SUCCESS : SysConstant.SERVICE_FILE_NEW_FAILED;
            short cid = SysConstant.CMD_FILE_NEW_RSP;

            if (XFileApp.mLinkType == LinkType.CLIENT) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, rspFile.build(), header.getSeqnum());
            } else if (XFileApp.mLinkType == LinkType.SERVER) {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, rspFile.build(), null, header.getSeqnum());
            }
            if (createFile) {
                if (Config.getSound()) {
                    SoundHelper.playReceiveFile();
                }
                if (Config.getVibration()) {
                    VibratorUtils.Vibrate();
                }
                triggerEvent(reqTFile);
            }

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


                    // 加入任务列表
                    TaskInstance taskInstance = new TaskInstance();
                    taskInstance.setCurrentTask(reqTFile);
                    triggerEvent(reqTFile);
                    if (!containsTask(reqFile.getTaskId())) {
                        taskQueue.addLast(taskInstance);
                    }
                    if (!isTransmit()) {
                        taskInstance.transferFile(reqFile);
                    }
                    triggerEvent(checkFile);

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
        logger.e("****checkTaskSend");
    }

    /**
     * 处理检查任务合法性
     */
    private void dispatchCheckTask(Header header, byte[] bodyData) {
        try {
            XFileProtocol.File reqFile = XFileProtocol.File.parseFrom(bodyData);
            TFileInfo reqTFile = XFileUtils.buildTFile(reqFile);
            short sid;
            short cid = SysConstant.CMD_TASK_CHECK_RSP;

            // 判断本地是否有这个taskId,找不到这个taskId返回失败信息,停止传送/续传
            if (fileDao.getDFileByTaskId(reqFile.getTaskId()) != null) {
                sid = SysConstant.SERVICE_TASK_CHECK_SUCCESS;
                // 正在传送,等待
                reqTFile.setFileEvent(FileEvent.WAITING);
                reqTFile.setIsSend(!reqTFile.isSend());
                // 加入任务列表
                TaskInstance taskInstance = new TaskInstance();
                taskInstance.setCurrentTask(reqTFile);
                triggerEvent(reqTFile);
                if (!containsTask(reqFile.getTaskId())) {
                    taskQueue.addLast(taskInstance);
                }
                if (!isTransmit()) {
                    taskInstance.waitReceive();
                }
            } else {
                sid = SysConstant.SERVICE_TASK_CHECK_FAILED;
            }

            // 答复发送端创建成功
            if (XFileApp.mLinkType == LinkType.CLIENT) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, reqFile, header.getSeqnum());
            } else if (XFileApp.mLinkType == LinkType.SERVER) {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, reqFile, null, header.getSeqnum());
            }

        } catch (Exception e) {
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
            TaskInstance taskInstance = getTaskInstance(reqFile.getTaskId());
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
        TaskInstance taskInstance = getTaskInstance(tFileInfo.getTaskId());
        taskInstance.resume();
    }

    public void stopReceive(TFileInfo tFileInfo) {
        TaskInstance taskInstance = getTaskInstance(tFileInfo.getTaskId());
        taskInstance.stop();

    }


    /**
     * 处理断点续传
     */
    private void dispatchResume(byte[] bodyData) {
        try {
            final XFileProtocol.File requestFile = XFileProtocol.File.parseFrom(bodyData);
            TaskInstance taskInstance = getTaskInstance(requestFile.getTaskId());
            taskInstance.dispatchResume(requestFile);
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    /**
     * 取消任务1取消当前任务，2取消等待任务
     */
    public void cancelTask(final TFileInfo tFileInfo) {
        logger.e("****cancelTask1111");
        TaskInstance taskInstance = getTaskInstance(tFileInfo.getTaskId());
        if (taskInstance != null) {
            taskInstance.cancel();
        }
        // 删除数据库记录
        DFile dFile = fileDao.getDFileByTaskId(tFileInfo.getTaskId());
        fileDao.deleteByTaskId(tFileInfo.getTaskId());
        // 删除缓存文件
        if (!tFileInfo.isSend()) {
            delCacheFile(dFile);
        }
    }

    /**
     * 处理取消命令,1取消当前发送的任务,2取消等待发送的任务
     */
    private void dispatchCancel(byte[] bodyData) {
        try {
            logger.e("****dispatchCancel1111");
            final XFileProtocol.File reqFile = XFileProtocol.File.parseFrom(bodyData);
            TaskInstance taskInstance = getTaskInstance(reqFile.getTaskId());
            taskInstance.dispatchCancel(reqFile);
            removeTask(taskInstance.getTaskId());
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
        for (int i = 0; i < taskQueue.size(); i++) {
            TaskInstance currentTask = taskQueue.get(i);
            if (currentTask.getTaskId() != null && currentTask.getTaskId().equals(taskId)) {
                taskQueue.remove(i);
                break;
            }
        }
    }

    /**
     * 获取当前实例
     */
    public TaskInstance getTaskInstance(String taskId) {
        for (TaskInstance taskInstance : taskQueue) {
            if (taskInstance.getTaskId().equals(taskId)) {
                return taskInstance;
            }
        }
        return null;
    }


    /**
     * 检查是否还有未完成的任务
     */
    public void checkUndone() {
        if (taskQueue != null && taskQueue.size() > 0) {
            TaskInstance task = taskQueue.pop();
            task.resume();
        }
    }


    /**
     * 是否包含任务
     */
    public boolean containsTask(String taskId) {
        for (TaskInstance taskInstance : taskQueue) {
            if (taskInstance.getTaskId().equals(taskId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通知界面
     */
    private void triggerEvent(TFileInfo tFileInfo) {
        EventBus.getDefault().post(tFileInfo);
    }

    public boolean isTransmit() {
        for (TaskInstance taskInstance : taskQueue) {
            if (taskInstance.isTransmit()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除本地缓存文件
     */
    public boolean delCacheFile(DFile dFile) {
        File cacheFile = new File(dFile.getSavePath());
        return cacheFile.delete();
    }


}
