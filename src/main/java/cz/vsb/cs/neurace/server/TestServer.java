package cz.vsb.cs.neurace.server;

import cz.vsb.cs.neurace.gui.MainWindow;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 *
 * @author Petr Hamalčík
 */
public class TestServer extends Thread {

		/** zastavení */
		private boolean stop = false;
		/** socket serveru */
		private ServerSocket serverSocket;

		/**
		 * Konstruktor, vytvoří vlákno, které bude čekat na spojení klienta.
		 * @param port
		 */
		public TestServer(int port) throws IOException {
			serverSocket = new ServerSocket(port);
			start();
		}

		@Override
		public void run() {
			try {
				serverSocket.setSoTimeout(100);

				while (!stop) {
					try {
						Socket s = serverSocket.accept();
						new TestRequest(s);
                                        } catch (SocketTimeoutException e) {
						continue;
					} catch (IOException ex) {
                                                stop = true;
                                        }
				}
			} catch (Exception e) {
				//e.printStackTrace();
				MainWindow.get().errorMsg(e.getMessage());

			} finally {
				try {
					serverSocket.close();
				} catch (IOException e1) {
                                    MainWindow.get().errorMsg(e1.getMessage());
                                    //e1.printStackTrace();
				}
			}
		}

		/**
		 * Zrušení čekání
		 */
		public void stopServer() {
			stop = true;
		}
	}
