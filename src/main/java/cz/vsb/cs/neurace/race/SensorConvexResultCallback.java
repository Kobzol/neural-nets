package cz.vsb.cs.neurace.race;

import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalConvexResult;

public class SensorConvexResultCallback extends CollisionWorld.ConvexResultCallback {

	private SensorBlock sensorBlock;
	
	
	
	public SensorConvexResultCallback(SensorBlock sensorBlock) {
		super();
		this.sensorBlock = sensorBlock;
	}

	@Override
	public float addSingleResult(LocalConvexResult convexResult, boolean normalInWorldSpace) {
//		System.out.println("Hit fraction: " + convexResult.hitFraction + " for " + convexResult.hitCollisionObject.getUserPointer() + " --> " + sensorBlock.getParentSensor().getName() + ": " + sensorBlock.getValue());
		sensorBlock.setInCollision(true);
		if(!(convexResult.hitFraction > 0)){
			sensorBlock.setInCollision(true);
		}
		return convexResult.hitFraction;
	}
}
