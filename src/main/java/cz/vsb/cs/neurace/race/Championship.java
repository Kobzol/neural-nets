package cz.vsb.cs.neurace.race;

import java.util.HashMap;

/**
 * Šampionát - série navazujících závodů.
 * Body získané v jetnotlivých závodech se sčítají.
 * @author Petr Hamalčík
 */
public class Championship {
    /** počet závodů série */
    private int races;
    /** pořadové číslo aktuálního závodu */
    private int raceNumber;
    /** body získané v šampionátu jednotlivými závodníky. */
    private HashMap<String, Integer> scores = new HashMap<String, Integer>();

    
    public Championship(int races) {
        this.races = races;
        this.raceNumber = 1;
    }

    /** Vymaže tabulku výsledků. */
    public void reset() {
        this.scores.clear();
        this.raceNumber = 1;
    }

    /**
     * Přidá body vybranému závodníkovi
     * @param racer závodník
     * @param points počet bodů
     */
    public void addPoints(String racer, int points) {
        if(scores.containsKey(racer)) {
            int s = scores.get(racer);
            scores.put(racer, s+points);
        }
        else
            scores.put(racer, points);
    }

    /** Ukončí závod, zvýší pořadové číslo závodu */
    public void finishRace() {
        raceNumber++;
    }

    public HashMap<String, Integer> getScoreboard() {
        return scores;
    }

    public int getRaces() {
        return races;
    }

    public void setRaces(int races) {
        this.races = races;
    }

    public int getRaceNumber() {
        return raceNumber;
    }

    public int getScore(String racer) {
        return scores.get(racer);
    }

}
