package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.connection.ClientRequest;
import cz.vsb.cs.neurace.connection.ClientRequestException;
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
 * Dialog pro výběr závodu.
 */
public class ConnectRaceDialog extends JDialog implements ActionListener, ListSelectionListener, CaretListener, KeyListener, MouseListener {

	/** tlačítka */
	private JButton ok,  storno;
	/** seznam názvů závodů */
	private Vector<String> races;
	/** výběr názvů závodů */
	private JList list;
	/** vybraný závod */
	private String value;

	/**
	 * Vytvoří a zobrazí dialog.
	 * 
	 * @param frame
	 * @param host
	 * @param port
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	private ConnectRaceDialog(JFrame frame, String host, int port) throws IOException,
			ClientRequestException {
		super(frame, Text.getString("connect_race"), true);

		JPanel contentPane = new JPanel(new BorderLayout());
		contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		contentPane.add(new JLabel(Text.getString("select_race")), BorderLayout.PAGE_START);

		races = load(host, port);
		list = new JList(races);
		list.addListSelectionListener(this);
		list.addKeyListener(this);
		list.addMouseListener(this);
		JScrollPane listScroller = new JScrollPane(list);
		listScroller.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		listScroller.setPreferredSize(new Dimension(400, 150));
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

		JPanel panelBottom = new JPanel(new BorderLayout());
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
	}

	/**
	 * Vrátí název vybraného závodu.
	 * @return
	 */
	public String getData() {
		String r = (String) list.getSelectedValue();
		return r;
	}

	/**
	 * Potvrzení a ukončení dialogu.
	 */
	public void ok() {
		value = getData();
		dispose();
	}

	/**
	 * Stornovaní dialogu.
	 */
	public void storno() {
		value = null;
		dispose();
	}

	/**
	 * Vrátí název vybraného závodu.
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Načte názvy běžících závodů.
	 * @param host
	 * @param port
	 * @return
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	private Vector<String> load(String host, int port) throws IOException, ClientRequestException {
		ClientRequest request = new ClientRequest("racelist", host, port);
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
	 * Zobrazí dialog.
	 * @param frame
	 * @param host
	 * @param port
	 * @return název vybraného závodu.
	 * @throws IOException
	 * @throws cz.vsb.cs.neurace.connection.ClientRequestException
	 */
	public static String showDialog(JFrame frame, String host, int port) throws IOException, ClientRequestException {
		ConnectRaceDialog dialog = new ConnectRaceDialog(frame, host, port);
		dialog.dispose();
		return dialog.getValue();
	}

        @Override
	public void caretUpdate(CaretEvent e) {
		controlForm();
	}

        @Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			storno();
		} else if (e.getKeyCode() == KeyEvent.VK_ENTER && !list.isSelectionEmpty()) {
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
		if (!list.isSelectionEmpty()) {
			ok.setEnabled(true);
			return true;
		} else {
			ok.setEnabled(false);
			return false;
		}
	}

        @Override
    	public void mouseClicked(MouseEvent e) {
		if (e.getSource() == list && e.getClickCount() == 2 && !list.isSelectionEmpty()) {
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

        @Override
	public void valueChanged(ListSelectionEvent e) {
		controlForm();
	}
}
