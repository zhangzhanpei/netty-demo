package demo02.handler;

import demo02.Server;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

public class HelloServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println("收到客户端消息 " + buf.toString(CharsetUtil.UTF_8));

        // 假如是个耗时任务，放入 EventLoop 的 TaskQueue 异步执行
        ctx.channel().eventLoop().execute(() -> {
            try {
                Thread.sleep(10 * 1000);
                ctx.writeAndFlush(Unpooled.copiedBuffer("耗时任务一执行完毕", CharsetUtil.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        // 加入 TaskQueue 的任务是顺序执行的，不是并发执行，总执行时间为所有任务的执行时间之和
        ctx.channel().eventLoop().execute(() -> {
            try {
                Thread.sleep(10 * 1000);
                ctx.writeAndFlush(Unpooled.copiedBuffer("耗时任务二执行完毕", CharsetUtil.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 可以将任务加入 scheduleTaskQueue 延时队列中，延后执行
        ctx.channel().eventLoop().schedule(() -> {
            try {
                ctx.writeAndFlush(Unpooled.copiedBuffer("延时任务一执行完毕", CharsetUtil.UTF_8));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 5, TimeUnit.SECONDS);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        // ctx.writeAndFlush(Unpooled.copiedBuffer("world", CharsetUtil.UTF_8));
        // 逐个连接发送消息
        for (SocketChannel ch : Server.socks) {
            ch.pipeline().channel().writeAndFlush(Unpooled.copiedBuffer("群发消息", CharsetUtil.UTF_8));
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }
}
