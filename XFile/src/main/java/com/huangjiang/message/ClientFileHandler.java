package com.huangjiang.message;

import com.huangjiang.manager.IMClientFileManager;
import com.huangjiang.manager.event.ClientFileSocketEvent;
import com.huangjiang.manager.event.SocketEvent;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 客户端文件处理
 */
public class ClientFileHandler extends ChannelHandlerAdapter {

    private Logger logger = Logger.getLogger(ClientFileHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.e("****ClientFileChannelActive");
        // 服务端需要初始化Handler，等到200毫秒
        Thread.sleep(200);
        IMClientFileManager.getInstance().shakeHand(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            IMClientFileManager.getInstance().packetDispatch(ctx, (ByteBuf) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.SOCKET_ERROR));
        logger.e(cause.getMessage());
        logger.e("****ClientFileExceptionCaught");
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
//        EventBus.getDefault().post(new ClientFileSocketEvent(SocketEvent.CONNECT_CLOSE));
//        logger.e("****ClientFileChannelReadComplete");
    }
}
