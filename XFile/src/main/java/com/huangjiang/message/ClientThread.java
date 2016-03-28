package com.huangjiang.message;

import com.google.protobuf.GeneratedMessage;
import com.huangjiang.manager.event.ClientMessageSocketEvent;
import com.huangjiang.manager.event.SocketEvent;
import com.huangjiang.message.base.Header;
import com.huangjiang.utils.Logger;

import org.greenrobot.eventbus.EventBus;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 消息客户端
 */
public class ClientThread extends Thread {

    private Logger logger = Logger.getLogger(ClientThread.class);

    EventLoopGroup eventLoopGroup;
    Bootstrap bootstrap;
    String host;
    int port;
    ChannelFuture channelFuture;
    XFileChannelInitializer.InitialType initialType;

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

    public ClientThread(XFileChannelInitializer.InitialType initialType) {
        super();
        this.initialType = initialType;
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
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .handler(new XFileChannelInitializer(this.initialType));
            channelFuture = bootstrap.connect(host, port).sync();
            if (channelFuture.isSuccess()) {
                if (onClientListener != null) {
                    onClientListener.connectSuccess();
                }
                channelFuture.channel().closeFuture().sync();
            } else {
                if (onClientListener != null) {
                    onClientListener.connectFailure();
                }
                logger.d("*****Client Connect Fail");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.d(e.getMessage());
        } finally {
            eventLoopGroup.shutdownGracefully();
            logger.e("*****Client Close");
            EventBus.getDefault().post(new ClientMessageSocketEvent(SocketEvent.CONNECT_CLOSE));
        }
    }

    public void closeConnect() {
        if (channelFuture != null && channelFuture.channel() != null && channelFuture.channel().isActive()) {
            channelFuture.channel().close();
            channelFuture = null;
        }

    }

    public void sendMessage(Header header, GeneratedMessage msg) {
        if (channelFuture != null && channelFuture.channel() != null && channelFuture.channel().isWritable()) {
            ByteBuf byteBuf = Unpooled.buffer(header.getLength());
            byteBuf.writeBytes(header.toByteArray());
            byteBuf.writeBytes(msg.toByteArray());
            channelFuture.channel().writeAndFlush(byteBuf);
        }
    }

    public interface OnClientListener {

        void connectSuccess();

        void connectFailure();
    }


}
