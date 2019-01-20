package chat.gui;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

public class ErrorLoadingButton extends JComponent {
	private static final long serialVersionUID = 1L;

	public ErrorLoadingButton() {
	}
	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(Color.black);
		g.fillRect(0, 0, getWidth(), getHeight());
		g.setColor(Color.red);
		int sz = 20;
		g.fillPolygon(new int[] {sz, 0, 0, getWidth() - sz, getWidth(), getWidth()}, new int[] {0,0,sz,getHeight(), getHeight(), getHeight() - sz}, 6);
		g.fillPolygon(new int[] {0,0,sz,getWidth(), getWidth(), getWidth() -sz}, new int[] {getHeight() - sz, getHeight(), getHeight(), sz, 0, 0}, 6);
	}
}
