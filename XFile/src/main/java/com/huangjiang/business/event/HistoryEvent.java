package com.huangjiang.business.event;

/**
 * 历史消息
 */
public class HistoryEvent {

    private boolean isSuccess;
    private HistoryType historyType;

    public HistoryType getHistoryType() {
        return historyType;
    }

    public void setHistoryType(HistoryType historyType) {
        this.historyType = historyType;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public enum HistoryType {
        DELETE_ALL
    }
}
