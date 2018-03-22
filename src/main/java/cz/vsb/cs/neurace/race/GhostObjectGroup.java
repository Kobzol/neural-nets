package cz.vsb.cs.neurace.race;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.bulletphysics.linearmath.Transform;

public class GhostObjectGroup implements TransformGroup, Iterable<TransformGroup>{

	Transform transform;
	Transform tmp = new Transform();
	List<TransformGroup> ghostObjects = new ArrayList<TransformGroup>();
	
	public GhostObjectGroup() {
		this.transform = new Transform();
		transform.setIdentity();
	}

	public GhostObjectGroup(Transform transform) {
		super();
		this.transform = transform;
	}

	public GhostObjectGroup(Transform transform, GhostObjectTransform ghostObject) {
		this(transform);
		this.ghostObjects.add(ghostObject);
	}

	public GhostObjectGroup(Transform transform, Collection<GhostObjectTransform> ghostObjects) {
		this(transform);
		this.ghostObjects.addAll(ghostObjects);
	}
	
	@Override
	public void applyTransform(Transform t, Transform tmp) {
		for(TransformGroup g : ghostObjects){
			tmp.set(t);
			tmp.mul(transform);
			g.applyTransform(tmp, this.tmp);
		}
	}

	@Override
	public void setTransform(Transform t) {
		transform = t;
	}
	
	@Override
	public Transform getTransform() {
		return transform;
	}

	public TransformGroup get(int index) {
		return ghostObjects.get(index);
	}

	public Iterator<TransformGroup> iterator() {
		return ghostObjects.iterator();
	}
	
	
	public int size() {
		return ghostObjects.size();
	}

	public void add(TransformGroup go){
		ghostObjects.add(go);
	}

	@Override
	public Transform getOldTransform() {
		return null;
	}
}
