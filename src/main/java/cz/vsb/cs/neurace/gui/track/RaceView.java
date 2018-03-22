package cz.vsb.cs.neurace.gui.track;

import cz.vsb.cs.neurace.gui.Config;
import cz.vsb.cs.neurace.gui.RaceOptionsPanel;
import cz.vsb.cs.neurace.gui.Text;
import cz.vsb.cs.neurace.gui.ToggleListener;
import cz.vsb.cs.neurace.race.Checkpoint;
import cz.vsb.cs.neurace.track.Track;
import cz.vsb.cs.neurace.track.TrackChangeListener;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JComponent;
import javax.vecmath.Point2f;

/**
 * Vykresluje trať a auta, která jsou v závodě.
 */
public class RaceView extends JComponent implements TrackChangeListener,
		MouseWheelListener, KeyListener, MouseListener, MouseMotionListener {

	/** Trať */
	private Track track;
	/** auta v závodě */
	private List<VisualCar> cars;
	/** vykreslování objektů */
	ObjectPainter op;
	/** vykreslování trati */
	TrackPainter tp;

	/** měřítko 1 metr = [scale] pixelů */
	private float scale = 5;
	/** přiblížení */
	private float zoom = 1;
	/** sledování auta */
	private boolean followCar = true;
	/** vybrané auto */
	private VisualCar selectedCar = null;
	/** určuje zda byla trať změněna */
	private boolean trackChange = false;

	/** centrování pohledu */
	private boolean moveToCenter = false;
	/** střed pohledu */
	private Point2f center = new Point2f(0, 0);
	private int offsetX = 0;
	private int offsetY = 0;
	/** zbývající čas do konce závodu */
	private float remainingTime = -1;

	Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	Cursor moveCursor = new Cursor(Cursor.MOVE_CURSOR);

	/** použít textury na trať a okolí */
	private boolean useTextures = true;
	/** šipka směřující k dalšímu checkpointu */
	private boolean drawArrow = false;
	/** jména závodníků */
	private boolean drawNames = true;
	/** senzor detekující překážky */
	private boolean drawSensor = false;
	private boolean drawSensorBlock = false;
	private boolean drawSensorForAllCars = false;
	/** kola se mají vykreslovat přes karoserii */
	private boolean drawWheelsOver = false;

	private RaceOptionsPanel optionsPanel = null;

	private ToggleListener listener = null;

	/**
	 * Konstruktor.
	 * 
	 * @param track
	 *            vykreslovaná trať
	 */
	public RaceView(Track track) {
		this.track = track;

		useTextures = Config.get().getBoolean("textures");
		drawArrow = Config.get().getBoolean("arrow");
		drawNames = Config.get().getBoolean("names");
		drawSensor = Config.get().getBoolean("sensors");
		drawWheelsOver = Config.get().getBoolean("wheels");

		op = new ObjectPainter(scale);
		op.setDrawOutline(Config.get().getBoolean("outlines"));
		tp = new TrackPainter(track, scale);
		tp.setLineColor(Color.WHITE);
		tp.setUseTextures(useTextures);

		cars = new LinkedList<VisualCar>();

		if (track != null) {
			this.track.updateTrack();
			track.addTrackChangeListener(this);
		}

		this.addMouseWheelListener(this);
		this.addKeyListener(this);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		// this.setDoubleBuffered(true);
	}

	/*
	 * public class CompositeStroke implements Stroke { private Stroke stroke1,
	 * stroke2;
	 * 
	 * public CompositeStroke( Stroke stroke1, Stroke stroke2 ) { this.stroke1 =
	 * stroke1; this.stroke2 = stroke2; }
	 * 
	 * @Override public Shape createStrokedShape( Shape shape ) { return
	 * stroke2.createStrokedShape( stroke1.createStrokedShape( shape ) ); } }
	 */

	/**
	 * Vrátí všechny auta.
	 * 
	 * @return
	 */
	public VisualCar[] getCarArray() {
		return cars.toArray(new VisualCar[cars.size()]);
	}

	public List<VisualCar> getCars() {
		return cars;
	}

	@Override
	public Dimension getMaximumSize() {
		return getPreferredSize();
	}

	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension((int) (track.getWidth() * scale * zoom),
				(int) (track.getHeight() * scale * zoom));
	}

	@Override
	public void paintComponent(Graphics g) {
		if (track == null) {
			return;
		}

		if (trackChange) {
			this.track.updateTrack();
			tp.setEnviroPaint(track.getEnviroment());
			tp.genRoad();
			trackChange = false;
		}

		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_SPEED);

		if (zoom != 1) {
			g2.scale(zoom, zoom);
		}

		tp.paintRoad(g2);

		AffineTransform tr = g2.getTransform();

		// kreslení stop po pneumatikách
		g2.setColor(new Color(50, 50, 50, 80));
		for (VisualCar car : cars) {
			for (Point point : car.skidmarks) {
				g2.fillRect(point.x, point.y, 1, 1);
			}
		}

		// stredova cara
		tp.drawLine(g2);
		// kreslení checkpointů
		tp.drawCheckpoints(g2);

		for (VisualCar car : cars) {
			car.paint(g2, (int) (track.getWidth() * scale),
					(int) (track.getHeight() * scale));
		}

		op.paintObjects(track.getObjects(), g2);
		g2.setTransform(tr);

		if (remainingTime > 0) {

			Rectangle r = g2.getClipBounds();
			g2.setColor(Color.red);
			g2.setFont(new Font(g2.getFont().getName(), Font.BOLD,
					(int) (16 / zoom)));
			g2.drawString(Text.getString("remaining_time") + ": "
					+ (int) remainingTime, r.x + (r.width / 2), r.y
					+ (20 / zoom));
		}
	}

	/**
	 * Vrátí auto podle indexu.
	 * 
	 * @param index
	 * @return
	 */
	public VisualCar getCar(int index) {
		if (index < cars.size())
			return cars.get(index);
		else
			return null;
	}

	/**
	 * Přidá auto do závodu.
	 * 
	 * @param car
	 */
	public void addCar(VisualCar car) {
		cars.add(car);
		repaint();
	}

	/**
	 * Vyřadí auta ze závodu.
	 * 
	 * @param removeCars
	 */
	public void removeCars(List<VisualCar> removeCars) {
		cars.removeAll(removeCars);
		repaint();
	}

	/**
	 * Trať na které je závod.
	 * 
	 * @return
	 */
	public Track getTrack() {
		return track;
	}

	public float getScale() {
		return scale;
	}

	public float getZoom() {
		return zoom;
	}

	public void setFollowCar(boolean followCar) {
		this.followCar = followCar;
	}

	public VisualCar getSelectedCar() {
		return selectedCar;
	}

	public void setSelectedCar(VisualCar selectedCar) {
		this.selectedCar = selectedCar;
	}

	public boolean getDrawArrow() {
		return drawArrow;
	}

	public boolean getDrawNames() {
		return drawNames;
	}

	public boolean getDrawSensor() {
		return drawSensor;
	}

	public boolean getDrawWheelsOver() {
		return drawWheelsOver;
	}

	public boolean getFollowCar() {
		return followCar;
	}

	public boolean getUseTextures() {
		return useTextures;
	}

	public boolean getDrawOutline() {
		return op.getDrawOutline();
	}

	public void setDrawSensor(boolean drawSensor) {
		this.drawSensor = drawSensor;
		this.update();
	}

	public void setDrawSensorForAllCars(boolean drawSensorForAllCars) {
		this.drawSensorForAllCars = drawSensorForAllCars;
		this.update();
	}

	public boolean getDrawSensorBlock() {
		return drawSensorBlock;
	}

	public boolean getDrawSensorForAllCars() {
		return drawSensorForAllCars;
	}

	public void setDrawSensorBlock(boolean drawSensorBlock) {
		this.drawSensorBlock = drawSensorBlock;
		this.update();
	}

	public void setDrawArrow(boolean drawArrow) {
		this.drawArrow = drawArrow;
		this.update();
	}

	public void setDrawNames(boolean drawNames) {
		this.drawNames = drawNames;
		this.update();
	}

	public void setDrawWheelsOver(boolean drawWheelsOver) {
		this.drawWheelsOver = drawWheelsOver;
		this.update();
	}

	public void setUseTextures(boolean useTextures) {
		this.useTextures = useTextures;
		tp.setUseTextures(useTextures);
		this.update();
	}

	public void setDrawOutline(boolean drawOutline) {
		op.setDrawOutline(drawOutline);
		this.update();
	}

	public void setOptionsPanel(RaceOptionsPanel optionsPanel) {
		this.optionsPanel = optionsPanel;
	}

	public void setToggleListener(ToggleListener listener) {
		this.listener = listener;
	}

	/**
	 * Změní trať.
	 */
	public void setTrack(Track track) {
		this.track = track;
		this.track.updateTrack();
		tp.setTrack(track);
		this.setSize(this.getPreferredSize());
		moveToStart();
		for (VisualCar car : cars) {
			car.skidmarks.clear();
		}

		track.addTrackChangeListener(this);
	}

	public void moveToStart() {
		if (track.getCheckpoints().length != 0) {
			int h = this.getParent().getHeight();
			int w = this.getParent().getWidth();
			Checkpoint start = track.getCheckpoints()[0];
			this.center = new Point2f(start.getX() * scale, start.getY()
					* scale);
			this.scrollRectToVisible(new Rectangle((int) (center.x * zoom) - w
					/ 2, (int) (center.y * zoom) - h / 2, w, h));
		}
	}

	@Override
	public void trackChange() {
		this.trackChange = true;

	}

	public void update() {
		int h = this.getParent().getHeight();
		int w = this.getParent().getWidth();
		Rectangle r = this.getBounds();
		if (followCar && selectedCar != null) {
			this.scrollRectToVisible(new Rectangle(
					(int) (selectedCar.getX() * zoom) - w / 2,
					(int) (selectedCar.getY() * zoom) - h / 2, w, h));
			moveToCenter = false;

		} else if (moveToCenter) {
			this.scrollRectToVisible(new Rectangle((int) (center.x * zoom)
					- offsetX, (int) (center.y * zoom) - offsetY, w, h));
			moveToCenter = false;
		}
		if (this.getBounds().equals(r)) {
			this.repaint();
		}
	}

	public void setRemainingTime(float remainingTime) {
		this.remainingTime = remainingTime;
	}

	public float getMinZoom() {
		float h = (float) this.getParent().getHeight()
				/ (track.getHeight() * scale);
		float w = (float) this.getParent().getWidth()
				/ (track.getWidth() * scale);
		return (w < h) ? w : h;
	}

	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int clicks = e.getWheelRotation();

		moveToCenter = true;
		if (clicks < 0) {
			if (zoom < 5) {
				zoom += 0.1;
			}
		} else {
			if (zoom > getMinZoom()) {
				if (zoom > 0.15) {
					zoom -= 0.1;
				} else {
					zoom -= 0.01;
				}
			}
		}
		this.setSize(this.getPreferredSize());
		update();
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyChar() == '+') {
			moveToCenter = true;
			if (zoom < 5) {
				zoom += 0.1;
			}
			this.setSize(this.getPreferredSize());
			update();
		} else if (e.getKeyChar() == '-') {
			moveToCenter = true;
			if (zoom > getMinZoom()) {
				zoom -= 0.1;
			}
			this.setSize(this.getPreferredSize());
			update();
		} else if (e.getKeyChar() == 'f') {
			this.followCar = !followCar;
			if (listener != null) {
				listener.toggleButton(ToggleListener.ButtonType.FOLLOW);
			}
		} else {
			if (e.getKeyChar() == 'w') {
				this.drawWheelsOver = !drawWheelsOver;
				update();
			} else if (e.getKeyChar() == 'o') {
				this.op.setDrawOutline(!op.getDrawOutline());
				update();
			} else if (e.getKeyChar() == 't') {
				this.useTextures = !useTextures;
				tp.setUseTextures(useTextures);
				update();
			} else if (e.getKeyChar() == 'a') {
				this.drawArrow = !drawArrow;
				update();
			} else if (e.getKeyChar() == 'n') {
				this.drawNames = !drawNames;
				update();
			} else if (e.getKeyChar() == 's') {
				this.drawSensor = !drawSensor;
				update();
			}
			updateOptionsPanel();
		}

	}

	private void updateOptionsPanel() {
		if (optionsPanel != null) {
			optionsPanel.setSelected();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		this.requestFocus();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// center.set(e.getX()/zoom, e.getY()/zoom);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		this.setCursor(defaultCursor);
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		center.set(e.getX() / zoom, e.getY() / zoom);
		Point m = this.getParent().getMousePosition();
		if (m != null) {
			offsetX = m.x;
			offsetY = m.y;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		this.setCursor(moveCursor);
		Point m = this.getParent().getMousePosition();
		if (m != null) {
			offsetX = m.x;
			offsetY = m.y;
			moveToCenter = true;
			update();
		}
	}

}
