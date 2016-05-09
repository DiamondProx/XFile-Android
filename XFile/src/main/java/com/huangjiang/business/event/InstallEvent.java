package com.huangjiang.business.event;

import com.huangjiang.business.model.TFileInfo;

/**
 * 安装事件
 */
public class InstallEvent {

    private boolean success;

    private TFileInfo tFileInfo;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public TFileInfo gettFileInfo() {
        return tFileInfo;
    }

    public void settFileInfo(TFileInfo tFileInfo) {
        this.tFileInfo = tFileInfo;
    }
}
