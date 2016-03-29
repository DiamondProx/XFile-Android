package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.callback.MessageServerListenerQueue;
import com.huangjiang.manager.callback.Packetlistener;
import com.huangjiang.message.XFileChannelInitializer;
import com.huangjiang.message.ServerThread;
import com.huangjiang.message.base.DataBuffer;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息服务管理
 */
public class IMServerMessageManager extends IMBaseManager {

    private Logger logger = Logger.getLogger(IMServerMessageManager.class);

    private static IMServerMessageManager inst = null;

    private ServerThread messageServerThread = null;

    private MessageServerListenerQueue listenerQueue = MessageServerListenerQueue.instance();


    /*
     * 认证连接
     */
    private ChannelHandlerContext AuthChannelHandlerContext = null;

    public static IMServerMessageManager getInstance() {
        if (inst == null) {
            inst = new IMServerMessageManager();
        }
        return inst;
    }

    public IMServerMessageManager() {

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
        listenerQueue.onStart();
        messageServerThread = new ServerThread(SysConstant.MESSAGE_PORT, XFileChannelInitializer.InitialType.SERVERMESSAGEHANDLER);
        messageServerThread.start();
    }

    void stopServer() {
        if (messageServerThread != null) {
            messageServerThread.stopServer();
            messageServerThread = null;
            listenerQueue.onDestory();
        }
        if (AuthChannelHandlerContext != null) {
            AuthChannelHandlerContext.close();
            AuthChannelHandlerContext = null;
        }
    }

    /**
     * 处理普通消息
     */
    public void packetDispatch(ByteBuf byteBuf) throws InvalidProtocolBufferException {
        DataBuffer dataBuffer = new DataBuffer(byteBuf);
        Header header = dataBuffer.getHeader();
        int commandId = header.getCommandId();
        switch (commandId) {
            case SysConstant.CMD_SEND_MESSAGE:
                recvMessage(byteBuf, header);
                break;

        }
    }

    /**
     * 处理认证消息
     */
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
                    // 验证服务端是否需要验证,是否需要输入密码
                    if (SysConstant.VERIFY) {
                        // 提示客户端输入密码
                        XFileProtocol.ShakeHand.Builder rspShakeHand = XFileProtocol.ShakeHand.newBuilder();
                        rspShakeHand.setStep(1);
                        rspShakeHand.setVerify(true);
                        short sid = SysConstant.SERVICE_DEFAULT;
                        short cid = SysConstant.CMD_SHAKE_HAND;
                        sendMessage(ctx, sid, cid, rspShakeHand.build(), null, reqSeqnum);
                    } else {
                        // 保存认证的连接
                        this.setAuthChannelHandlerContext(ctx);
                        // 提示客户端连接成功
                        XFileProtocol.ShakeHand.Builder rspShakeHand = XFileProtocol.ShakeHand.newBuilder();
                        rspShakeHand.setStep(1);
                        rspShakeHand.setVerify(false);
                        rspShakeHand.setToken(SysConstant.TOKEN);
                        short sid = SysConstant.SERVICE_DEFAULT;
                        short cid = SysConstant.CMD_SHAKE_HAND;
                        logger.e("****ServerMessageResponse1111");
                        sendMessage(ctx, sid, cid, rspShakeHand.build(), null, reqSeqnum);
                        logger.e("****ServerMessageResponse2222");

                    }
                    break;
                case 2:
                    String reqPassword = shakeHand.getPassword();
                    if (SysConstant.PASS_WORD.equals(reqPassword)) {
                        // 保存认证的连接
                        this.setAuthChannelHandlerContext(ctx);
                        // 生成服务器生成服务器token通知客户端
                        IMServerFileManager.getInstance().setToken(SysConstant.TOKEN);
                        // 发送消息提示客户端
                        XFileProtocol.ShakeHand.Builder rspShakeHand = XFileProtocol.ShakeHand.newBuilder();
                        rspShakeHand.setStep(2);
                        rspShakeHand.setResult(true);
                        rspShakeHand.setToken(SysConstant.TOKEN);
                        short sid = SysConstant.SERVICE_DEFAULT;
                        short cid = SysConstant.CMD_SHAKE_HAND;
                        sendMessage(sid, cid, rspShakeHand.build());
                    } else {
                        // 认证失败
                        XFileProtocol.ShakeHand.Builder rspShakeHand = XFileProtocol.ShakeHand.newBuilder();
                        rspShakeHand.setStep(2);
                        rspShakeHand.setResult(false);
                        short sid = SysConstant.SERVICE_DEFAULT;
                        short cid = SysConstant.CMD_SHAKE_HAND;
                        sendMessage(ctx, sid, cid, rspShakeHand.build(), null, reqSeqnum);
                        ctx.close();
                    }
                    break;
            }


        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }

    }

    public void sendMessage(short serviceId, short commandId, GeneratedMessage msg) {
        sendMessage(serviceId, commandId, msg, null, (short) 0);
    }

    public void sendMessage(short serviceId, short commandId, GeneratedMessage msg, Packetlistener packetlistener, short seqnum) {
        sendMessage(AuthChannelHandlerContext, serviceId, commandId, msg, packetlistener, seqnum);
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


    public ChannelHandlerContext getAuthChannelHandlerContext() {
        return AuthChannelHandlerContext;
    }

    public void setAuthChannelHandlerContext(ChannelHandlerContext authChannelHandlerContext) {
        AuthChannelHandlerContext = authChannelHandlerContext;
    }
}
