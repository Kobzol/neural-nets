package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.connection.ClientRequest;
import cz.vsb.cs.neurace.connection.ClientRequestException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;


/**
 * Panel s konfigurací.
 */
public class ConfigTab implements ActionListener, DocumentListener, ChangeListener {
	/** jediná instance této třídy */
	private static ConfigTab configTab;
	/** Panel s konfigurací */
	private JPanel panel;
	/** ComboBox pro výběr lookAndFeel */
	private JComboBox lookAndFeelList, localeList;
	/** pole pro server */
	private JTextField host;
	/** nastavení portů */
	private JSpinner port,  testPort, dbPort;
	/** nastavení portů */
	private SpinnerNumberModel portModel,  testPortModel, dbPortModel;
        /** panel s testem připojení */
        private JPanel testPanel;
        /** test připojení */
        private JButton testButton;
        /** výsledek testu připojení */
        private JLabel testLabel;
        
        private JCheckBox useTexturesBox;

	/**
	 * KOnstruktor, vytvoří část GUI pro konfiguraci.
	 */
	private ConfigTab() {
		panel = new JPanel(new BorderLayout());
		panel.add(makeConfig(), BorderLayout.PAGE_START);
	}

	/**
	 * Vytvoří část GUI pro konfiguraci.
	 * @return
	 */
	protected final JComponent makeConfig() {
		JPanel configPanel = new JPanel(new SpringLayout());
                
		JLabel l = new JLabel(Text.getString("config_server_host"), JLabel.TRAILING);
		configPanel.add(l);
		host = new JTextField(Config.get().getString("host"));
		host.getDocument().addDocumentListener(this);
		l.setLabelFor(host);
		configPanel.add(host);

		l = new JLabel(Text.getString("config_server_port"), JLabel.TRAILING);
		configPanel.add(l);
		portModel = new SpinnerNumberModel(Config.get().getInt("port"), 0, 65536, 1);
		port = new JSpinner(portModel);
		port.addChangeListener(this);
		l.setLabelFor(port);
		configPanel.add(port);

		l = new JLabel(Text.getString("config_test_port"), JLabel.TRAILING);
		configPanel.add(l);
		testPortModel = new SpinnerNumberModel(Config.get().getInt("testPort"), 0, 65536, 1);
		testPort = new JSpinner(testPortModel);
		testPort.addChangeListener(this);
		l.setLabelFor(testPort);
		configPanel.add(testPort);
                
                l = new JLabel(Text.getString("config_db_port"), JLabel.TRAILING);
		configPanel.add(l);
		dbPortModel = new SpinnerNumberModel(Config.get().getInt("dbPort"), 0, 65536, 1);
		dbPort = new JSpinner(dbPortModel);
		dbPort.addChangeListener(this);
		l.setLabelFor(dbPort);
		configPanel.add(dbPort);
                
                
                l = new JLabel(Text.getString("locale"), JLabel.TRAILING);
		configPanel.add(l);
                
                String[] locales = {"default", "en", "cs"};
                localeList= new JComboBox(locales);
		localeList.setSelectedItem(Config.get().getString("locale"));
		localeList.addActionListener(this);
		l.setLabelFor(localeList);
                configPanel.add(localeList);

		l = new JLabel(Text.getString("config_look_feel"), JLabel.TRAILING);
		configPanel.add(l);
  
		LookAndFeelInfo[] lookAndFeelInfo = UIManager.getInstalledLookAndFeels();
		String[] lafStrings = new String[lookAndFeelInfo.length];
		int anIndex = 0;
		for (int i = 0; i < lookAndFeelInfo.length; i++) {
			lafStrings[i] = lookAndFeelInfo[i].getName();
			if (lookAndFeelInfo[i].getClassName().equals(UIManager.getLookAndFeel().getClass().getName())) {
				anIndex = i;
			}
		}
		lookAndFeelList = new JComboBox(lafStrings);
		lookAndFeelList.setSelectedIndex(anIndex);
		lookAndFeelList.addActionListener(this);
		l.setLabelFor(lookAndFeelList);
                configPanel.add(lookAndFeelList);
                
                l = new JLabel(Text.getString("config_textures"), JLabel.TRAILING);
		configPanel.add(l);
                useTexturesBox = new JCheckBox();
                useTexturesBox.setSelected(Config.get().getBoolean("textures"));
                useTexturesBox.addActionListener(this);
                l.setLabelFor(useTexturesBox);
                configPanel.add(useTexturesBox);

                testPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
                testButton = new JButton(Text.getString("test_connection"));
                testButton.addActionListener(this);
                
                testLabel = new JLabel();
                testPanel.add(testButton);
                testPanel.add(new JLabel(" "));
                testPanel.add(testLabel);
                l = new JLabel("", JLabel.TRAILING);
                configPanel.add(l);
                l.setLabelFor(testPanel);
                configPanel.add(testPanel);
		

		SpringUtilities.makeCompactGrid(configPanel, 8, 2, 6, 6, 6, 6);
		return configPanel;
	}

	/**
	 * Vrátí panel s GUI konfigurace.
	 * @return
	 */
	public JPanel getPanel() {
		return panel;
	}

	/**
	 * Vrátí instanci ConfigTab
	 * @return
	 */
	public static ConfigTab get() {
		if (configTab == null) {
			configTab = new ConfigTab();
		}
		return configTab;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == lookAndFeelList) {
			try {
				String lf = UIManager.getInstalledLookAndFeels()[lookAndFeelList.getSelectedIndex()].getClassName();
				UIManager.setLookAndFeel(lf);
				MainWindow.get().updateComponentTreeUI();
                                MainWindow.get().updateComponentTreeUI();
				Config.get().set("LookAndFeel", lf);
			} catch (Exception ex) {
				//ex.printStackTrace();
				MainWindow.get().errorMsg(ex.getLocalizedMessage());
			}
		}
                else if (e.getSource() == localeList) {
			try {
				String loc = localeList.getSelectedItem().toString();
                                Config.get().set("locale", loc);
                                Text.setLocale(loc);
			} catch (Exception ex) {
				//ex.printStackTrace();
				MainWindow.get().errorMsg(ex.getLocalizedMessage());
			}
		}
                else if(e.getSource() == useTexturesBox) {
                    Config.get().set("textures", String.valueOf(useTexturesBox.isSelected()));
                }
                else if(e.getSource() == testButton){
                    ClientRequest request;
                    try {
                        request = new ClientRequest("test", host.getText(), portModel.getNumber().intValue());
                        request.endHead();
                        testLabel.setForeground(Color.black);
                        testLabel.setText(Text.getString("test_ok"));
                    } catch (IOException ex) {
                        testLabel.setForeground(Color.red);
                        testLabel.setText(Text.translate(ex.getLocalizedMessage()));
                    } catch (ClientRequestException ex) {
                        testLabel.setForeground(Color.red);
                        testLabel.setText(Text.translate(ex.getLocalizedMessage()));
                    }

                }
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		if (e.getDocument() == host.getDocument()) {
			Config.get().set("host", host.getText());
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		if (e.getDocument() == host.getDocument()) {
			Config.get().set("host", host.getText());
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		if (e.getDocument() == host.getDocument()) {
			Config.get().set("host", host.getText());
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == port) {
			Config.get().set("port", portModel.getNumber().intValue());
		}
                else if (e.getSource() == testPort) {
			Config.get().set("testPort", testPortModel.getNumber().intValue());
		}
                else if (e.getSource() == dbPort) {
			Config.get().set("dbPort", dbPortModel.getNumber().intValue());
		}
	}
}
