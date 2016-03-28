package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.XFileApplication;
import com.huangjiang.config.SysConstant;
import com.huangjiang.message.DeviceServerThread;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.event.DeviceInfoEvent;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.NetStateUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import io.netty.buffer.ByteBuf;

/**
 * 广播服务管理
 */
public class IMDeviceServerManager extends IMManager {

    private Logger logger = Logger.getLogger(IMDeviceServerManager.class);

    private static IMDeviceServerManager inst = null;

    private DeviceServerThread mDeviceServerThread = null;

    private String ip;
    private int port;

    public static IMDeviceServerManager getInstance() {
        if (inst == null) {
            inst = new IMDeviceServerManager();
        }
        return inst;
    }

    public IMDeviceServerManager() {
        mDeviceServerThread = new DeviceServerThread();
    }

    public void sendMessage(GeneratedMessage msg, short serviceId, short commandId, String host, int port) {
        try {
            Header header = new Header();
            header.setCommandId(commandId);
            header.setServiceId(serviceId);
            header.setLength(SysConstant.HEADER_LENGTH + msg.getSerializedSize());
            mDeviceServerThread.sendRequest(msg, header, host, port);
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    public void packetDispatch(ByteBuf byteBuf) throws InvalidProtocolBufferException {
        byte[] byteHeader = byteBuf.readBytes(SysConstant.HEADER_LENGTH).array();
        Header header = new Header(byteHeader);
        int commandId = header.getCommandId();
        switch (commandId) {
            case SysConstant.CMD_Bonjour:
                reqEcho(byteBuf, header);
                break;
            case SysConstant.CMD_ECHO:
                rspEcho(byteBuf, header);
                break;
        }
    }


    /**
     * 请求
     *
     * @param byteBuf
     * @param header
     * @throws InvalidProtocolBufferException
     */
    void reqEcho(ByteBuf byteBuf, Header header) throws InvalidProtocolBufferException {

        // 读取发送地址
        byte[] bodyData = byteBuf.readBytes(header.getLength() - SysConstant.HEADER_LENGTH).array();
        XFileProtocol.Bonjour request = XFileProtocol.Bonjour.parseFrom(bodyData);
        String remoteIp = request.getIp();
        int remotePort = request.getPort();
        // 发送本机地址
        String localIp = NetStateUtil.getIPv4(XFileApplication.context);
        XFileProtocol.Echo.Builder msg = XFileProtocol.Echo.newBuilder();
        msg.setIp(localIp);
        // 发送本机服务绑定的文件端口,服务端口
        msg.setMessagePort(SysConstant.MESSAGE_PORT);
        msg.setFilePort(SysConstant.FILE_SERVER_PORT);
        msg.setName(android.os.Build.MODEL);
        short serviceId = 0;
        short commandId = SysConstant.CMD_ECHO;
        sendMessage(msg.build(), serviceId, commandId, remoteIp, remotePort);

    }


    void rspEcho(ByteBuf byteBuf, Header header) throws InvalidProtocolBufferException {
        // 读取到其他远程地址
        byte[] bodyData = byteBuf.readBytes(header.getLength() - SysConstant.HEADER_LENGTH).array();
        XFileProtocol.Echo echo = XFileProtocol.Echo.parseFrom(bodyData);
        DeviceInfoEvent event = new DeviceInfoEvent();
        event.setIp(echo.getIp());
        event.setName(echo.getName());
        event.setMessage_port(echo.getMessagePort());
        event.setFile_port(echo.getFilePort());
        EventBus.getDefault().postSticky(event);

    }


    @Override
    public void start() {
        mDeviceServerThread.start();
    }

    @Override
    public void stop() {
        mDeviceServerThread.stopService();
    }


    public void DeviceBroCast(String ipAddress, int port) {
        logger.e("*****DeviceBroCast");
        XFileProtocol.Bonjour.Builder bonjour = XFileProtocol.Bonjour.newBuilder();
        bonjour.setIp(ipAddress);
        bonjour.setPort(port);
        sendMessage(bonjour.build(), SysConstant.CMD_Bonjour, SysConstant.CMD_Bonjour, SysConstant.BROADCASE_ADDRESS, SysConstant.BROADCASE_PORT);
    }

    public void startBrocastService(){

        timer.schedule(timerTask,500,500);
    }
    public void cancelBrocastServer(){
        timer.cancel();
    }

    Timer timer = new Timer();

    TimerTask timerTask=new TimerTask() {
        @Override
        public void run() {
            DeviceBroCast(ip,port);
        }
    };

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
