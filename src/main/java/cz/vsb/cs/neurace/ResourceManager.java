package cz.vsb.cs.neurace;

import java.net.URL;
import java.util.HashMap;

import javax.swing.ImageIcon;

/***
 * 
 * @author David Jezek
 *
 */
public class ResourceManager {
	
	private static HashMap<String, ImageIcon> iconMap = new HashMap<String, ImageIcon>();
	private static HashMap<String, ImageIcon> surfaceMap = new HashMap<>();
	private static HashMap<String, ImageIcon> objectMap = new HashMap<>();
	
	public static ImageIcon getImageIcon(String resourceName)
	{
		ImageIcon icon  = iconMap.get(resourceName);
		if(icon == null){
			URL resourceURL = ClassLoader.getSystemResource("resources/img/" + resourceName);
			if(resourceURL == null){
				throw new RuntimeException(String.format("Specified icon cannot be found (resources/img/%s).", resourceName));
			}
			icon = new ImageIcon(resourceURL);
			iconMap.put(resourceName, icon);
		}
		return icon;
	}

	
	public static ImageIcon getSurface(String resourceName)
	{
		ImageIcon surface = surfaceMap.get(resourceName);
		if(surface == null){
			URL resourceURL = ClassLoader.getSystemResource("resources/raceGraphics/" + resourceName);
			if(resourceURL == null){
				throw new RuntimeException(String.format("Specified icon cannot be found (resources/raceGraphics/%s).", resourceName));
			}
			surface = new ImageIcon(resourceURL);
			iconMap.put(resourceName, surface);
		}
		return surface;
	}
}
