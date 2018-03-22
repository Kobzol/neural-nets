package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.ResourceManager;
import cz.vsb.cs.neurace.gui.MainWindow.Status;
import cz.vsb.cs.neurace.gui.track.TrackEdit;
import cz.vsb.cs.neurace.track.Track;
import cz.vsb.cs.neurace.track.TrackObject;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Panel s editorem trati.
 */
public class TrackEditorTab implements ActionListener, ListSelectionListener {

	/** tlačítka pro menu */
	private JButton buttonNew, buttonOpen, buttonSave, buttonRemovePoint,  buttonSetFirst,  buttonCheckpoint, buttonRevert,  buttonProperties;
        private JToggleButton buttonSelectPoint, buttonNewPoint, buttonObject, buttonSurface, buttonBulldozer;
        private ButtonGroup toggleButtons;
        private JPopupMenu saveMenu, openMenu;
	/** instance */
	private static TrackEditorTab trackEditorTab;
	/** panel s GUI prvky */
	private JPanel panel;
	/** scroll panel pro trať */
	private JScrollPane scrollPanel;
	/** trať */
	private TrackEdit trackEdit;
        private ObjectView objectView;
        private WallPanel wallPanel;

        private JList list;
        private JPanel listPanel;
        private DefaultListModel listModel;
        private boolean showRoads = false;


        private String[] objectTypes;
        private String[] roadTypes = new String[]{Text.getString("asphalt"), Text.getString("gravel"), Text.getString("dirt"),
        Text.getString("sand"), Text.getString("snow"),Text.getString("ice")};

	/**
	 * Konstruktor vytvoří panel.
	 */
	private TrackEditorTab() {
		panel = new JPanel(new BorderLayout());
		panel.add(makeMenuTrackEditor(), BorderLayout.PAGE_START);

                listPanel = makeList();
                panel.add(listPanel, BorderLayout.LINE_START);
                listPanel.setEnabled(false);
	}

        private JPanel makeList() {
            JPanel lPanel = new JPanel();
            lPanel.setLayout(new BorderLayout(0, 10));
            lPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

            
            listModel = new DefaultListModel();
            list = new JList(listModel);
            list.addListSelectionListener(this);
            JScrollPane listScroll = new JScrollPane(list);
            listScroll.setPreferredSize(new Dimension(150, 200));

            lPanel.add(listScroll, BorderLayout.CENTER);
            objectView = new ObjectView();
            wallPanel = new WallPanel();
            wallPanel.setTrackEdit(trackEdit);
            lPanel.add(wallPanel, BorderLayout.PAGE_END);
            
            return lPanel;
        }
        
        private void setObjectView() {
            if(!objectView.isDisplayable()) {
                listPanel.remove(wallPanel);
                listPanel.add(objectView, BorderLayout.PAGE_END);
                listPanel.revalidate();
            }
        }
        
        private void setWallPanel() {
            if(!wallPanel.isDisplayable()) {
                listPanel.remove(objectView);
                listPanel.add(wallPanel, BorderLayout.PAGE_END);
                listPanel.revalidate();
            }
        } 


        private class ObjectView extends JComponent {
            @Override
            public void paintComponent(Graphics g) {
                String key = null;
                if(list.getSelectedValue() != null) {
                    key = list.getSelectedValue().toString();
                }
                if(!showRoads && key != null) {
                    BufferedImage img = Resources.get().getImage(key);
                    if(img != null) {
                        int max = Math.max(img.getHeight(), img.getWidth());
                        g.drawImage(Resources.get().getImage(key), 0, 0, (this.getWidth()*img.getWidth())/max,
                                (this.getHeight()*img.getHeight())/max, null);
                    }
                }
                else if(showRoads && key != null) {
                    key = TrackPropertiesDialog.getSurface(key).toString().toLowerCase();
                    BufferedImage img = Resources.get().getImage(key);
                    if(img != null) {

                        g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
                    }
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(150, 150);
            }

        }



	/**
	 * Panel s ovladacími prvky pro menu editoru trati
	 * 
	 * @return ovladací prvky
	 */
	private JComponent makeMenuTrackEditor() {
		JPanel panelMenu = new JPanel();
		panelMenu.setLayout(new BoxLayout(panelMenu, BoxLayout.X_AXIS));
                JPanel panelButtons = new JPanel();
                panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.X_AXIS));
                toggleButtons = new ButtonGroup();

                
                buttonNew = new JButton(ResourceManager.getImageIcon("new.png"));
		buttonNew.setToolTipText(Text.getString("new_track"));
		buttonNew.addActionListener(this);
		panelButtons.add(buttonNew);
                
                buttonOpen = new JButton(ResourceManager.getImageIcon("load_local.png"));
		buttonOpen.setToolTipText(Text.getString("open"));
		buttonOpen.addActionListener(this);
                openMenu = createOpenMenu();
		panelButtons.add(buttonOpen);
                
                buttonSave = new JButton(ResourceManager.getImageIcon("save_local.png"));
		buttonSave.setToolTipText(Text.getString("save"));
                saveMenu = createSaveMenu();
		buttonSave.addActionListener(this);
                
		panelButtons.add(buttonSave);
                
		buttonSelectPoint = new JToggleButton(ResourceManager.getImageIcon("select.png"));
		buttonSelectPoint.setToolTipText(Text.getString("select_point"));
		buttonSelectPoint.addActionListener(this);
		panelButtons.add(buttonSelectPoint);
                toggleButtons.add(buttonSelectPoint);

		buttonNewPoint = new JToggleButton(ResourceManager.getImageIcon("new_point.png"));
		buttonNewPoint.setToolTipText(Text.getString("new_point"));
		buttonNewPoint.addActionListener(this);
		panelButtons.add(buttonNewPoint);
                toggleButtons.add(buttonNewPoint);

                buttonObject = new JToggleButton(ResourceManager.getImageIcon("block.png"));
                buttonObject.setToolTipText(Text.getString("add_objects"));
		buttonObject.addActionListener(this);
		panelButtons.add(buttonObject);
                toggleButtons.add(buttonObject);


		buttonSurface = new JToggleButton(ResourceManager.getImageIcon("surface.png"));
		buttonSurface.setToolTipText(Text.getString("road_surface"));
		buttonSurface.addActionListener(this);
                panelButtons.add(buttonSurface);
                toggleButtons.add(buttonSurface);

                buttonBulldozer = new JToggleButton(ResourceManager.getImageIcon("bulldozer.png"));
		buttonBulldozer.setToolTipText(Text.getString("bulldozer"));
		buttonBulldozer.addActionListener(this);
                panelButtons.add(buttonBulldozer);
                toggleButtons.add(buttonBulldozer);
		
		buttonRemovePoint = new JButton(ResourceManager.getImageIcon("remove_point.png"));
		buttonRemovePoint.setToolTipText(Text.getString("remove_point"));
		buttonRemovePoint.addActionListener(this);
                panelButtons.add(buttonRemovePoint);

		buttonSetFirst = new JButton(ResourceManager.getImageIcon("first_point.png"));
		buttonSetFirst.setToolTipText(Text.getString("set_first_point"));
		buttonSetFirst.addActionListener(this);
		panelButtons.add(buttonSetFirst);

                buttonCheckpoint = new JButton(ResourceManager.getImageIcon("flag.png"));
		buttonCheckpoint.setToolTipText(Text.getString("checkpoint"));
		buttonCheckpoint.addActionListener(this);
		panelButtons.add(buttonCheckpoint);

		buttonRevert = new JButton(ResourceManager.getImageIcon("revert.png"));
		buttonRevert.setToolTipText(Text.getString("revert_points"));
		buttonRevert.addActionListener(this);
		panelButtons.add(buttonRevert);

                

                buttonProperties = new JButton(ResourceManager.getImageIcon("config_track.png"));
		buttonProperties.setToolTipText(Text.getString("track_properties"));
		buttonProperties.addActionListener(this);

                panelButtons.add(buttonProperties);
                

                
                panelMenu.add(panelButtons);
                panelMenu.add(Box.createRigidArea(new Dimension(10, 0)));

                

		return panelMenu;
	}

        public void loadObjectTypes() {
            LinkedList<String> objectList = new LinkedList<String>();
            for(TrackObject object: Resources.get().getObjects()) {
                objectList.add(object.getType());
            }
            objectTypes = new String[objectList.size()];
            objectList.toArray(objectTypes);
            setList(objectTypes);
        }

	/**
	 * Nastaví trať
	 * @param track
	 */
	public void setTrack(Track track) {
		if (scrollPanel != null) {
			panel.remove(scrollPanel);
		}
		this.trackEdit = new TrackEdit(track);
                if(TestTab.get().getRace() != null) {
                    trackEdit.setPhysics(TestTab.get().getRace().getPhysics());
                }
                wallPanel.setTrackEdit(trackEdit);
		scrollPanel = new JScrollPane(this.trackEdit);
		panel.add(scrollPanel, BorderLayout.CENTER);
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
	public static TrackEditorTab get() {
		if (trackEditorTab == null) {
			trackEditorTab = new TrackEditorTab();
		}
		return trackEditorTab;
	}
        
        public static JPopupMenu createOpenMenu() {
            final JPopupMenu popup = new JPopupMenu();
            JMenuItem item;
            item = new JMenuItem(Text.getString("load_from_disk"), ResourceManager.getImageIcon("load_local.png"));
            popup.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MainWindow.get().loadTrackLocal();
                }
            });
            item = new JMenuItem(Text.getString("load_from_server"), ResourceManager.getImageIcon("load_server.png"));
            popup.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MainWindow.get().loadTrackServer();
                }
            });
            
            return popup;
        }
        
        public static JPopupMenu createSaveMenu() {
            final JPopupMenu popup = new JPopupMenu();
            JMenuItem item;
            item = new JMenuItem(Text.getString("save_to_disk"), ResourceManager.getImageIcon("save_local.png"));
            popup.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MainWindow.get().saveTrackLocal(false);
                }
            });
            item = new JMenuItem(Text.getString("save_to_disk_as"), ResourceManager.getImageIcon("save_local_as.png"));
            popup.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MainWindow.get().saveTrackLocal(true);
                }
            });
            item = new JMenuItem(Text.getString("save_to_server"), ResourceManager.getImageIcon("save_server.png"));
            popup.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MainWindow.get().saveTrackServer(false);
                }
            });
            item = new JMenuItem(Text.getString("save_to_server_as"), ResourceManager.getImageIcon("save_server_as.png"));
            popup.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MainWindow.get().saveTrackServer(true);
                }
            });
            
            return popup;
        }


        @Override
	public void actionPerformed(ActionEvent e) {
                if (e.getSource() == buttonNew) {
                    MainWindow.get().newTrack();
                } else if (e.getSource() == buttonOpen) {
                    openMenu.show(buttonOpen, 0, buttonOpen.getHeight());
                } else if (e.getSource() == buttonSave) {
                    saveMenu.show(buttonSave, 0, buttonSave.getHeight());
                } else if (e.getSource() == buttonSelectPoint) {
			trackEdit.selectPoint();
                        list.clearSelection();
		} else if (e.getSource() == buttonNewPoint) {
			trackEdit.newPoint();
                        list.clearSelection();
		} else if (e.getSource() == buttonRemovePoint) {
			trackEdit.removePoint();
		} else if (e.getSource() == buttonRevert) {
			trackEdit.revert();
		} else if (e.getSource() == buttonSetFirst) {
			trackEdit.setFirstPoint();
                } else if (e.getSource() == buttonCheckpoint) {
			trackEdit.setCheckpoint();
                } else if (e.getSource() == buttonObject) {
                        setList(objectTypes);
                        showRoads = false;
                        list.setSelectedIndex(0);
                        
			//this.trackEdit.setObjectMode(fromString(this.list.getSelectedValue().toString()));
                } else if (e.getSource() == buttonSurface) {
			setList(roadTypes);
                        showRoads = true;
                        list.setSelectedIndex(0);
                        
                } else if (e.getSource() == buttonBulldozer) {
                        this.trackEdit.bulldozer();
			list.clearSelection();
			//this.trackEdit.setObjectMode(fromString(this.list.getSelectedValue().toString()));
		} else if (e.getSource() == buttonProperties) {
			if (TrackPropertiesDialog.showDialog(MainWindow.get(), trackEdit.getTrack()) != null) {
				trackEdit.repaint();
			}
		}
	}

        private void setList(String[] types) {
            listModel.clear();
            for(String s:types) {
                this.listModel.addElement(s);
            }
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if(list.getSelectedValue() != null && showRoads) {
                this.trackEdit.setSurfaceMode(TrackPropertiesDialog.getSurface(this.list.getSelectedValue().toString()));
                this.buttonSurface.setSelected(true);
                setObjectView();
                objectView.repaint();
            }
            else if(list.getSelectedValue() != null) {
                this.trackEdit.setObjectMode(this.list.getSelectedValue().toString());
                this.buttonObject.setSelected(true);
                setObjectView();
                objectView.repaint();
            }
            else {
                setWallPanel();
                wallPanel.repaint();
            }
     
        }


	/**
	 * Umožní používat tlačítko select.
	 */
	public void canSelect() {
		buttonSelectPoint.setEnabled(true);
		//buttonSelectPoint.requestFocus();
	}

	/**
	 * Nastaví tlačítka podle toho, zda je bod vybraný.
	 * @param is
	 */
	public void selected(boolean point) {
		buttonRemovePoint.setEnabled((point && trackEdit.getTrack().getPoints().length > 2));
		buttonSetFirst.setEnabled(point);
                buttonCheckpoint.setEnabled(point);
		//buttonSelectPoint.requestFocus();
	}

        public void setSelectionMode() {
            this.buttonSelectPoint.setSelected(true);
            //this.buttonSelectPoint.requestFocus();
            this.list.clearSelection();
        }

 
	/**
	 * Nastaví se podle stavu.
	 * @param status
	 */
	public void setStatus(Status status) {
		if (status == Status.NOTRACK) {
                        list.setEnabled(false);
                        buttonSave.setEnabled(false);
			buttonNewPoint.setEnabled(false);
			buttonRemovePoint.setEnabled(false);
			buttonSelectPoint.setEnabled(false);
			buttonProperties.setEnabled(false);
			buttonSetFirst.setEnabled(false);
                        buttonCheckpoint.setEnabled(false);
			buttonRevert.setEnabled(false);
                        buttonObject.setEnabled(false);
                        buttonSurface.setEnabled(false);
                        buttonBulldozer.setEnabled(false);
		} else if (trackEdit.isSelected()) {
                        list.setEnabled(status != Status.NEWTRACK);
                        buttonSave.setEnabled(status != Status.NEWTRACK);
			buttonNewPoint.setEnabled(true);
			buttonRemovePoint.setEnabled(trackEdit.getTrack().getPoints().length > 2);
			buttonSelectPoint.setEnabled(true);
			buttonProperties.setEnabled(true);
			buttonSetFirst.setEnabled(true);
                        buttonCheckpoint.setEnabled(true);
			buttonRevert.setEnabled(true);
                        buttonObject.setEnabled(true);
                        buttonSurface.setEnabled(true);
                        buttonBulldozer.setEnabled(true);
		} else {
                        list.setEnabled(status != Status.NEWTRACK);
                        buttonSave.setEnabled(status != Status.NEWTRACK);
			buttonNewPoint.setEnabled(status != Status.NEWTRACK);
			buttonRemovePoint.setEnabled(false);
			buttonSelectPoint.setEnabled(status != Status.NEWTRACK);
			buttonProperties.setEnabled(true);
			buttonSetFirst.setEnabled(false);
                        buttonCheckpoint.setEnabled(false);
			buttonRevert.setEnabled(status != Status.NEWTRACK);
                        buttonObject.setEnabled(status != Status.NEWTRACK);
                        buttonSurface.setEnabled(status != Status.NEWTRACK);
                        buttonBulldozer.setEnabled(status != Status.NEWTRACK);
		}
		//buttonSelectPoint.requestFocus();
	}

        public TrackEdit getTrackEdit() {
            return trackEdit;
        }

        public void setScale(float scale) {
            this.trackEdit.setScale(scale);
        }




}
