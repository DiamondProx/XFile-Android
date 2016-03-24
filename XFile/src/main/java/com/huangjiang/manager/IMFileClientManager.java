package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.event.ClientFileSocketEvent;
import com.huangjiang.manager.event.SocketEvent;
import com.huangjiang.message.ClientFileHandler;
import com.huangjiang.message.ClientThread;
import com.huangjiang.message.base.Header;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 文件管理客户端
 */
public class IMFileClientManager extends IMManager implements ClientThread.OnClientListener {

    private Logger logger = Logger.getLogger(IMMessageClientManager.class);


    private static IMFileClientManager inst = null;

    private ClientThread fileClientThread = null;

    private String host;

    private int port;

    public static IMFileClientManager getInstance() {
        if (inst == null) {
            inst = new IMFileClientManager();
        }
        return inst;
    }

    public IMFileClientManager() {

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

    }

    void startClient() {
        if (fileClientThread != null) {
            fileClientThread.closeConnect();
            fileClientThread = null;
        }
        fileClientThread = new ClientThread(new ClientFileHandler());
        fileClientThread.setOnClientListener(this);
        fileClientThread.setHost(this.host);
        fileClientThread.setPort(this.port);
        fileClientThread.start();
    }


    public void sendMessage(short serviceId, short commandId, GeneratedMessage msg) {
        try {
            Header header = new Header();
            header.setCommandId(commandId);
            header.setServiceId(serviceId);
            header.setLength(SysConstant.HEADER_LENGTH + msg.getSerializedSize());
            fileClientThread.sendMessage(header, msg);
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
                IMFileManager.getInstance().continueSendFile(header, byteBuf);
                break;

        }
    }

    @Override
    public void connectSuccess() {

    }

    @Override
    public void connectFailure() {
        EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.CONNECT_FAILE));
    }
}
