package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.huangjiang.XFileApplication;
import com.huangjiang.config.Config;
import com.huangjiang.config.SysConstant;
import com.huangjiang.message.DeviceServerThread;
import com.huangjiang.message.base.DataBuffer;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.event.ScanDeviceInfo;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.NetStateUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import io.netty.buffer.ByteBuf;

/**
 * 广播服务管理
 */
public class IMDeviceServerManager extends IMBaseManager {

    private Logger logger = Logger.getLogger(IMDeviceServerManager.class);

    private static IMDeviceServerManager inst = null;

    private DeviceServerThread mDeviceServerThread = null;

    private String ip;

    private int port;

    private String device_id;

    Timer timer = null;

    TimerTask timerTask = null;

    public static IMDeviceServerManager getInstance() {
        if (inst == null) {
            inst = new IMDeviceServerManager();
        }
        return inst;
    }

    public IMDeviceServerManager() {

    }

    @Override
    public void start() {
        startServer();
    }

    @Override
    public void stop() {
        stopServer();
    }

    void startServer() {
        stopServer();
        this.ip = Config.is_ap ? SysConstant.DEFAULT_AP_IP : NetStateUtils.getIPv4(XFileApplication.context);
        this.port = SysConstant.BROADCASE_PORT;
        this.device_id = XFileApplication.device_id;
        mDeviceServerThread = new DeviceServerThread();
        mDeviceServerThread.start();
    }

    void stopServer() {
        if (mDeviceServerThread != null) {
            mDeviceServerThread.stopServer();
            mDeviceServerThread = null;
        }
    }


    public void sendMessage(short serviceId, short commandId, GeneratedMessage msg, String host, int port) {
        if (mDeviceServerThread != null) {
            Header header = new Header();
            header.setCommandId(commandId);
            header.setServiceId(serviceId);
            header.setLength(SysConstant.HEADER_LENGTH + msg.getSerializedSize());
            mDeviceServerThread.sendRequest(header, msg, host, port);
        }
    }

    public void packetDispatch(ByteBuf byteBuf) {
        DataBuffer dataBuffer = new DataBuffer(byteBuf);
        Header header = dataBuffer.getHeader();
        byte[] bodyData = dataBuffer.getBodyData();
        int commandId = header.getCommandId();
        switch (commandId) {
            case SysConstant.CMD_Bonjour:
                dispatchBonjour(bodyData);
                break;
            case SysConstant.CMD_ECHO:
                dispatchEcho(bodyData);
                break;
        }
    }


    /**
     * 请求设备
     */
    void dispatchBonjour(byte[] bodyData) {
        try {
            XFileProtocol.Bonjour request = XFileProtocol.Bonjour.parseFrom(bodyData);
            String remoteIp = request.getIp();
            int remotePort = request.getPort();
            String deviceId = request.getDeviceId();
            if (!this.device_id.equals(deviceId)) {
                XFileProtocol.Echo.Builder response = XFileProtocol.Echo.newBuilder();
                response.setIp(this.ip);
                response.setMessagePort(SysConstant.MESSAGE_PORT);
                response.setFilePort(SysConstant.FILE_SERVER_PORT);
                response.setName(android.os.Build.MODEL);
                response.setDeviceId(XFileApplication.device_id);
                short serviceId = SysConstant.SERVICE_DEFAULT;
                short commandId = SysConstant.CMD_ECHO;
                sendMessage(serviceId, commandId, response.build(), remoteIp, remotePort);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }


    /**
     * 设备答复
     */
    void dispatchEcho(byte[] bodyData) {
        try {
            XFileProtocol.Echo request = XFileProtocol.Echo.parseFrom(bodyData);
            ScanDeviceInfo event = new ScanDeviceInfo();
            event.setIp(request.getIp());
            event.setName(request.getName());
            event.setMessage_port(request.getMessagePort());
            event.setFile_port(request.getFilePort());
            event.setDevice_id(request.getDeviceId());
            EventBus.getDefault().postSticky(event);
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }


    public void startBonjour() {
        cancelBonjour();
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                XFileProtocol.Bonjour.Builder request = XFileProtocol.Bonjour.newBuilder();
                request.setIp(ip);
                request.setPort(port);
                request.setDeviceId(device_id);
                short serviceId = SysConstant.SERVICE_DEFAULT;
                short commandId = SysConstant.CMD_Bonjour;
                sendMessage(serviceId, commandId, request.build(), SysConstant.BROADCASE_ADDRESS, SysConstant.BROADCASE_PORT);
            }
        };
        timer.schedule(timerTask, 200, 200);
    }

    public void cancelBonjour() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }


}
