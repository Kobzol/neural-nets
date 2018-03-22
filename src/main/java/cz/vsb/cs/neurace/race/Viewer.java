package cz.vsb.cs.neurace.race;

import cz.vsb.cs.neurace.connection.ClientSocket;
import cz.vsb.cs.neurace.race.Race.Status;
import java.io.IOException;

/**
 * Prohlížeč. Posílá informace o závodě klientovi.
 */
public class Viewer extends Thread {

	/** klient */
	private ClientSocket socket;
	/** sledovaný závod */
	private Race race;
	/** synchronizace odesílání */
	private Lock lock;
	/** zpráva pro diváka */
	private String message;
	/** ukončení závodu */
	private boolean finish;

	/**
	 * Konstruktor
	 * @param socket klient
	 * @param race závod
	 * @param lock zámek všech pro synchronizaci
	 * @throws IOException
	 */
	public Viewer(ClientSocket socket, Race race, Lock lock) throws IOException {
		super("viewer " + race.getRaceName());
		this.socket = socket;
		this.race = race;
		this.lock = lock;
		socket.writeOk();
		socket.write("track", race.getTrackName());
		socket.write("laps", race.getLaps());
                if(race.getChampionship() != null) {
                    socket.write("races", race.getChampionship().getRaces());
                    socket.write("raceNumber", race.getChampionship().getRaceNumber());
                }
                socket.write("status", race.getStatus().toString());
		socket.send();
		start();
	}



	@Override
	public void run() {
		try {
			socket.send(race.getCars().getInfo());
			if (race.getStatus() == Status.RACE) {
                            String msg = "start\n";
                            msg += "startCars:" + race.getStartCars() + "\n";
                            msg +="\n";
                            socket.send(msg);
			}
			while (!finish) {
				lock.suspend();
				String m;
				synchronized (this) {
					m = message;
					message = null;
				}
				if (m != null) {
					socket.send(m);
				}
			}
		} catch (IOException e) {
			//e.printStackTrace();
		}
		socket.close();
		race.getViewers().remove(this);
	}

	/**
	 * Poslat zprávu prohlížeči.
	 * @param m zpráva pro prohlížeč
	 */
	public void message(String m) {
		synchronized (this) {
			if (message == null) {
				message = m;
			} else {
				message += m;
			}
		}
	}

	/**
	 * Ukončení závodu.
	 */
	public void finish() {
		finish = true;
	}
}
