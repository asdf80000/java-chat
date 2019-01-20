package chat.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.security.KeyPair;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.lang3.time.DurationFormatUtils;

import chat.common.handler.ChannelState;
import chat.common.listener.PacketAllCbListener;
import chat.common.listener.PacketListener;
import chat.common.listener.PacketMatchCbListener;
import chat.common.main.Utils;
import chat.common.packet.all.PacketAllCbDisconnect;
import chat.common.packet.all.PacketAllCbMessage;
import chat.common.packet.all.PacketAllCbSetState;
import chat.common.packet.match.PacketMatchCbMatchFound;
import chat.common.packet.play.PacketPlayCbStart;
import chat.common.packet.play.PacketPlaySbStart;
import chat.common.work.RSAUtils;
import chat.main.MainGUI;

public class MatchingPane extends JPanel implements PacketMatchCbListener, PacketAllCbListener {
	private static final long serialVersionUID = 1L;

	private PacketAllCbListener apl;
	private JLabel label = null;

	public MatchingPane(PacketListener allState) {
		this.apl = (PacketAllCbListener) allState;
		setOpaque(true);
		setBackground(new Color(0x42d7f4));
		Dimension sz = new Dimension(500, 500);
		setPreferredSize(sz);
		setSize(sz);
		setLayout(null);

		label = new JLabel("매치메이킹이 시작되었습니다");
		label.setFont(MainGUI.instance.uiFont.deriveFont(1, 25f));
		label.setSize(label.getPreferredSize());
		label.setForeground(Color.white);
		label.setLocation((getWidth() - label.getWidth()) / 2, getHeight() / 2 + 60);
		add(label);

		Font f = null;
		try {
			f = Font.createFont(Font.TRUETYPE_FONT,
					getClass().getResourceAsStream("/chat/resource/font/AgencyFB-Bold.ttf"));
		} catch (Exception e) {
			f = MainGUI.instance.uiFont;
		}
		JLabel time = new JLabel("00:00");
		time.setFont(f.deriveFont(0, 50));
		time.setSize(time.getPreferredSize());
		time.setForeground(new Color(255, 215, 0));
		time.setLocation((getWidth() - time.getWidth()) / 2, getHeight() / 2 + label.getHeight() + time.getHeight());
		add(time);
			long start = System.currentTimeMillis();

		TimeLoadingButton tlb = new TimeLoadingButton();
		tlb.setSize(200, 200);
		tlb.setLocation((getWidth() - tlb.getWidth()) / 2, getHeight() / 2 - tlb.getHeight());
		add(tlb);
		
		JPanel p2 = new JPanel();
		p2.setBackground(new Color(40, 40, 40));
		p2.setOpaque(true);
		p2.setBounds(tlb.getBounds());
		add(p2);
		
		JPanel p1 = new JPanel();
		p1.setBackground(new Color(50, 50, 50));
		p1.setOpaque(true);
		p1.setSize(getWidth(), label.getY());
		add(p1);

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (!isDisplayable()) {
					Utils.sleep();
				}
				while (isDisplayable()) {
					time.setText(DurationFormatUtils.formatPeriod(start, System.currentTimeMillis(), "mm:ss"));
					time.setSize(time.getPreferredSize());
					time.setLocation((getWidth() - time.getWidth()) / 2, time.getY());
					Utils.sleep(100);
				}
			}
		}).start();

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (!isDisplayable()) {
					Utils.sleep();
				}
				while (isDisplayable()) {
					tlb.repaint();
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(new Color(30, 30, 30));
		g2.fillRect(0, 0, getWidth(), getHeight());
	}

	private KeyPair client;

	@Override
	public void process(PacketMatchCbMatchFound packet) {
		label.setText("매치 참가 중");
		label.setSize(label.getPreferredSize());
		label.setLocation((getWidth() - label.getWidth()) / 2, label.getY());
		client = RSAUtils.genKey();

		// TODO prepare PLAY handler
		ChatPanel cp = new ChatPanel(apl);
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				Utils.sleep(1500);
				MainGUI.instance.frame.setContentPane(cp);
				cp.text.requestFocusInWindow();
			}
		}).start();

		PacketPlaySbStart pkt = new PacketPlaySbStart();
		pkt.cp = client.getPublic();
		PacketPlayCbStart.decodeworkwith(client.getPrivate());
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (MainGUI.currentState != ChannelState.PLAY)
					Utils.sleep();
				MainGUI.initializer.handler.currentPacketListener = cp;
				MainGUI.initializer.handler.sendPacket(pkt);
			}
		}).start();
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
}
