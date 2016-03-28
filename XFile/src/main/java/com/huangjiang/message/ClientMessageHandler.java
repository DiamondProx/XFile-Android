package com.huangjiang.message;

import com.huangjiang.manager.IMMessageClientManager;
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
        // 请求确认连接
        logger.e("****ClientMessageChannelActive-sendSuccess1111");
        IMMessageClientManager.getInstance().sendShakeHand(ctx);
        logger.e("****ClientMessageChannelActive-sendSuccess2222");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.e("****ClientMessageChannelRead");
        if (msg instanceof ByteBuf) {
            IMMessageClientManager.getInstance().packetDispatch((ByteBuf) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        EventBus.getDefault().post(new ClientMessageSocketEvent(SocketEvent.SOCKET_ERROR));
        logger.e(cause.getMessage());
        logger.e("****ClientMessageExceptionCaught");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        //EventBus.getDefault().post(new ClientMessageSocketEvent(SocketEvent.CONNECT_CLOSE));
        logger.e("****ClientMessageChannelReadComplete");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        logger.e("****ClientMessageHandlerRemoved");
        //            EventBus.getDefault().post(new ServerMessageSocketEvent(SocketEvent.CONNECT_CLOSE));
    }
}
