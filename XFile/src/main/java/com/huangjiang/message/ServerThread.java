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
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;

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
    Channel channel = null;
    ChannelHandlerAdapter handler = null;

    public ServerThread(int port, ChannelHandlerAdapter handler) {
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
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024 * 2, 0, 4, 0, 4));
                            pipeline.addLast(handler);
                        }
                    });
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
        if (channelFuture.channel() != null) {
            channelFuture.channel().close();
            channel = null;
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
