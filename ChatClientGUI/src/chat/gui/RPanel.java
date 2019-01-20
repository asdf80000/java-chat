package chat.gui;

import javax.swing.JPanel;

import chat.common.main.Utils;

public class RPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public RPanel() {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(!isDisplayable())
					Utils.sleep();
				while(isDisplayable()) {
					Utils.sleep(15);
					repaint();
				}
			}
		}).start();
	}
}
