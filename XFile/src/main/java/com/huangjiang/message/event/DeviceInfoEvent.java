package com.huangjiang.message.event;

/**
 * 设备信息
 */
public class DeviceInfoEvent {

    private String ip;
    private int brocast_port;
    private int message_port;
    private int file_port;
    private String name;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getBrocast_port() {
        return brocast_port;
    }

    public void setBrocast_port(int brocast_port) {
        this.brocast_port = brocast_port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMessage_port() {
        return message_port;
    }

    public void setMessage_port(int message_port) {
        this.message_port = message_port;
    }

    public int getFile_port() {
        return file_port;
    }

    public void setFile_port(int file_port) {
        this.file_port = file_port;
    }
}
