package cz.vsb.cs.neurace.race;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.broadphase.CollisionFilterGroups;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.broadphase.DispatchFunc;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.broadphase.DispatcherInfo;
import com.bulletphysics.collision.broadphase.OverlapFilterCallback;
import com.bulletphysics.collision.dispatch.CollisionConfiguration;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.dispatch.DefaultNearCallback;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.ManifoldResult;
import com.bulletphysics.collision.dispatch.NearCallback;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.*;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.ConstraintSolver;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;

import cz.vsb.cs.neurace.track.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import javax.vecmath.Point2f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * 
 * @author Petr Hamalčík
 */
public class RacePhysics {

	public DynamicsWorld dynamicsWorld;
	// private ObjectArrayList<CollisionShape> collisionShapes = new
	// ObjectArrayList<CollisionShape>();
	private BroadphaseInterface overlappingPairCache;
	private Dispatcher dispatcher;
	private ConstraintSolver constraintSolver;
	private CollisionConfiguration collisionConfiguration;
	// private DefaultMotionState defMotionState;

	private TriangleIndexVertexArray indexVertexArrays;
	private ByteBuffer vertices;

	/** seznam aut */
	public ArrayList<PhysicalCar> cars = new ArrayList<PhysicalCar>();
	/** seznam objektů */
	public ArrayList<RigidBody> objects = new ArrayList<RigidBody>();
	/** země */
	private RigidBody groundBody;
	/** 1/2 velikosti terénu v metrech */
	private float worldSize = 500;
	/** délka kroku simulace v sekundách */
	private float timeStep;
	/** maximální počet interpolovaných mezikroků */
	private int maxSubSteps;

	/** kolize mezi auty */
	public boolean carCollisions = true;
	/** kolizní skupina aut při vypnutých kolizích */
	public static short carColGroup = (short) 0x20;
	/** kolizní maska aut při vypnutých kolizích */
	public static short carColMask = (short) 0x4f;

	private RigidBody bounds[] = new RigidBody[4];

	public RacePhysics(float timeStep, int maxSubSteps) {
		this.timeStep = timeStep;
		this.maxSubSteps = maxSubSteps;
		initPhysics();
	}

	/**
	 * Vytvoření fyzikálního světa
	 */
	public final void initPhysics() {

		overlappingPairCache = new DbvtBroadphase();
		collisionConfiguration = new DefaultCollisionConfiguration();
		CollisionDispatcher collisionDispatcher = new CollisionDispatcher(collisionConfiguration);
//		collisionDispatcher.setNearCallback(new DefaultNearCallback(){
//
//			@Override
//			public void handleCollision(BroadphasePair collisionPair,
//					CollisionDispatcher dispatcher, DispatcherInfo dispatchInfo) {
//				super.handleCollision(collisionPair, dispatcher, dispatchInfo);
//				for(PersistentManifold persistentManifold : dispatcher.getInternalManifoldPointer()){
//					for(int i=0; i< persistentManifold.getNumContacts(); i++  ){
//						if(persistentManifold.getContactPoint(i).distance1 < 0){
//							if(collisionPair.pProxy0.clientObject instanceof CollisionObject && 
//									collisionPair.pProxy1.clientObject instanceof CollisionObject){
//								CollisionObject c0 = (CollisionObject)collisionPair.pProxy0.clientObject;
//								CollisionObject c1 = (CollisionObject)collisionPair.pProxy1.clientObject;
//								SensorBlock sensorBlock = null;
//								PhysicalCar physicalCar = null;
//								if(c0.getUserPointer() instanceof SensorBlock){
//									sensorBlock = (SensorBlock)c0.getUserPointer();
//								}
//								if(c1.getUserPointer() instanceof SensorBlock){//obe podminky zaroven by nemneli nastat, sensory spolu nekoliduji
//									sensorBlock = (SensorBlock)c1.getUserPointer();
//								}
//								if(c0.getUserPointer() instanceof PhysicalCar){
//									physicalCar = (PhysicalCar)c0.getUserPointer();
//								}
//								if(c1.getUserPointer() instanceof PhysicalCar){//je to jedno kolize dvou aut me nezajima
//									physicalCar = (PhysicalCar)c1.getUserPointer();
//								}
//								
//								if(sensorBlock != null){
//									if(physicalCar == null){ //kolize senzoru s necim jinym
//										sensorBlock.setInCollision(true);
//									}
//									else if(!sensorBlock.getParentSensor().getCar().equals(physicalCar)){ //koliduju s autem, ale neni to moje auto
//										sensorBlock.setInCollision(true);
//									}
//								}
//							}
//							//System.out.println(((CollisionObject)collisionPair.pProxy0.clientObject).getUserPointer() + " x " + ((CollisionObject)collisionPair.pProxy1.clientObject).getUserPointer());
//						}
//					}
//				}
//				if(dispatcher.getNumManifolds() > 0){
//				}
//			}
//			
//		});
		dispatcher = collisionDispatcher;
//		collisionDispatcher.setNearCallback(new NearCallback() {
//			
//			private final ManifoldResult contactPointResult = new ManifoldResult();
//			
//			@Override
//			public void handleCollision(BroadphasePair collisionPair,
//					CollisionDispatcher dispatcher, DispatcherInfo dispatchInfo) {
//				CollisionObject colObj0 = (CollisionObject) collisionPair.pProxy0.clientObject;
//				CollisionObject colObj1 = (CollisionObject) collisionPair.pProxy1.clientObject;
//				if (dispatcher.needsCollision(colObj0, colObj1)) {
//					// dispatcher will keep algorithms persistent in the collision pair
//					if (collisionPair.algorithm == null) {
//						collisionPair.algorithm = dispatcher.findAlgorithm(colObj0, colObj1);
//					}
//
//					if (collisionPair.algorithm != null) {
//						//ManifoldResult contactPointResult = new ManifoldResult(colObj0, colObj1);
//						contactPointResult.init(colObj0, colObj1);
//
//						if (dispatchInfo.dispatchFunc == DispatchFunc.DISPATCH_DISCRETE) {
//							// discrete collision detection query
//							collisionPair.algorithm.processCollision(colObj0, colObj1, dispatchInfo, contactPointResult);
//						}
//						else {
//							// continuous collision detection query, time of impact (toi)
//							float toi = collisionPair.algorithm.calculateTimeOfImpact(colObj0, colObj1, dispatchInfo, contactPointResult);
//							if (dispatchInfo.timeOfImpact > toi) {
//								dispatchInfo.timeOfImpact = toi;
//							}
//						}
//					}
//				}
//				
//			}
//		});
		constraintSolver = new SequentialImpulseConstraintSolver();
		dynamicsWorld = new DiscreteDynamicsWorld(dispatcher,
				overlappingPairCache, constraintSolver, collisionConfiguration);
		// dynamicsWorld.setGravity(new Vector3f(0, -10, 0));
        dynamicsWorld.getBroadphase().getOverlappingPairCache().setInternalGhostPairCallback(new GhostPairCallback());
		dynamicsWorld.getPairCache().setOverlapFilterCallback(new OverlapFilterCallback() {
			@Override
			public boolean needBroadphaseCollision(BroadphaseProxy proxy0,
					BroadphaseProxy proxy1) {
				boolean collision = (proxy0.collisionFilterGroup & proxy1.collisionFilterMask) != 0;
				collision = collision && (proxy1.collisionFilterGroup & proxy0.collisionFilterMask) != 0;
				if(!collision){
					return false;
				}
				CollisionObject c0 = (CollisionObject)proxy0.clientObject;
				CollisionObject c1 = (CollisionObject)proxy1.clientObject;
				SensorBlock sensorBlock = null;
				PhysicalCar physicalCar = null;
				if(c0.getUserPointer() instanceof SensorBlock){
					sensorBlock = (SensorBlock)c0.getUserPointer();
				}
				if(c1.getUserPointer() instanceof SensorBlock){//obe podminky zaroven by nemneli nastat, sensory spolu nekoliduji
					sensorBlock = (SensorBlock)c1.getUserPointer();
				}
				if(c0.getUserPointer() instanceof PhysicalCar){
					physicalCar = (PhysicalCar)c0.getUserPointer();
				}
				if(c1.getUserPointer() instanceof PhysicalCar){//je to jedno kolize dvou aut me nezajima
					physicalCar = (PhysicalCar)c1.getUserPointer();
				}
				
				if(sensorBlock != null){
					if(sensorBlock.getParentSensor().getCar().equals(physicalCar)){ //koliduju s autem, a je to moje auto
						return false;
					}
				}
				return true;
			}
		});
		// vytvoření povrchu země
		Transform tr = new Transform();
		tr.setIdentity();
		tr.origin.set(worldSize, 0.0f, worldSize);
		TriangleMeshShape groundShape = createGroundShape();
		groundBody = createRigidBody(
				0,
				tr,
				groundShape,
				(short) 64,
				(short) (CollisionFilterGroups.ALL_FILTER ^ CollisionFilterGroups.STATIC_FILTER),
				dynamicsWorld);
		groundBody.setFriction(1.0f);
		groundBody.setUserPointer("Ground");
	}

	/**
	 * Vytvoří ohraničení na krajích ve tvaru obdélníku
	 * 
	 * @param a
	 *            první rozměr
	 * @param b
	 *            druhý rozměr
	 */
	public void setBounds(float a, float b) {
		Transform tr = new Transform();
		tr.setIdentity();
		if (bounds[0] != null) {
			for (RigidBody body : bounds) {
				dynamicsWorld.removeRigidBody(body);
			}
		}
		float h = 1.0f;
		BoxShape boxShape = new BoxShape(new Vector3f(a / 2, h, 0.5f));
		tr.origin.set(a / 2, h, 0);
		bounds[0] = createRigidBody(0, tr, boxShape, dynamicsWorld);
		tr.origin.set(a / 2, h, b);
		bounds[1] = createRigidBody(0, tr, boxShape, dynamicsWorld);
		boxShape = new BoxShape(new Vector3f(0.5f, h, b / 2));
		tr.origin.set(0, h, b / 2);
		bounds[2] = createRigidBody(0, tr, boxShape, dynamicsWorld);
		tr.origin.set(a, h, b / 2);
		bounds[3] = createRigidBody(0, tr, boxShape, dynamicsWorld);
	}

	/**
	 * Vytvoří tvar terénu
	 * 
	 * @return trojúhelníková síť
	 */
	public TriangleMeshShape createGroundShape() {
		final float TRIANGLE_SIZE = 20.0f;

		// create a triangle-mesh ground
		int vertStride = 4 * 3 /* sizeof(btVector3) */;
		int indexStride = 3 * 4 /* 3*sizeof(int) */;

		final int NUM_VERTS_X = (int) (2 * worldSize / TRIANGLE_SIZE + 2);
		final int NUM_VERTS_Y = (int) (2 * worldSize / TRIANGLE_SIZE + 2);
		final int totalVerts = NUM_VERTS_X * NUM_VERTS_Y;

		final int totalTriangles = 2 * (NUM_VERTS_X - 1) * (NUM_VERTS_Y - 1);

		vertices = ByteBuffer.allocateDirect(totalVerts * vertStride).order(
				ByteOrder.nativeOrder());
		ByteBuffer gIndices = ByteBuffer.allocateDirect(totalTriangles * 3 * 4)
				.order(ByteOrder.nativeOrder());

		Vector3f tmp = new Vector3f();
		for (int i = 0; i < NUM_VERTS_X; i++) {
			for (int j = 0; j < NUM_VERTS_Y; j++) {
				float wl = 0.2f;
				// height set to zero, but can also use curved landscape, just
				// uncomment out the code
				float height = 0f; // 20f * (float)Math.sin(i * wl) *
									// (float)Math.cos(j * wl);
				tmp.set((i - NUM_VERTS_X * 0.5f) * TRIANGLE_SIZE, height,
						(j - NUM_VERTS_Y * 0.5f) * TRIANGLE_SIZE);

				int index = i + j * NUM_VERTS_X;
				vertices.putFloat((index * 3 + 0) * 4, tmp.x);
				vertices.putFloat((index * 3 + 1) * 4, tmp.y);
				vertices.putFloat((index * 3 + 2) * 4, tmp.z);
				// #endif
			}
		}

		// int index=0;
		gIndices.clear();
		for (int i = 0; i < NUM_VERTS_X - 1; i++) {
			for (int j = 0; j < NUM_VERTS_Y - 1; j++) {
				gIndices.putInt(j * NUM_VERTS_X + i);
				gIndices.putInt(j * NUM_VERTS_X + i + 1);
				gIndices.putInt((j + 1) * NUM_VERTS_X + i + 1);

				gIndices.putInt(j * NUM_VERTS_X + i);
				gIndices.putInt((j + 1) * NUM_VERTS_X + i + 1);
				gIndices.putInt((j + 1) * NUM_VERTS_X + i);
			}
		}
		gIndices.flip();

		indexVertexArrays = new TriangleIndexVertexArray(totalTriangles,
				gIndices, indexStride, totalVerts, vertices, vertStride);

		boolean useQuantizedAabbCompression = true;
		return new BvhTriangleMeshShape(indexVertexArrays,
				useQuantizedAabbCompression);
	}

	/**
	 * Vytvoří kvádr
	 * 
	 * @param block
	 */
	public void createBlock(BlockObject block) {
		Transform tr = new Transform();
		tr.setIdentity();
		tr.origin.set(block.getPosX(), block.getHeight() / 2, block.getPosY());
		CollisionShape objectShape = new BoxShape(new Vector3f(
				block.getLength() / 2, block.getHeight() / 2,
				block.getWidth() / 2));
		Quat4f quat = new Quat4f();
		QuaternionUtil.setRotation(quat, new Vector3f(0, 1, 0),
				-block.getAngle());
		tr.setRotation(quat);
		RigidBody body = createRigidBody(0, tr, objectShape, dynamicsWorld);
		body.setUserPointer(block.getType());
		block.setBody(body);
		objects.add(body);
	}

	/**
	 * Vytvoří kužel
	 * 
	 * @param cyl
	 */
	public void createCylinder(CylinderObject cyl) {
		Transform tr = new Transform();
		tr.setIdentity();
		tr.origin.set(cyl.getPosX(), cyl.getHeight() / 2, cyl.getPosY());
		CollisionShape objectShape = new CylinderShape(new Vector3f(
				cyl.getRadius(), cyl.getHeight() / 2, cyl.getRadius()));
		RigidBody body = createRigidBody(0, tr, objectShape, dynamicsWorld);
		body.setUserPointer(cyl.getType());
		cyl.setBody(body);
		objects.add(body);
	}

	/**
	 * Vytvoří zeď
	 * 
	 * @param wall
	 */
	public void createWall(WallObject wall) {
		Transform tr = new Transform();
		tr.setIdentity();
		tr.origin.set(wall.getPosX(), 0, wall.getPosY());
		Point2f last = new Point2f(wall.getPosX(), wall.getPosY());
		Vector3f axis = new Vector3f(0, 1, 0);
		for (Point2f point : wall.getPoints()) {
			float x = (last.x + point.x) / 2;
			float z = (last.y + point.y) / 2;
			float angle = (float) Math.atan((last.x - x) / (last.y - z));
			float length = last.distance(point);
			tr.origin.set(x, wall.getHeight() / 2, z);
			Quat4f quat = new Quat4f();
			QuaternionUtil.setRotation(quat, axis, angle + (float) Math.PI / 2);
			tr.setRotation(quat);

			CollisionShape objectShape = new BoxShape(new Vector3f(length / 2,
					wall.getHeight() / 2, wall.getWidth() / 2));
			RigidBody body = createRigidBody(0, tr, objectShape, dynamicsWorld);
			body.setUserPointer("wall");
			wall.addBody(body);
			objects.add(body);
			last = point;
		}
	}

	/**
	 * Vytvoří model objektu
	 * 
	 * @param object
	 */
	public void createObject(TrackObject object) {
		if (object.getClass() == BlockObject.class) {
			BlockObject block = (BlockObject) object;
			createBlock(block);
		} else if (object.getClass() == CylinderObject.class) {
			CylinderObject cyl = (CylinderObject) object;
			createCylinder(cyl);
		} else if (object.getClass() == WallObject.class) {
			WallObject wall = (WallObject) object;
			createWall(wall);
		}
	}

	/**
	 * Vytvoření všech objektů na trati
	 * 
	 * @param track
	 *            trať
	 */
	public void createObjects(Track track) {
		for (TrackObject object : track.getObjects()) {
			createObject(object);
		}
	}

	/**
	 * Odstraní objekty z trati
	 */
	public void destroyObjects() {
		for (RigidBody body : objects) {
			this.dynamicsWorld.removeRigidBody(body);
		}
		objects.clear();
	}

	/**
	 * Odstraní objekt z trati
	 */
	public void destroyObject(TrackObject object) {
		if (object.getClass() == BlockObject.class) {
			BlockObject block = (BlockObject) object;
			RigidBody body = block.getBody();
			this.dynamicsWorld.removeRigidBody(body);
			objects.remove(body);
		} else if (object.getClass() == CylinderObject.class) {
			CylinderObject cyl = (CylinderObject) object;
			RigidBody body = cyl.getBody();
			this.dynamicsWorld.removeRigidBody(body);
			objects.remove(body);
		} else if (object.getClass() == WallObject.class) {
			WallObject wall = (WallObject) object;
			for (RigidBody body : wall.getBodies()) {
				this.dynamicsWorld.removeRigidBody(body);
				objects.remove(body);
			}
		}

	}

	/**
	 * Vytvoří vozidlo
	 * 
	 * @param x
	 *            x souřadnice
	 * @param y
	 *            y souřadnice
	 * @param z
	 *            z souřadnice
	 * @param angle
	 *            úhel v radiánech
	 * @param car
	 *            objekt s parametry auta
	 * @return nové auto
	 */
	public PhysicalCar createCar(float x, float y, float z, float angle,
			PhysicalCar car, String carName) {
		Transform tr = new Transform();
		tr.setIdentity();
		tr.origin.set(x, y, z);

		Quat4f quat = new Quat4f();
		QuaternionUtil.setRotation(quat, new Vector3f(0, 1, 0), angle);
		tr.setRotation(quat);

		car.createCar(tr, dynamicsWorld, groundBody, carCollisions, carName);
		// collisionShapes.add(newCar.getRaycastVehicle().getRigidBody().getCollisionShape());
		cars.add(car);
		return car;
	}

	/**
	 * Odstraní auto
	 * 
	 * @param car
	 *            auto
	 */
	public void destroyCar(PhysicalCar car) {
		RigidBody body = car.getRaycastVehicle().getRigidBody();
		body.destroy();
		this.dynamicsWorld.removeRigidBody(body);
		this.dynamicsWorld.removeVehicle(car.getRaycastVehicle());
		cars.remove(car);
	}

	/**
	 * Provede jeden krok simulace
	 */
	public void moveCars() {
		synchronized (dynamicsWorld) {
			dynamicsWorld.stepSimulation(timeStep, maxSubSteps);
		}
	}

	/**
	 * Vytvoří pevné těleso
	 * 
	 * @param mass
	 *            hmotnost
	 * @param startTransform
	 *            trasformace v prostoru
	 * @param group
	 *            kolizní skupina
	 * @param mask
	 *            kolizní maska
	 * @param shape
	 *            tvar tělesa
	 * @param dynamicsWorld
	 *            svět, ve kterém bude vytvořeno
	 * @return
	 */
	public static RigidBody createRigidBody(float mass,
			Transform startTransform, CollisionShape shape, short group,
			short mask, DynamicsWorld dynamicsWorld) {
		boolean isDynamic = (mass != 0f);
		Vector3f localInertia = new Vector3f(0f, 0f, 0f);
		if (isDynamic) {
			shape.calculateLocalInertia(mass, localInertia);
		}

		DefaultMotionState myMotionState = new DefaultMotionState(
				startTransform);
		RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(mass,
				myMotionState, shape, localInertia);
		RigidBody body = new RigidBody(cInfo);
		dynamicsWorld.addCollisionObject(body, group, mask);
		body.setGravity(dynamicsWorld.getGravity(new Vector3f()));

		return body;
	}

	/**
	 * Vytvoří pevné těleso
	 * 
	 * @param mass
	 *            hmotnost
	 * @param startTransform
	 *            trasformace v prostoru
	 * @param shape
	 *            tvar tělesa
	 * @param dynamicsWorld
	 *            svět, ve kterém bude vytvořeno
	 * @return
	 */
	public static RigidBody createRigidBody(float mass,
			Transform startTransform, CollisionShape shape,
			DynamicsWorld dynamicsWorld) {
		// rigidbody is dynamic if and only if mass is non zero, otherwise
		// static
		boolean isDynamic = (mass != 0f);
		Vector3f localInertia = new Vector3f(0f, 0f, 0f);
		if (isDynamic) {
			shape.calculateLocalInertia(mass, localInertia);
		}

		DefaultMotionState myMotionState = new DefaultMotionState(
				startTransform);
		RigidBodyConstructionInfo cInfo = new RigidBodyConstructionInfo(mass,
				myMotionState, shape, localInertia);
		RigidBody body = new RigidBody(cInfo);
		dynamicsWorld.addRigidBody(body);

		return body;
	}

	/**
	 * Nastaví kolize mezi auty.
	 * 
	 * @param carsCollide
	 */
	public void setCarCollisions(boolean collisions) {
		this.carCollisions = collisions;
		for (PhysicalCar car : cars) {
			car.setCollisions(collisions);
		}
	}

}
