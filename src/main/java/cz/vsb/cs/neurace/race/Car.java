package cz.vsb.cs.neurace.race;

import cz.vsb.cs.neurace.connection.ClientSocket;
import cz.vsb.cs.neurace.track.Surface;

import java.io.IOException;
import java.util.HashMap;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 * 
 * Auto. Zajišťuje data pro klienty, počítá stav na trati.
 * 
 */
public class Car implements Comparable {

	/** Barva auta, format rgb */
	private String color;
	/** Vzdalenost od cary, kde končí trať (1/2 šířky tratě) [m] */
	private float distanceLimit;
	/** Vzdalenost od cary [m] */
//	private float distance, distance2;
	/** Uhel k trati */
	private float angleLine;
	/** pořadí v závodě */
	private int position;
	/** typ auta */
	private String carType;
	/** řidič auta */
	private Driver driver;
	/** závod */
	private Race race;
	/** číslo auta v závodě */
	// private int number;
	/** kolo, které zrovna auto jede */
	private int lap;
	/** čas v tomto kole */
	private int lapTime;
	/** nejlepší čas na kolo */
	private int bestLapTime = Integer.MAX_VALUE;
	/** kontrolní bod ke kterému jede */
	private Checkpoint nextCheckpoint;
	/** počet projetých checkpointů */
	private int checkpoints = 0;
	/** cas projeti checkpointu */
	private int checkPointTime = 0;
	/** nejbližší část tratě */
	private RaceLine nearestLine;
	/** zda je konec pro auto */
	private boolean finish = false;
	/** zrychlení od klienta */
	private float clientAcc = 0.5f;
	/** točení od klienta */
	private float clientWheel = 0.5f;
	/** vzdálenost od čáry pro klienta */
	private float clientDistance0 = 0.5f;
	/** vzdálenost od čáry pro klienta 4m*/
	private float clientDistance4 = 0.5f;
	/** vzdálenost od čáry pro klienta 8m*/
	private float clientDistance8 = 0.5f;
	/** vzdálenost od čáry pro klienta 16m*/
	private float clientDistance16 = 0.5f;
	/** vzdálenost od čáry pro klienta 32m*/
	private float clientDistance32 = 0.5f;
	/** rychlost auta pro klienta */
	private float clientSpeed = 0.5f;
	/** úhel od čáry pro klienta */
	private float clientAngle = 0.5f;
	/** tření mezi pneumatikou a vozovkou */
	private float clientFriction = 1.0f;
	/** určuje jestli je auto ve smyku */
	private float clientSkid = 1.0f;
	/** směr k dalšímu checkpointu */
	private float clientCheckpoint = 0.5f;
	/** čas od startu do cíle */
	private int time;
	/** maximální úhel zatáčení */
	private float maxWheel = (float) (Math.PI / 3);
	/**
	 * Souřadnice středu auta
	 */
	private float sx, sy;
	/**
	 * Souřadnice přední části auta
	 */
	private float rx, ry;
	/** délka auta */
	private float carLength;
	private float angle;
	private float wheel;
	private float speed;
	private float maxSpeed;

	private PhysicalCar physical;

//	private float clientSensor1 = 0.5f;
//	private float clientSensor2 = 0.5f;
	
	private HashMap<String, Float> sensors = new HashMap<String, Float>(18);
	
//	private float sensorFrontMiddleLeft = 0f;
//	private float sensorFrontMiddleRight = 0f;
//	private float sensorFrontLeft = 0f;
//	private float sensorFrontRight = 0f;
//	private float sensorFrontRightCorner1 = 0f;
//	private float sensorFrontRightCorner2 = 0f;
//	private float sensorFrontLeftCorner1 = 0f;
//	private float sensorFrontLeftCorner2 = 0f;
//	private float sensorLeft1 = 0f;
//	private float sensorLeft2 = 0f;
//	private float sensorRight1 = 0f;
//	private float sensorRigth2 = 0f;
//	private float sensorRearRightCorner1 = 0f;
//	private float sensorRearRightCorner2 = 0f;
//	private float sensorRearLeftCorner1 = 0f;
//	private float sensorRearLeftCorner2 = 0f;
//	private float sensorRearLeft = 0f;
//	private float sensorRearRight = 0f;
	
	private boolean disconnected = false;

	/**
	 * Konstruktor auta.
	 * 
	 * @param race
	 *            závod
	 * @param socket
	 *            klient - řidič
	 * @param number
	 *            číslo auta v závodě
	 * @param lock
	 *            zámek pro synchronizaci kominikace
	 * @throws IOException
	 */
	public Car(String carType, Race race, ClientSocket socket, int position,
			Lock lock, PhysicalCar physical) throws IOException {
		this.carType = carType;
		this.race = race;
		this.position = position;
		this.color = socket.getString("color");
		this.physical = physical;
		this.maxSpeed = physical.getMaxSpeed();
		this.carLength = physical.getCarLength();
		this.nextCheckpoint = this.race.getTrack().getTrack().getCheckpoints()[0];
		this.distanceLimit = this.race.getTrack().getTrack().getRoadWidth() / 2;
		driver = new Driver(socket, this, lock);
	}

	/**
	 * Vrátí řidiče.
	 * 
	 * @return klient - řidič
	 */
	public Driver getDriver() {
		return driver;
	}

	public String getColor() {
		return color;
	}

	/**
	 * Nastaví řízení auta
	 */
	public void setControls() {
		float acc = 2 * (check(clientAcc) - 0.5f);
		if (acc < 0.0f && this.speed > 1) {
			// brzda
			physical.setBrake(-acc);
		} else {
			// plyn
			physical.setThrottle(acc);
		}

		physical.setSteering(-((check(clientWheel) - 0.5f) * 2 * maxWheel));

		if (finish) {
			physical.setBrake(0.5f);
		}
	}

	/**
	 * Po posunu aut odečte hodnoty parametrů auta z fyzikálního modelu a
	 * přepočítá je na vstupy sítě.
	 * 
	 * @param dt
	 *            časový úsek, který uplynul během posunu.
	 */
	public void updateValues(float dt) {
		if (!finish) {
			this.time += dt;
			this.lapTime += dt;
		}

		float last_rx = rx;
		float last_ry = ry;
		speed = physical.getSpeedKmh();

		// nova pozice s
		sx = physical.getX();
		sy = physical.getZ();
		wheel = -physical.getSteering();

		angle = -physical.getAngle() + (float) Math.PI / 2;

		// nova pozice r
		this.rx = sx + (float) Math.cos(angle) * (carLength / 2);
		this.ry = sy + (float) Math.sin(angle) * (carLength / 2);

		nearestLine = race.getTrack().getNearestLine(rx, ry, nearestLine);

//		float l = speed / 2;
//		float x2 = rx + (float) Math.cos(angle) * l;
//		float y2 = ry + (float) Math.sin(angle) * l;
//		RaceLine line2 = race.getTrack().getNearestLine(x2, y2, nearestLine);

		float cx = this.nextCheckpoint.getX();
		float cy = this.nextCheckpoint.getY();
		Vector2f direction = new Vector2f(rx - sx, ry - sy);
		Vector2f checkpoint = new Vector2f(cx - sx, cy - sy);
		float cAngle = direction.angle(checkpoint);
		if (direction.x * checkpoint.y - direction.y * checkpoint.x < 0) {
			cAngle = -cAngle;
		}
		clientCheckpoint = cAngle / (float) (2 * Math.PI) + 0.5f;

		float distance = nearestLine.getDistance(rx, ry);
		clientDistance0 = (distance / distanceLimit) / 2 + 0.5f;
		clientDistance0 = check(clientDistance0);

		clientFriction = calculateFriction(nearestLine, Math.abs(distance));
		physical.setFriction(clientFriction);
		clientFriction = (clientFriction > 1.0f) ? 1.0f : clientFriction;

		clientSkid = (physical.getSkidInfo(0) + physical.getSkidInfo(1)
				+ physical.getSkidInfo(2) + physical.getSkidInfo(3)) / 4;

		clientDistance4 = getDistanceFromRaceLine(angle, rx, ry, nearestLine, 4.0f);
		clientDistance8 = getDistanceFromRaceLine(angle, rx, ry, nearestLine, 8.0f);
		clientDistance16 = getDistanceFromRaceLine(angle, rx, ry, nearestLine, 16.0f);
		clientDistance32 = getDistanceFromRaceLine(angle, rx, ry, nearestLine, 32.0f);
//		distance2 = line2.getDistance(x2, y2);
//		clientDistance2 = (distance2 / distanceLimit) / 2 + 0.5f;
//		clientDistance2 = check(clientDistance2);

		angleLine = angle - nearestLine.getTheta();
		if (angleLine > Math.PI) {
			angleLine -= 2 * Math.PI;
		} else if (angleLine < -Math.PI) {
			angleLine += 2 * Math.PI;
		}

		clientAngle = (float) (angleLine / (Math.PI * 2) + 0.5);

		clientSpeed = speed / (2 * maxSpeed) + 0.5f;

		if (nextCheckpoint.check(last_rx, last_ry, rx, ry)) {
			if (nextCheckpoint.isFirst()) {
				lap++;
				if (lap > 1 && lapTime < bestLapTime) {
					bestLapTime = lapTime;
					lapTime = 0;
				}
				if (lap > race.getLaps()) {
					finish = true;
					race.setFirstFinished(true);
				}
			}
			checkpoints++;
			checkPointTime = race.getTime();
			int index = nextCheckpoint.getIndex();
			nextCheckpoint = this.race.getTrack().getNextCheckpoint(index);
		}

		physical.detect(sensors);
//		for(String key : sensors.keySet()){
//			System.out.print(String.format("%s:%1.2f  ", key, sensors.get(key)));
//		}
//		System.out.println();
//		clientSensor1 = 0.5f;
//		clientSensor2 = 0.5f;
//		if (sensors.get("Front_1") > 0) {
//			clientSensor1 = 0;
//		}
//		if (sensors.get("Front_2") > 0) {
//			clientSensor1 = 0.25f;
//		}
//		if (sensors.get("Front_3") > 0) {
//			clientSensor1 = 1;
//		}
//		if (sensors.get("Front_4") > 0) {
//			clientSensor1 = 1;
//		}
		// Vector3f obstaclePos;
		// obstaclePos = physical.detect(2.0f);
		// if(obstaclePos != null) {
		// float obstX = obstaclePos.x;
		// float obstY = obstaclePos.z;
		// RaceLine line3 = race.getTrack().getNearestLine(obstX, obstY,
		// nearestLine);
		// float obstDistance = line3.getDistance(obstX, obstY);
		// //float obstDistance = getDistance(sx, sy, rx, ry, obstX, obstY);
		//
		// if(obstDistance > 0) {
		// clientSensor1 = 1.0f;
		// } else if (obstDistance < 0f) {
		// clientSensor1 = 0.0f;
		// }
		// }
		// else {
		// clientSensor1 = 0.5f;
		// }
		// obstaclePos = physical.detect(4.0f);
		// if(obstaclePos != null) {
		// float obstX = obstaclePos.x;
		// float obstY = obstaclePos.z;
		// RaceLine line3 = race.getTrack().getNearestLine(obstX, obstY,
		// nearestLine);
		// float obstDistance = line3.getDistance(obstX, obstY);
		// //float obstDistance = getDistance(sx, sy, rx, ry, obstX, obstY);
		//
		// if(obstDistance > 0) {
		// clientSensor2 = 1.0f;
		// } else if (obstDistance < 0f) {
		// clientSensor2 = 0.0f;
		// }
		// }
		// else {
		// clientSensor2 = 0.5f;
		// }
	} // updateValues

	private float getDistanceFromRaceLine(float angle, float x, float y, RaceLine nearestLine, float distance){
		float newX = x + (float) Math.cos(angle) * distance;
		float newY = y + (float) Math.sin(angle) * distance;
		RaceLine line2 = race.getTrack().getNearestLine(newX, newY, nearestLine);
		float distance2 = line2.getDistance(newX, newY);
		return check((distance2 / distanceLimit) / 2 + 0.5f);
	}
	/**
	 * Zjistí tření mezi autem a vozovkou
	 * 
	 * @param line
	 *            nejbližší úsečka
	 * @param distance
	 *            vzdálenost od nejbližší úsečky
	 */
	private float calculateFriction(RaceLine line, float distance) {
		if (distance < distanceLimit) {
			if (line.getPrevPoint().getSurface() == Surface.ASPHALT) {
				return line.getFriction() + physical.getAsphaltBonus();
			} else {
				return line.getFriction() + physical.getOffroadBonus();
			}
		} else {
			return this.race.getTrack().getTrack().getEnviroment()
					.getFriction()
					+ physical.getOffroadBonus();
		}
	}

	/**
	 * Vzdálenost bodu x,y od úsečky definované body [x1,y1] a [x2, y2].
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public float getDistance(float x1, float y1, float x2, float y2, float x,
			float y) {
		float length = length(x1, y1, x2, y2);
		float sin = (y2 - y1) / length;
		float cos = (x2 - x1) / length;
		float xo = (x - x1) * cos + (y - y1) * sin;
		float yo = -(x - x1) * sin + (y - y1) * cos;
		if (xo < 0) {
			return (float) (length(0, 0, xo, yo) * (yo < 0 ? -1 : 1));
		} else if (xo > length) {
			return (float) (length(length, 0, xo, yo) * (yo < 0 ? -1 : 1));
		} else {
			return (float) yo;
		}
	}

	private float length(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	/**
	 * Posun auta - původní
	 * 
	 * @param time
	 *            čas, který uplynul během posunu.
	 */
	/*
	 * public void move(float time) { float lx = rx; float ly = ry; float newAcc
	 * = (check(clientAcc) - 0.5f) * 2 * maxAcc; float newWheel =
	 * (check(clientWheel) - 0.5f) * 2 * maxWheel; boolean out =
	 * Math.abs(distance) > distanceLimit * 1.5f;
	 * 
	 * if (finish) { newAcc = 0f; } else { this.time += time * 1000; }
	 * 
	 * //super.move(time, newAcc, newWheel, out);
	 * 
	 * nearestLine = race.getTrack().getNearestLine(rx, ry, nearestLine);
	 * distance = nearestLine.getDistance(rx, ry); clientDistance = distance /
	 * distanceLimit + 0.5f; if (clientDistance > 1) { clientDistance = 1; }
	 * else if (clientDistance < 0) { clientDistance = 0; }
	 * 
	 * float x2 = rx + (float) Math.cos(angle) * speed; float y2 = ry + (float)
	 * Math.sin(angle) * speed; RaceLine line2 =
	 * race.getTrack().getNearestLine(x2, y2, nearestLine); distance2 =
	 * line2.getDistance(x2, y2); clientDistance2 = distance2 / distanceLimit +
	 * 0.5f; if (clientDistance2 > 1) { clientDistance2 = 1; } else if
	 * (clientDistance2 < 0) { clientDistance2 = 0; }
	 * 
	 * angleLine = angle - nearestLine.getTheta(); if (angleLine > Math.PI) {
	 * angleLine -= 2 * Math.PI; } else if (angleLine < -Math.PI) { angleLine +=
	 * 2 * Math.PI; } if (angleLine > Math.PI) { angleLine -= 2 * Math.PI; }
	 * else if (angleLine < -Math.PI) { angleLine += 2 * Math.PI; }
	 * 
	 * clientAngle = (float) (angleLine / (Math.PI * 2) + 0.5);
	 * 
	 * clientSpeed = speed / (2 * maxSpeed) + 0.5f;
	 * 
	 * Checkpoint cp = race.getTrack().getCheckPoint(lx, ly, rx, ry,
	 * lastCheckPoint); if (cp != null) { if (cp.isFirst()) { lap++; if
	 * (race.isFinishLap()) { finish = true; } else if (lap > race.getLaps()) {
	 * race.setFinishLap(); finish = true; } } if (!finish) { checkpoints++;
	 * checkPointTime = race.getTime(); lastCheckPoint = cp; } }
	 * 
	 * }
	 */

	/**
	 * Vrátí číslo auta.
	 * 
	 * @return číslo auta.
	 */
	/*
	 * public int getNumber() { return number; }
	 */

	/**
	 * Změní číslo auta
	 * 
	 * @param number
	 *            číslo auta.
	 */
	/*
	 * public void setNumber(int number) { this.number = number; }
	 */

	/**
	 * Zapíše vlastnosti do socketu
	 * 
	 * @param socket
	 *            kam má se informace zapsat.
	 * @throws IOException
	 */
	public void driverWrite(ClientSocket socket) throws IOException {
		socket.write("distance0", clientDistance0);
		socket.write("angle", clientAngle);
		socket.write("speed", clientSpeed);
		socket.write("distance4", clientDistance4);
		socket.write("distance8", clientDistance8);
		socket.write("distance16", clientDistance16);
		socket.write("distance32", clientDistance32);

		socket.write("friction", clientFriction);
		socket.write("skid", clientSkid);
		socket.write("checkpoint", clientCheckpoint);
//		socket.write("sensor1", clientSensor1);
//		socket.write("sensor2", clientSensor2);
		for(String key : sensors.keySet()){
			socket.write(key, sensors.get(key));
		}
	}

	/**
	 * Nastavení vlastností.
	 * 
	 * @param atr
	 *            název vlastnosti
	 * @param value
	 *            hodtota vlastnosti
	 * @return pokud není chyba, pak null, jinak řetězec s popisem chyby.
	 */
	public String set(String atr, String value) {
		try {
			if (atr.equals("acc")) {
				clientAcc = Float.parseFloat(value);
			} else if (atr.equals("wheel")) {
				clientWheel = Float.parseFloat(value);
			} else {
				return ("Car: unknown attribute '" + atr + "'");
			}
		} catch (NumberFormatException e) {
			return ("Car: value '" + value + "' not float for attribute'" + atr + "'");
		}
		return null;
	}

	/**
	 * Vrátí auto na start závodu.
	 * 
	 */
	public void reset() {
//		distance = 0f;
//		distance2 = 0f;
		angleLine = 0f;
		lap = 0;
		checkpoints = 0;
		checkPointTime = 0;

		nearestLine = null;
		finish = false;
		clientAcc = 0.5f;
		clientWheel = 0.5f;
		clientDistance0 = 0.5f;
		clientDistance4 = 0.5f;
		clientDistance8 = 0.5f;
		clientDistance16 = 0.5f;
		clientDistance32 = 0.5f;
		clientSpeed = 0.5f;
		clientAngle = 0.5f;
		time = 0;
		lapTime = 0;
		bestLapTime = Integer.MAX_VALUE;

		Checkpoint[] checkpts = race.getTrack().getCheckpoints();
		if (checkpts != null && checkpts.length > 1) {
			nextCheckpoint = checkpts[0];
			RaceLine line = this.race.getTrack().calculateStartLine(
					this.position - 1, race.hasCollisions());
			Point2f p = this.race.getTrack().calculateStartPos(
					this.position - 1, race.hasCollisions());
			this.physical.resetCar(p.x, 0, p.y, -line.getTheta()
					+ (float) Math.PI / 2);
			this.distanceLimit = this.race.getTrack().getTrack().getRoadWidth() / 2;
			this.updateValues(0f);
		}
	}

	public void setDistanceLimit(float distanceLimit) {
		this.distanceLimit = distanceLimit;
	}

	/**
	 * Vrátí hodnotu podle toho, zda auto ukončilo závod.
	 * 
	 * @return true, pokud auto ukončilo závod jinak false
	 */
	public boolean isFinish() {
		return finish && Math.abs(physical.getSpeedKmh()) < 1.0f;
	}

	/**
	 * Vrátí hodnotu podle toho, zda auto ukončilo závod.
	 * 
	 * @return true, pokud auto ukončilo závod a stoji, jinak false
	 */
	public boolean isStoped() {
		return finish && Math.abs(physical.getSpeedKmh()) < 1.0f;
	}

	/**
	 * Kontroluje a upravuje vstupni hodnoty, ktere musi být v rozmezí 0 až 1.
	 * 
	 * @param value
	 *            hodnota
	 * @return opravena hodnota
	 */
	private float check(float value) {
		if (value < 0) {
			return 0;
		}
		if (value > 1) {
			return 1;
		}
		return value;
	}

	/**
	 * Vygeneruje řetězec s popisem stavu auta.
	 * 
	 * @return popis stavu auta.
	 */
	public String getInfo() {
		StringBuffer sb = new StringBuffer();
		sb.append("car\n");
		sb.append("name:");
		sb.append(driver.getDriverName());
		sb.append("\n");
		sb.append("type:");
		sb.append(carType);
		sb.append("\n");
		// sb.append("number:"); sb.append(number); sb.append("\n");
		if (color != null) {
			sb.append("color:");
			sb.append(color);
			sb.append("\n");
		}
		getInfo(sb);
		sb.append("enddriver\n");
		return sb.toString();
	}

	/**
	 * Zasiše popis auta do bufferu.
	 * 
	 * @param sb
	 *            buffer pro zápis
	 */
	public void getInfo(StringBuffer sb) {
		// info
		setInfo(sb, "car", driver.getName());
		setInfo(sb, "x", sx);
		setInfo(sb, "y", sy);
		setInfo(sb, "angle", angle);
		setInfo(sb, "wheel", wheel);
		setInfo(sb, "lap", lap);
		setInfo(sb, "position", position);
		setInfo(sb, "checkpoints", checkpoints);
		setInfo(sb, "carTime", time);
		setInfo(sb, "lapTime", lapTime);
		setInfo(sb, "bestLap", bestLapTime);
		setInfo(sb, "speed", physical.getSpeedKmh());
		setInfo(sb, "gear", physical.getGear());
		setInfo(sb, "rpm", physical.getRpm());
		// client
		setInfo(sb, "clientDistance0", clientDistance0);
		setInfo(sb, "clientAngle", clientAngle);
		setInfo(sb, "clientSpeed", clientSpeed);
		setInfo(sb, "clientDistance4", clientDistance4);
		setInfo(sb, "clientDistance8", clientDistance8);
		setInfo(sb, "clientDistance16", clientDistance16);
		setInfo(sb, "clientDistance32", clientDistance32);
		setInfo(sb, "clientAcc", clientAcc);
		setInfo(sb, "clientWheel", clientWheel);
		setInfo(sb, "skidInfo0", physical.getSkidInfo(0));
		setInfo(sb, "skidInfo1", physical.getSkidInfo(1));
		setInfo(sb, "skidInfo2", physical.getSkidInfo(2));
		setInfo(sb, "skidInfo3", physical.getSkidInfo(3));
		setInfo(sb, "clientSkid", clientSkid);
		setInfo(sb, "clientFriction", clientFriction);
		setInfo(sb, "clientCheckpoint", clientCheckpoint);
//		setInfo(sb, "clientSensor1", clientSensor1);
//		setInfo(sb, "clientSensor2", clientSensor2);
		for(String key : sensors.keySet()){
			setInfo(sb, key, sensors.get(key));
		}
	}

	/**
	 * Zapiše hodnotu do bufferu,
	 * 
	 * @param sb
	 *            buffer
	 * @param prop
	 *            vlastnost
	 * @param value
	 *            hodnota
	 */
	private void setInfo(StringBuffer sb, String prop, int value) {
		sb.append(prop);
		sb.append(':');
		sb.append(value);
		sb.append('\n');
	}

	/**
	 * Zapiše hodnotu do bufferu,
	 * 
	 * @param sb
	 *            buffer
	 * @param prop
	 *            vlastnost
	 * @param value
	 *            hodnota
	 */
	private void setInfo(StringBuffer sb, String prop, float value) {
		sb.append(prop);
		sb.append(':');
		sb.append(value);
		sb.append('\n');
	}

	private void setInfo(StringBuffer sb, String prop, String value) {
		sb.append(prop);
		sb.append(':');
		sb.append(value);
		sb.append('\n');
	}

	private void setInfo(StringBuffer sb, String prop, boolean value) {
		sb.append(prop);
		sb.append(':');
		sb.append(value);
		sb.append('\n');
	}

	public String getCarType() {
		return carType;
	}

	/**
	 * vzdálenost od čáry pro klienta
	 * 
	 * @return vzdálenost od čáry pro klienta
	 */
	public float getClientDistance0() {
		return clientDistance0;
	}

	/**
	 * vzdálenost od čáry pro klienta za časový úsek
	 * 
	 * @return vzdálenost od čáry pro klienta za časový úsek
	 */
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
	 * rychlost auta pro klienta
	 * 
	 * @return rychlost auta pro klienta
	 */
	public float getClientSpeed() {
		return clientSpeed;
	}

	/**
	 * zrychlení od klienta
	 * 
	 * @return zrychlení od klienta
	 */
	public float getClientAcc() {
		return clientAcc;
	}

	/**
	 * točení od klienta
	 * 
	 * @return točení od klienta
	 */
	public float getClientWheel() {
		return clientWheel;
	}

	/**
	 * počet projetých checkpointů
	 * 
	 * @return počet projetých checkpointů
	 */
	public int getCheckPointCount() {
		return checkpoints;
	}

	/**
	 * úhel od čáry pro klienta
	 * 
	 * @return úhel od čáry pro klienta
	 */
	public float getClientAngle() {
		return clientAngle;
	}

	/**
	 * čas od startu do cíle
	 * 
	 * @return čas od startu do cíle
	 */
	public int getTime() {
		return time;
	}

	public int getPosition() {
		return position;
	}

	public boolean isDisconnected() {
		return disconnected;
	}

	public void setDisconnected(boolean disconnected) {
		this.disconnected = disconnected;
	}

	/**
	 * Nastavení pořadí v závodě.
	 * 
	 * @param p
	 *            nové pořadí
	 */
	public void setPosition(int p) {
		position = p;
	}

	@Override
	public int compareTo(Object o) {
		Car c = (Car) o;
		if (checkpoints != c.checkpoints) {
			return c.checkpoints - checkpoints;
		} else if (checkPointTime != c.checkPointTime) {
			return checkPointTime - c.checkPointTime;
		} else {
			return position - c.position;
		}
	}

	public int getLapTime() {
		return lapTime;
	}

	public int getBestLapTime() {
		return bestLapTime;
	}

	public float getAngle() {
		return angle;
	}

	public float getRX() {
		return rx;
	}

	public float getRY() {
		return ry;
	}

	public float getSX() {
		return sx;
	}

	public float getSY() {
		return sy;
	}

	public float getWheel() {
		return wheel;
	}

	public int getLap() {
		return lap;
	}

	public float getSpeed() {
		return speed;
	}

	public float getClientCheckpoint() {
		return clientCheckpoint;
	}

	public float getClientFriction() {
		return clientFriction;
	}

//	public float getClientSensor1() {
//		return clientSensor1;
//	}
//
//	public float getClientSensor2() {
//		return clientSensor2;
//	}

	public HashMap<String, Float> getSensors(){
		return sensors;
	}
	
	public float getRpm() {
		return physical.getRpm();
	}

	public int getGear() {
		return physical.getGear();
	}

	public boolean isSkidding(int wheel) {
		return physical.isSkidding(wheel);
	}

	public float getSkidInfo(int wheel) {
		return physical.getSkidInfo(wheel);
	}

	public float getClientSkid() {
		return clientSkid;
	}

	public PhysicalCar getPhysical() {
		return physical;
	}

}
