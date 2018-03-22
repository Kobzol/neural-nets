package cz.vsb.cs.neurace.connection;

import java.io.IOException;
import java.net.Socket;

/**
 * Příchozí spojení.
 * Zjistí typ požadavku.
 *
 */
public class ClientSocket extends Connection {
	/** typ, protkol požadavku klienta */
	private String type;
        private Socket socket;

	/**
	 * Konstruktor
	 * @param socket připojení klienta
	 * @throws IOException
	 * @throws diplomka3.communication.ClientSocketException
	 */
	public ClientSocket(Socket socket) throws IOException {
		super(socket);
                socket.setTcpNoDelay(true);
		socket.setSoTimeout(6000000);
		type = readline();
                this.socket = socket;
	}

	/**
	 * Typ(protokol) požadavku.
	 * @return Typ(protokol) požadavku.
	 */
	public String getType() {
		return type;
	}

        public Socket getSocket() {
            return socket;
        }


}
