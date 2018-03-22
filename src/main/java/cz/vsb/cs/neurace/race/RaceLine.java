package cz.vsb.cs.neurace.race;

import cz.vsb.cs.neurace.track.LinePoint;
import cz.vsb.cs.neurace.track.TrackPoint;

/**
 * Úsečka v trati.
 * 
 */
public class RaceLine {

	/**
	 * Souřadnice koncových bodů úsečky.
	 */
	private float x1,  y1,  x2,  y2;
	/**
	 * Úhel úsečky.
	 */
	private float theta;
	/**
	 * Délka úsečky.
	 */
	private float length;
	/**
	 * Číslo úsečky na trati.
	 */
	private int index;

        private int prevCheckpoint;

        private TrackPoint prevPoint;

	/**
	 * Konstruktor
	 * @param p1 počáteční pod
	 * @param p2 koncový bod
	 * @param index
	 */
	public RaceLine(LinePoint p1, LinePoint p2, int index, int checkPoint, TrackPoint prevPoint) {
		x1 = p1.x;
		y1 = p1.y;
		x2 = p2.x;
		y2 = p2.y;
		length = length(x1, y1, x2, y2);

		float tx = this.x2 - this.x1;
		float ty = this.y2 - this.y1;
		theta = (float) Math.acos(tx / length);
		if (ty < 0) {
			theta *= -1;
		}

		this.index = index;
                this.prevCheckpoint = checkPoint;
                this.prevPoint = prevPoint;
	}

	/**
	 * Délka úsečky.
	 * 
	 * @return
	 */
	public float getLength() {
		return length;
	}

	/**
	 * Úhel úsečky.
	 * 
	 * @return
	 */
	public float getTheta() {
		return theta;
	}

	/**
	 * Výpočtení vzdálenosti dvou bodů.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	private float length(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	/**
	 * Vzdálenost bodu x,y od úsečky.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public float getDistance(float x, float y) {
		float sin = (y2 - y1) / length;
		float cos = (x2 - x1) / length;
		float xo = (x - x1) * cos + (y - y1) * sin;
		float yo = -(x - x1) * sin + (y - y1) * cos;
		if (xo < 0) {
			return (float) (length(0, 0, xo, yo) * (yo < 0 ? -1 : 1));
		} else if (xo > length) {
			return (float) (length(length, 0, xo, yo) * (yo < 0 ? -1 : 1));
		} else {
			return (float) yo;
		}
	}

	/**
	 * Číslo úsečky na trati.
	 * @return Číslo úsečky na trati.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Souřadnice X počátku úsečky.
	 * @return
	 */
	public float getX1() {
		return x1;
	}

	/**
	 * Souřadnice Y počátku úsečky.
	 * @return
	 */
	public float getY1() {
		return y1;
	}

	/**
	 * Souřadnice X konce úsečky.
	 * @return
	 */
	public float getX2() {
		return x2;
	}

	/**
	 * Souřadnice Y konce úsečky.
	 * @return
	 */
	public float getY2() {
		return y2;
	}


        public int getPrevCheckpoint() {
            return prevCheckpoint;
        }

        public TrackPoint getPrevPoint() {
            return prevPoint;
        }

        public float getFriction() {
            return this.getPrevPoint().getSurface().getFriction();
        }

}
