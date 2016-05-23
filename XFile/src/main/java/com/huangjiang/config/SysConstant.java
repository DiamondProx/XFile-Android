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

    public static final short CMD_FILE_NEW = 0x07;//发送文件-发送文件步骤1

    public static final short CMD_FILE_NEW_RSP = 0x08;//发送文件-发送文件步骤1答复

    public static final short CMD_TASK_CHECK = 0x09;//检查文件-发送文件步骤2

    public static final short CMD_TASK_CHECK_RSP = 0x10;//检查文件-发送文件步骤2答复

    public static final short CMD_FILE_SET = 0x11;//文件保存-发送文件步骤3

    public static final short CMD_FILE_SET_RSP = 0x12;//文件保存-发送文件步骤3答复

    public static final short CMD_FILE_RESUME = 0x13;//文件保存-短点续传

    public static final short CMD_FILE_CANCEL = 0x14;//文件取消

    public static final short CMD_FILE_CANCEL_RSP = 0x15;//文件取消-答复

    public static final short CMD_FILE_RESUME_REMOTE = 0x16;//续传

    public static final String BROADCASE_ADDRESS = "255.255.255.255";//广播地址

    public static final int BROADCASE_PORT = 8081;// 发现设备服务器端口

    public static final int MESSAGE_PORT = 9099;// 消息服务器端口

    public static final int FILE_SERVER_PORT = 9098;// 文件服务器端口

    public static final int FILE_SEGMENT_SIZE = 1024 * 50;// 分段文件大小

    public static final short SERVICE_DEFAULT = 0x99;// 保留服务号

    public static final short SERVICE_FILE_NEW_SUCCESS = 0x98;//接收方新建文件成功-接收文件步骤1

    public static final short SERVICE_FILE_NEW_FAILED = 0x97;//新建文件失败-接收文件步骤1

    public static final short SERVICE_TASK_CHECK_SUCCESS = 0x96;//检查任务成功-接收文件步骤2

    public static final short SERVICE_TASK_CHECK_FAILED = 0x95;//检查任务失败-接收文件步骤2

    public static final short SERVICE_FILE_SET_SUCCESS = 0x94;//保存文件成功-接收文件步骤3

    public static final short SERVICE_FILE_SET_FAILED = 0x93;//保存文件失败-接收文件步骤3

    public static final short SERVICE_FILE_SET_STOP = 0x92;//暂停接收文件

    public static final boolean VERIFY = false;// 是否需要密码验证

    public static final String PASS_WORD = "123456";// 后台密码

    public static final String TOKEN = "token";// 测试token

    public static final String DEFAULT_AP_IP = "192.168.43.1";// 热点默认地址

    public static final String SEARCH_KEY = "XFile";


}
