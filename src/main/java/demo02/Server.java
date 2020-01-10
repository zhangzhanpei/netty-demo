package demo02;

import demo02.handler.HelloServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.util.LinkedList;
import java.util.List;

public class Server {
    static int port = 6666;
    public static List<SocketChannel> socks;

    static {
        socks = new LinkedList<>();
    }

    public static void main(String[] args) throws Exception {
        // 创建两个线程组 bossGroup 和 workerGroup
        // bossGroup 只处理连接请求，workerGroup 处理业务请求
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // 使用 NioServerSocketChannel 作为服务端通道实现
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            socks.add(ch);
                            ch.pipeline().addLast(new HelloServerHandler());
                        }
                    });
            // 绑定端口启动服务
            ChannelFuture future = serverBootstrap.bind(port).sync();
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
