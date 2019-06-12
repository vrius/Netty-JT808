package com.discovery.nettytest.coder;

import com.discovery.nettytest.TLog;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author ruanwenjiang
 * @date 19-5-26 上午11:56
 * 消息 计算.填充.转义
 */

public class JT808EscapeEncoder extends MessageToByteEncoder<byte[]> {
    public static final String TAG = "JT808EscapeEncoder";
    @Override
    protected void encode(ChannelHandlerContext ctx, byte[] msg, ByteBuf out) throws Exception {
        encodeNewByte(msg, out);
    }

    private void encodeNewByte(byte[] bytes, ByteBuf byteBuf) {
        byte checkByte = 0;

        //添加头标识
        byteBuf.writeByte(NettyConstant.IDENTIFIER);

        //此处进行所有需要的数据转义，并计算校验码
        for (byte b : bytes) {
            if (b == NettyConstant.BYTE_7D) {
                byteBuf.writeShort(NettyConstant.BYTE_RET_7D);
                checkByte ^= NettyConstant.BYTE_7D;
            } else if (b == NettyConstant.BYTE_7E) {
                byteBuf.writeShort(NettyConstant.BYTE_RET_7E);
                checkByte ^= NettyConstant.BYTE_7E;
            } else {
                byteBuf.writeByte(b);
                checkByte ^= b;
            }
        }
        //校验码转义
        if (checkByte == NettyConstant.BYTE_7D) {
            byteBuf.writeShort(NettyConstant.BYTE_RET_7D);
        } else if (checkByte == NettyConstant.BYTE_7E) {
            byteBuf.writeShort(NettyConstant.BYTE_RET_7E);
        } else {
            byteBuf.writeByte(checkByte);
        }

        //添加结尾标识
        byteBuf.writeByte(NettyConstant.IDENTIFIER);
    }
}
