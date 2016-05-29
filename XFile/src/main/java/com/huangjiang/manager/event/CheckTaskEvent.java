package com.huangjiang.manager.event;

import com.huangjiang.business.model.TFileInfo;

/**
 * 检查任务
 */
public class CheckTaskEvent {

    public Event event;
    public TFileInfo TFile;


    public CheckTaskEvent(Event event) {
        this.event = event;
    }

    public CheckTaskEvent(Event event, TFileInfo TFile) {
        this(event);
        this.TFile = TFile;
    }

    public enum Event {
        SUCCESS,
        FAILED
    }
}
