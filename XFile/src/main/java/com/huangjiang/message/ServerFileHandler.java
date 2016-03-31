package com.huangjiang.message;

import com.huangjiang.manager.IMServerFileManager;
import com.huangjiang.manager.IMServerMessageManager;
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
        logger.e("****ServerFile-ChannelActive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        logger.e("****ServerFile-ChannelRead");
        IMServerFileManager imServerFileManager = IMServerFileManager.getInstance();
        if (imServerFileManager.getAuthChannelHandlerContext() != null) {
            // 连接已经认证,判断是否是认证连接
            if (ctx.channel().id() == imServerFileManager.getAuthChannelHandlerContext().channel().id()) {
                // 分发认证数据
                imServerFileManager.packetDispatch((ByteBuf) msg);
            }
        } else {
            // 分发认证数据
            logger.e("****ServerFile-DispatchAuth");
            imServerFileManager.packetDispatchAuth(ctx, (ByteBuf) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.e(cause.getMessage());
        logger.e("****ServerFile-ExceptionCaught");

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
//        logger.e("****ServerFile-ChannelReadComplete");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        IMServerFileManager imServerFileManager = IMServerFileManager.getInstance();
        if (imServerFileManager.getAuthChannelHandlerContext() != null && ctx.channel().id().equals(imServerFileManager.getAuthChannelHandlerContext().channel().id())) {
            imServerFileManager.setAuthChannelHandlerContext(null);
            if (IMServerMessageManager.getInstance().getAuthChannelHandlerContext() != null) {
                IMServerMessageManager.getInstance().getAuthChannelHandlerContext().close();
            }
            EventBus.getDefault().post(new ServerFileSocketEvent(SocketEvent.CONNECT_CLOSE));
        }

        logger.e("****ServerFile-HandlerRemoved");
    }
}
