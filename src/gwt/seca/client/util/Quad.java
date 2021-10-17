package gwt.seca.client.util;

import javax.vecmath.Vector3f;

/**
 * Defines a Quad in 3D space
 * @author stagiaire
 *
 */
public class Quad {
	
	private Vector3f mVerticeA;
	private Vector3f mVerticeB;
	private Vector3f mVerticeC;
	private Vector3f mVerticeD;
	
	/**
	 * Default constructor - sets everything to 0. 
	 */
	public Quad() {
		mVerticeA = new Vector3f();
		mVerticeB = new Vector3f();
		mVerticeC = new Vector3f();
		mVerticeD = new Vector3f();
	}
	
	public Quad(Vector3f verticeA, Vector3f verticeB, Vector3f verticeC, Vector3f verticeD) {
		this();
		this.setVerticeA(verticeA);
		this.setVerticeB(verticeB);
		this.setVerticeC(verticeC);
		this.setVerticeD(verticeD);
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
	public Vector3f getVerticeD() {
		return new Vector3f(mVerticeD);
	}
	public void setVerticeD(Vector3f vertice) {
		mVerticeD.set(vertice);
	}
	public Pair<Triangle, Triangle> getTriangles() {
		return Pair.create(new Triangle(mVerticeA, mVerticeB, mVerticeC), new Triangle(mVerticeA, mVerticeC, mVerticeD));
	}
	public void translate(Vector3f trans) {
		mVerticeA.add(trans);
		mVerticeB.add(trans);
		mVerticeC.add(trans);
		mVerticeD.add(trans);
	}
	public void translate(float x, float y, float z) {
		translate(new Vector3f(x, y, z));
	}

}
