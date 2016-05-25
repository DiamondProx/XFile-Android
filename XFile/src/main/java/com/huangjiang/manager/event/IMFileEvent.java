package com.huangjiang.manager.event;

/**
 * 文件事件
 */
public class IMFileEvent {

    private String taskId;

    EventType eventType;

    public IMFileEvent(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public enum EventType {
        COMPLETE
    }
}
