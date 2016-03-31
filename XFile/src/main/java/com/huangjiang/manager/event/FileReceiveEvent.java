package com.huangjiang.manager.event;

import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.message.protocol.XFileProtocol;

/**
 * EventBus-文件接受参数
 */
public class FileReceiveEvent {

    FileEvent event;

    TFileInfo fileInfo;

    private String taskId;

    public FileReceiveEvent(FileEvent event) {
        this.event = event;
    }

    public void setEvent(FileEvent event) {
        this.event = event;
    }

    public FileEvent getEvent() {
        return event;
    }

    public void setFileInfo(TFileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public TFileInfo getFileInfo() {
        return fileInfo;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
