package gwt.seca.client;

import static gwt.g3d.client.math.MatrixStack.MODELVIEW;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import ernest.Ernest;

import gwt.g2d.client.graphics.Color;
import gwt.seca.client.util.AxisAlignedBox;
import gwt.seca.client.util.SecaColor;

/**
 * A block is a type of stuff that can be placed in a grid cell.
 * Blocks have rendering properties for their rendering.
 * Blocks have a material that defines what interactions they afford to Ernest.
 * @author Olivier Voisin
 */
public class Block {
	SceneManager mSceneMgr;
	
	Block(String name, Material material, int meshID, int textureID, Color color) {
		blockList[BLOCK_COUNT] = this;
		mBlockID = BLOCK_COUNT++;
		mName = name;
		mMaterial = material;
		mMeshID = meshID;
		mTextureID = textureID;
		mColor = color;
//		mMeshSize = new Vector3f(2, 2, 2);
//		mBlockSize = new Vector3f(1, 1, 1);
		mMeshAABB = new AxisAlignedBox(-1, -1, -1, 1, 1, 1);
		mBlockAABB = new AxisAlignedBox(-.501f, -.501f, -.501f, .501f, .501f, .501f);
		mRotation = new Vector3f();
		mScale = new Vector3f(1, 1 , 1);
		mOffset = new Vector3f();
		setVisible(true);
		setBuildingBlock(true);
		mRatioLocked = true;
		//
		computeTransformation();
	}
	Block(String name, Material material, int meshID, int textureID) {
		this(name, material, meshID, textureID, new Color(0, 0, 0));
	}
	Block(String name, Material material) {
		this(name, material, 0, 0);
	}
	/** Copy constructor*/
	Block(String name, Block block) {
		this(name, block.getMaterial(), block.getMeshID(), block.getTexID(), block.getColor());
		this.setRotation(block.getRotation());
		this.setMeshAABB(block.getMeshAABB());
		this.setBlockAABB(block.getBlockAABB());
		this.setVisible(block.isVisible());
		this.setBuildingBlock(block.isBuildingBlock());
		this.setRatioLocked(block.isRatioLocked());
	}
	
	public int getBlockID() {
		return mBlockID;
	}
	public String getName() {
		return mName;
	}
	/**
	 * @return The block's material.
	 */
	public Material getMaterial() {
		return mMaterial;
	}
	@SuppressWarnings("unused")
	private Block setMaterial(Material material) {
		mMaterial = material;
		return this;
	}
	public int getMeshID() {
		return mMeshID;
	}
	@SuppressWarnings("unused")
	private Block setMeshID(int meshID) {
		mMeshID = meshID;
		return this;
	}
	public int getTexID() {
		return mTextureID;
	}
	private Block setTexID(int texID) {
		mTextureID = texID;
		return this;
	}
	public Color getColor() {
		return mColor;
	}
	private Block setColor(Color color) {
		mColor = color;
		return this;
	}
//	public Vector3f getMeshSize() {
//		return new Vector3f(mMeshSize);
//	}
//	private Block setMeshSize(float x, float y, float z) {
//		mMeshSize.set(x, y, z);
//		computeScale();
//		return this;
//	}
//	private Block setMeshSize(Vector3f vec) {
//		return setMeshSize(vec.getX(), vec.getY(), vec.getZ());
//	}
//	public Vector3f getBlockSize() {
//		return new Vector3f(mBlockSize);
//	}
//	private Block setBlockSize(float x, float y, float z) {
//		mBlockSize.set(x, y, z);
//		mBlockSize.clamp(0, 1);
//		if (mBlockSize.lengthSquared() <= .01)
//			mVisible = false;
//		computeScale();
//		return this;
//	}
//	private Block setBlockSize(Vector3f vec) {
//		return setBlockSize(vec.getX(), vec.getY(), vec.getZ());
//	}
	public AxisAlignedBox getMeshAABB() {
		return new AxisAlignedBox(mMeshAABB);
	}
	private Block setMeshAABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, boolean keepRatio) {
		mMeshAABB.setMinimum(minX, minY, minZ);
		mMeshAABB.setMaximum(maxX, maxY, maxZ);
		computeTransformation();
		return this;
	}
	private Block setMeshAABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return setMeshAABB(minX, minY, minZ, maxX, maxY, maxZ, true);
	}
	private Block setMeshAABB(AxisAlignedBox box, boolean keepRatio) {
		Vector3f min = box.getCorner(AxisAlignedBox.MINIMUM);
		Vector3f max = box.getCorner(AxisAlignedBox.MAXIMUM);
		return setMeshAABB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(), keepRatio);
	}
	private Block setMeshAABB(AxisAlignedBox box) {
		return setMeshAABB(box, true);
	}
	public AxisAlignedBox getBlockAABB() {
		return new AxisAlignedBox(mBlockAABB);
	}
	private Block setBlockAABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, boolean keepRatio) {
		Vector3f min = new Vector3f(minX, minY, minZ);
		Vector3f max = new Vector3f(maxX, maxY, maxZ);
		min.clamp(-.5f, .5f);
		max.clamp(-.5f, .5f);
		mBlockAABB.setMinimum(min);
		mBlockAABB.setMaximum(max);
		if (mBlockAABB.getVolume() <= .001)
			setVisible(false);
		if (mBlockAABB.getVolume() < 1)
			setBuildingBlock(false);
		mRatioLocked = keepRatio;
		computeTransformation();
		return this;
	}
	private Block setBlockAABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
		return setBlockAABB(minX, minY, minZ, maxX, maxY, maxZ, true);
	}
	private Block setBlockAABB(AxisAlignedBox box, boolean keepRatio) {
		Vector3f min = box.getCorner(AxisAlignedBox.MINIMUM);
		Vector3f max = box.getCorner(AxisAlignedBox.MAXIMUM);
		return setBlockAABB(min.getX(), min.getY(), min.getZ(), max.getX(), max.getY(), max.getZ(), keepRatio);
	}
	private Block setBlockAABB(AxisAlignedBox box) {
		return setBlockAABB(box, true);
	}
//	public Vector3f getOffset() {
//		return new Vector3f(mOffset);
//	}
//	private Block setOffset(float x, float y, float z) {
//		mOffset.set(x, y, z);
////		mOffset.setX(Math.min(mOffset.getX(), .5f - mBlockSize.getX()/2f));
////		mOffset.setX(Math.max(mOffset.getX(), -.5f + mBlockSize.getX()/2f));
////		mOffset.setY(Math.min(mOffset.getY(), .5f - mBlockSize.getY()/2f));
////		mOffset.setY(Math.max(mOffset.getY(), -.5f + mBlockSize.getY()/2f));
////		mOffset.setZ(Math.min(mOffset.getZ(), .5f - mBlockSize.getZ()/2f));
////		mOffset.setZ(Math.max(mOffset.getZ(), -.5f + mBlockSize.getZ()/2f));
//		return this;
//	}
//	private Block setOffset(Vector3f vec) {
//		return setOffset(vec.getX(), vec.getY(), vec.getZ());
//	}
	public Vector3f getRotation() {
		return new Vector3f(mRotation);
	}
	private Block setRotation(float rotX, float rotY, float rotZ) {
		mRotation.set(rotX, rotY, rotZ);
		return this;
	}
	private Block setRotation(Vector3f vec) {
		return setRotation(vec.getX(), vec.getY(), vec.getZ());
	}
	public boolean isVisible() {
		return mVisible;
	}
	public Block setVisible(boolean visible) {
		mVisible = visible && (mBlockAABB.getVolume() > .001);
		return this;
	}
	public boolean isBuildingBlock() {
		return mBuildingBlock;
	}
	public Block setBuildingBlock(boolean buildingBlock) {
		mBuildingBlock = buildingBlock;
		return this;
	}
	public boolean isRatioLocked() {
		return mRatioLocked;
	}
	public Block setRatioLocked(boolean ratioLocked) {
		mRatioLocked = ratioLocked;
		return this;
	}
	
	public boolean render(Renderer renderer, boolean texturedMode, boolean bindTexOrColor) {
		if (!mVisible)
			return false;
		if (bindTexOrColor) {
			boolean bindTex = texturedMode;
			//Force to apply a color if no texture is available
			if (mTextureID==0 || mTextureID>=Resources.TEX_LIST_LENGTH)
				bindTex = false;
			else if (Resources.mTexList[mTextureID]==null)
				bindTex = false;
			if (bindTex) {
				//Apply texture
				renderer.enableTextures();
				(Resources.mTexList[mTextureID]).bind();
			} else {
				//Apply uniform color
				renderer.enableUniformColor();
				renderer.setColorUniform(new Vector4f(mColor.getR()/255f, mColor.getG()/255f, mColor.getB()/255f, (float) mColor.getAlpha()*0.8f));
			}
		}
		
		MODELVIEW.push();
		MODELVIEW.translate(mOffset.getX(), mOffset.getY(), mOffset.getZ());
		MODELVIEW.scale(mScale.getX(), mScale.getY(), mScale.getZ());
		MODELVIEW.rotateX((float) Math.toRadians(mRotation.getX()));
		MODELVIEW.rotateY((float) Math.toRadians(mRotation.getY()));
		MODELVIEW.rotateZ((float) Math.toRadians(mRotation.getZ()));
		renderer.setMatrixUniforms();
		if (mMeshID<Resources.MESH_LIST_LENGTH && Resources.mMeshList[mMeshID]!=null) {
			Resources.mMeshList[mMeshID].draw();
		}
		MODELVIEW.pop();
		return true;
	}
	private void computeTransformation() {
		mScale.setX(mBlockAABB.getSize().getX()/mMeshAABB.getSize().getX());
		mScale.setY(mBlockAABB.getSize().getY()/mMeshAABB.getSize().getY());
		mScale.setZ(mBlockAABB.getSize().getZ()/mMeshAABB.getSize().getZ());
		if (mRatioLocked) {
			float minScale = Math.min(Math.min(mScale.getX(), mScale.getY()), mScale.getZ());
			mScale.set(minScale, minScale, minScale);
		}
		Vector3f meshCenter = new Vector3f(mMeshAABB.getCenter());
		meshCenter.setX(meshCenter.x * mScale.x);
		meshCenter.setY(meshCenter.y * mScale.y);
		meshCenter.setZ(meshCenter.z * mScale.z);
		mOffset.sub(mBlockAABB.getCenter(), meshCenter);
	}
	
	private int mBlockID;
	private String mName;
	//Material
	private Material mMaterial;
	//Rendering attributes
	private int mMeshID;
	private int mTextureID;
	private Color mColor;
	
//	private Vector3f mMeshSize; 	//The default initial size is (2, 2, 2)
//	private Vector3f mBlockSize;	//The default wanted size is (1, 1, 1)
	private AxisAlignedBox mMeshAABB;		//The default initial size is [(-1, -1, -1), (1, 1, 1)]
	private AxisAlignedBox mBlockAABB;	//The default initial size is [(-.5, -.5, -.5), (.5, .5, .5)]
	private Vector3f mScale;
	private Vector3f mOffset;
	private Vector3f mRotation;
	private boolean mVisible;
	private boolean mBuildingBlock;
	private boolean mRatioLocked;
	
	public static final int BLOCK_LIST_LENGTH = 23;
	public static int BLOCK_COUNT = 0;
	public static final Block blockList[];
	// Warning: Never use the index number in the code. Use the method 'getBlockID()' instead.
	public static final Block air;
	public static final Block stone;
	public static final Block grass;
	public static final Block sand;
	public static final Block bricks;
	public static final Block crate;
	public static final Block brickOrange;
	public static final Block brickChartreuseGreen;
	public static final Block brickSpringGreen;
	public static final Block brickAzure;
	public static final Block brickViolet;
	public static final Block brickRose;
	public static final Block ballBlue;
	public static final Block ballPurple;
	public static final Block stoneSlab;
	public static final Block grassSlab;
	public static final Block plantStoplightYellow;
	public static final Block plantStoplightCyan;
	public static final Block plantStoplightMagenta;
	public static final Block plantStoplightRed;
	public static final Block plantStoplightGreen;
	public static final Block plantStoplightBlue;
	public static final Block animalFish;
	public static final Block agent;
	
	static {
		blockList = new Block[BLOCK_LIST_LENGTH];
		air = new Block("Air", Material.AIR);
		//Building blocks
		stone = new Block("Green stone", Material.WALL, Resources.MESH_ID_BOX, Resources.TEX_ID_NONE, SecaColor.getColor("#486858"));
		//stone = new Block("Green stone", Material.WALL, Resources.MESH_ID_BOX, Resources.TEX_ID_STONE_GREEN, SecaColor.getColor("#486858"));
		//stone = new Block("Green stone", Material.WALL, Resources.MESH_ID_BOX, Resources.TEX_ID_NONE, SecaColor.getColor(Ernest.STIMULATION_VISUAL_WALL));
		sand = new Block("Sand", Material.WALL, Resources.MESH_ID_BOX, Resources.TEX_ID_SAND_DARK, SecaColor.getColor("#b8a888"));
		grass = new Block("Grass", Material.WALL, Resources.MESH_ID_BOX, Resources.TEX_ID_GRASS_GREEN, new Color(0, 200, 0));
		//Other buildings blocks
		bricks = new Block("Bricks", Material.WALL, Resources.MESH_ID_BOX, Resources.TEX_ID_BRICKS, SecaColor.getColor("#b88868"));
		crate = new Block("Crate", Material.WALL, Resources.MESH_ID_BOX, Resources.TEX_ID_CRATE, new Color(200, 200, 0));
		brickOrange = new Block("Orange brick", Material.WALL, Resources.MESH_ID_BOX, Resources.TEX_ID_NONE, SecaColor.getColor("#FF8000"));
		brickChartreuseGreen = new Block("Chartreuse green brick", brickOrange).setColor(SecaColor.getColor("#80FF00"));
		brickSpringGreen = new Block("Spring green brick", brickOrange).setColor(SecaColor.getColor("#80FF00"));
		brickAzure = new Block("Azure brick", brickOrange).setColor(SecaColor.getColor("#80FF00"));
		brickViolet = new Block("Violet brick", brickOrange).setColor(SecaColor.getColor("#80FF00"));
		brickRose = new Block("Rose brick", brickOrange).setColor(SecaColor.getColor("#80FF00"));
		//Balls
		ballBlue = new Block("Blue ball", Material.FOOD, Resources.MESH_ID_SPHERE, Resources.TEX_ID_NONE, SecaColor.getColor("#0033CC"));
		ballPurple = new Block("Purple ball", Material.FOOD, Resources.MESH_ID_SPHERE, Resources.TEX_ID_NONE, SecaColor.getColor("#660099"));
		//Slabs
		stoneSlab = new Block("Stone slab", stone).setBlockAABB(-.5f, -.5f, -.5f, .5f, .5f, 0.5f, false);
		grassSlab = new Block("Grass slab", grass).setBlockAABB(-.5f, -.5f, -.5f, .5f, .5f, 0f, false);
		//Blocks from models
		//Plants
//		plantStoplightYellow = new Block("Yellow stoplight", Material.DIRT, Resources.MESH_ID_PLANT_STOPLIGHT, Resources.TEX_ID_PLANT_STOPLIGHT_YELLOW, SecaColor.getColor("#FFFF00"))
//			.setRotation(90, 0, 0).setMeshAABB(-20, -20, 0, 20, 20, 50, false).setBuildingBlock(false);
//		plantStoplightCyan = new Block("Cyan stoplight" , plantStoplightYellow)
//			.setTexID(Resources.TEX_ID_PLANT_STOPLIGHT_CYAN).setColor(SecaColor.getColor("#00FFFF"));
//		plantStoplightMagenta = new Block("Magenta stoplight" , plantStoplightYellow)
//			.setTexID(Resources.TEX_ID_PLANT_STOPLIGHT_MAGENTA).setColor(SecaColor.getColor("#FF00FF"));
//		plantStoplightRed = new Block("Red stoplight" , plantStoplightYellow)
//			.setTexID(Resources.TEX_ID_PLANT_STOPLIGHT_RED).setColor(SecaColor.getColor("#FF0000"));
//		plantStoplightGreen = new Block("Green stoplight" , plantStoplightYellow)
//			.setTexID(Resources.TEX_ID_PLANT_STOPLIGHT_GREEN).setColor(SecaColor.getColor("#00FF00"));
//		plantStoplightBlue = new Block("Blue stoplight" , plantStoplightYellow)
//			.setTexID(Resources.TEX_ID_PLANT_STOPLIGHT_BLUE).setColor(SecaColor.getColor("#0000FF"));
		plantStoplightYellow = new Block("Yellow stoplight", Material.DIRT, Resources.MESH_ID_BOX, Resources.TEX_ID_NONE, SecaColor.getColor("#FFFF00"))
			.setBlockAABB(-.5f, -.5f, -.5f, .5f, .5f, 0.07f, false).setBuildingBlock(false);
		//For the other colors, we copy the block 'plantStoplightYellow' and change only the name and the color.
		plantStoplightCyan = new Block("Cyan stoplight" , plantStoplightYellow)
			.setColor(SecaColor.getColor("#00FFFF"));
		plantStoplightCyan.setBlockAABB(-.5f, -.5f, -.5f, .5f, .5f, 0.07f, false);
		plantStoplightMagenta = new Block("Magenta stoplight" , plantStoplightYellow)
			.setColor(SecaColor.getColor("#FF00FF"));
		plantStoplightMagenta.setBlockAABB(-.5f, -.5f, -.5f, .5f, .5f, 0.07f, false);
		plantStoplightRed = new Block("Red stoplight" , plantStoplightYellow)
			.setColor(SecaColor.getColor("#FF0000"));
		plantStoplightRed.setBlockAABB(-.5f, -.5f, -.5f, .5f, .5f, 0.07f, false);
		plantStoplightGreen = new Block("Green stoplight" , plantStoplightYellow)
			.setColor(SecaColor.getColor("#00FF00"));
		plantStoplightGreen.setBlockAABB(-.5f, -.5f, -.5f, .5f, .5f, 0.07f, false);
		plantStoplightBlue = new Block("Blue stoplight" , plantStoplightYellow)
			.setColor(SecaColor.getColor("#0000FF"));
		plantStoplightBlue.setBlockAABB(-.5f, -.5f, -.5f, .5f, .5f, 0.07f, false);
		
		//Animals
//		animalFish = new Block("Fish", Material.FOOD, Resources.MESH_ID_FISH, Resources.TEX_ID_FISH, SecaColor.getColor("#800000"))
//			.setRotation(90, 0, 0).setMeshAABB(-.2f, -.2f, -.2f, .2f, .2f, .2f).setBlockAABB(-.3f, -.3f, -.3f, .3f, .3f, .3f);
		animalFish = new Block("Fish", Material.FOOD, Resources.MESH_ID_BOX, Resources.TEX_ID_NONE, SecaColor.getColor("#9680ff"))
			.setBlockAABB(-.4f, -.4f, .1f, .4f, .4f, .5f, false);
		agent = new Block("Agent", Material.AGENT, Resources.MESH_ID_BOX, Resources.TEX_ID_NONE, SecaColor.getColor("#A0A0A0"))
			.setBlockAABB(-.1f, -.1f, -.1f, .1f, .1f, .1f, false);
		
	}
}
