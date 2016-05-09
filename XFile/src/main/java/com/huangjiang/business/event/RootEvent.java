package com.huangjiang.business.event;

/**
 * 获取root权限
 */
public class RootEvent {

    private boolean result;

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
