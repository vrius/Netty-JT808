package com.discovery.nettytest.coder;


import com.discovery.nettytest.TLog;
import com.discovery.nettytest.entity.PackageData;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

/**
 * @author ruanwenjiang
 * @date 19-5-14 下午2:17
 * <p>
 * 消息包装.分包
 * 流水号,消息体属性等
 * <p>
 * 确保线程安全
 * 这里我们把所有的消息包信息（byte[]）顺序压入到out中，这样netty处理链就会把消息包也会按
 * 顺序分发到下一个编码器做处理
 */

public class JT808WrapperEncoder extends MessageToMessageEncoder<PackageData> {
    public static final String TAG = "JT808WrapperEncoder";

    @Override
    protected void encode(ChannelHandlerContext ctx, PackageData msg, List<Object> out) throws Exception {
        List<byte[]> bytes = wrapperMsg(msg);
        out.addAll(bytes);
    }

    /**
     * 封装消息
     *
     * @param msg
     * @return
     */
    private List<byte[]> wrapperMsg(PackageData msg) throws NettyWrapperException {
        List<byte[]> list = new ArrayList<>();

        byte[] body = msg.getBody();
        int bodyLen = body != null ? body.length : 0;

        short msgId = msg.getMsgId();
        short msgNum = msg.getMsgNum();

        //target校验长度不足用*代替
        String target = msg.getTarget();
        if (target == null || target.length() == 0) throw new NettyWrapperException("设备标识[target]不能为空!");
        StringBuffer targetBuff = new StringBuffer();
        if (target.length() < NettyConstant.TARGET_LENGTH) {
            short nullStr = (short) (NettyConstant.TARGET_LENGTH - target.length());
            for (int i = 0; i < nullStr; i++) {
                targetBuff.append(" ");
            }
        }
        targetBuff.append(target);
        byte[] targetBytes = targetBuff.toString().getBytes();

        short source = msg.getSource();
        short encryp = msg.getEncryp();

        //没有分包
        if (bodyLen < NettyConstant.MAX_BODY_LENGTH) {
            ByteBuf byteBuf = wrapperByteBuf(msgId, bodyLen, encryp, source, targetBytes, msgNum, 1, 1, body);
            list.add(byteBufToBytes(byteBuf));
            return list;
        }

        /**
         * 走到此处标识需要进行分包操作，这里需要注意的一点是我们需要对剩余数据进行独立处理
         * 当我们的总包数大于255时，JT808协议中用于描述分包总数的字节不足以描述大于255个数据包
         */
        int totalPkg = bodyLen / NettyConstant.MAX_BODY_LENGTH+1;
        int lastBodyLen = bodyLen % NettyConstant.MAX_BODY_LENGTH;

        if (totalPkg > 255) throw new NettyWrapperException("分包总数大于255");

        for (int i = 1; i < totalPkg; i++) {
            ByteBuf wrapperByteBuf = wrapperByteBuf(msgId, NettyConstant.MAX_BODY_LENGTH, encryp, source, targetBytes, msgNum, totalPkg, i, body);
            list.add(byteBufToBytes(wrapperByteBuf));
        }


        //做最后一个包的处理
        ByteBuf wrapperLastByteBuf = wrapperByteBuf(msgId, lastBodyLen, encryp, source, targetBytes, msgNum, totalPkg, totalPkg, body);
        list.add(byteBufToBytes(wrapperLastByteBuf));


        return list;
    }

    /**
     * 进行数据封包操作
     *
     * @param msgid    消息包id
     * @param bodyLen  当前消息包的消息体长度
     * @param source   消息来源
     * @param target   设备标识
     * @param totalPkg 总包数
     * @param pkgNum   包序号
     * @param msg      待操作数据
     * @return :
     */
    private ByteBuf wrapperByteBuf(short msgid, int bodyLen, short encryp, short source, byte[] target, short msgNum, int totalPkg, int pkgNum, byte[] msg) {
        ByteBuf byteBuf = Unpooled.buffer();

        //设置两个字节消息id
        byteBuf.writeShort(msgid);

        /**
         * 这里我们对消息体属性进行封装，也是整个808协议比较难封装的一部分
         */
        short bodyAttr = (short) (bodyLen & 0x03FF);
        bodyAttr |= (encryp << 10);
        if (totalPkg > 1) bodyAttr |= 0x2000;
        bodyAttr |= (source << 15);
        bodyAttr &= 0x7FFF;
        byteBuf.writeShort(bodyAttr);


        byteBuf.writeBytes(target);
        byteBuf.writeShort(msgNum);
        byteBuf.writeByte(totalPkg & 0x000000FF);
        byteBuf.writeByte(pkgNum & 0x000000FF);

        //计算写入位置，并封装消息体
        short startIndex = (short) (--pkgNum * bodyLen);
        byteBuf.writeBytes(msg, startIndex, bodyLen);
        return byteBuf;
    }

    private byte[] byteBufToBytes(ByteBuf byteBuf) {
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return bytes;
    }
}