package gwt.seca.client.util;

import java.util.Iterator;

public class CircularGridIterator<E> implements Iterator<E> {
	
	/**
	 * Specify the type of an iteration: on the grid or on a ring.
	 * @author Olivier Voisin (ENSIETA 2011)
	 *
	 */
	public enum IterationType {GridIteration, RingIteration};
	
	/**
	 * Constructor for an iteration over the specified grid.
	 * @param grid
	 */
	public CircularGridIterator(CircularGrid<E> grid) {
		mGrid = grid;
		Pair<Integer, Integer> size = grid.getMatrixSize();
		mColumnCount = size.mLeft;
		mRowCount = size.mRight;
		mType = IterationType.GridIteration;
		mRingIndex = -1;
		mSectorIndex = -1;
	}
	/**
	 * Constructor for an iteration over the specified ring of the specified grid.
	 * @param grid
	 */
	public CircularGridIterator(CircularGrid<E> grid, int ringIdx) {
		this(grid);
		mType = IterationType.RingIteration;
		mRingIndex = ringIdx;
	}
	
	/**
	 * Returns the maximal angle of the sector that contains the last element returned by next or previous.
	 * Angles are clockwise.
	 * @return Maximal angle in degrees.
	 */
	public float getMaxAngle() {
		return mGrid.getMaxAngle(mRingIndex, mSectorIndex);
	}
	/**
	 * Returns the maximal radius of the ring that contains the last element returned by next or previous.
	 * @return Maximal radius in [0, 1].
	 */
	public float getMaxRadius() {
		return mGrid.getMaxRadius(mRingIndex);
	}
	/**
	 * Returns the mean angle of the sector that contains the last element returned by next or previous.
	 * Angles are clockwise.
	 * @return Mean angle in degrees.
	 */
	public float getMeanAngle() {
		return mGrid.getMeanAngle(mRingIndex, mSectorIndex);
	}
	/**
	 * Returns the mean radius of the ring that contains the last element returned by next or previous.
	 * @return Mean radius in [0, 1].
	 */
	public float getMeanRadius() {
		return mGrid.getMeanRadius(mRingIndex);
	}
	/**
	 * Returns the minimal angle of the sector that contains the last element returned by next or previous. 
	 * Angles are clockwise.
	 * @return Minimal angle in degrees.
	 */
	public float getMinAngle() {
		return mGrid.getMinAngle(mRingIndex, mSectorIndex);
	}
	/**
	 * Returns the minimal radius of the ring that contains the last element returned by next or previous.
	 * @return Minimal radius in [0, 1].
	 */
	public float getMinRadius() {
		return mGrid.getMinRadius(mRingIndex);
	}
	/**
	 * Returns the index of the sector that contains the last element returned by next or previous.
	 * @return
	 */
	public int getSectorIndex() {
		return mSectorIndex;
	}
	/**
	 * Returns the index of the ring that contains the last element returned by next or previous.
	 * @return
	 */
	public int getRingIndex() {
		return mRingIndex;
	}
	
	/**
	 * Returns true if the iteration has more elements when traversing the grid in the forward direction.
	 * @return true if the iteration has more elements when traversing the grid in the forward direction.
	 */
	public boolean hasNext() {
		if (mGrid.getRingCount()<=0)
			//No iteration is possible
			return false;
		if (mRingIndex==-1)
			//The iteration has just been initialized
			return true;
		else {
			//The iteration has already returned an element
			if (mSectorIndex==mGrid.getRingLength(mRingIndex)-1) {
				//The iteration has reached the last sector of the current ring
				if (mType==IterationType.RingIteration || mRingIndex==mGrid.getRingCount()-1)
					//The iteration on the ring or on the grid is finished
					return false;
				else {
					//Another ring is available
					return true;
				}
			} else
				//Another sector is available on the current ring
				return true;
		}
	}
	/**
	 * Returns true if the iteration has more elements when traversing the grid in the reverse direction.
	 * @return true if the iteration has more elements when traversing the grid in the reverse direction.
	 */
	public boolean hasPrevious() {
		if (mGrid.getRingCount()<=0)
			//No iteration is possible
			return false;
		if (mRingIndex==-1)
			//The iteration has just been initialized
			return true;
		else {
			//The iteration has already returned an element
			if (mSectorIndex==0) {
				//The iteration has reached the first sector of the current ring
				if (mType==IterationType.RingIteration || mRingIndex==0)
					//The iteration on the ring or on the grid is finished
					return false;
				else {
					//Another ring is available
					return true;
				}
			} else
				//Another sector is available on the current ring
				return true;
		}
	}
	/**
	 * Tests if the last element returned by next or previous is a central circle or not.
	 * @return
	 */
	public boolean isCentralCircle() {
		if (mGrid.hasCentralCircle() && mRingIndex==0)
			return true;
		else
			return false;
	}

	/**
	 * Returns the next element in the iteration.
	 * @return The next element in the iteration.
	 * @throws NoSuchElementException Iteration has no more elements.
	 */
	@Override
	public E next() {
		if (mRingIndex == -1) {
			//First call after the initialization of the grid iteration
			mRingIndex = 0;
		}
		if (mSectorIndex==mGrid.getRingLength(mRingIndex)-1) {
			//The iteration has reached the last sector of the current ring
			if (mType==IterationType.RingIteration || mRingIndex==mGrid.getRingCount()-1)
				//The iteration on the ring or on the grid is finished
				throw new java.util.NoSuchElementException();
			else {
				//Go to the next ring
				mRingIndex++;
				mSectorIndex = -1;
			}
		}
		if (mSectorIndex == -1) {
			//Start a new ring
			//Initialize the position at the beginning of the ring 'mRingIndex'.
			mGridX = (mColumnCount-1)/2 - mRingIndex;
			mGridY = (mRowCount-1)/2 - mRingIndex;
			mSectorIndex = 0;
		} else {
			//Update the position on the ring 'mRingIndex'.
			int outsideRings = mGrid.getRingCount()-1-mRingIndex;
			if (mGridX<(mColumnCount-1-outsideRings) && mGridY==outsideRings)
				mGridX++;
			else if (mGridX==(mColumnCount-1-outsideRings) && mGridY<(mRowCount-1-outsideRings))
				mGridY++;
			else if (mGridX>outsideRings && mGridY==(mRowCount-1-outsideRings))
				mGridX--;
			else if (mGridX==outsideRings && mGridY>outsideRings)
				mGridY--;
			mSectorIndex++;
		}
		return mGrid.getFromMatrix(mGridX, mGridY);
	}

//	public int nextIndex() {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	/**
	 * Returns the previous element in the iteration.
	 * @return The previous element in the iteration.
	 * @throws NoSuchElementException Iteration has no more elements.
	 */
	public E previous() {
		if (mRingIndex == -1) {
			//First call after the initialization of the grid iteration
			mRingIndex = mGrid.getRingCount()-1;
		}
		if (mSectorIndex==0) {
			//The iteration has reached the last sector of the current ring
			if (mType==IterationType.RingIteration || mRingIndex==0)
				//The iteration on the ring or on the grid is finished
				throw new java.util.NoSuchElementException();
			else {
				//Go to the previous ring
				mRingIndex--;
				mSectorIndex = -1;
			}
		}
		if (mSectorIndex == -1) {
			//Start a new ring
			//Initialize the position on the beginning of the ring 'mRingIndex'.
			mGridX = (mColumnCount-1)/2 - mRingIndex;
			mGridY = (mRowCount-1)/2 - mRingIndex;
			//Translate the position to the end of the ring.
			if (mRingIndex == 0) {
				if (mGrid.hasHorizontalCut())
					mGridY++;
				else if (mGrid.hasVerticalCut())
					mGridX++;
			} else
				mGridY++;
			mSectorIndex = mGrid.getRingLength(mRingIndex)-1;
		} else {
			//Update the position on the ring 'mRingIndex'.
			int outsideRings = mGrid.getRingCount()-1-mRingIndex;
			if (mGridX == outsideRings && mGridY < (mRowCount-1-outsideRings))
				mGridY++;
			else if (mGridX < (mColumnCount-1-outsideRings) && mGridY == (mRowCount-1-outsideRings))
				mGridX++;
			else if (mGridX == (mColumnCount-1-outsideRings) && mGridY > outsideRings)
				mGridY--;
			else if (mGridX > outsideRings && mGridY == outsideRings)
				mGridX--;
			mSectorIndex--;
		}
		return mGrid.getFromMatrix(mGridX, mGridY);
	}

//	public int previousIndex() {
//		// TODO Auto-generated method stub
//		return 0;
//	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Replaces the last element returned by next or previous with the specified element
	 * @param e
	 */
	public void set(E element) {
		mGrid.setInMatrix(mGridX, mGridY, element);
	}

	/** The circular grid on which the iteration is done.*/
	private CircularGrid<E> mGrid;
	/** Number of columns of the matrix representation.*/
	private int mColumnCount;
	/** Number of rows of the matrix representation.*/
	private int mRowCount;
	/** Type of the iteration*/
	private IterationType mType;
	/** Index of the current ring. Equals -1 after the initialization of an iteration on the whole grid.*/
	private int mRingIndex;
	/** Index of the current ring. Equals -1 after the initialization.*/
	private int mSectorIndex;
	/** Column index in the matrix representation of the grid.*/
	private int mGridX;
	/** Row index in the matrix representation of the grid.*/
	private int mGridY;

}
