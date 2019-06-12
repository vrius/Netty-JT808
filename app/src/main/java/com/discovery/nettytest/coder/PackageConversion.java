package com.discovery.nettytest.coder;


import com.discovery.nettytest.entity.Message;
import com.discovery.nettytest.entity.PackageData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;


/**
 * @author ruanwenjiang
 * @date 19-5-17 下午4:49
 * <p>
 * 分包聚合，并转为PackageData实例
 * 这里需要说明一下合包的原理，当我们不往out这个容器压入内容时，netty的处理链
 * 是不会往下走的，这就是合包的关键点了，还有一点就是netty底层走的tcp也会保证
 * 包的发送和接收顺序，这就免去了我们对包顺序整理的这部分逻辑,我们处理合包时
 * 只需要正确的处理好第一个包与最后一个包就可以很好的处理好合包的问题了！
 */

public class PackageConversion extends MessageToMessageDecoder<Message> {
    public static final String TAG = "PackageConversion";
    private Map<Short, PackageBuf> mMap;

    @Override
    protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        if (!msg.isPkg()) {
            PackageData data = new PackageData();
            data.setTarget(msg.getTarget());
            data.setEncryp(msg.getEncryp());
            data.setMsgId(msg.getMsgId());
            data.setSource(msg.getSource());
            data.setBody(msg.getBody());
            data.setMsgNum(msg.getMsgNum());
            out.add(data);
            return;
        }

        short msgId = msg.getMsgId();

        if (mMap == null) {
            mMap = new HashMap<>(msg.getTotalPkg());
        }

        if (!mMap.containsKey(msgId)) {
            if (msg.getPkgNum() == 1) {
                PackageBuf packageBuf = new PackageBuf(msg.getTotalPkg());
                packageBuf.addMessage0(msg);
                mMap.put(msgId, packageBuf);
            }
        } else {
            PackageBuf packageBuf = mMap.get(msgId);
            int ret = packageBuf.addMessage0(msg);
            if (ret == 0) {
                mMap.remove(msgId);
                byte[] bytes = packageBuf.getBufArray();
                PackageData data = new PackageData();
                data.setTarget(msg.getTarget());
                data.setEncryp(msg.getEncryp());
                data.setMsgId(msg.getMsgId());
                data.setSource(msg.getSource());
                data.setBody(bytes);
                data.setMsgNum(msg.getMsgNum());
                out.add(data);
            }
        }
    }

    private class PackageBuf {
        private int maxSize;

        private CompositeByteBuf byteBufs;

        public PackageBuf(int maxSize) {
            this.maxSize = maxSize;
            byteBufs = Unpooled.compositeBuffer(maxSize);
        }


        public int addMessage0(Message message) {
            ByteBuf body = Unpooled.wrappedBuffer(message.getBody());

            //顺序接收
            byteBufs.addComponent(true, body);

            //接收完成
            if (message.getPkgNum() == maxSize) {
                return 0;
            }
            return -1;
        }

        public byte[] getBufArray() {
            byte[] bytes = new byte[byteBufs.readableBytes()];
            byteBufs.readBytes(bytes);
            return bytes;
        }
    }

}
