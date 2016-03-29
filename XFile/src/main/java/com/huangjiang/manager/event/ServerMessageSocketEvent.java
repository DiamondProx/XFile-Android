package com.huangjiang.manager.event;

/**
 * 消息服务端Socket参数
 */
public class ServerMessageSocketEvent {

    private SocketEvent event;

    public ServerMessageSocketEvent(SocketEvent event) {
        this.event = event;
    }

    public SocketEvent getEvent() {
        return event;
    }

    public void setEvent(SocketEvent event) {
        this.event = event;
    }
}
