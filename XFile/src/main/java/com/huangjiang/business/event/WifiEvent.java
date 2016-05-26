package com.huangjiang.business.event;

/**
 * 连接事件
 */
public class WifiEvent {

    private boolean isConnected;

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}
