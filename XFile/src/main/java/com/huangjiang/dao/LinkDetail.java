package com.huangjiang.dao;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT. Enable "keep" sections if you want to edit. 
/**
 * Entity mapped to table "DLINK_DETAIL".
 */
public class LinkDetail {

    private Long id;
    private String deviceId;
    private Integer count;

    public LinkDetail() {
    }

    public LinkDetail(Long id) {
        this.id = id;
    }

    public LinkDetail(Long id, String deviceId, int count) {
        this.id = id;
        this.deviceId = deviceId;
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}