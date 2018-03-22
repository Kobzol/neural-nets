package cz.vsb.cs.neurace.track;

import java.util.LinkedList;
import java.util.List;

/**
 * Křivka mezi dvěma body.
 * 
 */
public class Line {
	/** Pole bodů tvořících křivku. */
	private List<LinePoint> points;

	/** počáteční a koncový bod */
	private TrackPoint p1, p2;

	/**
	 * Křivka mezi dvěma body.
	 * @param p1
	 * @param p2
	 */
	public Line(TrackPoint p1, TrackPoint p2) {
		this.p1 = p1;
		this.p2 = p2;
		generate();
	}

	/**
	 * Pole bodů tvořících křivku.
	 * @return pole bodů tvořících křivku.
	 */
	public LinePoint[] getLinePoints() {
		return points.toArray(new LinePoint[points.size()]);
	}
	
	/**
	 * Generování křivky.
	 */
	public final void generate() {
            List<LinePoint> line = new LinkedList<LinePoint>();
            double dt;
            int i;
            int numberOfPoints = 20;
            dt = 1.0 / (numberOfPoints - 1);

            for (i = 0; i < numberOfPoints; i++) {
                    double ax, bx, cx;
                    double ay, by, cy;
                    double tSquared, tCubed;
                    double t = i * dt;
                    LinePoint p = new LinePoint();

                    cx = 3.0 * (p1.getFromX() - p1.getX());
                    bx = 3.0 * (p2.getToX() - p1.getFromX()) - cx;
                    ax = p2.getX() - p1.getX() - cx - bx;

                    cy = 3.0 * (p1.getFromY() - p1.getY());
                    by = 3.0 * (p2.getToY() - p1.getFromY()) - cy;
                    ay = p2.getY() - p1.getY() - cy - by;

                    tSquared = t * t;
                    tCubed = tSquared * t;

                    p.x = (float) ((ax * tCubed) + (bx * tSquared) + (cx * t) + p1.getX());
                    p.y = (float) ((ay * tCubed) + (by * tSquared) + (cy * t) + p1.getY());

                    line.add(p);
            }
            this.points = line;
	}

}
