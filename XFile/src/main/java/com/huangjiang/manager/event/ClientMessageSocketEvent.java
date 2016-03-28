package com.huangjiang.manager.event;

/**
 * 消息客户端Socket参数
 */
public class ClientMessageSocketEvent {

    private SocketEvent event;

    public ClientMessageSocketEvent(SocketEvent event) {
        this.event = event;
    }

    public SocketEvent getEvent() {
        return event;
    }

    public void setEvent(SocketEvent event) {
        this.event = event;
    }
}
