package cz.vsb.cs.neurace.track;

import com.bulletphysics.dynamics.RigidBody;

/**
 * Objekt ve tvaru kvádru
 * @author Petr Hamalcik
 */
public class BlockObject extends TrackObject{

    
    private float length;
    private float width;
    private float height;
    private float angle;
    
    private RigidBody body;

    public BlockObject(float posX, float posY, String type, float length, float width, float height, float angle) {
        super(posX, posY, type);
        
        this.length = length;
        this.width = width;
        this.height = height;
        this.angle = angle;
    }

    public BlockObject(BlockObject o) {
        super(0, 0, o.getType());
        this.length = o.length;
        this.width = o.width;
        this.height = o.height;
        this.angle = o.angle;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public float getLength() {
        return length;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public RigidBody getBody() {
        return body;
    }

    public void setBody(RigidBody body) {
        this.body = body;
    }

}
