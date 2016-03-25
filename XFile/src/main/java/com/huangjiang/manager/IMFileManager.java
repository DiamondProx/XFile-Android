package com.huangjiang.manager;

import android.os.Environment;

import com.google.protobuf.ByteString;
import com.huangjiang.business.model.FileInfo;
import com.huangjiang.config.SysConstant;
import com.huangjiang.message.DeviceServerThread;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.XFileUtils;

import java.io.File;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 * 文件管理
 */
public class IMFileManager extends IMManager {


    private IMFileServerManager imFileServerManager = IMFileServerManager.getInstance();
    private IMFileClientManager imFileClientManager = IMFileClientManager.getInstance();

    private FileInfo sendFile;
    private FileInfo recvFile;
    private RandomAccessFile sendRAFReader;
    private RandomAccessFile recvRAFWriter;

    private Logger logger = Logger.getLogger(IMFileManager.class);

    private static IMFileManager inst = null;

    public static IMFileManager getInstance() {
        if (inst == null) {
            inst = new IMFileManager();
        }
        return inst;
    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }


    public void sendFile(File file) {
        try {
            sendFile = new FileInfo();
            sendFile.setName(file.getName());
            sendFile.setFileLength(file.length());
            String md5 = XFileUtils.getMd5ByFile(file);
            sendFile.setMd5(md5);
            sendFile.setPath(file.getAbsolutePath());
            sendFile.setPostion(0);
            sendFile.setFileLength(file.length());
            sendRAFReader = new RandomAccessFile(file, "r");
            long fileSize = file.length();
            byte[] sendData;
            if (fileSize >= SysConstant.FILE_SEGMENT_SIZE) {
                // 分段发送
                sendData = new byte[SysConstant.FILE_SEGMENT_SIZE];
                sendRAFReader.read(sendData);

            } else {
                // 一次性全部发送
                sendData = new byte[(int) fileSize];
                sendRAFReader.read(sendData);
            }

            XFileProtocol.File.Builder fileBuilder = XFileProtocol.File.newBuilder();
            fileBuilder.setName(sendFile.getName());
            fileBuilder.setMd5(sendFile.getMd5());
            fileBuilder.setPosition(sendFile.getPostion());
            fileBuilder.setLength(sendFile.getFileLength());
            ByteString byteString = ByteString.copyFrom(sendData);
            fileBuilder.setData(byteString);
            System.out.println("*****开始发送文件:" + System.currentTimeMillis());
            imFileClientManager.sendMessage(SysConstant.SERVICE_DEFAULT, SysConstant.CMD_TRANSER_FILE_SEND, fileBuilder.build());


        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }

    }

    void continueSendFile(Header header, ByteBuf bf) {
        try {
            int lenght = header.getLength();
            ByteBuf byteBuf = bf.readBytes(lenght - SysConstant.HEADER_LENGTH);
            byte[] body = new byte[lenght - SysConstant.HEADER_LENGTH];
            byteBuf.readBytes(body);
            XFileProtocol.File file = XFileProtocol.File.parseFrom(body);
            sendRAFReader.seek(file.getPosition());
            XFileProtocol.File.Builder fileBuilder = XFileProtocol.File.newBuilder();
            if (file.getLength() - file.getPosition() >= SysConstant.FILE_SEGMENT_SIZE) {
                // 大于一个包以上
                byte[] fileData = new byte[SysConstant.FILE_SEGMENT_SIZE];
                sendRAFReader.read(fileData);

                fileBuilder.setName(file.getName());
                fileBuilder.setMd5(file.getMd5());
                fileBuilder.setPosition(file.getPosition());
                fileBuilder.setLength(file.getLength());
                ByteString byteString = ByteString.copyFrom(fileData);
                fileBuilder.setData(byteString);


            } else {
                // 不足一个数据包
                byte[] fileData = new byte[(int) (file.getLength() - file.getPosition())];
                sendRAFReader.read(fileData);

                fileBuilder.setName(file.getName());
                fileBuilder.setMd5(file.getMd5());
                fileBuilder.setPosition(file.getPosition());
                fileBuilder.setLength(file.getLength());
                ByteString byteString = ByteString.copyFrom(fileData);
                fileBuilder.setData(byteString);
            }
            imFileClientManager.sendMessage(SysConstant.SERVICE_DEFAULT, SysConstant.CMD_TRANSER_FILE_SEND, fileBuilder.build());


        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }

    }


    public void recvFile(ChannelHandlerContext ctx, Header header, ByteBuf bf) {
        try {
            int lenght = header.getLength();
            ByteBuf byteBuf = bf.readBytes(lenght - SysConstant.HEADER_LENGTH);
//            byte[] body = new byte[lenght - SysConstant.HEADER_LENGTH];
//            byteBuf.readBytes(body);
            ByteBuf readByteBuf = byteBuf.readBytes(lenght - SysConstant.HEADER_LENGTH);
            byte[] body = readByteBuf.array();
            XFileProtocol.File file = XFileProtocol.File.parseFrom(body);
//            System.out.println("*****file.Name:" + file.getName());
//            System.out.println("*****file.MD5:" + file.getMd5());
//            String content = new String(file.getData().toByteArray(), "UTF-8");
//            System.out.println("*****file.Data:" + content);
            saveFile(ctx, file);
        } catch (Exception e) {
            System.out.println("*****RecvFile:" + e.getMessage());
        }

    }


    void saveFile(ChannelHandlerContext ctx, XFileProtocol.File requestFile) {
        try {

            // 保存文件
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/save.mp3");
            if (!file.exists()) {
                file.createNewFile();
            }
            byte[] content = requestFile.getData().toByteArray();
            RandomAccessFile rf = new RandomAccessFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/save.mp3", "rw");
            rf.seek(requestFile.getPosition());
            rf.write(content);
            rf.close();

            if (requestFile.getPosition() + content.length < requestFile.getLength()) {


                XFileProtocol.File.Builder respFile = XFileProtocol.File.newBuilder();
                respFile.setName(requestFile.getName());
                respFile.setLength(requestFile.getLength());
                respFile.setMd5(requestFile.getMd5());
                respFile.setPosition(requestFile.getPosition() + ((long) content.length));
                respFile.setData(ByteString.copyFrom("1".getBytes()));


                //imFileServerManager.sendMessage(ctx, respFile.build(), SysConstant.SERVICE_DEFAULT, SysConstant.CMD_TRANSER_FILE_REC);

            } else {
                System.out.println("*****文件发送完毕:" + System.currentTimeMillis());
            }


        } catch (Exception e) {
            System.out.println("*****RecvFile:" + e.getMessage());
        }
    }
}
