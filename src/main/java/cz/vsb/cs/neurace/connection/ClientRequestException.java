package cz.vsb.cs.neurace.connection;

/**
 * Výjimka volaná pokud přijde chyba ze serveru.
 */
public class ClientRequestException extends Exception {
	public ClientRequestException(String message) {
		super(message);
	}
}
