package gwt.seca.client.util;


import javax.vecmath.Vector3f;

/**
 * A sphere primitive, mostly used for bounds checking.
 * @author stagiaire
 * Adapted from Ogre3D: http://www.ogre3d.org/docs/api/html/classOgre_1_1Sphere.html 
 */
public class Sphere {
	/**
	 * Standard constructor - creates a unit sphere around the origin.
	 */
	public Sphere() {
		this(new Vector3f(), 1);
	}
	/**
	 * Copy constructor.
	 */
	public Sphere(Sphere sphere) {
		this(sphere.mCenter, sphere.mRadius);
	}
	/**
	 * Constructor allowing arbitrary spheres.
	 * @param center
	 * @param radius
	 */
	public Sphere(Vector3f center, float radius) {
		mCenter = new Vector3f(center);
		mRadius = radius;
	}	
	public void setCenter(Vector3f center) {
		mCenter.set(center);
	}
	public Vector3f getCenter() {
		return mCenter;
	}
	public void setRadius(float radius) {
		mRadius = radius;
	}
	public float getRadius() {
		return mRadius;
	}
	public float getRadiusSquared() {
		return mRadius*mRadius;
	}
	/**
	 * Returns whether or not this sphere intersects another sphere.
	 * @param sphere
	 * @return
	 */
	public boolean intersects(Sphere sphere) {
		Vector3f centerVec = new Vector3f();
		centerVec.scaleAdd(-1, mCenter, sphere.mCenter);
		float sqrCenterDist = centerVec.lengthSquared();
		float sqrRadiusDist = (sphere.mRadius + mRadius)*(sphere.mRadius + mRadius);
		return (sqrCenterDist <= sqrRadiusDist);
	}
	/**
	 * Returns whether or not this sphere intersects a box.
	 * @param box
	 * @return
	 */
	public boolean intersects(AxisAlignedBox box) {
		return SecaMath.intersect(this, box);
	}
	/**
	 * Returns whether or not this sphere intersects a plane.
	 * @param plane
	 * @return
	 */
	public boolean intersects(Plane plane) {
		return SecaMath.intersect(this, plane);
	}
	/**
	 * Returns whether or not this sphere intersects a point.
	 * @param vector
	 * @return
	 */
	public boolean intersects(Vector3f vector) {
		Vector3f diffVector = new Vector3f();
		diffVector.scaleAdd(-1, mCenter, vector);
		float sqrDistance = diffVector.lengthSquared();
		return (sqrDistance <= (mRadius)*(mRadius));
	}
	
	final private Vector3f mCenter;
	private float mRadius;

}
