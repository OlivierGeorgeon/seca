package gwt.seca.client.util;

import java.util.Iterator;

/**
 * An ArcGrid combines a data structure with a geometric structure.
 * By calling a constructor, only the resolutions of the grid is defined - This is the size of the data structure.
 * The real size - the size of the geometric object - still needs to be defined.
 * There are three ways to do that and so to define the set of angles delimiting the arcs of the grid:
 * setAngles(), setCentralAngleAndMagnitude() or setExtremeAngles().
 * The last two methods generate uniform sets of angles, taking into account the resolutions of the grid.
 * 
 * @author Olivier Voisin (ENSIETA 2011)
 *
 * @param <E> Type of element to store in the grid.
 */
public class ArcGrid<E> implements Iterable<E> {
	
	/**
	 * Construct an empty grid.
	 */
	public ArcGrid() {
		
	}
	/**
	 * Constructs an empty one-dimension grid with the specified resolution.
	 * @param resolution The arc resolution.
	 * @param radius Radius of the arcs.
	 */
	public ArcGrid(int resolution, float maxRadius) {
		this(resolution, 1, maxRadius);
	}
	/**
	 * Constructs an empty two-dimension grid with the specified resolutions.
	 * @param resolutionX The arc resolution along X.
	 * @param resolutionY The arc resolution along Y.
	 * @param radius Radius of the arcs.
	 */
	public ArcGrid(int resolutionX, int resolutionY, float maxRadius) {
		if (resolutionX<=0)
			throw new IllegalArgumentException("The resolution along X is illegal: "+resolutionX);
		if (resolutionY<=0)
			throw new IllegalArgumentException("The resolution along Y is illegal: "+resolutionY);
		init(resolutionX, resolutionY);
		setMaxRadius(maxRadius);
	}
	/**
	 * Constructs a grid with the elements of the array.
	 * @param matrix Array of elements to fill the new grid with.
	 * @param radius Radius of the arcs.
	 */
	public ArcGrid(E[] matrix, float maxRadius) {
		if (matrix==null)
			throw new IllegalArgumentException("The null array cannot be used to initialize an arc grid.");
		init(matrix.length, 1);
		fill(matrix);
		setMaxRadius(maxRadius);
	}
	/**
	 * Constructs a grid with the elements of the matrix.
	 * @param matrix Matrix of elements to fill the new grid with.
	 * @param radius Radius of the arcs.
	 */
	public ArcGrid(E[][] matrix, float maxRadius) {
		if (matrix==null)
			throw new IllegalArgumentException("The null matrix cannot be used to initialize an arc grid.");
		init(matrix.length, matrix[0].length);
		fill(matrix);
		setMaxRadius(maxRadius);
	}
	/**
	 * Constructs a copy of the specified grid.
	 * @param grid Grid that is cloned.
	 */
	@SuppressWarnings("unchecked")
	public ArcGrid(ArcGrid<E> grid) {
		if (grid==null)
			throw new IllegalArgumentException("A null grid cannot be used to initialize an arc grid.");
		copyDataStructure(grid);
		fill((E[][]) grid.mElements);
		copyGeometricStructure(grid);
	}
	
	/**
	 * Copies the data structure (but not the elements) of the specified grid.
	 * @param grid Grid from which the data structure is copied.
	 */
	@SuppressWarnings("rawtypes")
	public void copyDataStructure(ArcGrid grid) {
		if (grid==null)
			throw new IllegalArgumentException("A null grid cannot be used to copy the data structure.");
		init(grid.mResolutionX, grid.mResolutionY);
	}
	/**
	 * Copies the geometric structure of the specified grid.
	 * @param grid Grid from which the geometric structure is copied.
	 */
	@SuppressWarnings("rawtypes")
	public void copyGeometricStructure(ArcGrid grid) {
		if (grid==null)
			throw new IllegalArgumentException("A null grid cannot be used to copy the geometric structure.");
		if (grid.isUniform())
			setExtremeAngles(grid.mAnglesAlongX[0], grid.mAnglesAlongX[grid.mResolutionX+1], grid.mAnglesAlongY[0], grid.mAnglesAlongY[grid.mResolutionY+1]);
		else if (grid.mResolutionX==mResolutionX && grid.mResolutionY==mResolutionY)
			setAngles(grid.mAnglesAlongX, grid.mAnglesAlongY);
		else
			throw new IllegalArgumentException("Cannot copy the geometric structure of the specified grid.");
		setMaxRadius(grid.mMaxRadius);
	}
	
	/**
	 * Returns the element at the specified location,
	 * in the case of a one-dimension grid.
	 * @param i Index of the element.
	 * @return The element at the specified location.
	 */
	public E getElement(int i) {
		return getElement(i, 0);
	}
	/**
	 * Returns the element at the specified location,
	 * in the case of a two-dimension grid.
	 * @param i Index along X of the element.
	 * @param j Index along Y of the element.
	 * @return The element at the specified location.
	 */
	@SuppressWarnings("unchecked")
	public E getElement(int i, int j) {
		if (i<0 || i>=mResolutionX)
			throw new IllegalArgumentException("Arc index along X out of bounds: "+i);
		if (j<0 || j>=mResolutionY)
			throw new IllegalArgumentException("Arc index along Y out of bounds: "+j);
		return (E) mElements[i][j];
	}
	
	/**
	 * Returns the maximal angle of the arc corresponding to the specified element,
	 * in the case of a one-dimension grid.
	 * @param i Index of the element.
	 * @return The maximal angle in degrees.
	 */
	public float getMaxAngle(int i) {
		return getMaxAngleAlongX(i);
	}
	/**
	 * Returns the maximal angle along X of the arc corresponding to the specified element,
	 * in the case of a two-dimension grid.
	 * @param i Index along X of the element.
	 * @return The maximal angle in degrees.
	 */
	public float getMaxAngleAlongX(int i) {
		return mAnglesAlongX[i+1];
	}
	/**
	 * Returns the maximal angle along Y of the arc corresponding to the specified element,
	 * in the case of a two-dimension grid.
	 * @param j Index along Y of the element.
	 * @return The maximal angle in degrees.
	 */
	public float getMaxAngleAlongY(int j) {
		return mAnglesAlongY[j+1];
	}
	
	/**
	 * Returns the maximal radius of the arcs.
	 * @return The maximal radius of the arcs.
	 */
	public float getMaxRadius() {
		return mMaxRadius;
	}
	
	/**
	 * Returns the mean angle of the arc corresponding to the specified element,
	 * in the case of a one-dimension grid.
	 * @param i Index of the element.
	 * @return The mean angle in degrees.
	 */
	public float getMeanAngle(int i) {
		return getMeanAngleAlongX(i);
	}
	/**
	 * Returns the mean angle along X of the arc corresponding to the specified element,
	 * in the case of a two-dimension grid.
	 * @param i Index along X of the element.
	 * @return The mean angle in degrees.
	 */
	public float getMeanAngleAlongX(int i) {
		return (mAnglesAlongX[i]+mAnglesAlongX[i+1])/2;
	}
	/**
	 * Returns the mean angle along Y of the arc corresponding to the specified element,
	 * in the case of a two-dimension grid.
	 * @param j Index along Y of the element.
	 * @return The mean angle in degrees.
	 */
	public float getMeanAngleAlongY(int j) {
		return (mAnglesAlongY[j]+mAnglesAlongY[j+1])/2;
	}
	
	/**
	 * Returns the minimal angle of the arc corresponding to the specified element,
	 * in the case of a one-dimension grid.
	 * @param i Index of the element.
	 * @return The minimal angle in degrees.
	 */
	public float getMinAngle(int i) {
		return getMinAngleAlongX(i);
	}
	/**
	 * Returns the minimal angle along X of the arc corresponding to the specified element,
	 * in the case of a two-dimension grid.
	 * @param i Index along X of the element.
	 * @return The minimal angle in degrees.
	 */
	public float getMinAngleAlongX(int i) {
		return mAnglesAlongX[i];
	}
	/**
	 * Returns the minimal angle along Y of the arc corresponding to the specified element,
	 * in the case of a two-dimension grid.
	 * @param j Index along Y of the element.
	 * @return The minimal angle in degrees.
	 */
	public float getMinAngleAlongY(int j) {
		return mAnglesAlongY[j];
	}
	
	/**
	 * Returns the radius of the specified element, 
	 * in the case of a one-dimension grid.
	 * @param i Index of the element.
	 * @return The radius of the specified element.
	 */
	public float getRadius(int i) {
		return getRadius(i, 0);
	}
	/**
	 * Returns the radius of the specified element, 
	 * in the case of a two-dimension grid.
	 * @param i Index of the element along X.
	 * @param j Index of the element along Y.
	 * @return The radius of the specified element.
	 */
	public float getRadius(int i, int j) {
		if (mRadius==null)
			throw new IllegalArgumentException("The radius structure is not initialized.");
		if (i<0 || i>=mResolutionX)
			throw new IllegalArgumentException("Arc index along X out of bounds: "+i);
		if (j<0 || j>=mResolutionY)
			throw new IllegalArgumentException("Arc index along Y out of bounds: "+j);
		return mRadius[i][j];
	}
	
	/**
	 * Returns the resolution of the grid. 
	 * This is the multiplication of both the resolution along X and the resolution along Y.
	 * @return The resolution of the grid.
	 */
	public int getResolution() {
		return mResolutionX*mResolutionY;
	}
	/**
	 * Returns the resolution of the grid along X.
	 * @return The resolution of the grid along X.
	 */
	public int getResolutionAlongX() {
		return mResolutionX;
	}
	/**
	 * Returns the resolution of the grid along Y.
	 * @return The resolution of the grid along Y.
	 */
	public int getResolutionAlongY() {
		return mResolutionY;
	}
	
	/**
	 * Returns true if the grid is uniform along X and along Y.
	 * @return True if the grid is uniform along X and along Y.
	 */
	public boolean isUniform() {
		return mUniformAlongX && mUniformAlongY;
	}
	/**
	 * Returns true if the grid is uniform along X.
	 * @return True if the grid is uniform along X.
	 */
	public boolean isUniformAlongX() {
		return mUniformAlongX;
	}
	/**
	 * Returns true if the grid is uniform along Y.
	 * @return True if the grid is uniform along Y.
	 */
	public boolean isUniformAlongY() {
		return mUniformAlongY;
	}
	
	/**
	 * Returns a default iterator over the grid.
	 */
	@Override
	public Iterator<E> iterator() {
		return new ArcGridIterator<E>(this);
	}
	/**
	 * Returns a specialized iterator over the grid.
	 * @return
	 */
	public ArcGridIterator<E> iteratorOverGrid() {
		return new ArcGridIterator<E>(this);
	}
	/**
	 * Returns a specialized iterator over a ring.
	 * @return
	 */
	public ArcGridIterator<E> iteratorOverRing(int rowIdx) {
		if (rowIdx<0 || rowIdx>=mResolutionY)
			throw new IllegalArgumentException("Row index out of bounds: "+rowIdx);
		return new ArcGridIterator<E>(this, rowIdx);
	}
	
	/**
	 * Defines a specific set of angles delimiting the arcs, 
	 * in the case of a one-dimension grid.
	 * The length of the specified array must equals the resolution plus one.
	 * @param angles New set of angles.
	 */
	public void setAngles(float[] angles) {
		if (angles == null)
			throw new IllegalArgumentException("A null table cannot be used to initialize angles.");
		if (angles.length != mResolutionX+1)
			throw new IllegalArgumentException("The specified table has a wrong size: "+angles.length+" (Expected size: "+(mResolutionX+1)+")");
		setAnglesAlongX(angles);
	}
	/**
	 * Defines specific sets of angles delimiting the arcs, 
	 * in the case of a two-dimension grid.
	 * The lengths of the specified arrays must equals respectively the resolution along X plus one and the resolution along Y plus one.
	 * @param anglesAlongX New set of angles along X.
	 * @param anglesAlongY New set of angles along Y.
	 */
	public void setAngles(float[] anglesAlongX, float[] anglesAlongY) {
		if (anglesAlongX == null)
			throw new IllegalArgumentException("A null table cannot be used to initialize angles along X.");
		if (anglesAlongX.length != mResolutionX+1)
			throw new IllegalArgumentException("The specified table for angles along X has a wrong size: "+anglesAlongX.length+" (Expected size: "+(mResolutionX+1)+")");
		if (anglesAlongY == null)
			throw new IllegalArgumentException("A null table cannot be used to initialize angles along Y.");
		if (anglesAlongY.length != mResolutionY+1)
			throw new IllegalArgumentException("The specified table for angles along Y has a wrong size: "+anglesAlongY.length+" (Expected size: "+(mResolutionY+1)+")");
		setAnglesAlongX(anglesAlongX);
		setAnglesAlongY(anglesAlongY);
	}
	
	/**
	 * Defines the set of angles delimiting the arcs from the central angle and the magnitude,
	 * in the case of a one-dimension grid.
	 * @param centralAngle Angle of the central point of the grid.
	 * @param magnitude Angle width of the grid.
	 */
	public void setCentralAngleAndMagnitude(float centralAngle, float magnitude) {
		computeAnglesAlongX(centralAngle-magnitude/2, centralAngle+magnitude/2);
	}
	/**
	 * Defines the sets of angles delimiting the arcs from the central angles and the magnitudes,
	 * in the case of a two-dimension grid.
	 * @param centralAngleX Angle of the central point of the grid along X.
	 * @param magnitudeX Angle width of the grid along X.
	 * @param centralAngleY Angle of the central point of the grid along Y.
	 * @param magnitudeY Angle width of the grid along Y.
	 */
	public void setCentralAnglesAndMagnitudes(float centralAngleX, float magnitudeX, float centralAngleY, float magnitudeY) {
		computeAnglesAlongX(centralAngleX-magnitudeX/2, centralAngleX+magnitudeX/2);
		computeAnglesAlongY(centralAngleY-magnitudeY/2, centralAngleY+magnitudeY/2);
	}
	
	/**
	 * Replaces the element at the specified position in this grid with the specified element,
	 * in the case of a one-dimension grid.
	 * @param i Index of the element to replace.
	 * @param element Element to replace with.
	 */
	public void setElement(int i, E element) {
		setElement(i, 0, element);
	}
	/**
	 * Replaces the element at the specified position in this grid with the specified element,
	 * in the case of a two-dimension grid.
	 * @param i Index along X of the element to replace.
	 * @param j Index along Y of the element to replace.
	 * @param element Element to replace with.
	 */
	public void setElement(int i, int j, E element) {
		if (i<0 || i>=mResolutionX)
			throw new IllegalArgumentException("Arc index along X out of bounds: "+i);
		if (j<0 || j>=mResolutionY)
			throw new IllegalArgumentException("Arc index along Y out of bounds: "+j);
		mElements[i][j] = element;
	}
	
	/**
	 * Defines the set of angles delimiting the arcs from the extreme angles,
	 * in the case of a one-dimension grid.
	 * @param minAngle Minimal angle of the grid.
	 * @param maxAngle Maximal angle of the grid.
	 */
	public void setExtremeAngles(float minAngle, float maxAngle) {
		computeAnglesAlongX(minAngle, maxAngle);
	}
	public void setExtremeAngles(float minAngleX, float maxAngleX, float minAngleY, float maxAngleY) {
		computeAnglesAlongX(minAngleX, maxAngleX);
		computeAnglesAlongY(minAngleY, maxAngleY);
	}
	
	/**
	 * Defines the maximal radius of the arcs.
	 * If the specified value is strictly positive, an array is allocated to save a radius for each element of the grid.
	 * @param maxRadius Maximal radius of the arcs.
	 */
	public void setMaxRadius(float maxRadius) {
		mMaxRadius = maxRadius;
		if (maxRadius>0)
			mRadius = new float[mResolutionX][mResolutionY];
		else
			mRadius = null;
	}
	
	/**
	 * Define the radius of the specified element, 
	 * in the case of a one-dimension grid.
	 * @param i Index of the element.
	 * @param radius New radius of the element.
	 */
	public void setRadius(int i, int radius) {
		setRadius(i, 0);
	}
	/**
	 * Defines the radius of the specified element, 
	 * in the case of a two-dimension grid.
	 * @param i Index of the element along X.
	 * @param j Index of the element along Y.
	 * @param radius New radius of the element.
	 */
	public void setRadius(int i, int j, int radius) {
		if (mRadius==null)
			throw new IllegalArgumentException("The radius structure is not initialized.");
		if (i<0 || i>=mResolutionX)
			throw new IllegalArgumentException("Arc index along X out of bounds: "+i);
		if (j<0 || j>=mResolutionY)
			throw new IllegalArgumentException("Arc index along Y out of bounds: "+j);
		mRadius[i][j] = radius;
	}
	
	/**
	 * Computes the set of angles delimiting the arcs along X.
	 * @param minAngle The minimal angle of the grid along X.
	 * @param maxAngle The maximal angle of the grid along X.
	 */
	private void computeAnglesAlongX(float minAngle, float maxAngle) {
		mUniformAlongX = true;
		float stepAngle = (maxAngle-minAngle)/(mResolutionX+1f);
		for(int i=0; i<=mResolutionX; i++) {
			mAnglesAlongX[i] = minAngle + i*stepAngle;
		}
	}
	/**
	 * Computes the set of angles delimiting the arcs along Y.
	 * @param minAngle The minimal angle of the grid along Y.
	 * @param maxAngle The maximal angle of the grid along Y.
	 */
	private void computeAnglesAlongY(float minAngle, float maxAngle) {
		mUniformAlongY = true;
		float stepAngle = (maxAngle-minAngle)/(mResolutionY+1f);
		for(int i=0; i<=mResolutionY; i++) {
			mAnglesAlongY[i] = minAngle + i*stepAngle;
		}
	}
	
	/**
	 * Initializes the data structure with the given size.
	 * @param resolutionX Size of the data structure along X.
	 * @param resolutionY Size of the data structure along Y.
	 */
	private void init(int resolutionX, int resolutionY) {
		mElements = new Object[resolutionX][resolutionY];
		mAnglesAlongX = new float[resolutionX+1];
		mAnglesAlongY = new float[resolutionY+1];
	}
	
	private void fill(E[][] matrix) {
		assert(matrix.length==mResolutionX && matrix[0].length==mResolutionY):"The size of the matrix is illegal.";
		for(int i=0; i<mResolutionX; i++)
			for(int j=0; j<mResolutionY; j++)
				setElement(i, j, matrix[i][j]);
	}
	
	private void fill(E[] array) {
		assert(array.length==mResolutionX):"The size of the array is illegal.";
		for(int i=0; i<mResolutionX; i++)
			setElement(i, 0, array[i]);
	}
	
	/**
	 * Replaces the array of angles along X with the specified array of angles.
	 * The length of the specified array must equals the resolution along X plus one.
	 * @param angles Array of angles to delimit the arcs along X.
	 */
	private void setAnglesAlongX(float[] angles) {
		float stepAngle = 0;
		mUniformAlongX = true;
		for(int i=0; i<=mResolutionX; i++) {
			mAnglesAlongX[i] = angles[i];
			if (i==1)
				stepAngle = angles[1]-angles[0];
			if (i>1 && angles[i]-angles[i-1]!=stepAngle)
				mUniformAlongX = false;
		}
	}
	/**
	 * Replaces the array of angles along Y with the specified array of angles.
	 * The size of the specified array must equals the resolution along Y plus one.
	 * @param angles Array of angles to delimit the arcs along Y.
	 */
	private void setAnglesAlongY(float[] angles) {
		float stepAngle = 0;
		mUniformAlongY = true;
		for(int i=0; i<=mResolutionY; i++) {
			mAnglesAlongY[i] = angles[i];
			if (i==1)
				stepAngle = angles[1]-angles[0];
			if (i>1 && angles[i]-angles[i-1]!=stepAngle)
				mUniformAlongY = false;
		}
	}
	
	/** Structure that contains the elements.*/
	private Object[][] mElements;
	/** Structure that contains a radius for each element of the grid.*/
	private float[][] mRadius;
	/** List of angles to delimit the arcs along X. The arcs being contiguous, the size of this list if the resolution along X plus one.*/
	private float[] mAnglesAlongX;
	/** List of angles to delimit the arcs along Y. The arcs being contiguous, the size of this list if the resolution along Y plus one.*/
	private float[] mAnglesAlongY;
	/** Resolution of the grid along X.*/
	private int mResolutionX;
	/** Resolution of the grid along Y.*/
	private int mResolutionY;
	/** True if angles along X are uniform.*/
	private boolean mUniformAlongX;
	/** True if angles along Y are uniform.*/
	private boolean mUniformAlongY;
	/** Radius of arcs.*/
	private float mMaxRadius;

}
