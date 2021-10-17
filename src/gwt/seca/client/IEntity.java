package gwt.seca.client;

import gwt.seca.client.util.AxisAlignedBox;

import javax.vecmath.Vector3f;

/**
 * An entity is anything that may not align in the grid.
 * So far, all our entities are agents.
 * @author Olivier Voisin
 */
public interface IEntity {
	
	/**
	 * @param elapsedTime
	 */
	public void update(double elapsedTime);
	/**
	 * @param renderer
	 */
	public void render(Renderer renderer);
	/**
	 *
	 */
	public void dispose();
	
	/**
	 * @return The entity's position.
	 */
	public Vector3f getPosition();
	/**
	 * @return
	 */
	public AxisAlignedBox getAABB();
	/**
	 * @param localVec
	 * @return
	 */
	public Vector3f localToParentRef(Vector3f localVec);
	
	/**
	 * Tell if this entity occupies a given cell.
	 * @param i X axis.
	 * @param j Y axis.
	 * @param k Z axis.
	 * @return true if this entity occupies the given cell.
	 */
	public boolean isInCell(int i, int j, int k);
	public boolean overlap(Vector3f position); 
	
	/**
	 * @return The entity's name.
	 */
	public String getName();

}
