package cz.vsb.cs.neurace.race;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * Různé užitečné funkce.
 * @author Petr Hamalcik
 */
public class Util {
    /**
     * Vypočte úhel rotace okolo osy y quaternionu
     * @param q1 quaternion
     * @return úhel v radiánech
     */
    public static float quatToAngle(Quat4f q1) {
        float heading;
        float test = q1.x*q1.y + q1.z*q1.w;

	if (test > 0.499) { // singularity at north pole
		heading = 2 * (float)Math.atan2(q1.x,q1.w);
		//attitude = Math.PI/2;
		//bank = 0;
	}
        else if(test < -0.499) { // singularity at south pole
		heading = -2 * (float)Math.atan2(q1.x,q1.w);
		//attitude = - Math.PI/2;
		//bank = 0;
	}
        else {
            double sqx = q1.x*q1.x;
            double sqy = q1.y*q1.y;
            double sqz = q1.z*q1.z;
            heading = (float)Math.atan2(2*q1.y*q1.w-2*q1.x*q1.z , 1 - 2*sqy - 2*sqz);
            //attitude = asin(2*test);
            //bank = atan2(2*q1.x*q1.w-2*q1.y*q1.z , 1 - 2*sqx - 2*sqz)
            
        }
        //heading += Math.PI;
        return heading;
    }

    /**
     * Vytvoří quaternion z úhlů v radiánech
     * @param heading rotace okolo osy y
     * @param attitude rotace okolo osy z
     * @param bank rotace okolo osy x
     * @return Quaternion
     */
    /*public static Quat4f angleToQuat(double heading, double attitude, double bank) {
        Quat4f q = new Quat4f();
        float c1 = (float)Math.cos(heading);
        float s1 = (float)Math.sin(heading);
        float c2 =(float)Math.cos(attitude);
        float s2 = (float)Math.sin(attitude);
        float c3 = (float)Math.cos(bank);
        float s3 = (float)Math.sin(bank);
        q.w = (float)Math.sqrt(1.0 + c1 * c2 + c1*c3 - s1 * s2 * s3 + c2*c3) / 2.0f;
        float w4 = (4.0f * q.w);
        if(q.w != 0.0) {
            q.x = (c2 * s3 + c1 * s3 + s1 * s2 * c3) / w4 ;
            q.y = (s1 * c2 + s1 * c3 + c1 * s2 * s3) / w4 ;
            q.z = (-s1 * s3 + c1 * s2 * c3 +s2) / w4 ;
        }
        return q;
    }*/

    /**
     * Rotuje vektor okolo osy y
     * @param vector původní vektor
     * @param angle úhel rotace v radiánech
     * @return
     */
    public static Vector3f rotateVec(Vector3f vector, float angle) {
        float x = (float)Math.cos(angle)*vector.x - (float)Math.sin(angle)*vector.z;
        float z = (float)Math.sin(angle)*vector.x + (float)Math.cos(angle)*vector.z;
        return new Vector3f(x, vector.y, z);
    }

}
