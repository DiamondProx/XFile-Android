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
import com.huangjiang.message.base.Header;
import com.huangjiang.message.protocol.XFileProtocol;
import com.huangjiang.utils.Logger;
import com.huangjiang.utils.XFileUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 文件管理
 */
public class IMFileManager extends IMBaseManager {

    private Logger logger = Logger.getLogger(IMFileManager.class);

    // 发送端记录位置
    private long readIndex;
    private long readPercent;

    // 接收端记录位置
    private long writeIndex;
    private long writePercent;

    private boolean isTransmit = false;
    private boolean isCancel = false;
    private boolean isCancelReceive = false;

    private List<XFileProtocol.File> taskFile = new ArrayList<>();


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
        final TFileInfo reqFile = XFileUtils.buildTFile(createTask);
        Packetlistener packetlistener = new Packetlistener() {
            @Override
            public void onSuccess(short serviceId, Object response) {

                if (serviceId != SysConstant.SERVICE_FILE_NEW_SUCCESS || response == null) {
                    reqFile.setFileEvent(FileEvent.CREATE_FILE_FAILED);
                    triggerEvent(reqFile);
                    return;
                }
                try {
                    // 创建文件成功,检查是否有传输任务,如果正在传输,添加缓存列表
                    taskFile.add(createTask);

                    reqFile.setFileEvent(FileEvent.CREATE_FILE_SUCCESS);
                    triggerEvent(reqFile);
                    logger.e("****createTaskSuccess");

                    if (!isTransmit) {
                        // 没有正在传输的任务,直接校验任务合法性
                        isTransmit = true;
                        checkTask(createTask);
                    } else {
                        // 正在传输文件,保存任务为准备传输状态
                        // saveTask();
                        // 发送Event消息,通知界面
                        reqFile.setFileEvent(FileEvent.WAITING);
                        triggerEvent(reqFile);
                        logger.e("****createTaskSuccess");
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    logger.e(e.getMessage());
                }

            }

            @Override
            public void onFaild() {
                reqFile.setFileEvent(FileEvent.CREATE_FILE_FAILED);
                triggerEvent(reqFile);
            }

            @Override
            public void onTimeout() {
                reqFile.setFileEvent(FileEvent.CREATE_FILE_FAILED);
                triggerEvent(reqFile);
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
        final TFileInfo reqFile = XFileUtils.buildTFile(checkTask);
        Packetlistener packetlistener = new Packetlistener() {
            @Override
            public void onSuccess(short serviceId, Object response) {
                if (serviceId != SysConstant.SERVICE_TASK_CHECK_SUCCESS || response == null) {
                    reqFile.setFileEvent(FileEvent.CHECK_TASK_FAILED);
                    triggerEvent(reqFile);
                    return;
                }
                try {
                    reqFile.setFileEvent(FileEvent.CHECK_TASK_SUCCESS);
                    triggerEvent(reqFile);
                    logger.e("***checkTaskSuccess");

                    // 判断是接受者还是发送者,只有接受者才能暂停,发送者直接取消操作,所以isSend只能是true
                    if (checkTask.getIsSend()) {
                        // 如果当前是发送者,继续发送
                        readPercent = 0;
                        transferFile(checkTask);
                    }

                } catch (Exception e) {
                    reqFile.setFileEvent(FileEvent.CHECK_TASK_FAILED);
                    triggerEvent(reqFile);
                    e.printStackTrace();
                    logger.e(e.getMessage());
                }

            }

            @Override
            public void onFaild() {
                reqFile.setFileEvent(FileEvent.CHECK_TASK_FAILED);
                triggerEvent(reqFile);
            }

            @Override
            public void onTimeout() {
                reqFile.setFileEvent(FileEvent.CHECK_TASK_FAILED);
                triggerEvent(reqFile);
            }
        };
        if (XFileApplication.connect_type == 1) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, checkTask, packetlistener, (short) 0);
        } else if (XFileApplication.connect_type == 2) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, checkTask, packetlistener, (short) 0);
        }
        logger.e("****checkTaskSend");
    }


    /**
     * 发送文件
     */
    public void transferFile(final XFileProtocol.File reqFile) {
        short cid = SysConstant.CMD_FILE_SET;
        short sid = SysConstant.SERVICE_DEFAULT;
        final TFileInfo reqTFile = XFileUtils.buildTFile(reqFile);
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(reqFile.getPath(), "r");
            byte[] readBytes;
            long remain = reqFile.getLength() - reqFile.getPosition();

            // 判断传回来的postion是否等于文件length，相等的情况下，当作已经传输完成
            if (remain == 0) {
                reqTFile.setFileEvent(FileEvent.SET_FILE_SUCCESS);
                reqTFile.setPercent(100);
                readPercent = 100;
                isTransmit = false;
                triggerEvent(reqTFile);
                return;
            }

            if (remain >= SysConstant.FILE_SEGMENT_SIZE) {
                readBytes = new byte[SysConstant.FILE_SEGMENT_SIZE];
            } else {
                readBytes = new byte[(int) remain];
            }
            randomAccessFile.seek(reqFile.getPosition());
            randomAccessFile.read(readBytes);
            ByteString byteString = ByteString.copyFrom(readBytes);
            XFileProtocol.File.Builder responseFile = reqFile.toBuilder();
            responseFile.setData(byteString);
//            responseFile.setFrom(Build.MODEL);
//            responseFile.setPosition(readBytes.length + responseFile.getPosition());
            Packetlistener packetlistener = new Packetlistener() {
                @Override
                public void onSuccess(short serviceId, Object response) {
                    if (response == null) {
                        reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                        triggerEvent(reqTFile);
                        return;
                    }

                    try {
                        byte[] rsp = (byte[]) response;
                        XFileProtocol.File rspFile = XFileProtocol.File.parseFrom(rsp);
                        TFileInfo rspTFile = XFileUtils.buildTFile(rspFile);

                        // 通知界面进度
                        readIndex = rspFile.getPosition();
                        long temPercent = readIndex * 100 / rspFile.getLength();
                        if (readPercent < temPercent) {
                            readPercent = temPercent;
                            rspTFile.setFileEvent(FileEvent.SET_FILE);
                            rspTFile.setPercent(readPercent);
                            triggerEvent(rspTFile);
                        }
                        // 收到暂停标记
                        if (serviceId == SysConstant.SERVICE_FILE_SET_STOP) {
                            rspTFile.setFileEvent(FileEvent.SET_FILE_STOP);
                            rspTFile.setPercent(readPercent);
                            triggerEvent(rspTFile);
                            // 对方暂停接收文件
                            isCancel = true;
                            isTransmit = false;
                        }

                        if (!isCancel) {
                            // 正常收到答复继续传送文件
                            transferFile(rspFile);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.e(e.getMessage());
                    }

                }

                @Override
                public void onFaild() {
                    reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                    triggerEvent(reqTFile);
                }

                @Override
                public void onTimeout() {
                    reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                    triggerEvent(reqTFile);
                }
            };
            if (XFileApplication.connect_type == 1) {
                IMClientFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, (short) 0);
            } else if (XFileApplication.connect_type == 2) {
                IMServerFileManager.getInstance().sendMessage(sid, cid, responseFile.build(), packetlistener, (short) 0);
            }


            // 保存数据库进度

//            logger.e("****setFileSend");
        } catch (Exception e) {
            reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
            triggerEvent(reqTFile);
            e.printStackTrace();
            logger.e(e.getMessage());
        }

    }


    public void dispatchMessage(Header header, byte[] bodyData) {
        switch (header.getCommandId()) {
            case SysConstant.CMD_FILE_NEW:
                // 新建接收文件任务
                dispatchCreateTask(header, bodyData);
                break;
            case SysConstant.CMD_TASK_CHECK:
                // 检查任务是否存在/任务存在进入文件传输
                dispatchCheckTask(header, bodyData);
                break;
            case SysConstant.CMD_FILE_SET:
                // 保存文件/断点续传
                dispatchReceiveData(header, bodyData);
                break;
            case SysConstant.CMD_FILE_RESUME:
                // 断点续传
                dispatchResume(bodyData);
                break;
            case SysConstant.CMD_FILE_CANCEL:
                dispatchCancel(bodyData);
                break;
        }
    }

    void dispatchCreateTask(Header header, byte[] bodyData) {
        try {
            XFileProtocol.File reqFile = XFileProtocol.File.parseFrom(bodyData);
            // 判断是否存在文件,如果不存在则创建
            String fullPath = XFileUtils.getStoragePathByExtension(reqFile.getExtension()) + reqFile.getFullName();
            File saveFile = new File(fullPath);
            if (!saveFile.getParentFile().exists()) {
                saveFile.getParentFile().mkdirs();
            }
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }
            // 保存接收方数据库,记录状态
            // saveTask();
            // 答复发送端创建成功
            XFileProtocol.File.Builder rspFile = reqFile.toBuilder();
            rspFile.setFrom(Build.MODEL);
            rspFile.setIsSend(false);
            TFileInfo rspTFile = XFileUtils.buildTFile(rspFile.build());
            short sid = SysConstant.SERVICE_FILE_NEW_SUCCESS;
            short cid = SysConstant.CMD_FILE_NEW_RSP;

            if (XFileApplication.connect_type == 1) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, rspFile.build(), header.getSeqnum());
            } else {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, rspFile.build(), null, header.getSeqnum());
            }
            // 创建成功
            rspTFile.setFileEvent(FileEvent.CREATE_FILE_SUCCESS);
            triggerEvent(rspTFile);
            taskFile.add(rspFile.build());

            if (isTransmit) {
                // 发送Event消息,通知界面
                rspTFile.setFileEvent(FileEvent.WAITING);
                triggerEvent(rspTFile);
            }


        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    void dispatchCheckTask(Header header, byte[] bodyData) {
        try {
            XFileProtocol.File requestFile = XFileProtocol.File.parseFrom(bodyData);

            short sid;
            short cid = SysConstant.CMD_TASK_CHECK_RSP;

            // 判断本地是否有这个taskId,找不到这个taskId返回失败信息,停止传送/续传
            XFileProtocol.File rspFile = verifyTask(requestFile);
            if (rspFile != null) {
                sid = SysConstant.SERVICE_TASK_CHECK_SUCCESS;
            } else {
                sid = SysConstant.SERVICE_TASK_CHECK_FAILED;
            }

            // 答复发送端创建成功
            if (XFileApplication.connect_type == 1) {
                IMClientMessageManager.getInstance().sendMessage(sid, cid, requestFile, header.getSeqnum());
            } else {
                IMServerMessageManager.getInstance().sendMessage(sid, cid, requestFile, null, header.getSeqnum());
            }
            isTransmit = true;


        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }


    /**
     * 接收保存文件
     */
    void dispatchReceiveData(Header header, byte[] bodyData) {
        try {
            final XFileProtocol.File reqFile = XFileProtocol.File.parseFrom(bodyData);
            final TFileInfo reqTFile = XFileUtils.buildTFile(reqFile);
            String fullPath = XFileUtils.getStoragePathByExtension(reqFile.getExtension()) + File.separator + reqFile.getFullName();
            byte[] fileData = reqFile.getData().toByteArray();
            RandomAccessFile randomAccessFile = new RandomAccessFile(fullPath, "rw");
            randomAccessFile.seek(reqFile.getPosition());
            randomAccessFile.write(fileData);
            randomAccessFile.close();

            if (reqFile.getPosition() + fileData.length <= reqFile.getLength()) {
                XFileProtocol.File.Builder responseFile = XFileProtocol.File.newBuilder();
                responseFile.setName(reqFile.getName());
                responseFile.setMd5(reqFile.getMd5());
                responseFile.setPosition(reqFile.getPosition() + ((long) fileData.length));
                responseFile.setLength(reqFile.getLength());
                responseFile.setPath(reqFile.getPath());
                responseFile.setExtension(reqFile.getExtension());
                responseFile.setFullName(reqFile.getFullName());
                responseFile.setTaskId(reqFile.getTaskId());
                responseFile.setFrom(Build.MODEL);

                Packetlistener packetlistener = new Packetlistener() {
                    @Override
                    public void onSuccess(short service, Object response) {

                    }

                    @Override
                    public void onFaild() {
                        reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                        triggerEvent(reqTFile);
                    }

                    @Override
                    public void onTimeout() {
                        reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                        triggerEvent(reqTFile);
                    }
                };
                writeIndex = reqFile.getPosition();
                long tempPercent = writeIndex * 100 / reqFile.getLength();
                if (writePercent < tempPercent) {
                    writePercent = tempPercent;
                    // 更新接收状态
                    reqTFile.setFileEvent(FileEvent.SET_FILE);
                    reqTFile.setPercent(writePercent);
                    triggerEvent(reqTFile);
                }
                short sid;
                if (reqFile.getPosition() + fileData.length == reqFile.getLength()) {
                    // 文件发送完成,提醒接收端结束状态

                    writePercent = 100;
                    isTransmit = false;
                    reqTFile.setFileEvent(FileEvent.SET_FILE_SUCCESS);
                    reqTFile.setPercent(writePercent);
                    triggerEvent(reqTFile);
                }
                if (isCancelReceive) {
                    sid = SysConstant.SERVICE_FILE_SET_STOP;
                    // 暂停操作
                    reqTFile.setFileEvent(FileEvent.SET_FILE_STOP);
                    reqTFile.setPercent(writePercent);
                    triggerEvent(reqTFile);
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

    void dispatchResume(byte[] bodyData) {
        try {
            final XFileProtocol.File requestFile = XFileProtocol.File.parseFrom(bodyData);
            isCancel = false;
            readIndex = requestFile.getPosition();
            readPercent = readIndex * 100 / requestFile.getLength();
            transferFile(requestFile);
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    void dispatchCancel(byte[] bodyData) {
        try {
            final XFileProtocol.File requestFile = XFileProtocol.File.parseFrom(bodyData);
            final TFileInfo reqTFile = XFileUtils.buildTFile(requestFile);
            if (requestFile.getIsSend()) {
                reqTFile.setFileEvent(FileEvent.CANCEL_FILE);
                triggerEvent(reqTFile);
            } else {
                reqTFile.setFileEvent(FileEvent.CANCEL_FILE);
                triggerEvent(reqTFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.e(e.getMessage());
        }
    }

    void resumeReq(final XFileProtocol.File requestFile) {
        final TFileInfo reqTFile = XFileUtils.buildTFile(requestFile);
        Packetlistener packetlistener = new Packetlistener() {
            @Override
            public void onSuccess(short service, Object response) {

            }

            @Override
            public void onFaild() {
                reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                triggerEvent(reqTFile);
            }

            @Override
            public void onTimeout() {
                reqTFile.setFileEvent(FileEvent.SET_FILE_FAILED);
                triggerEvent(reqTFile);
            }
        };
        // 重新定位写入文件标记
        writeIndex = requestFile.getPosition();
        writePercent = writeIndex * 100 / requestFile.getLength();
        short sid = SysConstant.SERVICE_DEFAULT;
        short cid = SysConstant.CMD_FILE_RESUME;
        if (XFileApplication.connect_type == 1) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, requestFile, packetlistener, (short) 0);
        } else if (XFileApplication.connect_type == 2) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, requestFile, packetlistener, (short) 0);
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
            fileBuilder.setPosition(tFileInfo.getPosition());
            fileBuilder.setLength(tFileInfo.getLength());
            fileBuilder.setPath(tFileInfo.getPath());
            fileBuilder.setExtension(tFileInfo.getExtension());
            fileBuilder.setFullName(tFileInfo.getFullName());
            fileBuilder.setTaskId(tFileInfo.getTaskId());
            fileBuilder.setFrom(Build.MODEL);
            resumeReq(fileBuilder.build());
            isCancelReceive = false;
        }
    }

    public void cancelTask(final TFileInfo tFileInfo) {

        final XFileProtocol.File requestFile = XFileUtils.buildSendFile(tFileInfo);
        if (verifyTask(XFileUtils.buildSendFile(tFileInfo)) == null) {
            return;
        }
        Packetlistener packetlistener = new Packetlistener() {
            @Override
            public void onSuccess(short service, Object response) {

            }

            @Override
            public void onFaild() {
                tFileInfo.setFileEvent(FileEvent.SET_FILE_FAILED);
                triggerEvent(tFileInfo);
            }

            @Override
            public void onTimeout() {
                tFileInfo.setFileEvent(FileEvent.SET_FILE_FAILED);
                triggerEvent(tFileInfo);
            }
        };
        // 重新定位写入文件标记
        short sid = SysConstant.SERVICE_DEFAULT;
        short cid = SysConstant.CMD_FILE_CANCEL;
        if (XFileApplication.connect_type == 1) {
            IMClientMessageManager.getInstance().sendMessage(sid, cid, requestFile, packetlistener, (short) 0);
        } else if (XFileApplication.connect_type == 2) {
            IMServerMessageManager.getInstance().sendMessage(sid, cid, requestFile, packetlistener, (short) 0);
        }
        tFileInfo.setFileEvent(FileEvent.CANCEL_FILE);
        triggerEvent(tFileInfo);

    }

    private XFileProtocol.File verifyTask(XFileProtocol.File requestFile) {
        for (XFileProtocol.File file : taskFile) {
            if (file.getTaskId().equals(requestFile.getTaskId())) {
                return file;
            }
        }
        return null;
    }

    private void triggerEvent(TFileInfo tFileInfo) {
        EventBus.getDefault().post(tFileInfo);
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
