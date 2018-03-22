package cz.vsb.cs.neurace.gui;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Tato třída slouží k překladům řetězců.
 * 
 */
public class Text {

	private Text() {
	}
	/** pro získání překladů */
	static private ResourceBundle text;

	/** inicializace */
	static private void init() {
            String loc = Config.get().getString("locale");
            setLocale(loc);
	}
        
        

	/**
	 * Získání překladu.
	 * @param key klíč
	 * @return překlad
	 */
	static public String getString(String key) {
		try {
			if (text == null) {
				init();
			}
			return text.getString(key);
		} catch (Exception e) {
			//e.printStackTrace();
                        System.err.println(e.getMessage());
			return key;
		}
	}

        public static String translate(String msg) {
            String newMsg = msg;
            if(msg != null && msg.startsWith("Connection refused")) {
                newMsg = Text.getString("cant_connect");
            }
            else if(msg != null && msg.equals("Connection reset")) {
                newMsg = Text.getString("disconnected");
            }
            else if(msg != null && msg.startsWith("Address already in use")) {
                newMsg = Text.getString("port_in_use");
            }
            return newMsg;
        }

        
        public static void setLocale(String locale) {
            //String locale = Config.get().get("locale");
            if(locale.equals("default")) {
                text = ResourceBundle.getBundle("Text");
            } else {
                text = ResourceBundle.getBundle("Text", new Locale(locale));
            }
        }
}
