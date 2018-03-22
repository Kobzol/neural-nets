package cz.vsb.cs.neurace.gui;

import cz.vsb.cs.neurace.server.Server;

import java.io.File;
import java.io.IOException;

import org.lwjgl.LWJGLException;

import com.bulletphysics.demos.vehicle.VehicleDemo;

/**
 * Slouží jenom jako spouštěč systému podle argumentů, které dostane.
 * @author marian
 */
public class Init {

	/**
	 * Spouští aplikaci. Rozděluje na server a okenního klienta.
	 * 
	 * @param args argumenty programu.
	 * @throws IOException
	 * @throws LWJGLException 
	 */


	public static void main(String[] args) throws IOException, LWJGLException {
		if (args.length > 0) {
			Server.main(args);
		} else {
                    //detekce systému pro LWJGL
                    String os = System.getProperty("os.name");
                    if(os.toLowerCase().contains("win")) {
                        os = "windows";
                    }
                    else if(os.toLowerCase().contains("mac")) {
                        os = "macosx";
                    }
                    else if(os.toLowerCase().contains("linux")) {
                        os = "linux";
                    }
                    else if(os.toLowerCase().contains("sunos")) {
                        os = "solaris";
                    }
                    //System.setProperty("org.lwjgl.librarypath",System.getProperty("user.dir") + "/client/lwjgl/"+os);
                    System.setProperty("org.lwjgl.librarypath", Resources.getExecLocationPath() +  File.separator + "bin_lib" + File.separator + "lwjgl" + File.separator + os);

                    MainWindow.main(args);
                    //VehicleDemo.main(args);
		}
	}
}
