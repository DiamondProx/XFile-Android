package com.huangjiang.message;

import com.huangjiang.config.SysConstant;
import com.huangjiang.message.protocol.XFileProtocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 消息处理
 */
public class MessageClientHandler extends ChannelHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        System.out.println("*****MessageClientHandler.channelActive.Client");
        sendMessage(ctx);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("*****MessageClientHandler.channelRead.Client");
        ByteBuf byteBuf = (ByteBuf) msg;
        if (byteBuf != null) {
            ByteBuf headBuf = byteBuf.readBytes(SysConstant.HEADER_LENGTH);
            byte[] headArray = headBuf.array();
            Header header = new Header(headArray);
            switch (header.getCommandId()) {
                case SysConstant.CMD_SEND_MESSAGE:
                    recvMessage(ctx, header, byteBuf);
                    break;
            }
        }
    }

    void sendMessage(ChannelHandlerContext ctx) {
        try {

            XFileProtocol.Chat.Builder chatBuilder = XFileProtocol.Chat.newBuilder();
            chatBuilder.setContent("hi,im client");
            chatBuilder.setFrom("client");
            chatBuilder.setMessagetype(1);
            Header header = new Header();
            header.setCommandId(SysConstant.CMD_SEND_MESSAGE);
            byte[] bodyData = chatBuilder.build().toByteArray();
            header.setLength(SysConstant.HEADER_LENGTH + bodyData.length);
            ByteBuf byteBuf = Unpooled.buffer(SysConstant.HEADER_LENGTH + bodyData.length);
            byteBuf.writeBytes(header.toByteArray());
            byteBuf.writeBytes(bodyData);
            ctx.writeAndFlush(byteBuf);

        } catch (Exception e) {
            System.out.println("*****sendFile.error:" + e.getMessage());
        }
    }


    void recvMessage(ChannelHandlerContext ctx, Header header, ByteBuf bf) {
        try {
            System.out.println("*****收到消息-3");
            int lenght = header.getLength();
            ByteBuf byteBuf = bf.readBytes(lenght - SysConstant.HEADER_LENGTH);
            byte[] body = new byte[lenght - SysConstant.HEADER_LENGTH];
            byteBuf.readBytes(body);
            XFileProtocol.Chat chat = XFileProtocol.Chat.parseFrom(body);
            System.out.println("*****Chat.getMessagetype:" + chat.getMessagetype());
            System.out.println("*****Chat.getContent:" + chat.getContent());
            System.out.println("*****Chat.getFrom:" + chat.getFrom());

        } catch (Exception e) {
            System.out.println("*****recvMessage.error:" + e.getMessage());
        }
    }
}
