package com.huangjiang.message;

import com.huangjiang.manager.IMMessageServerManager;
import com.huangjiang.manager.event.ServerMessageSocketEvent;
import com.huangjiang.manager.event.SocketEvent;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;

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
        logger.e("****ServerMessageChannelActive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.e("****ServerMessageChannelRead");
        IMMessageServerManager imMessageServerManager = IMMessageServerManager.getInstance();
        if (imMessageServerManager.getAuthChannelHandlerContext() != null) {
            // 连接已经认证,判断是否是认证连接
            if (ctx.channel().id() == imMessageServerManager.getAuthChannelHandlerContext().channel().id()) {
                // 分发认证数据
                imMessageServerManager.packetDispatch((ByteBuf) msg);
            }
        } else {
            // 分发认证数据
            imMessageServerManager.packetDispatchAuth(ctx, (ByteBuf) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
        logger.e(cause.getMessage());
        logger.e("****ServerMessageExceptionCaught");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        logger.e("****ServerMessageChannelReadComplete");
        IMMessageServerManager imMessageServerManager = IMMessageServerManager.getInstance();
        if (imMessageServerManager.getAuthChannelHandlerContext() != null && ctx.channel().id().equals(imMessageServerManager.getAuthChannelHandlerContext().channel().id())) {
//            EventBus.getDefault().post(new ServerMessageSocketEvent(SocketEvent.CONNECT_CLOSE));
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        logger.e("****ServerMessageHandlerAdded");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        IMMessageServerManager imMessageServerManager = IMMessageServerManager.getInstance();
        logger.e("****ServerMessageHandlerRemoved1111");
        if (imMessageServerManager.getAuthChannelHandlerContext() != null && ctx.channel().id().equals(imMessageServerManager.getAuthChannelHandlerContext().channel().id())) {
//            EventBus.getDefault().post(new ServerMessageSocketEvent(SocketEvent.CONNECT_CLOSE));
            logger.e("****ServerMessageHandlerRemoved2222");
        }
    }
}
