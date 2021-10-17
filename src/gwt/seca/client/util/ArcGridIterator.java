package gwt.seca.client.util;

import java.util.Iterator;

public class ArcGridIterator<E> implements Iterator<E> {
	
	/**
	 * Specify the type of an iteration: on the grid or on a ring.
	 * @author Olivier Voisin (ENSIETA 2011)
	 *
	 */
	public enum IterationType {GridIteration, RowIteration};
	
	/**
	 * Constructor for an iteration over the specified grid.
	 * @param grid
	 */
	public ArcGridIterator(ArcGrid<E> grid) {
		mGrid = grid;
		mType = IterationType.GridIteration;
		mRowIndex = -1;
		mColumnIndex = -1;
	}
	/**
	 * Constructor for an iteration over the specified ring of the specified grid.
	 * @param grid
	 */
	public ArcGridIterator(ArcGrid<E> grid, int rowIdx) {
		this(grid);
		mType = IterationType.RowIteration;
		mRowIndex = rowIdx;
	}
	
	/**
	 * Returns the maximal angle of the arc corresponding to the last element returned by next or previous,
	 * in the case of a one-dimension grid.
	 * @return The maximal angle in degrees.
	 */
	public float getMaxAngle(int i) {
		return getMaxAngleAlongX();
	}
	/**
	 * Returns the maximal angle along X of the arc corresponding to the last element returned by next or previous,
	 * in the case of a two-dimension grid.
	 * @return The maximal angle in degrees.
	 */
	public float getMaxAngleAlongX() {
		return mGrid.getMaxAngleAlongX(mColumnIndex);
	}
	/**
	 * Returns the maximal angle along Y of the arc corresponding to the last element returned by next or previous,
	 * in the case of a two-dimension grid.
	 * @return The maximal angle in degrees.
	 */
	public float getMaxAngleAlongY(int j) {
		return mGrid.getMaxAngleAlongY(mRowIndex);
	}
	
	/**
	 * Returns the mean angle of the arc corresponding to the last element returned by next or previous,
	 * in the case of a one-dimension grid.
	 * @return The mean angle in degrees.
	 */
	public float getMeanAngle() {
		return getMeanAngleAlongX();
	}
	/**
	 * Returns the mean angle along X of the arc corresponding to the last element returned by next or previous,
	 * in the case of a two-dimension grid.
	 * @return The mean angle in degrees.
	 */
	public float getMeanAngleAlongX() {
		return mGrid.getMeanAngleAlongX(mColumnIndex);
	}
	/**
	 * Returns the mean angle along Y of the arc corresponding to the last element returned by next or previous,
	 * in the case of a two-dimension grid.
	 * @return The mean angle in degrees.
	 */
	public float getMeanAngleAlongY() {
		return mGrid.getMeanAngleAlongY(mRowIndex);
	}
	
	/**
	 * Returns the minimal angle of the arc corresponding to the last element returned by next or previous,
	 * in the case of a one-dimension grid.
	 * @return The minimal angle in degrees.
	 */
	public float getMinAngle() {
		return getMinAngleAlongX();
	}
	/**
	 * Returns the minimal angle along X of the arc corresponding to the last element returned by next or previous,
	 * in the case of a two-dimension grid.
	 * @return The minimal angle in degrees.
	 */
	public float getMinAngleAlongX() {
		return mGrid.getMinAngleAlongY(mColumnIndex);
	}
	/**
	 * Returns the minimal angle along Y of the arc corresponding to the specified element,
	 * in the case of a two-dimension grid.
	 * @return The minimal angle in degrees.
	 */
	public float getMinAngleAlongY() {
		return mGrid.getMinAngleAlongY(mRowIndex);
	}
	
	/**
	 * Returns the radius of the last element returned by next or previous.
	 * @return The radius of the last element returned by next or previous.
	 */
	public float getRadius() {
		return mGrid.getRadius(mColumnIndex, mRowIndex);
	}
	
	/**
	 * Returns true if the iteration has more elements when traversing the grid in the forward direction.
	 * @return true if the iteration has more elements when traversing the grid in the forward direction.
	 */
	public boolean hasNext() {
		if (mGrid.getResolutionAlongY()<=0 || mGrid.getResolutionAlongX()<=0)
			//No iteration is possible
			return false;
		if (mRowIndex==-1)
			//The iteration has just been initialized
			return true;
		else {
			//The iteration has already returned an element
			if (mColumnIndex==mGrid.getResolutionAlongX()-1) {
				//The iteration has reached the last cell of the current row
				if (mType==IterationType.RowIteration || mRowIndex==mGrid.getResolutionAlongY()-1)
					//The iteration on the row or on the grid is finished
					return false;
				else {
					//Another row is available
					return true;
				}
			} else
				//Another cell is available on the current row
				return true;
		}
	}
	
	/**
	 * Returns true if the iteration has more elements when traversing the grid in the reverse direction.
	 * @return true if the iteration has more elements when traversing the grid in the reverse direction.
	 */
	public boolean hasPrevious() {
		if (mGrid.getResolutionAlongY()<=0 || mGrid.getResolutionAlongX()<=0)
			//No iteration is possible
			return false;
		if (mRowIndex==-1)
			//The iteration has just been initialized
			return true;
		else {
			//The iteration has already returned an element
			if (mColumnIndex==0) {
				//The iteration has reached the first cell of the current row
				if (mType==IterationType.RowIteration || mRowIndex==0)
					//The iteration on the row or on the grid is finished
					return false;
				else {
					//Another row is available
					return true;
				}
			} else
				//Another cell is available on the current row
				return true;
		}
	}
	
	/**
	 * Returns the next element in the iteration.
	 * @return The next element in the iteration.
	 * @throws NoSuchElementException Iteration has no more elements.
	 */
	@Override
	public E next() {
		if (mRowIndex == -1) {
			//First call after the initialization of the grid iteration
			mRowIndex = 0;
		}
		if (mColumnIndex==mGrid.getResolutionAlongX()-1) {
			//The iteration has reached the last cell of the current row.
			if (mType==IterationType.RowIteration || mRowIndex==mGrid.getResolutionAlongY()-1)
				//The iteration on the row or on the grid is finished
				throw new java.util.NoSuchElementException();
			else {
				//Go to the next row
				mRowIndex++;
				mColumnIndex = -1;
			}
		}
		if (mColumnIndex == -1) {			
			//Start a new row
			//Initialize the position at the beginning of the ring 'mRingIndex'.
			mColumnIndex = 0;
		} else {
			//Update the position on the row 'mRowIndex'.
			mColumnIndex++;
		}
		return mGrid.getElement(mColumnIndex, mRowIndex);
	}
	
	/**
	 * Returns the previous element in the iteration.
	 * @return The previous element in the iteration.
	 * @throws NoSuchElementException Iteration has no more elements.
	 */
	public E previous() {
		if (mRowIndex == -1) {
			//First call after the initialization of the grid iteration
			mRowIndex = mGrid.getResolutionAlongY()-1;
		}
		if (mColumnIndex==0) {
			//The iteration has reached the first cell of the current row.
			if (mType==IterationType.RowIteration || mRowIndex==0)
				//The iteration on the row or on the grid is finished
				throw new java.util.NoSuchElementException();
			else {
				//Go to the next row
				mRowIndex--;
				mColumnIndex = -1;
			}
		}
		if (mColumnIndex == -1) {			
			//Start a new row
			//Initialize the position at the beginning of the ring 'mRingIndex'.
			mColumnIndex = mGrid.getResolutionAlongX()-1;
		} else {
			//Update the position on the row 'mRowIndex'.
			mColumnIndex--;
		}
		return mGrid.getElement(mColumnIndex, mRowIndex);
	}
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Replaces the last element returned by next or previous with the specified element
	 * @param e
	 */
	public void set(E element) {
		mGrid.setElement(mColumnIndex, mRowIndex, element);
	}
	
	/**
	 * Defines the radius of the last element returned by next or previous.
	 * @param radius New radius of the element.
	 */
	public void setRadius(int radius) {
		mGrid.setRadius(mColumnIndex, mRowIndex, radius);
	}
	
	/** The arc grid on which the iteration is done.*/
	private ArcGrid<E> mGrid;
	/** Type of the iteration*/
	private IterationType mType;
	/** Index of the current row. Equals -1 after the initialization of an iteration on the whole grid.*/
	private int mRowIndex;
	/** Index of the current column. Equals -1 after the initialization.*/
	private int mColumnIndex;

}
