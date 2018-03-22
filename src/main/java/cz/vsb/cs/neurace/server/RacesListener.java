package cz.vsb.cs.neurace.server;

/**
 * Interface pro změny v závodech na serveru.
 * 
 */
public interface RacesListener {

	/**
	 * Změna v závodech. Nový nebo ukončený závod.
	 */
	public void racesAction();
}
