package com.huangjiang.manager;

import android.os.Build;
import android.os.Environment;

import com.google.protobuf.ByteString;
import com.huangjiang.XFileApplication;
import com.huangjiang.business.model.FileInfo;
import com.huangjiang.business.model.TFileInfo;
import com.huangjiang.config.SysConstant;
import com.huangjiang.manager.callback.Packetlistener;
import com.huangjiang.manager.event.FileEvent;
import com.huangjiang.manager.event.FileReceiveEvent;
import com.huangjiang.manager.event.FileSendEvent;
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

    private long readIndex;
    private long readPercent;
    private long readLength;

    private long writeIndex;
    private long writePercent;
    private long writeLength;

    private boolean isTransmit = false;
    private boolean isCancel = false;
    private boolean isCancelReceive = false;


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
                    FileSendEvent event = new FileSendEvent(FileEvent.CREATE_FILE_FAILED);
                    event.setTaskId(createTask.getTaskId());
                    EventBus.getDefault().post(event);
                    return;
                }
                byte[] rsp = (byte[]) response;
                try {
                    // 创建文件成功,检查是否有传输任务,如果正在传输,添加
                    XFileProtocol.File rspTask = XFileProtocol.File.parseFrom(rsp);
                    if (!isTransmit) {
                        // 没有正在传输的任务,直接校验任务合法性
//                        isTransmit = true;
                        checkTask(rspTask);
                    } else {
                        // TODO 正在传输文件,保存任务为准备传输状态
                        // saveTask();
                    }
                    // 发送Event消息,通知界面
                    FileSendEvent event = new FileSendEvent(FileEvent.CREATE_FILE_SUCCESS);
                    TFileInfo tFile = XFileUtils.buildTFile(rspTask);
                    event.setFileInfo(tFile);
                    EventBus.getDefault().post(event);
                    logger.e("****createTaskSuccess");


                } catch (Exception e) {
                    e.printStackTrace();
                    logger.e(e.getMessage());
                }

            }

            @Override
            public void onFaild() {
                FileSendEvent event = new FileSendEvent(FileEvent.CREATE_FILE_FAILED);
                event.setTaskId(createTask.getTaskId());
                EventBus.getDefault().post(event);
            }

            @Override
            public void onTimeout() {
                FileSendEvent event = new FileSendEvent(FileEvent.CREATE_FILE_FAILED);
                event.setTaskId(createTask.getTaskId());
                EventBus.getDefault().post(event);
            }
        };
        if (XFileApplication.connect_type == 1) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, createTask, packetlistener, (short) 0);
        } else if (XFileApplication.connect_type == 2) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, createTask, packetlistener, (short) 0);
        }
        logger.e("****createTaskSend");

    }

    /**
     * 检查接收方任务是否存在
     */
    public void checkTask(final XFileProtocol.File checkTask) {
        short cid = SysConstant.CMD_TASK_CHECK;
        short sid = SysConstant.SERVICE_DEFAULT;
        Packetlistener packetlistener = new Packetlistener() {
            @Override
            public void onSuccess(short serviceId, Object response) {
                if (serviceId != SysConstant.SERVICE_TASK_CHECK_SUCCESS || response == null) {
                    FileSendEvent event = new FileSendEvent(FileEvent.CHECK_TASK_FAILED);
                    event.setTaskId(checkTask.getTaskId());
                    EventBus.getDefault().post(event);
                    return;
                }
                try {
                    FileSendEvent event = new FileSendEvent(FileEvent.CHECK_TASK_SUCCESS);
                    event.setTaskId(checkTask.getTaskId());
                    EventBus.getDefault().post(event);
                    logger.e("***checkTaskSuccess");
                    byte[] rsp = (byte[]) response;
                    XFileProtocol.File rspTask = XFileProtocol.File.parseFrom(rsp);
//                    if (isTransmit) {
                    transferFile(rspTask);
//                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.e(e.getMessage());
                    FileSendEvent event = new FileSendEvent(FileEvent.CHECK_TASK_FAILED);
                    event.setTaskId(checkTask.getTaskId());
                    EventBus.getDefault().post(event);
                }

            }

            @Override
            public void onFaild() {
                FileSendEvent event = new FileSendEvent(FileEvent.CHECK_TASK_FAILED);
                event.setTaskId(checkTask.getTaskId());
                EventBus.getDefault().post(event);
            }

            @Override
            public void onTimeout() {
                FileSendEvent event = new FileSendEvent(FileEvent.CHECK_TASK_FAILED);
                event.setTaskId(checkTask.getTaskId());
                EventBus.getDefault().post(event);
            }
        };
        if (XFileApplication.connect_type == 1) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, checkTask, packetlistener, (short) 0);
        } else if (XFileApplication.connect_type == 2) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, checkTask, packetlistener, (short) 0);
        }
        logger.e("****checkTaskSend");
    }

    public boolean startTransferFile(final XFileProtocol.File requestFile) {
//        if (isTransmit) {
//            return false;
//        }
        readIndex = requestFile.getPosition();
        writePercent = readIndex * 100 / requestFile.getLength();
        createTask(requestFile);
//        isTransmit = true;
        return true;
    }

    public void transferFile(final XFileProtocol.File requestFile) {
        short cid = SysConstant.CMD_FILE_SET;
        short sid = SysConstant.SERVICE_DEFAULT;
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(requestFile.getPath(), "r");
            byte[] readBytes = null;
            long remain = requestFile.getLength() - requestFile.getPosition();

            // 判断传回来的postion是否等于文件length，相等的情况下，当作已经传输完成
            if (remain == 0) {
                FileSendEvent event = new FileSendEvent(FileEvent.SET_FILE_SUCCESS);
                TFileInfo tFile = XFileUtils.buildTFile(requestFile);
                tFile.setPercent(100);
                readPercent = 100;
                event.setFileInfo(tFile);
                event.setTaskId(requestFile.getTaskId());
                isTransmit = false;
                EventBus.getDefault().post(event);
                return;
            }

            if (remain >= SysConstant.FILE_SEGMENT_SIZE) {
                readBytes = new byte[SysConstant.FILE_SEGMENT_SIZE];
            } else {
                readBytes = new byte[(int) remain];
            }
            randomAccessFile.seek(requestFile.getPosition());
            randomAccessFile.read(readBytes);
            ByteString byteString = ByteString.copyFrom(readBytes);
            XFileProtocol.File.Builder responseFile = requestFile.toBuilder();
            responseFile.setData(byteString);
//            responseFile.setPosition(readBytes.length + responseFile.getPosition());
            Packetlistener packetlistener = new Packetlistener() {
                @Override
                public void onSuccess(short serviceId, Object response) {
                    if (response == null) {
                        FileSendEvent event = new FileSendEvent(FileEvent.SET_FILE_FAILED);
                        event.setTaskId(requestFile.getTaskId());
                        EventBus.getDefault().post(event);
                        return;
                    }


                    try {
                        byte[] rsp = (byte[]) response;
                        XFileProtocol.File responseFile = XFileProtocol.File.parseFrom(rsp);

                        // TODO 通知界面进度
                        readIndex = responseFile.getPosition();
                        long temPercent = readIndex * 100 / responseFile.getLength();
                        if (readPercent < temPercent) {
                            readPercent = temPercent;
                            FileSendEvent event = new FileSendEvent(FileEvent.SET_FILE);
                            TFileInfo tFile = XFileUtils.buildTFile(responseFile);
                            tFile.setPercent(readPercent);
                            event.setFileInfo(tFile);
                            EventBus.getDefault().post(event);
                        }
                        if (serviceId == SysConstant.SERVICE_FILE_SET_STOP) {
                            FileSendEvent event = new FileSendEvent(FileEvent.SET_FILE_STOP);
                            TFileInfo tFile = XFileUtils.buildTFile(responseFile);
                            tFile.setPercent(readPercent);
                            event.setFileInfo(tFile);
                            EventBus.getDefault().post(event);
                            // 对方暂停接收文件
                            isCancel = true;
                            isTransmit = false;
                        }

                        if (!isCancel) {
                            // 正常收到答复继续传送文件
                            transferFile(responseFile);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.e(e.getMessage());
                    }

                }

                @Override
                public void onFaild() {
                    FileSendEvent event = new FileSendEvent(FileEvent.SET_FILE_FAILED);
                    event.setTaskId(requestFile.getTaskId());
                    EventBus.getDefault().post(event);
                }

                @Override
                public void onTimeout() {
                    FileSendEvent event = new FileSendEvent(FileEvent.SET_FILE_FAILED);
                    event.setTaskId(requestFile.getTaskId());
                    EventBus.getDefault().post(event);
                }
            };
            if (XFileApplication.connect_type == 1) {
                IMClientFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, (short) 0);
            } else if (XFileApplication.connect_type == 2) {
                IMServerFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, (short) 0);
            }


            // TODO 保存数据库进度

//            logger.e("****setFileSend");
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
            FileSendEvent event = new FileSendEvent(FileEvent.SET_FILE_FAILED);
            event.setTaskId(requestFile.getTaskId());
            EventBus.getDefault().post(event);
        }

    }


    public void dispatchMessage(Header header, byte[] bodyData) {
        switch (header.getCommandId()) {
            case SysConstant.CMD_FILE_NEW:
                // 新建接收文件任务
                createNewFile(header, bodyData);
                break;
            case SysConstant.CMD_TASK_CHECK:
                // 检查任务是否存在/任务存在进入文件传输
                checkTask(header, bodyData);
                break;
            case SysConstant.CMD_FILE_SET:
                // 保存文件/断点续传
                receiveData(header, bodyData);
                break;
        }
    }

    void createNewFile(Header header, byte[] bodyData) {
        try {
            XFileProtocol.File requestFile = XFileProtocol.File.parseFrom(bodyData);

            // 创建文件
            String savePath = XFileUtils.getStoragePathByExtension(requestFile.getExtension());
            String fullPath = savePath + requestFile.getFullName();
            File saveFile = new File(fullPath);
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }

            // TODO 保存接收方数据库,记录状态
            // saveTask();


            // 答复发送端创建成功
            XFileProtocol.File.Builder responseFile = requestFile.toBuilder();
            responseFile.setFrom(Build.MODEL);
            short sid = SysConstant.SERVICE_FILE_NEW_SUCCESS;
            short cid = SysConstant.CMD_FILE_NEW_RSP;

            if (XFileApplication.connect_type == 1) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, responseFile.build(), header.getSeqnum());
            } else {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, responseFile.build(), null, header.getSeqnum());
            }
            // 发送Event消息,通知界面
            FileReceiveEvent event = new FileReceiveEvent(FileEvent.CREATE_FILE_SUCCESS);
            TFileInfo tFile = XFileUtils.buildTFile(requestFile);
            event.setFileInfo(tFile);
            EventBus.getDefault().post(event);


        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    void checkTask(Header header, byte[] bodyData) {
        try {
            XFileProtocol.File requestFile = XFileProtocol.File.parseFrom(bodyData);

            short sid;
            short cid = SysConstant.CMD_TASK_CHECK_RSP;

            // 判断本地是否有这个taskId,找不到这个taskId返回失败信息,停止传送/续传
            if (SysConstant.TEMP_TASK_ID.equals(requestFile.getTaskId())) {
                sid = SysConstant.SERVICE_TASK_CHECK_SUCCESS;
            } else {
                sid = SysConstant.SERVICE_TASK_CHECK_FAILED;
            }

            writeIndex = requestFile.getPosition();
            writeLength = requestFile.getLength();
            writePercent = writeIndex * 100 / writeLength;

            // 答复发送端创建成功
            if (XFileApplication.connect_type == 1) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, requestFile, header.getSeqnum());
            } else {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, requestFile, null, header.getSeqnum());
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }


    /**
     * 接收保存文件
     */
    void receiveData(Header header, byte[] bodyData) {
        try {
            final XFileProtocol.File requestFile = XFileProtocol.File.parseFrom(bodyData);
            String savePath = XFileUtils.getStoragePathByExtension(requestFile.getExtension());
            String fullPath = savePath + File.separator + requestFile.getFullName();
            byte[] fileData = requestFile.getData().toByteArray();
            RandomAccessFile randomAccessFile = new RandomAccessFile(fullPath, "rw");
            randomAccessFile.seek(requestFile.getPosition());
            randomAccessFile.write(fileData);
            randomAccessFile.close();

            if (requestFile.getPosition() + fileData.length <= requestFile.getLength()) {
                XFileProtocol.File.Builder responseFile = XFileProtocol.File.newBuilder();
                responseFile.setName(requestFile.getName());
                responseFile.setMd5(requestFile.getMd5());
                responseFile.setData(ByteString.copyFrom("1".getBytes()));
                responseFile.setPosition(requestFile.getPosition() + ((long) fileData.length));
                responseFile.setLength(requestFile.getLength());
                responseFile.setPath(requestFile.getPath());
                responseFile.setExtension(requestFile.getExtension());
                responseFile.setFullName(requestFile.getFullName());
                responseFile.setTaskId(requestFile.getTaskId());

                Packetlistener packetlistener = new Packetlistener() {
                    @Override
                    public void onSuccess(short service, Object response) {

                    }

                    @Override
                    public void onFaild() {
                        FileReceiveEvent event = new FileReceiveEvent(FileEvent.SET_FILE_FAILED);
                        TFileInfo tFile = XFileUtils.buildTFile(requestFile);
                        event.setFileInfo(tFile);
                        EventBus.getDefault().post(event);
                    }

                    @Override
                    public void onTimeout() {
                        FileReceiveEvent event = new FileReceiveEvent(FileEvent.SET_FILE_FAILED);
                        TFileInfo tFile = XFileUtils.buildTFile(requestFile);
                        event.setFileInfo(tFile);
                        EventBus.getDefault().post(event);
                    }
                };
                writeIndex = requestFile.getPosition();
                long tempPercent = writeIndex * 100 / requestFile.getLength();
                if (writePercent < tempPercent) {
                    writePercent = tempPercent;
                    // TODO 更新接收状态
                    FileReceiveEvent event = new FileReceiveEvent(FileEvent.SET_FILE);
                    TFileInfo tFile = XFileUtils.buildTFile(requestFile);
                    tFile.setPercent(writePercent);
                    event.setFileInfo(tFile);
                    EventBus.getDefault().post(event);
                }
                short sid = 0;
                if (requestFile.getPosition() + fileData.length == requestFile.getLength()) {
                    // 文件发送完成,提醒接收端结束状态
                    FileReceiveEvent event = new FileReceiveEvent(FileEvent.SET_FILE_SUCCESS);
                    TFileInfo tFile = XFileUtils.buildTFile(requestFile);
                    tFile.setPercent(100);
                    writePercent = 100;
                    event.setFileInfo(tFile);
                    EventBus.getDefault().post(event);
                }
                if (isCancelReceive) {
                    sid = SysConstant.SERVICE_FILE_SET_STOP;
                    // TODO 更新接收状态
                    FileReceiveEvent event = new FileReceiveEvent(FileEvent.SET_FILE_STOP);
                    TFileInfo tFile = XFileUtils.buildTFile(requestFile);
                    tFile.setPercent(writePercent);
                    event.setFileInfo(tFile);
                    EventBus.getDefault().post(event);
                } else {
                    sid = SysConstant.SERVICE_FILE_SET_SUCCESS;
                }

                short cid = SysConstant.CMD_FILE_SET_RSP;
                if (XFileApplication.connect_type == 1) {
                    IMClientFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, header.getSeqnum());
                } else if (XFileApplication.connect_type == 2) {
                    IMServerFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, header.getSeqnum());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    void resumeRsp(final XFileProtocol.File requestFile) {

        Packetlistener packetlistener = new Packetlistener() {
            @Override
            public void onSuccess(short service, Object response) {

            }

            @Override
            public void onFaild() {
                FileReceiveEvent event = new FileReceiveEvent(FileEvent.SET_FILE_FAILED);
                TFileInfo tFile = XFileUtils.buildTFile(requestFile);
                event.setFileInfo(tFile);
                EventBus.getDefault().post(event);
            }

            @Override
            public void onTimeout() {
                FileReceiveEvent event = new FileReceiveEvent(FileEvent.SET_FILE_FAILED);
                TFileInfo tFile = XFileUtils.buildTFile(requestFile);
                event.setFileInfo(tFile);
                EventBus.getDefault().post(event);
            }
        };

        short sid = SysConstant.SERVICE_FILE_SET_SUCCESS;
        short cid = SysConstant.CMD_FILE_SET_RSP;
        if (XFileApplication.connect_type == 1) {
            IMClientFileManager.getInstance().sendMessage(sid, cid, requestFile, packetlistener, (short) 0);
        } else if (XFileApplication.connect_type == 2) {
            IMServerFileManager.getInstance().sendMessage(sid, cid, requestFile, packetlistener, (short) 0);
        }

    }


    /**
     * 暂停接收数据
     */
    public void stopReceive() {
        if (!isCancelReceive) {
            isCancelReceive = true;
        }
    }

    /**
     * 继续接收数据/短点续传
     */
    public void resumeReceive(TFileInfo tFileInfo) {
        if (isCancelReceive) {
            XFileProtocol.File.Builder fileBuilder = XFileProtocol.File.newBuilder();
            fileBuilder.setName(tFileInfo.getName());
            fileBuilder.setMd5(tFileInfo.getMd5());
            fileBuilder.setData(ByteString.copyFrom("1".getBytes()));
            fileBuilder.setPosition(tFileInfo.getPostion());
            fileBuilder.setLength(tFileInfo.getLength());
            fileBuilder.setPath(tFileInfo.getPath());
            fileBuilder.setExtension(tFileInfo.getExtension());
            fileBuilder.setFullName(tFileInfo.getFull_name());
            fileBuilder.setTaskId(tFileInfo.getTask_id());
            fileBuilder.setFrom(Build.MODEL);
            resumeRsp(fileBuilder.build());
            isCancelReceive = false;
        }
    }


    //------------------------------TEST--------------------------------------------

    public void sendFile(File file) {
        try {
            FileInfo sendFile = new FileInfo();
            sendFile.setName(file.getName());
            sendFile.setFileLength(file.length());
            String md5 = XFileUtils.getMd5ByFile(file);
            sendFile.setMd5(md5);
            sendFile.setPath(file.getAbsolutePath());
            sendFile.setPostion(0);
            sendFile.setFileLength(file.length());
            RandomAccessFile sendRAFReader = new RandomAccessFile(file, "r");
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
//            imClientFileManager.sendMessage(SysConstant.SERVICE_DEFAULT, SysConstant.CMD_TRANSER_FILE_SEND, fileBuilder.build());


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
            RandomAccessFile sendRAFReader = new RandomAccessFile("", "");
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
//            imClientFileManager.sendMessage(SysConstant.SERVICE_DEFAULT, SysConstant.CMD_TRANSER_FILE_SEND, fileBuilder.build());


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
