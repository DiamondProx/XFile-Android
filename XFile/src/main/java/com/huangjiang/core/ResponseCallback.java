package com.huangjiang.core;

/**
 * 返回结果
 */
public interface ResponseCallback<Result> {

    /**
     * 请求响应的回调接口
     *
     * @param stateCode 请求状态码
     * @param code      响应的消息的状态码
     * @param msg       响应的消息的状态码对应的消息
     * @throws
     * @param: @param result  实体对象
     * @return: void
     */
    public void onResponse(int stateCode, int code, String msg, Result result);

}
