package com.huangjiang.message;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;

/**
 * 初始化handler
 */
public class XFileChannelInitializer extends ChannelInitializer<SocketChannel> {


    private InitialType type;

    public XFileChannelInitializer(InitialType type) {
        this.type = type;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new LengthFieldPrepender(4));
        ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024 * 1024 * 2, 0, 4, 0, 4));
        switch (this.type) {
            case CLIENTMESSAGEHANDLER:
                ch.pipeline().addLast(new ClientMessageHandler());
                break;
            case CLIENTFILEHANDLER:
                ch.pipeline().addLast(new ClientFileHandler());
                break;
            case SERVERMESSAGEHANDLER:
                ch.pipeline().addLast(new ServerMessageHandler());
                break;
            case SERVERFILEHANDLER:
                ch.pipeline().addLast(new ServerFileHandler());
                break;
        }

    }

    public enum InitialType {
        CLIENTMESSAGEHANDLER,
        CLIENTFILEHANDLER,
        SERVERMESSAGEHANDLER,
        SERVERFILEHANDLER
    }
}
