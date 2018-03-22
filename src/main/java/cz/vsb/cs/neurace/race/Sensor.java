package cz.vsb.cs.neurace.race;

import java.util.ArrayList;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.CollisionAlgorithm;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalConvexResult;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.ConvexHullShape;
import com.bulletphysics.collision.shapes.ConvexShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.linearmath.Transform;
import com.bulletphysics.util.ObjectArrayList;
import com.sun.xml.internal.fastinfoset.algorithm.BuiltInEncodingAlgorithm.WordListener;

import cz.vsb.cs.neurace.race.SensorTool.BlockDescription;

public class Sensor extends GhostObjectGroup {

	private ArrayList<SensorBlock> blocks;
	private String sensorName;
	private String carName;
	private PhysicalCar car;
	private CollisionWorld world;

	private Sensor(String sensorName, String carName, PhysicalCar car) {
		super();
		this.car = car;
		this.sensorName = sensorName;
		this.carName = carName;
	}

	public PhysicalCar getCar() {
		return car;
	}

	public static Sensor createLinearStripSensor(float length, float width,
			float height, int parts, String sensorName, String carName,
			PhysicalCar car) {
		Sensor sensor = new Sensor(sensorName, carName, car);
		createStripSensor(width, height, sensor, SensorTool.getLinearSteps(parts, length));
		return sensor;
	}

	public static Sensor createLogarithmicStripSensor(float length,
			float width, float height, int parts, String sensorName,
			String carName, PhysicalCar car) {
		Sensor sensor = new Sensor(sensorName, carName, car);
		createStripSensor(width, height, sensor, SensorTool.getLogaritmicsSteps(parts, length));
		return sensor;
	}

	private static void createStripSensor(float width, float height,
			Sensor sensor, BlockDescription[] blockDescriptions) {
		sensor.blocks = new ArrayList<SensorBlock>();
		for(BlockDescription blockDescription : blockDescriptions){
			CollisionShape ghostShape = new BoxShape(new Vector3f(width / 2,
					height / 2, blockDescription.getLength()/2));
			Transform t = new Transform();
			t.setIdentity();
			t.origin.set(0, 0, blockDescription.start + blockDescription.getLength()/2);
			SensorBlock sensorBlock = new SensorBlock(sensor, blockDescription.value,
					ghostShape);
			sensor.addBlock(sensorBlock, t);
		}
	}

	private void addBlock(SensorBlock sensorBlock, Transform t) {
		blocks.add(sensorBlock);
		GhostObjectTransform gTransform = new GhostObjectTransform(t);
		sensorBlock.setGhostObjectTransform(gTransform);
		gTransform.setGhostObject(sensorBlock.getCollisionObject());
		add(gTransform);
	}

	public static Sensor createAngleSensor(float length, float startAngle,
			float endAngle, float height, int parts, String sensorName,
			String carName, PhysicalCar car) {
		Sensor sensor = new Sensor(sensorName, carName, car);
		BlockDescription[] blockDescriptions = SensorTool.getLogaritmicsSteps(parts, length);
		sensor.blocks = new ArrayList<SensorBlock>();
//		float blockStartOffset = 0;
//		float blockLength = 0;
		Transform startAngleTransfor = new Transform();
		startAngleTransfor.setRotation(new Quat4f(0, 1 * (float) Math
				.sin(startAngle / 2), 0, (float) Math.cos(startAngle / 2)));
		Transform endAngleTransfor = new Transform();
		endAngleTransfor.setRotation(new Quat4f(0, 1 * (float) Math
				.sin(endAngle / 2), 0, (float) Math.cos(endAngle / 2)));
		for(BlockDescription blockDescription : blockDescriptions){
			Vector3f startAngleFirstDown = new Vector3f(0, -height / 2,
					blockDescription.start);
			Vector3f startAngleSecondDown = new Vector3f(0, -height / 2,
					blockDescription.end);
			Vector3f endAngleFirstDown = new Vector3f(startAngleFirstDown);
			Vector3f endAngleSecondDown = new Vector3f(startAngleSecondDown);
			Vector3f startAngleFirstUp = new Vector3f(startAngleFirstDown);
			startAngleFirstUp.y += height;
			Vector3f startAngleSecondUp = new Vector3f(startAngleSecondDown);
			startAngleSecondUp.y += height;
			Vector3f endAngleFirstUp = new Vector3f(endAngleFirstDown);
			endAngleFirstUp.y += height;
			Vector3f endAngleSecondUp = new Vector3f(endAngleSecondDown);
			endAngleSecondUp.y += height;
			startAngleTransfor.transform(startAngleFirstDown);
			startAngleTransfor.transform(startAngleSecondDown);
			endAngleTransfor.transform(endAngleFirstDown);
			endAngleTransfor.transform(endAngleSecondDown);
			startAngleTransfor.transform(startAngleFirstUp);
			startAngleTransfor.transform(startAngleSecondUp);
			endAngleTransfor.transform(endAngleFirstUp);
			endAngleTransfor.transform(endAngleSecondUp);
			ObjectArrayList<Vector3f> corners = new ObjectArrayList<Vector3f>();
			corners.add(startAngleFirstDown);
			corners.add(startAngleSecondDown);
			corners.add(endAngleFirstDown);
			corners.add(endAngleSecondDown);
			corners.add(startAngleFirstUp);
			corners.add(startAngleSecondUp);
			corners.add(endAngleFirstUp);
			corners.add(endAngleSecondUp);
			CollisionShape ghostShape = new ConvexHullShape(corners);
			Transform t = new Transform();
			t.setIdentity();
			t.origin.set(0, 0, 0);

			SensorBlock sensorBlock = new SensorBlock(sensor, blockDescription.value,
					ghostShape);
			sensor.addBlock(sensorBlock, t);
		}
//		for (int i = 0; i < parts; i++) {
//			float partValue = 1 - (float) i / parts;
//			float step = (10f - 1) / parts;
//			float endScale = (float) Math.log10(10f - step * (i + 1));
//			float startScale = (float) Math.log10(10f - step * (i));
//
//			blockLength = (startScale - endScale) * length;
//			Vector3f startAngleFirstDown = new Vector3f(0, -height / 2,
//					blockStartOffset);
//			Vector3f startAngleSecondDown = new Vector3f(0, -height / 2,
//					blockStartOffset + blockLength);
//			Vector3f endAngleFirstDown = new Vector3f(startAngleFirstDown);
//			Vector3f endAngleSecondDown = new Vector3f(startAngleSecondDown);
//			Vector3f startAngleFirstUp = new Vector3f(startAngleFirstDown);
//			startAngleFirstUp.y += height;
//			Vector3f startAngleSecondUp = new Vector3f(startAngleSecondDown);
//			startAngleSecondUp.y += height;
//			Vector3f endAngleFirstUp = new Vector3f(endAngleFirstDown);
//			endAngleFirstUp.y += height;
//			Vector3f endAngleSecondUp = new Vector3f(endAngleSecondDown);
//			endAngleSecondUp.y += height;
//			startAngleTransfor.transform(startAngleFirstDown);
//			startAngleTransfor.transform(startAngleSecondDown);
//			endAngleTransfor.transform(endAngleFirstDown);
//			endAngleTransfor.transform(endAngleSecondDown);
//			startAngleTransfor.transform(startAngleFirstUp);
//			startAngleTransfor.transform(startAngleSecondUp);
//			endAngleTransfor.transform(endAngleFirstUp);
//			endAngleTransfor.transform(endAngleSecondUp);
//			ObjectArrayList<Vector3f> corners = new ObjectArrayList<Vector3f>();
//			corners.add(startAngleFirstDown);
//			corners.add(startAngleSecondDown);
//			corners.add(endAngleFirstDown);
//			corners.add(endAngleSecondDown);
//			corners.add(startAngleFirstUp);
//			corners.add(startAngleSecondUp);
//			corners.add(endAngleFirstUp);
//			corners.add(endAngleSecondUp);
//			CollisionShape ghostShape = new ConvexHullShape(corners);
//			Transform t = new Transform();
//			t.setIdentity();
//			t.origin.set(0, 0, 0);
//			blockStartOffset += blockLength;
//
//			SensorBlock sensorBlock = new SensorBlock(sensor, partValue,
//					ghostShape);
//			sensor.addBlock(sensorBlock, t);
//		}
		return sensor;
	}

	public void addToWorld(CollisionWorld world) {
		this.world = world;
		for (SensorBlock block : blocks) {
			world.addCollisionObject(
					block.getCollisionObject(),
					CollisionFilterGroups.SENSOR_TRIGGER,
					(short) (CollisionFilterGroups.ALL_FILTER
							^ ~CollisionFilterGroups.SENSOR_TRIGGER ^ ~(short) 64));
		}

	}

	private DispatcherInfo dispatchInfo = new DispatcherInfo();
	private ManifoldResult manifoldResult = new ManifoldResult();

	public float getValue() {
		float value = 0;
		int count = 0;
		for (SensorBlock block : blocks) {
			int colCount = 0;
			if (block.getCollisionObject() instanceof PairCachingGhostObject) {
				PairCachingGhostObject ghost = (PairCachingGhostObject) block
						.getCollisionObject();
				block.setInCollision(false);
				for (CollisionObject o : ghost.getOverlappingPairs()) {
					CollisionAlgorithm alg = this.world.getDispatcher()
							.findAlgorithm(block.getCollisionObject(), o);
					DispatcherInfo dispatchInfo = new DispatcherInfo();
					manifoldResult.init(block.getCollisionObject(), o);
					alg.processCollision(block.getCollisionObject(), o,
							dispatchInfo, manifoldResult);
					if (manifoldResult.getPersistentManifold().getNumContacts() > 0) {
						block.setInCollision(true);
						alg.destroy();
						return block.getValue();
					}
					alg.destroy();
				}
				count = colCount;
			}
			if (block.isInCollision() && value == 0) {
				value = block.getValue();
			}
			if (count > 0) {
				value = block.getValue();
				break;
			}
		}

		return value;
	}

	public String getName() {
		return sensorName;
	}

	public String getCarName() {
		return carName;
	}

}
