package com.sxhl.test.core;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


public class NettyDecoder extends LengthFieldBasedFrameDecoder {
    private static final Logger log = LoggerFactory.getLogger(NettyDecoder.class);
    private static final int FRAME_MAX_LENGTH = 16777216;

    public NettyDecoder() {
        super(FRAME_MAX_LENGTH, 0, 4, 0, 4);
    }

    @Override
    public Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = null;
        try {
            frame = (ByteBuf) super.decode(ctx, in);
            if (null == frame) {
                return null;
            }

            ByteBuffer byteBuffer = frame.nioBuffer();

            int length = byteBuffer.limit();
            int oriHeaderLen = byteBuffer.getInt();
//            int headerLength = getHeaderLength(oriHeaderLen);
//
//            byte[] headerData = new byte[headerLength];
//            byteBuffer.get(headerData);
//
//            RemotingCommand cmd = headerDecode(headerData, getProtocolType(oriHeaderLen));
//
//            int bodyLength = length - 4 - headerLength;
//            byte[] bodyData = null;
//            if (bodyLength > 0) {
//                bodyData = new byte[bodyLength];
//                byteBuffer.get(bodyData);
//            }
//            cmd.body = bodyData;
//
//            return cmd;
//            
//            return RemotingCommand.decode(byteBuffer);
        } catch (Exception e) {
            log.error("decode exception, ", e);
            RemotingUtil.closeChannel(ctx.channel());
        } finally {
            if (null != frame) {
                frame.release();
            }
        }

        return null;
    }
}
