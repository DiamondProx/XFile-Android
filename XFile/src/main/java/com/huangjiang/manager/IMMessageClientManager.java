package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.event.ClientFileSocketEvent;
import com.huangjiang.manager.event.ClientMessageSocketEvent;
import com.huangjiang.manager.event.SocketEvent;
import com.huangjiang.message.ClientFileHandler;
import com.huangjiang.message.ClientMessageHandler;
import com.huangjiang.message.ClientThread;
import com.huangjiang.message.base.Header;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import io.netty.buffer.ByteBuf;

/**
 * 消息客户端管理
 */
public class IMMessageClientManager extends IMManager implements ClientThread.OnClientListener {


    private Logger logger = Logger.getLogger(IMMessageClientManager.class);

    private static IMMessageClientManager inst = null;

    private ClientThread messageClientThread = null;

    private String host;

    private int port;

    public static IMMessageClientManager getInstance() {
        if (inst == null) {
            inst = new IMMessageClientManager();
        }
        return inst;
    }

    public IMMessageClientManager() {

    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public void start() {
        startClient();
    }

    @Override
    public void stop() {
        if (messageClientThread != null) {
            messageClientThread.closeConnect();
            messageClientThread = null;
        }
    }

    void startClient() {
        if (messageClientThread != null) {
            messageClientThread.closeConnect();
            messageClientThread = null;
        }
        messageClientThread = new ClientThread(new ClientFileHandler());
        messageClientThread.setOnClientListener(this);
        messageClientThread.setHost(this.host);
        messageClientThread.setPort(this.port);
        messageClientThread.start();
    }

    public void packetDispatch(ByteBuf byteBuf) throws InvalidProtocolBufferException {
        byte[] byteHeader = byteBuf.readBytes(SysConstant.HEADER_LENGTH).array();
        Header header = new Header(byteHeader);
        int commandId = header.getCommandId();
        switch (commandId) {
            case SysConstant.CMD_SEND_MESSAGE:

                break;

        }
    }

    public void sendMessage(GeneratedMessage msg, short serviceId, short commandId) {
        if (messageClientThread != null) {
            Header header = new Header();
            header.setCommandId(commandId);
            header.setServiceId(serviceId);
            header.setLength(SysConstant.HEADER_LENGTH + msg.getSerializedSize());
            messageClientThread.sendMessage(header, msg);
        }
    }

    public void sendShakeHand() {

    }

    @Override
    public void connectSuccess() {

    }

    @Override
    public void connectFailure() {
        EventBus.getDefault().post(new ClientMessageSocketEvent(SocketEvent.CONNECT_FAILE));
    }
}
