package com.huangjiang.message;

import android.os.Environment;

import com.google.protobuf.ByteString;
import com.huangjiang.config.SysConstant;
import com.huangjiang.message.protocol.XFileProtocol;

import java.io.File;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 客户端文件处理
 */
public class FileClientHandler extends ChannelHandlerAdapter {


    private RandomAccessFile rafi;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
//        byte[] req = "t".getBytes();
//        ByteBuf byteBuf = Unpooled.buffer(req.length);
//        byteBuf.writeBytes(req);
//        ctx.writeAndFlush(byteBuf);
        System.out.println("*****channelActive.Client");
        sendFile(ctx);

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        System.out.println("*****channelRead.Client");
//        ByteBuf byteBuf = (ByteBuf) msg;
//        System.out.println("*****channelRead:" + byteBuf.toString());
    }


    void sendFile(ChannelHandlerContext ctx) {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File path = Environment.getExternalStorageDirectory();
                File sendFile = new File(path.getAbsolutePath() + "/send.txt");

                System.out.println("*****length:" + sendFile.length());
                if (sendFile.exists()) {
                    rafi = new RandomAccessFile(sendFile.getAbsoluteFile(), "r");
                    long fileSize = sendFile.length();
                    byte[] sendData;
                    int readIndex = 0;
                    if (fileSize >= SysConstant.FILE_SEGMENT_SIZE) {
                        // 分段发送
                        sendData = new byte[SysConstant.FILE_SEGMENT_SIZE];
                        rafi.read(sendData);

                    } else {
                        // 一次性全部发送
                        sendData = new byte[(int) fileSize];
                        rafi.read(sendData);
                    }

                    XFileProtocol.File.Builder fileBuilder = XFileProtocol.File.newBuilder();
                    fileBuilder.setName(sendFile.getName());
                    fileBuilder.setMd5("md4");
                    fileBuilder.setReadindex(readIndex);
                    fileBuilder.setWriteindex(readIndex);
                    fileBuilder.setSeqnum("seqnum001");
                    fileBuilder.setLength((int) fileSize);
                    ByteString byteString = ByteString.copyFrom(sendData);
                    fileBuilder.setData(byteString);


                    Header header = new Header();
                    header.setCommandId(SysConstant.CMD_TRANSER_FILE_SEND);
                    header.setLength(SysConstant.HEADER_LENGTH + fileBuilder.getLength());

                    ByteBuf byteBuf = Unpooled.buffer(SysConstant.HEADER_LENGTH + fileBuilder.getLength());
                    byteBuf.writeBytes(header.toByteArray());
                    byteBuf.writeBytes(fileBuilder.build().toByteArray());

                    ctx.writeAndFlush(byteBuf);

                }
            }
        } catch (Exception e) {
            System.out.println("*****sendFile.error:" + e.getMessage());
        }
    }
}
