package com.huangjiang.manager.callback;

/**
 * @author : yingmu on 15-1-7.
 * @email : yingmu@mogujie.com.
 */
public interface IMListener<T> {
    public abstract void onSuccess(short serviceId, T response);

    public abstract void onFaild();

    public abstract void onTimeout();
}
