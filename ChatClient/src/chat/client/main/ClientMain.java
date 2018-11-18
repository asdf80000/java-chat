package chat.client.main;

import java.util.Scanner;

import chat.client.handler.ChatClientInboundHandler;
import chat.client.handler.ChatClientPacketDecoder;
import chat.client.handler.ChatClientPacketEncoder;
import chat.client.handler.InboundExceptionHandler;
import chat.common.enums.CloseCause;
import chat.common.handler.ChannelState;
import chat.common.handler.ChatCommonInboundPriorityHandler;
import chat.common.handler.ChatCommonPacketPrepender;
import chat.common.handler.ChatCommonPacketSplitter;
import chat.common.main.HandleQueueGeneric;
import chat.common.packet.all.PacketAllSbDisconnect;
import chat.common.packet.user.PacketUserCbSetUsername;
import chat.common.packet.user.PacketUserSbGetUsername;
import chat.common.packet.user.PacketUserSbSetUsername;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientMain {
	public static int PORT = 46435;
	public static String ADDRESS = "127.0.0.1";

	public static ChannelState cs = ChannelState.LOGIN;

	public static void main(String[] args) throws Exception {
		new ClientMain(ADDRESS, PORT);
	}

	public static Scanner f = new Scanner(System.in);
	public ChatClientInboundHandler handle = null;
	public ChatCommonInboundPriorityHandler priority = null;

	public ClientMain(String addr, int port) throws Exception {
		// TODO Auto-generated constructor stub
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioSocketChannel.class).remoteAddress(addr, port)
					.handler(new ChannelInitializer<Channel>() {
						InboundExceptionHandler ieh = new InboundExceptionHandler();

						@Override
						protected void initChannel(Channel ch) throws Exception {
							handle = new ChatClientInboundHandler();
							priority = new ChatCommonInboundPriorityHandler();
							ch.pipeline().addLast("inboundsplit", new ChatCommonPacketSplitter())
									.addLast("outboundprepend", new ChatCommonPacketPrepender())
									.addLast("inboundpacketdecoder", new ChatClientPacketDecoder())
									.addLast("outboundpacketencoder", new ChatClientPacketEncoder())
									.addLast("inboundpriority", priority).addLast("inboundhandle", handle)
									.addLast("inboundexception", ieh);
						};
					});
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (true) {
						System.out.print("Input here: ");
						String cmd = f.next();
						System.out.println();
						System.out.println("Command: " + cmd);
						if (cmd.equals("/close")) {
							System.out.println("Closing connection...");
							PacketAllSbDisconnect p = new PacketAllSbDisconnect();
							p.cc = CloseCause.DISCONNECT;
							try {
								handle.sendPacket(p);
							} catch (Exception e) {
							}
							handle.ctx.close().awaitUninterruptibly().syncUninterruptibly();
							System.out.println("Closed connection. Closing...");
							System.exit(0);
						} else if (cmd.equals("/help")) {
							System.out.println("------------- Help");
							System.out.println("/getnick - 닉네임 출력");
							System.out.println("/setnick - 닉네임 변경");
							System.out.println("/close - 연결 닫기");
							System.out.println("/help - 도움말");
							System.out.println("/match - 매치메이킹 토글");
						} else if (cmd.equals("/getnick")) {
							System.out.println("Processing...");
							PacketUserSbGetUsername p = new PacketUserSbGetUsername();
							p.message = false;
							priority.addQueue(PacketUserCbSetUsername.class,
									new HandleQueueGeneric<PacketUserCbSetUsername>() {

										@Override
										public void run(PacketUserCbSetUsername p) {
											System.out.println("당신의 사용자 이름은 다음과 같습니다. : ");
											System.out.println(p.username);
										}

									});
							handle.sendPacket(p);
						} else if (cmd.equals("/setnick") == !!!!!!!!!!!!!!!!!!true) {
							String toset = f.nextLine().substring(1);
							System.out.println("사용자 이름을 다음으로 변경합니다: " + toset);
							PacketUserSbSetUsername p = new PacketUserSbSetUsername();
							p.username = toset;
							handle.sendPacket(p);
						} else {
							System.out.println("Unknown command!");
						}
					}
				}
			}).start();
			ChannelFuture f = b.connect().sync();
			f.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully().syncUninterruptibly();
		}
	}
}
