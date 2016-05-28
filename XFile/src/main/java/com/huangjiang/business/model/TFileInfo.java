package com.huangjiang.business.model;

import android.support.annotation.NonNull;

import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.utils.XFileUtils;

/**
 * 传输文件实体
 */
public class TFileInfo implements Comparable<TFileInfo> {


    /**
     * 数据库编号
     */
    private Long id;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 传输位置
     */
    private long position;
    /**
     * 文件长度
     */
    private long length;
    /**
     * 路径
     */
    private String path;
    /**
     * 后缀名
     */
    private String extension;
    /**
     * 文件全名
     */
    private String fullName;
    /**
     * 任务编号
     */
    private String taskId;
    /**
     * 是否发送方,true发送方,false接收方
     */
    private boolean isSend;
    /**
     * 发送方
     */
    private String from;
    /**
     * 传输状态
     */
    private FileEvent fileEvent = FileEvent.NONE;
    /*
    * 文件类型
     */
    private FileType fileType = FileType.Normal;
    /*
     *创建时间
     */
    private String createTime;

    /**
     * 播放时长
     */
    private int playTime;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 是否目录
     */
    private boolean directory = false;

    public TFileInfo() {
    }

    public TFileInfo(String name, long position, long length, String path, String extension, String fullName, String taskId, boolean isSend, String from, String createTime, int playTime, String packageName, boolean directory, FileEvent fileEvent, FileType fileType) {
        this.name = name;
        this.position = position;
        this.length = length;
        this.path = path;
        this.extension = extension;
        this.fullName = fullName;
        this.taskId = taskId;
        this.isSend = isSend;
        this.from = from;
        this.createTime = createTime;
        this.playTime = playTime;
        this.packageName = packageName;
        this.directory = directory;
        this.fileEvent = fileEvent;
        this.fileType = fileType;
    }

    public TFileInfo(Long id, String name, String taskId, Long length, Long position, String path, Boolean isSend, String extension, String fullName, String from, Integer status) {
        this.id = id;
        this.name = name;
        this.taskId = taskId;
        this.length = length;
        this.position = position;
        this.path = path;
        this.isSend = isSend;
        this.extension = extension;
        this.fullName = fullName;
        this.from = from;
        setStatus(status);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setIsSend(boolean isSend) {
        this.isSend = isSend;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public FileEvent getFileEvent() {
        return fileEvent;
    }

    public void setFileEvent(FileEvent fileEvent) {
        this.fileEvent = fileEvent;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getPlayTime() {
        return playTime;
    }

    public void setPlayTime(int playTime) {
        this.playTime = playTime;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public int compareTo(@NonNull TFileInfo fileInfo) {
        if (this.name != null)
            return this.name.compareTo(fileInfo.getName());
        else
            throw new IllegalArgumentException();
    }

    public TFileInfo newInstance() {
        return new TFileInfo(this.name, this.position, this.length, this.path, this.extension, this.fullName, this.taskId, this.isSend, this.from, this.createTime, this.playTime, this.packageName, this.directory, this.fileEvent, this.fileType);
    }

    public boolean isDirectory() {
        return directory;
    }

    public void setDirectory(boolean directory) {
        this.directory = directory;
    }

    public Integer getStatus() {
        switch (fileEvent) {
            case CREATE_FILE_SUCCESS:
            case CHECK_TASK_SUCCESS:
            case SET_FILE:
            case SET_FILE_STOP:
            case WAITING:
                // 正在传送
                return 0;
            case SET_FILE_SUCCESS:
                // 传输完成
                return 1;
            case CREATE_FILE_FAILED:
            case CHECK_TASK_FAILED:
            case SET_FILE_FAILED:
                // 传输失败
                return 2;
            default:
                return 0;
        }
    }

    public void setStatus(Integer status) {
        switch (status) {
            case 0:
                this.fileEvent = FileEvent.SET_FILE_STOP;
                break;
            case 1:
                this.fileEvent = FileEvent.SET_FILE_SUCCESS;
                break;
            case 2:
                this.fileEvent = FileEvent.SET_FILE_FAILED;
                break;
        }
    }
}
