package cz.vsb.cs.neurace.race;
import java.awt.Point;

/**
 * Kontrolní body na trati. Kontrola projetím.
 * 
 */
public class Checkpoint {

	/** souřadnice krajních bodů */
	private float x1,  y1,  x2,  y2;
	/** číslo checkpointu na trati */
	private int index;
	/** velikost checkpoitu [m]*/
	private float checkpointSize = 40;
        /** tolerance při kontrole projetí 0.0 až 1.0 */
	private float tolerance = 0.5f;
        /** souřadnice středu */
        private float x, y;

	/**
	 * Konstruktor. 
	 * @param x souřadnice x
	 * @param y souřadnice y
	 * @param alpha úhel trati v místě checkpoitu
	 * @param index číslo checkpointu na trati
	 */
	public Checkpoint(float x, float y, float alpha, float size, int index) {
		this.index = index;
                this.x = x;
                this.y = y;
                this.checkpointSize = size;

		x1 = x + checkpointSize / 2 * (float) Math.cos(alpha + Math.PI / 2);
		y1 = y + checkpointSize / 2 * (float) Math.sin(alpha + Math.PI / 2);

		x2 = x + checkpointSize / 2 * (float) Math.cos(alpha - Math.PI / 2);
		y2 = y + checkpointSize / 2 * (float) Math.sin(alpha - Math.PI / 2);
	}

	/**
	 * Kontrola, zda auto z bodu [x1,y1] do bodu [x2,y2] projelo checkpoitem.
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public boolean check(float x1, float y1, float x2, float y2) {
		float x3 = this.x1;
		float y3 = this.y1;
		float x4 = this.x2;
		float y4 = this.y2;
		try {
			float ta = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
			if (ta+tolerance < 0 || ta-tolerance > 1) {
				return false;
			}
			float tb = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / ((y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1));
			if (tb+tolerance < 0 || tb-tolerance > 1) {
				return false;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Vrátí číslo checkpointu na trati.
	 * @return číslo checkpointu na trati.
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * Vrátí zda je toto první checkpoint.
	 * @return true pokud je toto první checkpoint, jinak false.
	 */
	public boolean isFirst() {
		return index == 0;
	}

        /**
         * Vrátí krajní body checkpointu.
         * @return krajní body
         */
        public Point[] getBorderPoints() {
            Point[] points = new Point[2];
            points[0] = new Point((int)x1, (int)y1);
            points[1] = new Point((int)x2, (int)y2);
            return points;
	}

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public float getX1() {
            return x1;
        }

        public float getX2() {
            return x2;
        }

        public float getY1() {
            return y1;
        }

        public float getY2() {
            return y2;
        }
}
