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
 * Dialogové okno pro výběr tratě ze serveru.
 */
public class TrackChooser extends JDialog implements ActionListener, ListSelectionListener, CaretListener, KeyListener, MouseListener {

	/** tlačítka */
	private JButton ok,  storno;
	/** název tratě */
	private JTextField name;
	/**  */
	private Vector<String> tracks;
	/** výběr */
	private JList list;
	/** vybraná trať */
	private String track;

        private TrackView trackView;

	/** typ dialogu */
	private enum Type {

		OPEN, SAVE
	}
        String host;
        int port;

	/**
	 * Konstruktor, vytvoří dialog.
	 * @param frame
	 * @param type
	 * @param name
	 * @param host
	 * @param port
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	private TrackChooser(JFrame frame, Type type, String name, String host, int port) throws IOException,
			ClientRequestException {
		super(frame, type == Type.OPEN ? Text.getString("open_track") : Text.getString("save_track"), true);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		contentPane.add(new JLabel(type == Type.OPEN ? Text.getString("select_track") : Text.getString("enter_name_track")), BorderLayout.PAGE_START);

                this.host = host;
                this.port = port;
		tracks = load(host, port);
		list = new JList(tracks);
                list.setSelectedIndex(0);
		list.addListSelectionListener(this);
		list.addKeyListener(this);
		list.addMouseListener(this);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		listScroller.setPreferredSize(new Dimension(300, 150));
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

		JPanel panelTextField = new JPanel(new BorderLayout());
		panelTextField.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		JLabel l = new JLabel(Text.getString("Track"));
		l.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		panelTextField.add(l, BorderLayout.LINE_START);
		this.name = new JTextField();
		l.setLabelFor(this.name);
		this.name.addCaretListener(this);
		this.name.addKeyListener(this);
		panelTextField.add(this.name, BorderLayout.CENTER);
		if (name != null) {
			this.name.setText(name);
		}
		this.name.setEditable(type == Type.SAVE);
		if (type == Type.SAVE) {
			this.name.requestFocus();
		}

		JPanel panelBottom = new JPanel(new BorderLayout());
		panelBottom.add(panelTextField, BorderLayout.PAGE_START);
		panelBottom.add(panelButtons, BorderLayout.PAGE_END);
		contentPane.add(panelBottom, BorderLayout.PAGE_END);
                
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
	 * Vrátí vybraný název.
	 * @return
	 */
	public String getTrack() {
		return track;
	}

	/**
	 * Potvrzení a ukončení dialogu.
	 */
	public void ok() {
		track = name.getText();
		dispose();
	}

	/**
	 * Stornovaní dialogu.
	 */
	public void storno() {
		track = null;
		dispose();
	}

	/**
	 * Načtení názvů tratí uložených na serveru.
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
	 * Zobrazí dialog pro načtení tratě ze serveru.
	 * @param frame
	 * @param host
	 * @param port
	 * @return vybraný název tratě, null pokud není vybraný
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	public static String showOpenDialog(JFrame frame, String host, int port) throws IOException, ClientRequestException {
		TrackChooser dialog = new TrackChooser(frame, Type.OPEN, null, host, port);
		dialog.dispose();
		return dialog.getTrack();
	}

	/**
	 * Zobrazí dialog pro uložení tratě na server.
	 * @param frame
	 * @param name
	 * @param host
	 * @param port
	 * @return vybraný název tratě, null pokud není vybraný
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	public static String showSaveDialog(JFrame frame, String name, String host, int port) throws IOException,
			ClientRequestException {
		TrackChooser dialog = new TrackChooser(frame, Type.SAVE, name, host, port);
		dialog.dispose();
		return dialog.getTrack();
	}

        @Override
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting() == false) {
			if (list.getSelectedIndex() != -1) {
				name.setText(tracks.get(list.getSelectedIndex()));
				ok.setEnabled(true);
			} else {
				name.setText("");
				ok.setEnabled(false);
			}
		}
                if(e.getSource() == list && !list.isSelectionEmpty()) {
                    try {
                        this.trackView.setTrack(new Track(list.getSelectedValue().toString(), host, port));
                        this.trackView.repaint();
                    }
                    catch(Exception x) {
                        System.err.println(x.getMessage());
                        //x.printStackTrace();
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
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER && name.getText().matches("[^\\\\/ ][^\\\\/]*")) {
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
		if (name.getText().matches("[^\\\\/ ][^\\\\/]*")) {
			ok.setEnabled(true);
			return true;
		} else {
			ok.setEnabled(false);
			return false;
		}
	}

    @Override
	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == list && e.getClickCount() == 2) {
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
