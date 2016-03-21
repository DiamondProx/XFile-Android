package com.huangjiang.message;

import com.huangjiang.manager.IMMessageClientManager;
import com.huangjiang.manager.event.ConnectSuccessEvent;
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
        if (ctx.channel().isWritable() && ctx.channel().isWritable()) {
            ConnectSuccessEvent event = new ConnectSuccessEvent();
            event.setIpAddress(ctx.channel().remoteAddress().toString());
            event.setPort(0);
            EventBus.getDefault().post(event);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            IMMessageClientManager.getInstance().packetDispatch((ByteBuf) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.e(cause.getMessage());
    }
}
