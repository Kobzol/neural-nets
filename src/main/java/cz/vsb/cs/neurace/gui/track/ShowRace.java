package cz.vsb.cs.neurace.gui.track;

import cz.vsb.cs.neurace.connection.ClientRequest;
import cz.vsb.cs.neurace.connection.ClientRequestException;
import cz.vsb.cs.neurace.gui.*;
import cz.vsb.cs.neurace.race.Race.Status;
import cz.vsb.cs.neurace.track.Track;
import java.awt.BorderLayout;
import java.io.IOException;
import java.util.*;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * 
 */
public class ShowRace extends Thread {

	/** název závodu */
	private String raceName;
	/** připojení na server */
	private ClientRequest request;
	/** panel se závodem a informacemi o něm */
	private JPanel panel;
	/** informační panel o závodě */
	private RaceInfoPanel infoPanel;
	/** vykreslení tratě */
	private RaceView raceView;
	/** status závodu */
	private Status status = Status.INITIATION;
	/** seznam RaceStatusListener */
	private List<RaceStatusListener> raceStatusListeners = new LinkedList<RaceStatusListener>();
	/** zda bylo odpojeni planovano */
	private boolean disconnect;

	private HashMap<String, Integer> champScore = new HashMap<String, Integer>();
	private String host;
	private int port;
	RaceResults results;
	private int laps = 1;
	private int races = 1;
	private int raceNumber = 1;

	private int startCars = 0;
	private List messages = new LinkedList<String>();

	/**
	 * Konstruktor
	 * 
	 * @param raceName
	 * @param host
	 * @param port
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public ShowRace(String raceName, String host, int port) throws IOException,
			ClientRequestException, ParserConfigurationException, SAXException,
			ClassNotFoundException {
		this.raceName = raceName;

		this.host = host;
		this.port = port;

		request = new ClientRequest("viewer", host, port);
		request.write("race", raceName);
		request.endHead();

		Track track = (new Track(request.getString("track"), host, port));

		raceView = new RaceView(track);

		if (request.getString("races") != null) {
			races = request.getInt("races");
			raceNumber = request.getInt("raceNumber");
			raceName = raceName + " (" + raceNumber + "/" + races + ")";
		}
		laps = request.getInt("laps");
		infoPanel = new RaceInfoPanel(raceName, laps, raceView);

		panel = new JPanel(new BorderLayout(10, 10));
		JScrollPane sc = new JScrollPane(raceView);

		panel.add(infoPanel, BorderLayout.LINE_START);
		panel.add(sc, BorderLayout.CENTER);

		new Thread(this).start();
	}

	@Override
	public void run() {
		try {
			readAndShow();
		} catch (Exception e) {
			if (!disconnect) {
				e.printStackTrace();
				MainWindow.get().errorMsg(e.getMessage());
			}
		}
	}

	/**
	 * Vloží nové auto do závodu.
	 * 
	 * @throws IOException
	 */
	private void newDriver() throws IOException {
		request.read("enddriver");
		VisualCar newCar = new VisualCar(request.getString("name"),
				request.getString("type"), raceView.getCars().size() + 1,
				raceView);
		newCar.setValue("x", request.getString("x"));
		newCar.setValue("y", request.getString("y"));
		newCar.setValue("angle", request.getString("angle"));
		newCar.setValue("wheel", request.getString("wheel"));
		newCar.setValue("speed", request.getString("speed"));
		newCar.setValue("acc", request.getString("acc"));
		if (request.getString("color") != null) {
			newCar.setValue("color", request.getString("color"));
		}
		raceView.addCar(newCar);
		raceView.repaint();
		infoPanel.updateCars(true);
	}

	/**
	 * Odstraní závodníka
	 * 
	 * @throws IOException
	 */
	private void remove() throws IOException {
		String name = request.readline();
		VisualCar carToRemove = null;
		for (VisualCar car : raceView.getCars()) {
			if (car.getDriver().equals(name)) {
				carToRemove = car;
				break;
			}
		}
		if (carToRemove != null) {
			raceView.getCars().remove(carToRemove);
		}

		raceView.repaint();
		infoPanel.updateCars(true);
		infoPanel.repaint();
	}

	/**
	 * Pro jedno kolo čte a nastavuje stav.
	 * 
	 * @throws IOException
	 */
	private void round() throws IOException {
		VisualCar car = null;
		int oldPosition = 0;
		boolean positionChanged = false;
		int number = 0;
		while (true) {
			String line = request.readline();
			if (line.equals("endround")) {
				break;
			}
			int pos = line.indexOf(':');
			if (pos == -1) {
				continue;
			}
			String key = line.substring(0, pos);
			String value = line.substring(pos + 1);
			if (key.equals("time")) {
				infoPanel.setTime(Integer.parseInt(value));
			} else if (key.equals("remaining")) {
				raceView.setRemainingTime(Float.parseFloat(value));
			} else if (key.equals("car")) {
				car = raceView.getCar(number++);
				if (car != null) {
					oldPosition = car.getPosition();
				}
			} else if (key.equals("position") && car != null) {
				int newPos = Integer.parseInt(value);
				car.setPosition(newPos);
				if (oldPosition != newPos) {
					positionChanged = true;
				}
			} else if (car != null) {
				car.setValue(key, value);
			}
		}
		raceView.update();
		infoPanel.updateCars(positionChanged);
	}

	/**
	 * Čte a zobrazuje probihající závod.
	 * 
	 * @throws IOException
	 */
	private void readAndShow() throws IOException {
		String str;
		while (true) {
			while (status == Status.INITIATION) {
				str = request.readline();
				if (str.equals("start")) {
					setStatus(Status.RACE);
					request.read();
					startCars = request.getInt("startCars");
				} else if (str.equals("change")) {
					changeTrack();
				} else if (str.equals("car")) {
					newDriver();
				} else if (str.equals("remove")) {
					remove();
				} else if (str.equals("restart")) {
					resetCars();
				} else if (str.equals("end")) {
					return;
				} else {
					System.err.println("waitForStart: unknown: '" + str + "'");
				}

			}
			while (status == Status.RACE || status == Status.FINISH) {
				str = request.readline();
				if (str.length() == 0) {
					continue;
				} else if (str.equals("round")) {
					round();
				} else if (str.equals("finish")) {
					setStatus(Status.FINISH);
					if (races > 1) {
						raceNumber++;
						getScores();
					}
					getMessages();
					showResults();
				} else if (str.equals("restart")) {
					resetCars();
					setStatus(Status.INITIATION);
				} else if (str.equals("change")) {
					changeTrack();
					setStatus(Status.INITIATION);
				} else if (str.equals("end")) {
					return;
				} else if (str.equals("car")) {
					newDriver();
				} else if (str.equals("remove")) {
					remove();
				} else {
					System.out.println("readAndShow: unknown: '" + str + "'");
				}
			}
		}
	}

	/**
	 * Odpojí se od závodu.
	 * 
	 * @throws IOException
	 */
	public void disconnect() throws IOException {
		disconnect = true;
		request.close();
	}

	/**
	 * Vrátí panel.
	 * 
	 * @return
	 */
	public JPanel getPanel() {
		return panel;
	}

	public void updateRaceName() {
		String newName = raceName;
		if (races > 1) {
			newName += " (" + raceNumber + "/" + races + ")";
		}
		this.infoPanel.setRaceName(newName);
	}

	public void showResults() {
		VisualCar[] cars = raceView.getCarArray();
		if (cars.length > 0) {
			Arrays.sort(cars);
			String[][] resultsData = new String[cars.length][6];
			int row = 0;
			for (VisualCar car : cars) {
				resultsData[row][0] = String.valueOf(car.getPosition());
				resultsData[row][1] = String.valueOf(car.getDriver());
				resultsData[row][2] = String.valueOf(car.getCarType());
				if (car.getLap() > laps) {
					resultsData[row][3] = TimeUtil.genTime(car.getTime());
				} else {
					resultsData[row][3] = Text.getString("dnf");
				}
				if (car.getBestLap() != Integer.MAX_VALUE) {
					resultsData[row][4] = TimeUtil.genTime(car.getBestLap());
				} else {
					resultsData[row][4] = "-";
				}

				int points = calcPoints(car, startCars);
				resultsData[row][5] = String.valueOf(points);

				row++;
			}

			if (races > 1) {
				String[][] champResults = new String[champScore.size()][3];
				List<ChampRecord> scoreList = new ArrayList<ChampRecord>(
						champScore.size());
				for (String key : champScore.keySet()) {
					scoreList.add(new ChampRecord(key, champScore.get(key)));
				}
				Collections.sort(scoreList);

				row = 0;
				for (ChampRecord r : scoreList) {
					champResults[row][0] = String.valueOf(row + 1);
					champResults[row][1] = r.driver;
					champResults[row][2] = String.valueOf(r.points);
					row++;
				}

				boolean lastRace = (this.raceNumber > this.races);
				this.results = new RaceResults(MainWindow.get(), true, lastRace);
				this.results.setData(resultsData);
				this.results.setChampData(champResults);
			} else {
				this.results = new RaceResults(MainWindow.get(), false, false);
				this.results.setData(resultsData);
			}

			if (startCars < 3) {
				messages.add(0, "not ranked");
			}

			this.results.setMessages(messages);
			this.results.setVisible(true);
		}

	}

	public int calcPoints(VisualCar car, int cars) {
		int points = 0;
		if (car.getLap() > this.laps) {
			int pos = car.getPosition();
			points = cars - pos;
			if (pos == 1) {
				points += 2;
			} else if (pos == 2) {
				points += 1;
			}
		}
		return points;
	}

	/**
	 * Vrátí všechna auta na start
	 */
	public void resetCars() throws IOException {
		/*
		 * List<VisualCar> removeCars = new LinkedList<VisualCar>();
		 * 
		 * for (VisualCar car : racePaint.getCars()) { if(!car.isConnected())
		 * removeCars.add(car); } if(!removeCars.isEmpty()) {
		 * racePaint.removeCars(removeCars); }
		 */
		startCars = raceView.getCars().size();

		for (VisualCar car : raceView.getCars()) {
			request.readline();
			request.read("enddriver");
			car.setValue("x", request.getString("x"));
			car.setValue("y", request.getString("y"));
			car.setValue("angle", request.getString("angle"));
			car.setValue("wheel", request.getString("wheel"));
			car.setValue("speed", request.getString("speed"));
			car.setValue("lap", request.getString("lap"));
			car.setValue("acc", request.getString("acc"));
			car.setValue("position", request.getString("position"));
			car.setValue("checkpoints", request.getString("checkpoints"));
			car.setValue("carTime", request.getString("carTime"));
			car.setValue("clientDistance", request.getString("clientDistance"));
			car.setValue("clientAngle", request.getString("clientAngle"));
			car.setValue("clientSpeed", request.getString("clientSpeed"));
			car.setValue("clientDistance2",
					request.getString("clientDistance2"));
			car.setValue("clientAcc", request.getString("clientAcc"));
			car.setValue("clientWheel", request.getString("clientWheel"));
			car.setValue("isSkiding", request.getString("isSkiding"));
		}
		// this.racePaint.setSelectedCar(infoPanel.getSelectedCar());
		infoPanel.setTime(0);
		infoPanel.updateCars(true);
		if (raceNumber > this.races) {
			raceNumber = 1;
		}
		updateRaceName();
		raceView.setRemainingTime(-1);
		raceView.repaint();
	}

	public void changeTrack() throws IOException {
		String name = request.readline();
		laps = Integer.parseInt(request.readline());
		int r = Integer.parseInt(request.readline());

		if (raceNumber > this.races) {
			raceNumber = 1;
		}
		this.races = r;

		updateRaceName();
		try {
			Track track = new Track(name, host, port);
			raceView.setTrack(track);
			infoPanel.setLaps(laps);
			resetCars();
			raceView.repaint();
		} catch (Exception e) {
			System.err.printf(e.getMessage());
			// e.printStackTrace();
		}
	}

	public void getScores() throws IOException {
		champScore.clear();
		String str;
		try {
			while ((str = request.readline()).length() != 0) {
				String[] split = str.split(" ");
				champScore.put(split[0], Integer.valueOf(split[1]));
			}
		} catch (Exception e) {
			System.err.printf(e.getMessage());
			// e.printStackTrace();
		}
	}

	public void getMessages() throws IOException {
		messages.clear();
		try {
			String str;
			while ((str = request.readline()).length() != 0) {
				messages.add(str);
			}
		} catch (Exception e) {
			System.err.printf(e.getMessage());
			// e.printStackTrace();
		}
	}

	/**
	 * Požadavek na změnu trati trati.
	 */
	public void changeRequest(String trackName, int laps, int races,
			boolean collisions) {
		try {
			ClientRequest changeRequest = new ClientRequest("changetrack",
					Config.get().getString("host"), Config.get().getInt("port"));
			changeRequest.write("race", this.raceName);
			changeRequest.write("track", trackName);
			changeRequest.write("laps", laps);
			changeRequest.write("races", races);
			changeRequest.write("collisions", collisions);
			changeRequest.send();
			changeRequest.check(true);
			changeRequest.close();

		} catch (Exception ex) {
			// e.printStackTrace();
			MainWindow.get().errorMsg(ex.getLocalizedMessage());
		}
	}

	/**
	 * Odstartuje závod
	 * 
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	public void startRace() throws IOException, ClientRequestException {
		ClientRequest requestStart = new ClientRequest("racestart", Config
				.get().getString("host"), Config.get().getInt("port"));
		requestStart.write("race", raceName);
		requestStart.send();
		requestStart.check(true);
		requestStart.close();
	}

	/**
	 * Zastaví závod.
	 * 
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	public void stopRace() throws IOException, ClientRequestException {
		ClientRequest requestStop = new ClientRequest("racestop", Config.get()
				.getString("host"), Config.get().getInt("port"));
		requestStop.write("race", raceName);
		requestStop.send();
		requestStop.check(true);
		requestStop.close();
	}

	/**
	 * Pozastaví závod
	 * 
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	public void pauseRace() throws IOException, ClientRequestException {
		ClientRequest requestPause = new ClientRequest("racepause", Config
				.get().getString("host"), Config.get().getInt("port"));
		requestPause.write("race", raceName);
		requestPause.send();
		requestPause.check(true);
		requestPause.close();

	}

	/**
	 * Krokuje závod.
	 * 
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	public void stepRace() throws IOException, ClientRequestException {
		ClientRequest requestStep = new ClientRequest("racestep", Config.get()
				.getString("host"), Config.get().getInt("port"));
		requestStep.write("race", raceName);
		requestStep.send();
		requestStep.check(true);
		requestStep.close();

	}

	/**
	 * Restartuje závod.
	 * 
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	public void restartRace() throws IOException, ClientRequestException {
		ClientRequest requestRestart = new ClientRequest("racerestart", Config
				.get().getString("host"), Config.get().getInt("port"));
		requestRestart.write("race", raceName);
		requestRestart.send();
		requestRestart.check(true);
		requestRestart.close();
	}

	public void followCar(boolean value) {
		this.raceView.setFollowCar(value);
	}

	public void setSelectedCar(VisualCar car) {
		this.raceView.setSelectedCar(car);
	}

	public RaceView getRacePaint() {
		return raceView;
	}

	/**
	 * Vrátí status závodu.
	 * 
	 * @return
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Přidá RaceStatusListener
	 * 
	 * @param raceStatusListener
	 */
	public void addRaceStatusListener(RaceStatusListener raceStatusListener) {
		raceStatusListeners.add(raceStatusListener);
	}

	/**
	 * Odstraní RaceStatusListener
	 * 
	 * @param raceStatusListener
	 */
	public void removeRaceStatusListener(RaceStatusListener raceStatusListener) {
		raceStatusListeners.remove(raceStatusListener);
	}

	/**
	 * Nastaví nový status závodu a oznámí to listenerům.
	 * 
	 * @param s
	 */
	private void setStatus(Status s) {
		status = s;
		for (RaceStatusListener l : raceStatusListeners) {
			l.changeRaceStatus(status);
		}
	}

	private static class ChampRecord implements Comparable {
		private String driver;
		private int points;

		public ChampRecord(String driver, int points) {
			this.driver = driver;
			this.points = points;
		}

		@Override
		public int compareTo(Object o) {
			ChampRecord other = (ChampRecord) o;
			if (other.points > this.points) {
				return 1;
			} else if (other.points < this.points) {
				return -1;
			} else {
				return 0;
			}
		}

	}
}
