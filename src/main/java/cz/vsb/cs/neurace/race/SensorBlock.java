package cz.vsb.cs.neurace.race;

import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.shapes.CollisionShape;

public class SensorBlock {

	
	private Sensor parentSensor;
	private float value;
	private CollisionObject collisionObject;
	private boolean inCollision;
	private GhostObjectTransform ghostObjectTransform;
	
	public Sensor getParentSensor() {
		return parentSensor;
	}
	public void setParentSensor(Sensor parentSensor) {
		this.parentSensor = parentSensor;
	}
	public float getValue() {
		return value;
	}
	public void setValue(float value) {
		this.value = value;
	}
	public CollisionObject getCollisionObject() {
		return collisionObject;
	}
	public void setCollisionObject(CollisionObject collisionObject) {
		this.collisionObject = collisionObject;
	}
	public boolean isInCollision() {
		return inCollision;
	}
	public void setInCollision(boolean inCollision) {
		this.inCollision = inCollision;
	}
	public SensorBlock(Sensor parentSensor, float value,
			CollisionShape collisionShape) {
		super();
		this.parentSensor = parentSensor;
		this.value = value;
		this.collisionObject = new PairCachingGhostObject();
		collisionObject.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
		collisionObject.setCollisionShape(collisionShape);
		collisionObject.setUserPointer(this);

	}
	public GhostObjectTransform getGhostObjectTransform() {
		return ghostObjectTransform;
	}
	public void setGhostObjectTransform(GhostObjectTransform ghostObjectTransform) {
		this.ghostObjectTransform = ghostObjectTransform;
	}
	@Override
	public String toString() {
		return "SensorBlock [" + parentSensor.getName() + ", value=" + value
				+ "]";
	}
	
	

	
}
