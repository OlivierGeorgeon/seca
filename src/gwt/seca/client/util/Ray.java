package gwt.seca.client.util;


import javax.vecmath.Vector3f;

/**
 * Representation of a ray in space 
 * i.e. a line with an origin and direction.
 * The direction should be normalized.
 * @author stagiaire
 * Adapted from Ogre3D: http://www.ogre3d.org/docs/api/html/classOgre_1_1Ray.html
 */
public class Ray {
	
	public Ray() {
		mOrigin = new Vector3f();
		mDirection = new Vector3f();
		return;
	}
	
	public Ray(Vector3f origin, Vector3f direction) {
		this();
		this.setOrigin(origin);
		this.setDirection(direction);
	}
	/**
	 * Sets the origin of the ray. 
	 * @param origin
	 */
	public void setOrigin(Vector3f origin) {
		mOrigin.set(origin);
	}
	/**
	 * Gets the origin of the ray. 
	 * @return
	 */
	public Vector3f getOrigin() {
		return new Vector3f(mOrigin);
	}
	/**
	 * Sets the direction of the ray.
	 * @param direction
	 */
	public void setDirection(Vector3f direction) {
		mDirection.set(direction);
	}
	/**
	 * Gets the direction of the ray.
	 * @return
	 */
	public Vector3f getDirection() {
		return new Vector3f(mDirection);
	}
	/**
	 * Gets the position of a point t units along the ray. 
	 * @param t
	 * @return
	 */
	public Vector3f getPoint(float t) {
		Vector3f point = new Vector3f();
		point.scaleAdd(t, mDirection, mOrigin);
		return point;
	}
	
	/**
	 * Normalize the ray,
	 * ie the ray's direction.
	 * @return The previous length of the ray's direction
	 */
	public float normalize() {
		float length = mDirection.length();
		if (length>0) {
			mDirection.normalize();
		}
		return length;
	}
	
	/**
	 * Tests whether this ray intersects the given plane.
	 * @param plane
	 * @return
	 */
	public Pair<Boolean, Float> intersects(Plane plane) {
		return SecaMath.intersect(this, plane);
	}
//	/**
//	 * Tests whether this ray intersects the given plane bounded volume.
//	 * @param volume
//	 * @return
//	 */
//	Pair<Boolean, Float> intersects(PlaneBoundedVolume volume) {
//		return SecaMath.intersect(this, volume);
//	}
	/**
	 * Tests whether this ray intersects the given sphere.
	 * @param sphere
	 * @return
	 */
	public Pair<Boolean, Float> intersects(Sphere sphere) {
		return SecaMath.intersect(this, sphere);
	}
	/**
	 * Tests whether this ray intersects the given box.
	 * @param box
	 * @return
	 */
	public Pair<Boolean, Float> intersects(AxisAlignedBox box) {
		return SecaMath.intersect(this, box);
	}
	/**
	 * Returns a string that contains the values of this Ray. The form is "Origin=(x,y,z), Direction=(x,y,z)".

	 */
	public String toString() {
		return "Ogirin="+this.getOrigin().toString()+", Direction="+this.getDirection().toString();
	}
	
	final private Vector3f mOrigin;
	final private Vector3f mDirection;
}
