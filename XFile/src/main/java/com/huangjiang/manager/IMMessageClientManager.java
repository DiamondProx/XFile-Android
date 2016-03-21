package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.config.SysConstant;
import com.huangjiang.message.ClientMessageHandler;
import com.huangjiang.message.ClientThread;
import com.huangjiang.message.base.Header;
import com.huangjiang.utils.Logger;

import io.netty.buffer.ByteBuf;

/**
 * 消息客户端管理
 */
public class IMMessageClientManager extends IMManager {


    private Logger logger = Logger.getLogger(IMMessageClientManager.class);

    private static IMMessageClientManager inst = null;

    private ClientThread messageClientThread = null;

    public static IMMessageClientManager getInstance() {
        if (inst == null) {
            inst = new IMMessageClientManager();
        }
        return inst;
    }

    public IMMessageClientManager() {
        messageClientThread = new ClientThread(new ClientMessageHandler());
    }

    public void setHost(String host) {
        messageClientThread.setHost(host);
    }

    public void setPort(int port) {
        messageClientThread.setPort(port);
    }

    @Override
    public void start() {
        messageClientThread.start();
    }

    @Override
    public void stop() {
        messageClientThread.closeConnect();
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
            messageClientThread.sendMessage(msg, header);
        }
    }
}
