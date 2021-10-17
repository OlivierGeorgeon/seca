package gwt.seca.client;

/**
 * A block's material defines what interaction Ernest can have with this block.
 * @author Olivier Voisin
 */
public class Material 
{
	
	/**
	 * Constructs a material from another material.
	 * @param name The material's name.
	 * @param material The other material.
	 */
	public Material(String name, Material material) {
		mName = name;
		if (material != null) {
			mAffordWalk = material.affordWalk();
			mAffordEat = material.affordEat();
			mAffordSee = material.affordSee();
			mAffordTouchSoft = material.affordTouchSoft();
		} else {
			mAffordWalk = false;
			mAffordEat = false;
			mAffordSee = false;
			mAffordTouchSoft = false;
		}
	}
	/**
	 * Construct a material from scratch.
	 * @param name The material's name.
	 */
	public Material(String name) {
		this(name, null);
	}
	
	/**
	 * @return The material's name.
	 */
	public String getName() {
		return mName;
	}
	/**
	 * Defines if this material affords walking.
	 * @param walk yes/no
	 * @return This material.
	 */
	public Material affordWalk(boolean walk) {
		mAffordWalk = walk;
		return this;
	}
	/**
	 * @return true if this material affords walking.
	 */
	public boolean affordWalk() {
		return mAffordWalk;
	}
	/**
	 * Defines if this material affords seeing.
	 * @param see yes/no
	 * @return This material.
	 */
	public Material affordSee(boolean see) {
		mAffordSee = see;
		return this;
	}
	/**
	 * @return True if this material affords seeing.
	 */
	public boolean affordSee() {
		return mAffordSee;
	}
	/**
	 * Defines if this material affords eating.
	 * @param eatable yes/no
	 * @return This material.
	 */
	public Material affordEat(boolean eatable) {
		mAffordEat = eatable;
		return this;
	}
	/**
	 * @return true if this material affords eating.
	 */
	public boolean affordEat() {
		return mAffordEat;
	}
	
	/**
	 * Defines if this material affords walking.
	 * @param soft yes/no
	 * @return This material.
	 */
	public Material affordTouchSoft(boolean soft) {
		mAffordTouchSoft = soft;
		return this;
	}
	/**
	 * @return True if this material affords touching soft.
	 */
	public boolean affordTouchSoft() {
		return mAffordTouchSoft;
	}
	/**
	 * Defines if this material affords cuddling.
	 * @param cuddle yes/no
	 * @return This material.
	 */
	public Material affordCuddle(boolean cuddle) {
		mAffordCuddle = cuddle;
		return this;
	}
	/**
	 * @return true if this material affords cuddling.
	 */
	public boolean affordCuddle() {
		return mAffordCuddle;
	}
	
	private String mName;
	private boolean mAffordWalk;
	private boolean mAffordSee;
	private boolean mAffordEat;
	private boolean mAffordTouchSoft;
	private boolean mAffordCuddle;
	
	/**
	 * WALL material.
	 * (see).
	 */
	public static final Material WALL;
	/**
	 * AIR material.
	 * (walk).
	 */
	public static final Material AIR;
	/**
	 * DIRT material.
	 * (see, walk, soft).
	 */
	public static final Material DIRT;
	/**
	 * FOOD material.
	 * (see, walk, eat).
	 */
	public static final Material FOOD;
	/**
	 * AGENT material.
	 * (see, cuddle).
	 */
	public static final Material AGENT;
	
	static {
		WALL = new Material("Wall").affordSee(true);
		AIR = new Material("Air").affordWalk(true);
		DIRT = new Material("Dirt").affordSee(true).affordWalk(true).affordTouchSoft(true);
		FOOD = new Material("Food").affordSee(true).affordWalk(true).affordEat(true);
		AGENT = new Material("Agent").affordSee(true).affordWalk(true).affordCuddle(true);
	}
}
