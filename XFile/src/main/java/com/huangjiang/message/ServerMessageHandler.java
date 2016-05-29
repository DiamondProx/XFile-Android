package com.huangjiang.message;

import com.huangjiang.XFileApp;
import com.huangjiang.business.model.LinkType;
import com.huangjiang.manager.IMServerMessageManager;
import com.huangjiang.manager.event.ServerFileSocketEvent;
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
        logger.e("****ServerMessage-ChannelActive");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.e("****ServerMessage-ChannelRead");
        IMServerMessageManager imServerMessageManager = IMServerMessageManager.getInstance();
        if (imServerMessageManager.getAuthChannelHandlerContext() != null) {
            // 连接已经认证,判断是否是认证连接
            if (ctx.channel().id() == imServerMessageManager.getAuthChannelHandlerContext().channel().id()) {
                // 分发认证数据
                imServerMessageManager.packetDispatch((ByteBuf) msg);
            }
        } else if (XFileApp.mLinkType == LinkType.NONE) {
            // 分发认证数据
            logger.e("****ServerMessage-DispatchAuth");
            imServerMessageManager.packetDispatchAuth(ctx, (ByteBuf) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
        logger.e(cause.getMessage());
        logger.e("****ServerMessage-ExceptionCaught");
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);
        logger.e("****ServerMessage-HandlerAdded");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        IMServerMessageManager imServerMessageManager = IMServerMessageManager.getInstance();
        logger.e("****ServerMessage-HandlerRemoved1111");
        if (imServerMessageManager.getAuthChannelHandlerContext() != null && ctx.channel().id().equals(imServerMessageManager.getAuthChannelHandlerContext().channel().id())) {
            imServerMessageManager.setAuthChannelHandlerContext(null);
            EventBus.getDefault().post(new ServerFileSocketEvent(SocketEvent.CONNECT_CLOSE));
            logger.e("****ServerMessage-HandlerRemoved2222");
        }
    }
}
