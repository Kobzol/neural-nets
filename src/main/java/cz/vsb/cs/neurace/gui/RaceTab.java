package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.ResourceManager;
import cz.vsb.cs.neurace.connection.ClientRequest;
import cz.vsb.cs.neurace.gui.track.RaceStatusListener;
import cz.vsb.cs.neurace.gui.track.ShowRace;
import cz.vsb.cs.neurace.race.Race.Status;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * Panel s prohlížečem závodů.
 */
public final class RaceTab implements ActionListener, RaceStatusListener, ToggleListener {

	private static RaceTab raceTab;
	/** tlačítka pro menu */
	private JButton buttonConnect,  buttonDisconnect,  buttonNew, buttonChange, buttonStart, buttonPause,  buttonStep,  buttonRestart;
        private JToggleButton buttonCamera, buttonOptions;
	/** panel s GUI prvky */
	private JPanel panel;
        private JPanel menuPanel;
        private RaceOptionsPanel optionsPanel;
	/** probíhající závod */
	private ShowRace showRace;
        private boolean paused;
        private boolean organizer;
        private ImageIcon start, pause;

	/**
	 * Konstruktor vytvoří panel.
	 */
	private RaceTab() {
		panel = new JPanel(new BorderLayout());
                menuPanel = makeMenuRace();
		panel.add(menuPanel, BorderLayout.PAGE_START);
		updateEnabled();
	}

	/**
	 * Vytvoří panel s ovladacími prvky pro menu race
	 * @return panel s ovladacími prvky
	 */
	protected JPanel makeMenuRace() {
                if(menuPanel == null) {
                    menuPanel = new JPanel();
                }
                else {
                    menuPanel.removeAll();
                }
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));

		buttonNew = new JButton(Text.getString("new_race"), ResourceManager.getImageIcon("new.png"));
		buttonNew.setToolTipText(Text.getString("new_race"));
		buttonNew.addActionListener(this);
		menuPanel.add(buttonNew);

                buttonConnect = new JButton(Text.getString("connect"), ResourceManager.getImageIcon("connect_server.png"));
		buttonConnect.setToolTipText(Text.getString("connect"));
		buttonConnect.addActionListener(this);
		menuPanel.add(buttonConnect);

		return menuPanel;
	}
        
        /**
	 * Vytvoří panel s ovladacími prvky pro diváka
	 * @return panel s ovladacími prvky
	 */
	protected JPanel makeMenuViewer() {
                menuPanel.removeAll();
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));

                buttonCamera = new JToggleButton(ResourceManager.getImageIcon("camera.png"));
		buttonCamera.setToolTipText(Text.getString("camera"));
                buttonCamera.setSelected(true);
		buttonCamera.addActionListener(this);
		menuPanel.add(buttonCamera);
                
                buttonOptions = new JToggleButton(ResourceManager.getImageIcon("config_track.png"));
		buttonOptions.setToolTipText(Text.getString("view_options"));
                buttonOptions.setSelected(false);
		buttonOptions.addActionListener(this);
		menuPanel.add(buttonOptions);

		buttonDisconnect = new JButton(ResourceManager.getImageIcon("disconnect.png"));
		buttonDisconnect.setToolTipText(Text.getString("disconnect"));
		buttonDisconnect.addActionListener(this);
		menuPanel.add(buttonDisconnect);

		return menuPanel;
	}
        
        /**
	 * Vytvoří panel s ovladacími prvky pro pořadatele
	 * @return panel s ovladacími prvky
	 */
	protected JPanel makeMenuOrganizer() {
                menuPanel.removeAll();
                menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));
                
		buttonCamera = new JToggleButton(ResourceManager.getImageIcon("camera.png"));
		buttonCamera.setToolTipText(Text.getString("camera"));
                buttonCamera.setSelected(true);
		buttonCamera.addActionListener(this);
		menuPanel.add(buttonCamera);
                
                buttonOptions = new JToggleButton(ResourceManager.getImageIcon("config_track.png"));
		buttonOptions.setToolTipText(Text.getString("view_options"));
                buttonOptions.setSelected(false);
		buttonOptions.addActionListener(this);
		menuPanel.add(buttonOptions);

                buttonChange = new JButton(ResourceManager.getImageIcon("chequered_flag.png"));
		buttonChange.setToolTipText(Text.getString("change_track"));
		buttonChange.addActionListener(this);
		menuPanel.add(buttonChange);

                start = ResourceManager.getImageIcon("start.png");
                pause = ResourceManager.getImageIcon("pause.png");
		buttonStart = new JButton(start);
		buttonStart.setToolTipText(Text.getString("start"));
		buttonStart.addActionListener(this);
		menuPanel.add(buttonStart);

		buttonStep = new JButton(ResourceManager.getImageIcon("step.png"));
		buttonStep.setToolTipText(Text.getString("step"));
		buttonStep.addActionListener(this);
		menuPanel.add(buttonStep);

		buttonRestart = new JButton(ResourceManager.getImageIcon("restart.png"));
		buttonRestart.setToolTipText(Text.getString("restart"));
		buttonRestart.addActionListener(this);
		menuPanel.add(buttonRestart);

                

		buttonDisconnect = new JButton(ResourceManager.getImageIcon("disconnect.png"));
		buttonDisconnect.setToolTipText(Text.getString("stop_race_title"));
		buttonDisconnect.addActionListener(this);
		menuPanel.add(buttonDisconnect);

		return menuPanel;
	}

	/**
	 * Vrátí panel s prvky.
	 * @return
	 */
	public JPanel getPanel() {
		return panel;
	}

	/**
	 * Vrátí instanci.
	 * @return
	 */
	public static RaceTab get() {
		if (raceTab == null) {
			raceTab = new RaceTab();
		}
		return raceTab;
	}

        @Override
	public void actionPerformed(ActionEvent e) {
		try {
                    if (e.getSource() == buttonConnect) {
                        connectRace();
                    } else if (e.getSource() == buttonDisconnect) {
                            disconnectRace();
                    } else if (e.getSource() == buttonNew) {
                            newRace();
                    } else if (e.getSource() == buttonStart) {
                            if(paused) {
                                showRace.startRace();
                                this.paused = false;
                            }
                            else {
                                showRace.pauseRace();
                                this.paused = true;
                            }
                            updateEnabled();
                    } else if (e.getSource() == buttonPause) {
                            showRace.pauseRace();
                            this.paused = true;
                            updateEnabled();
                    } else if (e.getSource() == buttonStep) {
                            showRace.stepRace();
                            this.paused = true;
                            updateEnabled();
                    } else if (e.getSource() == buttonRestart) {
                            showRace.restartRace();
                            this.paused = true;
                            updateEnabled();
                    } else if (e.getSource() == buttonChange) {
                            changeTrack();
                            this.paused = true;
                            //updateEnabled();
                    } else if(e.getSource() == buttonCamera) {
                            this.showRace.followCar(buttonCamera.isSelected());
                            //updateEnabled();
                    
                    } else if(e.getSource() == buttonOptions) {
                            if(buttonOptions.isSelected()) {
                                optionsPanel = new RaceOptionsPanel(showRace.getRacePaint());
                                showRace.getRacePaint().setOptionsPanel(optionsPanel);
                                optionsPanel.setToggleButtonListener(this);
                                panel.add(optionsPanel, BorderLayout.LINE_END);
                                panel.revalidate();
                                panel.repaint();
                            }
                            else {
                               panel.remove(optionsPanel);
                               optionsPanel.setToggleButtonListener(null);
                               showRace.getRacePaint().setOptionsPanel(null);
                               panel.revalidate();
                               panel.repaint();
                            }
                            //updateEnabled();
                    }
                        
		} catch (Exception ex) {
			//ex.printStackTrace();
			MainWindow.get().errorMsg(ex.getMessage());
		}
	}

	/**
	 * Vutvoří nový závod.
	 */
	public void newRace() {
		try {
			if (NewRaceDialog.showDialog(MainWindow.get(), Config.get().getString("host"),
                                Config.get().getInt("port"), true) == NewRaceDialog.OK) {
				ClientRequest request = new ClientRequest("racenew", Config.get().getString("host"), Config.get().getInt("port"));
				request.write("race", NewRaceDialog.race);
				request.write("track", NewRaceDialog.track);
				request.write("laps", NewRaceDialog.laps);
                                request.write("races", NewRaceDialog.races);
                                request.write("collisions", String.valueOf(NewRaceDialog.collisions));
				request.send();
				request.check(true);
				request.close();

				showRace = new ShowRace(NewRaceDialog.race, Config.get().getString("host"), Config.get().getInt("port"));
				showRace.addRaceStatusListener(this);
                                showRace.getRacePaint().setToggleListener(this);
				panel.add(showRace.getPanel(), BorderLayout.CENTER);
                                this.paused = true;
                                this.organizer = true;
                                makeMenuOrganizer();
                                
                                menuPanel.repaint();
				updateEnabled();
                                showRace.getRacePaint().moveToStart();
			}
		} catch (Exception ex) {
			//ex.printStackTrace();
                        
			MainWindow.get().errorMsg(ex.getMessage());
		}
	}


	/**
	 * Změní trať.
	 */
	public void changeTrack() {
		try {
			if (NewRaceDialog.showDialog(MainWindow.get(), Config.get().getString("host"),
                                Config.get().getInt("port"), false) == NewRaceDialog.OK) {
                            this.showRace.changeRequest(NewRaceDialog.track, NewRaceDialog.laps, NewRaceDialog.races, NewRaceDialog.collisions);
			}
		} catch (Exception ex) {
			//ex.printStackTrace();
			MainWindow.get().errorMsg(ex.getMessage());
		}
	}

	/**
	 * Připojí se k závodu.
	 */
	public void connectRace() {
		try {
			String race = ConnectRaceDialog.showDialog(MainWindow.get(), Config.get().getString("host"), Config.get().getInt("port"));
			if (race != null) {
				showRace = new ShowRace(race, Config.get().getString("host"), Config.get().getInt("port"));
				showRace.addRaceStatusListener(this);
                                showRace.getRacePaint().setToggleListener(this);
				this.panel.add(showRace.getPanel(), BorderLayout.CENTER);
                                this.organizer = false;
                                makeMenuViewer();
                                showRace.getRacePaint().moveToStart();
                                menuPanel.repaint();
			}
		} catch (Exception ex) {
			//ex.printStackTrace();
			MainWindow.get().errorMsg(ex.getMessage());
		}
	}

	/**
	 * Odstartuje závod.
	 */
	/*public void startRace() {
		try {
			showRace.startRace();
		} catch (Exception ex) {
			ex.printStackTrace();
			MainWindow.get().errorMsg(ex.getLocalizedMessage());
		}
	}*/

	/**
	 * Ukončí závod.
	 */
	/*public void stopRace() {
		try {
			showRace.stopRace();
		} catch (Exception ex) {
			ex.printStackTrace();
			MainWindow.get().errorMsg(ex.getLocalizedMessage());
		}
	}*/

	/**
	 * Odpojí se od závodu.
	 */
	public void disconnectRace() {
		try {
			if(organizer) {
                            int n = JOptionPane.showConfirmDialog(MainWindow.get(),
                                    Text.getString("stop_race_question"), Text.getString("stop_race_title"), 
                                    JOptionPane.YES_NO_OPTION);
                            if(n == JOptionPane.YES_OPTION) {
                                showRace.stopRace();
                                panel.remove(showRace.getPanel());
                                showRace = null;
                                makeMenuRace();
                                panel.repaint();
                            }
                        }
                        else {
                            showRace.disconnect();
                            panel.remove(showRace.getPanel());
                            showRace = null;
                            makeMenuRace();
                            panel.repaint();
                        }

		} catch (Exception ex) {
			MainWindow.get().errorMsg(ex.getMessage());
                        panel.remove(showRace.getPanel());
                        showRace = null;
                        makeMenuRace();
                        panel.repaint();
		}
	}

	/**
	 * Aktualizuje nastavení tlačítek.
	 */
	public final void updateEnabled() {
                Status s;
                if(showRace == null) {
                   buttonConnect.setEnabled(true);
                   buttonNew.setEnabled(true);
                }
                else {
                    buttonCamera.setEnabled(true);
                    buttonDisconnect.setEnabled(true);
                    if(organizer) {
                        s = this.showRace.getStatus();
                        buttonStart.setEnabled(s != Status.FINISH);
                        buttonStep.setEnabled(this.paused && s != Status.FINISH);
                        buttonRestart.setEnabled(this.paused && s != Status.INITIATION);
                        buttonChange.setEnabled(this.paused);

                        if(paused) {
                            buttonStart.setIcon(start);
                            buttonStart.setToolTipText(Text.getString("start"));
                        }
                        else {
                            buttonStart.setIcon(pause);
                            buttonStart.setToolTipText(Text.getString("pause"));
                        }
                    }

                }

                
		
                

	}

        @Override
	public void changeRaceStatus(Status status) {
                if(status == Status.FINISH)
                    this.paused = true;
		updateEnabled();
	}

        @Override
        public void toggleButton(ButtonType type) {
            if(type == ToggleListener.ButtonType.FOLLOW) {
                buttonCamera.setSelected(!buttonCamera.isSelected());
            }
            else if(type == ToggleListener.ButtonType.OPTIONS) {
                buttonOptions.setSelected(!buttonOptions.isSelected());
            }
        }
}
