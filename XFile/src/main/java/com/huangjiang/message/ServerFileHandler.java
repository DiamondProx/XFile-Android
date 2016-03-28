package com.huangjiang.message;

import com.huangjiang.manager.IMFileServerManager;
import com.huangjiang.manager.IMMessageServerManager;
import com.huangjiang.manager.event.ServerFileSocketEvent;
import com.huangjiang.manager.event.SocketEvent;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 文件消息处理
 */
public class ServerFileHandler extends ChannelHandlerAdapter {

    private Logger logger = Logger.getLogger(ServerMessageHandler.class);


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.e("****ServerFileExceptionCaught");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        IMFileServerManager imFileServerManager = IMFileServerManager.getInstance();
        if (imFileServerManager.getAuthChannelHandlerContext() != null) {
            // 连接已经认证,判断是否是认证连接
            if (ctx.channel().id() == imFileServerManager.getAuthChannelHandlerContext().channel().id()) {
                // 分发认证数据
                imFileServerManager.packetDispatch((ByteBuf) msg);
            }
        } else {
            // 分发认证数据
            imFileServerManager.packetDispatchAuth(ctx, (ByteBuf) msg);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.e(cause.getMessage());
        logger.e("****ServerFileExceptionCaught");

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
        IMFileServerManager imFileServerManager = IMFileServerManager.getInstance();
        if (imFileServerManager.getAuthChannelHandlerContext() != null && ctx.channel().id().equals(imFileServerManager.getAuthChannelHandlerContext().channel().id())) {
//            EventBus.getDefault().post(new ServerFileSocketEvent(SocketEvent.CONNECT_CLOSE));
        }
        logger.e("****ServerFileChannelReadComplete");
    }
}
