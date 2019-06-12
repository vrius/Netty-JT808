package com.discovery.nettytest.server;

import com.discovery.nettytest.TLog;
import com.discovery.nettytest.coder.JT808EscapeDecoder;
import com.discovery.nettytest.coder.JT808EscapeEncoder;
import com.discovery.nettytest.coder.JT808WrapperDecoder;
import com.discovery.nettytest.coder.JT808WrapperEncoder;
import com.discovery.nettytest.coder.NettyConstant;
import com.discovery.nettytest.coder.PackageConversion;

import java.util.concurrent.TimeUnit;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;

/**
 * Created by ruanwenjiang
 * on 19-5-15
 */
public class NettyServerManager {
    public static final String TAG = "NettyServerManager";
    public static String mBintIP;
    public static int mBindPort;

    private NettyServerManager() {
    }

    public static class INettyServerManager {
        public static final NettyServerManager NETTY_SERVER_MANAGER = new NettyServerManager();
    }

    public static NettyServerManager instance() {
        return INettyServerManager.NETTY_SERVER_MANAGER;
    }

    public void startServer(String bindIP, int bindPort) {
        this.mBintIP = bindIP;
        this.mBindPort = bindPort;

        new NettyServerThread().start();
    }

    public static class NettyServerThread extends Thread {

        @Override
        public void run() {
            super.run();

            NioEventLoopGroup boosGroup = new NioEventLoopGroup();
            NioEventLoopGroup workerGroup = new NioEventLoopGroup();

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boosGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.TCP_NODELAY,true)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.SO_RCVBUF, NettyConstant.MAX_BUFF)
                    .option(ChannelOption.SO_SNDBUF, NettyConstant.MAX_BUFF)
                    //这里的ChannelInitializer很关键，用于初始化每个连接的Channel
                    .childHandler(new ChildChannelInitializer());

            try {
                //该方法会阻塞，知道绑定完成
                ChannelFuture future = bootstrap.bind(mBintIP, mBindPort).sync();

                TLog.d(TAG, "服务器启动成功");
                //获取Channel，并阻塞线程
                future.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                boosGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }
    }

    public static class ChildChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline pipeline = socketChannel.pipeline();
            //这里我们写示例时，直接创建了NettyServerHandler，理论上NettyServerHandler可以定义为全局变量
            //供多个channel使用
            pipeline.addLast(new LoggingHandler());

            //15秒读空闲就会回调userEventTriggered方法，这里是做心跳的关键地方
            pipeline.addLast("ping", new IdleStateHandler(15, 0, 0, TimeUnit.SECONDS));

            //DelimiterBasedFrameDecoder是netty自带的解决TCP粘包拆包问题的
            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer(new byte[]{0x7e})));
            pipeline.addLast(new JT808EscapeDecoder());
            pipeline.addLast(new JT808EscapeEncoder());
            pipeline.addLast(new JT808WrapperDecoder());
            pipeline.addLast(new JT808WrapperEncoder());
            pipeline.addLast(new PackageConversion());
            pipeline.addLast(new NettyServerHandler());
        }
    }
}
