package com.huangjiang.business.event;

/**
 * 连接,传输记录
 */
public class RecordEvent {

    /**
     * 设备连接数
     */
    private int deviceCount;
    /**
     * 连接人次
     */
    private int connectCount;
    /**
     * 传输历史大小
     */
    private long totalSize;

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public int getConnectCount() {
        return connectCount;
    }

    public void setConnectCount(int connectCount) {
        this.connectCount = connectCount;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
}
