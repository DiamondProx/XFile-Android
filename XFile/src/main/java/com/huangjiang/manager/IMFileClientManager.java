package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.config.SysConstant;
import com.huangjiang.message.ClientFileHandler;
import com.huangjiang.message.ClientThread;
import com.huangjiang.message.base.Header;
import com.huangjiang.utils.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 文件管理客户端
 */
public class IMFileClientManager extends IMManager {

    private Logger logger = Logger.getLogger(IMMessageClientManager.class);

    private IMFileManager imFileManager = IMFileManager.getInstance();

    private static IMFileClientManager inst = null;

    private ClientThread fileClientThread = null;

    public static IMFileClientManager getInstance() {
        if (inst == null) {
            inst = new IMFileClientManager();
        }
        return inst;
    }

    public IMFileClientManager() {
        fileClientThread = new ClientThread(new ClientFileHandler());
    }

    public void setHost(String host) {
        fileClientThread.setHost(host);
    }

    public void setPort(int port) {
        fileClientThread.setPort(port);
    }

    @Override
    public void start() {
        fileClientThread.start();
    }

    @Override
    public void stop() {

    }

    public void sendMessage(GeneratedMessage msg, short serviceId, short commandId) {
        try {
            Header header = new Header();
            header.setCommandId(commandId);
            header.setServiceId(serviceId);
            header.setLength(SysConstant.HEADER_LENGTH + msg.getSerializedSize());
            fileClientThread.sendMessage(msg, header);
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    public void packetDispatch(ChannelHandlerContext ctx, ByteBuf byteBuf) throws InvalidProtocolBufferException {
        byte[] byteHeader = byteBuf.readBytes(SysConstant.HEADER_LENGTH).array();
        Header header = new Header(byteHeader);
        int commandId = header.getCommandId();
        switch (commandId) {
            case SysConstant.CMD_TRANSER_FILE_REC:
                imFileManager.continueSendFile(header, byteBuf);
                break;

        }
    }
}
