package cz.vsb.cs.neurace.race;

/**
 * Rozraní pro sledování změn v závodě.
 */
public interface RaceListener {

	/**
	 * Proveden jedno posunutí aut.
	 */
	public void round();
}
