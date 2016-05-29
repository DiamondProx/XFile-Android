package com.huangjiang.manager.event;

import com.huangjiang.business.model.TFileInfo;

/**
 * 创建任务
 */
public class CreateTaskEvent {

    public Event event;
    public TFileInfo TFile;


    public CreateTaskEvent(Event event) {
        this.event = event;
    }

    public CreateTaskEvent(Event event, TFileInfo TFile) {
        this(event);
        this.TFile = TFile;
    }

    public enum Event {
        SUCCESS,
        FAILED
    }
}
