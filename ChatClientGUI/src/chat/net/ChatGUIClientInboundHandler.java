package chat.net;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import chat.common.handler.ChannelState;
import chat.common.listener.PacketAllCbListener;
import chat.common.listener.PacketListener;
import chat.common.listener.PacketLoginCbListener;
import chat.common.listener.PacketMatchCbListener;
import chat.common.listener.PacketUserCbListener;
import chat.common.main.HandleQueueGeneric;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.common.packet.all.PacketAllCbDisconnect;
import chat.common.packet.all.PacketAllCbMessage;
import chat.common.packet.all.PacketAllCbMessage.PacketAllCbMessageType;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.login.PacketLoginCbWelcome;
import chat.common.packet.login.PacketLoginSbHandshake;
import chat.common.packet.match.PacketMatchCbMatchFound;
import chat.common.packet.user.PacketUserCbSetUsername;
import chat.common.packet.user.PacketUserCbUntranslatedMessage;
import chat.common.packet.user.PacketUserSbStartMatchmake;
import chat.component.AButton;
import chat.gui.MatchingPane;
import chat.gui.MsgsPanel;
import chat.gui.RPanel;
import chat.gui.SetNickMenu;
import chat.main.MainGUI;
import chat.work.PacketAllCbMessageToString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChatGUIClientInboundHandler extends SimpleChannelInboundHandler<Packet<?>> implements
		PacketLoginCbListener, PacketUserCbListener, PacketAllCbListener, PacketMatchCbListener {

	public Channel channel = null;
	public ChannelHandlerContext context = null;
	public PacketListener currentPacketListener = null;
	public PacketListener defaultPacketListener = null;

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		this.context = ctx;
		this.channel = this.context.channel();
		defaultPacketListener = this;
		currentPacketListener = defaultPacketListener;

		sendHandshakePacket();
	}
	
	private void rec1(JComponent c) {
		for(Component co : c.getComponents()) {
			if(co.getName() != null && co.getName().equals("text")) {
				co.requestFocusInWindow();
			}
			if(co instanceof JComponent) {
				rec1((JComponent) co);
			}
		}
	}

	public LobbyResult showLobby() {
		LobbyResult lr = new LobbyResult();
		RPanel jp = new RPanel();
		Dimension sz = new Dimension(500, 500);
		jp.setLayout(new BorderLayout());
		jp.setSize(sz);
		jp.setPreferredSize(sz);
		jp.setBackground(new Color(45, 45, 45));

		JPanel topmenu = new JPanel();
		topmenu.setOpaque(false);
		topmenu.setLayout(new BorderLayout());
		jp.add(topmenu, BorderLayout.PAGE_START);

		JLabel title = new JLabel(MainGUI.instance.frame.getTitle());
		title.setHorizontalAlignment(JLabel.LEFT);
		title.setForeground(Color.white);
		title.setFont(MainGUI.instance.boldFont.deriveFont(0, 35f));
		topmenu.add(title, BorderLayout.LINE_START);

		JPanel lobby = new JPanel();
		lobby.setBackground(new Color(0, 0, 0));
		lobby.setLayout(new BorderLayout());
		jp.add(lobby, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		panel.setOpaque(false);
		lobby.add(panel, BorderLayout.PAGE_START);

		JLabel nick = new JLabel("N/A");
		nick.setFont(MainGUI.instance.uiFont.deriveFont(1, 20f));
		nick.setForeground(Color.WHITE);
		nick.setHorizontalAlignment(JLabel.RIGHT);
		lr.nick = nick;
		panel.add(nick);

		panel.add(Box.createHorizontalStrut(35));

		AButton setnick = new AButton("변경");
		setnick.setBackground(new Color(255, 205, 0));
		setnick.setForeground(Color.white);
		setnick.setFont(MainGUI.instance.boldFont.deriveFont(0, 20f));
		lobby.add(setnick);
		lobby.validate();
		setnick.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SetNickMenu snm = new SetNickMenu(jp);
				MainGUI.instance.frame.setContentPane(snm);
				MainGUI.instance.frame.pack();
				rec1(snm);
			}
		});
		setnick.setSize(setnick.getPreferredSize());
		setnick.setLocation(lobby.getWidth(), lobby.getHeight());
		panel.add(setnick);

		panel.setMinimumSize(panel.getPreferredSize());
		panel.setMaximumSize(panel.getPreferredSize());
		panel.setSize(panel.getPreferredSize());

		JPanel body = new JPanel();
		body.setOpaque(false);
		body.setLayout(new BorderLayout());
		lobby.add(body);

		{
			JScrollPane jsp = new JScrollPane();
			jsp.setOpaque(false);
			jsp.setPreferredSize(new Dimension(250, 0));
			jsp.setBorder(null);
			jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
			jsp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

				@Override
				public void adjustmentValueChanged(AdjustmentEvent e) {
					jsp.getVerticalScrollBar().setValue(jsp.getVerticalScrollBar().getMaximum());
				}
			});
			jsp.getVerticalScrollBar().setUnitIncrement(5);
			body.add(jsp, BorderLayout.LINE_END);

			MsgsPanel messages = new MsgsPanel();
			lr.messages = messages;
			lr.jsp = jsp;
			messages.setBackground(new Color(20, 20, 20));
			messages.setOpaque(true);
			messages.setSize(new Dimension(250, 0));
			jsp.setViewportView(messages);
		}

		{
			JPanel p = new JPanel();
			p.setLayout(new BorderLayout());
			p.setOpaque(false);
			
			Font f = null;
			try {
				f = Font.createFont(Font.TRUETYPE_FONT,
						getClass().getResourceAsStream("/chat/resource/font/AgencyFB-Bold.ttf"));
			} catch (Exception e) {
				f = MainGUI.instance.uiFont;
			}
			
			AButton start = new AButton("START");
			start.setForeground(Color.white);
			start.setBackground(new Color(255, 215, 0));
			start.setFont(f.deriveFont(0, 60f));
			start.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.out.println("매치메이킹이 시작되었습니다.");
					MainGUI.initializer.pipeline.addQueue(PacketAllCbSetState.class,
							new HandleQueueGeneric<PacketAllCbSetState>() {
								@Override
								public void run(PacketAllCbSetState p) {
									if (p.cs != ChannelState.MATCH) {
										MainGUI.initializer.pipeline.addQueue(PacketAllCbSetState.class, this);
										return;
									}
									new Thread(new Runnable() {

										@Override
										public void run() {
											while (MainGUI.currentState != ChannelState.MATCH) {
												Utils.sleep();
											}
											System.out.println("match started");
											MatchingPane mp = new MatchingPane(defaultPacketListener);
											MainGUI.instance.frame.setContentPane(mp);
											MainGUI.instance.frame.pack();
											currentPacketListener = mp;
										}
									}).start();
								}
							});
					PacketUserSbStartMatchmake p = new PacketUserSbStartMatchmake();
					sendPacket(p);
				}
			});
			start.setCursor(MainGUI.hand);
			start.setPreferredSize(new Dimension(230, 130));
			p.add(start, BorderLayout.PAGE_END);
			
			body.add(p, BorderLayout.LINE_START);
		}

		MainGUI.instance.frame.setContentPane(jp);
		MainGUI.instance.frame.pack();
		return lr;
	}

	public static class LobbyResult {
		public JLabel nick;
		public MsgsPanel messages;
		public JScrollPane jsp;
	}

	private void sendHandshakePacket() {
		{
			PacketLoginSbHandshake packet = new PacketLoginSbHandshake();
			packet.username = "undefined";
			sendPacket(packet);
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Packet<?> msg) throws Exception {
		processPacket(msg);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void processPacket(Packet packet) {
		System.out.println("Process " + packet.getClass().getName() + Utils.serializePacket(packet));
		packet.process(currentPacketListener);
	}

	@Override
	public void process(PacketMatchCbMatchFound packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void process(PacketAllCbDisconnect packet) {
		// TODO Auto-generated method stub

	}

	@Override
	public void process(PacketAllCbSetState packet) {
		System.out.println("SetState received");
		MainGUI.currentState = packet.cs;
	}

	@Override
	public void process(PacketAllCbMessage packet) {
		addMessage(packet.type, packet.array);
	}

	public void addMessage(PacketAllCbMessageType type, String[] arr) {
		addMessage(PacketAllCbMessageToString.getString(type, arr));
	}

	public void addMessage(String str) {
		if (MainGUI.instance.lobbyResult != null) {
			String txt = "<서버> " + str;
			while (true) {
				JLabel la = new JLabel();
				la.setOpaque(false);
				la.setText(txt);
				txt = "";
				la.setFont(MainGUI.instance.uiFont.deriveFont(0, 20));
				FontMetrics fm = la.getFontMetrics(la.getFont());
				la.setForeground(Color.white);
				MainGUI.instance.lobbyResult.messages.add(la);
				if (fm.stringWidth(la.getText()) < MainGUI.instance.lobbyResult.messages.getWidth() - 10) {
					break;
				}
				while (fm.stringWidth(la.getText()) >= MainGUI.instance.lobbyResult.messages.getWidth() - 10) {
					txt = la.getText().charAt(la.getText().length() - 1) + txt;
					la.setText(la.getText().substring(0, la.getText().length() - 1));
				}
			}
			MainGUI.instance.lobbyResult.messages.repaint();
			MainGUI.instance.lobbyResult.messages.validate();
		}
		MainGUI.instance.lobbyResult.messages.add(Box.createVerticalStrut(15));
		if (MainGUI.instance.lobbyResult.messages.getHeight() > 500) {
			MainGUI.instance.lobbyResult.messages.remove(0);
		}
	}

	@Override
	public void process(PacketUserCbSetUsername packet) {
		if (MainGUI.instance.lobbyResult != null) {
			MainGUI.instance.lobbyResult.nick.setText(packet.username);
			MainGUI.instance.lobbyResult.nick.getTopLevelAncestor().validate();
		}
	}

	@Override
	public void process(PacketLoginCbWelcome packet) {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("rawtypes")
	public void sendPacket(Packet packet) {
		context.writeAndFlush(packet).awaitUninterruptibly().syncUninterruptibly();
	}

	@Override
	public void process(PacketUserCbUntranslatedMessage p) {
		addMessage(p.message);
	}
}
