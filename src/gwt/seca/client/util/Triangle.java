package gwt.seca.client.util;

import javax.vecmath.Vector3f;

/**
 * Defines a Triangle in 3D space
 * @author stagiaire
 *
 */
public class Triangle {
	
	private Vector3f mVerticeA;
	private Vector3f mVerticeB;
	private Vector3f mVerticeC;
	
	/**
	 * Default constructor - sets everything to 0. 
	 */
	public Triangle() {
		mVerticeA = new Vector3f();
		mVerticeB = new Vector3f();
		mVerticeC = new Vector3f();
	}
	
	public Triangle(Vector3f verticeA, Vector3f verticeB, Vector3f verticeC) {
		this();
		this.setVerticeA(verticeA);
		this.setVerticeB(verticeB);
		this.setVerticeC(verticeC);
	}
	
	public Vector3f getVerticeA() {
		return new Vector3f(mVerticeA);
	}
	public void setVerticeA(Vector3f vertice) {
		mVerticeA.set(vertice);
	}
	public Vector3f getVerticeB() {
		return new Vector3f(mVerticeB);
	}
	public void setVerticeB(Vector3f vertice) {
		mVerticeB.set(vertice);
	}
	public Vector3f getVerticeC() {
		return new Vector3f(mVerticeC);
	}
	public void setVerticeC(Vector3f vertice) {
		mVerticeC.set(vertice);
	}

}
