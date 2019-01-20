package chat.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import chat.common.packet.user.PacketUserSbGetUsername;
import chat.common.packet.user.PacketUserSbSetUsername;
import chat.component.AButton;
import chat.main.MainGUI;

public class SetNickMenu extends JPanel {
	private static final long serialVersionUID = 1L;

	public SetNickMenu(JPanel showOnExit) {
		setOpaque(true);
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(showOnExit.getPreferredSize().width, 250));
		setSize(getPreferredSize());
		setBackground(new Color(0, 0, 0));

		JLabel title = new JLabel("닉네임 변경");
		title.setFont(MainGUI.instance.boldFont.deriveFont(0, 40f));
		title.setHorizontalAlignment(JLabel.CENTER);
		title.setForeground(Color.white);
		add(title, BorderLayout.PAGE_START);

		JPanel body = new JPanel();
		body.setBackground(new Color(40, 40, 40));
		body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
		add(body, BorderLayout.CENTER);

		JPanel bottom = new JPanel();
		bottom.setBackground(new Color(20, 20, 20));
		add(bottom, BorderLayout.PAGE_END);

		JPanel bodyTop = new JPanel();
		{
			Box b = new Box(BoxLayout.X_AXIS);

			JLabel inputLabel = new JLabel("새로운 닉네임 입력:");
			inputLabel.setFont(MainGUI.instance.uiFont.deriveFont(0, 20));
			inputLabel.setForeground(Color.WHITE);
			b.add(inputLabel);

			body.add(b);
		}

		JTextField jtf = new JTextField(15);
		jtf.setFont(MainGUI.instance.uiFont.deriveFont(0, 25f));
		jtf.setBorder(null);
		jtf.setHorizontalAlignment(JTextField.CENTER);
		jtf.setBackground(new Color(60, 60, 60));
		jtf.setForeground(Color.white);
		jtf.setCaretColor(Color.white);
		jtf.setName("text");
		bodyTop.add(jtf);
		bodyTop.setOpaque(false);
		body.add(bodyTop);

		AButton conf = new AButton("확인");
		conf.setForeground(Color.white);
		conf.setBackground(new Color(255, 215, 0));
		conf.setFont(MainGUI.instance.boldFont.deriveFont(0, 30));
		conf.setCursor(MainGUI.hand);
		bottom.add(conf);
		conf.addActionListener(new ActionListener() {
			private static final int MAX_NICK_LENGTH = 13;

			@Override
			public void actionPerformed(ActionEvent e) {
				boolean suc = true;
				String msg = "";
				if (jtf.getText().length() == 0) {
					suc = false;
					msg = "닉네임을 입력하세요.";
				} else if (jtf.getText().length() > MAX_NICK_LENGTH) {
					suc = false;
					msg = "닉네임은 " + MAX_NICK_LENGTH + "글자 이하여야 합니다.";
				}
				if (suc) {
					PacketUserSbSetUsername p = new PacketUserSbSetUsername();
					p.username = jtf.getText();

					PacketUserSbGetUsername p0 = new PacketUserSbGetUsername();
					p0.message = false;

					MainGUI.initializer.handler.sendPacket(p);
					MainGUI.initializer.handler.sendPacket(p0);
					MainGUI.instance.frame.setContentPane(showOnExit);
					MainGUI.instance.frame.pack();
					MainGUI.instance.frame.repaint();
				} else {
					// JOptionPane.showMessageDialog(null, msg, "닉네임 변경",
					// JOptionPane.ERROR_MESSAGE);
					addMessage(body, msg);
				}
				System.gc();
			}
		});

		AButton back = new AButton("취소");
		back.setForeground(Color.white);
		back.setBackground(new Color(100, 100, 100));
		back.setFont(MainGUI.instance.boldFont.deriveFont(0, 30));
		back.setCursor(MainGUI.hand);
		bottom.add(back);
		back.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				MainGUI.instance.frame.setContentPane(showOnExit);
				MainGUI.instance.frame.pack();
				MainGUI.instance.frame.repaint();
				System.gc();
			}
		});
		jtf.addActionListener(conf.getActionListeners()[0]);
	}

	public void addMessage(JPanel body, String message) {
		for (Component c : body.getComponents()) {
			if (c.getName() != null && c.getName().equals("errormsg"))
				body.remove(c);
		}
		JPanel pn = new JPanel();
		pn.setName("errormsg");
		pn.setOpaque(false);

		JLabel error = new JLabel(message);
		error.setForeground(Color.RED);
		error.setHorizontalAlignment(JLabel.CENTER);
		error.setFont(MainGUI.instance.uiFont.deriveFont(0, 30));
		pn.add(error);

		body.add(pn);
		body.repaint();
		body.validate();
	}
}
