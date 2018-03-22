package cz.vsb.cs.neurace.race;

import com.bulletphysics.collision.broadphase.BroadphasePair;
import com.bulletphysics.collision.broadphase.BroadphaseProxy;
import com.bulletphysics.collision.broadphase.Dbvt.Node;
import com.bulletphysics.collision.broadphase.Dispatcher;
import com.bulletphysics.collision.broadphase.OverlappingPairCallback;
import com.bulletphysics.collision.dispatch.CollisionFlags;
import com.bulletphysics.collision.dispatch.CollisionObject;
import com.bulletphysics.collision.dispatch.CollisionWorld;
import com.bulletphysics.collision.dispatch.CollisionWorld.LocalConvexResult;
import com.bulletphysics.collision.dispatch.GhostObject;
import com.bulletphysics.collision.dispatch.GhostPairCallback;
import com.bulletphysics.collision.dispatch.PairCachingGhostObject;
import com.bulletphysics.collision.narrowphase.PersistentManifold;
import com.bulletphysics.collision.shapes.BoxShape;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.CompoundShape;
import com.bulletphysics.dynamics.DynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.vehicle.*;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.MotionState;
import com.bulletphysics.linearmath.QuaternionUtil;
import com.bulletphysics.linearmath.Transform;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 *
 * @author Petr Hamalčík
 */
public class PhysicalCar {

        /** maximální točivý moment [Nm]*/
        private float maxTorque = 160;
        /** maximální brzdná síla */
	private float maxBrakingForce = 300; //100.f
        /** maximální rychlost */
        private float maxSpeed = 180;

        /** poloměr pneumatiky */
	private float wheelRadius = 0.3f; //0.5f
        /** šířka pneumatiky */
	private float wheelWidth = 0.2f; //0.4f
        /** součinitel tření mezi pneumatikou a vozovkou */
    	private float wheelFriction = 1.0f;//1e30f;
        /** bonus k součiniteli tření na asfaltu/betonu */
        private float asphaltBonus = 0.0f;
        /** bonus k součiniteli tření na nezpevněných cestách a mimo trať*/
        private float offroadBonus = 0.0f;

        /** tuhost tlumičů */
	private float suspensionStiffness = 50.0f; //20.f
        /** roztahování tlumičů */
	private float suspensionDamping = 0.3f * 2f * (float)Math.sqrt(suspensionStiffness); //2.3f
        /** stlačování tlumičů */
	private float suspensionCompression = 0.2f * 2f * (float)Math.sqrt(suspensionStiffness); //4.4f
        /** délka tlumičů v klidu */
        private float suspensionRestLength = 0.2f; //0.6f
	/** výška připojení tlumičů */
        private float connectionHeight = 0.6f; //1.2f
        /** maximální síla která může působit na tlumiče */
        private float maxSuspensionForce = 10000;
        /** maximalní stlačení tlumičů [cm]*/
        private float maxSuspensionTravelCm = 15;
        
        /** váha auta [kg] */
        private float mass = 1116.0f;//0.1f;
        /** součinitel odporu vzduchu */
	private float airCoef = 0.33f;
        /** hustota vzduchu [kg/m3]*/
	private float airDensity = 1.30f;
        /** šířka auta [m] */
	private float carWidth = 1.64f;
        /** výška auta [m] */
	private float carHeight = 1.20f;
        /** délka auta [m] */
        private float carLength = 4.00f;
        /** rozvor [m] */
        private float wheelbase = 2.46f; //1.2f
        /** rozchod [m] */
        private float wheelTrack = 1.53f;
        /** pravděpodobnost převrácení auta
         * 0.0 = nelze převrátit, 1.0 = fyzikální chování */
        private float rollInfluence = 1.0f;//0.1f;

        /** nejvyšší stupeň převodovky */
        private int topGear = 5;
        /** převodové poměry */
        private float gearRatios[] = new float[]{3.455f, 1.955f, 1.281f, 0.927f, 0.74f};
        /** převodový poměr diferenciálu */
        private float diffRatio = 3.63f;
        /** zařazený stupeň */
        private int gear = 1;
        /** otáčky motoru */
        private float rpm = 0;
        /** otáčky motoru při zařazení nižšího stupně */
        private int shiftDownRPM = 2500;
        /** otáčky motoru při zařazení vyššího stupně */
        private int shiftUpRPM = 5000;
        /** účinnost převodovky */
        private float efficiency = 0.8f;

        private VehicleTuning tuning;
        private VehicleRaycaster vehicleRayCaster;
        private RaycastVehicle vehicle;
        private DynamicsWorld dynamicsWorld;
        private RigidBody ground;
        private String carName;
        private boolean carCollisions;

        ArrayList<PairCachingGhostObject> ghostObject = new ArrayList<PairCachingGhostObject>();
//      private HashMap<Float, BoxShape> sensorShapes = new HashMap<Float, BoxShape>();
      private GhostObjectGroup allSensors;
        
        public enum Drive {
		FRONT, REAR, ALL
	}
        
        private Drive drive = Drive.FRONT;

        
        /**
         * Konstruktor
         */
        public PhysicalCar() {
        }

        /**
         * Konstruktor 
         * @param file soubor s paramety auta
         */
        public PhysicalCar(HashMap<String, String> carSpec){
            readDataFromSpecification(carSpec);
        }

        /**
         * Vytvoří fyzikální model auta ve fyzikáním světě 
         * @param transform transformace v prostoru
         * @param dynamicsWorld fyzikální svět
         * @param ground těleso reprezentující terén
         * @param carsCollide zda vozidlo koliduje s jinými vozidly
         */
        public void createCar(Transform transform, DynamicsWorld dynamicsWorld, RigidBody ground, boolean carsCollide, String carName) {
        	this.carName = carName;
            this.dynamicsWorld = dynamicsWorld;
            this.ground = ground;
            this.carCollisions = carsCollide;
//            CollisionShape ghostShape = new BoxShape(new Vector3f(carWidth/4, carHeight/3f, carLength*2));
            CollisionShape chassisShape = new BoxShape(new Vector3f(carWidth/2, carHeight/2, carLength/2));
            Vector3f localInertia = new Vector3f(0f, 0f, 0f);
            chassisShape.calculateLocalInertia(mass, localInertia);

            Transform allSensorTransform = new Transform();
            allSensorTransform.setIdentity();
            allSensorTransform.origin.set(0,1,carLength/2);
            //allSensorTransform.setRotation(new Quat4f(0, 0, 3.14f/4, 1));
            allSensors = new GhostObjectGroup(allSensorTransform);
            
//            Transform s1t = new Transform();
//            s1t.setIdentity();
//            s1t.origin.set(carWidth/4*2,0,0);
//            GhostObjectTransform s1 = new GhostObjectTransform(s1t);
//            PairCachingGhostObject s1g = new PairCachingGhostObject();
//            s1g.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
//            ghostShape.calculateLocalInertia(0, localInertia);
//            s1g.setCollisionShape(ghostShape);
//            Transform s1tt = new Transform(transform);
//            s1tt.origin.y = s1tt.origin.y + 1;
//            s1tt.origin.z +=-carWidth;
//            s1tt.origin.x +=-4*carWidth;
//            s1g.setWorldTransform(s1tt);
//          s1.setGhostObject(s1g);
//            allSensors.add(s1);
//            dynamicsWorld.addCollisionObject(s1g, (short)0x10, (short)0x0f);

//            Transform s4t = new Transform();
//            s4t.setIdentity();
//            s4t.origin.set(-carWidth/4*2,0,0);
//            GhostObjectTransform s4 = new GhostObjectTransform(s4t);
//            PairCachingGhostObject s4g = new PairCachingGhostObject();
//            s4g.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
//            ghostShape.calculateLocalInertia(0, localInertia);
//            s4.setGhostObject(s4g);
//            allSensors.add(s4);
//            dynamicsWorld.addCollisionObject(s4g, (short)0x10, (short)0x0f);
            
            allSensors.add(Sensor.createLogarithmicStripSensor(SensorTool.longSensorLength, carWidth/4, carHeight, SensorTool.numberOfPartsForLongSensor, "sensorFrontLeft", carName, this));
            allSensors.add(Sensor.createLogarithmicStripSensor(SensorTool.longSensorLength, carWidth/4, carHeight, SensorTool.numberOfPartsForLongSensor, "sensorFrontMiddleLeft", carName, this));
            allSensors.add(Sensor.createLogarithmicStripSensor(SensorTool.longSensorLength, carWidth/4, carHeight, SensorTool.numberOfPartsForLongSensor, "sensorFrontMiddleRight", carName, this));
            allSensors.add(Sensor.createLogarithmicStripSensor(SensorTool.longSensorLength, carWidth/4, carHeight, SensorTool.numberOfPartsForLongSensor, "sensorFrontRight", carName, this));
            allSensors.add(Sensor.createAngleSensor(SensorTool.longSensorLength, (float)(-Math.PI/180*0), (float)(-Math.PI/180*45), carHeight, SensorTool.numberOfPartsForLongSensor, "sensorFrontRightCorner1", carName, this));
            allSensors.add(Sensor.createAngleSensor(SensorTool.longSensorLength, (float)(-Math.PI/180*45), (float)(-Math.PI/180*90), carHeight, SensorTool.numberOfPartsForLongSensor, "sensorFrontRightCorner2", carName, this));
            allSensors.add(Sensor.createLinearStripSensor(SensorTool.shortSensorLength, carLength/2, carHeight, SensorTool.numberOfPartsForShortSensor, "sensorRight1", carName, this));
            allSensors.add(Sensor.createLinearStripSensor(SensorTool.shortSensorLength, carLength/2, carHeight, SensorTool.numberOfPartsForShortSensor, "sensorRight2", carName, this));
            allSensors.add(Sensor.createAngleSensor(SensorTool.shortSensorLength, (float)(-Math.PI/180*90), (float)(-Math.PI/180*135), carHeight, SensorTool.numberOfPartsForShortSensor, "sensorRearRightCorner2", carName, this));
            allSensors.add(Sensor.createAngleSensor(SensorTool.shortSensorLength, (float)(-Math.PI/180*135), (float)(-Math.PI/180*180), carHeight, SensorTool.numberOfPartsForShortSensor, "sensorRearRightCorner1", carName, this));
            allSensors.add(Sensor.createLogarithmicStripSensor(SensorTool.shortSensorLength, carWidth/2, carHeight, SensorTool.numberOfPartsForShortSensor, "sensorRearRight", carName, this));
            allSensors.add(Sensor.createLogarithmicStripSensor(SensorTool.shortSensorLength, carWidth/2, carHeight, SensorTool.numberOfPartsForShortSensor, "sensorRearLeft", carName, this));
            allSensors.add(Sensor.createAngleSensor(SensorTool.shortSensorLength, (float)(-Math.PI/180*180), (float)(-Math.PI/180*225), carHeight, SensorTool.numberOfPartsForShortSensor, "sensorRearLeftCorner1", carName, this));
            allSensors.add(Sensor.createAngleSensor(SensorTool.shortSensorLength, (float)(-Math.PI/180*225), (float)(-Math.PI/180*270), carHeight, SensorTool.numberOfPartsForShortSensor, "sensorRearLeftCorner2", carName, this));
            allSensors.add(Sensor.createLinearStripSensor(SensorTool.shortSensorLength, carLength/2, carHeight, SensorTool.numberOfPartsForShortSensor, "sensorLeft1", carName, this));
            allSensors.add(Sensor.createLinearStripSensor(SensorTool.shortSensorLength, carLength/2, carHeight, SensorTool.numberOfPartsForShortSensor, "sensorLeft2", carName, this));
            allSensors.add(Sensor.createAngleSensor(SensorTool.longSensorLength, (float)(Math.PI/180*0), (float)(Math.PI/180*45), carHeight, SensorTool.numberOfPartsForLongSensor, "sensorFrontLeftCorner1", carName, this));
            allSensors.add(Sensor.createAngleSensor(SensorTool.longSensorLength, (float)(Math.PI/180*45), (float)(Math.PI/180*90), carHeight, SensorTool.numberOfPartsForLongSensor, "sensorFrontLeftCorner2", carName, this));
//            dynamicsWorld.getPairCache().findPair(proxy0, proxy1)
            for(TransformGroup sensor : allSensors){
            	sensor.setTransform(new Transform());
            	sensor.getTransform().setIdentity();
            	((Sensor)sensor).addToWorld(dynamicsWorld);
            }
            allSensors.get(0).getTransform().origin.set((carWidth/4+carWidth/8),0,0);
            allSensors.get(1).getTransform().origin.set(carWidth/8,0,0);
            allSensors.get(2).getTransform().origin.set(-carWidth/8,0,0);
            allSensors.get(3).getTransform().origin.set(-(carWidth/4+carWidth/8),0,0);
            allSensors.get(4).getTransform().origin.set(-(carWidth/2),0,0);
            allSensors.get(5).getTransform().origin.set(-(carWidth/2),0,0);
            allSensors.get(6).getTransform().origin.set(-(carWidth/2),0,-carLength/4);
            allSensors.get(6).getTransform().setRotation(new Quat4f(0, 1*(float)Math.sin(-Math.PI/4), 0, (float)Math.cos(-Math.PI/4)));
            allSensors.get(7).getTransform().origin.set(-(carWidth/2),0,-carLength/2-carLength/4);
            allSensors.get(7).getTransform().setRotation(new Quat4f(0, 1*(float)Math.sin(-Math.PI/4), 0, (float)Math.cos(-Math.PI/4)));
            allSensors.get(8).getTransform().origin.set(-(carWidth/2),0,-carLength);
            allSensors.get(9).getTransform().origin.set(-(carWidth/2),0,-carLength);
            allSensors.get(10).getTransform().origin.set(-(carWidth/4),0,-carLength);
            allSensors.get(10).getTransform().setRotation(new Quat4f(0, 1*(float)Math.sin(-Math.PI/2), 0, (float)Math.cos(-Math.PI/2)));
            allSensors.get(11).getTransform().origin.set(+(carWidth/4),0,-carLength);
            allSensors.get(11).getTransform().setRotation(new Quat4f(0, 1*(float)Math.sin(-Math.PI/2), 0, (float)Math.cos(-Math.PI/2)));
            allSensors.get(12).getTransform().origin.set((carWidth/2),0,-carLength);
            allSensors.get(13).getTransform().origin.set((carWidth/2),0,-carLength);
            allSensors.get(14).getTransform().origin.set((carWidth/2),0,-carLength/4);
            allSensors.get(14).getTransform().setRotation(new Quat4f(0, 1*(float)Math.sin(Math.PI/4), 0, (float)Math.cos(Math.PI/4)));
            allSensors.get(15).getTransform().origin.set((carWidth/2),0,-carLength/2-carLength/4);
            allSensors.get(15).getTransform().setRotation(new Quat4f(0, 1*(float)Math.sin(Math.PI/4), 0, (float)Math.cos(Math.PI/4)));
            allSensors.get(16).getTransform().origin.set((carWidth/2),0,0);
            allSensors.get(17).getTransform().origin.set((carWidth/2),0,0);
//            for(int i=0; i<4; i++){
//	            PairCachingGhostObject go = new PairCachingGhostObject();
//	            go.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
//	            Transform t = new Transform(transform);
//	            t.origin.y = t.origin.y + 1;
//	            t.origin.z +=i-carWidth;
//	            t.origin.x +=-4*carWidth;
//	            go.setWorldTransform(t);
//	            ghostShape.setMargin(0);
//	            ghostShape.calculateLocalInertia(0, localInertia);
//	            go.setCollisionShape(ghostShape);
//	            dynamicsWorld.addCollisionObject(go, (short)0x10, (short)0x0f);
//	            ghostObject.add(go);
//            }
//            dynamicsWorld.getBroadphase().getOverlappingPairCache().setInternalGhostPairCallback(new OverlappingPairCallback() {
//				
//				@Override
//				public void removeOverlappingPairsContainingProxy(BroadphaseProxy proxy0,
//						Dispatcher dispatcher) {
//					System.out.println("remove " + proxy0 + dispatcher);
//					
//				}
//				
//				@Override
//				public Object removeOverlappingPair(BroadphaseProxy proxy0,
//						BroadphaseProxy proxy1, Dispatcher dispatcher) {
//					System.out.println("remove2 " + proxy1 + dispatcher);
//					return null;
//				}
//				
//				@Override
//				public BroadphasePair addOverlappingPair(BroadphaseProxy proxy0,
//						BroadphaseProxy proxy1) {
//					System.out.println("add " + proxy0 + proxy1);
//					return null;
//				}
//			});

//            CollisionShape cs = new BoxShape(new Vector3f(3,3,3));
//	        Transform t =  new Transform();
//	        t.setIdentity();
//	        t.set(transform);
//	        t.origin.y = 5;
//            MotionState motionState = new DefaultMotionState(t);
//            cs.calculateLocalInertia(10, new Vector3f(0, 0, 0));
//			DefaultMotionState myMotionState = new DefaultMotionState(t);
//			RigidBodyConstructionInfo rbInfo = new RigidBodyConstructionInfo(20, myMotionState, cs, localInertia);
//			RigidBody body = new RigidBody(rbInfo);
			// add the body to the dynamics world
//			dynamicsWorld.addRigidBody(body);
	        
            CompoundShape compound = new CompoundShape();
            //collisionShapes.add(compound);
            Transform localTrans = new Transform();
            localTrans.setIdentity();
            // localTrans effectively shifts the center of mass with respect to the chassis
            localTrans.origin.set(0, 1, 0);
            compound.addChildShape(localTrans, chassisShape);
            Transform localTrans1 = new Transform();
            localTrans1.setIdentity();
            // localTrans effectively shifts the center of mass with respect to the chassis
            localTrans1.origin.set(0, 1.5f, carLength*4 + carLength/2);
            
            RigidBody carChassis;
            if(carsCollide) {
                carChassis = RacePhysics.createRigidBody(mass, transform, compound, dynamicsWorld);
            }
            else {
                carChassis = RacePhysics.createRigidBody(mass, transform, compound,
                    RacePhysics.carColGroup, RacePhysics.carColMask, dynamicsWorld);
            }
            //carChassis.setDamping(0.0f, 0.5f);

            
            tuning = new VehicleTuning();
            vehicleRayCaster = new DefaultVehicleRaycaster(dynamicsWorld);
            vehicle = new RaycastVehicle(tuning, carChassis, vehicleRayCaster);

            carChassis.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
            carChassis.setUserPointer(this);
            dynamicsWorld.addVehicle(vehicle);
            
            tuning.frictionSlip = wheelFriction;
            tuning.maxSuspensionTravelCm = maxSuspensionTravelCm;
            tuning.suspensionCompression = suspensionCompression;
            tuning.suspensionDamping = suspensionDamping;
            tuning.suspensionStiffness = suspensionStiffness;

            setupVehicle();
        }
        
        
        
        private void setupVehicle() {
            // choose coordinate system
            int rightIndex = 0;
            int upIndex = 1;
            int forwardIndex = 2;
            vehicle.setCoordinateSystem(rightIndex, upIndex, forwardIndex);

            //směr jízdy
            Vector3f wheelDirectionCS0 = new Vector3f(0,-1,0);
            //směr osy kol
            Vector3f wheelAxleCS = new Vector3f(-1,0,0);
            
            //přední kola
            boolean isFrontWheel = true;
            Vector3f connectionPointCS0 = new Vector3f(wheelTrack/2, connectionHeight, wheelbase/2);
            vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, tuning, isFrontWheel);

            connectionPointCS0.set(-wheelTrack/2, connectionHeight, wheelbase/2);
            vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, tuning, isFrontWheel);

            //zadní kola
            isFrontWheel = false;
            connectionPointCS0.set(wheelTrack/2, connectionHeight, -wheelbase/2);
            vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, tuning, isFrontWheel);

            connectionPointCS0.set(-wheelTrack/2, connectionHeight, -wheelbase/2);
            vehicle.addWheel(connectionPointCS0, wheelDirectionCS0, wheelAxleCS, suspensionRestLength, wheelRadius, tuning, isFrontWheel);


            /*for (int i = 0; i < vehicle.getNumWheels(); i++) {
                    WheelInfo wheel = vehicle.getWheelInfo(i);
                    wheel.rollInfluence = rollInfluence;
            }*/

            vehicle.setgMaxSuspensionForce(maxSuspensionForce);
        }

        /** nastaví kolize s ostatními auty */
        public void setCollisions(boolean collisions) {
            RigidBody carChassis = vehicle.getRigidBody();
            Transform transform = new  Transform();
            carChassis.getWorldTransform(transform);
            CollisionShape compound = carChassis.getCollisionShape();
            dynamicsWorld.removeRigidBody(carChassis);
            if(collisions) {
                carChassis = RacePhysics.createRigidBody(mass, transform, compound, dynamicsWorld);
            }
            else {
                carChassis = RacePhysics.createRigidBody(mass, transform, compound,
                    RacePhysics.carColGroup, RacePhysics.carColMask, dynamicsWorld);
            }
            
            dynamicsWorld.removeVehicle(vehicle);
            vehicle = new RaycastVehicle(tuning, carChassis, vehicleRayCaster);
            
            carChassis.setActivationState(CollisionObject.DISABLE_DEACTIVATION);
            dynamicsWorld.addVehicle(vehicle);

            setupVehicle();
        }

        /**
         * Nastaví úhel natočení kol
         * @param value úhel v radiánech
         */
        public void setSteering(float value) {
              vehicle.setSteeringValue(value, 0);
              vehicle.setSteeringValue(value, 1);
        }

        /**
         * Vrátí úhel natočení kol.
         * @return úhel v radiánech
         */
        public float getSteering() {
              return vehicle.getSteeringValue(0);
        }

        /**
         * Nastaví brzdový pedál (1 = max stlačení).
         * @param value hodnota v rozmezí [0,1]
         */
        public void setBrake(float value) {
            vehicle.applyEngineForce(0, 0);
            vehicle.applyEngineForce(0, 1);
            vehicle.applyEngineForce(0, 2);
            vehicle.applyEngineForce(0, 3);

            vehicle.setBrake(value*maxBrakingForce, 2);
            vehicle.setBrake(value*maxBrakingForce, 3);

            vehicle.getRigidBody().applyCentralForce(airResistance());
        }

        /**
         * Nastaví plynový pedál (1 = max stlačení).
         * Záporné hodnoty značí zpětný chod (-1 = max zpátečka).
         * @param value hodnota v rozmezí [)-1,1]
         */
        public void setThrottle(float value) {
            
            vehicle.setBrake(0, 2);
            vehicle.setBrake(0, 3);

            if(drive == Drive.FRONT) {
              vehicle.applyEngineForce(engineForce(value)/2, 0);
              vehicle.applyEngineForce(engineForce(value)/2, 1);
            }
            if(drive == Drive.REAR) {
              vehicle.applyEngineForce(engineForce(value)/2, 2);
              vehicle.applyEngineForce(engineForce(value)/2, 3);
            }
            else {
              vehicle.applyEngineForce(engineForce(value)/4, 0);
              vehicle.applyEngineForce(engineForce(value)/4, 1);
              vehicle.applyEngineForce(engineForce(value)/4, 2);
              vehicle.applyEngineForce(engineForce(value)/4, 3);
            }
            vehicle.getRigidBody().applyCentralForce(airResistance());
        }

        /**
         * Vypočte sílu, kterou působí motor auta.
         * @param throttle stlačení plynového pedálu 0-1
         * @return síla v Nm
         */
        private float engineForce(float throttle) {
            float speed = this.vehicle.getCurrentSpeedKmHour()/3.6f;
            float wheelRotation = speed/wheelRadius;
            rpm = (wheelRotation * gearRatios[gear-1] * diffRatio * 60) / (float)(2*Math.PI);
            if(rpm < 1000) {
                rpm = 1000;
            }

            if(rpm > shiftUpRPM && gear < topGear)
                gear++;
            else if(rpm < shiftDownRPM  && gear > 1)
                gear--;

            //síla motoru
            float engineForce = (throttle * maxTorque * gearRatios[gear - 1] * diffRatio * efficiency) / wheelRadius;
            return engineForce;
        }

        private Vector3f airResistance() {
            Vector3f vector = new Vector3f();
            vector = vehicle.getRigidBody().getLinearVelocity(vector);
            if(vector.length() != 0) {
                float speed = vector.length();
                float airRes = 0.5f * airCoef * airDensity * carHeight * carWidth * speed * speed;
                vector.normalize();
                vector.scale(-airRes);
                return vector;
            }
            else {
                return new Vector3f(0,0,0);
            }
        }

/*        public Vector3f detectRay(float angle) {
            Transform tr = new Transform();
            vehicle.getChassisWorldTransform(tr);
            Vector3f vec = new Vector3f();
            vec = vehicle.getForwardVector(vec);
            vec.scale(100.0f);
            vec = Util.rotateVec(vec, angle);
            Vector3f from = new Vector3f(tr.origin.x, tr.origin.y+1, tr.origin.z);
            Vector3f to = new Vector3f(tr.origin.x + vec.x, tr.origin.y+1, tr.origin.z + vec.z);
            CollisionWorld.ClosestRayResultCallback rayCallback = new CollisionWorld.ClosestRayResultCallback(tr.origin, to);
            if(!carCollisions) {
                //vypne detekci ostatních aut
                rayCallback.collisionFilterGroup = RacePhysics.carColGroup;
                rayCallback.collisionFilterMask = RacePhysics.carColMask;
            }
            
            synchronized(dynamicsWorld) {
                dynamicsWorld.rayTest(from, to, rayCallback);
            }
            
            if(rayCallback.hasHit()) {
                RigidBody body = RigidBody.upcast(rayCallback.collisionObject);
                Vector3f pos = new Vector3f();
                return body.getCenterOfMassPosition(pos);
            }
            else
                return null;
        }
*/
        private Transform chassisWorldTransform = new Transform();
        private Transform tmpSensorTransform = new Transform();
        public HashMap<String, Float> detect(HashMap<String, Float> sensors) {
//            Transform tr = new Transform();
//            vehicle.getChassisWorldTransform(tr);
//            Vector3f vec = new Vector3f();
//            vec = vehicle.getForwardVector(vec);
            //vec = Util.rotateVec(vec, -vehicle.getSteeringValue(0));
            //BoxShape shape = sensorShapes.get(sensorWidth);
//            if(shape == null) {
//                shape = new BoxShape(new Vector3f(carWidth/2 * sensorWidth, carHeight/2, carLength/2));
//                sensorShapes.put(sensorWidth, shape);
//            }
//            Vector3f from = new Vector3f(tr.origin.x +vec.x*(carLength), tr.origin.y+40, tr.origin.z + vec.z*(carLength));
//            tr.origin.set(from);
//            float sensorLength = this.getSpeedKmh()/2;
//            float minLength = 20;
//            if(sensorLength < minLength) {
//               sensorLength = minLength;
//            }
//            vec.scale(sensorLength);
//            Vector3f to = new Vector3f(from.x/*+vec.x*/, from.y-39, from.z/*+vec.z*/ );
//            Transform tr2 = new Transform(tr);
//            tr2.origin.set(to);
//            GhostObject ghostObject = new GhostObject();
//            ghostObject.setCollisionFlags(CollisionFlags.NO_CONTACT_RESPONSE);
//            ghostObject.setCollisionShape(shape);
//            shape.setMargin(0);
//            Vector3f localInertia = new Vector3f(0f, 0f, 0f);
//            shape.calculateLocalInertia(0, localInertia);
//            ghostObject.convexSweepTest(shape, tr, tr2, new CollisionWorld.ConvexResultCallback(){
//
//				@Override
//				public float addSingleResult(LocalConvexResult convexResult,
//						boolean normalInWorldSpace) {
//					System.out.println("hit");
//					return 0;
//				}
//            	
//            }, 0);
//        	Transform t = new Transform();
        	vehicle.getChassisWorldTransform(chassisWorldTransform);
//        	Transform tmp = new Transform();
            //allSensors.setTransform(tmp)
//        	Vector3f from = new Vector3f(tr.origin.x -vec.x*(carLength/2), tr.origin.y+1, tr.origin.z - vec.z*(carLength/2));
            allSensors.applyTransform(chassisWorldTransform, tmpSensorTransform);
//            HashMap<String, Float> retMap = new HashMap<String, Float>();
//            for(PersistentManifold m : dynamicsWorld.getDispatcher().getInternalManifoldPointer()){
//            	CollisionObject o1 = (CollisionObject)m.getBody0();
//            	CollisionObject o2 = (CollisionObject)m.getBody1();
//            	System.out.println(o1.getUserPointer() + " > " + o2.getUserPointer());
//            }
            for (TransformGroup tg : allSensors){
            	Sensor s  = (Sensor)tg;
            	sensors.put(s.getName(), s.getValue());
            }
//            for(int i=0; i<4; i++){
//            	Transform t = new Transform();
//            	vehicle.getChassisWorldTransform(t);
//            	int offset = i-2;
//            	if(offset >= 0){
//            		offset++;
//            	}
//            	vec = vehicle.getForwardVector(vec);
//            	Vector3f from = new Vector3f(tr.origin.x -vec.x*(carLength/2), tr.origin.y+1, tr.origin.z - vec.z*(carLength/2));
//            	Vector3f f = new Vector3f(t.origin.x +vec.x*(carLength*2+carLength/2) - vec.z*((carWidth/2)*offset - Math.signum(offset)*carWidth/4), t.origin.y+3, tr.origin.z + vec.z*(carLength*2+carLength/2)+ vec.x*((carWidth/2)*offset - Math.signum(offset)*carWidth/4));
//            	t.origin.set(f);
//                ghostObject.get(i).setWorldTransform(t);
//                if(ghostObject.get(i).getNumOverlappingObjects() > 0){
//                	System.out.println(ghostObject.get(i).getNumOverlappingObjects());
//                }
//            }
//            for(int i=0; i<4; i++){
//                if(ghostObject.get(i).getNumOverlappingObjects() > 0){
//                	return i;
//                }
//            }
            return sensors;
//            CollisionWorld.ClosestConvexResultCallback convCallback = new CollisionWorld.ClosestConvexResultCallback(from, to);
//            if(!carCollisions) {
//                //vypne detekci ostatních aut
//                convCallback.collisionFilterGroup = RacePhysics.carColGroup;
//                convCallback.collisionFilterMask = RacePhysics.carColMask;
//            }
//
//            synchronized(dynamicsWorld) {
//                dynamicsWorld.convexSweepTest(shape, tr, tr2, convCallback);
//            }
//
//            if(convCallback.hasHit()) {
//                
//                RigidBody body = RigidBody.upcast(convCallback.hitCollisionObject);
//                if(body == null || body == ground || body == vehicle.getRigidBody()) {
//                    //System.out.println("ground hit " + sensorWidth);
//                    return null;
//                }
//                else {
//                    //System.out.println("hit " + sensorWidth);
//                    Vector3f pos = new Vector3f();
//                    return body.getCenterOfMassPosition(pos);
//                }
//            }
//            else
//                return null;
        }
        
        
        public void resetCar(float x, float y, float z, float angle) {
            Transform tr = new Transform();
            tr.setIdentity();
            tr.origin.set(x, y, z);

            Quat4f quat = new Quat4f();
            QuaternionUtil.setRotation(quat, new Vector3f(0,1,0), angle);
            tr.setRotation(quat);

            setSteering(0f);
            setThrottle(0f);
            this.getRaycastVehicle().getRigidBody().setAngularVelocity(new Vector3f(0,0,0));
            this.getRaycastVehicle().getRigidBody().setLinearVelocity(new Vector3f(0,0,0));
            this.getRaycastVehicle().getRigidBody().setCenterOfMassTransform(tr);
        }


        public float getX() {
            Transform tr = new Transform();
            vehicle.getChassisWorldTransform(tr);
            return tr.origin.x;
        }

        public float getY() {
            Transform tr = new Transform();
            vehicle.getChassisWorldTransform(tr);
            return tr.origin.y;
        }

        public float getZ() {
            Transform tr = new Transform();
            vehicle.getChassisWorldTransform(tr);
            return tr.origin.z;
        }

        public Vector3f getPosition() {
            Transform tr = new Transform();
            vehicle.getChassisWorldTransform(tr);
            return tr.origin;
        }

        public float getSpeedKmh() {
            return vehicle.getCurrentSpeedKmHour();
        }

        public float getAngle() {
            Quat4f quat = new Quat4f();
            vehicle.getRigidBody().getOrientation(quat);
            float angle = Util.quatToAngle(quat);
            return angle;
        }

        public RaycastVehicle getRaycastVehicle() {
            return this.vehicle;
        }

        
        public float getCarHeight() {
            return carHeight;
        }

        public float getCarLength() {
            return carLength;
        }

        public float getCarWidth() {
            return carWidth;
        }

        public float getWheelRadius() {
            return wheelRadius;
        }

        public float getWheelWidth() {
            return wheelWidth;
        }

        public int getGear() {
            return gear;
        }

        public float getRpm() {
            return rpm;
        }

        public float getMaxSpeed() {
            return maxSpeed;
        }

        public void setFriction(float value) {
            for(WheelInfo wheel: this.vehicle.wheelInfo) {
                wheel.frictionSlip = value;
            }
        }

        public float getOffroadBonus() {
            return offroadBonus;
        }

        public float getAsphaltBonus() {
            return asphaltBonus;
        }



        public float getSkidInfo(int wheelNum) {
            return vehicle.getWheelInfo(wheelNum).skidInfo;
        }


        public boolean isSkidding(int wheelNum) {
            if(this.vehicle.getWheelInfo(wheelNum).skidInfo < 0.05f)
                return true;
            else
                return false;
        }

    /**
     * Načte parametry vozidla ze souboru
     * @param file cesta k souboru
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void readDataFromSpecification(HashMap<String, String> values) {
    	maxTorque  = values.containsKey("maxTorque")?Float.parseFloat(values.get("maxTorque")): maxTorque;
        maxBrakingForce = values.containsKey("maxBrakingForce")?Float.parseFloat(values.get("maxBrakingForce")): maxBrakingForce;
        maxSpeed = values.containsKey("maxSpeed")?Float.parseFloat(values.get("maxSpeed")): maxSpeed;
        drive = Drive.valueOf(values.get("drive"));
        wheelRadius = values.containsKey("wheelRadius")?Float.parseFloat(values.get("wheelRadius")): wheelRadius;
        wheelWidth = values.containsKey("wheelWidth")?Float.parseFloat(values.get("wheelWidth")): wheelWidth;
        wheelFriction = values.containsKey("wheelFriction")?Float.parseFloat(values.get("wheelFriction")): wheelFriction;
        asphaltBonus = values.containsKey("asphaltBonus")?Float.parseFloat(values.get("asphaltBonus")): asphaltBonus;
        offroadBonus = values.containsKey("offroadBonus")?Float.parseFloat(values.get("offroadBonus")): offroadBonus;
        suspensionStiffness = values.containsKey("suspensionStiffness")?Float.parseFloat(values.get("suspensionStiffness")): suspensionStiffness;
        suspensionDamping = values.containsKey("suspensionDampingCoef")?Float.parseFloat(values.get("suspensionDampingCoef")): suspensionDamping;
        suspensionCompression = values.containsKey("suspensionCompressionCoef")?Float.parseFloat(values.get("suspensionCompressionCoef")): suspensionCompression;
        suspensionRestLength = values.containsKey("suspensionRestLength")?Float.parseFloat(values.get("suspensionRestLength")): suspensionRestLength;
        connectionHeight = values.containsKey("connectionHeight")?Float.parseFloat(values.get("connectionHeight")): connectionHeight;
        maxSuspensionForce = values.containsKey("maxSuspensionForce")?Float.parseFloat(values.get("maxSuspensionForce")): maxSuspensionForce;
        maxSuspensionTravelCm = values.containsKey("maxSuspensionTravelCm")?Float.parseFloat(values.get("maxSuspensionTravelCm")): maxSuspensionTravelCm;
        mass = values.containsKey("mass")?Float.parseFloat(values.get("mass")): mass;
        airCoef = values.containsKey("airCoef")?Float.parseFloat(values.get("airCoef")): airCoef;
        carWidth = values.containsKey("carWidth")?Float.parseFloat(values.get("carWidth")): carWidth;
        carHeight = values.containsKey("carHeight")?Float.parseFloat(values.get("carHeight")): carHeight;
        carLength = values.containsKey("carLength")?Float.parseFloat(values.get("carLength")): carLength;
        wheelbase = values.containsKey("wheelbase")?Float.parseFloat(values.get("wheelbase")): wheelbase;
        wheelTrack = values.containsKey("wheelTrack")?Float.parseFloat(values.get("wheelTrack")): wheelTrack;
        rollInfluence = values.containsKey("rollInfluence")?Float.parseFloat(values.get("rollInfluence")): rollInfluence;
        topGear = values.containsKey("topGear")?(int)Float.parseFloat(values.get("topGear")): topGear;
        if(values.containsKey("gearRatios")){
	        String[] stringGearRatio = values.get("gearRatios").split(" ");
	        gearRatios = new float[topGear];
	        for(int g = 0; g < topGear && g < stringGearRatio.length; g++) {
	            gearRatios[g] = Float.parseFloat(stringGearRatio[g]);
	        }
        }
        diffRatio = values.containsKey("diffRatio")?Float.parseFloat(values.get("diffRatio")): diffRatio;
        shiftDownRPM = values.containsKey("shiftDownRPM")?(int)Float.parseFloat(values.get("shiftDownRPM")): shiftDownRPM;
        shiftUpRPM = values.containsKey("shiftUpRPM")?(int)Float.parseFloat(values.get("shiftUpRPM")): shiftUpRPM;
        efficiency = values.containsKey("efficiency")?Float.parseFloat(values.get("efficiency")): efficiency;
    }

	@Override
	public String toString() {
		return "PhysicalCar [carName=" + carName + "]";
	} 
    
}
