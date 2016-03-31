package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.callback.FileServerListenerQueue;
import com.huangjiang.manager.callback.Packetlistener;
import com.huangjiang.manager.event.ServerFileSocketEvent;
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
public class IMServerFileManager extends IMBaseManager {

    private Logger logger = Logger.getLogger(IMServerMessageManager.class);

    private static IMServerFileManager inst = null;

    private ServerThread messageServerThread = null;

    private String token;

    private FileServerListenerQueue listenerQueue = FileServerListenerQueue.instance();

    /*
    * 认证连接
    */
    private ChannelHandlerContext AuthChannelHandlerContext = null;

    public static IMServerFileManager getInstance() {
        if (inst == null) {
            inst = new IMServerFileManager();
        }
        return inst;
    }

    public IMServerFileManager() {

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
        byte[] body = dataBuffer.getBodyData();
        short commandId = header.getCommandId();
        short serviceId = header.getServiceId();
        Packetlistener packetlistener = listenerQueue.pop(header.getSeqnum());
//        logger.e("****ServerFilePacketDispatch1111");
        if (packetlistener != null) {
//            logger.e("****ServerFilePacketDispatch2222");
            packetlistener.onSuccess(serviceId, body);
        }
        switch (commandId) {
            case SysConstant.CMD_SEND_MESSAGE:

                break;
            case SysConstant.CMD_FILE_SET:
                IMFileManager.getInstance().dispatchMessage(header, body);
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
                logger.e("****ServerMessage-DispatchShakeHand");
                ShakeHand(ctx, body, header.getSeqnum());
                break;
        }
    }


    public void sendMessage(short serviceId, short commandId, GeneratedMessage msg) {
        sendMessage(AuthChannelHandlerContext, serviceId, commandId, msg, null, (short) 0);
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
            if (packetlistener != null && seqnum == 0) {
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
                        logger.e("****ServerFile-Response1111");
                        // 保存认证的连接
                        this.setAuthChannelHandlerContext(ctx);
                        // 连接成功
                        XFileProtocol.ShakeHand.Builder rspShakeHand = XFileProtocol.ShakeHand.newBuilder();
                        rspShakeHand.setStep(1);
                        rspShakeHand.setResult(true);
                        rspShakeHand.setDeviceName(android.os.Build.MODEL);
                        short sid = SysConstant.SERVICE_DEFAULT;
                        short cid = SysConstant.CMD_SHAKE_HAND;
                        sendMessage(ctx, sid, cid, rspShakeHand.build(), null, reqSeqnum);
                        ServerFileSocketEvent event = new ServerFileSocketEvent(SocketEvent.SHAKE_HAND_SUCCESS);
                        event.setDevice_name(shakeHand.getDeviceName());
                        EventBus.getDefault().post(event);
                        logger.e("****ServerFile-Response2222");
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
