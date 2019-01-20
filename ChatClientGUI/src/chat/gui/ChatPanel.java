package chat.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;

import javax.crypto.SecretKey;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import chat.common.handler.ChannelState;
import chat.common.listener.PacketAllCbListener;
import chat.common.listener.PacketPlayCbListener;
import chat.common.main.HandleQueueGeneric;
import chat.common.main.Utils;
import chat.common.packet.Packet;
import chat.common.packet.all.PacketAllCbDisconnect;
import chat.common.packet.all.PacketAllCbMessage;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.play.PacketPlayCbChat;
import chat.common.packet.play.PacketPlayCbGetList;
import chat.common.packet.play.PacketPlayCbGetMatchSummary;
import chat.common.packet.play.PacketPlayCbMatchId;
import chat.common.packet.play.PacketPlayCbSafeMessage;
import chat.common.packet.play.PacketPlayCbStart;
import chat.common.packet.play.PacketPlaySbChat;
import chat.common.packet.play.PacketPlaySbGetList;
import chat.common.packet.play.PacketPlaySbGetMatchSummary;
import chat.common.packet.play.PacketPlaySbMatchInfo;
import chat.common.packet.play.PacketPlaySbQuitMatch;
import chat.common.packet.user.PacketUserSbGetUsername;
import chat.common.work.Aes256Utils;
import chat.component.AButton;
import chat.main.MainGUI;
import chat.work.PacketPlayCbSafeMessageToString;

public class ChatPanel extends JPanel implements PacketPlayCbListener, PacketAllCbListener {
	private static final long serialVersionUID = 1L;

	private PacketAllCbListener apl;
	private SecretKey aes;
	JLabel mid = null;
	public JTextField text;

	public ChatPanel(PacketAllCbListener allState) {
		this.apl = allState;
		setOpaque(true);
		setBackground(new Color(60, 60, 60));
		Dimension sz = new Dimension(500, 500);
		setPreferredSize(sz);
		setSize(sz);
		setLayout(new BorderLayout());

		// TODO add components
		JPanel top = new JPanel();
		top.setOpaque(true);
		top.setBackground(new Color(0, 0, 0));
		top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
		add(top, BorderLayout.PAGE_START);

		JLabel title = new JLabel("매치ID: ");
		title.setFont(MainGUI.instance.boldFont.deriveFont(2, 20f));
		title.setForeground(Color.white);
		top.add(title);

		Font f = null;
		try {
			f = Font.createFont(Font.TRUETYPE_FONT,
					getClass().getResourceAsStream("/chat/resource/font/AgencyFB-Bold.ttf"));
		} catch (Exception e) {
			f = MainGUI.instance.uiFont;
		}
		mid = new JLabel("00000000");
		mid.setFont(f.deriveFont(2, 20f));
		mid.setForeground(Color.white);
		top.add(mid);
		PacketPlaySbGetMatchSummary p = new PacketPlaySbGetMatchSummary();
		MainGUI.initializer.priority.addQueue(PacketPlayCbGetMatchSummary.class,
				new HandleQueueGeneric<PacketPlayCbGetMatchSummary>() {

					@Override
					public void run(PacketPlayCbGetMatchSummary p) {
						mid.setText(p.mid);
					}
				});
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (MainGUI.currentState != ChannelState.PLAY)
					Utils.sleep();
				MainGUI.initializer.handler.sendPacket(p);
			}
		}).start();

		top.add(Box.createHorizontalStrut(25));

		AButton quit = new AButton("매치 나가기");
		quit.setFont(MainGUI.instance.uiFont.deriveFont(0, 15f));
		quit.setBackground(Color.RED);
		quit.setForeground(Color.white);
		quit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PacketPlaySbQuitMatch packet = new PacketPlaySbQuitMatch();
				MainGUI.initializer.pipeline.addQueue(PacketAllCbSetState.class,
						new HandleQueueGeneric<PacketAllCbSetState>() {

							@Override
							public void run(PacketAllCbSetState p) {
								if (p.cs != ChannelState.USER) {
									MainGUI.initializer.pipeline.addQueue(PacketAllCbSetState.class, this);
									return;
								}
								MainGUI.instance.lobbyResult = MainGUI.initializer.handler.showLobby();
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
							}
						});
				MainGUI.initializer.handler.sendPacket(packet);
				MainGUI.initializer.handler.currentPacketListener = MainGUI.initializer.handler.defaultPacketListener;
			}
		});
		top.add(quit);

//		JScrollPane jsp = new JScrollPane();
//		jsp.setOpaque(false);
//		jsp.setPreferredSize(new Dimension(250, 0));
//		jsp.setBorder(null);
//		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		jsp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
//		jsp.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {
//
//			@Override
//			public void adjustmentValueChanged(AdjustmentEvent e) {
//				jsp.getVerticalScrollBar().setValue(jsp.getVerticalScrollBar().getMaximum());
//			}
//		});
//		jsp.getVerticalScrollBar().setUnitIncrement(5);
//		body.add(jsp, BorderLayout.LINE_END);
		msgs = new JPanel();
		msgs.setLayout(new BoxLayout(msgs, BoxLayout.Y_AXIS));
		msgs.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		msgs.setOpaque(false);
		msgs.setMaximumSize(getPreferredSize());
		for (int i = 0; i < 70; i++) {
			msgs.add(Box.createVerticalStrut(10));
		}
		JScrollPane center = new JScrollPane(msgs);
		center.getViewport().setOpaque(false);
		center.setMaximumSize(getPreferredSize());
		center.setBorder(null);
		center.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		center.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		center.getVerticalScrollBar().addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				center.getVerticalScrollBar().setValue(center.getVerticalScrollBar().getMaximum());
			}
		});
		center.setOpaque(true);
		center.setBackground(new Color(60, 60, 60));
		add(center, BorderLayout.CENTER);

		JPanel bottom = new JPanel();
		bottom.setOpaque(true);
		bottom.setBackground(new Color(255, 255, 255));
		bottom.setLayout(new BorderLayout());
		add(bottom, BorderLayout.PAGE_END);

		text = new JTextField();
		text.setFont(MainGUI.instance.uiFont.deriveFont(0, 25f));
		ActionListener sendac = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String str = text.getText();
				if (str.isEmpty())
					return;
				text.setText("");
				if (!str.startsWith("/")) {
					PacketPlaySbChat chat = new PacketPlaySbChat();
					chat.encText = Aes256Utils.encrypt(Utils.byteStr(str), aes);
					sendPacket(chat);
				} else {
					String cmd = str.substring(1);
					switch (cmd) {
					case "help": {
						addMessage("-- 명령어 도움말 ---");
						addMessage("/help - 도움말");
						addMessage("/matchinfo - 매치 정보 표시");
						addMessage("/list - 사용자 목록 표시");
						addMessage("----------------");
						break;
					}
					case "matchinfo": {
						addMessage("[명령어] 서버에 매치 정보 표시를 요청하는 중");
						PacketPlaySbMatchInfo p = new PacketPlaySbMatchInfo();
						sendPacket(p);
						break;
					}
					case "list": {
						PacketPlaySbGetList p = new PacketPlaySbGetList();
						MainGUI.initializer.priority.addQueue(PacketPlayCbGetList.class,
								new HandleQueueGeneric<PacketPlayCbGetList>() {

									@Override
									public void run(PacketPlayCbGetList p) {
										String[] lists = new String[p.getLength()];
										for (int i = 0; i < p.getLength(); i++) {
											lists[i] = Utils.byteStr(Aes256Utils.decrypt(p.list.get(i), aes));
										}
										String list = String.join(", ", lists);
										list = "[명령어] 사용자 목록(" + p.getLength() + "명): " + list;
										addMessage(list);
									}

								});
						sendPacket(p);
						break;
					}
					default: {
						addMessage("[명령어] 올바르지 않은 명령어입니다.");
						break;
					}
					}
				}

			}
		};
		text.addActionListener(sendac);
		text.setBorder(null);
		bottom.add(text, BorderLayout.CENTER);

		AButton send = new AButton("전송");
		send.addActionListener(sendac);
		send.setBackground(new Color(0, 128, 255));
		send.setForeground(Color.white);
		send.setFont(MainGUI.instance.uiFont.deriveFont(0, 15f));
		bottom.add(send, BorderLayout.LINE_END);

		new Thread(new Runnable() {

			@Override
			public void run() {
				MainGUI.instance.frame.getRootPane().registerKeyboardAction(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						System.out.println("enter press");
						text.requestFocusInWindow();
					}
				}, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true), JComponent.WHEN_IN_FOCUSED_WINDOW);
				while (!isDisplayable())
					;
				while (isDisplayable())
					;
				MainGUI.instance.frame.getRootPane()
						.unregisterKeyboardAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, true));
			}
		}).start();
//
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				while (!isDisplayable()) {
//					Utils.sleep();
//				}
//				while (isDisplayable()) {
//					repaint();
//					try {.
//						Thread.sleep(10);
//					} catch (InterruptedException e) {
//					}
//				}
//			}
//		}).start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.GRAY);
		g2.fillRect(0, 0, getWidth(), getHeight());
	}

	@Override
	public void process(PacketAllCbDisconnect packet) {
		packet.process(apl);
	}

	@Override
	public void process(PacketAllCbSetState packet) {
		packet.process(apl);
	}

	@Override
	public void process(PacketAllCbMessage packetAllCbMessage) {
		packetAllCbMessage.process(apl);
	}

	@Override
	public void process(PacketPlayCbStart packet) {
		this.aes = packet.sk;
	}

	@Override
	public void process(PacketPlayCbChat packetPlayCbChat) {
		String nick = Utils.byteStr(Aes256Utils.decrypt(packetPlayCbChat.userNameData, aes));
		String text = Utils.byteStr(Aes256Utils.decrypt(packetPlayCbChat.chatData, aes));
		addMessage("[" + nick + "] " + text);
	}

	@Override
	public void process(PacketPlayCbSafeMessage p) {
		System.out.println("PAES key: " + aes);
		p.decodeAfter(aes);
		String txt = PacketPlayCbSafeMessageToString.getString(p.type, p.array);
		addMessage("<서버 메시지> " + txt);
		System.out.println("<서버 메시지> " + txt);
	}

	private JPanel msgs = null;

	public void chat(String text) {
		if (MainGUI.currentState != ChannelState.PLAY)
			return;
		PacketPlaySbChat packet = new PacketPlaySbChat();
		packet.encText = Aes256Utils.encrypt(Utils.byteStr(text), aes);
		sendPacket(packet);
	}

	public void sendPacket(Packet<?> packet) {
		MainGUI.initializer.handler.sendPacket(packet);
	}

	public void addMessage(String message) {
		String txt = message;
		while (true) {
			JLabel la = new JLabel();
			la.setOpaque(false);
			la.setText(txt);
			txt = "";
			la.setFont(MainGUI.instance.uiFont.deriveFont(0, 20));
			la.setForeground(Color.white);
			msgs.add(la);
			int twd = 500;
			if (la.getPreferredSize().getWidth() + 10 < twd) {
				break;
			}
			while (la.getPreferredSize().getWidth() + 10 >= twd) {
				txt = la.getText().charAt(la.getText().length() - 1) + txt;
				la.setText(la.getText().substring(0, la.getText().length() - 1));
			}
		}
		repaint();
		validate();
		msgs.add(Box.createVerticalStrut(5));
		if (msgs.getHeight() > getHeight()) {
			msgs.remove(0);
		}
	}

	@Override
	public void process(PacketPlayCbGetMatchSummary packetPlayCbGetMatchId) {

	}

	@Override
	public void process(PacketPlayCbGetList packetPlayCbGetList) {
		// do nothing
	}

	@Override
	public void process(PacketPlayCbMatchId p) {
		mid.setText(p.newId);
	}
}
