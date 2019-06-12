package com.discovery.nettytest.client;

import com.discovery.nettytest.TLog;
import com.discovery.nettytest.coder.NettyConstant;
import com.discovery.nettytest.entity.PackageData;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Created by ruanwenjiang
 * on 19-5-15
 * SimpleChannelInboundHandler与ChannelInboundHandler你可能会想：为什么我们在客户端使用的是
 * SimpleChannelInboundHandler，而不是在Echo-ServerHandler中所使用的ChannelInboundHandlerAdapter呢？
 * 这和两个因素的相互作用有关：业务逻辑如何处理消息以及Netty如何管理资源。在客户端，当channelRead0()
 * 方法完成时，你已经有了传入消息，并且已经处理完它了。当该方法返回时，SimpleChannelInboundHandler负责释放
 * 指向保存该消息的ByteBuf的内存引用。在EchoServerHandler中，你仍然需要将传入消息回送给发送者，而write()操
 * 作是异步的，直到channelRead()方法返回后可能仍然没有完成（如代码清单2-1  所示）。为此，EchoServerHandler
 * 扩展了ChannelInboundHandlerAdapter，其在这个时间点上不会释放消息。消息在EchoServerHandler的
 * channelReadComplete()方法中，当writeAndFlush()方法被调用时被释放（见代码清单 2-1）。
 *
 * 上面这段废话是我再netty的实战那本书上弄下来的，大家不必在意
 */
public class NettyClientHandler extends SimpleChannelInboundHandler<PackageData> {
    public static final String TAG = "NettyClientHandler";

    /*以原子的方式记录当前心跳失败次数*/
    private AtomicInteger num = new AtomicInteger(1);
    /*心跳最大重试次数*/
    private static final int MAX_NUM = 10;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, PackageData data) throws Exception {
        /**
         * 每当接收数据时，都会调用这个方法。需要注意的是，由服务器发送的消息可能会被分块接收。也就是说，
         * 如果服务器发送了5字节，那么不能保证这5字节会被一次性接收。即使是对于这么少量的数据，channelRead0()
         * 方法也可能会被调用两次，第一次使用一个持有3字节的ByteBuf（Netty的字节容器），第二次使用一个持有2字
         * 节的ByteBuf。作为一个面向流的协议，TCP保证了字节数组将会按照服务器发送它们的顺序被接收。
         * 但是上述得问题可以通过netty提供的解码器解决
         */

        TLog.d(TAG,"客户端收到数据："+data.toString());


        if (data.getMsgId() == 0) {
            TLog.d(TAG,"收到心跳响应！");
            if (num != null) num.set(1);//心跳消息，重置消息重发计时器
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        TLog.d(TAG,"channelActive ---> 连接服务端成功！");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        TLog.d(TAG,"channelInactive ---> 连接已断开！");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);

        IdleStateEvent e = (IdleStateEvent) evt;
        if (e.state() == IdleState.READER_IDLE) {

            /**
             * 此时处于读空闲状态，发送心跳消息
             * 所谓的读空闲状态即是初始化netty时所设置的时间
             * */

            if (num.intValue() > MAX_NUM) {
                //这里我们设置一个发心跳的最大值，发的心跳消息操作这个值那么就应该关闭管道然后重新连接或者做其他操作
                ctx.channel().close();
            } else {
                num.getAndIncrement();

                //接着我们假装发一条心跳消息
                PackageData data = new PackageData();
                data.setMsgId((short) 0);
                data.setSource((short) 0);
                data.setEncryp((short) 0);
                data.setTarget("123456789123");
                data.setBody("My name is heard!".getBytes());
                data.setMsgNum((short) 100);
                ctx.channel().writeAndFlush(data).addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        TLog.d(TAG,"发送心跳消息："+future.isSuccess());
                    }
                });
            }

        } else if (e.state() == IdleState.WRITER_IDLE) {
            //这里是读空闲状态
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx,cause);
        cause.printStackTrace();
        TLog.d(TAG, "exceptionCaught ---> netty异常！");
        ctx.close();
    }
}
