package gwt.seca.client.util;


import gwt.seca.client.util.SecaMath.CardinalDirection;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 * A 3D box aligned with the x/y/z axes.
 * @author stagiaire
 * Adapted from Ogre3D: http://www.ogre3d.org/docs/api/html/classOgre_1_1AxisAlignedBox.html
 */
public class AxisAlignedBox {

	enum Extent
	{
		EXTENT_NULL,
		EXTENT_FINITE,
		EXTENT_INFINITE
	};
	enum Corner {
	}
	/* Ogre3D view:
	   1-----2 
	  /|    /|
	 / |   / |
	5-----4  |
	|  0--|--3
	| /   | /
	|/    |/
	6-----7
	
	   Y
	   |
	   |
	   O---X
	  /
	 Z
	 */
	public final static int FAR_LEFT_BOTTOM = 0;
	public final static int FAR_LEFT_TOP = 1;
	public final static int FAR_RIGHT_TOP = 2;
	public final static int FAR_RIGHT_BOTTOM = 3;
	public final static int NEAR_RIGHT_TOP = 4;
	public final static int NEAR_LEFT_TOP = 5;
	public final static int NEAR_LEFT_BOTTOM = 6;
	public final static int NEAR_RIGHT_BOTTOM = 7;
	public final static int MINIMUM = 0;
	public final static int MAXIMUM = 4;
	
	/**
	 * Standard constructor - creates a unit box around the origin.
	 */
	public AxisAlignedBox() {
		this(Extent.EXTENT_FINITE);
	}
	public AxisAlignedBox(Extent extent) {
		this(-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f);
		mExtent = extent;
	}
	/**
	 * Copy constructor.
	 */
	public AxisAlignedBox(AxisAlignedBox box) {
		if (box.isNull())
			setNull();
		else if (box.isInfinite())
			setInfinite();
		else
			setExtents( box.mMinimum, box.mMaximum );
	}
	public AxisAlignedBox(Vector3f minimum, Vector3f maximum) {
		setExtents(minimum, maximum);
	}
	public AxisAlignedBox(float mx, float my, float mz, float Mx, float My, float Mz) {
		setExtents(mx, my, mz, Mx, My, Mz);
	}
	
	public void setNull() {
		mExtent = Extent.EXTENT_NULL;
	}
	public boolean isNull() {
		return (mExtent == Extent.EXTENT_NULL);
	}
	public boolean isFinite() {
		return (mExtent == Extent.EXTENT_FINITE);
	}
	public void setInfinite() {
		mExtent = Extent.EXTENT_INFINITE;
	}
	public boolean isInfinite() {
		return (mExtent == Extent.EXTENT_INFINITE);
	}
	
	boolean setExtents( Vector3f min, Vector3f max )
	{
//		assert( (min.x <= max.x && min.y <= max.y && min.z <= max.z) &&
//				"The minimum corner of the box must be less than or equal to maximum corner" );
		if (!(min.x <= max.x && min.y <= max.y && min.z <= max.z)) {
			return false;
		}
		mExtent = Extent.EXTENT_FINITE;
		mMinimum.set(min);
		mMaximum.set(max);
		return true;
	}
	boolean setExtents(float mx, float my, float mz, float Mx, float My, float Mz ) {
//		assert( (mx <= Mx && my <= My && mz <= Mz) &&
//		"The minimum corner of the box must be less than or equal to maximum corner" );
		if (!(mx <= Mx && my <= My && mz <= Mz)) {
			return false;
		}
		mExtent = Extent.EXTENT_FINITE;
		mMinimum.x = mx;
		mMinimum.y = my;
		mMinimum.z = mz;
		mMaximum.x = Mx;
		mMaximum.y = My;
		mMaximum.z = Mz;
		return true;
	}

	public void setMinimum(Vector3f minimum) {
		mMinimum.set(minimum);
	}
	public void setMinimum(float x, float y, float z) {
		mMinimum.set(x, y, z);
	}
	public void setMinimumX(float x) {
		mMinimum.setX(x);
	}
	public void setMinimumY(float y) {
		mMinimum.setY(y);
	}
	public void setMinimumZ(float z) {
		mMinimum.setZ(z);
	}
	
	public void setMaximum(Vector3f maximum) {
		mMaximum.set(maximum);
	}
	public void setMaximum(float x, float y, float z) {
		mMaximum.set(x, y, z);
	}
	public void setMaximumX(float x) {
		mMaximum.setX(x);
	}
	public void setMaximumY(float y) {
		mMaximum.setY(y);
	}
	public void setMaximumZ(float z) {
		mMaximum.setZ(z);
	}
	
	public Vector3f[] getAllCorners() {
		Vector3f[] corners = new Vector3f[8];
		corners[0].set(mMinimum);
		corners[1].set(mMinimum.x, mMaximum.y, mMinimum.z);
		corners[2].set(mMaximum.x, mMaximum.y, mMinimum.z);
		corners[3].set(mMaximum.x, mMinimum.y, mMinimum.z);
		corners[4].set(mMaximum);
		corners[5].set(mMinimum.x, mMaximum.y, mMaximum.z);
		corners[6].set(mMinimum.x, mMinimum.y, mMaximum.z);
		corners[7].set(mMaximum.x, mMinimum.y, mMaximum.z);
		return corners;
	}
	
	public Vector3f getCorner(int cornerToGet) {
		switch(cornerToGet)
		{
		case FAR_LEFT_BOTTOM:
			return mMinimum;
		case FAR_LEFT_TOP:
			return new Vector3f(mMinimum.x, mMaximum.y, mMinimum.z);
		case FAR_RIGHT_TOP:
			return new Vector3f(mMaximum.x, mMaximum.y, mMinimum.z);
		case FAR_RIGHT_BOTTOM:
			return new Vector3f(mMaximum.x, mMinimum.y, mMinimum.z);
		case NEAR_RIGHT_BOTTOM:
			return new Vector3f(mMaximum.x, mMinimum.y, mMaximum.z);
		case NEAR_LEFT_BOTTOM:
			return new Vector3f(mMinimum.x, mMinimum.y, mMaximum.z);
		case NEAR_LEFT_TOP:
			return new Vector3f(mMinimum.x, mMaximum.y, mMaximum.z);
		case NEAR_RIGHT_TOP:
			return mMaximum;
		default:
			return new Vector3f();
		}
	}
	/**
	 * Return a quad representing the face given in parameter.
	 * @param face
	 * @return
	 */
	public Quad getFace(CardinalDirection faceDirection) {
		/* Seca view:
		   5-----4
		  /|    /|
		 / |   / |
		6-----7  |
		|  1--|--2
		| /   | /
		|/    |/
		0-----3
		
		Z
		|  Y
		| /
		|/
		O----X

		 */
		switch(faceDirection) {
		case Up:
			return new Quad(getCorner(NEAR_RIGHT_BOTTOM), getCorner(MAXIMUM), getCorner(NEAR_LEFT_TOP), getCorner(NEAR_LEFT_BOTTOM));
		case Down:
			return new Quad(getCorner(FAR_RIGHT_BOTTOM), getCorner(MINIMUM), getCorner(FAR_LEFT_TOP), getCorner(FAR_RIGHT_TOP));
		case North:
			return new Quad(getCorner(NEAR_LEFT_TOP), getCorner(MAXIMUM), getCorner(FAR_RIGHT_TOP), getCorner(FAR_LEFT_TOP));
		case South:
			return new Quad(getCorner(NEAR_LEFT_BOTTOM), getCorner(MINIMUM), getCorner(FAR_RIGHT_BOTTOM), getCorner(NEAR_RIGHT_BOTTOM));
		case East:
			return new Quad(getCorner(NEAR_RIGHT_BOTTOM), getCorner(MAXIMUM), getCorner(FAR_RIGHT_TOP), getCorner(FAR_RIGHT_BOTTOM));
		case West:
			return new Quad(getCorner(FAR_LEFT_TOP), getCorner(MINIMUM), getCorner(NEAR_LEFT_BOTTOM), getCorner(NEAR_LEFT_TOP));
		default:
			return null;
		}	
	}
	/**
	 * Return a quad representing the face given in parameter. This quad is slightly translated away from the box.
	 * @param face
	 * @return
	 */
	public Quad getExtractedFace(CardinalDirection faceDirection) {
		Quad quad = getFace(faceDirection);
		switch(faceDirection) {
		case Up:
			quad.translate(0, 0, .01f);
			break;
		case Down:
			quad.translate(0, 0, -.01f);
			break;
		case North:
			quad.translate(0, .01f, 0);
			break;
		case South:
			quad.translate(0, -.01f, 0);
			break;
		case East:
			quad.translate(.01f, 0, 0);
			break;
		case West:
			quad.translate(-.01f, 0, 0);
			break;
		}
		return quad;
	}

	public Vector3f getCenter() {
		if (mExtent != Extent.EXTENT_FINITE) {
			return null;
		}
		return new Vector3f(
				(mMaximum.x + mMinimum.x) * 0.5f,
				(mMaximum.y + mMinimum.y) * 0.5f,
				(mMaximum.z + mMinimum.z) * 0.5f);
	}
	public Vector3f getSize() {
		if (mExtent == Extent.EXTENT_NULL) {
			return new Vector3f();
		} else if (mExtent == Extent.EXTENT_FINITE) {
			Vector3f size = new Vector3f();
			size.scaleAdd(-1, mMinimum, mMaximum);
			return size;
		} else {
			return new Vector3f(
					Float.POSITIVE_INFINITY,
					Float.POSITIVE_INFINITY,
					Float.POSITIVE_INFINITY);
		}
	}
	public Vector3f getHalfSize() {
		if (mExtent == Extent.EXTENT_NULL) {
			return new Vector3f();
		} else if (mExtent == Extent.EXTENT_FINITE) {
			Vector3f size = new Vector3f();
			size.scaleAdd(-1, mMinimum, mMaximum);
			size.scale(0.5f);
			return size;
		} else {
			return new Vector3f(
					Float.POSITIVE_INFINITY,
					Float.POSITIVE_INFINITY,
					Float.POSITIVE_INFINITY);
		}
	}

	public float getVolume() {
		if (mExtent == Extent.EXTENT_NULL) {
			return 0.0f;
		} else if (mExtent == Extent.EXTENT_FINITE) {
			Vector3f diff = new Vector3f();
			diff.scaleAdd(-1, mMinimum, mMaximum);
			return diff.x * diff.y * diff.z;
		} else {
			return Float.POSITIVE_INFINITY;
		}
	}

	public boolean contains(Vector3f v) {
		if (isNull())
			return false;
		if (isInfinite())
			return true;

		return mMinimum.x <= v.x && v.x <= mMaximum.x &&
		mMinimum.y <= v.y && v.y <= mMaximum.y &&
		mMinimum.z <= v.z && v.z <= mMaximum.z;
	}

	public boolean contains(AxisAlignedBox box) {
		if (box.isNull() || this.isInfinite())
			return true;

		if (this.isNull() || box.isInfinite())
			return false;

		return mMinimum.x <= box.mMinimum.x &&
		mMinimum.y <= box.mMinimum.y &&
		mMinimum.z <= box.mMinimum.z &&
		box.mMaximum.x <= mMaximum.x &&
		box.mMaximum.y <= mMaximum.y &&
		box.mMaximum.z <= mMaximum.z;
	}
	
	public void merge(AxisAlignedBox box) {
		// Do nothing if box null, or this is infinite
		if ((box.mExtent == Extent.EXTENT_NULL) || (mExtent == Extent.EXTENT_INFINITE)) {
			return;
		}
		// Otherwise if box is infinite, make this infinite, too
		else if (box.mExtent == Extent.EXTENT_INFINITE) {
			mExtent = Extent.EXTENT_INFINITE;
		}
		// Otherwise if current null, just take box
		else if (mExtent == Extent.EXTENT_NULL) {
			setExtents(box.mMinimum, box.mMaximum);
		}
		// Otherwise merge
		else {
			mMinimum.setX(Math.min(mMinimum.x, box.mMinimum.x));
			mMinimum.setY(Math.min(mMinimum.y, box.mMinimum.y));
			mMinimum.setZ(Math.min(mMinimum.z, box.mMinimum.z));
			mMaximum.setX(Math.max(mMaximum.x, box.mMaximum.x));
			mMaximum.setY(Math.max(mMaximum.y, box.mMaximum.y));
			mMaximum.setZ(Math.max(mMaximum.z, box.mMaximum.z));
		}

	}

	public void merge(Vector3f point)
	{
		if (mExtent == Extent.EXTENT_NULL) { // if null, use this point
			setExtents(point, point);
			return;
		} else if (mExtent == Extent.EXTENT_FINITE) {
			mMinimum.setX(Math.min(mMinimum.x, point.x));
			mMinimum.setY(Math.min(mMinimum.y, point.y));
			mMinimum.setZ(Math.min(mMinimum.z, point.z));
			mMaximum.setX(Math.max(mMaximum.x, point.x));
			mMaximum.setY(Math.max(mMaximum.y, point.y));
			mMaximum.setZ(Math.max(mMaximum.z, point.z));
			return;
		} else { // if infinite, makes no difference
			return;
		}
	}
	
	public void transform(Matrix4f matrix)
	{
		// Do nothing if current null or infinite
		if( mExtent != Extent.EXTENT_FINITE )
			return;

		Vector3f oldMin, oldMax, currentCorner, tmpCorner;

		// Getting the old values so that we can use the existing merge method.
		oldMin = new Vector3f(mMinimum);
		oldMax = new Vector3f(mMaximum);

		// reset
		setNull();

		// We sequentially compute the corners in the following order :
		// 0, 6, 5, 1, 2, 4 ,7 , 3
		// This sequence allows us to only change one member at a time to get at all corners.

		// For each one, we transform it using the matrix
		// Which gives the resulting point and merge the resulting point.

		// First corner 
		// min min min
		currentCorner = new Vector3f(oldMin);
		tmpCorner = new Vector3f();
		matrix.transform(currentCorner, tmpCorner);
		merge(tmpCorner);

		// min,min,max
		currentCorner.z = oldMax.z;
		matrix.transform(currentCorner, tmpCorner);
		merge(tmpCorner);

		// min max max
		currentCorner.y = oldMax.y;
		matrix.transform(currentCorner, tmpCorner);
		merge(tmpCorner);

		// min max min
		currentCorner.z = oldMin.z;
		matrix.transform(currentCorner, tmpCorner);
		merge(tmpCorner);

		// max max min
		currentCorner.x = oldMax.x;
		matrix.transform(currentCorner, tmpCorner);
		merge(tmpCorner);

		// max max max
		currentCorner.z = oldMax.z;
		matrix.transform(currentCorner, tmpCorner);
		merge(tmpCorner);

		// max min max
		currentCorner.y = oldMin.y;
		matrix.transform(currentCorner, tmpCorner);
		merge(tmpCorner);

		// max min min
		currentCorner.z = oldMin.z;
		matrix.transform(currentCorner, tmpCorner);
		merge(tmpCorner);
	}
	
	public void transformAffine(Matrix4f m) {
//		assert(m.isAffine());

		// Do nothing if current null or infinite
		if ( mExtent != Extent.EXTENT_FINITE )
			return;

		Vector3f centre = getCenter();
		Vector3f halfSize = getHalfSize();

		m.transform(centre);
		Vector3f newHalfSize = new Vector3f(
				Math.abs(m.m00) * halfSize.x + Math.abs(m.m01) * halfSize.y + Math.abs(m.m02) * halfSize.z, 
				Math.abs(m.m10) * halfSize.x + Math.abs(m.m11) * halfSize.y + Math.abs(m.m12) * halfSize.z,
				Math.abs(m.m20) * halfSize.x + Math.abs(m.m21) * halfSize.y + Math.abs(m.m22) * halfSize.z);
		
		Vector3f min = new Vector3f();
		min.scaleAdd(-1, newHalfSize, centre);
		Vector3f max = new Vector3f();
		max.add(newHalfSize, centre);
		setExtents(min, max);
	}
	
	public void translate(Vector3f v) {
		mMinimum.add(v);
		mMaximum.add(v);
	}
	
	public boolean intersects(AxisAlignedBox box) {
		// Early-fail for nulls
		if (this.isNull() || box.isNull())
			return false;

		// Early-success for infinites
		if (this.isInfinite() || box.isInfinite())
			return true;

		// Use up to 6 separating planes
		if (mMaximum.x < box.mMinimum.x)
			return false;
		if (mMaximum.y < box.mMinimum.y)
			return false;
		if (mMaximum.z < box.mMinimum.z)
			return false;

		if (mMinimum.x > box.mMaximum.x)
			return false;
		if (mMinimum.y > box.mMaximum.y)
			return false;
		if (mMinimum.z > box.mMaximum.z)
			return false;

		// otherwise, must be intersecting
		return true;

	}

	public AxisAlignedBox intersection(AxisAlignedBox box) {
		if (this.isNull() || box.isNull())
		{
			return new AxisAlignedBox(Extent.EXTENT_NULL);
		}
		else if (this.isInfinite())
		{
			return box;
		}
		else if (box.isInfinite())
		{
			return this;
		}

		Vector3f intMin = new Vector3f();
		Vector3f intMax = new Vector3f();
		
		intMin.setX(Math.max(mMinimum.x, box.mMinimum.x));
		intMin.setY(Math.max(mMinimum.y, box.mMinimum.y));
		intMin.setZ(Math.max(mMinimum.z, box.mMinimum.z));
		intMax.setX(Math.min(mMaximum.x, box.mMaximum.x));
		intMax.setY(Math.min(mMaximum.y, box.mMaximum.y));
		intMax.setZ(Math.min(mMaximum.z, box.mMaximum.z));

		// Check intersection isn't null
		if (intMin.x < intMax.x &&
				intMin.y < intMax.y &&
				intMin.z < intMax.z)
		{
			return new AxisAlignedBox(intMin, intMax);
		}

		return new AxisAlignedBox(Extent.EXTENT_NULL);
	}
	
	public boolean intersects(Sphere sphere)
	{
		return SecaMath.intersect(sphere, this); 
	}
	public boolean intersects(Plane plane)
	{
		return SecaMath.intersect(plane, this);
	}
	public boolean intersects(Vector3f vector)
	{
		if (mExtent == Extent.EXTENT_NULL) {
			return false;
		} else if (mExtent == Extent.EXTENT_FINITE) {
			return(vector.x >= mMinimum.x  &&  vector.x <= mMaximum.x  && 
					vector.y >= mMinimum.y  &&  vector.y <= mMaximum.y  && 
					vector.z >= mMinimum.z  &&  vector.z <= mMaximum.z);
		} else { // mExtent == Extent.EXTENT_INFINITE
			return true;
		}
	}

	protected Vector3f mMinimum = new Vector3f();
	protected Vector3f mMaximum = new Vector3f();
	protected Extent mExtent;
}
