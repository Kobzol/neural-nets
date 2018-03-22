package cz.vsb.cs.neurace.race;

import cz.vsb.cs.neurace.connection.ClientSocket;
import java.io.IOException;

/**
 * Zajišťuje komunikaci s klientem. Ve zvlaštním vlákně.
 * 
 */
public class Driver extends Thread {

	/** auto řidiče */
	private Car car;
	/** Zámek na uspavani vlákna */
	private Lock lock;
	/** Jméno řidiče */
	private String name;
	/** Klient, který řídí auto. */
	private ClientSocket socket;
	/** zda se má klient odpojit */
	private boolean disconnect;

	/**
	 * Konstruktor
	 * 
	 * @param socket
	 *            spojení na klienta
	 * @param car
	 *            auto
	 * @param lock
	 *            zámek
	 * @throws IOException
	 */
	public Driver(ClientSocket socket, Car car, Lock lock) throws IOException {
		super("driver " + socket.getString("driver"));
		this.name = socket.getString("driver");
		this.socket = socket;
		this.car = car;
		this.lock = lock;
		socket.writeln("ok");
		socket.send();
		start();
	}

	@Override
	public void run() {
		while (socket != null) {
			lock.suspend();
			if (disconnect) {
				// jizda je ukoncena
				try {
					socket.writeln("finish");
					socket.writeln();
					socket.send();
					socket.close();
				} catch (IOException e) {
					// e.printStackTrace();
				}
				return;
			} else {
				//
				if (socket != null) {
					try {
						// pošle klientovi informace o autě.
						socket.writeln("round");
						car.driverWrite(socket);
						socket.send();
						// přečte řádek, který by měl být ok.
						String ans = socket.readline();
						if (ans.equals("ok")) {
							String line;
							// čte řádky a ukláda hodnoty do auta.
							while ((line = socket.readline()) != null
									&& !line.equals("")) {
								int pos = line.indexOf(':');
								if (pos != -1) {
									String error = car.set(
											line.substring(0, pos),
											line.substring(pos + 1));
									if (error != null) {
										socket.send("error:" + error + "\n");
									}
								} else {
									socket.send("error:unknown value: '" + line
											+ "'\n");
								}
							}
						} else {
							socket.send("error:unknown answer: '" + ans
									+ "', need 'ok'\n");
						}
					} catch (IOException e) {
						// e.printStackTrace();
						socket.close();
						socket = null;
					}
				}
			}
		}
	}

	/**
	 * Ukončení komunikace s klientem
	 */
	public void disconnect() {
		disconnect = true;
	}

	public ClientSocket getSocket() {
		return socket;
	}

	/**
	 * Vrátí jméno řidiče
	 * 
	 * @return jméno řidiče
	 */
	public String getDriverName() {
		return name;
	}
}
