package gwt.seca.client.util;


import javax.vecmath.Vector3f;

/**
 * Defines a plane in 3D space.
 * @author stagiaire
 * Adapted from Ogre3D: http://www.ogre3d.org/docs/api/html/classOgre_1_1Plane.html
 */
public class Plane {
	
	public enum Side {
		NO_SIDE,
		POSITIVE_SIDE, 	
		NEGATIVE_SIDE,	
		BOTH_SIDE
	}
	
	private Vector3f mNormal;
	private float mConstant;
	
	/**
	 * Default constructor - sets everything to 0. 
	 */
	public Plane() {
		mNormal = new Vector3f();
		mConstant = 0;
	}
	/**
	 * Copy constructor
	 * @param plane
	 */
	public Plane(Plane plane) {
		mNormal = new Vector3f(plane.mNormal);
		mConstant = plane.mConstant;
	}
	public Plane(float a, float b, float c, float d) {
		mNormal = new Vector3f(a, b, c);
		mConstant = d;
	}
	public Plane(Vector3f normal, float constant) {
		mNormal = new Vector3f(normal);
		mConstant = constant;
	}
	public Plane(Vector3f normal, Vector3f point) {
		mNormal = new Vector3f(normal);
		mConstant = -normal.dot(point);
	}
	public void setNormal(Vector3f normal) {
		mNormal.set(normal);
	}
	public Vector3f getNormal() {
		return mNormal;
	}
	public void setConstant(float constant) {
		mConstant = constant;
	}
	public float getConstant() {
		return mConstant;
	}
	/**
	 * Normalize the plane,
	 * ie the plane's normal and the length scale of d as well.
	 * @return The previous length of the plane's normal
	 */
	public float normalize() {
		float length = mNormal.length();
		if (length>0) {
			mNormal.normalize();
			mConstant = mConstant/length;
		}
		return length;
	}
 	/**
 	 * This is a pseudodistance.
 	 * The sign of the return value is positive if the point is on the positive side of the plane, negative if the point is on the negative side, and zero if the point is on the plane.
 	 * The absolute value of the return value is the true distance only when the plane normal is a unit length vector.
 	 * @param point
 	 * @return
 	 */
	public float getDistance(Vector3f point) {
		float distance = mNormal.dot(point);
		return distance;
	}
	/**
	 * Return the side where the aligneBox is.
	 * @param point
	 * @return
	 */
	public Side getSide(Vector3f point) {
		float distance = mNormal.dot(point);
		if (distance>0) {
			return Side.POSITIVE_SIDE;
		} else if (distance<0) {
			return Side.NEGATIVE_SIDE;
		}
		return Side.NO_SIDE;
	}
	/**
	 * Return the side where the aligneBox is.
	 * The flag BOTH_SIDE indicates an intersecting box. One corner ON the plane is sufficient to consider the box and the plane intersecting.
	 * @param box
	 * @return
	 */
	public Side getSide(AxisAlignedBox box) {
		//TODO
		return null;
	}
	

}
