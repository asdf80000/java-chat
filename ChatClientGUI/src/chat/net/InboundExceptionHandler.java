package chat.net;

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
		System.out.println("[" + isa.getHostString() + "] 처리되지 않은 오류가 발생하여 서버로부터 연결이 끊어졌습니다.");
		System.out.println(cause.getClass().getName());
		cause.printStackTrace();
		ctx.close();
	}
}
