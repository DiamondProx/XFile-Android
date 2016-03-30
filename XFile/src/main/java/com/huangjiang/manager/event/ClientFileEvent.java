package com.huangjiang.manager.event;

/**
 * EventBus-客户端文件参数
 */
public class ClientFileEvent {

    FileEvent event;

    public ClientFileEvent(FileEvent event) {
        this.event = event;
    }

    public void setEvent(FileEvent event) {
        this.event = event;
    }

    public FileEvent getEvent() {
        return event;
    }
}
