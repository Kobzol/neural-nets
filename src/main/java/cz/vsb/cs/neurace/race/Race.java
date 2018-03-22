package cz.vsb.cs.neurace.race;

import cz.vsb.cs.neurace.connection.ClientSocket;
import cz.vsb.cs.neurace.gui.PhysicsDisplay;
import cz.vsb.cs.neurace.server.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * 
 * Závod. Závod probíhá ve vlastním vlákně.
 * 
 */
public class Race extends Thread {

	/** trať závodu */
	private RaceTrack track;
	/** auta v závodě */
	private Cars cars;
	/** diváci závodu */
	private Viewers viewers;
	/** název závodu */
	private String name;
	/** název tratě */
	private String trackName;
	/** počet kol závodu */
	private int laps;
	/** čas vytvoření závodu ms */
	private long createdTime;
	/** status závodu */
	private Status status;
        /** status závodu */
	private Command command;
	/** jak dlouho může existovat závod */
	private int raceTimeout = 4 * 60 *60;
        /** počet kroků simulace za sekundu */
        private int updatesPerSec = 60;
        /** čas v ms pro jeden pohyb auta */
	private int roundTime = 1000/updatesPerSec;
	/** zda se má zastavovat smyčka závodu po každém průchodu */
	private boolean paused;
	/** čas závodu od startu v ms */
	private int time = 0;
	/** Listenery pro závod */
	private List<RaceListener> raceListeners = new LinkedList<RaceListener>();
        /** Fyzikální model závodu*/
        private RacePhysics physics;

        /** šampionát */
        private Championship championship = null;
        /** nastavení kolizí */
        private boolean collisions = true;
        /** určuje, je to test */
        private boolean test = false;
        /** počet aut na startu závodu*/
        private int startCars = 0;
        /** určuje, jestli některý závodník dokončil závod */
        private boolean firstFinished = false;
        /** časový limit pro závod*/ 
        private int timeLimit = 60;
        /** zbývající čas do konce závodu */ 
        private float remainingTime = timeLimit;
        /** zprávy, které jsou na konci závodu zasílány klientům */
        private List messages = new LinkedList<String>();

	/**
	 * Stavy závodu.
	 */
	public enum Status {
		INITIATION, RACE, FINISH
	}

        /**
	 * Přikazy od pořadatele závodu.
	 */
	public enum Command {
		END, RESTART, CHANGE
	}

	/**
	 * Konstruktor závodu.
	 * 
	 * @param track trať, na které je závod
	 */
	public Race(RaceTrack track) {
		this("test", track, "", Integer.MAX_VALUE, 1, true, 60, 60);
		raceTimeout = Integer.MAX_VALUE / 1000;
                test = true;
	}

	/**
	 * Konstruktor závodu.
	 * 
	 * @param name název závodu
	 * @param track trať, na které je závod
	 * @param trackName název té trati
	 * @param laps počet okruhů závodu
	 */
	public Race(String name, RaceTrack track, String trackName, int laps, int races, boolean collisions, int timeLimit, int updatesPerSec) {
		super("race: " + name);
		this.track = track;
		this.name = name;
		this.trackName = trackName;
		this.laps = laps;
                this.updatesPerSec = updatesPerSec;
                this.roundTime = 1000/updatesPerSec;
                this.collisions = collisions;
                if(races != 1) {
                    this.championship = new Championship(races);
                }
                physics = new RacePhysics(roundTime*0.001f, (int)(60.0f/updatesPerSec));
                physics.setBounds(track.getTrack().getWidth(), track.getTrack().getHeight());
                physics.setCarCollisions(collisions);
                physics.createObjects(track.getTrack());
                
		cars = new Cars(this);
		viewers = new Viewers(this);

                createdTime = System.currentTimeMillis();
		status = Status.INITIATION;
                command = null;
		start();
	}

	/**
	 * Přidat prohlížeč
	 * 
	 * @param socket klient
	 * @throws IOException
	 */
	public void addViewer(ClientSocket socket) throws IOException {
		viewers.add(socket);
	}

	/**
	 * Přidat řidice
	 * 
	 * @param socket klient
	 * @throws IOException
	 */
	public Car addDriver(ClientSocket socket) throws IOException {
		Car car = cars.add(socket);
		if (car != null) {
			viewers.sendMessage(car.getInfo());
		}
		return car;
	}

	/**
	 * Vrátí trať závodu.
	 * @return trať závodu
	 */
	public RaceTrack getTrack() {
		return track;
	}

        /**
         * Vrátí názvy aut povolených v závodě.
         * @return názvy aut
         */
        public Collection<String> getAvailableCars() {
//            if(!test)
                return Garage.get().availableCars();
//            else
//                return Garage.get().availableCarsLocal();
	}

	@Override
	public void run() {
                raceLoop();

		viewers.stopRace();
		cars.finish();
		Races.getRaces().remove(this);
	}

	/**
	 * Smyčka pro závod. Probíhá komunikace s klienty a posunovani aut.
	 * 
	 */
	public void raceLoop() {

            while(command != Command.END) {
                
                while (status == Status.INITIATION) {
			synchronized (this) {
                            try {
                                    wait(1000);
                                    ArrayList<Car> carsToRemove = new ArrayList();
                                    for(Car car: cars.getCars()) {
                                        try {
                                            if(car.getDriver().isAlive()) {
                                                car.getDriver().getSocket().send();
                                            }
                                            else {
                                                carsToRemove.add(car);
                                            }
                                        }
                                        catch(IOException e) {
                                            carsToRemove.add(car);
                                        }
                                    }
                                    if(!carsToRemove.isEmpty()) {
                                        cars.removeCars(carsToRemove);
                                        if(collisions) {
                                            cars.reset();
                                            viewers.restartRace(cars.getInfo());
                                        }
                                        for (RaceListener rl : raceListeners) {
                                            rl.round();
                                        }
                                    }
                            } catch (InterruptedException e) {
                            }
                        }
			if (createdTime + raceTimeout * 1000 <= System.currentTimeMillis()) {
                            return;
			}
		}
                
                if(status == Status.RACE) {
                    startCars = cars.getCars().size();
                    viewers.startRace(startCars);
                }
                
                while (status == Status.RACE) {
			if (createdTime + raceTimeout * 1000L <= System.currentTimeMillis()) {
                            return;
			}
			try {
				long rstime = System.currentTimeMillis();
                                if (cars.allFinish() || (firstFinished && remainingTime <= 0)) {
                                    
                                    status = Status.FINISH;

                                    if(!test) {
                                        saveResults();
                                        if(championship != null) {
                                            viewers.sendScore(championship.getScoreboard(), messages);
                                            championship.finishRace();
                                            if(championship.getRaceNumber() > championship.getRaces()) {
                                                championship.reset();
                                            }
                                        }
                                        else {
                                            viewers.finish(messages);
                                        }
                                    }
				}
                                else {
                                    viewers.prepare();
                                    cars.move(roundTime);

                                    cars.communicate();
                                    viewers.communicate();
       
                                    for (RaceListener rl : raceListeners) {
                                            rl.round();
                                    }

                                    long sleep = roundTime - (System.currentTimeMillis() - rstime);
                                    if (sleep > 0L) {
                                            Thread.sleep(sleep);
                                    }

                                    time += roundTime;

                                    if(firstFinished) {
                                        remainingTime -= (roundTime * 0.001f);
                                    }


                                    if (paused) {
                                        synchronized (this) {
                                                this.wait();
                                        }
                                    }
                                }
                        } catch (InterruptedException e) {
                            System.err.printf(e.getMessage());
				//e.printStackTrace();
			}
		}
                while (status == Status.FINISH && command == null) {
                    synchronized (this) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                        }
                    }
                }
                if(command == Command.RESTART) {
                    status = Status.INITIATION;
                    resetTimers();
                    this.cars.reset();
                    startCars = cars.getCars().size();
                    if(!test) {
                        viewers.restartRace(cars.getInfo());
                    }
                    else {
                        for (RaceListener rl : raceListeners) {
                            rl.round();
                        }
                    }
                    command = null;
                }
                else if(command == Command.CHANGE) {
                    status = Status.INITIATION;
                    resetTimers();
                    this.cars.reset();
                    startCars = cars.getCars().size();
                    physics.destroyObjects();
                    physics.createObjects(track.getTrack());
                    physics.setBounds(track.getTrack().getWidth(), track.getTrack().getHeight());
                    physics.setCarCollisions(collisions);
                    int races = (championship == null) ? 1 : championship.getRaces();
                    if(!test) {
                        viewers.changeTrack(trackName, laps, races, cars.getInfo());
                    }
                    else {
                        for (RaceListener rl : raceListeners) {
                            rl.round();
                        }
                    }
                    command = null;
                }
            } //end of while(command != Command.END)
            physics.dynamicsWorld.destroy();
	} //end of raceloop

	/**
	 * Stav závodu.
	 * 
	 * @return stav závodu.
	 */
	public Status getStatus() {
		return status;
	}

        public boolean getStepping() {
		return paused;
	}

        public int getStartCars() {
            return startCars;
        }

        public void setStartCars(int startCars) {
            this.startCars = startCars;
        }
        
        public void setDistanceLimit() {
            for(Car car: cars.getCars()) {
                car.setDistanceLimit(track.getTrack().getRoadWidth()/2);
            }
        }
        
        
        

        private void resetTimers() {
            time = 0;
            firstFinished = false;
            remainingTime = timeLimit;
        }

	/**
	 * Odstartuje závod.
	 */
	public void startRace() {
		synchronized (this) {
			paused = false;
			status = Status.RACE;
			notify();
		}
	}

	/**
	 * Zastaví závod.
	 */
	public void stopRace() {
		synchronized (this) {
			command = Command.END;
                        status = Status.FINISH;
			notify();
		}
	}

	/**
	 * Udělá jeden krok závodu.
	 */
	public void stepRace() {
		synchronized (this) {
			paused = true;
			status = Status.RACE;
			notify();
		}
	}

	/**
	 * Pozastaví závod.
	 */
	public void pauseRace() {
		synchronized (this) {
			paused = true;
			status = Status.RACE;
			notify();
		}
	}

        /**
	 * Restartuje závod.
	 */
	public void restartRace() {
		synchronized (this) {
                        command = Command.RESTART;
                        status = Status.FINISH;
			notify();
		}
                
	}

        /**
	 * Restartuje závod.
	 */
	public void changeTrack(String trackName, RaceTrack track, int laps, int races, boolean collisions) {
		synchronized (this) {
                        this.trackName = trackName;
                        this.track = track;
                        this.laps = laps;
                        this.collisions = collisions;
                        if(races == 1) {
                            championship = null;
                        }
                        else if(races > 1 && championship == null)  {
                            championship = new Championship(races);
                        }
                        else if(races > 1 && championship != null)  {
                            championship.setRaces(races);
                            if(championship.getRaceNumber() > championship.getRaces()) {
                                championship.reset();
                            }
                        }
                        command = Command.CHANGE;
                        status = Status.FINISH;
			notify();
		}
	}

	/**
	 * název závodu
	 * @return název závodu
	 */
	public String getRaceName() {
		return name;
	}

	/**
	 * diváci závodu
	 * @return diváci závodu
	 */
	public Viewers getViewers() {
		return viewers;
	}

	/**
	 * auta v závodě
	 * @return auta v závodě
	 */
	public Cars getCars() {
		return cars;
	}

	/**
	 * název tratě
	 * @return název tratě
	 */
	public String getTrackName() {
		return trackName;
	}

	/**
	 * čas závodu od startu v ms
	 * @return čas závodu od startu v ms
	 */
	public int getTime() {
		return time;
	}

	/**
	 * počet kol závodu
	 * @return počet kol závodu
	 */
	public int getLaps() {
		return laps;
	}


	@Override
	public String toString() {
		return name + " (" + trackName + ")";
	}

	/**
	 * Přidání listenera
	 * @param listener nový listener
	 */
	public void addRaceListener(RaceListener listener) {
		raceListeners.add(listener);
	}

	/**
	 * Odstranění listenera
	 * @param listener listener pro odstranění
	 */
	public void removeRaceListener(RaceListener listener) {
		raceListeners.remove(listener);
	}

        /**
         * Fyzikální model závodu
         * @return Fyzikální model závodu
         */
        public RacePhysics getPhysics() {
            return physics;
        }

        public Championship getChampionship() {
            return championship;
        }




        private boolean saveResults() {
            boolean error = false;
            messages.clear();
            if(cars.getCars().isEmpty()) {
                return false;
            }

            ArrayList<TrackRecord> times = new ArrayList<TrackRecord>();
            for(Car car: cars.getCars()) {
                if(car.getBestLapTime() != Integer.MAX_VALUE) {
                    times.add(new TrackRecord(car.getDriver().getDriverName(), car.getCarType(), car.getBestLapTime()));
                }
            }

            ArrayList<RacesRecord> drivers = new ArrayList<RacesRecord>();
            for(Car car: cars.getCars()) {
                boolean finished = (car.getLap() > laps);
                if(finished) {
                    int pos = car.getPosition();
                    int points = startCars - pos;
                    if(pos == 1) {
                        points += 2;
                    } else if(pos == 2)  {
                        points += 1;
                    }
                    if(this.championship != null) {
                        championship.addPoints(car.getDriver().getDriverName(), points);
                    }
                    
                    if(startCars > 2) {
                        int gold = 0, silver = 0, bronze = 0;
                        if(pos == 1)
                            gold = 1;
                        else if(pos == 2)
                            silver = 1;
                        else if(pos == 3)
                            bronze = 1;
                        drivers.add(new RacesRecord(car.getDriver().getDriverName(), 1, gold, silver, bronze, points));
                    }
                }
            }
                
            try {
                Database db = new Database();
                boolean ok = db.connect();
                if(!ok) {
                    System.err.println(db.getError());
                    messages.add("db error");
                    return false;
                }
                ok = db.updateTimes(trackName.replace(".ntr", ""), times);
                if(!ok) {
                    System.err.println(db.getError());
                    messages.add("db error");
                    error = true;
                }
                else {
                    messages.addAll(db.getMessages());
                }
                
                
                if(startCars > 2) {
                    ok = db.updateDrivers(drivers);
                    if(!ok) {
                        System.err.println(db.getError());
                        messages.add("db error");
                        error = true;
                    }
                }

                db.disconnect();
            }
            catch(Exception e) {
                //e.printStackTrace();
                error = true;
            }
            return error;
        }

        public boolean hasCollisions() {
            return collisions;
        }

        public int getFps() {
            return updatesPerSec;
        }

        public boolean isTest() {
            return test;
        }

        
        public boolean isFirstFinished() {
            return firstFinished;
        }



        public void setFirstFinished(boolean firstFinished) {
            this.firstFinished = firstFinished;
        }


        public float getRemainingTime() {
            return remainingTime;
        }














}
