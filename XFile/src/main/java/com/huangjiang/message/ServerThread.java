package com.huangjiang.message;

import com.google.protobuf.GeneratedMessage;
import com.huangjiang.message.base.Header;
import com.huangjiang.utils.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * 消息服务器
 */
public class ServerThread extends Thread {

    private Logger logger = Logger.getLogger(ServerThread.class);
    private int port;
    EventLoopGroup bossGroup = null;
    EventLoopGroup workGroup = null;
    ServerBootstrap serverBootstrap = null;
    ChannelFuture channelFuture = null;
//    Channel channel = null;
    final XFileChannelInitializer.InitialType initialType ;

    public ServerThread(int port, XFileChannelInitializer.InitialType initialType) {
        this.port = port;
        this.initialType = initialType;
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
                    .childHandler(new XFileChannelInitializer(initialType));
            channelFuture = serverBootstrap.bind(port).sync();
//            channel = channelFuture.channel();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            logger.d("*****Server Close");
        }

    }

    public void stopServer() {
        if (channelFuture.channel() != null) {
            channelFuture.channel().close();
            channelFuture = null;
        }
    }

    public void sendMessage(ChannelHandlerContext ctx, Header header, GeneratedMessage msg) {
        if (ctx != null && ctx.channel().isWritable()) {
            ByteBuf byteBuf = Unpooled.buffer(header.getLength());
            byteBuf.writeBytes(header.toByteArray());
            byteBuf.writeBytes(msg.toByteArray());
            ctx.writeAndFlush(byteBuf);
        }
    }


}
