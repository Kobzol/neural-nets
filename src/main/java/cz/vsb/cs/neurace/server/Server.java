package cz.vsb.cs.neurace.server;

import cz.vsb.cs.neurace.gui.Config;
import cz.vsb.cs.neurace.gui.Resources;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

/**
 * 
 * Server - plní funkci serveru. Čeká na portu a zpracovává požadavky.
 * 
 * @author Marian Krucina
 * 
 */
public class Server implements Runnable{

	/** určuje, zda server má běžet */
	private boolean running = true;
	/** socket serveru */
	private ServerSocket serverSocket;

	/**
	 * Konstruktor serveru. Připojí se k serveru a nastaví adresář pro tratě.
	 * @param port port serveru
	 * @param trackDir adresář pro tratě
	 * @throws IOException
	 */
	public Server(int port, String serverDir, boolean ldap, int ups) throws IOException {

		serverSocket = new ServerSocket(port);
		serverSocket.setSoTimeout(100);
		try{
		Tracks.setTrackDir(serverDir + File.separatorChar + "tracks");
                Garage.get().setServerCarsDir(serverDir + File.separatorChar + "cars");
                Races.setLdap(ldap);
                Races.setUps(ups);
		} catch(IOException ex){
			if(serverSocket != null){
				serverSocket.close();
			}
			throw ex;
		}
	}

	/**
	 * Spoučtí smyčku serveru.
	 */
        @Override
	public void run() {
                
		while (running) {
			try {
				Socket socket = serverSocket.accept();
                                socket.setTcpNoDelay(true);
				new Request(socket);
			} catch (SocketTimeoutException e) {
			} catch (IOException ex) {
				stop();
			}
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
                    System.err.printf(e.getMessage());
                    //e.printStackTrace();
		}
	}

	/**
	 * Ukončuje server.
	 */
	public void stop() {
		running = false;
		Races.getRaces().stop();
	}
        
        public static void printUsage() {
            System.out.println("Usage: server \n -port <port>\n -dir <server directory>\n -ldap <on|off>\n -ups <updates per second>\n "
                                + "-dbport <database port>\n -dbpass <database password>\n");
        }

	/**
	 * Spouští server jako aplikaci.
	 * @param args argumenty
	 * @throws IOException
	 */
	public static void main(String[] args) {
		if (!args[0].startsWith("s"))  {
			printUsage();
			System.exit(0);
		}
                int i = 1;
                try {
                    for(i = 1; i < args.length; i++) {
                        //-port
                        if(args[i].startsWith("-p") && args.length > i + 1) {
                            int port = Integer.parseInt(args[++i]);
                            Config.prefs.putInt("serverPort", port);
                            System.out.println("Server port set to: "+ args[i]);
                        }
                        //-dir
                        else if(args[i].startsWith("-di") && args.length > i + 1) {
                            Config.prefs.put("serverDir", args[++i]);
                            System.out.println("Server directory set to: "+ args[i]);
                        }
                        //-ldap
                        else if(args[i].startsWith("-l") && args.length > i + 1) {
                            i++;
                            if(args[i].equals("on")) {
                                Config.prefs.putBoolean("ldap", true);
                                System.out.println("LDAP login on");
                            }
                            else if(args[i].equals("off")) {
                                Config.prefs.putBoolean("ldap", false);
                                System.out.println("LDAP login off");
                            }
                        }
                        //-ups
                        else if(args[i].startsWith("-u") && args.length > i + 1) {
                            int ups = Integer.parseInt(args[++i]);
                            Config.prefs.putInt("ups", ups);
                            System.out.println("Updates per second set to " + ups);
                        }
                        //-dbport
                        else if(args[i].startsWith("-dbpo") && args.length > i + 1) {
                            int port = Integer.parseInt(args[++i]);
                            Config.prefs.putInt("dbPort", port);
                            System.out.println("Database port set to: "+ args[i]);
                        }
                        //-dbpass
                        else if(args[i].startsWith("-dbpa") && args.length > i + 1) {
                            String password = args[++i];
                            Config.prefs.put("dbPassword", password);
                            System.out.println("Database password changed");
                        }
                        //-help
                        else if(args[i].startsWith("-h") || args[i].startsWith("h")) {
                            printUsage();
                            System.exit(0);
                        }
                        else {
                            System.out.println("Unknown or incomplete argument: " + args[i]);
                            System.exit(0);
                        }
                    }
                }
                catch(Exception e) {
                    System.out.println("Incorrect argument: " + args[i]);
                    System.exit(0);
                }

                try {
                    int port = Config.prefs.getInt("serverPort", 9460);
                    String dir = Config.prefs.get("serverDir", System.getProperty("user.dir")+ File.separator+"server");
                    boolean ldap = Config.prefs.getBoolean("ldap", true);
                    int ups = Config.prefs.getInt("ups", 60);
                    try {
                        Resources.set(new Resources());
                    }
                    catch(IOException e) {
                        System.err.println(e.getMessage());
                    }                    Server server = new Server(port, dir, ldap, ups);
                    Thread serverThread = new Thread(server);
                    serverThread.start();
                    System.out.println("Server is running on port " + port);
                    Scanner in = new Scanner(System.in);
                    while(!in.nextLine().startsWith("s")) {}
                    server.stop();
                }
                catch(Exception e) {
                    System.err.printf(e.getMessage());
                    //e.printStackTrace();
                }
		
	}
}
