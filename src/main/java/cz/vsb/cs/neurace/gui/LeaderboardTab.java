package cz.vsb.cs.neurace.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * Panel se žebříčky
 * @author Petr Hamalčík
 */
public class LeaderboardTab extends JPanel implements DBListener{

    private static LeaderboardTab leaderTab;
    private DefaultListModel trackListModel = new DefaultListModel();
    
    private int driversPage = 0;
    private int timesPage = 0;
    
    private int driversCount = 0;
    private int timesCount = 0;
    
    private int maxRows = 30;
    private boolean updateTracks = false;
    private Object selected = null;
    public final String trackPrefix = "NTR_";
    
    /** Creates new form LeaderboardTab */
    public LeaderboardTab() {
        initComponents();
    }

    /**
     * Vrátí instanci.
     * @return
     */
    public static LeaderboardTab get() {
            if (leaderTab == null) {
                    leaderTab = new LeaderboardTab();
            }
            return leaderTab;
    }

    /**
     * Vrátí panel s prvky.
     * @return
     */
    public JPanel getPanel() {
            return this;
    }
    
    public static enum Table {RACES, TRACKS, TIMES};
   
    public void updateTable(Table table) {
        if(table == Table.TIMES && trackList.getSelectedValue() == null) {
            return;
        }
        JComponent parent;
        DefaultTableModel model;
        if(table == Table.RACES) {
            parent = (JComponent)racesTable.getParent();
            model = (DefaultTableModel) racesTable.getModel();
            updateDriversButtons(false);
        } else {
            parent = (JComponent)timesTable.getParent();
            model = (DefaultTableModel) timesTable.getModel();
            updateTimesButtons(false);
        }
        if(table == Table.TRACKS) {
            selected = trackList.getSelectedValue();
            trackListModel.clear();
        }
        model.setRowCount(0);
        drawString(Text.getString("updating"), parent, false);
        DBAdapter adapter = new DBAdapter(this);
        Thread thread = new Thread(adapter);
        adapter.table = table;
        thread.start();
    }

    
    public void drawString(String s, JComponent c, boolean error) {
        Graphics2D g = (Graphics2D) c.getGraphics();
        c.update(g);
        g.setFont(new Font(g.getFont().getName(), Font.BOLD, 16));
        int strWidth = g.getFontMetrics().stringWidth(s);

        if(error)
            g.setColor(Color.RED);
        else
            g.setColor(Color.BLACK);

        g.drawString(s, (c.getWidth()-strWidth)/2, c.getHeight()/2);
    }
    
    public void updateDriversButtons(boolean setEnabled) {
        if(setEnabled) {
            driversPrevButton.setEnabled(driversPage > 0);
            driversNextButton.setEnabled((driversPage+1)*maxRows < driversCount);
        }
        else {
            driversPrevButton.setEnabled(false);
            driversNextButton.setEnabled(false);       
        }
        
        driversLabel.setText(Text.getString("drivers_count") +" "+ String.valueOf(driversCount));
    }
    
    public void updateTimesButtons(boolean setEnabled) {
        if(setEnabled) {
            timesPrevButton.setEnabled(timesPage > 0);
            timesNextButton.setEnabled((timesPage+1)*maxRows < timesCount);
        }
        else {
            timesPrevButton.setEnabled(false);
            timesNextButton.setEnabled(false);       
        }
        timesLabel.setText(Text.getString("drivers_count") +" "+ String.valueOf(timesCount));
    }
    
    @Override
    public void updateCompleted(Table table, List<String[]> data, String msg) {
        JComponent parent;
        
        if(data == null) {
            if(table == Table.RACES) {
                parent = (JComponent)racesTable.getParent();
                driversCount = 0;
                updateDriversButtons(true);
            } else {
                parent = (JComponent)timesTable.getParent();
                timesCount = 0;
                updateTimesButtons(true);
            }
            
            if(this.isShowing()) {
                drawString(msg, parent, true); }
            return;
        }
        
        if(table == Table.RACES) {
            parent = (JComponent)racesTable.getParent();
            updateDriversButtons(true);
        } else {
            parent = (JComponent)timesTable.getParent();
            updateTimesButtons(true);
        }
        parent.repaint();
        
        if(table == Table.TRACKS) {
            for(String[] row: data) {
                trackListModel.addElement(row[0]);
            }
            if(selected != null) {
                trackList.setSelectedValue(selected.toString(), true);
            }
            else if(!trackListModel.isEmpty()) {
                trackList.setSelectedIndex(0);
            }
        }
        else {
            DefaultTableModel model;
            if(table == Table.TIMES) {
                model = (DefaultTableModel) timesTable.getModel();
                for(String[] row: data) {
                    row[3] = TimeUtil.genTime(Integer.valueOf(row[3]));
                    model.addRow(row);
                }
            }
            else {
                model = (DefaultTableModel) racesTable.getModel();
                for(String[] row: data) {
                    model.addRow(row);
                }
            }
            
        }
    }
    
    private class DBAdapter implements Runnable{
        public Table table = Table.RACES;
        private DBListener listener;
        
        public DBAdapter(DBListener listener) {
            this.listener = listener;
        }
        
        @Override
        public void run() {
            if(table == Table.RACES) {
                getDrivers();
            }
            else if(table == Table.TIMES) {
                if(trackList.getSelectedValue() != null)
                    getTimes(trackList.getSelectedValue().toString());
            }
            else {
                getTracks();
            }
        }
        
        public Connection connect() throws SQLException {
            String user="client";
            String pass="client";
            String host = Config.get().getString("host");
            int port = Config.get().getInt("dbPort");
            String database = "neurace";
            String connectionURL = "jdbc:derby://"+ host + ":" + port + "/" + database + ";"
                    + "create=true";
            Properties connectionProps = new Properties();
            connectionProps.put("user", user);
            connectionProps.put("password", pass);
            Connection conn = DriverManager.getConnection(connectionURL, connectionProps);
            conn.setSchema("SERVER");
            return conn;
        }
        
        
        public boolean getDrivers() {
            Connection conn = null;
            List data = null;
            String msg = null;
            try {
                conn = connect();

                ResultSet result;
                Statement select = conn.createStatement();
                result = select.executeQuery("SELECT COUNT(driver) FROM Races");
                if(result.next())
                    driversCount = result.getInt(1);
                else
                    return false;

                select.setMaxRows(maxRows);
                result = select.executeQuery(
                        "SELECT * FROM Races ORDER BY points DESC OFFSET " + driversPage*maxRows + " ROWS") ;

                int row = driversPage*maxRows + 1;
                data = new LinkedList<String[]>();
                int cols = racesTable.getColumnCount();
                while (result.next()) {
                    String[] record = new String[cols];
                    record[0] = String.valueOf(row++);
                    for (int i = 1; i < cols; i++) {
                        record[i] = result.getString(i);
                    }
                    data.add(record);
                }
            }
            catch(SQLNonTransientConnectionException e) {
                System.err.println(e.getCause().getMessage());
                msg = Text.getString("db_connect_error");
                return false;
            }
            catch(SQLException e) {
                System.err.println(e.getCause().getMessage());
                msg = Text.getString("db_select_error");
                return false;
            }
            finally {
                if(conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        System.err.printf(e.getMessage());
                        //e.printStackTrace();
                    }
                }
                listener.updateCompleted(table, data, msg);
            }
            return true;
        }
        
        public boolean getTimes(String track) {
            Connection conn = null;
            List data = null;
            String msg = null;
            try {
                conn = connect();

                ResultSet result;
                Statement select = conn.createStatement();
                result = select.executeQuery("SELECT COUNT(driver) FROM " + trackPrefix + track);
                if(result.next())
                    timesCount = result.getInt(1);
                else {
                    return false;
                }

                select.setMaxRows(maxRows);
                result = select.executeQuery(
                        "SELECT * FROM " + trackPrefix + track + " ORDER BY time ASC OFFSET " + timesPage*maxRows + " ROWS") ;

                int row = timesPage*maxRows + 1;
                data = new LinkedList<String[]>();
                int cols = timesTable.getColumnCount();
                while (result.next()) {
                    String[] record = new String[cols];
                    record[0] = String.valueOf(row++);
                    for (int i = 1; i < cols; i++) {
                        record[i] = result.getString(i);
                    }
                    data.add(record);
                }
            }
            catch(SQLNonTransientConnectionException e) {
                System.err.println(e.getCause().getMessage());
                msg = Text.getString("db_connect_error");
                return false;
            }
            catch(SQLException e) {
                System.err.println(e.getCause().getMessage());
                msg = Text.getString("db_select_error");
                return false;
            }
            finally {
                if(conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        System.err.printf(e.getMessage());
                        //e.printStackTrace();
                    }
                }
                listener.updateCompleted(table, data, msg);
            }
            return true;
        }
        
        public boolean getTracks() {
            Connection conn = null;
            List data = null;
            String msg = null;
            try {
                conn = connect();
                
                ResultSet result;
                DatabaseMetaData dbmd = conn.getMetaData();
                result = dbmd.getTables(null, "SERVER", trackPrefix +"%", null);
                String track;
                
                data = new LinkedList<String[]>();
                while (result.next()) {
                    /*if(!(track = result.getString("TABLE_NAME")).equalsIgnoreCase("Races")) {
                        data.add(new String[]{track.toLowerCase()});
                    }*/
                    data.add(new String[]{result.getString("TABLE_NAME").replaceFirst(trackPrefix, "").toLowerCase()});
                }
            }
            catch(SQLNonTransientConnectionException e) {
                System.err.println(e.getCause().getMessage());
                msg = Text.getString("db_connect_error");
                return false;
            }
            catch(SQLException e) {
                System.err.println(e.getCause().getMessage());
                msg = Text.getString("db_select_error");
                return false;
            }
            finally {
                if(conn != null) {
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        System.err.printf(e.getMessage());
                        //e.printStackTrace();
                    }
                }
                listener.updateCompleted(table, data, msg);
            }
            return true;
        }
        
    }
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new JPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        trackPanel = new JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        trackList = new javax.swing.JList();
        tracksLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        timesTable = new javax.swing.JTable(){
            public Component prepareRenderer(
                TableCellRenderer renderer, int row, int column)
            {
                Component c = super.prepareRenderer(renderer, row, column);

                if (!isCellSelected(row, column)) {
                    if(row % 2 == 1) {
                        c.setBackground(new Color(235,245,255));
                    }
                    else {
                        c.setBackground(Color.white);
                    }
                }

                return c;
            }
        };
        jPanel2 = new JPanel();
        refreshButton = new javax.swing.JButton();
        timesPrevButton = new javax.swing.JButton();
        timesNextButton = new javax.swing.JButton();
        timesLabel = new javax.swing.JLabel();
        racesPanel = new JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        racesTable = new javax.swing.JTable(){
            public Component prepareRenderer(
                TableCellRenderer renderer, int row, int column)
            {
                Component c = super.prepareRenderer(renderer, row, column);

                if (!isCellSelected(row, column)) {
                    if(row % 2 == 1) {
                        c.setBackground(new Color(235,245,255));
                    }
                    else {
                        c.setBackground(Color.white);
                    }
                }

                return c;
            }
        };
        tracksLabel1 = new javax.swing.JLabel();
        jPanel3 = new JPanel();
        refreshRacesButton = new javax.swing.JButton();
        driversPrevButton = new javax.swing.JButton();
        driversNextButton = new javax.swing.JButton();
        driversLabel = new javax.swing.JLabel();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        trackPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                trackPanelComponentShown(evt);
            }
        });

        trackList.setModel(trackListModel);
        trackList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        trackList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                LeaderboardTab.this.valueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(trackList);

        tracksLabel.setFont(new Font("Tahoma", 1, 14)); // NOI18N
        tracksLabel.setText(Text.getString("Tracks")); // NOI18N

        jLabel1.setFont(new Font("Tahoma", 1, 14)); // NOI18N
        jLabel1.setText(Text.getString("Best_times")); // NOI18N

        timesTable.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Rank", "Driver", "Car", "Time"
            }
        ) {
            Class[] types = new Class [] {
                String.class, String.class, String.class, String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        timesTable.setColumnSelectionAllowed(true);
        jScrollPane2.setViewportView(timesTable);
        timesTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        timesTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        timesTable.getColumnModel().getColumn(0).setHeaderValue(Text.getString("Rank")); // NOI18N
        timesTable.getColumnModel().getColumn(1).setHeaderValue(Text.getString("Driver")); // NOI18N
        timesTable.getColumnModel().getColumn(2).setHeaderValue(Text.getString("Car")); // NOI18N
        timesTable.getColumnModel().getColumn(3).setHeaderValue(Text.getString("Time")); // NOI18N

        refreshButton.setText(Text.getString("Refresh")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LeaderboardTab.this.actionPerformed(evt);
            }
        });

        timesPrevButton.setText("<");
        timesPrevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timesPrevButtonActionPerformed(evt);
            }
        });

        timesNextButton.setText(">");
        timesNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timesNextButtonActionPerformed(evt);
            }
        });

        timesLabel.setText("0");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(refreshButton, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(timesPrevButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(timesNextButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 386, Short.MAX_VALUE)
                .addComponent(timesLabel))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refreshButton)
                    .addComponent(timesPrevButton)
                    .addComponent(timesNextButton)
                    .addComponent(timesLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout trackPanelLayout = new javax.swing.GroupLayout(trackPanel);
        trackPanel.setLayout(trackPanelLayout);
        trackPanelLayout.setHorizontalGroup(
            trackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(trackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(trackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tracksLabel)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 155, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(trackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1)
                    .addComponent(jScrollPane2)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(51, Short.MAX_VALUE))
        );
        trackPanelLayout.setVerticalGroup(
            trackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(trackPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(trackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(tracksLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(trackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addGap(7, 7, 7)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        tabbedPane.addTab(Text.getString("Tracks"), trackPanel); // NOI18N

        racesPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                racesPanelComponentShown(evt);
            }
        });

        racesTable.setModel(new DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Rank", "Driver", "Points", "Races", "Gold", "Silver", "Bronze"
            }
        ) {
            Class[] types = new Class [] {
                String.class, String.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        racesTable.setColumnSelectionAllowed(true);
        jScrollPane3.setViewportView(racesTable);
        racesTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        racesTable.getColumnModel().getColumn(0).setPreferredWidth(20);
        racesTable.getColumnModel().getColumn(0).setHeaderValue(Text.getString("Rank")); // NOI18N
        racesTable.getColumnModel().getColumn(1).setHeaderValue(Text.getString("Driver")); // NOI18N
        racesTable.getColumnModel().getColumn(2).setHeaderValue(Text.getString("Points")); // NOI18N
        racesTable.getColumnModel().getColumn(3).setHeaderValue(Text.getString("Races")); // NOI18N
        racesTable.getColumnModel().getColumn(4).setPreferredWidth(20);
        racesTable.getColumnModel().getColumn(4).setHeaderValue(Text.getString("Gold")); // NOI18N
        racesTable.getColumnModel().getColumn(5).setPreferredWidth(20);
        racesTable.getColumnModel().getColumn(5).setHeaderValue(Text.getString("Silver")); // NOI18N
        racesTable.getColumnModel().getColumn(6).setPreferredWidth(20);
        racesTable.getColumnModel().getColumn(6).setHeaderValue(Text.getString("Bronze")); // NOI18N

        tracksLabel1.setFont(new Font("Tahoma", 1, 14)); // NOI18N
        tracksLabel1.setText(Text.getString("Drivers_leaderboard")); // NOI18N

        refreshRacesButton.setText(Text.getString("Refresh")); // NOI18N
        refreshRacesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LeaderboardTab.this.actionPerformed(evt);
            }
        });

        driversPrevButton.setFont(new Font("Tahoma", 1, 11)); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("Text"); // NOI18N
        driversPrevButton.setText(bundle.getString("previous")); // NOI18N
        driversPrevButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                driversPrevButtonActionPerformed(evt);
            }
        });

        driversNextButton.setFont(new Font("Tahoma", 1, 11)); // NOI18N
        driversNextButton.setText(bundle.getString("next")); // NOI18N
        driversNextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                driversNextButtonActionPerformed(evt);
            }
        });

        driversLabel.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(refreshRacesButton)
                .addGap(18, 18, 18)
                .addComponent(driversPrevButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(driversNextButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 576, Short.MAX_VALUE)
                .addComponent(driversLabel))
        );

        jPanel3Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new Component[] {driversNextButton, driversPrevButton});

        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refreshRacesButton)
                    .addComponent(driversPrevButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(driversNextButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(driversLabel)))
        );

        javax.swing.GroupLayout racesPanelLayout = new javax.swing.GroupLayout(racesPanel);
        racesPanel.setLayout(racesPanelLayout);
        racesPanelLayout.setHorizontalGroup(
            racesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(racesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(racesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tracksLabel1)
                    .addComponent(jScrollPane3)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(39, Short.MAX_VALUE))
        );
        racesPanelLayout.setVerticalGroup(
            racesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(racesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tracksLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 402, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        tabbedPane.addTab(Text.getString("Racers"), racesPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane)
        );

        tabbedPane.getAccessibleContext().setAccessibleName(Text.getString("Tracks")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        try {
            if(tabbedPane.getSelectedComponent() == trackPanel)
                updateTable(Table.TRACKS);
            else {
                updateTable(Table.RACES);
            }
        }
        catch(Exception e) {
            System.err.println(e.getMessage());
            //MainWindow.get().errorMsg(Text.translate(e.getMessage()));
        }
    }//GEN-LAST:event_formComponentShown

    private void actionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_actionPerformed
        if(evt.getSource() == refreshButton) {
            updateTable(Table.TIMES);
        }
        else if(evt.getSource() == refreshRacesButton) {
            updateTable(Table.RACES);
        }
    }//GEN-LAST:event_actionPerformed

    private void valueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_valueChanged
        if(trackList.getSelectedIndex() != -1) {
            timesPage = 0;
            updateTable(Table.TIMES);
        }
    }//GEN-LAST:event_valueChanged

    private void driversNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_driversNextButtonActionPerformed
        driversPage++;
        updateTable(Table.RACES);
    }//GEN-LAST:event_driversNextButtonActionPerformed

    private void driversPrevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_driversPrevButtonActionPerformed
        driversPage--;
        updateTable(Table.RACES);
    }//GEN-LAST:event_driversPrevButtonActionPerformed

    private void timesNextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timesNextButtonActionPerformed
        timesPage++;
        updateTable(Table.TIMES);
    }//GEN-LAST:event_timesNextButtonActionPerformed

    private void timesPrevButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timesPrevButtonActionPerformed
        timesPage--;
        updateTable(Table.TIMES);
    }//GEN-LAST:event_timesPrevButtonActionPerformed

    private void trackPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_trackPanelComponentShown
        if(updateTracks) {
            updateTable(Table.TRACKS);
        }
        else {
            updateTracks = true;
        }
    }//GEN-LAST:event_trackPanelComponentShown

    private void racesPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_racesPanelComponentShown
        updateTable(Table.RACES);
    }//GEN-LAST:event_racesPanelComponentShown


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel driversLabel;
    private javax.swing.JButton driversNextButton;
    private javax.swing.JButton driversPrevButton;
    private javax.swing.JLabel jLabel1;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private JPanel racesPanel;
    private javax.swing.JTable racesTable;
    private javax.swing.JButton refreshButton;
    private javax.swing.JButton refreshRacesButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JLabel timesLabel;
    private javax.swing.JButton timesNextButton;
    private javax.swing.JButton timesPrevButton;
    private javax.swing.JTable timesTable;
    private javax.swing.JList trackList;
    private JPanel trackPanel;
    private javax.swing.JLabel tracksLabel;
    private javax.swing.JLabel tracksLabel1;
    // End of variables declaration//GEN-END:variables

}
