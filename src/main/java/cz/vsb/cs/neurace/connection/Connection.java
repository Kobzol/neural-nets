package cz.vsb.cs.neurace.connection;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Základní spojení mezi prvky. 
 *
 */
public class Connection {
	/** příchozí hodnoty */
	private Map<String, String> map = new TreeMap<String, String>();
	/** spojení */
	private Socket socket;
	/** pro zápis */
	private Writer out;
	/** pro čtení */
	private Reader in;
	/** vnitřní buffer */
	private StringBuffer outBuffer = new StringBuffer();

	/**
	 * Navázaní komunikace
	 * 
	 * @param host server
	 * @param port port serveru
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Connection(String host, int port) throws UnknownHostException, IOException {
		socket = new Socket(host, port);
		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
	}

	/**
	 * Konstruktor z připojení klienta.
	 * 
	 * @param socket připojení klienta
	 * @throws IOException
	 */
	public Connection(Socket socket) throws IOException {
		this.socket = socket;
		out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
		in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
	}

	/**
	 * Přečte a vrátí jeden řádek.
	 * 
	 * @return jeden řádek
	 * @throws IOException
	 */
	public String readline() throws IOException {
		int ch;
		StringBuilder line = new StringBuilder();
		while ((ch = in.read()) != -1 && ch != '\n') {
			if (ch != '\r') {
				line.append((char)ch);
			}
		}
		if (ch == -1 && line.length() == 0) {
			throw new ConnectException("disconnect");
		} else {
			return line.toString();
		}
	}

	/**
	 * Zapíše řetězec.
	 * 
	 * @param string
	 * @throws IOException
	 */
	public void write(String string) throws IOException {
		outBuffer.append(string);
	}

	/**
	 * Zapíše řetězec a ukončí řádek.
	 * 
	 * @param string
	 * @throws IOException
	 */
	public void writeln(String string) throws IOException {
		write(string + "\n");
	}

	/**
	 * Ukončí řádek.
	 * 
	 * @throws IOException
	 */
	public void writeln() throws IOException {
		write("\n");
	}

	/**
	 * Zapíše klíč a hodnotu.
	 * 
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void write(String key, String value) throws IOException {
		writeln(key + ":" + value);
	}

	/**
	 * Zapíše klíč a hodnotu.
	 * 
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void write(String key, int value) throws IOException {
		writeln(key + ":" + value);
	}
        
        /**
	 * Zapíše klíč a hodnotu.
	 * 
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void write(String key, boolean value) throws IOException {
		writeln(key + ":" + value);
	}

	/**
	 * Zapíše klíč a hodnotu.
	 * 
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void write(String key, float value) throws IOException {
		writeln(key + ":" + value);
	}

	/**
	 * Zapíše klíč a hodnotu.
	 * 
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void write(String key, double value) throws IOException {
		writeln(key + ":" + value);
	}

	/**
	 * Zapíše ok.
	 * 
	 * @throws IOException
	 */
	public void writeOk() throws IOException {
		writeln("ok");
	}

	/**
	 * Zapíše chybu.
	 * 
	 * @param msg popis chyby
	 */
	public void error(String msg) {
		try {
			write("error", msg);
		} catch (IOException e) {
			System.err.println("Can't write error msg: " + msg);
		}
	}

	/**
	 * Uzavře spojení.
	 */
	public void close() {
		try {
			flush();
		} catch (IOException ex) {
                    System.err.printf(ex.getMessage());
		}
		try {
			socket.close();
		} catch (IOException ex) {
                    System.err.printf(ex.getMessage());
		}
	}

	/**
	 * Zjistí, zda je spojení ukončené.
	 * 
	 * @return pokud je ukončené true, jinak false
	 */
	public boolean isClosed() {
		return socket.isClosed();
	}

	/**
	 * Pošle zapsaná data.
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException {
		if (outBuffer.length() > 0) {
			String s = outBuffer.toString();
			outBuffer.setLength(0);
			out.write(s);
			out.flush();
		}
	}

	/**
	 * Přidá nový řádek a odešle data.
	 * 
	 * @throws IOException
	 */
	public void send() throws IOException {
		writeln();
		flush();
	}

	/**
	 * Odešle řetězec.
	 * 
	 * @param string
	 * @throws IOException
	 */
	public void send(String string) throws IOException {
		write(string);
		flush();
	}
	
	/**
	 * Čte a data ukládá do vlastní struktury. Ty bydou přístupné funkcemi getTyp(String key).
	 * 
	 * @throws IOException
	 */
	public void read() throws IOException {
		read("");
	}

	/**
	 * Čte a data ukládá do vlastní struktury. Ty bydou přístupné funkcemi getTyp(String key).
	 * 
	 * @param stop
	 * @throws IOException
	 */
	public void read(String stop) throws IOException {
		map = new TreeMap<String, String>();
		String line;
		while ((line = readline()) != null && !line.equals(stop)) {
			int pos = line.indexOf(':');
			if (pos != -1) {
				map.put(line.substring(0, pos), line.substring(pos + 1));
			} else {
				System.err.println("Communication.read() bad artibut:value read:'" + line + "'");
			}
		}
	}

	/**
	 * Vrátí hodnotu přečtenou podle klíče.
	 * @param key
	 * @return
	 */
	public String getString(String key) {
		if (map == null) {
			return null;
		}
		return map.get(key);
	}

	/**
	 * Vrátí hodnotu přečtenou podle klíče.
	 * @param key
	 * @return
	 */
	public double getDouble(String key) {
		try {
			return Double.parseDouble(getString(key));
		} catch (NumberFormatException e) {
                    System.err.println(e.getMessage());
                    //e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Vrátí hodnotu přečtenou podle klíče.
	 * @param key
	 * @return
	 */
	public float getFloat(String key) {
		try {
			return Float.parseFloat(getString(key));
		} catch (NumberFormatException e) {
			System.err.println(e.getMessage());
                         //e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Vrátí hodnotu přečtenou podle klíče.
	 * @param key
	 * @return
	 */
	public int getInt(String key) {
		try {
			return Integer.parseInt(getString(key));
		} catch (NumberFormatException e) {
			System.err.println(e.getMessage());
                        //e.printStackTrace();
		}
		return 0;
	}
}
