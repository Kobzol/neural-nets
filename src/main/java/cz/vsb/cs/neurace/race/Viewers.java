package cz.vsb.cs.neurace.race;

import cz.vsb.cs.neurace.connection.ClientSocket;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Prohlížeče, kteří sledují závod.
 */
public class Viewers {
	/** prohlížeče */
	private List<Viewer> viewers;
	/** sledovaný závod */
	private Race race;
	/** zámek pro synchronizaci */
	private Lock lock;
	/** zpráva pro prohlížeče */
	private String message;

	/**
	 * Konstruktor
	 * @param race
	 */
	public Viewers(Race race) {
		this.race = race;
		this.lock = new Lock();
		viewers = new Vector<Viewer>();
	}

	/**
	 * Přidá prohlížeč.
	 * @param socket klient
	 * @throws IOException
	 */
	public void add(ClientSocket socket) throws IOException {
		viewers.add(new Viewer(socket, race, lock));
	}

	/**
	 * Příprava zprávy pro prohlížeče.
	 */
	public void prepare() {
		StringBuffer sb = new StringBuffer("round\n");
		sb.append("time:");
		sb.append(race.getTime());
                sb.append("\n");
                sb.append("remaining:");
                if(race.isFirstFinished())
                    sb.append(race.getRemainingTime());
                else
                    sb.append("-1");
		sb.append("\n");
		race.getCars().getInfo(sb);
		sb.append("endround\n");
		message = sb.toString();
	}

	/**
	 * Zaslaní připravené zprávy prohlížečům.
	 */
	public void communicate() {
		sendMessage(message);
	}

	/**
	 * Pošle zprávu prohlížečům.
	 * @param message zpráva
	 */
	public void sendMessage(String message) {
		for (Viewer v : viewers) {
			v.message(message);
		}
		lock.resume();
	}

	/**
	 * Odstraní prohlížeč ze seznamu.
	 * @param viewer
	 */
	public void remove(Viewer viewer) {
		viewers.remove(viewer);
	}

	/**
	 * Oznámí start prohlížečům.
	 */
	public void startRace(int startCars) {
            String msg = "start\n";
            msg += "startCars:" + startCars + "\n";
            msg +="\n";
            sendMessage(msg);
	}

        /**
	 * Oznámí prohlížečům restart závodu.
	 */
	public void restartRace(String carInfo) {
		sendMessage("restart\n" + carInfo);
	}

        /**
	 * Odstraní závodníka
	 */
	public void removeDriver(String name) {
		sendMessage("remove\n" + name + "\n");
	}

        /**
	 * Oznámí prohlížečům změnu trati.
	 */
	public void changeTrack(String trackName, int laps, int races, String carInfo) {
		sendMessage("change\n" + trackName + "\n" + laps + "\n" + races + "\n"+ carInfo);
	}

        /**
	 * Oznámí prohlížečům, že jsou všechna auta v cíli.
	 */
	public void finish(List<String> messages) {
            String msg = "finish\n";
            for(String m: messages) {
                msg += m+"\n";
            }
            msg +="\n";
            sendMessage(msg);
	}

        public void sendScore(HashMap<String, Integer> score, List<String> messages) {
            String msg = "finish\n";
            for(String driver: score.keySet()) {
                msg += driver + " " + score.get(driver)+"\n";
            }
            msg +="\n";
            
            for(String m: messages) {
                msg += m+"\n";
            }
            msg +="\n";
            sendMessage(msg);
        }

	/**
	 * Oznámí konec prohlížečům.
	 */
	public void stopRace() {
		for (Viewer v : viewers) {
			v.finish();
		}
		sendMessage("end\n");
	}
}
