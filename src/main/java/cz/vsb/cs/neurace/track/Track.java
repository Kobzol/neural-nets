package cz.vsb.cs.neurace.track;

import cz.vsb.cs.neurace.connection.ClientRequest;
import cz.vsb.cs.neurace.connection.ClientRequestException;
import cz.vsb.cs.neurace.race.Checkpoint;
import cz.vsb.cs.neurace.race.RaceLine;
import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import javax.vecmath.Point2f;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.SAXException;


/**
 * Trať. Načítání ze souboru, z řetězce a natažení ze serveru.
 * 
 */
public class Track {


	/** Body, které tvoří trať */
	protected Vector<TrackPoint> points = new Vector<TrackPoint>();
	/** Rozměry tratě */
	private int width,  height;

        /** Vzdálenost mezi checkpointy */
	//final public static float checkpointsDistance = 100f;
	/** čary tratě */
        private RaceLine[] lines;
	/** checkpoity na trati */
	private Checkpoint[] checkPoints;

        /** seznam objektů na trati */
        private List<TrackObject> objects  = new ArrayList<TrackObject>();
	
        /** délka tratě */
	private float length;

        /** Listenery změn tratě */
	private List<TrackChangeListener> trackChangeListeners = new LinkedList<TrackChangeListener>();

        /** povrch v okolí tratě */
        private Surface enviroment = Surface.GRASS;
        /** základní povrch cesty */
        private Surface basicSurface = Surface.ASPHALT;
        /** šířka cesty v metrech */
        private float roadWidth = 20;


	/**
	 * Konstruktor nové tratě.
	 * 
	 * @param width šířka v px
	 * @param height výška v px
	 */
	public Track(int width, int height) {
		this.width = width;
		this.height = height;
	}

        /**
	 * Konstruktor nové tratě.
	 *
	 * @param width šířka v px
	 * @param height výška v px
	 */
	public Track(int width, int height, Surface enviroment, Surface road) {
		this.width = width;
		this.height = height;
                this.enviroment = enviroment;
                this.basicSurface = road;
	}

        public Track(int width, int height, Surface enviroment, Surface road, float roadWidth) {
		this.width = width;
		this.height = height;
                this.enviroment = enviroment;
                this.basicSurface = road;
                this.roadWidth = roadWidth;
	}

	/**
	 * Trať načtená ze souboru.
	 * 
	 * @param file soubor s tratí
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public Track(File file) throws FileNotFoundException, ParserConfigurationException, IOException, SAXException, ClassNotFoundException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(file);
		fromXML(doc.getDocumentElement());
	}

	/**
	 * Trať načtená z řetězce.
	 * 
	 * @param xml
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public Track(String xml) throws FileNotFoundException, ParserConfigurationException, IOException, SAXException, ClassNotFoundException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
		fromXML(doc.getDocumentElement());
	}

	/**
	 * Stažení tratě ze serveru.
	 * 
	 * @param name
	 * @param host
	 * @param port
	 * @throws IOException
	 * @throws ClientRequestException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public Track(String name, String host, int port) throws IOException, ClientRequestException,
			ParserConfigurationException, SAXException, ClassNotFoundException {
		ClientRequest request = new ClientRequest("loadtrack", host, port);
		request.write("track", name);
		request.endHead();
		String xml = request.readline();
		request.close();

		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new ByteArrayInputStream(xml.getBytes()));
		fromXML(doc.getDocumentElement());
	}

	/**
	 * Uložení tratě do souboru.
	 * 
	 * @param file do kterého se uloží trať
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws IOException
	 */
	public void save(File file) throws ParserConfigurationException, TransformerException, IOException {
                BufferedWriter fw = new BufferedWriter(new FileWriter(file));
		toXML(fw);
                fw.close();
	}

	/**
	 * Uložení tratě na server.
	 * 
	 * @param host
	 * @param port
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 * @throws ClientRequestException
	 */
	public void save(String name, String host, int port) throws IOException, ParserConfigurationException, TransformerException,
			ClientRequestException {
		ClientRequest request = new ClientRequest("savetrack", host, port);
		request.write("track", name);
		request.writeln();
		request.writeln(streamTrack());
		request.send();
		request.check(true);
		request.close();
	}

	/**
	 * Trať jako xml řetězec.
	 * 
	 * @return trať jako xml.
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public String streamTrack() throws ParserConfigurationException, TransformerException {
		StringWriter sw = new StringWriter();
		toXML(sw);
		return sw.toString();
	}

	/**
	 * Načtení tratě z xml.
	 * 
	 * @param root
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	private void fromXML(Element root) throws ParserConfigurationException, IOException, SAXException, ClassNotFoundException {
		NamedNodeMap rootAttributies = root.getAttributes();
		width = Integer.parseInt(rootAttributies.getNamedItem("width").getNodeValue());
		height = Integer.parseInt(rootAttributies.getNamedItem("height").getNodeValue());
                if(rootAttributies.getNamedItem("enviroment") != null)
                    enviroment = Surface.valueOf(rootAttributies.getNamedItem("enviroment").getNodeValue());
                if(rootAttributies.getNamedItem("basicSurface") != null)
                    basicSurface = Surface.valueOf(rootAttributies.getNamedItem("basicSurface").getNodeValue());
                if(rootAttributies.getNamedItem("roadWidth") != null)
                    roadWidth = Float.parseFloat(rootAttributies.getNamedItem("roadWidth").getNodeValue());

		NodeList nl = root.getChildNodes();
                Surface surf;
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			if (node.getNodeName().equals("point")) {
				NamedNodeMap attributies = node.getAttributes();
				float x = Float.parseFloat(attributies.getNamedItem("x").getNodeValue());
				float y = Float.parseFloat(attributies.getNamedItem("y").getNodeValue());
				float toX = Float.parseFloat(attributies.getNamedItem("toX").getNodeValue());
				float toY = Float.parseFloat(attributies.getNamedItem("toY").getNodeValue());
				float fromX = Float.parseFloat(attributies.getNamedItem("fromX").getNodeValue());
				float fromY = Float.parseFloat(attributies.getNamedItem("fromY").getNodeValue());
                                if(attributies.getNamedItem("surface") != null) {
                                    surf = Surface.valueOf(attributies.getNamedItem("surface").getNodeValue());
                                }
                                else {
                                    surf = Surface.ASPHALT;
                                }
				addPoint(new TrackPoint(x, y, toX, toY, fromX, fromY, surf, 40));
			}
                        else {
                            Class c = Class.forName(node.getAttributes().getNamedItem("class").getNodeValue());
                            float x = Float.parseFloat(node.getAttributes().getNamedItem("x").getNodeValue());
                            float y = Float.parseFloat(node.getAttributes().getNamedItem("y").getNodeValue());
                            String type = node.getAttributes().getNamedItem("type").getNodeValue();

                            if(c == BlockObject.class) {
                                float l = Float.parseFloat(node.getAttributes().getNamedItem("length").getNodeValue());
                                float w = Float.parseFloat(node.getAttributes().getNamedItem("width").getNodeValue());
                                float h = Float.parseFloat(node.getAttributes().getNamedItem("height").getNodeValue());
                                float angle= Float.parseFloat(node.getAttributes().getNamedItem("angle").getNodeValue());
                                objects.add(new BlockObject(x, y, type, l, w, h, angle));
                            }
                            else if(c == CylinderObject.class) {
                                float h = Float.parseFloat(node.getAttributes().getNamedItem("height").getNodeValue());
                                float radius= Float.parseFloat(node.getAttributes().getNamedItem("radius").getNodeValue());
                                objects.add(new CylinderObject(x, y, type, h, radius));
                            }
                            else if(c == WallObject.class) {
                                LinkedList pointList = new LinkedList();
                                float h = Float.parseFloat(node.getAttributes().getNamedItem("height").getNodeValue());
                                float w = Float.parseFloat(node.getAttributes().getNamedItem("width").getNodeValue());
                                NodeList pointNodes = node.getChildNodes();
                                for (int j = 0; j < pointNodes.getLength(); j++) {
                                    Node point = pointNodes.item(j);
                                    float px = Float.parseFloat(point.getAttributes().getNamedItem("x").getNodeValue());
                                    float py = Float.parseFloat(point.getAttributes().getNamedItem("y").getNodeValue());
                                    pointList.add(new Point2f(px, py));
                                }
                                objects.add(new WallObject(x, y, type, h, w, pointList));
                            }
                        }
		}
		this.close();
	}

	/**
	 * Zapíše trať do xml.
	 * 
	 * @param writer
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public void toXML(Writer writer) throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.newDocument();

		Element root = doc.createElement("track");
		doc.appendChild(root);
		root.setAttribute("width", String.valueOf(width));
		root.setAttribute("height", String.valueOf(height));
                root.setAttribute("enviroment", enviroment.toString());
                root.setAttribute("basicSurface", basicSurface.toString());
                root.setAttribute("roadWidth", String.valueOf(roadWidth));

		for (TrackPoint point : points) {
			Element elementPoint = doc.createElement("point");
			elementPoint.setAttribute("x", String.valueOf(point.getX()));
			elementPoint.setAttribute("y", String.valueOf(point.getY()));
			elementPoint.setAttribute("toX", String.valueOf(point.getToX()));
			elementPoint.setAttribute("toY", String.valueOf(point.getToY()));
			elementPoint.setAttribute("fromX", String.valueOf(point.getFromX()));
			elementPoint.setAttribute("fromY", String.valueOf(point.getFromY()));
                        elementPoint.setAttribute("surface", String.valueOf(point.getSurface()));
			root.appendChild(elementPoint);
		}
                for (TrackObject object : objects) {
                    Element element = doc.createElement("object");

                    element.setAttribute("class", object.getClass().getName());
                    element.setAttribute("x", String.valueOf(object.getPosX()));
                    element.setAttribute("y", String.valueOf(object.getPosY()));
                    element.setAttribute("type", object.getType().toString());

                    if(object.getClass() == CylinderObject.class) {
                        CylinderObject cylinder = (CylinderObject)object;
                        element.setAttribute("height", String.valueOf(cylinder.getHeight()));
			element.setAttribute("radius", String.valueOf(cylinder.getRadius()));
                    }
                    else if(object.getClass() == BlockObject.class) {
                        BlockObject block = (BlockObject)object;
			element.setAttribute("length", String.valueOf(block.getLength()));
                        element.setAttribute("width", String.valueOf(block.getWidth()));
                        element.setAttribute("height", String.valueOf(block.getHeight()));
                        element.setAttribute("angle", String.valueOf(block.getAngle()));
                    }
                    else if(object.getClass() == WallObject.class) {
                        WallObject wall = (WallObject)object;
                        element.setAttribute("height", String.valueOf(wall.getHeight()));
                        element.setAttribute("width", String.valueOf(wall.getWidth()));
                        for(Point2f point: wall.getPoints()) {
                            Element wallPoint = doc.createElement("point");
                            wallPoint.setAttribute("x", String.valueOf(point.x));
                            wallPoint.setAttribute("y", String.valueOf(point.y));
                            element.appendChild(wallPoint);
                        }
                    }

                    root.appendChild(element);
		}

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(writer);
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer = factory.newTransformer();
		transformer.transform(source, result);
	}

        public void updateTrack() {
                if(this.getPoints().length < 2) {
                    lines = new RaceLine[0];
                    checkPoints = new Checkpoint[0];
                    return;
                }
		int i = 0;
                int pointNum = 0;
		length = 0;
                LinkedList<RaceLine> linesList= new LinkedList<RaceLine>();
                LinkedList<Checkpoint> listc = new LinkedList<Checkpoint>();
		for (TrackPoint p : this.getPoints()) {
                        //vytvoření checkpoitu v bodě trati
                        if(p.isCheckpoint()) {
                            float alpha = (float)Math.atan((p.getToY()-p.getY())/(p.getToX()-p.getX()));
                            listc.add(new Checkpoint(p.getX(), p.getY(), alpha, this.roadWidth*1.5f, pointNum));
                        }
                        //vytvoření úseček z kterých se skládá trať
			LinePoint lp1 = null;
			for (LinePoint lp2 : p.getLineFrom().getLinePoints()) {
				if (lp1 != null) {
					RaceLine rl = new RaceLine(lp1, lp2, i++, pointNum, p);
					linesList.add(rl);
					length += rl.getLength();
				}
				lp1 = lp2;
			}
                        pointNum++;
		}

		lines = new RaceLine[linesList.size()];
		linesList.toArray(lines);

		//LinkedList<Checkpoint> listc = new LinkedList<Checkpoint>();
		//i = 0;
		//float d = checkpointsDistance;// delka od ostatniho checkpoitu
		//float dl = 0;//celkova delka od zacatku
		/*for (TrackPoint point : this.getPoints()) {
				float x = point.getX();
				float y = point.getY();
                                float alpha = (float)Math.atan((point.getToY()-point.getY())/(point.getToX()-point.getX()));
				listc.add(new Checkpoint(x, y, alpha, i++));

		}*/
		checkPoints = new Checkpoint[listc.size()];
		listc.toArray(checkPoints);
	}

	/**
	 * Šířka
	 * 
	 * @return šířka
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Výška
	 * 
	 * @return výška
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Nastaví šířku
	 * 
	 * @param width šířka
	 */
	public void setWidth(int width) {
		this.width = width;
		changed();
	}

	/**
	 * Nastaví výsku
	 * 
	 * @param height výška
	 */
	public void setHeight(int height) {
		this.height = height;
		changed();
	}

	/**
	 * Vrátí pole bodů, které tvoří trať.
	 * 
	 * @return pole bodů.
	 */
	public TrackPoint[] getPoints() {
		return points.toArray(new TrackPoint[points.size()]);
	}

        public Checkpoint[] getCheckpoints() {
		return checkPoints;
	}

        public RaceLine[] getLines() {
		return lines;
	}

        public List<TrackObject> getObjects() {
            return objects;
        }

        public Surface getEnviroment() {
            return enviroment;
        }

        public void setEnviroment(Surface enviroment) {
            this.enviroment = enviroment;
        }

        public Surface getBasicSurface() {
            return basicSurface;
        }

        public void setBasicSurface(Surface basicSurface) {
            this.basicSurface = basicSurface;
        }


	/**
	 * Nastaví nové body.
	 * 
	 * @param newPoints nové body
	 */
	public void setPoints(TrackPoint[] newPoints) {
		points.removeAllElements();
		for (TrackPoint p : newPoints) {
			addPoint(p);
                    
		}
		close();
		changed();
	}

	/**
	 * Přidání do tratě nového bodu.
	 * 
	 * @param point Nový bod
	 */
	public void addPoint(TrackPoint point) {
		if (points.size() > 0) {
			TrackPoint lastPoint = points.lastElement();
			Line line = new Line(lastPoint, point);
			lastPoint.setLineFrom(line);
			point.setLineTo(line);
		}
		points.add(point);
                //changed();
	}

        /**
         * Přidá objekt na trať
         * @param object nový objekt
         */
        public void addObject(TrackObject object) {
            objects.add(object);
            //changed();
        }

        public float getRoadWidth() {
            return roadWidth;
        }

        public void setRoadWidth(float roadWidth) {
            this.roadWidth = roadWidth;
        }


	/**
	 * Uzavře trať pridáním poslední čáry.
	 */
	public void close() {
		TrackPoint p1 = points.lastElement();
		TrackPoint p2 = points.firstElement();
		Line line = new Line(p1, p2);
		p1.setLineFrom(line);
		p2.setLineTo(line);
		//changed();
	}

	/**
	 * Zjistí, zda je trať uzavřená (ukončená)
	 * 
	 * @return true, pokud je trať uzavřená. Jinak false.
	 */
	public boolean isClosed() {
		if (points.isEmpty()) {
			return false;
		}
		return points.get(0).getLineTo() != null;
	}

	/**
	 * Přidání listenera.
	 * 
	 * @param trackChangeListener
	 */
	public void addTrackChangeListener(TrackChangeListener listener) {
		trackChangeListeners.add(listener);
	}

	/**
	 * Odstranění listenera
	 * 
	 * @param trackChangeListener
	 */
	public void removeTrackChangeListener(TrackChangeListener listener) {
		trackChangeListeners.remove(listener);
	}

	/**
	 * Trať změněna, upozorní listenery.
	 */
	public void changed() {
		for (TrackChangeListener l : trackChangeListeners) {
			l.trackChange();
		}
	}
}
