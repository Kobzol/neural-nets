package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.connection.ClientRequest;
import cz.vsb.cs.neurace.connection.ClientRequestException;
import cz.vsb.cs.neurace.gui.track.TrackView;
import cz.vsb.cs.neurace.track.Track;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.IOException;
import java.util.Vector;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


/**
 * Dialog pro vytvoření závodu.
 */
public class NewRaceDialog extends JDialog implements ActionListener, ListSelectionListener, CaretListener, KeyListener, MouseListener {

	/** tlačítka */
	private JButton ok,  storno;
	/** pole pro název závodu */
	private JTextField name;
	/** názvy tratí */
	private Vector<String> tracks;
	/** vyběr tratě */
	private JList list;
	/** nastavení počtu kol */
	private JSpinner lapsSpinner;
	/** nastavení počtu kol */
	private SpinnerNumberModel lapsModel;
	/** název tratě */
	public static String track;
	/** název závodu */
	public static String race;
	/** počet kol */
	public static int laps;

        public static int races = 1;

        public static boolean collisions = true;
	/** hodnoty nstavené */
	public static final int OK = 0;
	/** hodnoty nenastavené */
	public static final int STORNO = 1;
	/** OK nebo STORNO */
	private int value = STORNO;

        private TrackView trackView;
        private String host;
        private int port;
        private JComboBox raceType;
        private JSpinner racesSpinner;
        private SpinnerNumberModel racesModel;
        private JLabel racesLabel;
        private JCheckBox collisionsBox;
	/**
	 * Vytvoří a zobrazí dialog.
	 * 
	 * @param frame
	 * @param host
	 * @param port
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	private NewRaceDialog(JFrame frame, String host, int port, boolean newRace) throws IOException,
			ClientRequestException {
                super(frame, Text.getString("new_race2"), true);

                this.host = host;
                this.port = port;

                if(!newRace) this.setTitle(Text.getString("change_track"));

		JPanel contentPane = new JPanel(new BorderLayout(10, 10));
		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		contentPane.add(new JLabel(Text.getString("select_track")), BorderLayout.PAGE_START);

		tracks = load(host, port);
		list = new JList(tracks);
                list.setSelectedValue(Config.prefs.get("track", null), false);
		list.addListSelectionListener(this);
		list.addKeyListener(this);
		list.addMouseListener(this);
                
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setPreferredSize(new Dimension(200, 150));
                contentPane.add(listScroller, BorderLayout.CENTER);
                

		JPanel panelButtons = new JPanel(new BorderLayout());
		JPanel panelButtons2 = new JPanel(new GridLayout(0, 2, 3, 3));
		ok = new JButton("OK");
		ok.addActionListener(this);
		ok.addKeyListener(this);
		ok.setEnabled(false);
		panelButtons2.add(ok);
		storno = new JButton("storno");
		storno.addActionListener(this);
		storno.addKeyListener(this);
		panelButtons2.add(storno);
		panelButtons.add(panelButtons2, BorderLayout.LINE_END);


		JPanel panelTextField = new JPanel(new SpringLayout());

		JLabel l = new JLabel(Text.getString("Race_name"), JLabel.TRAILING);
		panelTextField.add(l);
		this.name = new JTextField();
		l.setLabelFor(this.name);
		this.name.addCaretListener(this);
		this.name.addKeyListener(this);
                this.name.setText(Config.prefs.get("racename", Text.getString("default_race")));
                this.name.setEnabled(newRace);
		panelTextField.add(this.name);

		l = new JLabel(Text.getString("Race_laps"), JLabel.TRAILING);
		panelTextField.add(l);
                int lapsValue = Config.prefs.getInt("laps", 3);
		lapsModel = new SpinnerNumberModel(lapsValue, 1, 100, 1);
		lapsSpinner = new JSpinner(lapsModel);
		panelTextField.add(lapsSpinner);

                l = new JLabel(Text.getString("Race_type"), JLabel.TRAILING);
		panelTextField.add(l);
                raceType = new JComboBox(new String[]{Text.getString("Single_race"),
                Text.getString("Championship")});
                boolean champ = Config.prefs.getBoolean("championship", false);
                if(champ) {
                    raceType.setSelectedIndex(1);
                }
                else {
                    raceType.setSelectedIndex(0);
                }
                raceType.addActionListener(this);
                panelTextField.add(raceType);
                
                racesLabel = new JLabel(Text.getString("Races_num"), JLabel.TRAILING);
                racesLabel.setEnabled(champ);
		panelTextField.add(racesLabel);
                int racesValue = Config.prefs.getInt("races", 3);
                racesModel = new SpinnerNumberModel(racesValue, 1, 10, 1);
		racesSpinner = new JSpinner(racesModel);
                racesSpinner.setEnabled(champ);
                panelTextField.add(racesSpinner);

                panelTextField.add(new JLabel());
                collisionsBox = new JCheckBox(Text.getString("Collisions"));
                collisionsBox.setSelected(Config.prefs.getBoolean("collisions", true));
                //collisionsBox.setEnabled(newRace);
                panelTextField.add(collisionsBox);


		SpringUtilities.makeCompactGrid(panelTextField, 5, 2, 0, 6, 6, 6);


                
                JPanel panelBottom = new JPanel(new BorderLayout());
                try {
                    Track tr = new Track(list.getSelectedValue().toString(), host, port);
                    trackView = new TrackView(tr, 300);
                }
                catch(Exception e) {
                    //e.printStackTrace();
                    trackView = new TrackView(null, 300);
                }
                JScrollPane panelTrack = new JScrollPane(trackView);
                panelTrack.setPreferredSize(new Dimension(302, 302));
                contentPane.add(panelTrack, BorderLayout.EAST);
                
		panelBottom.add(panelTextField, BorderLayout.PAGE_START);
		panelBottom.add(panelButtons, BorderLayout.PAGE_END);
		contentPane.add(panelBottom, BorderLayout.PAGE_END);


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
                else if(e.getSource() == raceType) {
                    boolean b = raceType.getSelectedItem() == Text.getString("Championship");
                    this.racesLabel.setEnabled(b);
                    this.racesSpinner.setEnabled(b);
                }
	}

	/**
	 * Potvrzení a ukončení dialogu.
	 */
	public void ok() {
		value = OK;
		dispose();
	}

	/**
	 * Stornovaní dialogu.
	 */
	public void storno() {
		value = STORNO;
		dispose();
	}

	/**
	 * Vrátí OK nebo STORNO.
	 * @return
	 */
	public int getValue() {
		if (value == OK) {
			track = (String) list.getSelectedValue();
			race = name.getText();
			laps = lapsModel.getNumber().intValue();
                        if(racesSpinner.isEnabled()) {
                            races = racesModel.getNumber().intValue();
                            Config.prefs.putInt("races", races);
                            Config.prefs.putBoolean("championship", true);
                        }
                        else {
                            races = 1;
                            Config.prefs.putBoolean("championship", false);
                        }
                        collisions = collisionsBox.isSelected();
                        Config.prefs.put("track", track);
                        Config.prefs.put("racename", race);
                        Config.prefs.putInt("laps", laps);
                        Config.prefs.putBoolean("collisions", collisions);

		}
		return value;
	}

	/**
	 * Načte se serveru názvy tratí, které jsou tam uloženy.
	 * @param host
	 * @param port
	 * @return
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	private Vector<String> load(String host, int port) throws IOException, ClientRequestException {
		ClientRequest request = new ClientRequest("tracklist", host, port);
		request.endHead();

		String line;
		Vector<String> rtracks = new Vector<String>();
		while ((line = request.readline()) != null && line.length() != 0) {
			if (line.length() > 0) {
				rtracks.add(line);
			}
		}
		request.close();
		return rtracks;
	}

	/**
	 * Vytvoří a zobrazí dialog.
	 * @param frame
	 * @param host
	 * @param port
	 * @return
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	public static int showDialog(JFrame frame, String host, int port, boolean newRace) throws IOException, ClientRequestException {
		NewRaceDialog dialog = new NewRaceDialog(frame, host, port, newRace);
		dialog.dispose();
		return dialog.getValue();
	}

	public void valueChanged(ListSelectionEvent e) {
		if (name.getText().matches("[^\\\\/ ][^\\\\/]*")) {
			ok.setEnabled(true);
		}
                if(e.getSource() == list && !list.isSelectionEmpty()) {
                    try {
                        this.trackView.setTrack(new Track(list.getSelectedValue().toString(), host, port));
                        this.trackView.repaint();
                    }
                    catch(Exception x) {
                        x.printStackTrace();
                    }
                }
       
	}



    @Override
	public void caretUpdate(CaretEvent e) {
		controlForm();
	}

    @Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			storno();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER && name.getText().matches("[^\\\\/ ][^\\\\/]*") && !list.isSelectionEmpty()) {
			ok();
		}
	}

    @Override
	public void keyReleased(KeyEvent e) {
	}

    @Override
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Kontroluje zda je dialog strávně vyplněn.
	 * @return true pokud je, jinak false
	 */
	private boolean controlForm() {
		if (name.getText().matches("[^\\\\/ ][^\\\\/]*") && !list.isSelectionEmpty()) {
			ok.setEnabled(true);
			return true;
		} else {
			ok.setEnabled(false);
			return false;
		}
	}

    @Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == list && e.getClickCount() == 2 && name.getText().matches("[^\\\\/ ][^\\\\/]*") && !list.isSelectionEmpty()) {
			ok();
		}
	}

    @Override
	public void mousePressed(MouseEvent e) {
	}

    @Override
	public void mouseReleased(MouseEvent e) {
	}

    @Override
	public void mouseEntered(MouseEvent e) {
	}

    @Override
	public void mouseExited(MouseEvent e) {
	}
}
