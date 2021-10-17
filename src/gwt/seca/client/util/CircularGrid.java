package gwt.seca.client.util;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * An CircularGrid combines a data structure with a geometric structure.
 * Sectors are clockwise.
 * Ring 1: 012
 *         7 3
 *         654
 * @author Olivier Voisin (ENSIETA 2011)
 *
 * @param <E> Type of element to store in the grid.
 */
public class CircularGrid<E> implements Iterable<E> {

	/**
	 * Constructs an empty grid.
	 */
	public CircularGrid() {
		this(0, false, false);
	}
	/**
	 * Constructs an empty grid with the specified number of rings and the specified cuts.
	 * @param ringCount
	 * @param verticalCut
	 * @param horizontalCut
	 */
	public CircularGrid(int ringCount, boolean verticalCut, boolean horizontalCut) {
		if (ringCount<0)
			throw new IllegalArgumentException("The number of rings is illegal: "+ringCount);
		int columnCount = 2*(ringCount-1) + ((verticalCut)?2:1);
		int rowCount = 2*(ringCount-1) + ((horizontalCut)?2:1);
		init(columnCount, rowCount);
	}
	/**
	 * Constructs a grid with the elements of the matrix.
	 * @param matrix
	 */
	public CircularGrid(E[][] matrix) {
		if (matrix==null)
			throw new IllegalArgumentException("The null matrix cannot be used to initialize a circular grid.");
		if (Math.abs(matrix.length-matrix[0].length) > 1)
			throw new IllegalArgumentException("The size of the matrix is illegal: "+matrix.length+"x"+matrix[0].length);
		init(matrix.length, matrix[0].length);
		fill(matrix);
	}
	/**
	 * Constructs a copy of the specified grid.
	 * @param grid
	 */
	public CircularGrid(CircularGrid<E> grid) {
		init(grid.mColumnCount, grid.mRowCount);
		fill(grid.mElements);
	}

	/**
	 * Copies the structure (but not the elements) of the specified grid.
	 * @param grid
	 */
	@SuppressWarnings("rawtypes")
	public void copyStructure(CircularGrid grid) {
		init(grid.mColumnCount, grid.mRowCount);
	}
	
	/**
	 * Returns the element saved in the sector 'sectorIdx' of the ring 'ringIdx'.
	 * Sectors are clockwise and the sector '0' is at the top-left (-45° from the top) of the ring.
	 * @param ringIdx In [0, numberOfRings-1]
	 * @param sectorIdx In [0, numberOfSectors-1]
	 * @return
	 */
	public E get(int ringIdx, int sectorIdx) {
		if (sectorIdx<0 || sectorIdx>=getRingLength(ringIdx))
			throw new IllegalArgumentException("Sector index out of bounds: "+sectorIdx);
		Pair<Integer, Integer> idx = gridToMatrix(ringIdx, sectorIdx);
		return getFromArray(idx.mLeft, idx.mRight);
	}
	/**
	 * Returns the element at the coordinates (i, j) of the matrix representation of the grid.
	 * This method has been made to be used by circular grid iterators. 
	 * Indeed, it is faster to iterate over the elements than the method 'get' 
	 * but it implies some knowledge on the structure of the grid.
	 * @param i
	 * @param j
	 * @return
	 */
	public E getFromMatrix(int i, int j) {
		return getFromArray(i, j);
	}
	/**
	 * Returns a matrix representation of the grid.
	 * @return
	 */
	public E[][] getMatrix() {
		@SuppressWarnings("unchecked")
		E[][] matrix = (E[][]) new Object[mColumnCount][mRowCount];
		for(int i=0; i<mColumnCount; i++)
			for(int j=0; j<mRowCount; j++)
				matrix[i][j] = getFromArray(i, j);
		return matrix;
	}
	/**
	 * Returns the size of the matrix in a Pair object.
	 * @return A Pair that contains the number of columns and the number of rows.
	 */
	public Pair<Integer, Integer> getMatrixSize() {
		return Pair.create(mColumnCount, mRowCount);
	}
	
	/**
	 * Returns the maximal angle of the sector 'sectorIdx' in the ring 'ringIdx'. 
	 * Angles are clockwise.
	 * @param ringIdx In [0, numberOfRings-1]
	 * @param sectorIdx In [0, numberOfSectors-1]
	 * @return The maximal angle in degrees.
	 */
	public float getMaxAngle(int ringIdx, int sectorIdx) {
		if (sectorIdx<0 || sectorIdx>=getRingLength(ringIdx))
			throw new IllegalArgumentException("Sector index out of bounds: "+sectorIdx);
		return (360*(sectorIdx+.5f) / (float)getRingLength(ringIdx) - 45);
	}
	/**
	 * Returns the maximal radius of the ring 'ringIdx'.
	 * @param ringIdx In [0, numberOfRings-1]
	 * @return The maximal radius in [0, 1].
	 */
	public float getMaxRadius(int ringIdx) {
		if (ringIdx<0 || ringIdx>=mRingCount)
			throw new IllegalArgumentException("Ring index out of bounds: "+ringIdx);
		return ((ringIdx+1) / (float)mRingCount);
	}
	/**
	 * Returns the mean angle of the sector 'sectorIdx' in the ring 'ringIdx'. 
	 * Angles are clockwise.
	 * @param ringIdx In [0, numberOfRings-1]
	 * @param sectorIdx In [0, numberOfSectors-1]
	 * @return The mean angle in degrees.
	 */
	public float getMeanAngle(int ringIdx, int sectorIdx) {
		if (sectorIdx<0 || sectorIdx>=getRingLength(ringIdx))
			throw new IllegalArgumentException("Sector index out of bounds: "+sectorIdx);
		return (360*sectorIdx / (float)getRingLength(ringIdx) - 45);
	}
	/**
	 * Returns the mean radius of the ring 'ringIdx'.
	 * @param ringIdx In [0, numberOfRings-1]
	 * @return The mean radius in [0, 1].
	 */
	public float getMeanRadius(int ringIdx) {
		if (ringIdx<0 || ringIdx>=mRingCount)
			throw new IllegalArgumentException("Ring index out of bounds: "+ringIdx);
		return ((ringIdx+.5f) / (float)mRingCount);
	}
	/**
	 * Returns the minimal angle of the sector 'sectorIdx' in the ring 'ringIdx'. 
	 * Angles are clockwise.
	 * @param ringIdx In [0, numberOfRings-1]
	 * @param sectorIdx In [0, numberOfSectors-1]
	 * @return The minimal angle in degrees.
	 */
	public float getMinAngle(int ringIdx, int sectorIdx) {
		if (sectorIdx<0 || sectorIdx>=getRingLength(ringIdx))
			throw new IllegalArgumentException("Sector index out of bounds: "+sectorIdx);
		return (360*(sectorIdx-.5f) / (float)getRingLength(ringIdx) - 45);
	}
	/**
	 * Returns the minimal radius of the ring 'ringIdx'.
	 * @param ringIdx In [0, numberOfRings-1]
	 * @return The minimal radius in [0, 1].
	 */
	public float getMinRadius(int ringIdx) {
		if (ringIdx<0 || ringIdx>=mRingCount)
			throw new IllegalArgumentException("Ring index out of bounds: "+ringIdx);
		return (ringIdx / (float)mRingCount);
	}
	/**
	 * Returns the number of rings.
	 */
	public int getRingCount() {
		return mRingCount;
	}
	/**
	 * Returns the number of sectors in a ring.
	 * @param ringIdx In [0, numberOfRings-1]
	 */
	public int getRingLength(int ringIdx) {
		if (ringIdx<0 || ringIdx>=mRingCount)
			throw new IllegalArgumentException("Ring index out of bounds: "+ringIdx);
		if (ringIdx==0)
			return (int) Math.round(Math.pow(2, mCutCount));
		else
			return (8*ringIdx+2*mCutCount);
	}
	
	/**
	 * Returns true if the grid has a vertical cut.
	 * @return  True if the grid has a vertical cut.
	 */
	public boolean hasVerticalCut() {
		return mVerticalCut;
	}
	/**
	 * Returns true if the grid has a horizontal cut.
	 * @return True if the grid has a horizontal cut.
	 */
	public boolean hasHorizontalCut() {
		return mHorizontalCut;
	}
	/**
	 * Returns true if the central ring of the grid is a circle. 
	 * It is a circle only if there is no cut.
	 * @return True if the central ring of the grid is a circle.
	 */
	public boolean hasCentralCircle() {
		return !(mVerticalCut || mHorizontalCut);
	}
	
	/**
	 * Returns a default iterator over the grid.
	 */
	@Override
	public Iterator<E> iterator() {
		return new CircularGridIterator<E>(this);
	}
	/**
	 * Returns a specialized iterator over the grid.
	 * @return
	 */
	public CircularGridIterator<E> iteratorOverGrid() {
		return new CircularGridIterator<E>(this);
	}
	/**
	 * Returns a specialized iterator over a ring.
	 * @return
	 */
	public CircularGridIterator<E> iteratorOverRing(int ringIdx) {
		if (ringIdx<0 || ringIdx>=mRingCount)
			throw new IllegalArgumentException("Ring index out of bounds: "+ringIdx);
		return new CircularGridIterator<E>(this, ringIdx);
	}
	
	public void setInMatrix(int i, int j, E element) {
		setInArray(i, j, element);
	}
	
	private void init(int columnCount, int rowCount) {
		mColumnCount = columnCount;
		mRowCount = rowCount;
		mElements = new ArrayList<E>(columnCount*rowCount);
		for(int i=0; i<columnCount*rowCount; i++)
			mElements.add(null);
		mVerticalCut = SecaMath.isEven(columnCount);
		mHorizontalCut = SecaMath.isEven(rowCount);
		mCutCount = ((mVerticalCut)? 1 : 0) + ((mHorizontalCut)? 1 : 0);
		mRingCount = (int) Math.ceil(columnCount/2f);
	}
	private void fill(E[][] matrix) {
		assert(matrix.length==mColumnCount && matrix[0].length==mRowCount):"The size of the matrix is illegal.";
		for(int i=0; i<mColumnCount; i++)
			for(int j=0; j<mRowCount; j++)
				setInArray(i, j, matrix[i][j]);
	}
	private void fill(ArrayList<E> array) {
		assert(array.size()==mColumnCount*mRowCount):"The size of the array is illegal.";
		for(int i=0; i<mColumnCount; i++)
			for(int j=0; j<mRowCount; j++)
				setInArray(i, j, array.get(j*mRowCount+i));
	}
	
	/**
	 * Returns the element at the coordinates (i, j).
	 * @param i
	 * @param j
	 * @return
	 */
	private E getFromArray(int i, int j) {
		return mElements.get(j*mRowCount+i);
	}
	
	private Pair<Integer, Integer> gridToMatrix(int ringIdx, int sectorIdx) {
		int gridX = (mColumnCount-1)/2;
		int gridY = (mRowCount-1)/2;
		gridX -= ringIdx;
		gridY -= ringIdx;
		int currentSector = 0;
		while (currentSector<sectorIdx) {
			int outsideRings = mRingCount-1-ringIdx;
			if (gridX<(mColumnCount-1-outsideRings) && gridY==outsideRings)
				gridX++;
			if (gridX==(mColumnCount-1-outsideRings) && gridY<(mRowCount-1-outsideRings))
				gridY++;
			if (gridX>outsideRings && gridY==(mRowCount-1-outsideRings))
				gridX--;
			if (gridX==outsideRings && gridY>outsideRings)
				gridY--;
			currentSector++;
		}
		return Pair.create(gridX, gridY);
	}
	/**
	 * Replaces the element at the specified position in this grid with the specified element.
	 * Values are saved in an ArrayList instead of a 2D-Array because of the generic type.
	 * @param i
	 * @param j
	 * @param element
	 */
	private void setInArray(int i, int j, E element) {
		mElements.set(j*mRowCount+i, element);
	}
	
	//Structure to save elements
	/** Structure that contains the elements.*/
	private ArrayList<E> mElements;
	/** Number of columns of the matrix representation.*/
	private int mColumnCount;
	/** Number of rows of the matrix representation.*/
	private int mRowCount;
	//Essential parameters
	/** Number of cuts of the circular grid. In [0, 2].*/
	private int mCutCount;
	/** Number of rings of the circular grid.*/
	private int mRingCount;
	/** True if the grid has a vertical cut.*/
	private boolean mVerticalCut;
	/** True if the grid has a horizontal cut.*/
	private boolean mHorizontalCut;
	
}
