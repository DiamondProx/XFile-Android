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
        System.out.println("*****channelRead.Client");
        ByteBuf byteBuf = (ByteBuf) msg;
        if (byteBuf != null) {
            ByteBuf headBuf = byteBuf.readBytes(SysConstant.HEADER_LENGTH);
            byte[] headArray = headBuf.array();
            Header header = new Header(headArray);
            switch (header.getCommandId()) {
                case SysConstant.CMD_TRANSER_FILE_REC:
                    continueSendFile(ctx, header, byteBuf);
                    break;
            }
        }
    }

    void continueSendFile(ChannelHandlerContext ctx, Header header, ByteBuf bf) {
        try {
            System.out.println("*****继续发-3");
            int lenght = header.getLength();
            ByteBuf byteBuf = bf.readBytes(lenght - SysConstant.HEADER_LENGTH);
            byte[] body = new byte[lenght - SysConstant.HEADER_LENGTH];
            byteBuf.readBytes(body);
            XFileProtocol.File file = XFileProtocol.File.parseFrom(body);

            System.out.println("*****file.setReadindex-2:"+file.getReadindex());

            RandomAccessFile raf = new RandomAccessFile(Environment.getExternalStorageDirectory() + "/send.txt", "r");
            raf.seek(file.getReadindex());
            XFileProtocol.File.Builder fileBuilder = XFileProtocol.File.newBuilder();
            if (file.getLength() - file.getReadindex() >= SysConstant.FILE_SEGMENT_SIZE) {
                // 大于一个包以上
                byte[] fileData = new byte[SysConstant.FILE_SEGMENT_SIZE];
                raf.read(fileData);

                fileBuilder.setName(file.getName());
                fileBuilder.setMd5("md4");
                fileBuilder.setReadindex(file.getReadindex());
                fileBuilder.setWriteindex(file.getReadindex());
                fileBuilder.setSeqnum("seqnum002");
                fileBuilder.setLength((int) file.getLength());
                ByteString byteString = ByteString.copyFrom(fileData);
                fileBuilder.setData(byteString);


                System.out.println("*****fileBuilder.getReadindex-3:"+fileBuilder.getReadindex());

            } else {
                // 不足一个数据包
                byte[] fileData = new byte[file.getLength() - file.getReadindex()];
                raf.read(fileData);


                fileBuilder.setName(file.getName());
                fileBuilder.setMd5("md4");
                fileBuilder.setReadindex(file.getReadindex());
                fileBuilder.setWriteindex(file.getReadindex());
                fileBuilder.setSeqnum("seqnum003");
                fileBuilder.setLength((int) file.getLength());
                ByteString byteString = ByteString.copyFrom(fileData);
                fileBuilder.setData(byteString);

                System.out.println("*****fileBuilder.getReadindex-4:"+fileBuilder.getReadindex());

            }


            Header sendHeader = new Header();
            sendHeader.setCommandId(SysConstant.CMD_TRANSER_FILE_SEND);
            byte[] bodyData = fileBuilder.build().toByteArray();
            sendHeader.setLength(SysConstant.HEADER_LENGTH + bodyData.length);

            ByteBuf sendBuf = Unpooled.buffer(SysConstant.HEADER_LENGTH + bodyData.length);
            sendBuf.writeBytes(sendHeader.toByteArray());
            sendBuf.writeBytes(bodyData);

            ctx.writeAndFlush(sendBuf);


        } catch (Exception e) {
            System.out.println("*****RecvFile:" + e.getMessage());
        }
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
                    byte[] bodyData = fileBuilder.build().toByteArray();
                    header.setLength(SysConstant.HEADER_LENGTH + bodyData.length);

                    ByteBuf byteBuf = Unpooled.buffer(SysConstant.HEADER_LENGTH + fileBuilder.getLength());
                    byteBuf.writeBytes(header.toByteArray());
                    byteBuf.writeBytes(bodyData);

                    ctx.writeAndFlush(byteBuf);


                }
            }
        } catch (Exception e) {
            System.out.println("*****sendFile.error:" + e.getMessage());
        }
    }


}
