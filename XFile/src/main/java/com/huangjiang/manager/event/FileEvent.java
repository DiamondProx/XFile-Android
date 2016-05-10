package com.huangjiang.manager.event;

/**
 * 文件EventBus参数
 */
public enum FileEvent {

    /**
     * 缺省
     */
    NONE,
    /**
     * 文件创建成功
     */
    CREATE_FILE_SUCCESS,
    /**
     * 文件创建失败
     */
    CREATE_FILE_FAILED,
    /**
     * 任务创建成功
     */
    CHECK_TASK_SUCCESS,
    /**
     * 任务创建失败
     */
    CHECK_TASK_FAILED,
    /**
     * 续传成功
     */
    SET_FILE_SUCCESS,
    /**
     * 续传失败
     */
    SET_FILE_FAILED,
    /**
     * 传输暂停
     */
    SET_FILE_STOP,
    /**
     * 设置文件
     */
    SET_FILE,
    /**
     * 取消发送
     */
    CANCEL_FILE,
    /**
     * 等待
     */
    WAITING


}
