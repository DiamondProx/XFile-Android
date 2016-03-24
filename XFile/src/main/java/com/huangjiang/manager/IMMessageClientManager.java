package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.callback.MessageClientListenerQueue;
import com.huangjiang.manager.callback.Packetlistener;
import com.huangjiang.manager.event.ClientFileSocketEvent;
import com.huangjiang.manager.event.ClientMessageSocketEvent;
import com.huangjiang.manager.event.SocketEvent;
import com.huangjiang.message.ClientFileHandler;
import com.huangjiang.message.ClientMessageHandler;
import com.huangjiang.message.ClientThread;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
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

    private MessageClientListenerQueue listenerQueue = MessageClientListenerQueue.instance();

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
            listenerQueue.onDestory();
        }
    }

    void startClient() {
        if (messageClientThread != null) {
            messageClientThread.closeConnect();
            messageClientThread = null;
            listenerQueue.onDestory();
        }
        listenerQueue.onStart();
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
        ByteBuf readBuf = byteBuf.readBytes(header.getLength() - SysConstant.HEADER_LENGTH);
        byte[] rspData = readBuf.array();
        Packetlistener packetlistener = listenerQueue.pop(header.getSeqnum());
        if (packetlistener != null) {
            packetlistener.onSuccess(rspData);
        }
        switch (commandId) {
            case SysConstant.CMD_SEND_MESSAGE:

                break;

        }
    }

    public void sendMessage(short serviceId, short commandId, GeneratedMessage msg, Packetlistener packetlistener) {
        if (messageClientThread != null) {
            Header header = new Header();
            header.setCommandId(commandId);
            header.setServiceId(serviceId);
            short seqnum = header.getSeqnum();
            listenerQueue.push(seqnum, packetlistener);
            header.setLength(SysConstant.HEADER_LENGTH + msg.getSerializedSize());
            messageClientThread.sendMessage(header, msg);
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
        XFileProtocol.ShakeHand.Builder shakeHand = XFileProtocol.ShakeHand.newBuilder();
        shakeHand.setStep(1);// 握手第一步
        short cid = SysConstant.CMD_SHAKE_HAND;
        short sid = SysConstant.SERVICE_DEFAULT;
        sendMessage(sid, cid, shakeHand.build(), new Packetlistener() {
            @Override
            public void onSuccess(Object response) {
                byte[] rsp = (byte[]) response;
                try {
                    XFileProtocol.ShakeHand shakeHand = XFileProtocol.ShakeHand.parseFrom(rsp);
                    if (shakeHand.getStep() == 1 && !shakeHand.getVerify()) {
                        // 不需要密码直接连接成功,继续连接文件服务器
                        EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_SETP1_SUCCESS));
                    } else if (shakeHand.getStep() == 1 && shakeHand.getVerify()) {
                        // 需要密码，请求输入密码
                        EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_INPUT_PASSWORD));
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFaild() {
                EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_FAILE));
            }

            @Override
            public void onTimeout() {
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
