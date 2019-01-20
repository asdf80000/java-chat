package chat.main;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import chat.common.handler.ChannelState;
import chat.common.handler.ChatCommonInboundPipelineHandler;
import chat.common.handler.ChatCommonInboundPriorityHandler;
import chat.common.handler.ChatCommonPacketPrepender;
import chat.common.handler.ChatCommonPacketSplitter;
import chat.common.main.HandleQueueGeneric;
import chat.common.main.Utils;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.user.PacketUserSbGetUsername;
import chat.gui.ErrorLabelPanel;
import chat.gui.WorkingLabelPanel;
import chat.net.ChatClientPacketDecoder;
import chat.net.ChatClientPacketEncoder;
import chat.net.ChatGUIClientInboundHandler;
import chat.net.ChatGUIExceptionPriorityHandler;
import chat.net.InboundExceptionHandler;
import chat.net.ChatGUIClientInboundHandler.LobbyResult;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class MainGUI {
	public static String addr = "127.0.0.1";
	public static int port = 46435;

	public static void main(String[] args) {
		System.out.println("[Client] Launched program.");
		if (args.length == 1) {
			addr = args[0];
		}
		connect(addr, port);
	}

	public static MainGUI instance = null;
	public LobbyResult lobbyResult = null;

	public static void connect(String address, int port) {
		System.out.println("[Client] Initializing connection...");
		NioEventLoopGroup group = new NioEventLoopGroup(5);
		try {
			Bootstrap bootstrap = new Bootstrap();
			initializer = new ChatGUIInitializer();
			bootstrap.group(group).remoteAddress(address, port).channel(NioSocketChannel.class).handler(initializer);

			System.out.println("[Client] Connecting...");
			new Thread(() -> new MainGUI()).run();

			ChannelFuture channelFuture = bootstrap.connect().syncUninterruptibly();

			MainGUI.initializer.pipeline.addQueue(PacketAllCbSetState.class,
					new HandleQueueGeneric<PacketAllCbSetState>() {

						@Override
						public void run(PacketAllCbSetState p) {
							if (p.cs == ChannelState.USER) {
								MainGUI.instance.lobbyResult = initializer.handler.showLobby();
								new Thread(new Runnable() {

									@Override
									public void run() {
										while (MainGUI.currentState != ChannelState.USER) {
											Utils.sleep();
											System.out.println(MainGUI.currentState);
										}
										PacketUserSbGetUsername p = new PacketUserSbGetUsername();
										p.message = false;
										MainGUI.initializer.handler.sendPacket(p);
									}
								}).start();
							} else {
								MainGUI.initializer.pipeline.addQueue(PacketAllCbSetState.class, this);
							}
						}
					});

			channelFuture.channel().closeFuture().syncUninterruptibly();
		} catch (Exception e) {
			System.out.println("Connection error");
			e.printStackTrace();

			ErrorLabelPanel panel = new ErrorLabelPanel("서버에 연결할 수 없습니다.");
			panel.setBackground(Color.red);
			// panel.remove(1);
			MainGUI.instance.frame.setContentPane(panel);
			MainGUI.instance.frame.pack();
		} finally {
			group.shutdownGracefully().syncUninterruptibly();
		}
	}

	public static void initializeLnF() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
	}

	public JFrame frame;
	public Font uiFont = null;
	public Font boldFont = null;
	public static Cursor hand = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

	public MainGUI() {
		instance = this;
		System.out.println("[GUI] Initializing GUI...");
		initializeLnF();
		uiFont = new JOptionPane().getFont();
		try {
			uiFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/chat/resource/font/NotoSansCJKtc-Regular.ttf"));
			boldFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/chat/resource/font/NotoSansCJKtc-Black.ttf"));
		} catch (Exception e) {
			// TODO: handle exception
		}

		System.out.println("[GUI] Initializing Window...");
		frame = new JFrame("Yeechateeeeee");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocation(300, 300);
		frame.setSize(0, 0);
		frame.setVisible(true);
		
		try {
			frame.setIconImage(ImageIO.read(getClass().getResourceAsStream("/chat/resource/img/gagao.png")));
		} catch (Exception e) {
		}

		WorkingLabelPanel panel = new WorkingLabelPanel("연결 중...");
		frame.setContentPane(panel);
		frame.pack();

		for (Object key : System.getProperties().keySet()) {
			System.out.println(key + ": " + System.getProperty((String) key, "default"));
		}
	}

	public static ChannelState currentState = ChannelState.LOGIN;
	public static ChatGUIInitializer initializer = null;

	public static class ChatGUIInitializer extends ChannelInitializer<NioSocketChannel> {
		public ChatGUIClientInboundHandler handler = null;
		public ChatCommonInboundPriorityHandler priority = null;
		public ChatCommonInboundPipelineHandler pipeline = null;
		public ChatCommonInboundPipelineHandler afterpipeline = null;
		public ChatGUIExceptionPriorityHandler exceptionpriority = null;
		public InboundExceptionHandler exceptions = null;

		@Override
		protected void initChannel(NioSocketChannel ch) throws Exception {
			handler = new ChatGUIClientInboundHandler();
			priority = new ChatCommonInboundPriorityHandler();
			pipeline = new ChatCommonInboundPipelineHandler();
			afterpipeline = new ChatCommonInboundPipelineHandler();
			exceptionpriority = new ChatGUIExceptionPriorityHandler();
			exceptions = new InboundExceptionHandler();
			ChannelPipeline channelPipeline = ch.pipeline();
			channelPipeline.addLast("in_split", new ChatCommonPacketSplitter());
			channelPipeline.addLast("out_prepend", new ChatCommonPacketPrepender());
			channelPipeline.addLast("in_decode", new ChatClientPacketDecoder());
			channelPipeline.addLast("out_encode", new ChatClientPacketEncoder());
			channelPipeline.addLast("in_priority", priority);
			channelPipeline.addLast("in_pipeline", pipeline);
			channelPipeline.addLast("in_handle", handler);
			channelPipeline.addLast("in_exception", exceptions);
			channelPipeline.addLast("in_exceptionpriority", exceptionpriority);
		}
	}
}
