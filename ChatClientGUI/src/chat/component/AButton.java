package chat.component;

import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JButton;

public class AButton extends JButton {
	private static final long serialVersionUID = 1L;
	public AButton(String text) {
		super(text);
	}
	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(getBackground());
		if(getModel().isRollover())
			g2.setColor(g2.getColor().darker());
		if(getModel().isPressed())
			g2.setColor(g2.getColor().darker());
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		g2.setColor(getForeground());
		g2.setFont(getFont());
		FontMetrics fm = g2.getFontMetrics();
		g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2, (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
	}
}
