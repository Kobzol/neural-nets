package cz.vsb.cs.neurace.gui.track;

import cz.vsb.cs.neurace.track.Track;
import cz.vsb.cs.neurace.track.TrackPoint;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Označený bod v editoru tratí.
 */
public class SelectPoint {

	/** Bod, který je označený */
	private TrackPoint point;
	/** část bodu, která je označená */
	private Selected selected;

	/** části bodu, které se můžou menit */
	public enum Selected {
		/** celý bod, označen střed */
		POINT, 
		/** konec vektoru vycházející křivky */
		FROM, 
		/** konec vektoru vcházející křivky */
		TO
	}

	/**
	 * Konstruktor
	 * 
	 * @param track
	 * @param point
	 * @param x
	 * @param y
	 */
	public SelectPoint(Track track, TrackPoint point, float x, float y) {
		this.point = point;
		selectPart(x, y);
	}

	/**
	 * Vykreslí bod pro editaci
	 * @param g2
	 */
	public void paint(Graphics2D g2, float scale) {
		float a = (point.getX() - point.getFromX())*scale ;
		float b = (point.getY() - point.getFromY())*scale;
		if (a == 0 && b == 0) {
			return;
		}
		double theta = Math.atan(b / a);
		if (a >= 0) {
			theta += Math.PI;
		}
                g2.setColor(Color.BLACK);
		g2.translate(point.getX()*scale, point.getY()*scale);
		g2.rotate(theta);
		int df = (int)(point.lengthFrom()*scale);
		g2.drawLine(0, 0, df, 0);
		g2.drawLine(df, 0, df - 5, 5);
		g2.drawLine(df, 0, df - 5, -5);
		int dt = -(int)(point.lengthTo()*scale);
		g2.drawLine(dt, 0, 0, 0);
		g2.drawLine(dt, 0, dt + 5, 5);
		g2.drawLine(dt, 0, dt + 5, -5);
		g2.rotate(-theta);
		g2.translate(-point.getX()*scale, -point.getY()*scale);
	}

	/**
	 * Na základě souřadnic zjistí, která část se vybraná
	 * @param x
	 * @param y
	 * @return true pokud je vybraný bod, jinak false.
	 */
	public final boolean selectPart(float x, float y) {
		if (point.lengthFrom(x, y) < 10) {
			selected = Selected.FROM;
		} else if (point.lengthTo(x, y) < 10) {
			selected = Selected.TO;
		} else if (point.length(x, y) < 10) {
			selected = Selected.POINT;
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Přesune bod, nebo některou část na [x,y]
	 * @param x
	 * @param y
	 */
	public void move(float x, float y) {
		if (selected == Selected.FROM) {
			point.moveFrom(x, y);
		} else if (selected == Selected.TO) {
			point.moveTo(x, y);
		} else {
			point.move(x, y);
		}
	}

	/**
	 * Vrátí bod, který edituje
	 * @return
	 */
	public TrackPoint getPoint() {
		return point;
	}
}
