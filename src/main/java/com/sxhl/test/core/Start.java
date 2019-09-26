//package com.sxhl.test.core;
//
//import java.net.InetSocketAddress;
//import java.util.NoSuchElementException;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.apache.rocketmq.remoting.common.RemotingUtil;
//import org.apache.rocketmq.remoting.common.TlsMode;
//import org.apache.rocketmq.remoting.netty.FileRegionEncoder;
//import org.apache.rocketmq.remoting.netty.NettyDecoder;
//import org.apache.rocketmq.remoting.protocol.RemotingCommand;
//import org.dc.penguin.core.DataServerHandler;
//import org.dc.penguin.core.ElectionServerHandler;
//
//import gzh.nettymaxthread.ConnectionCountHandler;
//import io.netty.bootstrap.ServerBootstrap;
//import io.netty.buffer.ByteBuf;
//import io.netty.channel.ChannelDuplexHandler;
//import io.netty.channel.ChannelFutureListener;
//import io.netty.channel.ChannelHandlerContext;
//import io.netty.channel.ChannelInitializer;
//import io.netty.channel.ChannelOption;
//import io.netty.channel.ChannelPipeline;
//import io.netty.channel.EventLoopGroup;
//import io.netty.channel.SimpleChannelInboundHandler;
//import io.netty.channel.epoll.Epoll;
//import io.netty.channel.epoll.EpollEventLoopGroup;
//import io.netty.channel.epoll.EpollServerSocketChannel;
//import io.netty.channel.nio.NioEventLoopGroup;
//import io.netty.channel.socket.SocketChannel;
//import io.netty.channel.socket.nio.NioServerSocketChannel;
//import io.netty.handler.codec.DelimiterBasedFrameDecoder;
//import io.netty.handler.codec.Delimiters;
//import io.netty.handler.codec.string.StringDecoder;
//import io.netty.handler.codec.string.StringEncoder;
//import io.netty.handler.timeout.IdleStateHandler;
//import io.netty.util.concurrent.DefaultEventExecutorGroup;
//
//public class Start {
//	public static void main(String[] args) {
//		EventLoopGroup eventLoopGroupBoss = new NioEventLoopGroup();
//        EventLoopGroup eventLoopGroupSelector = new NioEventLoopGroup();
//
//        
//        if (useEpoll()) {
//            eventLoopGroupBoss = new EpollEventLoopGroup(1, new ThreadFactory() {
//                private AtomicInteger threadIndex = new AtomicInteger(0);
//
//                @Override
//                public Thread newThread(Runnable r) {
//                    return new Thread(r, String.format("NettyEPOLLBoss_%d", this.threadIndex.incrementAndGet()));
//                }
//            });
//
//            eventLoopGroupSelector = new EpollEventLoopGroup(3, new ThreadFactory() {
//                private AtomicInteger threadIndex = new AtomicInteger(0);
//                private int threadTotal = 3;
//
//                @Override
//                public Thread newThread(Runnable r) {
//                    return new Thread(r, String.format("NettyServerEPOLLSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
//                }
//            });
//        } else {
//        	eventLoopGroupBoss = new NioEventLoopGroup(1, new ThreadFactory() {
//                private AtomicInteger threadIndex = new AtomicInteger(0);
//
//                @Override
//                public Thread newThread(Runnable r) {
//                    return new Thread(r, String.format("NettyNIOBoss_%d", this.threadIndex.incrementAndGet()));
//                }
//            });
//
//            eventLoopGroupSelector = new NioEventLoopGroup(3, new ThreadFactory() {
//                private AtomicInteger threadIndex = new AtomicInteger(0);
//                private int threadTotal = 3;
//
//                @Override
//                public Thread newThread(Runnable r) {
//                    return new Thread(r, String.format("NettyServerNIOSelector_%d_%d", threadTotal, this.threadIndex.incrementAndGet()));
//                }
//            });
//        }
//        
//        DefaultEventExecutorGroup defaultEventExecutorGroup = new DefaultEventExecutorGroup(8,
//                new ThreadFactory() {
//                    private AtomicInteger threadIndex = new AtomicInteger(0);
//                    @Override
//                    public Thread newThread(Runnable r) {
//                        return new Thread(r, "NettyServerCodecThread_" + this.threadIndex.incrementAndGet());
//                    }
//                });
//        ServerBootstrap serverBootstrap = new ServerBootstrap();
//        serverBootstrap.group(eventLoopGroupBoss, eventLoopGroupSelector)
//        .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
//        .option(ChannelOption.SO_BACKLOG, 1024)
//        .option(ChannelOption.SO_REUSEADDR, true)
//        .option(ChannelOption.SO_KEEPALIVE, false)
//        .childOption(ChannelOption.TCP_NODELAY, true)
//        .childOption(ChannelOption.SO_SNDBUF, 65535)
//        .childOption(ChannelOption.SO_RCVBUF, 65535)
//        .localAddress(new InetSocketAddress(8888))
//        .childHandler(new ChannelInitializer<SocketChannel>() {
//            @Override
//            public void initChannel(SocketChannel ch) throws Exception {
//            	ch.config().setAllowHalfClosure(true);
//        		ChannelPipeline pipeline = ch.pipeline();
//
//        		//IdleStateHandler 与客户端链接后，根据超出配置的时间自动触发userEventTriggered
//        		//readerIdleTime服务端长时间没有读到数据，则为读空闲，触发读空闲监听，并自动关闭链路连接，周期性按readerIdleTime的超时间触发空闲监听方法
//        		//writerIdleTime服务端长时间没有发送写请求，则为空闲，触发写空闲监听,空闲期间，周期性按writerIdleTime的超时间触发空闲监听方法
//        		//allIdleTime 服务端在allIdleTime时间内未接收到客户端消息，或者，也未去向客户端发送消息，则触发周期性操作
//        		pipeline.addLast("ping", new IdleStateHandler(10, 20, 35, TimeUnit.SECONDS));
//        		// 以("\n")为结尾分割的 解码器
//        		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
//        		// 字符串解码 和 编码
//        		pipeline.addLast("decoder", new StringDecoder());
//        		pipeline.addLast("encoder", new NettyDecoder());
//        		// 自己的逻辑Handler
//        		pipeline.addLast("handler", new DataServerHandler(nodeInfo));
//            }
//        });
//        
//        bootstrap.group(eventLoopGroupBoss, eventLoopGroupSelector);
//        bootstrap.channel(NioServerSocketChannel.class);
//        bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
//
//        bootstrap.childHandler(new ConnectionCountHandler());
//
//        /**
//         * 绑定100个端口号
//         */
//        for (int i = 0; i < nPort; i++) {
//            int port = beginPort + i;
//            bootstrap.bind(port).addListener((ChannelFutureListener) future -> { // 同一个服务器可以监听多个端口
//                System.out.println("bind success in port: " + port);
//            });
//        }
//        System.out.println("server started!");
//	}
//	
//	private static boolean useEpoll() {
//        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
//    }
//}
