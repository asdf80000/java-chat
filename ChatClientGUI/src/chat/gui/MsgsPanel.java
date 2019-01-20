package chat.gui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class MsgsPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	public MsgsPanel() {
		BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(layout);
		setOpaque(false);
		//add(Box.createVerticalStrut(500));
//		addContainerListener(new ContainerListener() {
//			
//			@Override
//			public void componentRemoved(ContainerEvent e) {
//				//n();
//			}
//			
//			@Override
//			public void componentAdded(ContainerEvent e) {
//				n();
//			}
//			
//			public void n() {
//			}
//		});
	}
}
