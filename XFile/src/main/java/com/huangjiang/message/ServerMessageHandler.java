package com.huangjiang.message;

import com.huangjiang.manager.IMMessageServerManager;
import com.huangjiang.utils.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息处理
 */
public class ServerMessageHandler extends ChannelHandlerAdapter {

    private Logger logger = Logger.getLogger(ServerMessageHandler.class);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg != null && msg instanceof ByteBuf && IMMessageServerManager.getInstance() != null) {
            IMMessageServerManager.getInstance().packetDispatch((ByteBuf) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.e(cause.getMessage());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }
}
