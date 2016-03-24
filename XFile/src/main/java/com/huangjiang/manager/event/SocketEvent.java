package com.huangjiang.manager.event;

/**
 * 提示连接参数
 */
public enum SocketEvent {
    NONE,
    CONNECT_FAILE,
    CONNECT_SUCCESS,
    CONNECT_TIMEOUT,
    REQUEST_SUCCESS,
    REQUEST_FAILE,
    REQUEST_TIMEOUT,
    CONNECT_CLOSE,
    SOCKET_ERROR
}
