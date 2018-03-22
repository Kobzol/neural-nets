package cz.vsb.cs.neurace.server;

import cz.vsb.cs.neurace.gui.Resources;
import cz.vsb.cs.neurace.race.PhysicalCar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;

import com.sun.org.glassfish.external.statistics.annotations.Reset;


/**
 *
 * @author Petr Hamalčík
 */
public class Garage {
    private static Garage garageInstance;
    
    private String serverCarsDir;
    private String localCarsDir = System.getProperty("user.dir") + File.separator + "client" + File.separator + "cars";
    
    /**
        * Vrátí instanci Garage.
        * 
        * @return Garage instance.
        */
    public static Garage get() {
            if (garageInstance == null) {
                    garageInstance = new Garage();
            }
            return garageInstance;
    }
    
    public Collection<String> availableCars() {
        return Resources.get().getCarNames();//availableCars(serverCarsDir);
    }
    
//    public Collection<String> availableCarsLocal() {
//        return Resources.get().getCarNames();//availableCars(localCarsDir);
//    }
    
//    private Collection<String> availableCars(String carsDir) {
//        File dir = new File(carsDir);
//        String[] availableCars;
//        if(dir.canRead() && dir.isDirectory()) {
//            FilenameFilter filter = new FilenameFilter() {
//                @Override
//                public boolean accept(File dir, String name) {
//                    return name.endsWith(".txt");
//                }
//            };
//            availableCars = dir.list(filter);
//            if(availableCars == null) {
//                return null;
//            }
//            for(int i = 0; i < availableCars.length; i++) {
//                availableCars[i] = availableCars[i].replace(".txt", "");
//            }
//            return availableCars;
//        }
//        else
//            return null;
//    }
    
//    public PhysicalCar getCarLocal(String carType) {
//        return getCar(carType, localCarsDir);
//    }
    
    public PhysicalCar getCar(String carType) {
        return new PhysicalCar(Resources.get().getCarData(carType));
    }
    
    
//    private PhysicalCar getCar(String carType, String carsDir) {
//        String carPath = carsDir + File.separatorChar + carType + ".txt";
//        PhysicalCar car;
//        try {
//            car = new PhysicalCar(carPath);
//
//        }
//        catch(FileNotFoundException e) {
//            //e.printStackTrace();
//            return null;
//        }
//        catch(IOException e) {
//            System.err.println(e.getMessage());
//            //e.printStackTrace();
//            return null;
//        } 
//        return car;
//    }
    
    
    
    public String getServerCarsDir() {
        return serverCarsDir;
    }

    public void setServerCarsDir(String carDir) {
        serverCarsDir = carDir;
    }
}
