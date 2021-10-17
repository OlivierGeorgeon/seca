package gwt.seca.client;

import static gwt.g3d.client.math.MatrixStack.MODELVIEW;

import gwt.seca.client.agents.AbstractModel;
import gwt.seca.client.agents.Ernest11Model;
//import gwt.seca.client.agents.Ernest10Model;
//import gwt.seca.client.agents.Ernest7Model;
//import gwt.seca.client.agents.Ernest8Model;
import gwt.seca.client.services.XMLStreamTracer;
import gwt.seca.client.util.AxisAlignedBox;
import gwt.seca.client.util.Pair;
import gwt.seca.client.util.Ray;
import gwt.seca.client.util.SecaColor;
import gwt.seca.client.util.SecaMath;
import gwt.seca.client.util.SecaMath.CardinalDirection;
import gwt.seca.client.util.Triangle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

//import spas.IPlace;

/**
28	+	 * Represents the scene.
29	+	 * @author Olivier Voisin
30	+	 */
public class SceneManager {

	/**
	 * The active camera.
	 */
	public Camera mActiveCam;
	/**
	 * @author Olivier Voisin
	 * The enumeration of possible Ernest models.
	 */
	public static enum Model {Ernest7, Ernest8, Ernest10, Ernest11};
	private int mErnestNumber = 2;
	
	/**
	 * Constructor for a predefined scene 18x14x2.
	 * @param renderer The renderer to associate to the scene.
	 */
	SceneManager(int width, int depth, int height) {
		mWidth = width;			//i
		mDepth = depth;			//j
		mHeight = height;		//k
		mCameraList = new HashMap<String, Camera>();
		mBlockArray = new int[width*depth*height];
		mEntityList = new HashMap<String, IEntity>();
		mOutOfSceneBlockID = Block.stone.getBlockID();
//		mAgentList = new HashMap<String, ErnestModel>();
		
		// Hard-code the ground layer of the scene.
		int k = 0;
		for(int i=0; i<width; i++) {
			for(int j=0; j<depth; j++) {
				if (i==0 || i==width-1 || j==0 || j==depth-1)
					setBlockID(i, j, k, Block.stone.getBlockID());
				else
					setBlockID(i, j, k, Block.sand.getBlockID());
			}
		}
		
		// Hard-code the first layer of the scene.
//		//For Ernest7
//		int[][] grid = {{1, 1, 1, 1, 1, 1, 1, 1, 1},
//				{1, 0, 0, 0, 1, 1, 1, 1, 1},
//				{1, 0, 3, 0, 0, 0, 1, 1, 1},
//				{1, 0, 3, 3, 3, 0, 0, 0, 1},
//				{1, 0, 0, 0, 3, 3, 3, 0, 1},
//				{1, 1, 1, 0, 0, 0, 3, 0, 1},
//				{1, 1, 1, 1, 1, 0, 0, 0, 1},
//				{1, 1, 1, 1, 1, 1, 1, 1, 1}};
//		//For Ernest8
//		int[][] grid = {
//				{12, 12, 12, 12, 12, 12, 12, 12, 12},
//				{12,  0,  0,  0,  0,  0,  0,  0, 12},
//				{12,  0,  0,  0,  0,  0,  7,  0, 12},
//				{12,  0,  0,  0,  0,  0,  0,  0, 12},
//				{12,  0,  0,  0,  0,  0,  0,  0, 12},
//				{12,  0,  0,  0,  0,  0,  0,  0, 12},
//				{12,  0,  0,  0,  0,  0,  0,  0, 12},
//				{12, 12, 12, 12, 12, 12, 12, 12, 12}};
//		int gridWidth = 9;
//		int gridHeight = 8;
		//For Ernest10
		int[][] grid = {
				{14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14},
				{14,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14, 14, 14, 14, 14},
				{14,  0, 21, 14,  0,  0,  0,  0, 22, 22, 22, 22,  0, 14, 18, 18,  0, 14},
				{14,  0,  0, 14,  0,  0,  0,  0,  0,  0, 22, 22,  0, 14,  0,  0,  0, 14},
				{14,  0,  0, 14, 14, 14,  0,  0,  0, 22, 22,  0,  0,  0,  0,  0,  0, 14},
				{14,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14},
				{14,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14},
				{14,  0,  0,  0,  0,  0,  0, 14, 14, 14,  6,  0,  0,  0,  0,  0,  0, 14},
				{14,  0,  0,  0,  0,  0,  0,  0,  0, 14, 14, 14, 14, 14,  0,  0,  0, 14},
				{14,  0,  0,  0,  0,  0,  0, 20,  0,  0,  0,  0,  0,  7, 16,  0,  0, 14},
				{14,  0,  0,  0,  0,  0,  0,  0,  0,  0, 22, 22,  0,  7,  0,  0,  0, 14},
				{14, 14, 17,  0, 14, 14,  0,  0,  0, 22, 22,  0,  0,  0,  0,  0,  0, 14},
				{14, 14, 14, 14, 14, 14, 14,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 14},
				{14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14}};
		int gridWidth = 18;
		int gridHeight = 14;
		k = 1;
		// Hard-code the walls around the scene by repeating the first layer.
		for(int j=0; j<gridHeight; j++) {
			for(int i=0; i<gridWidth; i++) {
				int blockID = grid[gridHeight-j-1][i];
				setBlockID(i, j, k, blockID);
			}
		}
	}
	
	/**
	 * Constructor for a predefined scene 18x14x2.
	 * @param renderer The renderer to associate to the scene.
	 */
	SceneManager() {
//		//For Ernest7, Ernest8
//		this(9, 8, 2);
		//For Ernest10
		this(18, 14, 2);
		//this(16, 16, 2);
		//this(16, 16, 128);
		//this(32, 32, 32);
		//this(64, 64, 8);
	}
	
	/**
	 * Associate the renderer to the scene.
	 * @param renderer The renderer to associate to the scene.
	 */
	public void setRenderer(Renderer renderer) {
		mRenderer = renderer;
	}
	
	/**
	 * @param soundMgr The sound manager.
	 */
	public void setSoundManager(SoundManager soundMgr) {
		mSoundMgr = soundMgr;
	}
	
	/**
	 * @return The scene's width (X axis).
	 */
	public int getWidth() {
		return mWidth;
	}
	/**
	 * @return The scene's depth (Y axis).
	 */
	public int getDepth() {
		return mDepth;
	}
	/**
	 * @return The scene's height (Z axis).
	 */
	public int getHeight() {
		return mHeight;
	}
	
	//---------------------Block management
	/**
	 * @param i X coordinate
	 * @param j Y coordinate
	 * @param k Z coordinate
	 * @return true if the given location is within the scene's boundaries.
	 */
	public boolean inScene(int i, int j, int k) {
		return (i>=0 && i<mWidth && j>=0 && j<mDepth && k>=0 && k<mHeight);
	}
	/**
	 * @param pos The given location
	 * @return true if the given location is within the scene's boundaries.
	 */
	public boolean inScene(Point3i pos) {
		return inScene(pos.x, pos.y, pos.z);
	}
	/**
	 * @param pos The given location
	 * @return true if the given location is within the scene's boundaries.
	 */
	public boolean inScene(Vector3f pos) {
		return inScene(Math.round(pos.x), Math.round(pos.y), Math.round(pos.z));
	}
	/**
	 * Record the block Id at a given location in the scene.
	 * @param i X axis value.
	 * @param j Y axis value.
	 * @param k Z axis value.
	 * @param blockID The block ID.
	 * @return True if the given location is within the scene boundaries.
	 */
	public boolean setBlockID(int i, int j, int k, int blockID) {
		if (!inScene(i, j, k))
			return false;
		mBlockArray[i*mHeight*mDepth+j*mHeight+k] = blockID;
		return true;
	}
	/**
	 * Record the block Id at a given location in the scene.
	 * @param pos The tri-dimensional vector that represents the location.
	 * @param blockID The block ID.
	 * @return True if the given location is within the scene boundaries.
	 */
	public boolean setBlockID(Point3i pos, int blockID) {
		return setBlockID(Math.round(pos.getX()), Math.round(pos.getY()), Math.round(pos.getZ()), blockID);
	}
	/**
	 * Record the block Id at a given location in the scene.
	 * @param pos The tri-dimensional vector that represents the location.
	 * @param blockID The block ID.
	 * @return True if the given location is within the scene boundaries.
	 */
	public boolean setBlockID(Vector3f pos, int blockID) {
		return setBlockID(Math.round(pos.getX()), Math.round(pos.getY()), Math.round(pos.getZ()), blockID);
	}
	/**
	 * Get the block ID at a given location in the board.
	 * or mOutOfSceneBlockID.
	 * (Do not include entities)
	 * @param index The linear index of the cell.
	 * @return The block ID.
	 */
	public int getBoardBlockID(int index) {
		int blockID;
		if (index<0 || index>=mBlockArray.length)
			blockID = mOutOfSceneBlockID;
		else
			blockID = mBlockArray[index];
		return blockID;
	}
	/**
	 * Get the block ID at a given location in the scene.
	 * or mOutOfSceneBlockID.
	 * (may include an AGENT block different from the given agent)
	 * @param i X coordinate.
	 * @param j Y coordinate.
	 * @param k Z coordinate.
	 * @param name The given agent's name (so the agent won't detect itself)
	 * @return The block ID.
	 */
	public int getBlockID(int i, int j, int k, String name) {
		int blockID;
		if (!inScene(i, j, k))
			blockID =  mOutOfSceneBlockID;
		else
		{
			// Board blocks
			blockID = getBoardBlockID(i*mHeight*mDepth+j*mHeight+k);
			
			// Agent blocks
			Iterator<String> iterator = mEntityList.keySet().iterator();
		    while (iterator.hasNext()) {
		    	IEntity entity = mEntityList.get(iterator.next());
		    	if (entity.isInCell(i, j, k) && !entity.getName().equals(name)) 
		    		blockID = Block.agent.getBlockID();
		    }
		}
		return blockID;
	}
	/**
	 * Get the entity at a given location in the scene or null.
	 * (only entities different from the given name)
	 * @param postion The position in the scene.
	 * @param name The given agent's name (so the agent won't detect itself)
	 * @return The entity.
	 */
	public IEntity getEntity(Vector3f position, String name) 
	{
		if (!inScene(Math.round(position.x), Math.round(position.y), Math.round(position.z)))
			return null;
		else
		{
			Iterator<String> iterator = mEntityList.keySet().iterator();
		    while (iterator.hasNext()) {
		    	IEntity entity = mEntityList.get(iterator.next());
		    	if (entity.overlap(position) && !entity.getName().equals(name)) 
		    		return entity;
		    }
		}
		return null;
	}
	
	/**
	 * Get the block ID at a given location in the scene.
	 * or mOutOfSceneBlockID.
	 * @param pos The given location.
	 * @return The block ID.
	 */
//	public int getBlockID(Point3i pos) {
//		return getBlockID(pos.x, pos.y, pos.z, "any");
//	}
	/**
	 * Get the block ID at a given location in the scene.
	 * or mOutOfSceneBlockID.
	 * @param pos The given location.
	 * @return The block ID.
	 */
//	public int getBlockID(Vector3f pos) {
//		int i = Math.round(pos.getX());
//		int j = Math.round(pos.getY());
//		int k = Math.round(pos.getZ());
//		return getBlockID(i, j, k);
//	}
	/**
	 * Get the block at a given location in the scene.
	 * or null if out of scene.
	 * @param index The linear index of the cell.
	 * @return The block.
	 */
	public Block getBlock(int index) {
		if (index<0 || index>=mBlockArray.length)
			return null;
		return Block.blockList[mBlockArray[index]];
	}
	/**
	 * Get the block at a given location in the scene.
	 * or null if out of scene.
	 * @param i X coordinate.
	 * @param j Y coordinate.
	 * @param k Z coordinate.
	 * @return The block.
	 */
	public Block getBlock(int i, int j, int k) {
		if (!inScene(i, j, k))
			return null;
		return getBlock(i*mHeight*mDepth+j*mHeight+k);
	}
	/**
	 * Get the block ID at a given location in the scene.
	 * or null if out of scene.
	 * @param pos The location.
	 * @return The block.
	 */
	public Block getBlock(Point3i pos) {
		return getBlock(pos.x, pos.y, pos.z);
	}
	/**
	 * Get the block ID at a given location in the scene.
	 * or null if out of scene.
	 * @param pos The location.
	 * @return The block.
	 */
//	public Block getBlock(Vector3f pos) {
//		int i = Math.round(pos.getX());
//		int j = Math.round(pos.getY());
//		int k = Math.round(pos.getZ());
//		return getBlock(i, j, k);
//	}
	/**
	 * @return The number of cells in the scene.
	 */
	public int getBlockArrayLength() {
		return mBlockArray.length;
	}
	/**
	 * Computes the tri-dimensional position from the linear index of the cell.
	 * @param idx The linear index of the cell.
	 * @param pos The tri-dimensional position.
	 */
	public void getPosition(int idx, Vector3f pos) {
		pos.setX(idx/(mHeight*mDepth));
		pos.setY((idx%(mHeight*mDepth))/mHeight);
		pos.setZ(idx%mHeight);
	}
	/**
	 * Computes the tri-dimensional position from the linear index of the cell.
	 * @param idx The linear index of the cell.
	 * @return The tri-dimensional position.
	 */
	public Vector3f getPosition(int idx) {
		Vector3f pos = new Vector3f();
		getPosition(idx, pos);
		return pos;
	}

	/**
	 * Computes the position of the cell pointed by the mouse.
	 * @param ray The ray
	 * @param maxDistance The maximul distance.
	 * @return The point in the scene.
	 */
//	public Pair<Point3i, CardinalDirection> rayTrace(Ray ray, int maxDistance) {
//		Vector3f destination = new Vector3f();
//		destination.normalize(ray.getDirection());
//		destination.scale(maxDistance);
//		destination.add(ray.getOrigin());
//		ArrayList<Pair<Point3i, CardinalDirection>> cellPath = SecaMath.getCellPath(ray.getOrigin(), destination);
//		for(Pair<Point3i, CardinalDirection> cell : cellPath) {
////			mRenderer.addCellToRenderQueue(cell.mLeft);
//			if (cell!=null && inScene(cell.mLeft) && getBlockID(cell.mLeft)>0) {
//				AxisAlignedBox boundingBox = SecaMath.getTranslatedAAB(getBlock(cell.mLeft).getBlockAABB(), cell.mLeft);
//				Pair<Boolean, Float> result = ray.intersects(boundingBox);
//				if (result.getLeft()) {
////					mRenderer.addRayToRenderQueue(ray, result.getRight());
//					return cell;
//				}
//			}
//		}
//		return Pair.create(null, CardinalDirection.None);
//	}
	
	//---------------------Camera management
	/**
	 * Create a camera. Add it to the list of cameras.
	 * @param name The camera's name.
	 * @return The created camera.
	 */
	public Camera createCamera(String name) {
		Camera camera = new Camera(name);
		mCameraList.put(name, camera);
		camera.setSceneMgr(this);
		camera.setRenderer(mRenderer);
		return camera;
	}
	/**
	 * @param name The camera's name.
	 * @return The camera.
	 */
	public Camera getCamera(String name) {
		return mCameraList.get(name);
	}
	/**
	 * Remove a camera from the list of camera.
	 * @param name The camera's name.
	 */
	public void deleteCamera(String name) {
		mCameraList.remove(name);
	}
	/**
	 * Give the next camera in the list after a given camera
	 * @param camera The given camera.
	 * @return The next camera.
	 */
	public Camera getNextCamera(Camera camera) {
		Camera nextCamera = null;
		Iterator<String> iterator = mCameraList.keySet().iterator();
	    while (iterator.hasNext()) {
	    	Camera cam = mCameraList.get(iterator.next());
	    	if (nextCamera==null) // Starts from the first camera.
	    		nextCamera = cam;
	    	if (cam.equals(camera) && iterator.hasNext()) {
	    		nextCamera = mCameraList.get(iterator.next());
	    		break;
	    	}
	    }
	    return nextCamera;
	}
	/**
	 * @return The list of names of all the cameras.
	 */
	public Set<String> getCameraNames() {
		return mCameraList.keySet();
	}
	
	//---------------------Entity management
	/**
	 * Create an entity.
	 * @param name The entity's name.
	 * @return The entity.
	 */
	public IEntity createEntity(String name) {
		Entity entity = new Entity(name);
		mEntityList.put(name, entity);
		return entity;
	}
	/**
	 * Create an entity with a random name.
	 * @return The entity.
	 */
	public IEntity createEntity() {
		return createEntity(new StringBuilder().append("Entity ").append(System.currentTimeMillis()).toString());
	}
	/**
	 * Get an entity by its name.
	 * @param name The entity's name.
	 * @return The entity.
	 */
	public IEntity getEntity(String name) {
		return mEntityList.get(name);
	}
	/**
	 * Delete an entity by its name.
	 * @param name The entity's name.
	 */
	public void deleteEntity(String name) {
		mEntityList.remove(name);
		SoundManager.terminate.play();
	}
	/**
	 * @return A list of all the entities' names.
	 */
	public Set<String> getEntityNames() {
		return mEntityList.keySet();
	}
	
	//---------------------Agent management
	/**
	 * Create an agent. Add it to the list of entities.
	 * @param name The agent's name.
	 * @param model The agent's model.
	 * @return The agent.
	 */
	public AbstractModel createAgent(int num, Model model) {
		AbstractModel agent = null;
		switch(model) {
//		case Ernest7:
//			agent = new Ernest7Model(this, name);
//			break;
//		case Ernest8:
//			agent = new Ernest8Model(this, name);
//			break;
//		case Ernest10:
//			agent = new Ernest10Model(this, name, mSoundMgr);
//			break;
		case Ernest11:
			agent = new Ernest11Model(this, num, mSoundMgr);
			break;
		}
		if (agent!=null)
			mEntityList.put(agent.getName(), agent);
		return agent;
	}
	/**
	 * Create an Ernest 11 agent with a random name. Add it to the list of entities.
	 * @return The agent.
	 */
	public AbstractModel createAgent() {
		mErnestNumber++;
		return createAgent((mErnestNumber), Model.Ernest11);
	}
	/**
	 * Get an agent from its name from the list of entities.
	 * @param name The agent's name.
	 * @return The agent.
	 */
	public AbstractModel getAgent(String name) {
		//TODO test the cast
		return (AbstractModel) mEntityList.get(name);
	}
	/**
	 * Delete an agent from the list of entities.
	 * (in fact, any entity).
	 * @param name The agent's name.
	 */
	public void deleteAgent(String name) {
		mEntityList.remove(name);
	}
	/**
	 * @return The list of the agents' names.
	 */
	public Set<String> getAgentNames() {
		return mEntityList.keySet();
	}
	
	//---------------------Update management
	/**
	 * Update all the entities
	 * @param elapsedTime ?
	 */
	public void update(double elapsedTime) {
		Iterator<String> iterator = mEntityList.keySet().iterator();
	    while (iterator.hasNext()) {
	    	IEntity entity = mEntityList.get(iterator.next());
	    	entity.update(elapsedTime);
	    }
//	    iterator = mAgentList.keySet().iterator();
//	    while (iterator.hasNext()) {
//	    	ErnestModel agent = mAgentList.get(iterator.next());
//	    	agent.update(elapsedTime);
//	    }
	}
	/**
	 * Delete all the entities.
	 */
	public void clear() {
		mCameraList.clear();
		Iterator<String> iterator = mEntityList.keySet().iterator();
	    while (iterator.hasNext()) {
	    	IEntity entity = mEntityList.get(iterator.next());
	    	entity.dispose();
	    }
		mEntityList.clear();
	}
	
	private Renderer mRenderer;
	private SoundManager mSoundMgr;
	private HashMap<String, Camera> mCameraList;
	private int mBlockArray[];			//32768 = 2^15 = 2^7*2^4+2^4 = 128*16*16
	private HashMap<String, IEntity> mEntityList;
//	private HashMap<String, ErnestModel> mAgentList;
	private int mWidth;				//16
	private int mDepth; 			//16
	private int mHeight; 			//128
	private int mOutOfSceneBlockID;
}
