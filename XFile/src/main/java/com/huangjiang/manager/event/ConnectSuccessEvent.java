package com.huangjiang.manager.event;

/**
 * 连接成功
 */
public class ConnectSuccessEvent {

    private String ipAddress;
    private int port;

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
