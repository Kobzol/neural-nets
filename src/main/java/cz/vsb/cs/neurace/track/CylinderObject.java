package cz.vsb.cs.neurace.track;

import com.bulletphysics.dynamics.RigidBody;

/**
 * Objekt ve tvaru válce
 * @author Petr Hamalčík
 */
public class CylinderObject extends TrackObject{
    private float radius;
    private float height;
    
    private RigidBody body;

    public CylinderObject(float posX, float posY, String type, float height, float radius) {
        super(posX, posY, type);
        this.height = height;
        this.radius = radius;
    }

    public CylinderObject(CylinderObject o) {
        super(0, 0, o.getType());
        this.height = o.height;
        this.radius = o.radius;
    }

        public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }


    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public RigidBody getBody() {
        return body;
    }

    public void setBody(RigidBody body) {
        this.body = body;
    }
}
