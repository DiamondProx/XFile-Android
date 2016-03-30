package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.callback.MessageClientListenerQueue;
import com.huangjiang.manager.callback.Packetlistener;
import com.huangjiang.manager.event.ClientFileSocketEvent;
import com.huangjiang.manager.event.ClientMessageSocketEvent;
import com.huangjiang.manager.event.SocketEvent;
import com.huangjiang.message.ClientThread;
import com.huangjiang.message.XFileChannelInitializer;
import com.huangjiang.message.base.DataBuffer;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息客户端管理
 */
public class IMClientMessageManager extends IMBaseManager implements ClientThread.OnClientListener {


    private Logger logger = Logger.getLogger(IMClientMessageManager.class);

    private static IMClientMessageManager inst = null;

    private ClientThread messageClientThread = null;

    private String host;

    private int port;

    private MessageClientListenerQueue listenerQueue = MessageClientListenerQueue.instance();

    private boolean verify = false;

    public static IMClientMessageManager getInstance() {
        if (inst == null) {
            inst = new IMClientMessageManager();
        }
        return inst;
    }

    public IMClientMessageManager() {

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
        stopClient();
    }

    void startClient() {
        stopClient();
        listenerQueue.onStart();
        messageClientThread = new ClientThread(XFileChannelInitializer.InitialType.CLIENTMESSAGEHANDLER);
        messageClientThread.setOnClientListener(this);
        messageClientThread.setHost(this.host);
        messageClientThread.setPort(this.port);
        messageClientThread.start();
    }

    void stopClient() {
        if (messageClientThread != null) {
            messageClientThread.closeConnect();
            messageClientThread = null;
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
        logger.e("****ClientMessagePacketDispatch1111");
        if (packetlistener != null) {
            logger.e("****ClientMessagePacketDispatch2222");
            packetlistener.onSuccess(serviceId, body);
        }
        switch (commandId) {
            case SysConstant.CMD_SEND_MESSAGE:

                break;
        }
    }


    public void sendMessage(short serviceId, short commandId, GeneratedMessage msg) {
        sendMessage(serviceId, commandId, msg, null);
    }

    public void sendMessage(short serviceId, short commandId, GeneratedMessage msg, Packetlistener packetlistener) {
        if (messageClientThread != null) {
            Header header = new Header();
            header.setCommandId(commandId);
            header.setServiceId(serviceId);
            if (packetlistener != null) {
                short reqSeqnum = header.getSeqnum();
                listenerQueue.push(reqSeqnum, packetlistener);
            }
            header.setLength(SysConstant.HEADER_LENGTH + msg.getSerializedSize());
            messageClientThread.sendMessage(header, msg);
        }
    }


    public void sendShakeHand(final ChannelHandlerContext ctx) {
        XFileProtocol.ShakeHand.Builder shakeHand = XFileProtocol.ShakeHand.newBuilder();
        shakeHand.setStep(1);// 握手第一步
        short sid = SysConstant.SERVICE_DEFAULT;
        short cid = SysConstant.CMD_SHAKE_HAND;
        sendMessage(sid, cid, shakeHand.build(), new Packetlistener(10000) {
            @Override
            public void onSuccess(short serviceId, Object response) {
                logger.e("****SendShakeHandOnResponse");
                if (response == null) {
                    return;
                }
                byte[] rsp = (byte[]) response;
                try {
                    XFileProtocol.ShakeHand shakeHandRsp = XFileProtocol.ShakeHand.parseFrom(rsp);
                    if (shakeHandRsp.getStep() == 1 && !shakeHandRsp.getVerify()) {
                        // 不需要密码直接连接成功,但是要获取token,用token继续连接文件服务器
                        String token = shakeHandRsp.getToken();
                        IMClientFileManager imClientFileManager = IMClientFileManager.getInstance();
                        imClientFileManager.setHost(host);
                        // TODO 获取文件端口
                        imClientFileManager.setPort(SysConstant.FILE_SERVER_PORT);
                        imClientFileManager.setToken(token);
                        imClientFileManager.startClient();
                        verify = true;
                    } else if (shakeHandRsp.getStep() == 1 && shakeHandRsp.getVerify()) {
                        // 需要密码，请求输入密码
                        EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_INPUT_PASSWORD));
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_FAILE));
                }
            }

            @Override
            public void onFaild() {
                ctx.close();
                logger.e("****SendShakeHandOnFaild");
                EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_FAILE));
            }

            @Override
            public void onTimeout() {
                logger.e("****SendShakeHandOnTimeout");
                ctx.close();
                EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_FAILE));
            }
        });
        logger.e("****SendShakeHand****");
    }

    public void sendShakeHandStepT(String password) {
        XFileProtocol.ShakeHand.Builder shakeHand = XFileProtocol.ShakeHand.newBuilder();
        shakeHand.setStep(2);// 握手第二步
        shakeHand.setPassword(password);
        short sid = SysConstant.SERVICE_DEFAULT;
        short cid = SysConstant.CMD_SHAKE_HAND;
        sendMessage(sid, cid, shakeHand.build(), new Packetlistener() {
            @Override
            public void onSuccess(short serviceId,Object response) {
                if (response == null) {
                    return;
                }
                byte[] rsp = (byte[]) response;
                try {
                    XFileProtocol.ShakeHand shakeHandRsp = XFileProtocol.ShakeHand.parseFrom(rsp);
                    if (shakeHandRsp.getStep() == 2 && shakeHandRsp.getResult()) {
                        // 验证成功,用token连接服务器
                        String token = shakeHandRsp.getToken();
                        IMClientFileManager imClientFileManager = IMClientFileManager.getInstance();
                        imClientFileManager.setHost(host);
                        // TODO 获取文件端口
                        imClientFileManager.setPort(SysConstant.FILE_SERVER_PORT);
                        imClientFileManager.setToken(token);
                        imClientFileManager.startClient();
                        verify = true;
                    } else if (shakeHandRsp.getStep() == 2 && !shakeHandRsp.getResult()) {
                        // 密码验证失败
                        EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_FAILE));
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                    EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_FAILE));
                }
            }

            @Override
            public void onFaild() {
                stopClient();
                EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_FAILE));
            }

            @Override
            public void onTimeout() {
                stopClient();
                EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_FAILE));
            }
        });
    }


    @Override
    public void connectSuccess() {

    }

    @Override
    public void connectFailure() {
        EventBus.getDefault().post(new ClientMessageSocketEvent(SocketEvent.CONNECT_FAILE));
    }
}
