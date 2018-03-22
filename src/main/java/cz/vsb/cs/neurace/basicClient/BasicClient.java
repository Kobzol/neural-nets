package cz.vsb.cs.neurace.basicClient;

import main.racer.KeyboardDriver;
import main.racer.NetDriver;

import java.io.IOException;
import java.util.List;
import java.util.Random;


/**
 * Jednoduchy ukazkovy klient.
 * Pripoji se k zavodnimu serveru a ridi auto.
 * 
 */
public class BasicClient {

	/**
	 * Funkce, ktera vytvari a spousti klienta.
	 * 
	 * @param args pole argumentu: server port nazev_zavodu jmeno_ridice
	 * @throws java.io.IOException problem ve spojeni k serveru, zavodu
	 */
	public static void main(String[] args) throws IOException {
		String host = "localhost";
		int port = 9461;
		String raceName = "test";
		String driverName = "basic_client";
		String carType = null;
		RaceConnector raceConnector = null;
		if (args.length < 4) {
			// kontrola argumentu programu
			raceConnector = new RaceConnector(host, port, null);
			System.err.println("argumenty: server port nazev_zavodu jmeno_ridice [typ_auta]");
			List<String> raceList =  raceConnector.listRaces();
			raceName = raceList.get(new Random().nextInt(raceList.size()));
			List<String> carList =  raceConnector.listCars(raceName);
			carType = carList.get(new Random().nextInt(carList.size()));
			driverName += "_" + carType;
//			host = JOptionPane.showInputDialog("Host:", host);
//			port = Integer.parseInt(JOptionPane.showInputDialog("Port:", Integer.toString(port)));
//			raceName = JOptionPane.showInputDialog("Race name:", raceName);
//			driverName = JOptionPane.showInputDialog("Driver name:", driverName);
		} else {
			// nacteni parametu
			host = args[0];
			port = Integer.parseInt(args[1]);
			raceName = args[2];
			driverName = args[3];
			if(args.length > 4){
				carType = args[4];
			}
			raceConnector = new RaceConnector(host, port, null);
		}
		// vytvoreni klienta
		/*raceConnector.setDriver(new DriverInterface() {
			
			@Override
			public HashMap<String, Float> drive(HashMap<String, Float> values) {
				HashMap<String, Float> responses = new HashMap<String, Float>();
				float distance0 = values.get("distance0");
				// pokud je v levo jede doprava, jinak do leva
				if (distance0 < 0.5) {
					responses.put("wheel", 0.8f);
				} else {
					responses.put("wheel", 0.2f);
				}
				// maximalni zrychleni
				responses.put("acc", 1f);
				return responses;
			}
		});*/
		//raceConnector.setDriver(new NetDriver());
		raceConnector.setDriver(new KeyboardDriver());
		raceConnector.start(raceName, driverName, carType);
	}
}
