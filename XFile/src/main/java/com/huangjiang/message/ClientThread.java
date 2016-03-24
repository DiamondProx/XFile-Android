package com.huangjiang.message;

import com.google.protobuf.GeneratedMessage;
import com.huangjiang.manager.event.SocketEvent;
import com.huangjiang.message.base.Header;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.LineBasedFrameDecoder;

/**
 * 消息客户端
 */
public class ClientThread extends Thread {

    private Logger logger = Logger.getLogger(ClientThread.class);

    EventLoopGroup eventLoopGroup;
    Bootstrap bootstrap;
    String host;
    int port;
    Channel channel;
    ChannelFuture channelFuture;
    ChannelHandlerAdapter handler = null;

    OnClientListener onClientListener;

    public void setOnClientListener(OnClientListener onClientListener) {
        this.onClientListener = onClientListener;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public ClientThread(ChannelHandlerAdapter handler) {
        super();
        this.handler = handler;
    }

    @Override
    public void run() {
        doConnect();
    }

    void doConnect() {
        try {
            eventLoopGroup = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .option(ChannelOption.AUTO_READ, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024 * 2, 0, 4, 0, 4));
                            pipeline.addLast(handler);
                        }
                    });
            channelFuture = bootstrap.connect(host, port);
            if (channelFuture.isSuccess()) {
                channel = channelFuture.channel();
                if (onClientListener != null) {
                    onClientListener.connectSuccess();
                }
                channel.closeFuture().sync();
            } else {
                if (onClientListener != null) {
                    onClientListener.connectFailure();
                }
                logger.d("*****Client File Connect Fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.d(e.getMessage());
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }

    public void closeConnect() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
    }

    public void sendMessage(Header header, GeneratedMessage msg) {
        if (channel != null) {
            ByteBuf byteBuf = Unpooled.buffer(header.getLength());
            byteBuf.writeBytes(header.toByteArray());
            byteBuf.writeBytes(msg.toByteArray());
            channel.writeAndFlush(byteBuf);
        }
    }

    public interface OnClientListener {

        void connectSuccess();

        void connectFailure();
    }


}