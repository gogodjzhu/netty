package io.netty.example.feature.demo.stateful;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class StatefulClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "256"));

    public static void main(String[] args) throws Exception {

        // Configure the client.
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new SimpleStatefulHandler(new Random().nextInt(5)));
                        }
                    });

            // Start the client.
            ChannelFuture f = b.connect(HOST, PORT).sync();

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            // Shut down the event loop to terminate all threads.
            group.shutdownGracefully();
        }
    }

    public static class SimpleStatefulHandler extends ChannelDuplexHandler {

        private final static Logger LOG = LoggerFactory.getLogger(SimpleStatefulHandler.class);

        private final ByteBuf message;

        public SimpleStatefulHandler(Integer messageNumber) {
            LOG.info("going to send messageNumber:{}", messageNumber);

            ByteBuf message = Unpooled.buffer(); // TODO how to estimate
            Random random = new Random();
            while (messageNumber-->0) {
                int i = random.nextInt(100);
                LOG.info("send message:{}", i);
                message.writeInt(i);
            }
            this.message = message;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            if (message.writerIndex() > 0) {
                ctx.writeAndFlush(message);
            } else {
                LOG.info("nothing to send...");
                ctx.close();
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            ByteBuf byteBuf = (ByteBuf) msg;
            while (byteBuf.isReadable()) {
                LOG.info("channelRead:{}", ((ByteBuf)msg).readInt());
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            LOG.info("channelReadComplete");
            ctx.close();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            LOG.info("channelInactive");
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }
    }
}
