package chat.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;
import javax.swing.JPanel;

import chat.common.main.Utils;
import chat.main.MainGUI;

public class WorkingLabelPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public WorkingLabelPanel(String text) {
		setOpaque(true);
		setBackground(new Color(0x42d7f4));
		Dimension sz = new Dimension(500, 500);
		setPreferredSize(sz);
		setSize(sz);
		setLayout(null);

		JLabel label = new JLabel(text);
		label.setFont(MainGUI.instance.uiFont.deriveFont(0, 20f));
		label.setSize(label.getPreferredSize());
		label.setForeground(Color.white);
		label.setLocation((getWidth() - label.getWidth()) / 2, getHeight() / 2 + 150);
		add(label);

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
}