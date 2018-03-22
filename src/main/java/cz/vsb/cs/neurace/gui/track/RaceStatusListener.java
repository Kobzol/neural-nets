package cz.vsb.cs.neurace.gui.track;

import cz.vsb.cs.neurace.race.Race.Status;

/**
 * Rozhraní, které je pro sledování změn stavů závodů.
 */
public interface RaceStatusListener {
	/**
	 * Změna stavu závodu.
	 * @param status
	 */
	public void changeRaceStatus(Status status);
}
