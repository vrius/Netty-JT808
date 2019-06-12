package com.discovery.nettytest.client;

import com.discovery.nettytest.TLog;
import com.discovery.nettytest.coder.JT808EscapeDecoder;
import com.discovery.nettytest.coder.JT808EscapeEncoder;
import com.discovery.nettytest.coder.JT808WrapperDecoder;
import com.discovery.nettytest.coder.JT808WrapperEncoder;
import com.discovery.nettytest.coder.NettyConstant;
import com.discovery.nettytest.coder.PackageConversion;
import com.discovery.nettytest.entity.PackageData;
import java.util.concurrent.TimeUnit;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Created by ruanwenjiang
 * on 19-5-15
 */
public class NettyClientManager {
    public static final String TAG = "NettyClientManager";
    public static String mConnIP;
    public static int mConnPort;
    public static Channel mChanel;

    private NettyClientManager() {
    }

    public static class INettyClientManager{
        public static final NettyClientManager NETTY_CLIENT_MANAGER = new NettyClientManager();
    }

    public static NettyClientManager instance(){
        return INettyClientManager.NETTY_CLIENT_MANAGER;
    }
    
    public void startClient(String connIP,int connPort){
        this.mConnIP = connIP;
        this.mConnPort = connPort;

        new NettyClientThread().start();
    }
    
    public static class NettyClientThread extends Thread{
        
        @Override
        public void run() {
            super.run();

            NioEventLoopGroup workerGroup = new NioEventLoopGroup();
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_RCVBUF, NettyConstant.MAX_BUFF)
                    .option(ChannelOption.SO_SNDBUF, NettyConstant.MAX_BUFF)
                    .handler(new ClientChannelInitializer());

            try {
                //连接至远程节点，并阻塞直至连接完成
                ChannelFuture future = bootstrap.connect(mConnIP, mConnPort).sync();
                mChanel = future.channel();
                TLog.d(TAG,"客户端连接成功");
                //阻塞线程知道channel关闭（由于调用了sync方法）
                mChanel.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                workerGroup.shutdownGracefully();
            }
        }
    }

    public static class ClientChannelInitializer extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new LoggingHandler());
            //10秒读空闲就会回调userEventTriggered方法
            pipeline.addLast("ping", new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
            pipeline.addLast("framer",new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(new byte[]{0x7e})));
            pipeline.addLast(new JT808EscapeDecoder());
            pipeline.addLast(new JT808EscapeEncoder());
            pipeline.addLast(new JT808WrapperDecoder());
            pipeline.addLast(new JT808WrapperEncoder());
            pipeline.addLast(new PackageConversion());
            pipeline.addLast(new NettyClientHandler());
        }
    }


    public void sendMsg(PackageData data){
        if (mChanel != null && data != null) {
            mChanel.writeAndFlush(data).addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    TLog.d(TAG,"operationComplete:"+future.isSuccess());
                }
            });
        }
    }
}
