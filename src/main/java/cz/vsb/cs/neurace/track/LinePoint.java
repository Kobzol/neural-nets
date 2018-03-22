package cz.vsb.cs.neurace.track;

/**
 * Jednoduchý bod na trati.
 * 
 */
public class LinePoint {
	/** souřadnice bodu */
	public float x, y;
	
	/** Konstruktor, který nenastavuje soužadnice */
	public LinePoint() {
	}

	/** Konstruktor, který nastaví souřadnice na [x,y] */
	public LinePoint(float x, float y) {
		this.x = x;
		this.y = y;
	}

}
