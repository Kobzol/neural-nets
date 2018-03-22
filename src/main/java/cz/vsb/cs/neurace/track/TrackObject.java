package cz.vsb.cs.neurace.track;

/**
 * Objekt na trati
 * @author Petr Hamalcik
 */
public abstract class TrackObject {
    /** souřadnice středu */
    private float posX, posY;
    /** typ objektu */ 
    private String type;
    
    

    public TrackObject(float posX, float posY, String type) {
        this.posX = posX;
        this.posY = posY;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public float getPosX() {
        return posX;
    }
    public float getPosY() {
        return posY;
    }

    public void setPosX(float posX) {
        this.posX = posX;
    }

    public void setPosY(float posY) {
        this.posY = posY;
    }

    public void move(float x, float y) {
        this.posX = x;
        this.posY = y;
    }

}
