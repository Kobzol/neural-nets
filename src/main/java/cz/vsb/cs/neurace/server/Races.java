package cz.vsb.cs.neurace.server;

import cz.vsb.cs.neurace.connection.ClientSocket;
import cz.vsb.cs.neurace.race.Race;
import cz.vsb.cs.neurace.race.RaceTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * 
 * Obsahuje, dělá nové a spravuje závody probíhající na serveru.
 * 
 */
public class Races {

	/** Jediná instance této třídy */
	private static Races racesInstance;
	/** Probíhající závody */
	private List<Race> racesList = new Vector<Race>();
	/** Listenery hlídající změny */
	private List<RacesListener> racesListeners = new LinkedList<RacesListener>();

        private static boolean ldap;
        private static int ups = 60;

	/** Konstruktor privátní */
	private Races() {

	}

	/**
	 * Založení nového závodu.
	 * 
	 * @param socket klietův příkaz pro založení nového závodu.
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public void newRace(ClientSocket socket) throws IOException, ParserConfigurationException, SAXException, ClassNotFoundException {
		if (getRace(socket.getString("race")) != null) {
			socket.error("race exists");
		} else {
			String name = socket.getString("track");
			int laps = (int) Float.parseFloat(socket.getString("laps"));
                        int racesNum = Integer.parseInt(socket.getString("races"));
                        boolean collisions = Boolean.parseBoolean(socket.getString("collisions"));
                        int limit = 60;
			racesList.add(new Race(socket.getString("race"), new RaceTrack(Tracks.getTracks().getTrack(name)), name, laps, racesNum, collisions, limit, ups));
			socket.writeOk();
			socket.send();
			for (RacesListener rl : racesListeners) {
				rl.racesAction();
			}
		}
	}

	/**
	 * Výpis probíhajících závodů.
	 * 
	 * @param socket klietův příkaz, do kterého zapíše se výsledek.
	 * @throws IOException
	 */
	public void raceList(ClientSocket socket) throws IOException {
		socket.writeOk();
		socket.writeln();
		for (Race race : racesList) {
			socket.writeln(race.getRaceName());
		}
		socket.send();
		socket.close();
	}

        /**
	 * Výpis aut dostupných v závodě.
	 *
	 * @param socket klietův příkaz, do kterého zapíše se výsledek.
	 * @throws IOException
	 */
	public void carList(ClientSocket socket) throws IOException {
		if (getRace(socket.getString("race")) == null) {
			socket.error("race not exists");
			return;
		}
        socket.writeOk();
        socket.writeln();
        Collection<String> cars = getRace(socket.getString("race")).getAvailableCars();
        for (String car : cars) {
			socket.writeln(car);
		}
		socket.writeln();
		socket.send();
                socket.close();
	}

	/**
	 * Odstartuje závod.
	 * 
	 * @param socket klietův příkaz pro odstartování závodu.
	 * @throws IOException
	 */
	public void startRace(ClientSocket socket) throws IOException {
		if (getRace(socket.getString("race")) == null) {
			socket.error("race not exists");
			return;
		}
		getRace(socket.getString("race")).startRace();
		socket.writeOk();
		socket.send();
	}

	/**
	 * Zastavi závod.
	 * 
	 * @param socket klietův příkaz pro zastavení závodu.
	 * @throws IOException
	 */
	public void stopRace(ClientSocket socket) throws IOException {
		if (getRace(socket.getString("race")) == null) {
			socket.error("race not exists");
			return;
		}
		getRace(socket.getString("race")).stopRace();
		socket.writeOk();
		socket.send();
	}

        /**
	 * Pozastavi závod.
	 *
	 * @param socket klietův příkaz pro zastavení závodu.
	 * @throws IOException
	 */
	public void pauseRace(ClientSocket socket) throws IOException {
		if (getRace(socket.getString("race")) == null) {
			socket.error("race not exists");
			return;
		}
		getRace(socket.getString("race")).pauseRace();
		socket.writeOk();
		socket.send();
	}

        /**
	 * Krokuje závod.
	 *
	 * @param socket klietův příkaz pro zastavení závodu.
	 * @throws IOException
	 */
	public void stepRace(ClientSocket socket) throws IOException {
		if (getRace(socket.getString("race")) == null) {
			socket.error("race not exists");
			return;
		}
		getRace(socket.getString("race")).stepRace();
		socket.writeOk();
		socket.send();
	}

        /**
	 * Restartuje závod.
	 *
	 * @param socket klietův příkaz pro zastavení závodu.
	 * @throws IOException
	 */
	public void restartRace(ClientSocket socket) throws IOException {
		if (getRace(socket.getString("race")) == null) {
			socket.error("race not exists");
			return;
		}
		getRace(socket.getString("race")).restartRace();
		socket.writeOk();
		socket.send();
	}

        /**
	 * Změní trať.
	 *
	 * @param socket klietův příkaz pro zastavení závodu.
	 * @throws IOException
	 */
	public void changeTrack(ClientSocket socket) throws IOException, ParserConfigurationException, SAXException, ClassNotFoundException {
		if (getRace(socket.getString("race")) == null) {
			socket.error("race not exists");
			return;
		}
                String tn = socket.getString("track");
                RaceTrack track = new RaceTrack(Tracks.getTracks().getTrack(tn));
		int laps = (int) Float.parseFloat(socket.getString("laps"));
                int races = (int) Float.parseFloat(socket.getString("races"));
                boolean collisions = (boolean) Boolean.parseBoolean(socket.getString("collisions"));
		getRace(socket.getString("race")).changeTrack(tn, track, laps, races, collisions);
		socket.writeOk();
		socket.send();
	}

	/**
	 * Přidá prohlížeč do závodu.
	 * 
	 * @param socket příkaz a připojení na prohlížeč.
	 * @throws IOException
	 */
	public void addViewer(ClientSocket socket) throws IOException {
                
		if (getRace(socket.getString("race")) == null) {
			socket.error("race not exist");
			socket.send();
			socket.close();
		}else {
			getRace(socket.getString("race")).addViewer(socket);
		}
	}

	/**
	 * Přidá prohlížeč do závodu.
	 * 
	 * @param socket připojení na řidiče.
	 * @throws IOException
	 */
	public void addDriver(ClientSocket socket) throws IOException {
            if (getRace(socket.getString("race")) == null) {
			socket.error("race not exist");
			socket.send();
			socket.close();
            }
            else {
                boolean ok = true;
                if(ldap) {
                    ArrayList<String>  messages = new ArrayList<String>();
                    ok = Login.login(socket.getString("driver"), socket.getString("password"), messages);
                    if(!ok) {
                        for(String msg: messages) {
                            socket.error(msg);
                        }
                        socket.send();
                        socket.close();
                    }
                }

                if(ok){
                        getRace(socket.getString("race")).addDriver(socket);
                }
            }
	}

	/**
	 * Zkontrolije, zda je trať používaná v závodě.
	 * 
	 * @param name název trati.
	 * @return true pokud je trať používaná.
	 */
	public boolean useTrack(String name) {
		for (Race race : racesList) {
			if (race.getTrackName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Odstraví závod ze seznamu závodů.
	 * 
	 * @param race závod pro odstranění.
	 */
	public void remove(Race race) {
		racesList.remove(race);
		for (RacesListener rl : racesListeners) {
			rl.racesAction();
		}

	}

	/**
	 * Vrátí závod podle názvu.
	 * 
	 * @param name název závodu.
	 * @return null pokud závod nenalezen, jinak nalezený závod.
	 */
	public Race getRace(String name) {
		for (Race race : racesList) {
			if (race.getRaceName().equals(name)) {
				return race;
			}
		}
		return null;
	}

	/**
	 * Přidá listener.
	 * 
	 * @param racesListener nový listener
	 */
	public void addRacesListener(RacesListener racesListener) {
		racesListeners.add(racesListener);
	}

        

	/**
	 * Ostraní listener.
	 * 
	 * @param racesListener listener pro odstranění
	 */
	public void removeRacesListener(RacesListener racesListener) {
		racesListeners.remove(racesListener);
	}

	/**
	 * Seznam běžících závodů.
	 * 
	 * @return běžící závody
	 */
	public List<Race> getRacesList() {
		return racesList;
	}

	/**
	 * Ukončuje všechny závody.
	 */
	public void stop() {
		for (Race r : racesList) {
			r.stopRace();
		}
	}

	/**
	 * Vrátí instanci Races.
	 * 
	 * @return Races
	 */
	public static Races getRaces() {
		if (racesInstance == null) {
			racesInstance = new Races();
		}
		return racesInstance;
	}



        public static void setLdap(boolean value) {
            ldap = value;
        }

        public static void setUps(int ups) {
            Races.ups = ups;
        }
}
