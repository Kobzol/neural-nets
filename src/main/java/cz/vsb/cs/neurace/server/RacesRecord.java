/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vsb.cs.neurace.server;

public class RacesRecord implements Comparable{
        private String driver;
        private int races;
        private int gold;
        private int silver;
        private int bronze;
        private int points;

        
        public RacesRecord(String driver) {
            this.driver = driver;
            this.races = 0;
            this.gold = 0;
            this.silver = 0;
            this.bronze = 0;
            this.points = 0;
        }

        public RacesRecord(String driver, int races, int gold, int silver, int bronze, int points) {
            this.driver = driver;
            this.races = races;
            this.gold = gold;
            this.silver = silver;
            this.bronze = bronze;
            this.points = points;
        }



        public void updateValues(int races, int gold, int silver, int bronze, int points) {
            this.races += races;
            this.gold += gold;
            this.silver += silver;
            this.bronze += bronze;
            this.points += points;
        }

        public String getDriver() {
            return driver;
        }

        public int getBronze() {
            return bronze;
        }

        public void setBronze(int bronze) {
            this.bronze = bronze;
        }

        public int getGold() {
            return gold;
        }

        public void setGold(int gold) {
            this.gold = gold;
        }

        public int getPoints() {
            return points;
        }

        public void setPoints(int points) {
            this.points = points;
        }

        public int getRaces() {
            return races;
        }

        public void setRaces(int races) {
            this.races = races;
        }

        public int getSilver() {
            return silver;
        }

        public void setSilver(int silver) {
            this.silver = silver;
        }

        @Override
        public int compareTo(Object o) {
            RacesRecord r = (RacesRecord) o;
            if(this.points < r.points)
                return 1;
            else if(this.points > r.points)
                return -1;
            else
                return 0;
        }
    }