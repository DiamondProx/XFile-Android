package com.huangjiang.business.model;

import com.huangjiang.manager.event.FileEvent;

/**
 * 传输文件实体
 */
public class TFileInfo {

    /*
    required string name = 1;
    required string md5 = 2;
    required bytes data = 3;
    required int64 position = 4;
    required int64 length = 5;
    required string path = 6;
    required string extension = 7;
    required string full_name = 8;
    required string task_id = 9;
    */

    private String name;
    private String md5;
    private long postion;
    private long length;
    private String path;
    private String extension;
    private String full_name;
    private String task_id;
    private boolean is_send;// 是否发送方
    private String from;//发送方
    private long percent;// 百分比
    private FileEvent stateEvent;//传输状态

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public long getPostion() {
        return postion;
    }

    public void setPostion(long postion) {
        this.postion = postion;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getFull_name() {
        return full_name;
    }

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public String getTask_id() {
        return task_id;
    }

    public void setTask_id(String task_id) {
        this.task_id = task_id;
    }

    public boolean is_send() {
        return is_send;
    }

    public void setIs_send(boolean is_send) {
        this.is_send = is_send;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public long getPercent() {
        return percent;
    }

    public void setPercent(long percent) {
        this.percent = percent;
    }

    public FileEvent getStateEvent() {
        return stateEvent;
    }

    public void setStateEvent(FileEvent stateEvent) {
        this.stateEvent = stateEvent;
    }
}
