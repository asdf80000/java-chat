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
import java.util.HashMap;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

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
import chat.common.packet.login.PacketLoginCbResult;
import chat.common.packet.login.PacketLoginCbWelcome;
import chat.common.packet.login.PacketLoginSbHandshake;
import chat.common.packet.login.PacketLoginSbRegister;
import chat.common.packet.match.PacketMatchCbMatchFound;
import chat.common.packet.user.PacketUserCbSetUsername;
import chat.common.packet.user.PacketUserCbUntranslatedMessage;
import chat.common.packet.user.PacketUserSbStartMatchmake;
import chat.common.work.Sha512Utils;
import chat.component.AButton;
import chat.gui.AgencyFont;
import chat.gui.MatchingPane;
import chat.gui.MsgsPanel;
import chat.gui.RPanel;
import chat.gui.SetNickMenu;
import chat.main.MainGUI;
import chat.work.PacketAllCbMessageToString;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ChatGUIClientInboundHandler extends SimpleChannelInboundHandler<Packet<?>>
		implements PacketLoginCbListener, PacketUserCbListener, PacketAllCbListener, PacketMatchCbListener {

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

		showLogin();
	}

	private void rec1(JComponent c) {
		for (Component co : c.getComponents()) {
			if (co.getName() != null && co.getName().equals("text")) {
				co.requestFocusInWindow();
			}
			if (co instanceof JComponent) {
				rec1((JComponent) co);
			}
		}
	}

	public void showLogin() {
//		sendRegisterPacket("user", "a");
//		sendHandshakePacket("user", "a");
		Dimension sz = new Dimension(500, 500);
		RPanel jp = new RPanel();
		jp.setSize(sz);
		jp.setPreferredSize(sz);
		jp.setBackground(new Color(45, 45, 45));
		jp.setLayout(new BorderLayout());

		JLabel title = new JLabel("로그인");
		title.setFont(MainGUI.instance.boldFont.deriveFont(0, 40));
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setForeground(Color.white);
		jp.add(title, BorderLayout.PAGE_START);

		JPanel p = new JPanel();
		p.setAlignmentX(Component.CENTER_ALIGNMENT);
		p.setOpaque(false);
		p.setLayout(new BorderLayout());

		JPanel bodyIn = new JPanel();
		bodyIn.setOpaque(false);
		bodyIn.setLayout(new BoxLayout(bodyIn, BoxLayout.PAGE_AXIS));

		Font f = AgencyFont.loadAgencyFB();

		bodyIn.add(Box.createVerticalStrut(10));

		JLabel msg = new JLabel(" ");
		msg.setFont(MainGUI.instance.uiFont.deriveFont(0, 20));
		msg.setForeground(Color.red);
		msg.setHorizontalAlignment(JLabel.CENTER);
		{
			p.add(msg, BorderLayout.PAGE_START);
		}

		bodyIn.add(Box.createVerticalStrut(10));

		JTextField id = new JTextField(15);
		id.setFont(MainGUI.instance.boldFont.deriveFont(0, 25f));
		id.setBorder(null);
		id.setHorizontalAlignment(JTextField.CENTER);
		id.setBackground(new Color(60, 60, 60));
		id.setForeground(Color.white);
		id.setCaretColor(Color.white);
		id.setName("id");
		JLabel idl = new JLabel("ID");
		idl.setForeground(Color.white);
		idl.setFont(f.deriveFont(0, 30));
		bodyIn.add(idl);
		bodyIn.add(id);

		bodyIn.add(Box.createVerticalStrut(30));

		JPasswordField pw = new JPasswordField(15);
		pw.setFont(MainGUI.instance.boldFont.deriveFont(0, 25f));
		pw.setBorder(null);
		pw.setHorizontalAlignment(JTextField.CENTER);
		pw.setBackground(new Color(60, 60, 60));
		pw.setForeground(Color.white);
		pw.setCaretColor(Color.white);
		pw.setName("pw");
		JLabel pwl = new JLabel("PWD");
		pwl.setForeground(Color.white);
		pwl.setFont(f.deriveFont(0, 30));
		bodyIn.add(pwl);
		bodyIn.add(pw);

		JPanel body = new JPanel();
		body.setBackground(new Color(0, 0, 0));
		body.setLayout(new FlowLayout());
		p.add(bodyIn);
		body.add(p);
		jp.add(body, BorderLayout.CENTER);
		jp.repaint();
		bodyIn.validate();
		jp.validate();

		JPanel buttons = new JPanel();
		buttons.setBackground(new Color(30, 30, 30));
		buttons.setOpaque(true);
		jp.add(buttons, BorderLayout.PAGE_END);
		
		ActionListener lal = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendHandshakePacket(id.getText(), new String(pw.getPassword()));
				MainGUI.initializer.priority.addQueue(PacketLoginCbResult.class,
						new HandleQueueGeneric<PacketLoginCbResult>() {

							@Override
							public void run(PacketLoginCbResult p) {
								msg.setText(getCode(p.key));
								if (!p.succ) {
									msg.setForeground(Color.RED);
								} else {
									msg.setForeground(Color.GREEN);
								}
							}
						});
			}
		};
		ActionListener ral = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				sendRegisterPacket(id.getText(), new String(pw.getPassword()));
				MainGUI.initializer.priority.addQueue(PacketLoginCbResult.class,
						new HandleQueueGeneric<PacketLoginCbResult>() {

							@Override
							public void run(PacketLoginCbResult p) {
								msg.setText(getCode(p.key));
								if (!p.succ) {
									msg.setForeground(Color.RED);
								} else {
									msg.setForeground(Color.GREEN);
									sendHandshakePacket(id.getText(), new String(pw.getPassword()));
								}
							}
						});
			}
		};
		
		id.addActionListener(lal);
		pw.addActionListener(lal);

		AButton login = new AButton("로그인");
		login.setFont(MainGUI.instance.boldFont.deriveFont(0, 30));
		login.setForeground(Color.white);
		login.setCursor(MainGUI.hand);
		login.addActionListener(lal);
		login.setBackground(new Color(50, 50, 50));
		buttons.add(login);

		AButton register = new AButton("회원가입");
		register.setFont(MainGUI.instance.boldFont.deriveFont(0, 30));
		register.setForeground(Color.white);
		register.setCursor(MainGUI.hand);
		register.addActionListener(ral);
		register.setBackground(new Color(50, 50, 50));
		buttons.add(register);

		MainGUI.instance.frame.setContentPane(jp);
		MainGUI.instance.frame.pack();
	}
	
	public String getCode(String key) {
		HashMap<String, String> m = new HashMap<>();
		m.put("CREATED", "회원가입을 성공했습니다.");
		m.put("INVALIDREG", "이미 만들어진 계정입니다.");
		m.put("SERVERIO", "서버 입출력 오류가 발생하였습니다.");
		m.put("INVALID_ID", "ID가 올바르지 않습니다.");
		m.put("LOGGED", "로그인을 성공했습니다.");
		m.put("INVALIDAUTH", "ID/비밀번호가 올바르지 않습니다.");
		m.put("REGAGAIN", "오류 발생. 다시 회원가입하세요.");
		return m.get(key);
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

	private void sendHandshakePacket(String id, String pw) {
		{
			PacketLoginSbHandshake packet = new PacketLoginSbHandshake();
			packet.id = id;
			packet.pwd = Sha512Utils.shaencode(pw);
			sendPacket(packet);
		}
	}

	private void sendRegisterPacket(String id, String pw) {
		PacketLoginSbRegister p = new PacketLoginSbRegister();
		p.username = id;
		p.password = Sha512Utils.shaencode(pw);
		sendPacket(p);
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

	@Override
	public void process(PacketLoginCbResult packet) {

	}
}
