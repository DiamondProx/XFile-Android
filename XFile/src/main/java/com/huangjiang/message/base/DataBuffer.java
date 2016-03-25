package com.huangjiang.message.base;


import com.huangjiang.config.SysConstant;

import java.nio.charset.Charset;

import io.netty.buffer.ByteBuf;

/**
 * 数据缓冲区对象(ChannelBuffer)
 *
 * @author Nana
 */
public class DataBuffer {


    private Header header;

    private byte[] bodyData;

    public Header getHeader() {
        return header;
    }

    public byte[] getBodyData() {
        return bodyData;
    }

    public DataBuffer(ByteBuf byteBuf) {
        byte[] byteHeader = byteBuf.readBytes(SysConstant.HEADER_LENGTH).array();
        Header readHeader = new Header(byteHeader);
        byte[] readBody = new byte[readHeader.getLength() - SysConstant.HEADER_LENGTH];
        byteBuf.readBytes(readBody);
        this.header = readHeader;
        this.bodyData = readBody;
    }
}
