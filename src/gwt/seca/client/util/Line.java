package gwt.seca.client.util;

import javax.vecmath.Vector3f;

/**
 * Defines a Line in 3D space
 * @author stagiaire
 *
 */
public class Line {
	
	private Vector3f mStartPoint;
	private Vector3f mEndPoint;
	
	/**
	 * Default constructor - sets everything to 0. 
	 */
	public Line() {
		mStartPoint = new Vector3f();
		mEndPoint = new Vector3f();
	}
	public Line(Vector3f startPoint, Vector3f endPoint) {
		this();
		this.setStartPoint(startPoint);
		this.setEndPoint(endPoint);
	}
	public Line(Ray ray, float t) {
		this();
		this.setStartPoint(ray.getOrigin());
		this.setEndPoint(ray.getPoint(t));
	}
	
	public Vector3f getStartPoint() {
		return new Vector3f(mStartPoint);
	}
	public void setStartPoint(Vector3f point) {
		mStartPoint.set(point);
	}
	public Vector3f getEndPoint() {
		return new Vector3f(mEndPoint);
	}
	public void setEndPoint(Vector3f point) {
		mEndPoint.set(point);
	}
	
	/**
	 * Gets the position of a point t units along the line segment.
	 * Points of the line segment are defined for t in [0, 1]
	 * @param t
	 * @return
	 */
	public Vector3f getPoint(float t) {
		Vector3f point = new Vector3f();
		point.sub(mEndPoint, mStartPoint);
		point.scale(t);
		point.add(mEndPoint);
		return point;
	}
	
	
}
