package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.callback.FileServerListenerQueue;
import com.huangjiang.manager.callback.Packetlistener;
import com.huangjiang.manager.event.ServerMessageSocketEvent;
import com.huangjiang.manager.event.SocketEvent;
import com.huangjiang.message.XFileChannelInitializer;
import com.huangjiang.message.ServerThread;
import com.huangjiang.message.base.DataBuffer;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 文件管理服务端
 */
public class IMFileServerManager extends IMManager {

    private Logger logger = Logger.getLogger(IMMessageServerManager.class);

    private static IMFileServerManager inst = null;

    private ServerThread messageServerThread = null;

    private String token;

    private FileServerListenerQueue listenerQueue = FileServerListenerQueue.instance();

    /*
    * 认证连接
    */
    private ChannelHandlerContext AuthChannelHandlerContext = null;

    public static IMFileServerManager getInstance() {
        if (inst == null) {
            inst = new IMFileServerManager();
        }
        return inst;
    }

    public IMFileServerManager() {

    }

    @Override
    public void start() {
        startFileServer();
    }

    @Override
    public void stop() {
        stopFileServer();
    }

    void startFileServer() {
        stopFileServer();
        listenerQueue.onStart();
        messageServerThread = new ServerThread(SysConstant.FILE_SERVER_PORT, XFileChannelInitializer.InitialType.SERVERFILEHANDLER);
        messageServerThread.start();
    }

    void stopFileServer() {
        if (messageServerThread != null) {
            messageServerThread.stopServer();
            messageServerThread = null;
            listenerQueue.onDestory();
        }
    }

    public void packetDispatch(ByteBuf byteBuf) throws InvalidProtocolBufferException {
        DataBuffer dataBuffer = new DataBuffer(byteBuf);
        Header header = dataBuffer.getHeader();
        int commandId = header.getCommandId();
        switch (commandId) {
            case SysConstant.CMD_SEND_MESSAGE:

                break;

        }
    }


    public void packetDispatchAuth(ChannelHandlerContext ctx, ByteBuf msg) {
        DataBuffer dataBuffer = new DataBuffer(msg);
        Header header = dataBuffer.getHeader();
        byte[] body = dataBuffer.getBodyData();
        short commandId = header.getCommandId();
        switch (commandId) {
            case SysConstant.CMD_SHAKE_HAND:
                ShakeHand(ctx, body, header.getSeqnum());
                break;
        }
    }


    public void sendMessage(short serviceId, short commandId, GeneratedMessage msg) {
        sendMessage(AuthChannelHandlerContext, serviceId, commandId, msg, null, (short) 0);
    }

    public void sendMessage(ChannelHandlerContext ctx, short serviceId, short commandId, GeneratedMessage msg, Packetlistener packetlistener, short seqnum) {
        if (ctx != null) {
            Header header = new Header();
            header.setCommandId(commandId);
            header.setServiceId(serviceId);
            if (seqnum != 0) {
                header.setSeqnum(seqnum);
            }
            if (packetlistener != null && seqnum != 0) {
                short reqSeqnum = header.getSeqnum();
                listenerQueue.push(reqSeqnum, packetlistener);
            }
            header.setLength(SysConstant.HEADER_LENGTH + msg.getSerializedSize());
            messageServerThread.sendMessage(ctx, header, msg);
        }
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public ChannelHandlerContext getAuthChannelHandlerContext() {
        return AuthChannelHandlerContext;
    }

    public void setAuthChannelHandlerContext(ChannelHandlerContext authChannelHandlerContext) {
        AuthChannelHandlerContext = authChannelHandlerContext;
    }

    /**
     * 处理连接认证
     *
     * @param ctx
     * @param bodyData
     */
    void ShakeHand(ChannelHandlerContext ctx, byte[] bodyData, short reqSeqnum) {
        try {
            XFileProtocol.ShakeHand shakeHand = XFileProtocol.ShakeHand.parseFrom(bodyData);
            int step = shakeHand.getStep();

            switch (step) {
                case 1:
                    // 直接对比token是否正确
                    String token = shakeHand.getToken();
                    if (SysConstant.TOKEN.equals(token)) {
                        // 保存认证的连接
                        this.setAuthChannelHandlerContext(ctx);
                        // 连接成功
                        XFileProtocol.ShakeHand.Builder rspShakeHand = XFileProtocol.ShakeHand.newBuilder();
                        rspShakeHand.setStep(1);
                        rspShakeHand.setResult(true);
                        short sid = SysConstant.SERVICE_DEFAULT;
                        short cid = SysConstant.CMD_SHAKE_HAND;
                        sendMessage(ctx, sid, cid, rspShakeHand.build(), null, reqSeqnum);
                        EventBus.getDefault().post(new ServerMessageSocketEvent(SocketEvent.SHAKE_HAND_SUCCESS));
                    } else {
                        // 连接失败
                        XFileProtocol.ShakeHand.Builder rspShakeHand = XFileProtocol.ShakeHand.newBuilder();
                        rspShakeHand.setStep(1);
                        rspShakeHand.setResult(false);
                        short sid = SysConstant.SERVICE_DEFAULT;
                        short cid = SysConstant.CMD_SHAKE_HAND;
                        sendMessage(ctx, sid, cid, rspShakeHand.build(), null, reqSeqnum);
                    }
                    break;
            }


        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }
}
