package cz.vsb.cs.neurace.race;

import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.linearmath.Transform;

public class GhostObjectTransform implements TransformGroup{
	
//	GhostObject ghostObject;
	CollisionObject ghostObject;
	Transform transform;
	Transform oldTransform = new Transform(); 
	
	public GhostObjectTransform(CollisionObject ghostObject, Transform transform) {
		super();
		this.ghostObject = ghostObject;
		this.transform = transform;
	}

	public GhostObjectTransform(Transform transform) {
		super();
		this.transform = transform;
	}
	
	public CollisionObject getGhostObject() {
		return ghostObject;
	}
	public void setGhostObject(CollisionObject ghostObject) {
		this.ghostObject = ghostObject;
	}
	public Transform getTransform() {
		return transform;
	}
	
	@Override
	public void setTransform(Transform transform) {
		this.transform = transform;
	}

	@Override
	public void applyTransform(Transform t, Transform tmp) {
		tmp.set(t);
		tmp.mul(transform);
		ghostObject.getWorldTransform(oldTransform);
		ghostObject.setWorldTransform(tmp);
		
	}

	@Override
	public Transform getOldTransform() {
		// TODO Auto-generated method stub
		return oldTransform;
	}
	
	
	

}
