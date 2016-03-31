package com.huangjiang.manager.event;

import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.message.protocol.XFileProtocol;

/**
 * EventBus-文件发送参数
 */
public class FileSendEvent {

    FileEvent event;

    TFileInfo fileInfo;

    private String taskId;

    public FileSendEvent(FileEvent event) {
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
