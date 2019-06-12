package com.discovery.nettytest.coder;


import com.discovery.nettytest.TLog;
import com.discovery.nettytest.entity.Message;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

/**
 * @author ruanwenjiang
 * @date 19-5-17 下午2:39
 * <p>
 * 该解码器在每次netty有入站包时会被调用，我们通过ByteBuf的操作可取出数据包对应的值。
 * 这里要注意我们的数据包格式都是严格按照JT808协议进行的封装。
 * ByteBuf的详细用法请参考netty官方文档。
 * 注意：由于我们接收到的信息有可能需要进行合包操作，所以我们需要同过{@link com.discovery.nettytest.entity.Message}
 * 来进行中间件，方便最后的解码器进行合包的操作，详见{@link PackageConversion}
 * </p>
 */

public class JT808WrapperDecoder extends ByteToMessageDecoder {
    private static final String TAG = "JT808WrapperDecoder";

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        short msgId = in.readShort();
        short bodyAttr = in.readShort();

        /**
         * 此处我们通过与运算对消息体属性进行解析：
         * 具体消息体属性的解析方式需参看数据的封装方式见
         * {@link JT808WrapperEncoder}
         * 1、body长度 取最后10个bit：0x03FF => 0000001111111111
         * 2、加密类型 取第11、12、13个bit：0x1C00 => 0001110000000000
         * 3、是否分包 取第14个bit：0x2000 => 0010000000000000
         * 4、消息来源 取第15个bit：0x4000 => 0100000000000000
         */
        short bodyLen = (short) (bodyAttr & 0x03FF);
        short encryp = (short) ((bodyAttr & 0x1C00) >> 10);
        boolean isPkg = ((bodyAttr & 0x2000) >> 13) != 0;
        short source = (short) ((bodyAttr & 0x4000) >> 14);


        byte[] targetByte = new byte[NettyConstant.TARGET_LENGTH];
        in.readBytes(targetByte);

        short msgNum = in.readShort();
        int pgkSize = in.readByte();
        int pkgNum = in.readByte();

        Message builder = new Message();
        builder.setMsgId(msgId);
        builder.setBodyLen(bodyLen);
        builder.setMsgNum(msgNum);
        builder.setEncryp(encryp);
        builder.setSource(source);
        builder.setTarget(new String(targetByte).trim());
        builder.setTotalPkg(pgkSize);
        builder.setPkgNum(pkgNum);
        builder.setPkg(isPkg);

        byte[] body = new byte[bodyLen];
        in.readBytes(body);

        builder.setBody(body);
        out.add(builder);
    }
}
