package cz.vsb.cs.neurace.track;


/**
 * Povrch trati
 * @author Petr Hamalčík
 */
public enum Surface {
    ASPHALT(1.0f), CONCRETE(1.0f), GRAVEL(0.6f), DIRT(0.65f), GRASS(0.5f), SAND(0.6f), SNOW(0.4f), ICE(0.2f);

    /** tření mezi povrchem a pneumatikou */
    private float friction;

    Surface(float friction) {
        this.friction = friction;
    }

    public float getFriction() {
        return friction;
    }
}
