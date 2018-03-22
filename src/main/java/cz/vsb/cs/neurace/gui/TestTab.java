package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.ResourceManager;
import cz.vsb.cs.neurace.gui.MainWindow.Status;
import cz.vsb.cs.neurace.gui.track.RaceView;
import cz.vsb.cs.neurace.gui.track.VisualCar;
import cz.vsb.cs.neurace.race.Car;
import cz.vsb.cs.neurace.race.Race;
import cz.vsb.cs.neurace.race.RaceListener;
import cz.vsb.cs.neurace.race.RaceTrack;
import cz.vsb.cs.neurace.server.TestServer;
import cz.vsb.cs.neurace.track.Track;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;


/**
 * Panel s testovacím prostředím.
 */
public class TestTab implements ActionListener, RaceListener, ToggleListener {

    

	public enum TestStatus {
		/** klient nepřipojen, test neběží */
		DISCONNECT, 
		/** klient připojen, ale test neběží */
		CONNECT, 
		/** klient připojen a test běží */
		RUN
	}
	/** tlačítka pro menu */
	private JButton buttonOpen, buttonConnect,  button3D, buttonRun,  buttonStep,  buttonRestart, buttonDisconect;
	private JButton buttonCopyElement;
	private JToggleButton buttonCamera, buttonOptions;
        private JPopupMenu openMenu;
        /** instance */
	private static TestTab testTab;
	/**panel s GUI prvky */
	private JPanel panel;
	/** scroll panel pro trať */
	private JScrollPane scrollPanel;
	/** trať */
	private RaceView raceView;
        /** okno se 3D zobrazením*/
        private PhysicsDisplay display;

	/** testovaní závod */
	private Race race;
	/** auto testovaního klienta */
	private ArrayList<Car> cars;
	/** panel s informacemi */
	private RaceInfoPanel infoPanel;
	/** stav testu */
	private TestStatus status = TestStatus.DISCONNECT;
        /** testovací server*/
        private TestServer server;
        
        private RaceOptionsPanel optionsPanel;
        private ImageIcon start, pause;


	/**
	 * Konstruktor vytvoří panel.
	 */
	private TestTab() {
		panel = new JPanel(new BorderLayout());
		panel.add(makeMenuTest(), BorderLayout.PAGE_START);
                this.raceView = new RaceView(null);
                raceView.setToggleListener(this);
                cars = new ArrayList<Car>();
		infoPanel = new RaceInfoPanel("test", 999, raceView);
		panel.add(infoPanel, BorderLayout.LINE_START);
                
	}

	/**
	 * Panel s ovladacími prvky pro menu testu
	 * 
	 * @return ovladací prvky
	 */
	protected final JComponent makeMenuTest() {
		JPanel panelMenu = new JPanel();
		panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.X_AXIS));

		buttonOpen = new JButton(ResourceManager.getImageIcon("load_local.png"));
		buttonOpen.setToolTipText(Text.getString("open"));
                openMenu = TrackEditorTab.createOpenMenu();
		buttonOpen.addActionListener(this);
		panelMenu.add(buttonOpen);
                
                buttonConnect = new JButton(ResourceManager.getImageIcon("connect_client.png"));
		buttonConnect.setToolTipText(Text.getString("start_test_server"));
		buttonConnect.addActionListener(this);
		panelMenu.add(buttonConnect);

                button3D = new JButton(ResourceManager.getImageIcon("3D.png"));
		button3D.setToolTipText(Text.getString("3D"));
		button3D.addActionListener(this);
		panelMenu.add(button3D);

                buttonCamera = new JToggleButton(ResourceManager.getImageIcon("camera.png"));
		buttonCamera.setToolTipText(Text.getString("camera"));
                buttonCamera.setSelected(true);
		buttonCamera.addActionListener(this);
		panelMenu.add(buttonCamera);
                
                buttonOptions = new JToggleButton(ResourceManager.getImageIcon("config_track.png"));
		buttonOptions.setToolTipText(Text.getString("view_options"));
                buttonOptions.setSelected(false);
		buttonOptions.addActionListener(this);
		panelMenu.add(buttonOptions);

		buttonRun = new JButton(ResourceManager.getImageIcon("start.png"));
		buttonRun.setToolTipText(Text.getString("start"));
		buttonRun.addActionListener(this);
                start = ResourceManager.getImageIcon("start.png");
                pause = ResourceManager.getImageIcon("pause.png");
		panelMenu.add(buttonRun);


		buttonStep = new JButton(ResourceManager.getImageIcon("step.png"));
		buttonStep.setToolTipText(Text.getString("step"));
		buttonStep.addActionListener(this);
		panelMenu.add(buttonStep);

                buttonRestart = new JButton(ResourceManager.getImageIcon("restart.png"));
		buttonRestart.setToolTipText(Text.getString("restart"));
		buttonRestart.addActionListener(this);
		panelMenu.add(buttonRestart);

		buttonDisconect = new JButton(ResourceManager.getImageIcon("disconnect.png"));
		buttonDisconect.setToolTipText(Text.getString("disconnect"));
		buttonDisconect.addActionListener(this);
		panelMenu.add(buttonDisconect);

		buttonCopyElement = new JButton("Copy");
		buttonCopyElement.setToolTipText("Copy input to clipboard");
		buttonCopyElement.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(cars.size() > 0){
					StringBuilder sb = new StringBuilder(255);
					sb.append("angle:").append(cars.get(0).getClientAngle()).append("\n");
					sb.append("speed:").append(cars.get(0).getClientSpeed()).append("\n");
					sb.append("distance0:").append(cars.get(0).getClientDistance0()).append("\n");
					sb.append("distance4:").append(cars.get(0).getClientDistance4()).append("\n");
					sb.append("distance8:").append(cars.get(0).getClientDistance8()).append("\n");
					sb.append("distance16:").append(cars.get(0).getClientDistance16()).append("\n");
					sb.append("distance32:").append(cars.get(0).getClientDistance32()).append("\n");
					sb.append("friction:").append(cars.get(0).getClientFriction()).append("\n");
					sb.append("skid:").append(cars.get(0).getClientSkid()).append("\n");
					sb.append("checkpoint:").append(cars.get(0).getClientCheckpoint()).append("\n");
					sb.append("sensorFrontLeft:").append(cars.get(0).getSensors().get("sensorFrontLeft")).append("\n");
					sb.append("sensorFrontMiddleLeft:").append(cars.get(0).getSensors().get("sensorFrontMiddleLeft")).append("\n");
					sb.append("sensorFrontMiddleRight:").append(cars.get(0).getSensors().get("sensorFrontMiddleRight")).append("\n");
					sb.append("sensorFrontRight:").append(cars.get(0).getSensors().get("sensorFrontRight")).append("\n");
					sb.append("sensorFrontRightCorner1:").append(cars.get(0).getSensors().get("sensorFrontRightCorner1")).append("\n");
					sb.append("sensorFrontRightCorner2:").append(cars.get(0).getSensors().get("sensorFrontRightCorner2")).append("\n");
					sb.append("sensorRight1:").append(cars.get(0).getSensors().get("sensorRight1")).append("\n");
					sb.append("sensorRight2:").append(cars.get(0).getSensors().get("sensorRight2")).append("\n");
					sb.append("sensorRearRightCorner2:").append(cars.get(0).getSensors().get("sensorRearRightCorner2")).append("\n");
					sb.append("sensorRearRightCorner1:").append(cars.get(0).getSensors().get("sensorRearRightCorner1")).append("\n");
					sb.append("sensorRearRight:").append(cars.get(0).getSensors().get("sensorRearRight")).append("\n");
					sb.append("sensorRearLeft:").append(cars.get(0).getSensors().get("sensorRearLeft")).append("\n");
					sb.append("sensorRearLeftCorner1:").append(cars.get(0).getSensors().get("sensorRearLeftCorner1")).append("\n");
					sb.append("sensorRearLeftCorner2:").append(cars.get(0).getSensors().get("sensorRearLeftCorner2")).append("\n");
					sb.append("sensorLeft1:").append(cars.get(0).getSensors().get("sensorLeft1")).append("\n");
					sb.append("sensorLeft2:").append(cars.get(0).getSensors().get("sensorLeft2")).append("\n");
					sb.append("sensorFrontLeftCorner1:").append(cars.get(0).getSensors().get("sensorFrontLeftCorner1")).append("\n");
					sb.append("sensorFrontLeftCorner2:").append(cars.get(0).getSensors().get("sensorFrontLeftCorner2")).append("\n");
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
				}
			
			
			}
		});
		panelMenu.add(buttonCopyElement);
                
		return panelMenu;
                
                
	}

	/**
	 * Vrátí panel s prvky.
	 * @return
	 */
	public JPanel getPanel() {
		return panel;
	}

	/**
	 * Nastaví trať.
	 * @param track
	 */
	public void setTrack(Track track, String trackName) {
                if(scrollPanel == null) {
                    scrollPanel = new JScrollPane(this.raceView);
                    panel.add(scrollPanel, BorderLayout.CENTER);
                }
                
                this.raceView.setTrack(track);
                
                if(race != null) {
                    race.changeTrack(trackName, new RaceTrack(track), 999, 1, true);
                    if(status == TestStatus.RUN) {
                        setTestStatus(TestStatus.CONNECT);
                    }
                    infoPanel.updateCars(true);
                    raceView.update();
                }
	}


        @Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonConnect) {
			connect();
                }
                else if (e.getSource() == buttonOpen) {
			openMenu.show(buttonOpen, 0, buttonOpen.getHeight());       
		} else if (e.getSource() == buttonRun) {
                    if(status == TestStatus.CONNECT) {
			race.startRace();
			setTestStatus(TestStatus.RUN);
                    }
                    else {
                        race.pauseRace();
			setTestStatus(TestStatus.CONNECT);
                    }
		} else if (e.getSource() == buttonStep) {
			race.stepRace();
			setTestStatus(TestStatus.CONNECT);
                } else if (e.getSource() == buttonRestart) {
			race.restartRace();
			setTestStatus(TestStatus.CONNECT);
		} else if (e.getSource() == buttonDisconect) {
			disconnect();
                        if(this.display != null) {
                            display.close();
                        }
                } else if (e.getSource() == button3D) {
			createPhysicsDisplay();
                } else if(e.getSource() == buttonCamera) {
                    this.raceView.setFollowCar(buttonCamera.isSelected());
                } else if(e.getSource() == buttonOptions) {
                    if(buttonOptions.isSelected()) {
                        optionsPanel = new RaceOptionsPanel(raceView);
                        raceView.setOptionsPanel(optionsPanel);
                        optionsPanel.setToggleButtonListener(this);
                        panel.add(optionsPanel, BorderLayout.LINE_END);
                        panel.revalidate();
                        panel.repaint();
                    }
                    else {
                        panel.remove(optionsPanel);
                        optionsPanel.setToggleButtonListener(null);
                        raceView.setOptionsPanel(null);
                        panel.revalidate();
                        panel.repaint();
                    }
                    //updateEnabled();
               }
	}

        public void createPhysicsDisplay() {
            if(display == null) {
                display = new PhysicsDisplay(race.getPhysics().dynamicsWorld, race.getPhysics().cars, race.getFps());
                new Thread(display).start();
            }
            else if(display.isClosed()) {
                new Thread(display).start();
            }
        }

	/**
	 * Připojí klienta, spustí test.
	 */
	public void connect() {
		try {
                    server = new TestServer(Config.get().getInt("testPort"));
                    race = new Race(new RaceTrack(raceView.getTrack()));
                    race.addRaceListener(this);
                    TrackEditorTab.get().getTrackEdit().setPhysics(race.getPhysics());
                    setTestStatus(TestStatus.CONNECT);
			
		} catch (IOException e) {
			//e.printStackTrace();
			MainWindow.get().errorMsg(e.getLocalizedMessage());
		}
	}

	/**
	 * Odpojí klienta, ukončí test.
	 */
	public void disconnect() {
		race.stopRace();
                server.stopServer();
		setTestStatus(TestStatus.DISCONNECT);
		race = null;
                cars.clear();
                raceView.getCars().clear();
                TrackEditorTab.get().getTrackEdit().setPhysics(null);
		panel.repaint();
                infoPanel.updateCars(true);
	}

	/**
	 * Vrátí instanci.
	 * @return
	 */
	public static TestTab get() {
		if (testTab == null) {
			testTab = new TestTab();
		}
		return testTab;
	}


        @Override
	public void round() {
            int i = 0;
            List<Car> carsToRemove = new LinkedList<Car>();
            boolean changed = false;
            for(VisualCar visualCar: raceView.getCars()) {
                Car car = cars.get(i);
                if(car.isDisconnected()) {
                    carsToRemove.add(car);
                }
                else{
                    //infoPanel.setTime(car.getTime());
                    if(visualCar.getPosition() != car.getPosition()) {
                        changed = true;
                    }
                    visualCar.setValue("x", car.getSX());
                    visualCar.setValue("y", car.getSY());
                    visualCar.setValue("angle", car.getAngle());
                    visualCar.setValue("wheel", car.getWheel());
                    visualCar.setValue("speed", car.getSpeed());
                    visualCar.setValue("lap", car.getLap());
                    visualCar.setValue("position", car.getPosition());
                    visualCar.setValue("gear", car.getGear());
                    visualCar.setValue("rpm", car.getRpm());
                    visualCar.setValue("checkpoints", car.getCheckPointCount());
                    visualCar.setValue("carTime", car.getLapTime());
                    visualCar.setValue("clientDistance0", car.getClientDistance0());
                    visualCar.setValue("clientAngle", car.getClientAngle());
                    visualCar.setValue("clientSpeed", car.getClientSpeed());
                    visualCar.setValue("clientDistance4", car.getClientDistance4());
                    visualCar.setValue("clientDistance8", car.getClientDistance8());
                    visualCar.setValue("clientDistance16", car.getClientDistance16());
                    visualCar.setValue("clientDistance32", car.getClientDistance32());
                    visualCar.setValue("clientAcc", car.getClientAcc());
                    visualCar.setValue("clientWheel", car.getClientWheel());
                    
                    
                    visualCar.setValue("clientFriction", car.getClientFriction());
                    visualCar.setValue("clientSkid", car.getClientSkid());
                    for(int wheel = 0; wheel < 4; wheel++) {
                        String prop = "skidInfo" + wheel;
                        visualCar.setValue(prop, car.getSkidInfo(wheel));
                    }
                    visualCar.setValue("clientCheckpoint", car.getClientCheckpoint());
//                    visualCar.setValue("clientSensor1", car.getClientSensor1());
//                    visualCar.setValue("clientSensor2", car.getClientSensor2());
                    HashMap<String, Float> sensors = car.getSensors();
                    for(String key : sensors.keySet()){
                    	visualCar.setValue(key, sensors.get(key));
                    }
                }
                i++;
            }
            for(Car car: carsToRemove) {
                raceView.getCars().remove(cars.indexOf(car));
                cars.remove(car);
                changed = true;
            }

            infoPanel.updateCars(changed);
            raceView.update();
	}

	/**
	 * Provést změny po změně stavu.
	 * @param status
	 */
	public void setStatus(Status status) {
		if (status == Status.TRACKLOAD) {
			buttonConnect.setEnabled(this.status == TestStatus.DISCONNECT);
                        button3D.setEnabled(this.status != TestStatus.DISCONNECT);
                        buttonCamera.setEnabled(this.status != TestStatus.DISCONNECT);
                        buttonOptions.setEnabled(this.status != TestStatus.DISCONNECT);
			buttonRun.setEnabled(this.status == TestStatus.CONNECT);
			buttonStep.setEnabled(this.status == TestStatus.CONNECT);
                        buttonRestart.setEnabled(this.status == TestStatus.CONNECT);
			buttonDisconect.setEnabled(this.status != TestStatus.DISCONNECT);
		} else {
			buttonConnect.setEnabled(false);
                        button3D.setEnabled(false);
                        buttonCamera.setEnabled(false);
                        buttonOptions.setEnabled(false);
			buttonRun.setEnabled(false);
			buttonStep.setEnabled(false);
                        buttonRestart.setEnabled(false);
			buttonDisconect.setEnabled(false);
		}
	}

	/**
	 * Nastavit stav pro test.
	 * @param status
	 */
	public void setTestStatus(TestStatus status) {
		this.status = status;
                
		buttonConnect.setEnabled(status == TestStatus.DISCONNECT);
                button3D.setEnabled(status != TestStatus.DISCONNECT);
                buttonOptions.setEnabled(this.status != TestStatus.DISCONNECT);
                buttonCamera.setEnabled(status != TestStatus.DISCONNECT);
		buttonRun.setEnabled(status != TestStatus.DISCONNECT);
		buttonStep.setEnabled(status == TestStatus.CONNECT);
                buttonRestart.setEnabled(status == TestStatus.CONNECT);
		buttonDisconect.setEnabled(status != TestStatus.DISCONNECT);
                if(status == TestStatus.CONNECT) {
                    buttonRun.setIcon(start);
                    buttonRun.setToolTipText(Text.getString("start"));
                }
                else {
                    buttonRun.setIcon(pause);
                    buttonRun.setToolTipText(Text.getString("pause"));
                }
	}

        public ArrayList<Car> getCars() {
            return cars;
        }

        public RaceView getPaint() {
            return raceView;
        }

        public Race getRace() {
            return race;
        }

        /**
         * Přidá auto do závodu
         * @param car auto
         */
        public void addCar(Car car) {
            synchronized(this) {
                VisualCar visualCar = new VisualCar(car.getDriver().getDriverName(), car.getCarType(), cars.size()+1, getPaint());
                visualCar.setColor(car.getColor());
                visualCar.setX(car.getSX()*getPaint().getScale());
                visualCar.setY(car.getSY()*getPaint().getScale());
                visualCar.setAngle(car.getAngle());
                cars.add(car);
                getPaint().addCar(visualCar);
                infoPanel.updateCars(true);
            }
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
