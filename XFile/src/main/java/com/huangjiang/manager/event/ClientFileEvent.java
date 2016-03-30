package com.huangjiang.manager.event;

import com.huangjiang.message.protocol.XFileProtocol;

/**
 * EventBus-客户端文件参数
 */
public class ClientFileEvent {

    FileEvent event;

    XFileProtocol.File fileInfo;

    private String taskId;

    public ClientFileEvent(FileEvent event) {
        this.event = event;
    }

    public void setEvent(FileEvent event) {
        this.event = event;
    }

    public FileEvent getEvent() {
        return event;
    }

    public void setFileInfo(XFileProtocol.File fileInfo) {
        this.fileInfo = fileInfo;
    }

    public XFileProtocol.File getFileInfo() {
        return fileInfo;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
