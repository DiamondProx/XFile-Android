package com.huangjiang.manager;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.callback.FileClientListenerQueue;
import com.huangjiang.manager.callback.Packetlistener;
import com.huangjiang.manager.event.ClientFileSocketEvent;
import com.huangjiang.manager.event.SocketEvent;
import com.huangjiang.message.ClientThread;
import com.huangjiang.message.XFileChannelInitializer;
import com.huangjiang.message.base.DataBuffer;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 文件管理客户端
 */
public class IMClientFileManager extends IMBaseManager implements ClientThread.OnClientListener {

    private Logger logger = Logger.getLogger(IMClientMessageManager.class);


    private static IMClientFileManager inst = null;

    private ClientThread fileClientThread = null;

    private String host;

    private int port;

    private String token;

    private FileClientListenerQueue listenerQueue = FileClientListenerQueue.instance();

    /**
     * 心跳处理
     */
    Timer timer = null;

    /**
     * 心跳任务
     */
    TimerTask timerTask = null;

    public static IMClientFileManager getInstance() {
        if (inst == null) {
            inst = new IMClientFileManager();
        }
        return inst;
    }

    public IMClientFileManager() {

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
        fileClientThread = new ClientThread(XFileChannelInitializer.InitialType.CLIENTFILEHANDLER);
        fileClientThread.setOnClientListener(this);
        fileClientThread.setHost(this.host);
        fileClientThread.setPort(this.port);
        fileClientThread.start();
        startHeart();
    }

    void stopClient() {
        if (fileClientThread != null) {
            fileClientThread.closeConnect();
            fileClientThread = null;
            listenerQueue.onDestory();
        }
        cancelHeart();
    }


    public void sendMessage(short serviceId, short commandId, GeneratedMessage msg) {
        sendMessage(serviceId, commandId, msg, (short) 0);
    }

    public void sendMessage(short serviceId, short commandId, GeneratedMessage msg, short seqnum) {
        sendMessage(serviceId, commandId, msg, null, seqnum);
    }

    public void sendMessage(short serviceId, short commandId, GeneratedMessage msg, Packetlistener packetlistener, short seqnum) {
        if (fileClientThread != null) {
            Header header = new Header();
            header.setCommandId(commandId);
            header.setServiceId(serviceId);
            header.setLength(SysConstant.HEADER_LENGTH + msg.getSerializedSize());
            if (seqnum != 0) {
                header.setSeqnum(seqnum);
            }
            if (packetlistener != null && seqnum == 0) {
                short reqSeqnum = header.getSeqnum();
                listenerQueue.push(reqSeqnum, packetlistener);
            }
            fileClientThread.sendMessage(header, msg);
        }
    }

    public void packetDispatch(ChannelHandlerContext ctx, ByteBuf byteBuf) throws InvalidProtocolBufferException {
        DataBuffer dataBuffer = new DataBuffer(byteBuf);
        Header header = dataBuffer.getHeader();
        byte[] body = dataBuffer.getBodyData();
        int commandId = header.getCommandId();
        short serviceId = header.getServiceId();
        Packetlistener packetlistener = listenerQueue.pop(header.getSeqnum());
        // logger.e("****ClientFilePacketDispatch1111");
        if (packetlistener != null) {
            // logger.e("****ClientFilePacketDispatch2222");
            packetlistener.onSuccess(serviceId, body);
        }
        switch (commandId) {
            case SysConstant.CMD_FILE_SET:
                IMFileManager.getInstance().dispatchMessage(header, body);
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


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


    public void shakeHand(final ChannelHandlerContext ctx) {
        XFileProtocol.ShakeHand.Builder shakeHand = XFileProtocol.ShakeHand.newBuilder();
        shakeHand.setStep(1);// 直接验证token
        shakeHand.setToken(this.token);
        shakeHand.setDeviceName(android.os.Build.MODEL);
        short cid = SysConstant.CMD_SHAKE_HAND;
        short sid = SysConstant.SERVICE_DEFAULT;
        sendMessage(sid, cid, shakeHand.build(), new Packetlistener() {
            @Override
            public void onSuccess(short serviceId, Object response) {
                if (response == null) {
                    return;
                }
                byte[] rsp = (byte[]) response;
                try {
                    XFileProtocol.ShakeHand rspShakeHand = XFileProtocol.ShakeHand.parseFrom(rsp);
                    if (rspShakeHand.getStep() == 1 && rspShakeHand.getResult()) {
                        // 服务器文件端口连接成功
                        ClientFileSocketEvent event = new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_SUCCESS);
                        event.setDevice_name(rspShakeHand.getDeviceName());
                        EventBus.getDefault().post(event);
                    } else if (rspShakeHand.getStep() == 1 && !rspShakeHand.getResult()) {
                        // 连接失败
                        ctx.close();
                        EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_FAILE));
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFaild() {
                ctx.close();
                EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_FAILE));
            }

            @Override
            public void onTimeout() {
                ctx.close();
                EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SHAKE_HAND_FAILE));
            }
        }, (short) 0);


    }

    /**
     * 心跳
     */
    public void startHeart() {
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                XFileProtocol.Heart.Builder builder = XFileProtocol.Heart.newBuilder();
                builder.setContent("f");
                short cid = SysConstant.CMD_HEART;
                short sid = SysConstant.SERVICE_DEFAULT;
                sendMessage(sid, cid, builder.build());
            }
        };
        timer.schedule(timerTask, SysConstant.HEART_TIME, SysConstant.HEART_TIME);

    }

    /**
     * 取消心跳
     */
    public void cancelHeart() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }
}
