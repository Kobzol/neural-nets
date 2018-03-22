package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.track.Surface;
import cz.vsb.cs.neurace.track.Track;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Dialog pro nastavení vlastností tratě.
 */
public class TrackPropertiesDialog extends JDialog implements ActionListener, CaretListener, ChangeListener,
		KeyListener {

	/** tlačítka */
	private JButton ok,  storno;
	/** nastanení */
	private JSpinner width,  height, roadWidth;
	/** nastanení */
	private SpinnerNumberModel widthModel, heightModel, roadWidthModel;

        private JComboBox enviroment, road;
	/** trať které se nastavují vlastnosi */
	private Track track;

	/**
	 * Konstruktor, vytvoří dialog.
	 * @param frame
	 * @param track
	 */
	private TrackPropertiesDialog(JFrame frame, Track track) {
		super(frame, Text.getString("track_properties"), true);

		this.track = track;
		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JPanel panelButtons = new JPanel(new BorderLayout());
		JPanel panelButtons2 = new JPanel(new GridLayout(0, 2, 3, 3));
		ok = new JButton("OK");
		ok.addActionListener(this);
		ok.addKeyListener(this);
		panelButtons2.add(ok);
		storno = new JButton("storno");
		storno.addActionListener(this);
		storno.addKeyListener(this);
		panelButtons2.add(storno);
		panelButtons.add(panelButtons2, BorderLayout.CENTER);

		JPanel middle = new JPanel(new SpringLayout());

		widthModel = new SpinnerNumberModel(this.track != null ? this.track.getWidth() : 300, 50, 1000, 10);

		JLabel l = new JLabel(Text.getString("Width"));
		middle.add(l);
		this.width = new JSpinner(widthModel);
		this.width.addChangeListener(this);
		this.width.addKeyListener(this);
		l.setLabelFor(this.width);
		middle.add(this.width);

		heightModel = new SpinnerNumberModel(this.track != null ? this.track.getHeight() : 300, 50, 1000, 10);

		l = new JLabel(Text.getString("Height"));
		middle.add(l);
		this.height = new JSpinner(heightModel);
		this.height.addChangeListener(this);
		this.height.addKeyListener(this);
		l.setLabelFor(this.height);
		middle.add(this.height);

                l = new JLabel(Text.getString("enviroment"));
                middle.add(l);
                this.enviroment = new JComboBox();
                this.enviroment.addItem(Text.getString("grass"));
                this.enviroment.addItem(Text.getString("sand"));
                this.enviroment.addItem(Text.getString("snow"));
                this.enviroment.addItem(Text.getString("concrete"));
                if(this.track != null)
                    this.enviroment.setSelectedItem(Text.getString(track.getEnviroment().toString().toLowerCase()));
                middle.add(enviroment);

                l = new JLabel(Text.getString("road_surface"));
                middle.add(l);
                this.road = new JComboBox();
                this.road.addItem(Text.getString("asphalt"));
                this.road.addItem(Text.getString("gravel"));
                this.road.addItem(Text.getString("dirt"));
                this.road.addItem(Text.getString("snow"));
                this.road.addItem(Text.getString("sand"));
                this.road.addItem(Text.getString("ice"));
                if(this.track != null)
                    this.road.setSelectedItem(Text.getString(track.getBasicSurface().toString().toLowerCase()));
                middle.add(road);

                l = new JLabel(Text.getString("road_width"));
                middle.add(l);
                roadWidthModel = new SpinnerNumberModel(this.track != null ? this.track.getRoadWidth() : 25, 5, 200, 1);
                roadWidth = new JSpinner(roadWidthModel);
                roadWidth.addChangeListener(this);
                l.setLabelFor(roadWidth);
		middle.add(roadWidth);

                middle.setBorder(new EmptyBorder(5, 0, 5, 0));

		SpringUtilities.makeCompactGrid(middle, 5, 2, 6, 6, 6, 6);

		contentPane.add(new JLabel(Text.getString("track_area")), BorderLayout.PAGE_START);
		contentPane.add(middle, BorderLayout.CENTER);
		contentPane.add(panelButtons, BorderLayout.PAGE_END);

		setContentPane(contentPane);
		pack();
		setLocationRelativeTo(frame);
		setVisible(true);
	}

    @Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == storno) {
			storno();
		} else if (e.getSource() == ok) {
			ok();
		}
	}

	/**
	 * Vrátí nastavovanou trať.
	 * @return
	 */
	public Track getTrack() {
		return track;
	}

        public static Surface getSurface(String s) {
            for(Surface surface :Surface.class.getEnumConstants()) {
                if(s.equals(Text.getString(surface.toString().toLowerCase()))) {
                    return surface;
                }
            }
            return Surface.ASPHALT;
        }

	/**
	 * Potvrzení a ukončení dialogu.
	 */
	public void ok() {
		if (track == null) {
			track = new Track(widthModel.getNumber().intValue(), heightModel.getNumber().intValue(),
                                getSurface(enviroment.getSelectedItem().toString()),
                                getSurface(road.getSelectedItem().toString()),
                                roadWidthModel.getNumber().intValue());
		} else {
			track.setWidth(widthModel.getNumber().intValue());
			track.setHeight(heightModel.getNumber().intValue());
                        track.setEnviroment(getSurface(enviroment.getSelectedItem().toString()));
                        track.setBasicSurface(getSurface(road.getSelectedItem().toString()));
                        track.setRoadWidth(roadWidthModel.getNumber().intValue());
                        track.changed();
		}
                
		dispose();
	}

	/**
	 * Stornovaní dialogu.
	 */
	public void storno() {
		track = null;
		dispose();
	}

    @Override
	public void caretUpdate(CaretEvent e) {
	}

    @Override
	public void keyReleased(KeyEvent e) {
	}

    @Override
	public void keyTyped(KeyEvent e) {
	}

    @Override
	public void stateChanged(ChangeEvent e) {
	}

    @Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			storno();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
			ok();
		}
	}

	/**
	 * Vytvoří a zobrazí dialog.
	 * @param frame
	 * @param track
	 * @return
	 */
	public static Track showDialog(JFrame frame, Track track) {
		TrackPropertiesDialog dialog = new TrackPropertiesDialog(frame, track);
		dialog.dispose();
		return dialog.getTrack();
	}
}
