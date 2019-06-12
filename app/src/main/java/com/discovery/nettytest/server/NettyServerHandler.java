package com.discovery.nettytest.server;

import com.discovery.nettytest.TLog;
import com.discovery.nettytest.entity.PackageData;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;


/**
 * Created by ruanwenjiang
 * on 19-5-15
 */
//当我们需要让所有连接的客户端使用同一个NettyServerHandler时，需要加上Sharable注解
//@ChannelHandler.Sharable
public class NettyServerHandler extends SimpleChannelInboundHandler<PackageData> {
    public static final String TAG = "NettyServerHandler";


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        TLog.d(TAG,"通道已连接成功！");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        TLog.d(TAG,"通道断开连接！");
    }



    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PackageData data) throws Exception {
        byte[] body = data.getBody();
        String bodyStr = new String(body);
        TLog.d(TAG, "收到了客户端数据："+data.toString()+";body:"+bodyStr);

        if (data.getMsgId() == 0){
            //接着我们假装发一条心跳响应消息
            PackageData data1 = new PackageData();
            data1.setMsgId((short) 0);
            data1.setSource((short) 0);
            data1.setEncryp((short) 0);
            data1.setTarget("123456789123");
            data1.setBody("我收到了心跳！".getBytes());
            data1.setMsgNum((short) 100);
            ctx.channel().writeAndFlush(data);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        /**
         * 在读取操作期间，有异常抛出时会调用
         */
        cause.printStackTrace();
        ctx.close();
        TLog.d(TAG,cause.getMessage());
    }
}
