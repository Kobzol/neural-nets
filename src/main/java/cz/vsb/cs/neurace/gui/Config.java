package cz.vsb.cs.neurace.gui;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javax.swing.UIManager;

/**
 * Konfigurace programu. Ukládá a načítá konfiguraci.
 * 
 */
public class Config {

	/** jediná instance konfgurace */
	private static Config config;
	/** soubor s konfiguraci */
	//private File configFile;
        /* nastavení programu */
        public static Preferences prefs = Preferences.userNodeForPackage(Config.class);
	/** hodnoty */
	private Map<String, String> values = new TreeMap<String, String>();
        
        

	/**
	 * Konstruktor - načte hodnoty.
	 */
	private Config() {
            values.put("host", prefs.get("host", "localhost"));
            values.put("port", prefs.get("port", "9460"));
            values.put("serverPort", prefs.get("serverPort", "9460"));
            values.put("testPort", prefs.get("testPort", "9461"));
            values.put("ups", prefs.get("ups", "60"));
            values.put("dbPort", prefs.get("dbPort", "1527"));
            values.put("dbPassword", prefs.get("dbPassword", "neurace"));
            values.put("locale", prefs.get("locale", "default"));
            values.put("LookAndFeel", prefs.get("LookAndFeel", UIManager.getSystemLookAndFeelClassName()));
            values.put("serverDir", prefs.get("serverDir", System.getProperty("user.dir")+ File.separator+"server"));
            values.put("arrow", prefs.get("arrow", "true"));
            values.put("names", prefs.get("names", "true"));
            values.put("outlines", prefs.get("outlines", "false"));
            values.put("sensors", prefs.get("sensors", "true"));
            values.put("textures", prefs.get("textures", "true"));
            values.put("wheels", prefs.get("wheels", "false"));
	}
         
        //původní konstruktor
        /*private Config() {
             configFile = new File(System.getProperty("user.home") + File.separatorChar + ".neurace.cfg");
		if (configFile.canRead()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(configFile));
				String line;
				while ((line = in.readLine()) != null) {
					if (line.matches("\\s*") || line.matches("\\s*#.*")) {
						continue;
					}
					String[] ss = line.split("=", 2);
					if (ss.length == 2) {
						values.put(ss[0], ss[1]);
					}
				}
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("Can't read config file: " + configFile);
		}
        }*/

	/**
	 * Uloží hodnoty do souboru.
	 */
	public void save(String key, String value) {
                prefs.put(key, value);
		/*try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
			for (String prop : values.keySet()) {
				writer.write(prop + "=" + values.get(prop) + "\n");
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}*/
	}

	/**
	 * Vrátí hodnotu z konfigurace. Pokud neexistuje, pak vrátí prázdný řetězec.
	 * @param key klíč
	 * @return hodnota
	 */
	public String getString(String key) {
		String value = values.get(key);
		return value == null ? "" : value;
	}

	/**
	 * Vrátí hodnotu z konfigurace.
	 * @param key hodnota, pokud existuje, jinak null.
	 * @return
	 */
	public String get(String key) {
		return values.get(key);
	}

	/**
	 * Nastaví hodnotu pro klíč a konfiguraci uloží.
	 * @param key
	 * @param value
	 */
	public void set(String key, String value) {
		values.put(key, value);
		save(key, value);
	}

	/**
	 * Vrátí hodnotu z konfigurace.
	 * @param key
	 * @return hodnota pokud je nastavená, jinak 0
	 */
	public int getInt(String key) {
		String n = values.get(key);
		if (n == null) {
			return 0;
		}
		try {
			return Integer.parseInt(n);
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Vrátí hodnotu z konfigurace.
	 * @param key
	 * @return hodnota pokud je nastavená, jinak 0
	 */
	public float getFloat(String key) {
		String n = values.get(key);
		if (n == null) {
			return 0;
		}
		try {
			return Float.parseFloat(n);
		} catch (Exception e) {
			return 0;
		}
	}
        
        public boolean getBoolean(String key) {
		String n = values.get(key);
		if (n == null) {
			return false;
		}
		try {
			return Boolean.parseBoolean(n);
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Nastaví hodnotu pro klíč a konfiguraci uloží.
	 * @param key
	 * @param value
	 */
	public void set(String key, int value) {
		set(key, String.valueOf(value));
	}
        
        /**
	 * Nastaví hodnotu pro klíč a konfiguraci uloží.
	 * @param key
	 * @param value
	 */
	public void set(String key, boolean value) {
		set(key, String.valueOf(value));
	}

	/**
	 * Vrátí jedinou instanci konfigurace.
	 * @return
	 */
	public static Config get() {
		if (config == null) {
			config = new Config();
		}
		return config;
	}
}
