package com.huangjiang.business.event;

/**
 * 磁盘状态
 */
public class DiskEvent {

    private DiskState diskState;

    public DiskEvent(DiskState diskState) {
        this.diskState = diskState;
    }

    public DiskState getDiskState() {
        return diskState;
    }

    public enum DiskState {
        ERROR,
        DENIED_PERMISSION,
        ENOUGH
    }
}
