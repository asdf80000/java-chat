package chat.server.handler;

import io.netty.channel.ChannelHandler.Sharable;

import java.io.IOException;
import java.net.InetSocketAddress;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class InboundExceptionHandler extends ChannelInboundHandlerAdapter {
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		//cause.printStackTrace();
		InetSocketAddress isa = (InetSocketAddress) ctx.channel().remoteAddress();
		System.out.println("[" + isa.getHostName() + "] Disconnected with error.");
		if(cause instanceof IOException) {
			System.out.println(cause.getClass().getName() + ": " + cause.getLocalizedMessage());
		}else {
			cause.printStackTrace();
		}
		ctx.close();
	}
}
