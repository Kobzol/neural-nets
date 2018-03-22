package cz.vsb.cs.neurace.server;

import cz.vsb.cs.neurace.connection.ClientSocket;
import cz.vsb.cs.neurace.track.Track;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;

/**
 * Spravuje tratě uložené na serveru.
 * 
 */
public class Tracks {

	/** Adresář s tratěma */
	private static String trackDir;

	/**
	 * Konstruktor privátní.
	 * 
	 * @throws FileNotFoundException
	 */
	private Tracks() throws FileNotFoundException {
		if (trackDir == null) {
			throw new FileNotFoundException("Not set trackDir");
		}
	}

	/**
	 * Vrátí trať se zadanám jménem.
	 * 
	 * @param name název trati.
	 * @return trať, pokud neexistuje vyhodí výjimku.
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public Track getTrack(String name) throws FileNotFoundException, ParserConfigurationException, IOException, ClassNotFoundException,
			SAXException {
		return new Track(new File(trackDir + File.separatorChar + name));
	}

	/**
	 * Pošle klientovi pořadouvanou trať.
	 * @param socket připojení klienta.
	 * @throws IOException
	 */
	public void loadTrack(ClientSocket socket) throws IOException {
		try {
			Track track = getTrack(socket.getString("track"));
			socket.writeOk();
			socket.writeln();
			socket.write(track.streamTrack());
			socket.send();
		} catch (Exception e) {
			//e.printStackTrace();
                        System.err.println(e.getMessage());
			socket.error("Track can't load.");
		}
		socket.close();
	}

	/**
	 * Uloží trať, kterou poslal klient.
	 * @param socket připojení klienta.
	 * @throws IOException
	 */
	public void saveTrack(ClientSocket socket) throws IOException {
		Track track;
		String name = socket.getString("track");
		if (name.indexOf("/") != -1) {
			socket.error("Wrong name.");
		} else {
			try {
				track = new Track(socket.readline());
				if (Races.getRaces().useTrack(name)) {
					socket.error("track is used");
					return;
				}
				track.save(new File(trackDir + File.separatorChar + name));
				socket.writeOk();
				socket.send();
			} catch (Exception e) {
				//e.printStackTrace();
                                System.err.println(e.getMessage());
				socket.error("Can't save track.");
			}
		}
		socket.close();
	}

	/**
	 * Pošle klientovi seznam tratí uložených na serveru.
	 * @param socket připojení klienta.
	 * @throws IOException
	 */
	public void tracksList(ClientSocket socket) throws IOException {
		File dir = new File(trackDir);
                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".ntr");
                    }
                };
		if (dir.canRead() && dir.isDirectory()) {
			socket.writeOk();
			socket.writeln();

			File[] files = dir.listFiles(filter);
			Arrays.sort(files);
			for (File file : files) {
				if (file.isFile() && file.canRead()) {
					socket.writeln(file.getName());
				}
			}
		} else {
			socket.error("Can't read tracks.");
		}
		socket.send();
		socket.close();
	}

	/**
	 * Vymaže trať definovanou klientem.
	 * @param socket připojení klienta.
	 * @throws IOException
	 */
	public void removeTrack(ClientSocket socket) throws IOException {
		if (Races.getRaces().useTrack(socket.getString("track"))) {
			socket.error("track is used");
			return;
		}
		if ((new File(socket.getString("track"))).delete()) {
			socket.writeOk();
		} else {
			socket.error("not deleted");
		}
	}
	/** Jediná instance trídy Tracks */
	private static Tracks tracksInstance;

	/**
	 * Nastaví adresář pro tratě.
	 * @param trackDir adresář pro tratě.
	 */
	public static void setTrackDir(String trackDir) throws FileNotFoundException {
		File td = new File(trackDir);
		if (!td.isDirectory()) {
			throw new FileNotFoundException(td.getPath() + " is not directory!");
		}
		Tracks.trackDir = trackDir;
	}

	/**
	 * Vrátí instanci Tracks.
	 * 
	 * @return Tracks.
	 * @throws FileNotFoundException
	 */
	public static Tracks getTracks() throws FileNotFoundException {
		if (tracksInstance == null) {
			tracksInstance = new Tracks();
		}
		return tracksInstance;
	}

        public String getTrackDir() {
            return trackDir;
        }
}
