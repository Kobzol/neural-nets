package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.gui.track.RaceView;
import cz.vsb.cs.neurace.gui.track.VisualCar;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Panel s informacemi o průběhu závodu
 */
public class RaceInfoPanel extends JPanel implements ListSelectionListener {

	private RaceView raceShow;
	private JList list;
	private DefaultListModel listModel;
	/** pole pro hodnoty */
	private JLabel position, carLap, carCheck, carTime, speed, gear, rpm;
	private JLabel clientDistance0, clientDistance4, clientDistance8, clientDistance16, clientDistance32,
			clientAngle, clientSpeed, clientAcc, clientWheel, clientFriction, clientSkid,
			clientCheckpoint,/* clientSensor1, clientSensor2,*/ sensorsFront, sensorsFrontCorner, sensorsSide, sensorsRearCorner, sensorsRear;
	private JLabel raceName, time, laps;

	/**
	 * Vytvoří info panel.
	 * 
	 * @param name
	 * @param laps
	 * @param raceShow
	 */
	public RaceInfoPanel(String name, int laps, RaceView raceShow) {
		super(new BorderLayout(5, 5));
		this.raceShow = raceShow;

		JPanel infoPanel = new JPanel(new SpringLayout());
		add(infoPanel, BorderLayout.PAGE_START);

		infoPanel.add(new JLabel(Text.getString("race2"), JLabel.TRAILING));
		raceName = new JLabel(name);
		infoPanel.add(raceName);

		infoPanel.add(new JLabel(Text.getString("laps"), JLabel.TRAILING));
		this.laps = new JLabel(String.valueOf(laps));
		infoPanel.add(this.laps);

		infoPanel.add(new JLabel(Text.getString("time"), JLabel.TRAILING));
		time = new JLabel();
		infoPanel.add(time);

		SpringUtilities.makeCompactGrid(infoPanel, 3, 2, 6, 6, 6, 6);

		listModel = new DefaultListModel();
		list = new JList(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(this);
		JScrollPane listScroller = new JScrollPane(list);
		add(listScroller, BorderLayout.CENTER);

		JPanel detailPanel = new JPanel(new SpringLayout());
		add(detailPanel, BorderLayout.PAGE_END);

		detailPanel
				.add(new JLabel(Text.getString("position"), JLabel.TRAILING));
		position = new JLabel();
		detailPanel.add(position);

		detailPanel.add(new JLabel(Text.getString("lap"), JLabel.TRAILING));
		carLap = new JLabel();
		detailPanel.add(carLap);

		detailPanel.add(new JLabel(Text.getString("checkpoints"),
				JLabel.TRAILING));
		carCheck = new JLabel();
		detailPanel.add(carCheck);

		detailPanel.add(new JLabel(Text.getString("time"), JLabel.TRAILING));
		carTime = new JLabel();
		detailPanel.add(carTime);

		detailPanel
				.add(new JLabel(Text.getString("speed_kmh"), JLabel.TRAILING));
		speed = new JLabel();
		detailPanel.add(speed);

		/*
		 * detailPanel.add(new JLabel(Text.getString("gear"), JLabel.TRAILING));
		 * gear = new JLabel(); detailPanel.add(gear);
		 * 
		 * detailPanel.add(new JLabel(Text.getString("rpm"), JLabel.TRAILING));
		 * rpm = new JLabel(); detailPanel.add(rpm);
		 */

		detailPanel.add(new JLabel(Text.getString("distance0"), JLabel.TRAILING));
		clientDistance0 = new JLabel();
		detailPanel.add(clientDistance0);
		detailPanel.add(new JLabel(Text.getString("distance4"), JLabel.TRAILING));
		clientDistance4 = new JLabel();
		detailPanel.add(clientDistance4);
		detailPanel.add(new JLabel(Text.getString("distance8"), JLabel.TRAILING));
		clientDistance8 = new JLabel();
		detailPanel.add(clientDistance8);
		detailPanel.add(new JLabel(Text.getString("distance16"), JLabel.TRAILING));
		clientDistance16 = new JLabel();
		detailPanel.add(clientDistance16);
		detailPanel.add(new JLabel(Text.getString("distance32"), JLabel.TRAILING));
		clientDistance32 = new JLabel();
		detailPanel.add(clientDistance32);

		detailPanel.add(new JLabel(Text.getString("angle"), JLabel.TRAILING));
		clientAngle = new JLabel();
		detailPanel.add(clientAngle);

		detailPanel.add(new JLabel(Text.getString("speed"), JLabel.TRAILING));
		clientSpeed = new JLabel();
		detailPanel.add(clientSpeed);

		detailPanel
				.add(new JLabel(Text.getString("friction"), JLabel.TRAILING));
		clientFriction = new JLabel();
		detailPanel.add(clientFriction);

		detailPanel.add(new JLabel(Text.getString("skid"), JLabel.TRAILING));
		clientSkid = new JLabel();
		detailPanel.add(clientSkid);

		detailPanel.add(new JLabel(Text.getString("checkpoint"),
				JLabel.TRAILING));
		clientCheckpoint = new JLabel();
		detailPanel.add(clientCheckpoint);

//		detailPanel.add(new JLabel(Text.getString("sensor1"), JLabel.TRAILING));
//		clientSensor1 = new JLabel();
//		detailPanel.add(clientSensor1);
//
//		detailPanel.add(new JLabel(Text.getString("sensor2"), JLabel.TRAILING));
//		clientSensor2 = new JLabel();
//		detailPanel.add(clientSensor2);

		detailPanel.add(new JLabel("sensorsFront", JLabel.TRAILING));
		sensorsFront = new JLabel();
		detailPanel.add(sensorsFront);
		
		detailPanel.add(new JLabel("sensorsFrontCorner", JLabel.TRAILING));
		sensorsFrontCorner = new JLabel("0.00  0.00  0.00  0.00");
		detailPanel.add(sensorsFrontCorner);
		
		detailPanel.add(new JLabel("sensorsSide", JLabel.TRAILING));
		sensorsSide = new JLabel();
		detailPanel.add(sensorsSide);
		
		detailPanel.add(new JLabel("sensorsRearCorner", JLabel.TRAILING));
		sensorsRearCorner = new JLabel();
		detailPanel.add(sensorsRearCorner);
		
		detailPanel.add(new JLabel("sensorsRear", JLabel.TRAILING));
		sensorsRear = new JLabel();
		detailPanel.add(sensorsRear);
		
		detailPanel.add(new JLabel(Text.getString("acc"), JLabel.TRAILING));
		clientAcc = new JLabel();
		detailPanel.add(clientAcc);

		detailPanel.add(new JLabel(Text.getString("wheel"), JLabel.TRAILING));
		clientWheel = new JLabel();
		detailPanel.add(clientWheel);

		SpringUtilities.makeCompactGrid(detailPanel, 22, 2, 3, 6, 6, 6);

		setPreferredSize(new Dimension(250, 50));
	}

	/**
	 * Aktualizuje seznam aut.
	 */
	public void updateCars(boolean updateList) {
		if (updateList) {
			VisualCar[] cars = raceShow.getCarArray();
			Arrays.sort(cars);
			Object obj = list.getSelectedValue();
			listModel.clear();
			for (VisualCar c : cars) {
				listModel.addElement(c);
			}
			list.setSelectedValue(obj, true);
		}
		updateCar();
	}

	/**
	 * Obnoví vlastnosti o vybraném autu.
	 */
	public void updateCar() {
		VisualCar car = (VisualCar) list.getSelectedValue();
		if (car != null) {
			position.setText(String.valueOf(car.getPosition()));
			carLap.setText(String.valueOf(car.getLap()));
			carTime.setText(TimeUtil.genTime(car.getTime()));
			speed.setText(String.valueOf(car.getSpeed()));
			// gear.setText(String.valueOf(car.getGear()));
			// rpm.setText(String.valueOf(car.getRpm()));
			carCheck.setText(String.valueOf(car.getCheckpoints()));
			clientDistance0.setText(String.valueOf(car.getClientDistance0()));
			clientDistance4.setText(String.valueOf(car.getClientDistance4()));
			clientDistance8.setText(String.valueOf(car.getClientDistance8()));
			clientDistance16.setText(String.valueOf(car.getClientDistance16()));
			clientDistance32.setText(String.valueOf(car.getClientDistance32()));
			clientAngle.setText(String.valueOf(car.getClientAngle()));
			clientSpeed.setText(String.valueOf(car.getClientSpeed()));

			clientFriction.setText(String.valueOf(car.getClientFriction()));
			clientSkid.setText(String.valueOf(car.getClientSkid()));
			clientCheckpoint.setText(String.valueOf(car.getClientCheckpoint()));
			clientAcc.setText(String.valueOf(car.getClientAcc()));
			clientWheel.setText(String.valueOf(car.getClientWheel()));
//			clientSensor1.setText(String.valueOf(car.getClientSensor1()));
//			clientSensor2.setText(String.valueOf(car.getClientSensor2()));
			sensorsFront.setText(String.format("%1.2f  %1.2f  %1.2f  %1.2f", car.getSensorFrontLeft(), car.getSensorFrontMiddleLeft(), car.getSensorFrontMiddleRight(), car.getSensorFrontRight()));
			sensorsFrontCorner.setText(String.format("%1.2f  %1.2f  %1.2f  %1.2f", car.getSensorFrontLeftCorner2(), car.getSensorFrontLeftCorner1(), car.getSensorFrontRightCorner1(), car.getSensorFrontRightCorner2()));
			sensorsSide.setText(String.format("%1.2f  %1.2f  %1.2f  %1.2f", car.getSensorLeft1(), car.getSensorLeft2(), car.getSensorRight1(), car.getSensorRight2()));
			sensorsRearCorner.setText(String.format("%1.2f  %1.2f  %1.2f  %1.2f", car.getSensorRearLeftCorner2(), car.getSensorRearLeftCorner1(), car.getSensorRearRightCorner1(), car.getSensorRearRightCorner2()));
			sensorsRear.setText(String.format("%1.2f  %1.2f ", car.getSensorRearLeft(), car.getSensorRearRight()));
		}
	}

	/**
	 * Zobrazí čas.
	 * 
	 * @param time
	 *            čes v milisekundách.
	 */
	public void setTime(int time) {
		this.time.setText(TimeUtil.genTime(time));
	}

	/**
	 * Nastaví počet kol.
	 * 
	 * @param počet
	 *            kol
	 */
	public void setLaps(int laps) {
		this.laps.setText(String.valueOf(laps));
	}

	public void setRaceName(String name) {
		this.raceName.setText(name);
	}

	/**
	 * Změní číslo na řetězec nejméně o dvou znacích.
	 * 
	 * @param n
	 * @return
	 */
	private String oo(int n) {
		if (n < 10) {
			return "0" + n;
		} else {
			return String.valueOf(n);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		VisualCar car = (VisualCar) this.list.getSelectedValue();
		this.raceShow.setSelectedCar(car);
		updateCar();
		this.raceShow.update();
	}

	public VisualCar getSelectedCar() {
		return (VisualCar) this.list.getSelectedValue();
	}
}
