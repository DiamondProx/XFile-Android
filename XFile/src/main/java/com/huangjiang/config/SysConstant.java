package com.huangjiang.config;

/**
 * 系统常量
 */
public class SysConstant {

    public static final int HEADER_LENGTH = 12;//协议长度

    public static final short CMD_Bonjour = 0x01;//发现服务器

    public static final short CMD_ECHO = 0x02;//服务器应答

    public static final String BROADCASE_ADDRESS = "255.255.255.255";//广播地址

    public static final int BROADCASE_PORT = 8081;// 发现设备服务器端口

    public static final int MESSAGE_PORT = 9091;// 消息服务器端口

    public static final int FILE_SERVER_PORT = 9092;// 文件服务器端口


}
