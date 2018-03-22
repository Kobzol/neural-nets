package cz.vsb.cs.neurace.gui.track;

import cz.vsb.cs.neurace.gui.Resources;
import cz.vsb.cs.neurace.gui.Text;
import cz.vsb.cs.neurace.race.Car;
import cz.vsb.cs.neurace.race.PhysicalCar.Drive;
import cz.vsb.cs.neurace.race.SensorTool;
import cz.vsb.cs.neurace.race.SensorTool.BlockDescription;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Vykresluje auto.
 * 
 */
public class VisualCar implements Comparable {

	/** vlastnosti auta */
	private float x, y, angle, wheel, speed, rpm;
	private float clientAngle, clientSpeed,
			clientDistance0, clientDistance4, clientDistance8, clientDistance16, clientDistance32,
			clientFriction, clientSkid, clientCheckpoint,/* clientSensor1,
			clientSensor2,*/ clientAcc, clientWheel, sensorFrontMiddleLeft,
			sensorFrontMiddleRight, sensorFrontLeft, sensorFrontRight,
			sensorFrontRightCorner1, sensorFrontRightCorner2,
			sensorFrontLeftCorner1, sensorFrontLeftCorner2, sensorLeft1,
			sensorLeft2, sensorRight1, sensorRight2, sensorRearRightCorner1,
			sensorRearRightCorner2, sensorRearLeftCorner1,
			sensorRearLeftCorner2, sensorRearLeft, sensorRearRight;

	private String driverName, carType;
	private int lap, position, checkpoints, time, lapTime, gear;
	private float skidInfo[] = new float[4];
	private int bestLap = Integer.MAX_VALUE;

	private Color color;
	private boolean connected;
	private RaceView racePaint;
	BufferedImage sprite;
	float length, width, wheelbase, wheelTrack, wheelRadius, wheelWidth;

	private int maxSkidmarks = 600;
	private float skidmarksTreshhold = 0.05f;

	private Car car;

	public LinkedList<Point> skidmarks = new LinkedList<Point>() {
		@Override
		public boolean add(Point e) {
			if (this.size() == maxSkidmarks) {
				this.pop();
			}
			return super.add(e);
		}
	};

	/**
	 * Konstruktor.
	 * 
	 * @param driverName
	 *            jméno řidiče
	 * @param number
	 *            číslo auta v závodě.
	 */
	public VisualCar(String driverName, String carType, int position,
			RaceView racePaint) {
		this.car = car;
		this.driverName = driverName == null ? "noname" : driverName;
		this.carType = carType;
		this.position = position;
		this.racePaint = racePaint;
		this.sprite = Resources.get().getImage(carType);
		HashMap<String, String> carData = Resources.get().getCarData(carType);
		this.length = Float.parseFloat(carData.get("carLength"));
		this.width = Float.parseFloat(carData.get("carWidth"));
		wheelRadius = Float.parseFloat(carData.get("wheelRadius"));
		wheelWidth = Float.parseFloat(carData.get("wheelWidth"));
		wheelbase = Float.parseFloat(carData.get("wheelbase"));
		wheelTrack = Float.parseFloat(carData.get("wheelTrack"));

		for (int i = 0; i < skidInfo.length; i++) {
			skidInfo[i] = 1.0f;
		}
		connected = true;
	}

	public boolean isConnected() {
		return connected;
	}

	public boolean disqualify(int laps, int dnfs) {
		connected = false;
		if (lap <= laps) {
			driverName = driverName.concat(" (" + Text.getString("dnf") + ")");
			this.position = racePaint.getCars().size() - dnfs;
			return false;
		}
		return true;
	}

	/**
	 * Vykreslí auto. Pokud je auto mimo kteslící oblast, pak vykreslí šipky,
	 * která ukazuje ve směru auta.
	 * 
	 * @param g2
	 */
	public void paint(Graphics2D g2, int trackWidth, int trackHeight) {
		if (!connected)
			return;
		paintCar(g2);
		/*
		 * if (y < 0) { // Auto za horním okrajem if (x < 0) { // za horním
		 * levým okrajem paintArrow(g2, -3 * Math.PI / 4, 0, 0);
		 * g2.drawString(driverName, 5, 10); g2.drawString(countLen(0, 0), 5,
		 * 20); } else if (x > trackWidth) { // za horním pravým okrajem
		 * paintArrow(g2, -Math.PI / 4, trackWidth, 0);
		 * g2.drawString(driverName, trackWidth - 40, 10);
		 * g2.drawString(countLen(trackWidth, 0), trackWidth - 40, 20); } else {
		 * // za horním okrajem paintArrow(g2, -Math.PI / 2, (int) x, 0);
		 * g2.drawString(driverName, (int) x, 10); g2.drawString(countLen((int)
		 * x, 0), (int) x, 20); } } else if (y > trackHeight) { // Auto za
		 * dolnim okrajem if (x < 0) { // za dolnim levým okrajem paintArrow(g2,
		 * 3 * Math.PI / 4, 0, trackHeight); g2.drawString(driverName, 5,
		 * trackHeight - 20); g2.drawString(countLen(0, trackHeight), 5,
		 * trackHeight - 10); } else if (x > trackWidth) { // za dolnim pravým
		 * okrajem paintArrow(g2, Math.PI / 4, trackWidth, trackHeight);
		 * g2.drawString(driverName, trackWidth - 40, trackHeight - 20);
		 * g2.drawString(countLen(trackWidth, trackHeight), trackWidth - 40,
		 * trackHeight - 10); } else { // za dolnim okrajem paintArrow(g2,
		 * Math.PI / 2, (int) x, trackHeight); g2.drawString(driverName, x,
		 * trackHeight - 20); g2.drawString(countLen((int) x, trackHeight), x,
		 * trackHeight - 10); } } else { // střední část if (x < 0) { // střední
		 * část v levo paintArrow(g2, Math.PI, 0, (int) y);
		 * g2.drawString(driverName, 5, (int) y + 10); g2.drawString(countLen(0,
		 * (int) y), 5, (int) y + 20); } else if (x > trackWidth) { // střední
		 * část v pravo paintArrow(g2, 0, trackWidth, (int) y);
		 * g2.drawString(driverName, trackWidth - 40, (int) y - 20);
		 * g2.drawString(countLen(trackWidth, (int) y), trackWidth - 40, (int) y
		 * - 10); } else { // střed, je auto je vidět. paintCar(g2); } }
		 */
	}

	/**
	 * Výpočet vzdálenosti auta od bodu [i,j]
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	/*
	 * private String countLen(int i, int j) { double d = Math.sqrt((x - i) * (x
	 * - i) + (y - j) * (y - j)); return String.valueOf(Math.round(d)); }
	 */

	/**
	 * Vykreslení šipky
	 * 
	 * @param g2
	 * @param theta
	 * @param x
	 * @param y
	 */
	/*
	 * private void paintArrow(Graphics2D g2, double theta, int x, int y) {
	 * g2.translate(x, y); g2.rotate(theta); g2.drawLine(0, 0, -20, 15);
	 * g2.drawLine(0, 0, -20, -15); g2.rotate(-theta); g2.translate(-x, -y); }
	 */

	/**
	 * Vykreslení šipky
	 * 
	 * @param g2
	 * @param theta
	 * @param x
	 * @param y
	 */
	private void drawArrow(Graphics2D g2, double theta, int scale) {
		g2.setColor(Color.BLUE);
		AffineTransform tr = g2.getTransform();
		Stroke s = g2.getStroke();

		g2.rotate(theta);
		g2.setStroke(new BasicStroke(scale));
		g2.drawLine(15 * scale, 0, 30 * scale, 0);
		g2.drawLine(30 * scale, 0, 25 * scale, 5 * scale);
		g2.drawLine(30 * scale, 0, 25 * scale, -5 * scale);

		g2.setTransform(tr);
		g2.setStroke(s);
	}

	private void drawSenzor(Graphics2D g2, int scale, boolean drawBlocks) {
		int sensor = (int)(SensorTool.longSensorLength * scale);

		g2.setColor(getSensorColor(sensorFrontMiddleLeft));
		g2.fillRect((int) (length/2 * scale), (int) (-width / 4 * scale),
				sensor, (int) (width / 4 * scale));
		
		g2.setColor(getSensorColor(sensorFrontLeft));
		g2.fillRect((int) (length / 2 * scale), (int) (-width / 2 * scale),
				sensor, (int) (width / 4 * scale));

		g2.setColor(getSensorColor(sensorFrontMiddleRight));
		g2.fillRect((int) (length / 2 * scale), (int) (0 * scale),
				sensor, (int) (width / 4 * scale));
		
		g2.setColor(getSensorColor(sensorFrontRight));
		g2.fillRect((int) (length / 2 * scale), (int) (width / 4 * scale),
				sensor, (int) (width / 4 * scale));
		
		g2.setColor(getSensorColor(sensorRight1));
		g2.fillRect((int) (0 * scale), (int) (width / 2 * scale), (int) length
				/ 2 * scale, (int) (SensorTool.shortSensorLength * scale));

		g2.setColor(getSensorColor(sensorRight2));
		g2.fillRect((int) (-length / 2 * scale), (int) (width / 2 * scale),
				(int) length / 2 * scale, (int) (SensorTool.shortSensorLength * scale));

		g2.setColor(getSensorColor(sensorLeft1));
		g2.fillRect((int) (0 * scale), (int) ((-width / 2 - SensorTool.shortSensorLength) * scale),
				(int) length / 2 * scale, (int) (SensorTool.shortSensorLength * scale));

		g2.setColor(getSensorColor(sensorLeft2));
		g2.fillRect((int) (-length / 2 * scale),
				(int) ((-width / 2 - SensorTool.shortSensorLength) * scale), (int) length / 2 * scale,
				(int) (SensorTool.shortSensorLength * scale));
		
		g2.setColor(getSensorColor(sensorRearLeft));
		g2.fillRect((int) ((-length / 2 - SensorTool.shortSensorLength) * scale),
				(int) (-width / 2 * scale), (int) SensorTool.shortSensorLength * scale,
				(int) (width / 2 * scale));
		
		g2.setColor(getSensorColor(sensorRearRight));
		g2.fillRect((int) ((-length / 2 - SensorTool.shortSensorLength) * scale), (int) (0 * scale),
				(int) SensorTool.shortSensorLength * scale, (int) (width / 2 * scale));

		int[] xPoints = new int[3];
		int[] yPoints = new int[3];
		g2.setColor(getSensorColor(sensorFrontLeftCorner1));
		xPoints[0] = (int)(length/2*scale); yPoints[0] = (int)(-width/2*scale);
		xPoints[1] = (int)(length/2*scale)+sensor; yPoints[1] = (int)(-width/2*scale);
		xPoints[2] = (int)(length/2*scale+sensor*Math.cos(Math.PI/180*45)); yPoints[2] = (int)(-width/2*scale-sensor*Math.sin(Math.PI/180*45));
		g2.fillPolygon(xPoints, yPoints, 3);
		
		g2.setColor(getSensorColor(sensorFrontLeftCorner2));
		xPoints[0] = (int)(length/2*scale); yPoints[0] = (int)(-width/2*scale);
		xPoints[1] = (int)(length/2*scale); yPoints[1] = (int)(-width/2*scale-sensor);
		xPoints[2] = (int)(length/2*scale+sensor*Math.cos(Math.PI/180*45)); yPoints[2] = (int)(-width/2*scale-sensor*Math.sin(Math.PI/180*45));
		g2.fillPolygon(xPoints, yPoints, 3);
		
		g2.setColor(getSensorColor(sensorFrontRightCorner1));
		xPoints[0] = (int)(length/2*scale); yPoints[0] = (int)(width/2*scale);
		xPoints[1] = (int)(length/2*scale)+sensor; yPoints[1] = (int)(width/2*scale);
		xPoints[2] = (int)(length/2*scale+sensor*Math.cos(Math.PI/180*45)); yPoints[2] = (int)(width/2*scale+sensor*Math.sin(Math.PI/180*45));
		g2.fillPolygon(xPoints, yPoints, 3);
		
		g2.setColor(getSensorColor(sensorFrontRightCorner2));
		xPoints[0] = (int)(length/2*scale); yPoints[0] = (int)(width/2*scale);
		xPoints[1] = (int)(length/2*scale); yPoints[1] = (int)(width/2*scale+sensor);
		xPoints[2] = (int)(length/2*scale+sensor*Math.cos(Math.PI/180*45)); yPoints[2] = (int)(width/2*scale+sensor*Math.sin(Math.PI/180*45));
		g2.fillPolygon(xPoints, yPoints, 3);

		g2.setColor(getSensorColor(sensorRearRightCorner1));
		xPoints[0] = (int)(-length/2*scale); yPoints[0] = (int)(width/2*scale);
		xPoints[1] = (int)(-length/2*scale-SensorTool.shortSensorLength*scale); yPoints[1] = (int)(width/2*scale);
		xPoints[2] = (int)(-length/2*scale-SensorTool.shortSensorLength*scale*Math.sin(Math.PI/180*45)); yPoints[2] = (int)(width/2*scale+SensorTool.shortSensorLength*scale*Math.cos(Math.PI/180*45));
		g2.fillPolygon(xPoints, yPoints, 3);

		g2.setColor(getSensorColor(sensorRearRightCorner2));
		xPoints[0] = (int)(-length/2*scale); yPoints[0] = (int)(width/2*scale);
		xPoints[1] = (int)(-length/2*scale); yPoints[1] = (int)(width/2*scale+SensorTool.shortSensorLength*scale);
		xPoints[2] = (int)(-length/2*scale-SensorTool.shortSensorLength*scale*Math.sin(Math.PI/180*45)); yPoints[2] = (int)(width/2*scale+SensorTool.shortSensorLength*scale*Math.cos(Math.PI/180*45));
		g2.fillPolygon(xPoints, yPoints, 3);

		g2.setColor(getSensorColor(sensorRearLeftCorner1));
		xPoints[0] = (int)(-length/2*scale); yPoints[0] = (int)(-width/2*scale);
		xPoints[1] = (int)(-length/2*scale-SensorTool.shortSensorLength*scale); yPoints[1] = (int)(-width/2*scale);
		xPoints[2] = (int)(-length/2*scale-SensorTool.shortSensorLength*scale*Math.sin(Math.PI/180*45)); yPoints[2] = (int)(-width/2*scale-SensorTool.shortSensorLength*scale*Math.cos(Math.PI/180*45));
		g2.fillPolygon(xPoints, yPoints, 3);
		
		g2.setColor(getSensorColor(sensorRearLeftCorner2));
		xPoints[0] = (int)(-length/2*scale); yPoints[0] = (int)(-width/2*scale);
		xPoints[1] = (int)(-length/2*scale); yPoints[1] = (int)(-width/2*scale-SensorTool.shortSensorLength*scale);
		xPoints[2] = (int)(-length/2*scale-SensorTool.shortSensorLength*scale*Math.sin(Math.PI/180*45)); yPoints[2] = (int)(-width/2*scale-SensorTool.shortSensorLength*scale*Math.cos(Math.PI/180*45));
		g2.fillPolygon(xPoints, yPoints, 3);
		if(drawBlocks){
			drawLogarithmicSensorBlocks(g2, SensorTool.numberOfPartsForLongSensor, SensorTool.longSensorLength*scale, sensorFrontMiddleLeft, length/2*scale,  -width / 4 * scale, 1, 0, width/4*scale);
			drawLogarithmicSensorBlocks(g2, SensorTool.numberOfPartsForLongSensor, SensorTool.longSensorLength*scale, sensorFrontLeft, length/2*scale, -width/2*scale, 1, 0, width/4*scale);
			drawLogarithmicSensorBlocks(g2, SensorTool.numberOfPartsForLongSensor, SensorTool.longSensorLength*scale, sensorFrontMiddleRight, length/2*scale, 0*scale, 1, 0, width/4*scale);
			drawLogarithmicSensorBlocks(g2, SensorTool.numberOfPartsForLongSensor, SensorTool.longSensorLength*scale, sensorFrontRight, length/2*scale, width / 4 * scale, 1, 0, width/4*scale);
			drawLinearSensorBlocks(g2, SensorTool.numberOfPartsForShortSensor, SensorTool.shortSensorLength*scale, sensorRight1, 0*scale, width/2*scale, 0, 1, length/2 * scale);
			drawLinearSensorBlocks(g2, SensorTool.numberOfPartsForShortSensor, SensorTool.shortSensorLength*scale, sensorRight2, -length/2*scale, width/2*scale, 0, 1, length/2 * scale);
			drawLinearSensorBlocks(g2, SensorTool.numberOfPartsForShortSensor, SensorTool.shortSensorLength*scale, sensorLeft1, 0*scale, -width/2*scale, 0, -1, length/2 * scale);
			drawLinearSensorBlocks(g2, SensorTool.numberOfPartsForShortSensor, SensorTool.shortSensorLength*scale, sensorLeft2, -length/2*scale, -width/2*scale, 0, -1, length/2 * scale);
			drawLogarithmicSensorBlocks(g2, SensorTool.numberOfPartsForShortSensor, SensorTool.shortSensorLength*scale, sensorRearLeft, -length/2*scale, -width/2*scale, -1, 0, width/2*scale);
			drawLogarithmicSensorBlocks(g2, SensorTool.numberOfPartsForShortSensor, SensorTool.shortSensorLength*scale, sensorRearRight, -length/2*scale, -0*scale, -1, 0, width/2*scale);
			drawAngleSensor(g2, SensorTool.numberOfPartsForLongSensor, SensorTool.longSensorLength*scale, sensorFrontLeftCorner1, length/2*scale, -width/2*scale, 0, -45);
			drawAngleSensor(g2, SensorTool.numberOfPartsForLongSensor, SensorTool.longSensorLength*scale, sensorFrontLeftCorner2, length/2*scale, -width/2*scale, -45, -90);
			drawAngleSensor(g2, SensorTool.numberOfPartsForLongSensor, SensorTool.longSensorLength*scale, sensorFrontRightCorner1, length/2*scale, width/2*scale, 0, 45);
			drawAngleSensor(g2, SensorTool.numberOfPartsForLongSensor, SensorTool.longSensorLength*scale, sensorFrontRightCorner2, length/2*scale, width/2*scale, 45, 90);
			drawAngleSensor(g2, SensorTool.numberOfPartsForShortSensor, SensorTool.shortSensorLength*scale, sensorRearRightCorner1, -length/2*scale, width/2*scale, 135, 180);
			drawAngleSensor(g2, SensorTool.numberOfPartsForShortSensor, SensorTool.shortSensorLength*scale, sensorRearRightCorner2, -length/2*scale, width/2*scale, 90, 135);
			drawAngleSensor(g2, SensorTool.numberOfPartsForShortSensor, SensorTool.shortSensorLength*scale, sensorRearLeftCorner1, -length/2*scale, -width/2*scale, -135, -180);
			drawAngleSensor(g2, SensorTool.numberOfPartsForShortSensor, SensorTool.shortSensorLength*scale, sensorRearLeftCorner2, -length/2*scale, -width/2*scale, -90, -135);
		}
}

	private void drawLinearSensorBlocks(Graphics2D g2, int parts,
			float sensorLength, float value, float xCenterOffset, float yCenterOffset, int xSensorDirection, int ySensorDirection, float sensorWidth) {
		for(BlockDescription blockDescription : SensorTool.getLinearSteps(parts, sensorLength)){
			if(blockDescription.value <= value){
				g2.setColor(Color.RED);
			} else {
				g2.setColor(Color.BLACK);
			}
			g2.drawRect((int)(blockDescription.start*xSensorDirection+xCenterOffset + (xSensorDirection==-1?-1:0)*blockDescription.getLength()),
					(int)(blockDescription.start*ySensorDirection+yCenterOffset + (ySensorDirection==-1?-1:0)*blockDescription.getLength()),
					(int)(blockDescription.getLength()*Math.abs(xSensorDirection) + sensorWidth*Math.abs(ySensorDirection)), 
					(int)(blockDescription.getLength()*Math.abs(ySensorDirection) + sensorWidth*Math.abs(xSensorDirection)));
		}
	}
	private void drawLogarithmicSensorBlocks(Graphics2D g2, int parts,
			float sensorLength, float value, float xCenterOffset, float yCenterOffset, int xSensorDirection, int ySensorDirection, float sensorWidth) {
		for(BlockDescription blockDescription : SensorTool.getLogaritmicsSteps(parts, sensorLength)){
			if(blockDescription.value <= value){
				g2.setColor(Color.RED);
			} else {
				g2.setColor(Color.BLACK);
			}
			g2.drawRect((int)(blockDescription.start*xSensorDirection+xCenterOffset + (xSensorDirection==-1?-1:0)*blockDescription.getLength()),
					(int)(blockDescription.start*ySensorDirection+yCenterOffset + (ySensorDirection==-1?-1:0)*blockDescription.getLength()),
					(int)(blockDescription.getLength()*Math.abs(xSensorDirection) + sensorWidth*Math.abs(ySensorDirection)), 
					(int)(blockDescription.getLength()*Math.abs(ySensorDirection) + sensorWidth*Math.abs(xSensorDirection)));
		}
	}
	
	private void drawAngleSensor(Graphics2D g2, int parts,
			float sensorLength, float value, float xCenterOffset, float yCenterOffset, float startAngle, float endAngle){
		int[] xPoints = new int[4];
		int[] yPoints = new int[4];
		for(BlockDescription blockDescription : SensorTool.getLogaritmicsSteps(parts, sensorLength)){
			xPoints[0] = (int)(blockDescription.start * Math.cos(2*Math.PI/360*startAngle)+xCenterOffset);
			yPoints[0] = (int)(blockDescription.start * Math.sin(2*Math.PI/360*startAngle)+yCenterOffset);
			xPoints[1] = (int)(blockDescription.end * Math.cos(2*Math.PI/360*startAngle)+xCenterOffset);
			yPoints[1] = (int)(blockDescription.end * Math.sin(2*Math.PI/360*startAngle)+yCenterOffset);
			xPoints[2] = (int)(blockDescription.end * Math.cos(2*Math.PI/360*endAngle)+xCenterOffset);
			yPoints[2] = (int)(blockDescription.end * Math.sin(2*Math.PI/360*endAngle)+yCenterOffset);
			xPoints[3] = (int)(blockDescription.start * Math.cos(2*Math.PI/360*endAngle)+xCenterOffset);
			yPoints[3] = (int)(blockDescription.start * Math.sin(2*Math.PI/360*endAngle)+yCenterOffset);
			if(blockDescription.value <= value){
				g2.setColor(Color.RED);
			} else {
				g2.setColor(Color.BLACK);
			}
			g2.drawPolygon(xPoints, yPoints, 4);
		}
	}


	private static Color getSensorColor(float value) {
		if (value == 0.0f) {
			return new Color(180, 180, 255, 120);
		} else if (value < 0.33) {
			return new Color(50, (int) (170 + value * 85), 50,
					(int) (120 - value * 60));
		} else if(value < 0.66){
			return new Color((int) (170 + value * 85), (int) (170 + value * 85), 50,
					(int) (120 - value * 60));
		} else {
			return new Color((int) (170 + value * 85), 50, 50,
					(int) (120 - value * 60));
		}
	}

	/**
	 * Vykreslení auta.
	 * 
	 * @param g2
	 */
	public void paintCar(Graphics2D g2) {
		AffineTransform af = g2.getTransform();
		g2.translate(x, y);

		// auta mají čtyřnásobný počet pixelů oproti trati a ostatním objektům
		float scale = this.racePaint.getScale() * 4;
		g2.scale(0.25, 0.25);

		g2.rotate(angle);

		if (racePaint.getSelectedCar() == this) {
			if (racePaint.getDrawArrow()) {
				double theta = (clientCheckpoint - 0.5) * 2 * Math.PI;
				drawArrow(g2, theta, (int) racePaint.getScale());
			}
		}
		if ((racePaint.getSelectedCar() == this && racePaint.getDrawSensor()) ||
				racePaint.getDrawSensorForAllCars()) {
			drawSenzor(g2, (int) scale, racePaint.getDrawSensorBlock());
		}
		// AffineTransform center = g2.getTransform();

		int halfBase = (int) (wheelbase / 2 * scale);
		int halfTrack = (int) (wheelTrack / 2 * scale);
		int halfWidth = (int) (width / 2 * scale);
		int whRadius = (int) (wheelRadius * scale);
		if (whRadius < 3)
			whRadius = 3;
		int halfWhWidth = (int) (wheelWidth / 2 * scale);
		if (halfWhWidth < 1)
			halfWhWidth = 1;
		/*
		 * if((width/2*scale)-halfWidth > 0.4) { halfWidth++; } if(halfTrack+2 >
		 * halfWidth) { halfTrack--; }
		 */
		// int sx = (int)x;
		// int sy = (int)y;
		Rectangle bounds = racePaint.getBounds();
		float zoom = racePaint.getZoom();

		if (racePaint.getDrawWheelsOver()) {
			g2.drawImage(sprite, (int) (-length / 2 * scale),
					(int) (-halfWidth), (int) (length * scale), 2 * halfWidth,
					null);
		}

		// prave predni kolo
		g2.translate(halfBase, halfTrack);
		if (skidInfo[0] < skidmarksTreshhold) {
			// skidmarks.add(new Point(sx+halfBase, sy+halfTrack));
			skidmarks
					.add(new Point(
							(int) ((g2.getTransform().getTranslateX() - bounds
									.getX()) / zoom), (int) ((g2.getTransform()
									.getTranslateY() - bounds.getY()) / zoom)));
		}
		g2.rotate(wheel);
		g2.setColor(Color.BLACK);
		g2.fillRect(-whRadius, -halfWhWidth, 2 * whRadius, 2 * halfWhWidth);
		g2.rotate(-wheel);

		// leve predni kolo
		g2.translate(0, -2 * halfTrack);
		if (skidInfo[1] < skidmarksTreshhold) {
			// skidmarks.add(new Point(sx+halfBase, sy-halfTrack));
			skidmarks
					.add(new Point(
							(int) ((g2.getTransform().getTranslateX() - bounds
									.getX()) / zoom), (int) ((g2.getTransform()
									.getTranslateY() - bounds.getY()) / zoom)));
		}
		g2.rotate(wheel);
		g2.fillRect(-whRadius, -halfWhWidth, 2 * whRadius, 2 * halfWhWidth);
		g2.rotate(-wheel);

		// leve zadni
		g2.translate(-2 * halfBase, 0);
		if (skidInfo[3] < skidmarksTreshhold) {
			// skidmarks.add(new Point(sx-halfBase, sy-halfTrack));
			skidmarks
					.add(new Point(
							(int) ((g2.getTransform().getTranslateX() - bounds
									.getX()) / zoom), (int) ((g2.getTransform()
									.getTranslateY() - bounds.getY()) / zoom)));
		}
		g2.fillRect(-whRadius, -halfWhWidth, 2 * whRadius, 2 * halfWhWidth);

		// prave zadni
		g2.translate(0, 2 * halfTrack);
		if (skidInfo[2] < skidmarksTreshhold) {
			// skidmarks.add(new Point(sx-halfBase, sy+halfTrack));
			skidmarks
					.add(new Point(
							(int) ((g2.getTransform().getTranslateX() - bounds
									.getX()) / zoom), (int) ((g2.getTransform()
									.getTranslateY() - bounds.getY()) / zoom)));
		}
		g2.fillRect(-whRadius, -halfWhWidth, 2 * whRadius, 2 * halfWhWidth);

		g2.translate(halfBase, -halfTrack);

		// karoserie
		// g2.setTransform(center);

		if (!racePaint.getDrawWheelsOver()) {
			g2.drawImage(sprite, (int) (-length / 2 * scale),
					(int) (-halfWidth), (int) (length * scale), 2 * halfWidth,
					null);
		}

		if (color == null) {
			color = Color.RED;
		}
		g2.setColor(color);
		g2.fillRect(-6, -6, 12, 12);

		g2.setTransform(af);

		if (racePaint.getDrawNames()) {
			g2.translate(x, y);
			g2.setColor(new Color(255, 255, 255, 80));
			FontMetrics fm = g2.getFontMetrics();
			int strWidth = fm.stringWidth(driverName);
			g2.fillRect(10, 0, strWidth, 12);
			g2.setColor(Color.BLACK);
			g2.drawString(driverName, 10, 10);
			g2.setTransform(af);
		}
	}

	public void setPosition(int value) {
		if (connected) {
			position = value;
		}
	}

	public void setValue(String prop, float value) {
		setValue(prop, String.valueOf(value));
	}

	public void setValue(String prop, int value) {
		setValue(prop, String.valueOf(value));
	}

	/**
	 * Nastavení vlastnosti auta.
	 * 
	 * @param prop
	 * @param value
	 */
	public void setValue(String prop, String value) {
		if (value == null) {
			return;
		}
		try {
			if (prop.equals("x")) {
				x = Float.parseFloat(value) * racePaint.getScale();
			} else if (prop.equals("y")) {
				y = Float.parseFloat(value) * racePaint.getScale();
			} else if (prop.equals("angle")) {
				angle = Float.parseFloat(value);
			} else if (prop.equals("wheel")) {
				wheel = Float.parseFloat(value);
			} else if (prop.equals("lap")) {
				lap = Integer.parseInt(value);
			} else if (prop.equals("position")) {
				if (connected) {
					position = Integer.parseInt(value);
				}
			} else if (prop.equals("checkpoints")) {
				checkpoints = Integer.parseInt(value);
			} else if (prop.equals("carTime")) {
				time = Integer.parseInt(value);
			} else if (prop.equals("lapTime")) {
				lapTime = Integer.parseInt(value);
			} else if (prop.equals("bestLap")) {
				bestLap = Integer.parseInt(value);
			} else if (prop.equals("speed")) {
				speed = Float.parseFloat(value);
			} else if (prop.equals("gear")) {
				gear = Integer.parseInt(value);
			} else if (prop.equals("rpm")) {
				rpm = Float.parseFloat(value);
			} else if (prop.equals("clientDistance0")) {
				clientDistance0 = Float.parseFloat(value);
			} else if (prop.equals("clientDistance4")) {
				clientDistance4 = Float.parseFloat(value);
			} else if (prop.equals("clientDistance8")) {
				clientDistance8 = Float.parseFloat(value);
			} else if (prop.equals("clientDistance16")) {
				clientDistance16 = Float.parseFloat(value);
			} else if (prop.equals("clientDistance32")) {
				clientDistance32 = Float.parseFloat(value);
			} else if (prop.equals("clientAngle")) {
				clientAngle = Float.parseFloat(value);
			} else if (prop.equals("clientSpeed")) {
				clientSpeed = Float.parseFloat(value);
			} else if (prop.equals("clientAcc")) {
				clientAcc = Float.parseFloat(value);
			} else if (prop.equals("clientWheel")) {
				clientWheel = Float.parseFloat(value);
			} else if (prop.equals("skidInfo0")) {
				skidInfo[0] = Float.parseFloat(value);
			} else if (prop.equals("skidInfo1")) {
				skidInfo[1] = Float.parseFloat(value);
			} else if (prop.equals("skidInfo2")) {
				skidInfo[2] = Float.parseFloat(value);
			} else if (prop.equals("skidInfo3")) {
				skidInfo[3] = Float.parseFloat(value);
			} else if (prop.equals("clientSkid")) {
				clientSkid = Float.parseFloat(value);
			} else if (prop.equals("clientFriction")) {
				clientFriction = Float.parseFloat(value);
			} else if (prop.equals("clientCheckpoint")) {
				clientCheckpoint = Float.parseFloat(value);
//			} else if (prop.equals("clientSensor1")) {
//				clientSensor1 = Float.parseFloat(value);
//			} else if (prop.equals("clientSensor2")) {
//				clientSensor2 = Float.parseFloat(value);
			} else if (prop.equals("sensorFrontMiddleLeft")) {
				sensorFrontMiddleLeft = Float.parseFloat(value);
			} else if (prop.equals("sensorFrontMiddleRight")) {
				sensorFrontMiddleRight = Float.parseFloat(value);
			} else if (prop.equals("sensorFrontLeft")) {
				sensorFrontLeft = Float.parseFloat(value);
			} else if (prop.equals("sensorFrontRight")) {
				sensorFrontRight = Float.parseFloat(value);
			} else if (prop.equals("sensorFrontRightCorner1")) {
				sensorFrontRightCorner1 = Float.parseFloat(value);
			} else if (prop.equals("sensorFrontRightCorner2")) {
				sensorFrontRightCorner2 = Float.parseFloat(value);
			} else if (prop.equals("sensorFrontLeftCorner1")) {
				sensorFrontLeftCorner1 = Float.parseFloat(value);
			} else if (prop.equals("sensorFrontLeftCorner2")) {
				sensorFrontLeftCorner2 = Float.parseFloat(value);
			} else if (prop.equals("sensorLeft1")) {
				sensorLeft1 = Float.parseFloat(value);
			} else if (prop.equals("sensorLeft2")) {
				sensorLeft2 = Float.parseFloat(value);
			} else if (prop.equals("sensorRight1")) {
				sensorRight1 = Float.parseFloat(value);
			} else if (prop.equals("sensorRight2")) {
				sensorRight2 = Float.parseFloat(value);
			} else if (prop.equals("sensorRearRightCorner1")) {
				sensorRearRightCorner1 = Float.parseFloat(value);
			} else if (prop.equals("sensorRearRightCorner2")) {
				sensorRearRightCorner2 = Float.parseFloat(value);
			} else if (prop.equals("sensorRearLeftCorner1")) {
				sensorRearLeftCorner1 = Float.parseFloat(value);
			} else if (prop.equals("sensorRearLeftCorner2")) {
				sensorRearLeftCorner2 = Float.parseFloat(value);
			} else if (prop.equals("sensorRearLeft")) {
				sensorRearLeft = Float.parseFloat(value);
			} else if (prop.equals("sensorRearRight")) {
				sensorRearRight = Float.parseFloat(value);
			} else if (prop.equals("color")) {
				setColor(value);
			} else {
				System.err
						.println("Car.setValue(String,String): unknown prop: '"
								+ prop + "'");
			}
		} catch (NumberFormatException e) {
			System.err
					.println("Car.setValue(String,String): parse value error: "
							+ e.getMessage() + " '" + value + "'");
		}
	}

	/**
	 * Nastavení barvy auta.
	 * 
	 * @param value
	 *            barva ve tvaru RRGGBB.
	 */
	public void setColor(String value) {
		try {
			int r = Integer.parseInt(value.substring(0, 2), 16);
			int g = Integer.parseInt(value.substring(2, 4), 16);
			int b = Integer.parseInt(value.substring(4, 6), 16);
			color = new Color(r, g, b);
		} catch (Exception e) {
			System.err.println("setColor exception");
			// e.printStackTrace();
		}
	}

	/**
	 * Jméno řidiče.
	 * 
	 * @return
	 */
	public String getDriver() {
		return driverName;
	}

	/**
	 * Nastaví souřadnici x auta
	 * 
	 * @param x
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * Nastaví souřadnici y auta
	 * 
	 * @param y
	 */
	public void setY(float y) {
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	/**
	 * Nastaví otočení auta
	 * 
	 * @param angle
	 */
	public void setAngle(float angle) {
		this.angle = angle;
	}

	/**
	 * Nastaví otočení kol auta
	 * 
	 * @param wheel
	 */
	public void setWheel(float wheel) {
		this.wheel = wheel;
	}

	@Override
	public int compareTo(Object o) {
		VisualCar c = (VisualCar) o;
		// if(position != c.position) {
		return position - c.position;
		/*
		 * } else { return number - c.number; }
		 */
	}

	@Override
	public String toString() {
		return driverName;
	}

	/**
	 * Nastaví vlastnost auta
	 * 
	 * @return
	 */
	public int getPosition() {
		return position;
	}

	/**
	 * Nastaví vlastnost auta
	 * 
	 * @return
	 */
	public int getLap() {
		return lap;
	}

	/**
	 * Získá počet projetých checkpointů
	 * 
	 * @return
	 */
	public int getCheckpoints() {
		return checkpoints;
	}

	public int getGear() {
		return gear;
	}

	public float getRpm() {
		return rpm;
	}

	public float getSpeed() {
		return speed;
	}

	/**
	 * Nastaví vlastnost auta
	 * 
	 * @return
	 */
	public float getClientDistance0() {
		return clientDistance0;
	}

	public float getClientDistance4() {
		return clientDistance4;
	}

	public float getClientDistance8() {
		return clientDistance8;
	}

	public float getClientDistance16() {
		return clientDistance16;
	}

	public float getClientDistance32() {
		return clientDistance32;
	}

	/**
	 * Nastaví vlastnost auta
	 * 
	 * @return
	 */
	public float getClientAngle() {
		return clientAngle;
	}

	/**
	 * Nastaví vlastnost auta
	 * 
	 * @return
	 */
	public float getClientSpeed() {
		return clientSpeed;
	}

	/**
	 * Nastaví vlastnost auta
	 * 
	 * @return
	 */
	public float getClientAcc() {
		return clientAcc;
	}

	/**
	 * Nastaví vlastnost auta
	 * 
	 * @return
	 */
	public float getClientWheel() {
		return clientWheel;
	}

	public float getClientFriction() {
		return clientFriction;
	}

	public float getClientSkid() {
		return clientSkid;
	}

	public float getClientCheckpoint() {
		return clientCheckpoint;
	}

//	public float getClientSensor1() {
//		return clientSensor1;
//	}
//
//	public float getClientSensor2() {
//		return clientSensor2;
//	}

	/*
	 * public int getNumber() { return number; }
	 */

	public float getSensorFrontMiddleLeft() {
		return sensorFrontMiddleLeft;
	}

	public float getSensorFrontMiddleRight() {
		return sensorFrontMiddleRight;
	}

	public float getSensorFrontLeft() {
		return sensorFrontLeft;
	}

	public float getSensorFrontRight() {
		return sensorFrontRight;
	}

	public float getSensorFrontRightCorner1() {
		return sensorFrontRightCorner1;
	}

	public float getSensorFrontRightCorner2() {
		return sensorFrontRightCorner2;
	}

	public float getSensorFrontLeftCorner1() {
		return sensorFrontLeftCorner1;
	}

	public float getSensorFrontLeftCorner2() {
		return sensorFrontLeftCorner2;
	}

	public float getSensorLeft1() {
		return sensorLeft1;
	}

	public float getSensorLeft2() {
		return sensorLeft2;
	}

	public float getSensorRight1() {
		return sensorRight1;
	}

	public float getSensorRight2() {
		return sensorRight2;
	}

	public float getSensorRearRightCorner1() {
		return sensorRearRightCorner1;
	}

	public float getSensorRearRightCorner2() {
		return sensorRearRightCorner2;
	}

	public float getSensorRearLeftCorner1() {
		return sensorRearLeftCorner1;
	}

	public float getSensorRearLeftCorner2() {
		return sensorRearLeftCorner2;
	}

	public float getSensorRearLeft() {
		return sensorRearLeft;
	}

	public float getSensorRearRight() {
		return sensorRearRight;
	}

	public String getCarType() {
		return carType;
	}

	public int getBestLap() {
		return bestLap;
	}

	public int getLapTime() {
		return lapTime;
	}

	public int getTime() {
		return time;
	}
}
