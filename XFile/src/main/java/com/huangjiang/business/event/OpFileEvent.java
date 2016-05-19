package com.huangjiang.business.event;

import com.huangjiang.business.model.FileType;
import com.huangjiang.business.model.TFileInfo;

/**
 * 文件操作
 */
public class OpFileEvent {

    private TFileInfo tFileInfo;
    private OpType opType;
    private boolean isSuccess;
    private String message;
    private FileType fileType;
    private Target target = Target.NONE;

    public OpFileEvent(OpType opType, TFileInfo tFileInfo) {
        this.opType = opType;
        this.tFileInfo = tFileInfo;
    }

    public TFileInfo getTFileInfo() {
        return tFileInfo;
    }

    public void setTFileInfo(TFileInfo tFileInfo) {
        this.tFileInfo = tFileInfo;
    }

    public OpType getOpType() {
        return opType;
    }

    public void setOpType(OpType opType) {
        this.opType = opType;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }


    public enum OpType {
        DELETE,
        RENAME,
        BACKUP,
        UNINSTALL,
        CHANGE
    }

    public enum Target {
        SEARCH_FRAGMENT,
        EXPLORER_CONTROL,
        VIDEO_FRAGMENT,
        MUSIC_FRAGMENT,
        PICTURE_FRAGMENT,
        NONE
    }
}
