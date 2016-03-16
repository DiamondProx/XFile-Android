package com.huangjiang.message;

import android.os.Environment;

import com.google.protobuf.ByteString;
import com.huangjiang.config.SysConstant;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;

import java.io.File;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

/**
 * 文件消息处理
 */
public class FileServerHandler extends ChannelHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("*****channelRead");
//        super.channelRead(ctx, msg);
        ByteBuf byteBuf = (ByteBuf) msg;
        if (byteBuf != null) {
            ByteBuf headBuf = byteBuf.readBytes(SysConstant.HEADER_LENGTH);
            byte[] headArray = headBuf.array();
            Header header = new Header(headArray);
            switch (header.getCommandId()) {
                case SysConstant.CMD_TRANSER_FILE_SEND:
                    recvFile(ctx, header, byteBuf);
                    break;
            }
        }

    }

    void recvFile(ChannelHandlerContext ctx, Header header, ByteBuf bf) {
        try {
            int lenght = header.getLength();
            ByteBuf byteBuf = bf.readBytes(lenght - SysConstant.HEADER_LENGTH);
            byte[] body = new byte[lenght - SysConstant.HEADER_LENGTH];
            byteBuf.readBytes(body);
            XFileProtocol.File file = XFileProtocol.File.parseFrom(body);
            System.out.println("*****file.Name:" + file.getName());
            System.out.println("*****file.MD5:" + file.getMd5());
            String content = new String(file.getData().toByteArray(), "UTF-8");
            System.out.println("*****file.Data:" + content);
            saveFile(ctx, file);


        } catch (Exception e) {
            System.out.println("*****RecvFile:" + e.getMessage());
        }

    }

    void respFile(ChannelHandlerContext ctx) {

    }

    void saveFile(ChannelHandlerContext ctx, XFileProtocol.File requestFile) {
        try {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                // 保存文件
                File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/save.mp3");
                if (!file.exists()) {
                    file.createNewFile();
                }
                byte[] content = requestFile.getData().toByteArray();
                RandomAccessFile rf = new RandomAccessFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/save.mp3", "rw");
                rf.seek(requestFile.getReadindex());
                rf.write(content);
                rf.close();

                if (requestFile.getReadindex() + content.length < requestFile.getLength()) {
                    // 是否要续传文件(没传完)
                    Header header = new Header();
                    header.setCommandId(SysConstant.CMD_TRANSER_FILE_REC);

                    XFileProtocol.File.Builder respFile = XFileProtocol.File.newBuilder();
                    respFile.setName(requestFile.getName());
                    respFile.setLength(requestFile.getLength());
                    respFile.setMd5("md5");
                    respFile.setReadindex(requestFile.getReadindex() + content.length);
                    respFile.setWriteindex(requestFile.getReadindex() + content.length);
                    respFile.setData(ByteString.copyFrom("1".getBytes()));
                    respFile.setSeqnum("seqnum");

                    byte[] bodyData = respFile.build().toByteArray();

                    header.setLength(SysConstant.HEADER_LENGTH + bodyData.length);
                    ByteBuf sendBuf = Unpooled.buffer(SysConstant.HEADER_LENGTH + bodyData.length);
                    sendBuf.writeBytes(header.toByteArray());
                    sendBuf.writeBytes(bodyData);

                    ctx.writeAndFlush(sendBuf);
                    System.out.println("*****继续发-1");
                    System.out.println("*****respFile.setReadindex:"+(requestFile.getReadindex() + content.length));
                    System.out.println("*****header.Lenght:"+header.getLength());
                    System.out.println("*****bodyData.length:"+bodyData.length);
                    System.out.println("*****继续发-2");
                }else{
                    System.out.println("*****文件发送完毕");
                }


            }
        } catch (Exception e) {
            System.out.println("*****RecvFile:" + e.getMessage());
        }
    }

}
