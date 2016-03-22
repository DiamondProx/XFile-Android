package com.huangjiang.message;

import com.google.protobuf.GeneratedMessage;
import com.huangjiang.message.base.Header;
import com.huangjiang.utils.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 消息服务器
 */
public class ServerThread<T extends ChannelHandlerAdapter> extends Thread {

    private Logger logger = Logger.getLogger(ServerThread.class);
    private int port;
    EventLoopGroup bossGroup = null;
    EventLoopGroup workGroup = null;
    ServerBootstrap serverBootstrap = null;
    ChannelFuture channelFuture = null;
    Channel channel = null;
    T handler = null;

    public ServerThread(int port, T handler) {
        this.port = port;
        this.handler = handler;
    }

    @Override
    public void run() {
        startServer();
    }

    public void startServer() {
        try {
            bossGroup = new NioEventLoopGroup();
            workGroup = new NioEventLoopGroup();
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(handler);
            channelFuture = serverBootstrap.bind(port).sync();
            channel = channelFuture.channel();
            channel.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
            logger.d(e.getMessage());
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }

    }

    public void stopServer() {
        if (channel != null) {
            channel.close();
        }
    }

    public void sendMessage(ChannelHandlerContext ctx, GeneratedMessage msg, Header header) {
        if (channel != null && channel.isWritable()) {
            ByteBuf byteBuf = Unpooled.buffer(header.getLength());
            byteBuf.writeBytes(header.toByteArray());
            byteBuf.writeBytes(msg.toByteArray());

            ctx.writeAndFlush(byteBuf);
        }
    }


}
