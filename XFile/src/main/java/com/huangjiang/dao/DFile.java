package com.huangjiang.dao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit.
/**
 * Entity mapped to table "DFILE".
 */
public class DFile {

    private Long id;
    private String name;
    private String taskId;
    private Long length;
    private Long position;
    private String path;
    private Boolean isSend;
    private String extension;
    private String fullName;
    private String from;
    private Long percent;
    private Integer status;
    private String savePath;

    public DFile() {
    }

    public DFile(Long id) {
        this.id = id;
    }

    public DFile(Long id, String name, String taskId, Long length, Long position, String path, Boolean isSend, String extension, String fullName, String from, Long percent, Integer status, String savePath) {
        this.id = id;
        this.name = name;
        this.taskId = taskId;
        this.length = length;
        this.position = position;
        this.path = path;
        this.isSend = isSend;
        this.extension = extension;
        this.fullName = fullName;
        this.from = from;
        this.percent = percent;
        this.status = status;
        this.savePath = savePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public Long getLength() {
        return length;
    }

    public void setLength(Long length) {
        this.length = length;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getIsSend() {
        return isSend;
    }

    public void setIsSend(Boolean isSend) {
        this.isSend = isSend;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Long getPercent() {
        return percent;
    }

    public void setPercent(Long percent) {
        this.percent = percent;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }
}
