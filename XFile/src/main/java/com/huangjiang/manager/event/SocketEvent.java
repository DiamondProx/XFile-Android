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
    SOCKET_ERROR,
    SHAKE_HAND_FAILE,
    SHAKE_HAND_SETP1_SUCCESS,
    SHAKE_INPUT_PASSWORD
}
