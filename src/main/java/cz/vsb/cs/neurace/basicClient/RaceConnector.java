package cz.vsb.cs.neurace.basicClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class RaceConnector {

	private String serverName;
	private int port;
	private DriverInterface driver;
	private boolean stop = false;
	
	public RaceConnector() {
		super();
	}
	public RaceConnector(String serverName, int port, DriverInterface driver) {
		super();
		this.serverName = serverName;
		this.port = port;
		this.driver = driver;
	}
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public DriverInterface getDriver() {
		return driver;
	}
	public void setDriver(DriverInterface driver) {
		this.driver = driver;
	}

	public void stop(){
		stop = true;
		try {
			socket.close();
		} catch (IOException e) {}
	}
	private Socket socket; // spojeni
	private BufferedReader in; // cteni se serveru
	private BufferedWriter out; // zapis na server

	/**
	 * Pripoji se k zavodu.
	 * 
	 * @param host zavodni server
	 * @param port port serveru
	 * @param raceName nazev zavodu, do nehoz se chce klient pripojit
	 * @param driverName jmeno ridice
	 * @throws java.lang.IOException  problem s pripojenim
	 */
	public void start(String raceName, String driverName, String carType) throws IOException  {
		// pripojeni k serveru
		socket = new Socket(serverName, port);
		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));

		// pripojeni k zavodu
		out.write("driver\n");                     // specifikace protokolu
		out.write("race:" + raceName + "\n");      // nazev zavodu
		out.write("driver:" + driverName + "\n");  // jmeno ridice
		out.write("color:0000FF\n");               // barva auta
		if(carType != null){
			out.write("car:" + carType + "\n");  // jmeno ridice
		}
		out.write("\n");
		out.flush();

		// precteni a kontrola dopovedi serveru
		String line = in.readLine();
		if (!line.equals("ok")) {
			// pokud se pripojeni nepodari, je oznamena chyba a vyvolana vyjimka
			System.err.println("Chyba: " + line);
			throw new ConnectException(line);
		}
		in.readLine();  // precteni prazdneho radku
		run();
	}

	public List<String> listRaces() throws IOException  {
		List<String> raceList = new ArrayList<String>();
		Socket socket = null;
		try{
			socket = new Socket(serverName, port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			// pripojeni k zavodu
			out.write("racelist\n");                     // specifikace protokolu
			out.write("\n");
			out.flush();

			// precteni a kontrola dopovedi serveru
			String line = in.readLine();
			if (!line.equals("ok")) {
				// pokud se pripojeni nepodari, je oznamena chyba a vyvolana vyjimka
				System.err.println("Chyba: " + line);
				throw new SocketException(line);
			}
			line = in.readLine();  // precteni prazdneho radku
			line = in.readLine();
			while(line != null && !"".equals(line)){
				raceList.add(line);
				line = in.readLine();
			}
			return raceList;
		} finally {
			if(socket != null){
				try {
					socket.close();
				} catch (Exception e) {}
			}
		}
	}
	
	public List<String> listCars(String raceName) throws IOException  {
		List<String> carList = new ArrayList<String>();
		Socket socket = null;
		try{
			socket = new Socket(serverName, port);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			// pripojeni k zavodu
			out.write("carlist\n");                     // specifikace protokolu
			out.write("race:" + raceName + "\n");
			out.write("\n");
			out.flush();

			// precteni a kontrola dopovedi serveru
			String line = in.readLine();
			if (!line.equals("ok")) {
				// pokud se pripojeni nepodari, je oznamena chyba a vyvolana vyjimka
				System.err.println("Chyba: " + line);
				throw new SocketException(line);
			}
			line = in.readLine();  // precteni prazdneho radku
			line = in.readLine();
			while(line != null && !"".equals(line)){
				carList.add(line);
				line = in.readLine();
			}
			return carList;
		} finally {
			if(socket != null){
				try {
					socket.close();
				} catch (Exception e) {}
			}
		}
	}

	/**
	 * Beh zavodu. Cte data ze serveru. Spousti rizeni auta. 
	 * Ukonci se po ukonceni zavodu.
	 * 
	 * @throws java.io.IOException  problem ve spojeni k serveru
	 */
	private void run() throws IOException {
		stop = false;
		while (!stop) {							// smycka do konce zavodu
			String line = in.readLine();
//			System.out.println(line);
			if (line.equals("round")) {			// dalsi kolo v zavode
				round();
			} else if (line.equals("finish")) {	// konec zavodu konci smucku
				stop = true;
			} else {
				System.err.println("Chyba se serveru: " + line);
			}
		}
	}

	/**
	 * Resi jedno posunuti auta. Precte pozici auta od servru,
	 * vypocte nastaveni rizeni, ktere na server.
	 * 
	 * @throws java.io.IOException   problem ve spojeni k serveru
	 */
	private HashMap<String, Float> values = new HashMap<String, Float>();
	private void round() throws IOException {
		float angle = 0;     // uhel k care <0,1>
		float speed = 0;     // rychlost auta <0,1>
		float distance0 = 0;  // vzdalenost od cary <0,1>
		float distance4 = 0; // vzdalenost od cary za 4m<0,1>
		float distance8 = 0; // vzdalenost od cary za 8m<0,1>
		float distance16 = 0; // vzdalenost od cary za 16m<0,1>
		float distance32 = 0; // vzdalenost od cary za 32m<0,1>
		float friction = 0;
		float skid = 0;
		float checkpoint = 0;
        float sensorFrontLeft = 0;
        float sensorFrontMiddleLeft = 0;
        float sensorFrontMiddleRight = 0;
        float sensorFrontRight = 0;
        float sensorFrontRightCorner1 = 0;
        float sensorFrontRightCorner2 = 0;
        float sensorRight1 = 0;
        float sensorRight2 = 0;
        float sensorRearRightCorner2 = 0;
        float sensorRearRightCorner1 = 0;
        float sensorRearRight = 0;
        float sensorRearLeft = 0;
        float sensorRearLeftCorner1 = 0;
        float sensorRearLeftCorner2 = 0;
        float sensorLeft1 = 0;
        float sensorLeft2 = 0;
        float sensorFrontLeftCorner1 = 0;
        float sensorFrontLeftCorner2 = 0;
		
		// cteni dat ze serveru
		String line = in.readLine();
//		System.out.println(line);
		values.clear();
		while (line.length() > 0) {
			String[] data = line.split(":", 2);
			String key = data[0];
			String value = data[1];
			values.put(key, Float.parseFloat(value));
			if (key.equals("angle")) {
				angle = Float.parseFloat(value);
			} else if (key.equals("speed")) {
				speed = Float.parseFloat(value);
			} else if (key.equals("distance0")) {
				distance0 = Float.parseFloat(value);
			} else if (key.equals("distance4")) {
				distance4 = Float.parseFloat(value);
			} else if (key.equals("distance8")) {
				distance8 = Float.parseFloat(value);
			} else if (key.equals("distance16")) {
				distance16 = Float.parseFloat(value);
			} else if (key.equals("distance32")) {
				distance32 = Float.parseFloat(value);
			} else if (key.equals("friction")) {
				friction = Float.parseFloat(value);
			} else if (key.equals("skid")) {
				skid = Float.parseFloat(value);
			} else if (key.equals("checkpoint")) {
				checkpoint = Float.parseFloat(value);
			} else if (key.equals("sensorFrontLeft")) {
				sensorFrontLeft = Float.parseFloat(value);
			} else if (key.equals("sensorFrontMiddleLeft")) {
				sensorFrontMiddleLeft = Float.parseFloat(value);
			} else if (key.equals("sensorFrontMiddleRight")) {
				sensorFrontMiddleRight = Float.parseFloat(value);
			} else if (key.equals("sensorFrontRight")) {
				sensorFrontRight = Float.parseFloat(value);
			} else if (key.equals("sensorFrontRightCorner1")) {
				sensorFrontRightCorner1 = Float.parseFloat(value);
			} else if (key.equals("sensorFrontRightCorner2")) {
				sensorFrontRightCorner2 = Float.parseFloat(value);
			} else if (key.equals("sensorRight1")) {
				sensorRight1 = Float.parseFloat(value);
			} else if (key.equals("sensorRight2")) {
				sensorRight2 = Float.parseFloat(value);
			} else if (key.equals("sensorRearRightCorner2")) {
				sensorRearRightCorner2 = Float.parseFloat(value);
			} else if (key.equals("sensorRearRightCorner1")) {
				sensorRearRightCorner1 = Float.parseFloat(value);
			} else if (key.equals("sensorRearRight")) {
				sensorRearRight = Float.parseFloat(value);
			} else if (key.equals("sensorRearLeft")) {
				sensorRearLeft = Float.parseFloat(value);
			} else if (key.equals("sensorRearLeftCorner1")) {
				sensorRearLeftCorner1 = Float.parseFloat(value);
			} else if (key.equals("sensorRearLeftCorner2")) {
				sensorRearLeftCorner2 = Float.parseFloat(value);
			} else if (key.equals("sensorLeft1")) {
				sensorLeft1 = Float.parseFloat(value);
			} else if (key.equals("sensorLeft2")) {
				sensorLeft2 = Float.parseFloat(value);
			} else if (key.equals("sensorFrontLeftCorner1")) {
				sensorFrontLeftCorner1 = Float.parseFloat(value);
			} else if (key.equals("sensorFrontLeftCorner2")) {
				sensorFrontLeftCorner2 = Float.parseFloat(value);
			} else {
				System.err.println("Chyba se serveru: " + line);
			}
			line = in.readLine();
//			System.out.println(line);
		}

		HashMap<String, Float> responses = driver.drive(values);
		// vypocet nastaveni rizeni, ktery je mozno zmenit za jiny algoritmus

		// odpoved serveru
		out.write("ok\n");
		for(String key : responses.keySet()){
			out.write(key + ":" + responses.get(key) + "\n");
		}
		out.write("\n");
		out.flush();
	}
}
