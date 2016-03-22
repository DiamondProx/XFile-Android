package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.config.SysConstant;
import com.huangjiang.message.ServerFileHandler;
import com.huangjiang.message.ServerThread;
import com.huangjiang.message.base.Header;
import com.huangjiang.utils.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 文件管理服务端
 */
public class IMFileServerManager extends IMManager {

    private Logger logger = Logger.getLogger(IMMessageServerManager.class);


    private static IMFileServerManager inst = null;

    private ServerThread messageServerThread = null;

    public static IMFileServerManager getInstance() {
        if (inst == null) {
            inst = new IMFileServerManager();
        }
        return inst;
    }

    public IMFileServerManager() {
        messageServerThread = new ServerThread(SysConstant.FILE_SERVER_PORT, new ServerFileHandler());
    }

    @Override
    public void start() {
        messageServerThread.start();
    }

    @Override
    public void stop() {
        messageServerThread.stopServer();
    }

    public void packetDispatch(ChannelHandlerContext ctx, ByteBuf byteBuf) throws InvalidProtocolBufferException {
        byte[] byteHeader = byteBuf.readBytes(SysConstant.HEADER_LENGTH).array();
        Header header = new Header(byteHeader);
        int commandId = header.getCommandId();
        switch (commandId) {
            case SysConstant.CMD_TRANSER_FILE_SEND:
                IMFileManager.getInstance().recvFile(ctx, header, byteBuf);
                break;

        }
    }

    public void sendMessage(ChannelHandlerContext ctx, GeneratedMessage msg, short serviceId, short commandId) {
        try {
            Header header = new Header();
            header.setCommandId(commandId);
            header.setServiceId(serviceId);
            header.setLength(SysConstant.HEADER_LENGTH + msg.getSerializedSize());
            messageServerThread.sendMessage(ctx, msg, header);
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }
}
