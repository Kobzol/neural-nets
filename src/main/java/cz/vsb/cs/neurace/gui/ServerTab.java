package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.ResourceManager;
import cz.vsb.cs.neurace.server.Races;
import cz.vsb.cs.neurace.server.RacesListener;
import cz.vsb.cs.neurace.server.Server;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Panel s ovládaním serveru.
 */
public class ServerTab implements ActionListener, DocumentListener, ChangeListener, RacesListener {
	/** tlačítka pro menu */
	private JButton buttonStart,  buttonStop,  buttonDirChoose;
	/** instance */
	private static ServerTab serverTab;
	/** panel s GUI prvky */
	private JPanel panel;
	/** běžící server */
	private Server server;
	/** adresář pro tratě serveru */
	private JTextField dir;
	/** nastavení portu serveru */
	private JSpinner port, ups;
	/** nastavení portu serveru */
	private SpinnerNumberModel portModel, upsModel;
	/** výber adresáře */
	private JFileChooser fileChooser;
	/** běžící závody */
	private JList info;
        /** nastavení LDAP ověřování */
        private JCheckBox ldap;

	/**
	 * Konstruktor vytvoří panel.
	 */
	private ServerTab() {
		panel = new JPanel(new BorderLayout());
		panel.add(makeMenu(), BorderLayout.PAGE_START);

		JPanel panelc = new JPanel(new BorderLayout());
		panelc.add(makeConfig(), BorderLayout.PAGE_START);
		panel.add(panelc, BorderLayout.CENTER);

		info = new JList();
		info.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		JScrollPane listScroller = new JScrollPane(info);
		panelc.add(listScroller, BorderLayout.CENTER);
	}

	/**
	 * Panel s ovladacími prvky pro menu track
	 * 
	 * @return ovladací prvky
	 */
	protected final JComponent makeMenu() {
		JPanel menuPanel = new JPanel();
		menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.X_AXIS));

		buttonStart = new JButton(ResourceManager.getImageIcon("start.png"));
		buttonStart.setToolTipText(Text.getString("start_server"));
		buttonStart.addActionListener(this);
		menuPanel.add(buttonStart);

		buttonStop = new JButton(ResourceManager.getImageIcon("stop.png"));
		buttonStop.setToolTipText(Text.getString("stop_server"));
		buttonStop.addActionListener(this);
		buttonStop.setEnabled(false);
		menuPanel.add(buttonStop);

		return menuPanel;
	}

	/**
	 * Vytvoří GUI konfigurace serveru
	 * @return
	 */
	protected final JComponent makeConfig() {
		JPanel configPanel = new JPanel(new SpringLayout());

		JLabel l = new JLabel(Text.getString("server_dir"), JLabel.TRAILING);
		configPanel.add(l);

		JPanel dirPanel = new JPanel(new BorderLayout());
		dir = new JTextField(Config.get().getString("serverDir"));
		dir.setToolTipText("Directory that contains subdirectory tracks and cars");
		dir.getDocument().addDocumentListener(this);
		l.setLabelFor(dir);
		dirPanel.add(dir, BorderLayout.CENTER);
		buttonDirChoose = new JButton(Text.getString("dir_choose"));
		buttonDirChoose.addActionListener(this);
		dirPanel.add(buttonDirChoose, BorderLayout.LINE_END);
		configPanel.add(dirPanel);

		fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		l = new JLabel(Text.getString("server_port"), JLabel.TRAILING);
		configPanel.add(l);
		portModel = new SpinnerNumberModel(Config.get().getInt("serverPort"), 0, 65536, 1);
		port = new JSpinner(portModel);
		port.addChangeListener(this);
		l.setLabelFor(port);
		configPanel.add(port);
                
                l = new JLabel(Text.getString("ups"), JLabel.TRAILING);
		configPanel.add(l);
		upsModel = new SpinnerNumberModel(Config.get().getInt("ups"), 10, 60, 10);
		ups = new JSpinner(upsModel);
		ups.addChangeListener(this);
		l.setLabelFor(ups);
		configPanel.add(ups);

                ldap = new JCheckBox();
                ldap.setSelected(false);
                l = new JLabel("LDAP", JLabel.TRAILING);
                l.setLabelFor(ldap);
                configPanel.add(l);
                configPanel.add(ldap);
                
		SpringUtilities.makeCompactGrid(configPanel, 4, 2, 6, 6, 6, 6);

		return configPanel;
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
	public static ServerTab get() {
		if (serverTab == null) {
			serverTab = new ServerTab();
		}
		return serverTab;
	}

        @Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == buttonStart) {
			try {
				server = new Server(Config.get().getInt("serverPort"), Config.get().getString("serverDir"), ldap.isSelected(), upsModel.getNumber().intValue());
				buttonStart.setEnabled(false);
				buttonStop.setEnabled(true);
				dir.setEnabled(false);
				buttonDirChoose.setEnabled(false);
				port.setEnabled(false);
                                ups.setEnabled(false);
				new ServerThread(server).start();
				Races.getRaces().addRacesListener(this);
			} catch (IOException ex) {
				//ex.printStackTrace();
				MainWindow.get().errorMsg(ex.getLocalizedMessage());
			}
		} else if (e.getSource() == buttonStop) {
			server.stop();
			buttonStart.setEnabled(true);
			buttonStop.setEnabled(false);
			dir.setEnabled(true);
			buttonDirChoose.setEnabled(true);
			port.setEnabled(true);
                        ups.setEnabled(true);
			info.setListData(new Object[0]);
		}
		if (e.getSource() == buttonDirChoose) {
			int returnVal = fileChooser.showOpenDialog(panel);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fileChooser.getSelectedFile();
				dir.setText(file.getAbsolutePath());
			}
		}
	}

	/**
	 * aktualizace LookAndFeel
	 */
	public void updateComponentUI() {
		fileChooser.updateUI();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		if (e.getDocument() == dir.getDocument()) {
			Config.get().set("serverDir", dir.getText());
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		if (e.getDocument() == dir.getDocument()) {
			Config.get().set("serverDir", dir.getText());
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if (e.getDocument() == dir.getDocument()) {
			Config.get().set("serverDir", dir.getText());
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == port) {
			Config.get().set("serverPort", portModel.getNumber().intValue());
		}
                else if (e.getSource() == ups) {
			Config.get().set("ups", upsModel.getNumber().intValue());
		}
	}

        @Override
	public void racesAction() {
		info.setListData(Races.getRaces().getRacesList().toArray());
	}

	/**
	 * Vlákno pro server.
	 */
	class ServerThread extends Thread {

		private Server server;

		public ServerThread(Server server) {
			this.server = server;
		}

		@Override
		public void run() {
			server.run();
		}
	}

}
