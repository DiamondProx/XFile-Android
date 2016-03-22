package com.huangjiang.message;

import com.google.protobuf.GeneratedMessage;
import com.huangjiang.message.base.Header;
import com.huangjiang.utils.Logger;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
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
public class ClientThread<T extends ChannelHandlerAdapter> extends Thread {

    private Logger logger = Logger.getLogger(ClientThread.class);

    EventLoopGroup eventLoopGroup;
    Bootstrap bootstrap;
    String host;
    int port;
    Channel channel;
    T handler = null;


    public void setPort(int port) {
        this.port = port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public ClientThread(T handler) {
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
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 1024 * 2, 0, 4, 0, 4));
                            pipeline.addLast(handler);
                        }
                    });
            channel = bootstrap.connect(host, port).channel();
            channel.closeFuture().sync();
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
        }
    }

    public void sendMessage(GeneratedMessage msg, Header header) {
        if (channel != null) {
            ByteBuf byteBuf = Unpooled.buffer(header.getLength());
            byteBuf.writeBytes(header.toByteArray());
            byteBuf.writeBytes(msg.toByteArray());
            channel.writeAndFlush(byteBuf);
        }
    }


}
