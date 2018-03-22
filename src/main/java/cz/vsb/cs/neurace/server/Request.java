package cz.vsb.cs.neurace.server;

import cz.vsb.cs.neurace.connection.ClientSocket;
import java.net.Socket;

/**
 * Požadavek klienta. Požadavek se spracovává ve vlastním vlakně.
 */
public class Request extends Thread {

	/**
	 * Konstruktor vytvoří vlastní vlákno s požadavkem.
	 * @param socket klientův socket
	 */
	public Request(Socket socket) {
		this.socket = socket;
		start();
	}

	@Override
	public void run() {
		ClientSocket client = null;
		try {
			client = new ClientSocket(socket);

			client.read();
			if (client.getType().equals("savetrack")) {
				Tracks.getTracks().saveTrack(client);
				client.close();
			} else if (client.getType().equals("loadtrack")) {
				Tracks.getTracks().loadTrack(client);
				client.close();
			} else if (client.getType().equals("tracklist")) {
				Tracks.getTracks().tracksList(client);
				client.close();
			} else if (client.getType().equals("racenew")) {
				Races.getRaces().newRace(client);
				client.close();
			} else if (client.getType().equals("racelist")) {
				Races.getRaces().raceList(client);
				client.close();
                        } else if (client.getType().equals("carlist")) {
				Races.getRaces().carList(client);
				client.close();
			} else if (client.getType().equals("racestart")) {
				Races.getRaces().startRace(client);
				client.close();
			} else if (client.getType().equals("racestop")) {
				Races.getRaces().stopRace(client);
				client.close();
                        } else if (client.getType().equals("racepause")) {
				Races.getRaces().pauseRace(client);
				client.close();
			} else if (client.getType().equals("racestep")) {
				Races.getRaces().stepRace(client);
				client.close();
                        } else if (client.getType().equals("racerestart")) {
				Races.getRaces().restartRace(client);
				client.close();
                        } else if (client.getType().equals("changetrack")) {
				Races.getRaces().changeTrack(client);
				client.close();
                        } else if (client.getType().equals("test")) {
                                client.writeOk();
                                client.send();
				client.close();
			} else if (client.getType().equals("viewer")) {
				Races.getRaces().addViewer(client);
			} else if (client.getType().equals("driver")) {
				Races.getRaces().addDriver(client);
			} else {
				System.err.println("Unknown request " + client.getType());
				client.error("Unknown request");
				client.close();
			}
		} catch (Exception e) {
			//e.printStackTrace();
                        System.err.println(e.getMessage());
			if (client != null) {
                            client.close();
                        }
		}
	}

	private Socket socket;
}
