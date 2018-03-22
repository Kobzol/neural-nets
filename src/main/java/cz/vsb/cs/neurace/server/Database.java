package cz.vsb.cs.neurace.server;
import cz.vsb.cs.neurace.gui.Config;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Ukládání výsledků do databáze
 * @author Petr Hamalčík
 */
public class Database {

    Connection conn;
    String errorMsg = "";
    static String DBdriver = "org.apache.derby.jdbc.ClientDriver";
    String user ="server";
    String pass = Config.get().get("dbPassword");
    List<String> messages = new LinkedList<String>();
    final String trackPrefix = "NTR_";
    
    public boolean checkDriver() {
        try {
            Class.forName(DBdriver);
        } catch (ClassNotFoundException e) {
            errorMsg = e.getLocalizedMessage();
            //e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean connect() {
        String database = "neurace";
        String host = "localhost";
        int port = Config.prefs.getInt("dbPort", 1527);
        String connectionURL = "jdbc:derby://"+ host + ":" + port + "/" + database + ";"
                + "user=" + user + ";"
                + "password=" + pass + ";"
                + "create=true";
        //localhost:1527/
        try {
            conn = DriverManager.getConnection(connectionURL);
        }
        catch(SQLException e) {
            errorMsg = e.getLocalizedMessage();
            //e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public boolean disconnect() {
        try {
            conn.close();
        }
        catch(SQLException e) {
            errorMsg = e.getLocalizedMessage();
            //e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public List<String> getMessages() {
        return messages;
    }
    public String getError() {
        return errorMsg;
    }
    
    public boolean updateDrivers(List<RacesRecord> drivers) {
        ResultSet result;
        PreparedStatement select;
        PreparedStatement update;
        PreparedStatement insert;
        
        try {
            select = conn.prepareStatement("SELECT * FROM Races WHERE driver=?");
            update = conn.prepareStatement("UPDATE Races SET points=?, races=?, gold=?, silver=?, bronze=? WHERE driver=?");
            insert = conn.prepareStatement("INSERT INTO Races VALUES (?,?,?,?,?,?)");
        }
        catch(SQLException e) {
            try {
                Statement create = conn.createStatement();
                create.execute("CREATE TABLE Races (Driver VARCHAR(255) NOT NULL PRIMARY KEY, points INT, races INT, gold INT, silver INT, bronze INT)");
                select = conn.prepareStatement("SELECT * FROM Races WHERE driver=?");
                update = conn.prepareStatement("UPDATE Races SET points=?, races=?, gold=?, silver=?, bronze=? WHERE driver=?");
                insert = conn.prepareStatement("INSERT INTO Races VALUES (?,?,?,?,?,?)");
            }
            catch(SQLException f) {
                errorMsg = f.getLocalizedMessage();
                return false;
            }
        }
        
        for(RacesRecord rr: drivers) {
            try {
                select.setString(1, rr.getDriver());
                result = select.executeQuery();
            
                if(result != null && result.next()) {

                    update.setInt(1, result.getInt("points") + rr.getPoints());
                    update.setInt(2, result.getInt("races") + rr.getRaces());

                    update.setInt(3, result.getInt("gold") + rr.getGold());
                    update.setInt(4, result.getInt("silver") + rr.getSilver());
                    update.setInt(5, result.getInt("bronze") + rr.getBronze());
                    update.setString(6, rr.getDriver());
                    update.execute();
                }
                else {
                    insert.setString(1, rr.getDriver());
                    insert.setInt(2, rr.getPoints());
                    insert.setInt(3, rr.getRaces());
                    insert.setInt(4, rr.getGold());
                    insert.setInt(5, rr.getSilver());
                    insert.setInt(6, rr.getBronze());
                    insert.execute();
                }
                if(result != null) {
                    result.close();
                }

            } catch (SQLException e) {
                errorMsg = e.getLocalizedMessage();
                //e.printStackTrace();
                return false;
            } catch (Exception e) {
                errorMsg = e.getLocalizedMessage();
                //e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    public boolean updateTimes(String track, List<TrackRecord> times) {
        ResultSet result;
        PreparedStatement select;
        PreparedStatement update;
        PreparedStatement insert = null;
        
        
        try {
            select = conn.prepareStatement("SELECT time FROM " + trackPrefix + track + " WHERE driver=?");
            update = conn.prepareStatement("UPDATE " + trackPrefix + track + " SET car=?, time=? WHERE driver=?");
        }
        catch(SQLException e) {
            try {
                Statement create = conn.createStatement();
                create.execute("CREATE TABLE " + trackPrefix + track + " (Driver VARCHAR(255) NOT NULL PRIMARY KEY, Car VARCHAR(255), time INT)");
                select = conn.prepareStatement("SELECT time FROM " + trackPrefix + track + " WHERE driver=?");
                update = conn.prepareStatement("UPDATE " + trackPrefix + track + " SET car=?, time=? WHERE driver=?");
            }
            catch(SQLException f) {
                errorMsg = f.getLocalizedMessage();
                return false;
            }
        }

        HashMap<String, String> map = new HashMap<String, String>();
        
        for(TrackRecord tr: times) {
            try {
                select.setString(1, tr.getDriver());
                result = select.executeQuery();
                
                if(result != null && result.next()) {
                    if(result.getInt("time") > tr.getTime()) {
                        
                        update.setString(1, tr.getCar());
                        update.setInt(2, tr.getTime());
                        update.setString(3, tr.getDriver());
                        update.execute();
                        map.put(tr.getDriver(), "driver best time");
                    }
                }
                else {
                    if(insert == null) {
                        insert = conn.prepareStatement("INSERT INTO " + trackPrefix + track + " VALUES(?, ?, ?)");
                    }
                    insert.setString(1, tr.getDriver());
                    insert.setString(2, tr.getCar());
                    insert.setInt(3, tr.getTime());
                    insert.execute();
                    map.put(tr.getDriver(), "driver best time");
                }
             
                
                if(result != null) {
                    result.close();
                }

            } catch (SQLException e) {
                errorMsg = e.getLocalizedMessage();
                return false;
            } catch (Exception e) {
                errorMsg = e.getLocalizedMessage();
                return false;
            }
            
            
        }
        
        try {
                Statement top = conn.createStatement();
                top.setMaxRows(3);
                result = top.executeQuery("SELECT driver FROM " + trackPrefix + track + " ORDER BY time");
                int rank = 1;
                String driver;
                while(result != null && result.next()) {
                    driver = result.getString("driver");
                    if(map.containsKey(driver)) {
                        map.put(driver, rank + ":best time");
                    }
                    rank++;
                }
                for(String drv: map.keySet()) {
                    messages.add(drv + ":" + map.get(drv));
                }
        } catch (SQLException e) {
                errorMsg = e.getLocalizedMessage();
                return false;
        }
        
        
        return true;
    }  
    
    
    /*public static void main(String[] args) {
        Database db = new Database();
        boolean ok = db.connect();
        if(!ok) {
            System.err.println(db.getError());
            return;
        }
        
        ArrayList<TrackRecord> times = new ArrayList<TrackRecord>();
        times.add(new TrackRecord("zavodnik", "trabant", 16025));
        for(int i = 0; i < 10; i++) {
            times.add(new TrackRecord("driver"+i, "lamborghini", 12356+100*i));
        }
        ok = db.updateTimes("trat13", times);
        if(!ok) {
            System.err.println(db.getError());
        }
        ArrayList<RacesRecord> drivers = new ArrayList<RacesRecord>();
        //drivers.add(new RacesRecord("ham112", 1, 1, 0, 0, 4));
        //drivers.add(new RacesRecord("a", 1, 0, 1, 0, 3));
        drivers.add(new RacesRecord("b", 1, 0, 0, 1, 2));
        //drivers.add(new RacesRecord("c", 1, 0, 0, 0, 1));
        //drivers.add(new RacesRecord("d", 1, 0, 0, 0, 0));
        for(int i = 0; i < 100; i++) {
            drivers.add(new RacesRecord("driver"+i, 1, 0, 0, 0, i));
        }
        ok = db.updateDrivers(drivers);
        if(!ok) {
            System.err.println(db.getError());
        }
    }*/
}
