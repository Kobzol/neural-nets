package cz.vsb.cs.neurace.server;

import cz.vsb.cs.neurace.connection.ClientSocket;
import cz.vsb.cs.neurace.gui.TestTab;
import cz.vsb.cs.neurace.race.Car;
import cz.vsb.cs.neurace.race.Race;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;

/**
 * Požadavek klienta. Požadavek se spracovává ve vlastním vlakně.
 * @author Petr Hamalčík
 */
public class TestRequest extends Thread {

	/**
	 * Konstruktor vytvoří vlastní vlákno s požadavkem.
	 * @param socket klientův socket
	 */
	public TestRequest(Socket socket) {
		this.socket = socket;
		start();
	}

	@Override
	public void run() {
		ClientSocket client = null;
		try {
			client = new ClientSocket(socket);

			client.read();
			if (client.getType().equals("racelist")) {
                            raceList(client);
                            client.close();
                        } else if (client.getType().equals("carlist")) {
                            carList(client);
                            client.close();
			} else if (client.getType().equals("driver")) {
                            addDriver(client);
			} else {
                            System.err.println("Unknown request " + client.getType());
                            client.error("Unknown request");
                            client.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (client != null) {
                            client.close();
                        }
		}
	}

	private Socket socket;


        public void raceList(ClientSocket socket) throws IOException {
		socket.writeOk();
		socket.writeln();
                socket.writeln("test");
		socket.send();
		socket.close();
	}

        public void carList(ClientSocket socket) throws IOException {
		Race race = TestTab.get().getRace();
                if (race == null) {
			socket.error("race not exists");
			return;
		}
                socket.writeOk();
                socket.writeln();
		Collection<String> cars = race.getAvailableCars();
        for (String car : cars) {
			socket.writeln(car);
		}
		socket.writeln();
		socket.send();
                socket.close();
	}


        /**
	 * Přidá prohlížeč do závodu.
	 *
	 * @param socket připojení na řidiče.
	 * @throws IOException
	 */
	public void addDriver(ClientSocket socket) throws IOException {
		Race race = TestTab.get().getRace();
                if (race == null) {
                    socket.error("race not exist");
                    socket.send();
                    socket.close();
		}
                else {
                    Car car = race.addDriver(socket);
                    if(car != null) {
                        TestTab.get().addCar(car);
                    }
		}
	}
}
