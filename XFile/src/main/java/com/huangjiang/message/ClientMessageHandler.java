package com.huangjiang.message;

import com.huangjiang.manager.IMClientMessageManager;
import com.huangjiang.manager.event.ClientMessageSocketEvent;
import com.huangjiang.manager.event.SocketEvent;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息处理
 */
public class ClientMessageHandler extends ChannelHandlerAdapter {

    private Logger logger = Logger.getLogger(ClientMessageHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.e("****ClientMessageChannelActive");
        // 服务端需要初始化Handler，等到200毫秒
        Thread.sleep(200);
        IMClientMessageManager.getInstance().sendShakeHand(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.e("****ClientMessageChannelRead");
        if (msg instanceof ByteBuf)
            IMClientMessageManager.getInstance().packetDispatch((ByteBuf) msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        EventBus.getDefault().post(new ClientMessageSocketEvent(SocketEvent.SOCKET_ERROR));
        logger.e(cause.getMessage());
        logger.e("****ClientMessageExceptionCaught");
    }

}
