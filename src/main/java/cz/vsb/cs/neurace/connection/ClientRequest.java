package cz.vsb.cs.neurace.connection;

import java.io.IOException;

/**
 * 
 * Požadavek klienta. Usnadnuje komunikaci se serverem.
 *
 */
public class ClientRequest extends Connection {

	/**
	 * Konstruktor.
	 * 
	 * @param proto protokol
	 * @param host počítač se serverem
	 * @param port port serveru
	 * @throws IOException
	 */
	public ClientRequest(String proto, String host, int port) throws IOException {
		super(host, port);
		writeln(proto);
	}

	/**
	 * Konec hlavičky - odešle data a zkontroluje příchozí.
	 * 
	 * @return odpověď serveru
	 * @throws IOException
	 * @throws diplomka3.communication.ClientRequestException
	 */
	public String endHead() throws IOException, ClientRequestException {
		writeln();
		flush();
		String s = check(false);
		read();
		return s;
	}

	/**
	 * Kontrola hlavičky odpovědi serveru. 
	 * 
	 * @param close zda se má ukončit spojení při chybě.
	 * @return odpověď serveru
	 * @throws IOException
	 * @throws diplomka3.communication.ClientRequestException
	 */
	public String check(boolean close) throws IOException, ClientRequestException {
		String s;
		do {
			s = readline();
		} while (s.matches("\\s*"));
		if (!s.matches("ok.*")) {
			if (close) {
				close();
			}
			throw new ClientRequestException(s);
		} else {
			return s;
		}
	}
}
