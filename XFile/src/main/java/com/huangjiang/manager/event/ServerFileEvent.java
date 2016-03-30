package com.huangjiang.manager.event;

/**
 * EventBus-服务端文件参数
 */
public class ServerFileEvent {

    FileEvent event;

    public ServerFileEvent(FileEvent event) {
        this.event = event;
    }

    public void setEvent(FileEvent event) {
        this.event = event;
    }

    public FileEvent getEvent() {
        return event;
    }
}
