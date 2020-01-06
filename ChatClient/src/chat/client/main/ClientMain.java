package chat.client.main;

import java.util.Scanner;

import chat.client.handler.ChatClientInboundHandler;
import chat.client.handler.ChatClientPacketDecoder;
import chat.client.handler.ChatClientPacketEncoder;
import chat.client.handler.InboundExceptionHandler;
import chat.common.enums.CloseCause;
import chat.common.handler.ChannelState;
import chat.common.handler.ChatCommonInboundPipelineHandler;
import chat.common.handler.ChatCommonInboundPriorityHandler;
import chat.common.handler.ChatCommonPacketPrepender;
import chat.common.handler.ChatCommonPacketSplitter;
import chat.common.main.HandleQueueGeneric;
import chat.common.main.Utils;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.all.PacketAllSbDisconnect;
import chat.common.packet.match.PacketMatchSbCancelMatchmake;
import chat.common.packet.play.PacketPlaySbGetList;
import chat.common.packet.play.PacketPlaySbMatchInfo;
import chat.common.packet.play.PacketPlaySbQuitMatch;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientMain {
	public static int PORT = 46435;
	public static String ADDRESS = "172.30.1.7";

	public static ChannelState cs = ChannelState.LOGIN;

	public static void main(String[] args) throws Exception {
		new ClientMain(ADDRESS, PORT);
	}

	public static Scanner f = new Scanner(System.in);
	public static ChatClientInboundHandler handle = null;
	public static ChatCommonInboundPriorityHandler priority = null;
	public static ChatCommonInboundPipelineHandler pipeline = null;
	public static ChatCommonInboundPipelineHandler afterpipeline = null;

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
							pipeline = new ChatCommonInboundPipelineHandler();
							afterpipeline = new ChatCommonInboundPipelineHandler();
							ch.pipeline().addLast("inboundsplit", new ChatCommonPacketSplitter())
									.addLast("outboundprepend", new ChatCommonPacketPrepender())
									.addLast("inboundpacketdecoder", new ChatClientPacketDecoder())
									.addLast("outboundpacketencoder", new ChatClientPacketEncoder())
									.addLast("inboundpriority", priority).addLast("inboundpipeline", pipeline)
									.addLast("inboundhandle", handle).addLast("inboundexception", ieh);
						};
					});
			new Thread(new Runnable() {

				@Override
				public void run() {
					System.out.println("명령어를 입력하십시오.");
					while (true) {
						String cmd = f.next();
						System.out.println();
						System.out.println("Command: " + cmd);
						if (cmd.equals("/close")) {
							System.out.println("연결 닫는 중...");
							PacketAllSbDisconnect p = new PacketAllSbDisconnect();
							p.cc = CloseCause.DISCONNECT;
							try {
								handle.sendPacket(p);
							} catch (Exception e) {
							}
							handle.ctx.close().awaitUninterruptibly().syncUninterruptibly();
							System.out.println("연결을 닫았습니다.");
							System.exit(0);
						} else if (cmd.equals("/help")) {
							System.out.println("---------- Chat 콘솔 클라이언트 ----------");
							System.out.println(" - ALL");
							System.out.println("/close - 연결 닫기");
							System.out.println("/help - 도움말");
							
							System.out.println(" - PLAY");
							System.out.println("/chat [내용] - 채팅하기");
							System.out.println("/list - 채팅방 유저 목록");
							System.out.println("/matchinfo - 매치 정보");
							System.out.println("/quit - 채팅방 나가기");
							System.out.println("/regame - 채팅방 다시 찾기");
							
							System.out.println(" - USER");
							System.out.println("/getnick - 닉네임 출력");
							System.out.println("/setnick [닉네임] - 닉네임 변경");

							System.out.println(" - MATCH");
							System.out.println("/startmatch - 매치메이킹 시작");
							System.out.println("/stopmatch - 메치메이킹 취소");						
						} else if (cmd.equals("/getnick")) {
							System.out.println("Processing...");
							PacketUserSbGetUsername p = new PacketUserSbGetUsername();
							p.message = false;
							priority.addQueue(PacketUserCbSetUsername.class,
									new HandleQueueGeneric<PacketUserCbSetUsername>() {

										@Override
										public void run(PacketUserCbSetUsername p) {
											System.out.println("<명령어 대답> 당신의 사용자 이름: " + p.username);
										}

									});
							handle.sendPacket(p);
						} else if (cmd.equals("/setnick") == !!!!!!!!!!!!!!!!!!true) {
							String toset = f.nextLine().substring(1);
							System.out.println("사용자 이름을 다음으로 변경합니다: " + toset);
							PacketUserSbSetUsername p = new PacketUserSbSetUsername();
							p.username = toset;
							handle.sendPacket(p);
						} else if (cmd.equals("/startmatch")) {
							PacketUserSbStartMatchmake mm = new PacketUserSbStartMatchmake();
							handle.sendPacket(mm);
						} else if (cmd.equals("/stopmatch")) {
							PacketMatchSbCancelMatchmake mm = new PacketMatchSbCancelMatchmake();
							handle.sendPacket(mm);
						} else if (cmd.equals("/chat")) {
							String text = f.nextLine().substring(1);
							handle.chat(text);
						} else if (cmd.equals("/list")) {
							handle.sendPacket(new PacketPlaySbGetList());
						} else if (cmd.equals("/quit")) {
							handle.sendPacket(new PacketPlaySbQuitMatch());
						} else if (cmd.equals("/regame")) {
							handle.sendPacket(new PacketPlaySbQuitMatch());
							pipeline.addQueue(PacketAllCbSetState.class, new HandleQueueGeneric<PacketAllCbSetState>() {

								@Override
								public void run(PacketAllCbSetState p) {
									final HandleQueueGeneric<PacketAllCbSetState> ts = this;
									new Thread(new Runnable() {

										@Override
										public void run() {
											if (p.cs != ChannelState.USER) {
												ClientMain.pipeline.addQueue(PacketAllCbSetState.class, ts);
												return;
											}
											while (ClientMain.cs != ChannelState.USER) {
												Utils.sleep();
											}
											PacketUserSbStartMatchmake mm = new PacketUserSbStartMatchmake();
											handle.sendPacket(mm);
										}
									}).start();
								}
							});

						} else if (cmd.equals("/matchinfo")) {
							handle.sendPacket(new PacketPlaySbMatchInfo());
						} else {
							System.out.println("명령어를 알 수 없습니다!");
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
