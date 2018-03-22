/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.vsb.cs.neurace.server;

import cz.vsb.cs.neurace.gui.TimeUtil;

public class TrackRecord implements Comparable{
        private String driver;
        private String car;
        private int time;

        public TrackRecord(String driver, String car, int time) {
            this.driver = driver;
            this.car = car;
            this.time = time;
        }

        public String getDriver() {
            return driver;
        }

        public String getCar() {
            return car;
        }

        public void setCar(String car) {
            this.car = car;
        }

        public int getTime() {
            return time;
        }

        public String getFormatedTime() {
            return TimeUtil.genTime(time);
        }

        public void setTime(int time) {
            this.time = time;
        }

        public void setRecord(String car, int time) {
            this.car = car;
            this.time = time;
        }

        @Override
        public int compareTo(Object o) {
            TrackRecord r = (TrackRecord) o;
            if(this.time > r.time)
                return 1;
            else if(this.time < r.time)
                return -1;
            else
                return 0;
        }
    }
