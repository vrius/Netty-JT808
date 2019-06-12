package com.discovery.nettytest.coder;


import com.discovery.nettytest.TLog;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author ruanwenjiang
 * @date 19-5-26 下午2:10
 * <p>
 * 转义还原.验证校验码
 */

public class JT808EscapeDecoder extends ByteToMessageDecoder {
    public static final String TAG = "JT808EscapeDecoder";

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int length = in.readableBytes();
        if (length <= 0) { return; }

        byte[] bs = new byte[in.readableBytes()];
        in.readBytes(bs);

        ByteBuf byteBuf = Unpooled.buffer();

        byte checkByte = 0;
        byte oldCheckByte = bs[bs.length - 1];
        for (int i = 0; i < bs.length - 1; i++) {
            byte b = bs[i];
            if (b == NettyConstant.BYTE_7D) {//遍历到转义的数据
                i++;
                b = bs[i];
                if (b == NettyConstant.BYTE_01) {//数据为0x7d01--->原数据为7d
                    if (i == bs.length - 1) {//校验位是特殊字符
                        oldCheckByte = NettyConstant.BYTE_7D;
                    } else {
                        byteBuf.writeByte(NettyConstant.BYTE_7D);
                        checkByte ^= NettyConstant.BYTE_7D;
                    }
                } else if (b == NettyConstant.BYTE_02) {//数据为0x7d02--->原数据为7e
                    if (i == bs.length - 1) {//校验位是特殊字符
                        oldCheckByte = NettyConstant.BYTE_7E;
                    } else {
                        byteBuf.writeByte(NettyConstant.BYTE_7E);
                        checkByte ^= NettyConstant.BYTE_7E;
                    }
                } else {
                    //do nothing 理论上程序不可能来到这里，如果程序走到这里表示编码的代码逻辑本身就有问题
                    return;
                }
                continue;
            }
            checkByte ^= b;
            byteBuf.writeByte(b);
        }


        if (checkByte != oldCheckByte) {
            TLog.e(TAG,"数据校验码验证不通过！！！！");
            return;
        }

        out.add(byteBuf);
    }
}
