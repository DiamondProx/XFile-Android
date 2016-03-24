package com.huangjiang.config;

/**
 * 系统常量
 */
public class SysConstant {

    public static final int HEADER_LENGTH = 12;//协议长度

    public static final short CMD_Bonjour = 0x01;//发现服务器

    public static final short CMD_ECHO = 0x02;//服务器应答

    public static final short CMD_TRANSER_FILE_SEND = 0x03;//发送文件

    public static final short CMD_TRANSER_FILE_REC = 0x04;//接收文件

    public static final short CMD_SEND_MESSAGE = 0x05;//发送消息

    public static final short CMD_SHAKE_HAND = 0x06;//握手

    public static final String BROADCASE_ADDRESS = "255.255.255.255";//广播地址

    public static final int BROADCASE_PORT = 8081;// 发现设备服务器端口

    public static final int MESSAGE_PORT = 9099;// 消息服务器端口

    public static final int FILE_SERVER_PORT = 9098;// 文件服务器端口

    public static final int FILE_SEGMENT_SIZE = 4096;// 分段文件大小

    public static final short SERVICE_DEFAULT = 0x99;// 保留服务号

    public static final boolean VERIFY = false;// 是否需要密码验证

    public static final String PASS_WORD = "123456";// 后台密码


}
