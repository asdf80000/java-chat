package chat.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Random;

import javax.swing.JComponent;

import chat.common.main.Utils;

public class TimeLoadingButton extends JComponent {
	private static final long serialVersionUID = 1L;

	public static int dotS = 20;

	public TimeLoadingButton() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				boolean has = false;
				Point ip = new Point((int) (Math.random() * getWidth()), (int) (Math.random() * getHeight()));
				while (!has || isDisplayable()) {
					if (!has)
						has = has || isDisplayable();
					for (int i = 0; i < dotS; i++) {
						if (dots[i] == null) {
							PointDot pd = new PointDot();
							pd.x = ip.x;
							pd.y = ip.y;
							pd.tx = (int) (Math.random() * getWidth());
							pd.ty = (int) (Math.random() * getHeight());
							pd.start = System.currentTimeMillis();
							pd.end = pd.start + (int) (Math.random() * 600) + 400;
							pd.oldCor = rc();
							pd.newCor = rc();
							dots[i] = pd;
						} else {
							PointDot pd = dots[i];
							if (pd.end < System.currentTimeMillis()) {
								pd.start = pd.end;
								pd.end = pd.start + (int) (Math.random() * 600) + 400;
								pd.x = pd.tx;
								pd.y = pd.ty;
								pd.tx = (int) (Math.random() * getWidth());
								pd.ty = (int) (Math.random() * getHeight());
//								if((int)(Math.random() * 100) < 30) {
//									pd.tx = ip.x;
//									pd.ty = ip.y;
//								}
								pd.oldCor = pd.newCor;
								pd.newCor = rc();
							}
						}
					}
					Utils.sleep(0);
				}
			}
		}).start();
	}

	public PointDot[] dots = new PointDot[dotS];
	Random r = new Random();

	public int d() {
		return (int) (r.nextFloat() * 19) - 9;
	}

	float dw = 0;
	Point last = null;

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.black);

		g2.setColor(Color.white);
		for (int i = 0; i < dotS; i++) {
			for (int j = 0; j < dotS; j++) {
				PointDot p1 = dots[i];
				PointDot p2 = dots[j];
				Color c1 = p1.cor();
				Color c2 = p2.cor();
				int r = c1.getRed() / 2 + c2.getRed() / 2;
				int gr = c1.getGreen() / 2 + c2.getGreen() / 2;
				int b = c1.getBlue() / 2 + c2.getBlue() / 2;
				g2.setColor(new Color(r, gr, b));
				Point p1p = p1.getCurrent();
				Point p2p = p2.getCurrent();
				if (last == null) {
					last = new Point((int)(Math.random() * getWidth()), (int)(Math.random() * getHeight()));
				}
				g2.fillPolygon(new int[] {p1p.x, p2p.x, last.x}, new int[] {p1p.y, p2p.y, last.y}, 3);
				last = new Point(p2p.x, p2p.y);
				g2.drawLine(p1p.x, p1p.y, p2p.x, p2p.y);
			}
		}
	}

	public int r255() {
		return (int) (Math.random() * 256);
	}

	public Color rc() {
		return new Color(r255(), r255(), r255());
	}

	public static class PointDot {
		public int x;
		public int y;
		public int tx;
		public int ty;
		public long start;
		public long end;
		public Color oldCor;
		public Color newCor;

		public Color cor() {
			long len = end - start;
			long per = System.currentTimeMillis() - start;
			float rat = (float) per / (float) len;
			rat = Math.min(rat, 1f);
			float nr = rat;
			rat = 1f - rat;
			float r1 = oldCor.getRed();
			float g1 = oldCor.getGreen();
			float b1 = oldCor.getBlue();
			float r2 = newCor.getRed();
			float g2 = newCor.getGreen();
			float b2 = newCor.getBlue();

			float r3 = r1 * rat + r2 * nr;
			float g3 = g1 * rat + g2 * nr;
			float b3 = b1 * rat + b2 * nr;

			return new Color((int) r3, (int) g3, (int) b3);
		}

		public Point getCurrent() {
			long len = end - start;
			long per = System.currentTimeMillis() - start;
			float rat = (float) per / (float) len;
			float xd = (tx - x) * rat;
			float yd = (ty - y) * rat;
			return new Point(x + (int) xd, y + (int) yd);
		}
	}
}
