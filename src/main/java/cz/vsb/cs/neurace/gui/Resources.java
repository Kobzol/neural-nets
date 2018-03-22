package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.track.BlockObject;
import cz.vsb.cs.neurace.track.CylinderObject;
import cz.vsb.cs.neurace.track.TrackObject;
import cz.vsb.cs.neurace.track.WallObject;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2f;

/**
 * Načítání souborů
 * @author Petr Hamalčík
 * @author David Jezek
 */
public class Resources {
    private static Resources resources;
    private HashMap<String, TrackObject>  objects = new HashMap<String, TrackObject>();
    private HashMap<String, BufferedImage>  images = new HashMap<String, BufferedImage>();
    private HashMap<String, Vector2f> imageSizes = new HashMap<String, Vector2f>();
    private HashMap<String, HashMap<String, String>> carData = new HashMap<String, HashMap<String, String>>();

    String dataPath = System.getProperty("user.dir") + File.separatorChar + "client";

    FilenameFilter configFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".txt");
        }
    };

    FilenameFilter imageFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".bmp") ||
                    name.endsWith(".gif") || name.endsWith(".jpeg");
        }
    };
    
/**
        * Vrátí instanci MainWindow
        * @return instance MainWindow
        */
    public static Resources get() {
        return resources;
    }
    
    public static void set(Resources res) {
        resources = res;
    }

    public Resources() throws IOException{
        loadObjects();
        loadCarData();
        loadImages();
    }

    private String[] innerResourcesToLoadImages = {
    		"raceGraphics/objects/house2.png", 
    		"raceGraphics/objects/house3.png", 
    		"raceGraphics/objects/palm.png", 
    		"raceGraphics/objects/snowTree.png", 
    		"raceGraphics/objects/testBox.png", 
    		"raceGraphics/objects/testCylinder.png", 
    		"raceGraphics/objects/tree.png", 
    		"raceGraphics/objects/tree2.png", 
    		"raceGraphics/objects/tree3.png", 
    		"raceGraphics/objects/tree4.png", 
    		"raceGraphics/objects/tree5.png", 
    		"raceGraphics/objects/tree6.png", 
    		"raceGraphics/objects/tree7.png", 
    		"raceGraphics/objects/tree8.png"
    		};
    private String[] innerResourcesToLoadConfig = {
    		"raceGraphics/objects/test.txt", 
    		"raceGraphics/objects/trees.txt", 
    		};
    private String[] innerResourcesToLoadSurfaces = {
    		"raceGraphics/surfaces/asphalt.jpg", 
    		"raceGraphics/surfaces/concrete.jpg", 
    		"raceGraphics/surfaces/concrete2.jpg", 
    		"raceGraphics/surfaces/concrete3.jpg", 
    		"raceGraphics/surfaces/concrete4.jpg", 
    		"raceGraphics/surfaces/dirt.jpg", 
    		"raceGraphics/surfaces/grass.jpg", 
    		"raceGraphics/surfaces/gravel.jpg", 
    		"raceGraphics/surfaces/gravel3.jpg", 
    		"raceGraphics/surfaces/ice.jpg", 
    		"raceGraphics/surfaces/sand.jpg", 
    		"raceGraphics/surfaces/snow.jpg", 
    		};
    private String[] innerResourcesToLoadCarsImages = {
    		"raceGraphics/cars/Fabia.png", 
    		"raceGraphics/cars/Hummer.png", 
    		"raceGraphics/cars/ImprezaWRX.png", 
    		"raceGraphics/cars/Jeep.png", 
    		"raceGraphics/cars/Lamborghini.png", 
    		"raceGraphics/cars/LandRover.png", 
    		"raceGraphics/cars/Porsche.png", 
    		"raceGraphics/cars/Veyron.png", 
    		"raceGraphics/cars/ZondaR.png", 
    		};
    private String[] innerResourcesToLoadCarsSpecification = {
    		"cars/Fabia.txt", 
    		"cars/Hummer.txt", 
//    		"cars/ImprezaWRX.txt", 
    		"cars/Jeep.txt", 
    		"cars/Lamborghini.txt", 
//    		"cars/LandRover.txt", 
    		"cars/Porsche.txt", 
    		"cars/Veyron.txt", 
    		"cars/ZondaR.txt", 
    		};
    /**
     * Načte vlastnosti objektů
     * @throws IOException
     */
    private void loadObjects() throws IOException {
    	for(String resourceName : innerResourcesToLoadConfig){
            readObject(new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("resources/" + resourceName))));    		
    	}
        ArrayList<String> objectPaths = new ArrayList<String>();
        objectPaths.add(dataPath + File.separatorChar + "objects");
        objectPaths.add(getExecLocationPath() + File.separatorChar + "objects");
//    	String url = ClassLoader.getSystemResource("").toString();
//    	if(url.startsWith("file:/")){
//    		objectPaths.add(url.substring(5));
//    	}
//        System.out.println(objectPaths);
        for(String objectPath : objectPaths){
        	File objectDir = new File(objectPath);
            
            if(objectDir.isDirectory()) {
                File files[] = objectDir.listFiles(configFilter);
                for(File f: files) {
                    readObject(new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8")));
                }
            }
        }
    }

	private void readObject(BufferedReader reader) throws IOException {
		StreamTokenizer st = new StreamTokenizer(reader);
		st.commentChar('#');
		st.slashSlashComments(true);
		st.slashStarComments(true);
		st.eolIsSignificant(false);
		st.wordChars('_', '_');

		while(st.nextToken() != StreamTokenizer.TT_EOF) {
		    String type = st.sval;
		    st.nextToken();
		    String cl = st.sval;
		    if(cl.equals("BlockObject")) {
		        float length, width, height, angle;
		        st.nextToken();
		        length = (float)st.nval;
		        st.nextToken();
		        width = (float)st.nval;
		        st.nextToken();
		        height = (float)st.nval;
		        st.nextToken();
		        angle = (float)st.nval;
		        objects.put(type, new BlockObject(0, 0, type, length, width, height, angle));
		        //System.out.println(type);
		    }
		    else if(cl.equals("CylinderObject")) {
		        float height, radius;
		        st.nextToken();
		        height = (float)st.nval;
		        st.nextToken();
		        radius = (float)st.nval;
		        objects.put(type, new CylinderObject(0, 0, type, height, radius));
		        //System.out.println(type);
		    }
		    else if(cl.equals("WallObject")) {
		        float height, width;
		        st.nextToken();
		        height = (float)st.nval;
		        st.nextToken();
		        width = (float)st.nval;
		        objects.put(type, new WallObject(0, 0, type, height, width));
		        //System.out.println(type);
		    }

		    if(!cl.equals("WallObject")) {
		        st.nextToken();
		        float imageWidth = (float)st.nval;
		        st.nextToken();
		        float imageHeight = (float)st.nval;
		        imageSizes.put(type, new Vector2f(imageWidth, imageHeight));
		    }
		}
	}

    /**
     * Načte rozměry aut
     * @throws IOException
     */
    private void loadCarData() throws IOException {
    	for(String resourceName : innerResourcesToLoadCarsSpecification){
    		String key = resourceName.substring(resourceName.lastIndexOf('/')+1, resourceName.lastIndexOf('.'));
            loadCar(key, new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("resources/" + resourceName))));    		
    	}
        String carPath = dataPath + File.separatorChar + "cars";
        File carDir = new File(carPath);
        if(carDir.isDirectory()) {
            File files[] = carDir.listFiles(configFilter);
            for(File f: files) {
                String key = f.getName().substring(0, f.getName().indexOf('.'));
        		BufferedReader reader = new BufferedReader(new FileReader(f));
                loadCar(key, reader);
            } //for
        }
    }

	private void loadCar(String key, BufferedReader reader) throws FileNotFoundException,
			IOException {
		StreamTokenizer st = new StreamTokenizer(reader);
		st.commentChar('#');
		st.slashSlashComments(true);
		st.slashStarComments(true);
		st.eolIsSignificant(false);
		st.whitespaceChars('=', '=');
		st.whitespaceChars(';', ';');
		st.wordChars('_', '_');

		HashMap<String, String> carSpec = new HashMap<String, String>();
		while(st.nextToken() != StreamTokenizer.TT_EOF) {
			String name = st.sval;
			st.nextToken();
			String value  = "";
			switch(st.ttype){
			case StreamTokenizer.TT_NUMBER:
				value = Double.toString(st.nval);
				break;
			case StreamTokenizer.TT_WORD:
				value = st.sval;
			}
			carSpec.put(name,  value);
		}
//		if(carSpec.get("widthMirrors") == 0) { 
//			widthMirrors = width; }
		carData.put(key, carSpec);
	}

    /**
     * Načte obrázky
     * @throws IOException
     */
    private void loadImages() throws IOException {
    	ArrayList<String> resourceNames = new ArrayList<String>();
    	resourceNames.addAll(Arrays.asList(innerResourcesToLoadImages));
    	resourceNames.addAll(Arrays.asList(innerResourcesToLoadSurfaces));
    	resourceNames.addAll(Arrays.asList(innerResourcesToLoadCarsImages));
    	for(String resourceName : resourceNames){
    		String key = resourceName.substring(resourceName.lastIndexOf('/')+1, resourceName.lastIndexOf('.'));
            images.put(key, ImageIO.read(ClassLoader.getSystemResourceAsStream("resources/" + resourceName)));
    	}
        ArrayList<String> imagePaths = new ArrayList<String>();
        imagePaths.add(dataPath + File.separatorChar + "objects");
        imagePaths.add(dataPath + File.separatorChar + "cars");
        imagePaths.add(dataPath + File.separatorChar + "surfaces");
		imagePaths.add(Resources.getExecLocationPath());
//    	String url = ClassLoader.getSystemResource("").toString();
//    	if(url.startsWith("file:/")){
//    		imagePaths.add(url.substring(5));
//    	}
        //System.out.println(objectPath);
        for(String imagePath : imagePaths){
            File imageDir = new File(imagePath);
            if(imageDir.isDirectory()) {
                File files[] = imageDir.listFiles(imageFilter);
                for(File f: files) {
                    String key = f.getName().substring(0, f.getName().indexOf('.'));
                    images.put(key, ImageIO.read(f));
                    //System.out.println(key + ", "+ f.getName());
                }
            }
        }
    }

    public HashMap<String, BufferedImage> getImages() {
        return images;
    }

    public Collection<TrackObject> getObjects() {
        return objects.values();
    }

    public TrackObject getObject(String key) {
        return this.objects.get(key);
    }

    public BufferedImage getImage(String key) {
        return this.images.get(key);
    }

    public HashMap<String, String> getCarData(String key) {
        return this.carData.get(key);
    }

    public Vector2f getImageSize(String key) {
        return this.imageSizes.get(key);
    }

    public Collection<String> getCarNames(){
    	return carData.keySet();
    }

    public static String getExecLocationPath(){
        String path  = ClassLoader.getSystemResource("Text.properties").toString();
//    	System.out.println(path);
        if(path.startsWith("jar:")){
        	path = path.substring(4);
        	path = path.substring(0,path.lastIndexOf('!'));
//        	System.out.println(path);
        }
        if(path.startsWith("file:/")){
        	path = path.substring(6);
        	path = path.substring(0,path.lastIndexOf('/'));
//        	System.out.println(path);
        }
//    	System.out.println(path);
    	return path;
    }
}
