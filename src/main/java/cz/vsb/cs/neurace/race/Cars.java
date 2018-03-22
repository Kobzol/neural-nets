package cz.vsb.cs.neurace.race;

import cz.vsb.cs.neurace.connection.ClientSocket;
import cz.vsb.cs.neurace.race.Race.Status;
import cz.vsb.cs.neurace.server.Garage;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.vecmath.Point2f;

/**
 * Auta, které jsou v závodě.
 * 
 */
public class Cars {

	/** Auta v závodě */
	private List<Car> cars;
	/** Závod */
	private Race race;
	/** Zámek pro synchronizaci komunikaci s klienty */
	private Lock lock;

	/**
	 * Konstruktor
	 * 
	 * @param race
	 *            závod
	 */
	public Cars(Race race) {
		cars = new LinkedList<Car>();
		this.race = race;
		lock = new Lock();
	}

	/**
	 * Přidání nového auta.
	 * 
	 * @param socket
	 *            klient
	 * @return nové auto
	 * @throws IOException
	 */
	public Car add(ClientSocket socket) throws IOException {
		String name = socket.getString("driver");
		for (Car car : cars) {
			if (car.getDriver().getDriverName().equals(name)) {
				socket.error("driver exists");
				socket.send();
				socket.close();
				return null;
			}
		}
		if (race.getStatus() != Status.INITIATION && !race.isTest()) {
			socket.error("race run");
			socket.send();
			socket.close();
			return null;
		}

		String carType = socket.getString("car");
		if (carType == null) {
			carType = "Fabia";
		}

		PhysicalCar physical;
		if (!race.isTest()) {
			physical = Garage.get().getCar(carType);
		} else {
			physical = Garage.get().getCar(carType);
		}

		if (physical == null) {
			socket.error("car not available");
			socket.send();
			socket.close();
			return null;
		}

		RaceLine line = this.race.getTrack().calculateStartLine(cars.size(),
				race.hasCollisions());
		Point2f p = this.race.getTrack().calculateStartPos(cars.size(),
				race.hasCollisions());
		race.getPhysics().createCar(p.x, 0, p.y,
				-line.getTheta() + (float) Math.PI / 2, physical, name);

		Car car = new Car(carType, race, socket, cars.size() + 1, lock,
				physical);
		car.updateValues(0);
		cars.add(car);
		return car;
	}

	/**
	 * Začne komunikaci s klienty.
	 */
	public void communicate() {
		lock.resume();
	}

	/**
	 * Posun všech aut.
	 */
	public void move(float time) {
		// Auta která je potřeba vyřadit
		List<Car> carsToRemove = new LinkedList<Car>();

		for (Car car : cars) {
			if (car.getDriver().isAlive()) {
				car.setControls();
			} else if (!car.isFinish()) {
				carsToRemove.add(car);
			}
		}
		removeCars(carsToRemove);

		race.getPhysics().moveCars();

		for (Car car : cars) {
			car.updateValues(time);
		}

		updatePositions();
	}

	public void removeCars(List<Car> carsToRemove) {
		if (!carsToRemove.isEmpty()) {

			for (Car car : carsToRemove) {
				race.getViewers().removeDriver(car.getDriver().getDriverName());
				car.setDisconnected(true);
				race.getPhysics().destroyCar(car.getPhysical());
			}

			cars.removeAll(carsToRemove);
		}
	}

	public void updatePositions() {
		Car[] cs = cars.toArray(new Car[cars.size()]);
		Arrays.sort(cs);
		for (int i = 0; i < cs.length; i++) {
			cs[i].setPosition(i + 1);
		}
	}

	/**
	 * Vrátí auta na start a přidělí jim startovní čísla.
	 */
	public void reset() {
		updatePositions();

		for (Car car : cars) {
			car.reset();
		}
	}

	/**
	 * Vygeneruje informace všech aut pro nové pozorovatele.
	 */
	public String getInfo() {
		String s = "";
		for (Car car : cars) {
			s += car.getInfo();
		}
		return s;
	}

	/**
	 * Vygeneruje info všech aut.
	 */
	public void getInfo(StringBuffer sb) {
		for (Car car : cars) {
			car.getInfo(sb);
		}
	}

	/**
	 * Pošle informaci klientům.
	 */
	public void finish() {
		for (Car car : cars) {
			car.getDriver().disconnect();
		}
		lock.resume();
	}

	/**
	 * Zjistí, zda všechny auta ukončily závod
	 * 
	 * @return true pokud všechny auta ukončily závod, jinak false
	 */
	public boolean allFinish() {
		for (Car car : cars) {
			if (!car.isFinish()) {
				return false;
			}
		}
		return true;
	}

	public List<Car> getCars() {
		return cars;
	}

}
