package cz.vsb.cs.neurace.race;

import com.bulletphysics.linearmath.Transform;

public interface TransformGroup {
	public void applyTransform(Transform t, Transform tmp);
	public void setTransform(Transform t);
	public Transform getTransform();
	public Transform getOldTransform();
}
