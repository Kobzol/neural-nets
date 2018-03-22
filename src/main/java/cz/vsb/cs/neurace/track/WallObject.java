package cz.vsb.cs.neurace.track;

import com.bulletphysics.dynamics.RigidBody;
import java.util.LinkedList;
import java.util.List;
import javax.vecmath.Point2f;

/**
 * Zeď složená ze spojených kvádrů
 * @author Petr Hamalčík
 */
public class WallObject extends TrackObject{

    /** body kterými zeď prochází (bez počátečního bodu)*/
    LinkedList<Point2f> points = new LinkedList<Point2f>();
    /** tělesa která tvoří zeď */
    private List<RigidBody> bodies = new LinkedList<RigidBody>();
    /** výška zdi */ 
    float height;
    /** šířka zdi */
    float width;

    public WallObject(float posX, float posY, String type, float height, float width) {
        super(posX, posY, type);
        this.height = height;
        this.width = width;
    }

    public WallObject(float posX, float posY, String type, float height, float width,
            LinkedList<Point2f> points) {
        super(posX, posY, type);
        this.height = height;
        this.width = width;
        this.points = points;
    }

    public WallObject(WallObject o) {
        super(0, 0, o.getType());
        this.height = o.height;
        this.width = o.width;
    }


    public void addPoint(Point2f point) {
        points.add(point);
    }

    public void movePoint(int index, Point2f point) {
        points.get(index).set(point);
    }

    public void removePoint(Point2f point) {
        points.remove(point);
    }

    public float getHeight() {
        return height;
    }

    public float getWidth() {
        return width;
    }


    public LinkedList<Point2f> getPoints() {
        return points;
    }
    
    
    public void addBody(RigidBody body) {
        this.bodies.add(body);
    }

    public List<RigidBody> getBodies() {
        return bodies;
    }
}
