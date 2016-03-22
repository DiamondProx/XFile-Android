package com.huangjiang.message;

import android.os.Environment;

import com.google.protobuf.ByteString;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.IMFileServerManager;
import com.huangjiang.manager.IMMessageServerManager;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;

import java.io.File;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;

/**
 * 文件消息处理
 */
public class ServerFileHandler extends ChannelHandlerAdapter {

    private Logger logger = Logger.getLogger(ServerMessageHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg != null && msg instanceof ByteBuf && IMMessageServerManager.getInstance() != null) {
            IMFileServerManager.getInstance().packetDispatch(ctx, (ByteBuf) msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.e(cause.getMessage());

    }


}
