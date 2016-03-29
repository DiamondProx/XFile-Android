package com.huangjiang.manager.event;

/**
 * 文件客户端Socket参数
 */
public class ClientFileSocketEvent {
    
    /**
     * 连接设备名称
     */
    private String device_name;

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

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }
}
