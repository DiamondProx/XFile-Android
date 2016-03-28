package com.huangjiang.manager.event;

/**
 * 文件客户端Socket参数
 */
public class ClientFileSocketEvent {

    private SocketEvent event;

    public ClientFileSocketEvent(SocketEvent event) {
        this.event = event;
    }

    public SocketEvent getEvent() {
        return event;
    }

    public void setEvent(SocketEvent event) {
        this.event = event;
    }
}
