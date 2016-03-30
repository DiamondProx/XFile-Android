package com.huangjiang.manager;

import android.os.Environment;

import com.google.protobuf.ByteString;
import com.huangjiang.XFileApplication;
import com.huangjiang.business.model.FileInfo;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.callback.Packetlistener;
import com.huangjiang.manager.event.ClientFileEvent;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.XFileUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.RandomAccessFile;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 文件管理
 */
public class IMFileManager extends IMBaseManager {

    private Logger logger = Logger.getLogger(IMFileManager.class);

    private IMServerFileManager imServerFileManager = IMServerFileManager.getInstance();
    private IMClientFileManager imClientFileManager = IMClientFileManager.getInstance();

    private FileInfo sendFile;
    private FileInfo recvFile;
    private RandomAccessFile sendRAFReader;
    private RandomAccessFile recvRAFWriter;

    private int writeIndex;
    private int readIndex;
    private boolean isTransmit = false;
    private boolean isCancel = false;


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


    /**
     * 创建发送文件任务
     */
    public void createTask(final XFileProtocol.File createTask) {
        short cid = SysConstant.CMD_FILE_NEW;
        short sid = SysConstant.SERVICE_DEFAULT;

        Packetlistener packetlistener = new Packetlistener() {
            @Override
            public void onSuccess(short serviceId, Object response) {

                if (serviceId != SysConstant.SERVICE_FILE_NEW_SUCCESS || response == null) {
                    return;
                }
                byte[] rsp = (byte[]) response;
                try {
                    // 创建文件成功,检查是否有传输任务,如果正在传输,添加
                    XFileProtocol.File rspTask = XFileProtocol.File.parseFrom(rsp);
                    if (isTransmit) {
                        checkTask(rspTask);
                    } else {
                        // TODO 保存数据库

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    logger.e(e.getMessage());
                }

            }

            @Override
            public void onFaild() {

            }

            @Override
            public void onTimeout() {

            }
        };
        if (XFileApplication.connect_type == 1) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, createTask, packetlistener, (short) 0);
        } else if (XFileApplication.connect_type == 2) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, createTask, packetlistener, (short) 0);
        }

    }

    public void checkTask(XFileProtocol.File checkTask) {
        short cid = SysConstant.CMD_TASK_CHECK;
        short sid = SysConstant.SERVICE_DEFAULT;
        Packetlistener packetlistener = new Packetlistener() {
            @Override
            public void onSuccess(short serviceId, Object response) {
                if (serviceId != SysConstant.SERVICE_TASK_CHECK_SUCCESS || response == null) {
                    return;
                }
                try {
                    byte[] rsp = (byte[]) response;
                    XFileProtocol.File rspTask = XFileProtocol.File.parseFrom(rsp);
                    if (isTransmit) {
                        transferFile(rspTask);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.e(e.getMessage());
                }

            }

            @Override
            public void onFaild() {

            }

            @Override
            public void onTimeout() {

            }
        };
        if (XFileApplication.connect_type == 1) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, checkTask, packetlistener, (short) 0);
        } else if (XFileApplication.connect_type == 2) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, checkTask, packetlistener, (short) 0);
        }
    }

    public void transferFile(final XFileProtocol.File requestFile) {
        short cid = SysConstant.CMD_FILE_SET;
        short sid = SysConstant.SERVICE_DEFAULT;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(requestFile.getPath(), "r");
            byte[] readBytes = null;
            if (requestFile.getLength() >= SysConstant.FILE_SEGMENT_SIZE) {
                readBytes = new byte[SysConstant.FILE_SEGMENT_SIZE];
            } else {
                readBytes = new byte[(int) requestFile.getLength()];
            }
            randomAccessFile.read(readBytes);
            ByteString byteString = ByteString.copyFrom(readBytes);
            requestFile.toBuilder().setData(byteString);
            requestFile.toBuilder().setPosition(readBytes.length);
            Packetlistener packetlistener = new Packetlistener() {
                @Override
                public void onSuccess(short serviceId, Object response) {
                    if (serviceId != SysConstant.SERVICE_TASK_CHECK_SUCCESS || response == null) {
                        return;
                    }
                    try {
                        byte[] rsp = (byte[]) response;
                        XFileProtocol.File responseFile = XFileProtocol.File.parseFrom(rsp);
                        // 判断传回来的postion是否等于文件length，相等的情况下，当作已经传输完成
                        if (responseFile.getLength() == responseFile.getLength()) {
                            ClientFileEvent event = new ClientFileEvent(FileEvent.SET_FILE_SUCCESS);
                            event.setTaskId(responseFile.getTaskId());
                            isTransmit = false;
                            return;
                        }

                        if (!isCancel) {
                            // 正常收到答复继续传送文件
                            transferFile(responseFile);
                        } else {
                            // 取消传送，记录当前传输的位置
                            ClientFileEvent event = new ClientFileEvent(FileEvent.SET_FILE_STOP);
                            event.setFileInfo(responseFile);
                            EventBus.getDefault().post(event);
                            isTransmit = false;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.e(e.getMessage());
                    }

                }

                @Override
                public void onFaild() {
                    ClientFileEvent event = new ClientFileEvent(FileEvent.SET_FILE_FAILED);
                    event.setTaskId(requestFile.getTaskId());
                    EventBus.getDefault().post(event);
                }

                @Override
                public void onTimeout() {
                    ClientFileEvent event = new ClientFileEvent(FileEvent.SET_FILE_FAILED);
                    event.setTaskId(requestFile.getTaskId());
                    EventBus.getDefault().post(event);
                }
            };
            if (XFileApplication.connect_type == 1) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, requestFile, packetlistener, (short) 0);
            } else if (XFileApplication.connect_type == 2) {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, requestFile, packetlistener, (short) 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }

    }


    public void dispatchMessage(Header header, byte[] bodyData) {
        switch (header.getCommandId()) {
            case SysConstant.CMD_FILE_NEW:
                // 新建接收文件任务
                break;
            case SysConstant.CMD_TASK_CHECK:
                // 检查任务是否存在/任务存在进入文件传输
                break;
            case SysConstant.CMD_FILE_SET:
                // 保存文件/断点续传

                break;
        }
    }

    void receiveData(Header header, byte[] bodyData) {
        try {
            XFileProtocol.File requestFile = XFileProtocol.File.parseFrom(bodyData);
            String savePath = XFileUtils.getStoragePathByExtension(requestFile.getExtension());
            byte[] fileData = requestFile.getData().toByteArray();
            RandomAccessFile randomAccessFile = new RandomAccessFile(savePath, "rw");
            randomAccessFile.seek(requestFile.getPosition());
            randomAccessFile.write(fileData);
            randomAccessFile.close();

            if (requestFile.getPosition() + fileData.length <= requestFile.getLength()) {
                XFileProtocol.File.Builder responseFile = XFileProtocol.File.newBuilder();
                responseFile.setName(requestFile.getName());
                responseFile.setLength(requestFile.getLength());
                responseFile.setMd5(requestFile.getMd5());
                responseFile.setPosition(requestFile.getPosition() + ((long) fileData.length));
                responseFile.setData(ByteString.copyFrom("1".getBytes()));

                Packetlistener packetlistener = new Packetlistener() {
                    @Override
                    public void onSuccess(short service, Object response) {

                    }

                    @Override
                    public void onFaild() {

                    }

                    @Override
                    public void onTimeout() {

                    }
                };

                if (requestFile.getPosition() + fileData.length == requestFile.getLength()) {
                    // 文件发送完成,提醒接收端结束状态
                }
                short sid = SysConstant.SERVICE_FILE_SET_SUCCESS;
                short cid = SysConstant.CMD_FILE_SET;
                if (XFileApplication.connect_type == 1) {
                    IMClientMessageManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, header.getSeqnum());
                } else if (XFileApplication.connect_type == 2) {
                    IMServerMessageManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, header.getSeqnum());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }


    void keepFile(XFileProtocol.File fileInfo) {

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
            imClientFileManager.sendMessage(SysConstant.SERVICE_DEFAULT, SysConstant.CMD_TRANSER_FILE_SEND, fileBuilder.build());


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
            imClientFileManager.sendMessage(SysConstant.SERVICE_DEFAULT, SysConstant.CMD_TRANSER_FILE_SEND, fileBuilder.build());


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


                //imServerFileManager.sendMessage(ctx, respFile.build(), SysConstant.SERVICE_DEFAULT, SysConstant.CMD_TRANSER_FILE_REC);

            } else {
                System.out.println("*****文件发送完毕:" + System.currentTimeMillis());
            }


        } catch (Exception e) {
            System.out.println("*****RecvFile:" + e.getMessage());
        }
    }
}
