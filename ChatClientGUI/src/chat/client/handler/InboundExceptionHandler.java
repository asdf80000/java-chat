package chat.client.handler;

import io.netty.channel.ChannelHandler.Sharable;

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
		System.out.println("[" + isa.getHostString() + "] Disconnected with error.");
		System.out.println(cause.getClass().getName());
		cause.printStackTrace();
		ctx.close();
	}
}
