package com.huangjiang.manager;

import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.config.SysConstant;
import com.huangjiang.message.ServerMessageHandler;
import com.huangjiang.message.ServerThread;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;

import io.netty.buffer.ByteBuf;

/**
 * 消息服务管理
 */
public class IMMessageServerManager extends IMManager {

    private Logger logger = Logger.getLogger(IMMessageServerManager.class);

    private static IMMessageServerManager inst = null;

    private ServerThread messageServerThread = null;

    public static IMMessageServerManager getInstance() {
        if (inst == null) {
            inst = new IMMessageServerManager();
        }
        return inst;
    }

    public IMMessageServerManager() {
        messageServerThread = new ServerThread(SysConstant.MESSAGE_PORT, new ServerMessageHandler());
    }

    @Override
    public void start() {
        messageServerThread.start();
    }

    @Override
    public void stop() {
        messageServerThread.stopServer();
    }

    public void packetDispatch(ByteBuf byteBuf) throws InvalidProtocolBufferException {
        byte[] byteHeader = byteBuf.readBytes(SysConstant.HEADER_LENGTH).array();
        Header header = new Header(byteHeader);
        int commandId = header.getCommandId();
        switch (commandId) {
            case SysConstant.CMD_SEND_MESSAGE:
                recvMessage(byteBuf, header);
                break;

        }
    }

    void recvMessage(ByteBuf bf, Header header) {
        try {
            int lenght = header.getLength();
            ByteBuf byteBuf = bf.readBytes(lenght - SysConstant.HEADER_LENGTH);
            byte[] body = new byte[lenght - SysConstant.HEADER_LENGTH];
            byteBuf.readBytes(body);
            XFileProtocol.Chat chat = XFileProtocol.Chat.parseFrom(body);
            System.out.println("*****Chat.getMessagetype:" + chat.getMessagetype());
            System.out.println("*****Chat.getContent:" + chat.getContent());
            System.out.println("*****Chat.getFrom:" + chat.getFrom());
        } catch (Exception e) {
            e.printStackTrace();
            logger.d(e.getMessage());
        }

    }
}
