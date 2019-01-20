package chat.gui;

import java.awt.Font;

import chat.main.MainGUI;

public class AgencyFont {
	public static Font loadAgencyFB() {
		Font f = null;
		try {
			f = Font.createFont(Font.TRUETYPE_FONT,
					MainGUI.class.getResourceAsStream("/chat/resource/font/AgencyFB-Bold.ttf"));
		} catch (Exception e) {
			f = MainGUI.instance.uiFont;
		}
		return f;
	}
}
