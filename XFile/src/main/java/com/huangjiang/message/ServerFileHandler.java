package com.huangjiang.message;

import android.os.Environment;

import com.google.protobuf.ByteString;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.IMFileServerManager;
import com.huangjiang.manager.IMMessageServerManager;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;

import java.io.File;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 文件消息处理
 */
public class ServerFileHandler extends ChannelHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg != null && msg instanceof ByteBuf && IMMessageServerManager.getInstance() != null) {
            IMFileServerManager.getInstance().packetDispatch(ctx,(ByteBuf) msg);
        }
    }





}
