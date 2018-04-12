package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.gui.track.TrackView;
import cz.vsb.cs.neurace.track.Track;
import org.xml.sax.SAXException;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;


/**
 * 
 * Okno systému.
 * 
 */
public class MainWindow extends JFrame {

        /** jediná instance */
	private static MainWindow mainWindow;
        
	/**
	 * Stavy, které systém může nabývat z hlediska GUI
	 */
	public enum Status {

		/** nenačtená trať */
		NOTRACK,
		/** nová trať */
		NEWTRACK,
		/** načtena trať */
		TRACKLOAD
	}
	/** Aktualni status */
	private Status status;
	/** Načtená trať */
	private Track track;
	/** Otevřený soubor s tratí */
	private File trackFile;
	/** open/save dialog */
	private JFileChooser fileTrackChooser;
	/** název tratě */
	private String trackName;
        
        JTabbedPane tabbedPane;

        Resources resources;

        /**
	 * Konstruktor, vytvoří okno s celým GUI.
	 */
	public MainWindow() {
      		super(Text.getString("title"));
		setLookAndFeelClassName();
		JPanel contentPane = new JPanel(new BorderLayout());
		setContentPane(contentPane);
		setSize(950, 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setDialogTexts();
                
		fileTrackChooser = new JFileChooser();
                fileTrackChooser.setLocale(new Locale(Config.get().getString("locale")));
		FileNameExtensionFilter filter = new FileNameExtensionFilter("track", "ntr");
		fileTrackChooser.setFileFilter(filter);
                File trackDir = new File(Config.prefs.get("localTrackDir", System.getProperty("user.dir")));
                fileTrackChooser.setCurrentDirectory(trackDir);
                TrackView tp = new TrackView(null, 300);
                fileTrackChooser.setAccessory(tp);
                fileTrackChooser.addPropertyChangeListener(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, tp);

                try {
                    Resources.set(new Resources());
                }
                catch(IOException e) {
                    System.err.println(e.getMessage());
                }

		tabbedPane = new JTabbedPane();

                //tabbedPane.addTab(Text.getString("track"), null, TrackTab.get().getPanel());
		
		
		tabbedPane.addTab(Text.getString("race"), null, RaceTab.get().getPanel());
                tabbedPane.addTab(Text.getString("track_editor"), null, TrackEditorTab.get().getPanel());
                tabbedPane.addTab(Text.getString("test"), null, TestTab.get().getPanel());
                tabbedPane.addTab(Text.getString("leaderboards"), null, LeaderboardTab.get().getPanel());
		tabbedPane.addTab(Text.getString("server"), null, ServerTab.get().getPanel());
		tabbedPane.addTab(Text.getString("config"), null, ConfigTab.get().getPanel());
               

		this.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		setStatus(Status.NOTRACK);

		tabbedPane.setSelectedIndex(2);
		try {
			setTrack(new Track(new File("src/main/resources/tracks/test1.ntr")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		setStatus(Status.TRACKLOAD);
		TestTab.get().connect();

		//pack();
		setVisible(true);
	}
        
        private void setDialogTexts() {
            UIManager.put("FileChooser.lookInLabelText", Text.getString("lookInLabelText"));
            UIManager.put("FileChooser.saveInLabelText", Text.getString("saveInLabelText"));
            UIManager.put("FileChooser.saveButtonText", Text.getString("saveButtonText"));
            UIManager.put("FileChooser.openButtonText", Text.getString("openButtonText"));
            UIManager.put("FileChooser.saveButtonToolTipText", Text.getString("saveButtonToolTipText"));
            UIManager.put("FileChooser.openButtonToolTipText", Text.getString("openButtonToolTipText"));
            UIManager.put("FileChooser.filesOfTypeLabelText", Text.getString("filesOfTypeLabelText"));
            UIManager.put("FileChooser.upFolderToolTipText", Text.getString("upFolderToolTipText"));
            UIManager.put("FileChooser.fileNameLabelText", Text.getString("fileNameLabelText"));
            UIManager.put("FileChooser.updateButtonText", Text.getString("updateButtonText"));
            UIManager.put("FileChooser.cancelButtonText", Text.getString("cancelButtonText"));
            UIManager.put("FileChooser.newFolderToolTipText", Text.getString("newFolderToolTipText"));
            UIManager.put("FileChooser.listViewButtonToolTipText", Text.getString("listViewButtonToolTipTextlist"));
            UIManager.put("FileChooser.detailsViewButtonToolTipText", Text.getString("detailsViewButtonToolTipText"));
            UIManager.put("OptionPane.yesButtonText", Text.getString("yes"));
            UIManager.put("OptionPane.noButtonText", Text.getString("no"));
            UIManager.put("OptionPane.cancelButtonText", Text.getString("cancelButtonText"));
        }

	/**
	 * Nastaví vhodný LookAndFeel
	 */
	private void setLookAndFeelClassName() {
		String lookAndFeelClassName = Config.get().get("LookAndFeel");
		if (lookAndFeelClassName != null) {
			try {
				UIManager.setLookAndFeel(lookAndFeelClassName);
			} catch (Exception e) {
                                System.err.printf(e.getMessage());
				//e.printStackTrace();
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e2) {
                                    System.err.printf(e2.getMessage());
					//e2.printStackTrace();
				}
			}
		} else {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
                            System.err.printf(e.getMessage());
				//e.printStackTrace();
			}
		}
	}

	/**
	 * Načte trať z disku.
	 */
	public void loadTrackLocal() {
		if (fileTrackChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                    Config.prefs.put("localTrackDir", fileTrackChooser.getCurrentDirectory().getPath());
			try {
				trackFile = fileTrackChooser.getSelectedFile();
				setTrack(new Track(trackFile));
				setStatus(Status.TRACKLOAD);
			} catch (Exception ex) {
				errorMsg(ex.getLocalizedMessage());
				//ex.printStackTrace();
			}
		}
	}

	/**
	 * Uloží trať do souboru
	 * @param as zda vždy zobrazovat save dialog
	 */
	public void saveTrackLocal(boolean as) {
                
		if (as || trackFile == null || !trackFile.canWrite()) {
			if (fileTrackChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
				trackFile = fileTrackChooser.getSelectedFile();
			} else {
				return;
			}
		}
		try {
                    if(!trackFile.getName().endsWith(".ntr")) {
                        trackFile = new File(trackFile.getPath()+".ntr");
                    }
                    track.save(trackFile);
		} catch (Exception ex) {
			errorMsg(ex.getLocalizedMessage());
			//ex.printStackTrace();
		}
	}

	/**
	 * Načte trať ze serveru.
	 */
	public void loadTrackServer() {
		try {
			String name = TrackChooser.showOpenDialog(this, Config.get().getString("host"), Config.get().getInt("port"));
			if (name != null) {
				setTrack(new Track(name, Config.get().getString("host"), Config.get().getInt("port")));
				trackName = name;
				setStatus(Status.TRACKLOAD);
			}
		} catch (Exception ex) {
                    errorMsg(ex.getLocalizedMessage());
		}
	}

	/**
	 * Uloží trať na server
	 * @param as zda vždy zobrazovat save dialog
	 */
	public void saveTrackServer(boolean as) {
		try {
			if (as || trackName == null) {
				String name = TrackChooser.showSaveDialog(this, trackName, Config.get().getString("host"), Config.get().getInt("port"));
				if (name != null && name.length() > 0) {
					trackName = name;
				} else {
					return;
				}
			}
                        if(!trackName.endsWith(".ntr")) {
                            trackName = trackName.concat(".ntr");
                        }
			track.save(trackName, Config.get().getString("host"), Config.get().getInt("port"));
		} catch (Exception ex) {
			//ex.printStackTrace();
			errorMsg(ex.getLocalizedMessage());
		}
	}

	/**
	 * Nastaví trať do všech částí systému.
	 * @param track
	 */
	public void setTrack(Track track) {
		this.track = track;
		//TrackTab.get().setTrack(track);
		TrackEditorTab.get().setTrack(track);
		TestTab.get().setTrack(track, trackName);
		this.getContentPane().validate();
	}

	/**
	 * Vytvoří plochu pro novou trať.
	 */
	public void newTrack() {
		trackFile = null;
		trackName = null;
		Track newTrack = TrackPropertiesDialog.showDialog(this, null);
		if (newTrack != null) {
			setTrack(newTrack);
			setStatus(Status.NEWTRACK);
		}
	}

	/**
	 * Zobrazí chybovou zprávu.
	 * @param msg text zprávy
	 */
	public void errorMsg(String msg) {
                msg = Text.translate(msg);
		JOptionPane.showMessageDialog(this, msg, Text.getString("error"), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Nastaví stav.
	 * @param status
	 */
	public final void setStatus(Status status) {
		this.status = status;
		TrackEditorTab.get().setStatus(status);
		TestTab.get().setStatus(status);
	}

	/**
	 * Vrátí aktuální stav.
	 * @return aktuální stav.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Vrátí instanci MainWindow
	 * @return instance MainWindow
	 */
	public static MainWindow get() {
		if (mainWindow == null) {
			mainWindow = new MainWindow();
		}
		return mainWindow;
	}
        
        /**
	 * Otevře nové okno a zavře původní
	 */
	public static void set(MainWindow window) {
                mainWindow.dispose();
		mainWindow = window;
	}

	/**
	 * Spustí aplikaci s GUI.
	 * @param args
	 */
	public static void main(String[] args) {
		mainWindow = new MainWindow();
                TrackEditorTab.get().loadObjectTypes();
	}

	/**
	 * Aktualizuje všechny prvky podle nastavené LookAndFeel
	 */
	public void updateComponentTreeUI() {
		SwingUtilities.updateComponentTreeUI(this);
		fileTrackChooser.updateUI();
		getContentPane().validate();
		ServerTab.get().updateComponentUI();
	}

        
        public void setPanel(JPanel c) {
            tabbedPane.setSelectedComponent(c);
        }
}
