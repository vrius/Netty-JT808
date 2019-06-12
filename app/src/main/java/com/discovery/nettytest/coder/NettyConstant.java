package com.discovery.nettytest.coder;

public final class NettyConstant {
    /*正式服务器地址*/
    public static final String HOST_RELEASE_IP = "";
    /*测试服务器地址*/
    public static final String HOST_TEST_IP = "";
    /*本地服务器地址*/
    public static final String HOST_LOCAL_IP = "";
    /*连接端口号*/
    public static final int HOST_PORT = 10138;

    /*消息体最大长度,这里就要特别注意了，由于部标808协议用于描述消息体长度只有10个bit，所以消息体的最大长度是不允许大于1023个byte的*/
    public static final short MAX_BODY_LENGTH = 256;
    /*目标长度，不足12个字符前面补0*/
    public static final int TARGET_LENGTH = 12;
    /*netty缓存池大小*/
    public static final int MAX_BUFF = Integer.MAX_VALUE;

    /*消息标识位*/
    public static final byte IDENTIFIER = 0x7e;

    /*消息转义相关*/
    public static final byte BYTE_7E = 0x7e;
    public static final byte BYTE_7D = 0x7d;
    public static final short BYTE_RET_7D = 0x7d01;
    public static final short BYTE_RET_7E = 0x7d02;
    public static final byte BYTE_01 = 0x01;
    public static final byte BYTE_02 = 0x02;

}
