package com.huangjiang.business.model;

/**
 * 设备信息
 */
public class ScanInfo {

    private String ip;
    private String name;
    private int msgPort;
    private int filePort;
    private String deviceId;
    private LinkType linkType;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMsgPort() {
        return msgPort;
    }

    public void setMsgPort(int msgPort) {
        this.msgPort = msgPort;
    }

    public int getFilePort() {
        return filePort;
    }

    public void setFilePort(int filePort) {
        this.filePort = filePort;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public LinkType getLinkType() {
        return linkType;
    }

    public void setLinkType(LinkType linkType) {
        this.linkType = linkType;
    }

    public enum LinkType {
        /**
         * wifi连接
         */
        WIFI,
        /**
         * 热点连接
         */
        HOTSPOT
    }
}
