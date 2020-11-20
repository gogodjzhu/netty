package io.netty.example.feature.demo.stateful;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Sharable // 注意增加了此注解, 否则不能在多个channel中复用
public class StatefulWithVariableHandler extends ChannelDuplexHandler {

    private static final Logger LOG = LoggerFactory.getLogger(StatefulWithVariableHandler.class);

    // 结合@Sharable, 构造一个在多个Channel之间共享的counter计数器
    private Integer counter = 0;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        while (byteBuf.isReadable()) {
            LOG.info("channelRead:{}", ((ByteBuf)msg).readInt());
            counter++;
        }
        ByteBuf ret = Unpooled.buffer();
        ret.writeInt(counter);

        ctx.writeAndFlush(ret);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        LOG.info("channelReadComplete");
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
