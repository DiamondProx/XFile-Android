package com.huangjiang.manager.event;

/**
 * 消息服务端Socket参数
 */
public class ServerFileSocketEvent {
    private SocketEvent event;

    public ServerFileSocketEvent(SocketEvent event) {
        this.event = event;
    }
}
