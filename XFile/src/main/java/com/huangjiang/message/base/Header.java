package com.huangjiang.message.base;


import com.huangjiang.config.SysConstant;
import com.huangjiang.utils.SequenceNumberMaker;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 协议头文件
 */
public class Header {


    private int length; // 数据包长度，包括包头

    private short version; // 版本号

    private short serviceId; // SID

    private short commandId; // CID

    private short seqnum;

    public Header() {

        length = 0;

        version = 0;

        serviceId = 0;

        commandId = 0;

        seqnum = SequenceNumberMaker.getInstance().make();

    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public short getVersion() {
        return version;
    }

    public void setVersion(short version) {
        this.version = version;
    }

    public short getCommandId() {
        return commandId;
    }

    public void setCommandId(short commandId) {
        this.commandId = commandId;
    }

    public short getServiceId() {
        return serviceId;
    }

    public void setServiceId(short serviceId) {
        this.serviceId = serviceId;
    }

    public short getSeqnum() {
        return seqnum;
    }

    public void setSeqnum(short seqnum) {
        this.seqnum = seqnum;
    }

    public byte[] toByteArray() {
        ByteBuf byteBuf = Unpooled.buffer(SysConstant.HEADER_LENGTH);
        byteBuf.writeInt(length);
        byteBuf.writeShort(version);
        byteBuf.writeShort(serviceId);
        byteBuf.writeShort(commandId);
        byteBuf.writeShort(seqnum);
        return byteBuf.array();
    }

    public Header(byte[] bytes) {
        ByteBuf byteBuf = Unpooled.buffer(SysConstant.HEADER_LENGTH);
        byteBuf.writeBytes(bytes);
        this.length = byteBuf.readInt();
        this.version = byteBuf.readShort();
        this.serviceId = byteBuf.readShort();
        this.commandId = byteBuf.readShort();
        this.seqnum = byteBuf.readShort();
        byteBuf.discardReadBytes();
    }

}
