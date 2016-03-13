package com.huangjiang.message;



import java.nio.charset.Charset;

/**
 * 数据缓冲区对象(ChannelBuffer)
 * 
 * @author Nana
 */
public class DataBuffer {


	public byte[] intToByteArray (final int integer) {
		int byteNum = (40 - Integer.numberOfLeadingZeros (integer < 0 ? ~integer : integer)) / 8;
		byte[] byteArray = new byte[4];

		for (int n = 0; n < byteNum; n++)
			byteArray[3 - n] = (byte) (integer >>> (n * 8));

		return (byteArray);
	}

	/**
	 * 将16位的short转换成byte数组
	 *
	 * @param s
	 *            short
	 * @return byte[] 长度为2
	 * */
	public byte[] shortToByteArray(short s) {
		byte[] targets = new byte[2];
		for (int i = 0; i < 2; i++) {
			int offset = (targets.length - 1 - i) * 8;
			targets[i] = (byte) ((s >>> offset) & 0xff);
		}
		return targets;
	}
}
