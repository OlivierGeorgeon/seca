package gwt.seca.client.agents;

import static gwt.g3d.client.math.MatrixStack.MODELVIEW;

import java.util.Iterator;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import tracing.ITracer;

import com.allen_sauer.gwt.voices.client.Sound;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

import ernest.Ernest;
import ernest.IErnest;
import ernest.Visual100SensorymotorSystem;
import gwt.g2d.client.graphics.Color;
import gwt.g2d.client.graphics.KnownColor;
import gwt.g2d.client.graphics.Surface;
import gwt.seca.client.Block;
import gwt.seca.client.IEntity;
import gwt.seca.client.Renderer;
import gwt.seca.client.Resources;
import gwt.seca.client.SceneManager;
import gwt.seca.client.Seca;
import gwt.seca.client.SoundManager;
import gwt.seca.client.util.CircularGrid;
import gwt.seca.client.util.Pair;
import gwt.seca.client.util.SecaColor;
import gwt.seca.client.util.SecaMath;
import gwt.seca.client.util.Sphere;

public class Ernest11Model extends AbstractModel{
	
	public static enum Version {v10_0, v10_1, v10_2, v10_3};
	
	public static final float ROTATION_STEP = (float)Math.PI/4;
	public static final float TRANSLATION_STEP = 1;
	public static final Color COLOR_WALL = Block.stone.getColor();
	
	//Animatable parts
	public final Vector3f mBodyTrans = new Vector3f();
	public final Vector3f mBodyRot = new Vector3f();
	public final Vector3f mPectoralFinsRot = new Vector3f(); //Rotation of the left fin. Rotation of the right fin is the opposite on the axis X and Z
	public final Vector3f mPelvicFinsRot = new Vector3f(); //Rotation of the left fin. Rotation of the right fin is the opposite
	public final Vector3f mCaudalFinRot = new Vector3f();
	public final Vector3f mDorsalFinRot = new Vector3f();
	//Sounds
	public Sound mSoundBump;
	public Sound mSoundEat;
	
	public Ernest11Model(SceneManager sceneMgr, int num, SoundManager soundMgr) {
		super(sceneMgr, num);
		setSoundManager(soundMgr);
		mSoundEat = mSoundMgr.createMp3("http://liris.cnrs.fr/ideal/demo/sound/goutte_eau.mp3");
		mSoundBump = mSoundMgr.createMp3("http://liris.cnrs.fr/ideal/demo/sound/ressort3.mp3");
		mAnimation = new Ernest11Anim(this);
		mDiscrete = false;
		mLocalBoundingSphere = new Sphere(new Vector3f(), .3f);
	}
	public Ernest11Model(SceneManager sceneMgr, int num) {
		this(sceneMgr, num, null);
	}
	
	/**
	 * Initialize the Ernest agent.
	 */
	public void init() {//v10_3
		
		mErnest = new Ernest();
		mSensorymotorSystem = new Visual100SensorymotorSystem();
		
		// Initialize the Ernest === 
		mErnest.setParameters(5, 4);
		mErnest.setTracer(mTracer);
		mErnest.setSensorymotorSystem(mSensorymotorSystem);
		
		// Ernest's inborn primitive interactions
		mErnest.addInteraction(">", " ",   20); // Move		
		mErnest.addInteraction("^", " ",  -10); // Left toward empty
		mErnest.addInteraction("v", " ",  -10); // Right toward empty

		mSchema = "";
		mStatus = true;
		mStimuliMatrix = new int[Ernest.RESOLUTION_RETINA][8 + 1 + 3];

		//Dashboard widgets
		mPanelWidget = Seca.dashboardMgr.createPanel(null, mName);
		mCycleWidget = Seca.dashboardMgr.createLabel(mPanelWidget, "Cycle");
		mVisionWidget = Seca.dashboardMgr.createSurface(mPanelWidget, "Visual stimuli");
		mTactitionWidget = Seca.dashboardMgr.createSurface(mPanelWidget, "Tactile stimuli");
		mGustationWidget = Seca.dashboardMgr.createSurface(mPanelWidget, "Other stimuli");
		mBundleMapWidget = Seca.dashboardMgr.createSurface(mPanelWidget, "Spatial memory");
		mControlWidget = Seca.dashboardMgr.createFlowPanel(mPanelWidget, "Control");

		initControls();
		mControlWidget.add(mPlayButton);
		mControlWidget.add(mPauseButton);
		mControlWidget.add(mStepButton); 
		mControlWidget.add(mRemoveButton);

	}
	/**
	 * Bind a tracer to the Ernest agent.
	 */
	public void setTracer(ITracer tracer) {
		mTracer = tracer;
	}

	/**
	 * Close the Ernest agent.
	 */
	public void dispose() {
		if (mTracer!=null)
			mTracer.close();
		mErnest = null;
		Seca.dashboardMgr.deleteWidgets(mCycleWidget, mVisionWidget, mTactitionWidget, mGustationWidget, mBundleMapWidget, mControlWidget);
		Seca.dashboardMgr.deletePanel(mPanelWidget);
		mSceneMgr.deleteEntity(mName);
	}
	
	/**
	 * Execute a cognitive step for the agent. 
	 * When the parameter 'elaspsedTime' is not used, this method is equivalent to a 'tick' method.
	 */
	protected void cognitiveLoop(boolean status, double elaspsedTime) 
	{
		if (mTracer != null)
			mTracer.startNewEvent(mCycle);

		mStatus = status;
		
		int[] step = stepErnest(mStatus);
		//step = stepErnest(mStatus);
		mSchema = Character.toString((char)step[0]);
		mImpulsion = step[1];
		mStatus = enactSchema(mSchema, mImpulsion);
		updateDashBoardWidgets();		
	}
	
	protected void drawAgent(Renderer renderer) {
		renderer.enableUniformColor();
		
		int fins = ~(mNumber - 1);
		
		//Transformations T Rzyx S
		//Ernest is a shark
		//1 body (1 sphere)
		//	(2 eyes)
		//	2 pectoral fins
		//	2 pelvic fins
		//	1 caudal fin
		//		2 parts (up/down)
		//	1 dorsal fin
		{ //Cephalothorax
			renderer.setColorUniform(new Vector4f(150/255f, 150/255f, 150/255f, 1f));
			MODELVIEW.push();
			MODELVIEW.translate(mBodyTrans.x, mBodyTrans.y, mBodyTrans.z);
			MODELVIEW.rotateZ((float) Math.toRadians(mBodyRot.z));
			MODELVIEW.rotateY((float) Math.toRadians(mBodyRot.y));
			MODELVIEW.rotateX((float) Math.toRadians(mBodyRot.x));
			MODELVIEW.scale(0.7f/2, 0.3f/2, 0.2f/2);
			renderer.setMatrixUniforms();
			Resources.mMeshList[Resources.MESH_ID_SPHERE].draw();
			{ //Eyes (1 eye = 1 box)
				//Left eye
				renderer.setColorUniform(new Vector4f((float)(mErnest.getAttention() / 65536)/256f, (float)((mErnest.getAttention() & 0xFF00) / 256)/256f, (float)(mErnest.getAttention() & 0xFF)/256f, 1f));
				MODELVIEW.push();
				MODELVIEW.translate(.6f, .6f, 0);
				MODELVIEW.scale(0.2f, 0.2f, 0.4f);
				renderer.setMatrixUniforms();
				Resources.mMeshList[Resources.MESH_ID_SPHERE].draw();
				MODELVIEW.pop();
				//Right eye
				renderer.setColorUniform(new Vector4f((float)(mErnest.getAttention() / 65536)/256f, (float)((mErnest.getAttention() & 0xFF00) / 256)/256f, (float)(mErnest.getAttention() & 0xFF)/256f, 1f));
				MODELVIEW.push();
				MODELVIEW.translate(.6f, -.6f, 0);
				MODELVIEW.scale(0.2f, 0.2f, 0.4f);
				renderer.setMatrixUniforms();
				Resources.mMeshList[Resources.MESH_ID_SPHERE].draw();
				MODELVIEW.pop();
			}
			{ //6 fins (1 fin = 1 hemisphere)
				renderer.setColorUniform(new Vector4f(100/255f, 100/255f, 100/255f, 1f));
				//2 pectoral fins
				MODELVIEW.push();
				MODELVIEW.translate(.1f, 0, 0);
				{
					//Left pectoral fin
					if ((fins & 8) == 8)
					{
						MODELVIEW.push();
						MODELVIEW.translate(0, .9f, -.2f);
						//The order of rotation is reversed because the hemisphere is along the z-axis
						MODELVIEW.rotateX((float) Math.toRadians(mPectoralFinsRot.x - 120));
						//The rotation of about -90° around X inverse the axis Y and Z: Y' = -Z, Z' = Y
						//We reverse the components y and z (and add signs) to make the (-90°)-rotation transparent in the animation
						MODELVIEW.rotateY((float) Math.toRadians(-mPectoralFinsRot.z));
						MODELVIEW.rotateZ((float) Math.toRadians(mPectoralFinsRot.y));
						MODELVIEW.scale(.2f, .2f, 1f);
						renderer.setMatrixUniforms();
						Resources.mMeshList[Resources.MESH_ID_HEMISPHERE].draw();
						MODELVIEW.pop();
					}
					//Right pectoral fin
					if ((fins & 16) == 16)
					{
						MODELVIEW.push();
						MODELVIEW.translate(0, -.9f, -.2f);
						//The order is reversed again but the angle is now about 90°: Y' = Z, Z' = -Y
						MODELVIEW.rotateX((float) Math.toRadians(-mPectoralFinsRot.x + 120));
						MODELVIEW.rotateY((float) Math.toRadians(-mPectoralFinsRot.z));
						MODELVIEW.rotateZ((float) Math.toRadians(-mPectoralFinsRot.y));
						MODELVIEW.scale(.2f, .2f, 1f);
						renderer.setMatrixUniforms();
						Resources.mMeshList[Resources.MESH_ID_HEMISPHERE].draw();
						MODELVIEW.pop();
					}
				}
				MODELVIEW.pop();
				//2 pelvic fins
				MODELVIEW.push();
				MODELVIEW.translate(-.7f, 0, 0);
				{
					//Left pelvic fin
					if ((fins & 1) == 1)
					{
						MODELVIEW.push();
						MODELVIEW.translate(0, .5f, -.2f);
						//The order is reversed again: Y' = -Z, Z' = Y
						MODELVIEW.rotateX((float) Math.toRadians(mPelvicFinsRot.x - 120));
						MODELVIEW.rotateY((float) Math.toRadians(-mPelvicFinsRot.z));
						MODELVIEW.rotateZ((float) Math.toRadians(mPelvicFinsRot.y - 45));
						MODELVIEW.scale(.1f, .1f, .4f);
						renderer.setMatrixUniforms();
						Resources.mMeshList[Resources.MESH_ID_HEMISPHERE].draw();
						MODELVIEW.pop();
					}

					//Right pelvic fin
					if ((fins & 2) == 2)
					{
						MODELVIEW.push();
						MODELVIEW.translate(0, -.6f, -.2f);
						//The order is reversed again: Y' = Z, Z' = -Y
						MODELVIEW.rotateX((float) Math.toRadians(-mPelvicFinsRot.x + 120));
						MODELVIEW.rotateY((float) Math.toRadians(-mPelvicFinsRot.z));
						MODELVIEW.rotateZ((float) Math.toRadians(-mPelvicFinsRot.y - 45));
						MODELVIEW.scale(.1f, .1f, .4f);
						renderer.setMatrixUniforms();
						Resources.mMeshList[Resources.MESH_ID_HEMISPHERE].draw();
						MODELVIEW.pop();
					}
				}
				MODELVIEW.pop();
				//1 caudal fin with 2 parts
				MODELVIEW.push();
				MODELVIEW.translate(-.8f, 0, 0);
				{	
					//Upper part
					MODELVIEW.push();
					//The order is reversed again
					//The rotation of about -90° around Y inverse the axis X and Z: X' = Z, Z' = -X
					//We reverse the components x and z (and add signs) to make the (-90°)-rotation transparent in the animation
					MODELVIEW.rotateY((float) Math.toRadians(mCaudalFinRot.y - 90 + 60));
					MODELVIEW.rotateX((float) Math.toRadians(mCaudalFinRot.z));
					//MODELVIEW.rotateY((float) Math.toRadians(mCaudalFinRot.y + 60));
					MODELVIEW.scale(.4f, .1f, 1f);
					renderer.setMatrixUniforms();
					Resources.mMeshList[Resources.MESH_ID_HEMISPHERE].draw();
					MODELVIEW.pop();
					//Downer part
					MODELVIEW.push();
					//The order is reversed again
					MODELVIEW.rotateY((float) Math.toRadians(mCaudalFinRot.y - 90 - 40));
					MODELVIEW.rotateX((float) Math.toRadians(mCaudalFinRot.z));
					//MODELVIEW.rotateY((float) Math.toRadians(mCaudalFinRot.y - 40));
					MODELVIEW.scale(.3f, .1f, .5f);
					renderer.setMatrixUniforms();
					Resources.mMeshList[Resources.MESH_ID_HEMISPHERE].draw();
					MODELVIEW.pop();
				}
				MODELVIEW.pop();
				{ //1 dorsal fin
					if ((fins & 4) == 4)
					{
						MODELVIEW.push();
						MODELVIEW.translate(0, 0, .9f);
						//MODELVIEW.rotateZ((float) Math.toRadians(mDorsalFinRot.z + 60));
						MODELVIEW.rotateY((float) Math.toRadians(mDorsalFinRot.y - 10));
						MODELVIEW.rotateX((float) Math.toRadians(mDorsalFinRot.x));
						MODELVIEW.scale(.2f, .1f, 1f);
						renderer.setMatrixUniforms();
						Resources.mMeshList[Resources.MESH_ID_HEMISPHERE].draw();
						MODELVIEW.pop();
					}
				}
			}
			MODELVIEW.pop();
		}
	}
	
	/**
	 * Run Ernest one step
	 */
	protected int[] stepErnest(boolean status) 
	{
		// Sense
		
		sense();
		
		// See the environment
		//mRetina = getRetina();
		for (int i = 0; i < Ernest.RESOLUTION_RETINA; i++) {
			mStimuliMatrix[i][0] = mRetina[i].mLeft;
			mStimuliMatrix[i][1] = mRetina[i].mRight.getR();
			mStimuliMatrix[i][2] = mRetina[i].mRight.getG();
			mStimuliMatrix[i][3] = mRetina[i].mRight.getB();
		}
		
		// Kinematic (simulates sensors of body action) 
		if (mSchema.equals(">"))
			mStimuliMatrix[1][8] = (status ? Ernest.STIMULATION_KINEMATIC_FORWARD : Ernest.STIMULATION_KINEMATIC_BUMP);
		else if (mSchema.equals("^"))
			mStimuliMatrix[1][8] = (status ? Ernest.STIMULATION_KINEMATIC_LEFT_EMPTY : Ernest.STIMULATION_KINEMATIC_LEFT_WALL);
		else if (mSchema.equals("v"))
			mStimuliMatrix[1][8] = (status ? Ernest.STIMULATION_KINEMATIC_RIGHT_EMPTY : Ernest.STIMULATION_KINEMATIC_RIGHT_WALL);
		
		// Tactile
		//mSomatoMap = somatoMap();
		for (int i = 0; i < 9; i++)
			mStimuliMatrix[i][9] = mSomatoMap[i];

		// Taste (after the others because the cell can be modified)
		//mPapillae = taste();
		mStimuliMatrix[0][8] = mPapillae;

		//String intention = mErnest.step(mStimuliMatrix);
		return mErnest.step(mStimuliMatrix);
		//return intention;
	}
	
	/**
	 * Sense the environment.
	 */ 
	protected void sense()
	{
		mRetina = getRetina();
		mSomatoMap = somatoMap();
		mPapillae = taste();		
	}
	/**
	 * Generates a retina image from Ernest's view point.
	 * @return The array of colors projected onto the retina.
	 */ 
	protected Pair<Integer, Color>[] getRetina() {
		@SuppressWarnings("unchecked")
		Pair<Integer, Color>[] retina = new Pair[Ernest.RESOLUTION_RETINA];
		float angle = (float) (mOrientation.getZ() - Math.PI/2);
		float angleStep = (float)Math.PI / Ernest.RESOLUTION_RETINA;
		for (int i = 0; i < Ernest.RESOLUTION_RETINA; i++) {
			retina[i] = scanArc(angle, angleStep);
			angle += angleStep;
		}
		return retina;
	}
	/**
	 * Scan an arc from Ernest's viewpoint, starting from the initial angle position and going through the angular span.
	 * Stop scanning at the first singularity found.
	 * @param t The initial angular position (trigonometric/counterclockwise)
	 * @param a The arc's angular span (trigonometric/counterclockwise)
	 * @param d The arc's diameter (the agent's visual range)
	 * @return the color detected. 
	 */
	protected Pair<Integer, Color> scanArc(float t, float a) {
		Pair<Integer, Color> eyeFixation = null;
		float step = a/2;
		int i = Math.round(mPosition.getX());
		int j = Math.round(mPosition.getY());
		for (float angle = t; angle <= t + a + .001; angle += step) {
			float x0 = (float) (mPosition.getX() + 20 * Math.cos(angle));
			float y0 = (float) (mPosition.getY() + 20 * Math.sin(angle)); // Y axis is upwards.
				eyeFixation = rayTrace(mPosition.getX(), mPosition.getY(), x0, y0);
			// We stop when we find a singularity.
			if (eyeFixation.mRight != COLOR_WALL)
				break;
		}
		if (eyeFixation==null)
			return Pair.create(Ernest.INFINITE, COLOR_WALL);
		return eyeFixation;
	}

	/**
	 * Scan the squares that are on a ray from a viewpoint to a target square
	 *  http://playtechs.blogspot.com/2007/03/raytracing-on-grid.html 
	 * @return Distance to the dirty square if any, 1000 if no dirt. 
	 */
	protected Pair<Integer, Color> rayTrace(float x0, float y0, float x1, float y1) {
		float dx = Math.abs(x1 - x0);
		float dy = Math.abs(y1 - y0);
	    int i = (int) Math.round(x0);
	    int j = (int) Math.round(y0);
	    int n = 1;
	    int i_inc, j_inc;
	    float error;
	    int k = Math.round(mPosition.getZ());
	    float cornerTresh = .05f * dx * dy;

	    if (dx == 0) {
	        i_inc = 0;
	        error = Float.POSITIVE_INFINITY;
	    } else if (x1 > x0) {
	        i_inc = 1;
	        n += (int) Math.round(x1) - i;
	        error = (float) (((Math.round(x0) + .5f) - x0) * dy);
	    } else {
	        i_inc = -1;
	        n += i - (int) Math.round(x1);
	        error = (float) ((x0 - (Math.round(x0) - .5f)) * dy);
	    }
	    if (dy == 0) {
	        j_inc = 0;
	        error -= Float.POSITIVE_INFINITY;
	    } else if (y1 > y0) {
	        j_inc = 1;
	        n += (int) Math.round(y1) - j;
	        error -= ((Math.round(y0) + .5f) - y0) * dx;
	    } else {
	        j_inc = -1;
	        n += j - (int) Math.round(y1);
	        error -= (y0 - (Math.round(y0) - .5f)) * dx;
	    }
	    for (; n > 0; --n) 
	    {
	        // move on along the ray
	        if (error > cornerTresh) {
	            j += j_inc;
	            error -= dx;
	        } else if (error < -cornerTresh) {
	            i += i_inc;
	            error += dy;
	        } else {
	        	i += i_inc;
	    		j += j_inc;
	    		error += dy - dx;
	    		--n;
	        }
	    	// Don't go outside the grid
	    	if ((i < 0) || (j < 0) || (i >= mSceneMgr.getWidth()) || (j >= mSceneMgr.getDepth())) 
	    		return Pair.create(Ernest.INFINITE, COLOR_WALL);
	    	
	    	// Examine the block on the ray. Return wall or uninhibited dirty squares.
	    	int blockID = mSceneMgr.getBlockID(i, j, k, mName);
	    	if (Block.blockList[blockID].getMaterial().affordSee()) {
	    		int dist = (int)Math.sqrt(((i-x0)*(i-x0) + (j-y0)*(j-y0)) * 100);
	    		Color color = Block.blockList[blockID].getColor();
	    		return Pair.create(dist, color);
    		}

	    }
		
		return Pair.create(Ernest.INFINITE, COLOR_WALL);
	}
	/**
	 * Taste the square where Ernest is. 
	 * Suck the square if it is food or water. 
	 * @return 0 if nothing, 1 if water, 2 if food. 
	 */
	protected int taste() 
	{
		int taste = Ernest.STIMULATION_GUSTATORY_NOTHING;
		Vector3f point = new Vector3f(DIRECTION_AHEAD);
		point.scale(TACTILE_RADIUS);

		// Sucking water or food if any
		if (affordEat(mPosition)) 
		{
			suck();
			taste = Ernest.STIMULATION_GUSTATORY_FISH;
		}
		// if no food then check for cuddle
		else if (affordCuddle(localToParentRef(point))) // mCuddled
		{
			taste = Ernest.STIMULATION_GUSTATORY_CUDDLE;
			SoundManager.cuddle.play();
		}
//		else if (affordCuddle(localToParentRef(DIRECTION_AHEAD_RIGHT)) ||
//				affordCuddle(localToParentRef(DIRECTION_AHEAD_LEFT)))
//			SoundManager.cuddle.play();

		return taste;
	}
	protected void suck() {
		if (affordEat(mPosition)) {
			mSceneMgr.setBlockID(mPosition, 0);
			mSoundEat.play();
		}
	}
	protected int[] somatoMap() {
		int[] somatoMap = new int[9];
		somatoMap[0] = soma(DIRECTION_BEHIND_RIGHT);
		somatoMap[1] = soma(DIRECTION_RIGHT);
		somatoMap[2] = soma(DIRECTION_AHEAD_RIGHT);
		somatoMap[3] = soma(DIRECTION_AHEAD);
		somatoMap[4] = soma(DIRECTION_AHEAD_LEFT);
		somatoMap[5] = soma(DIRECTION_LEFT);
		somatoMap[6] = soma(DIRECTION_BEHIND_LEFT);
		somatoMap[7] = soma(DIRECTION_BEHIND);
		somatoMap[8] = soma(new Vector3f());
		
		return somatoMap;
	}
	protected int soma(Vector3f direction) {
		int soma = Ernest.STIMULATION_TOUCH_EMPTY;
		Vector3f localPoint = new Vector3f(direction);
		localPoint.scale(TACTILE_RADIUS);
		Vector3f point = localToParentRef(localPoint);
		if (affordTouchSoft(point))
			soma = Ernest.STIMULATION_TOUCH_SOFT;
		if (affordEat(point))
			soma = Ernest.STIMULATION_TOUCH_FISH;
		if (affordCuddle(point))
			soma = Ernest.STIMULATION_TOUCH_AGENT;
		else if (!affordWalk(point))
			soma = Ernest.STIMULATION_TOUCH_WALL;
		return soma;
	}
	
	/**
	 * Enact the primitive schema chosen by Ernest.
	 * @return binary feedback. 
	 */
	protected boolean enactSchema(String schema, int impulsion) 
	{
		// Sucking the squares is automatic
		suck();
		
		boolean status = false;
		
	    if (schema.equals("v"))
			status = turnRight(impulsion);
		else if (schema.equals("^"))
			status = turnLeft(impulsion);
		else if (schema.equals(">"))
			status = stepForward(impulsion);
	    return status;
	}
	
	/**
	 * Turn.
	 * @param angle in degrees. A positive angle will make the agent turn on the left.
	 * @return true if adjacent wall, false if adjacent empty. 
	 */
//	protected boolean turn(float angle) {
//		//mOrientation.z += angle;
//		return affordWalk(localToParentRef(DIRECTION_AHEAD));
//	}
	protected boolean turnLeft(int impulsion) 
	{
		if (mAnimation != null)
		{
			mAnimation.init(Ernest11Anim.ANIM_TURN_LEFT);
			mAnimation.setEndPosition(mPosition);
			//mAnimation.setImpulsion((float)impulsion / 10f * Ernest11Anim.ROTATION_IMPULSION / ((float)Math.PI/4));
			mAnimation.setImpulsion(Ernest11Anim.ROTATION_IMPULSION);
			//mAnimation.setEndOrientation(new Vector3f(0,0, mOrientation.z + ROTATION_STEP ));
			//mAnimation.setEndOrientation(new Vector3f(0,0, mOrientation.z + (float)impulsion / 10f ));
		}
		//return turn(ROTATION_STEP);
		return affordWalk(localToParentRef(DIRECTION_AHEAD_LEFT));
	}
	protected boolean turnRight(int impulsion) {
		if (mAnimation != null)
		{
			mAnimation.init(Ernest11Anim.ANIM_TURN_RIGHT );
			mAnimation.setEndPosition(mPosition);
			//mAnimation.setImpulsion((float)impulsion / 10f * Ernest11Anim.ROTATION_IMPULSION / ((float)Math.PI/4));
			mAnimation.setImpulsion(Ernest11Anim.ROTATION_IMPULSION);
			//mAnimation.setEndOrientation(new Vector3f(0,0, mOrientation.z - ROTATION_STEP ));
			//mAnimation.setEndOrientation(new Vector3f(0,0, mOrientation.z - (float)impulsion / 10f ));
		}
		//return turn(- ROTATION_STEP);
		return affordWalk(localToParentRef(DIRECTION_AHEAD_RIGHT));
	}
	
	/**
	 * Move forward.
	 * @return true if adjacent wall, false if adjacent empty. 
	 */
	protected boolean goForward(float impulsion) 
	{
		if (mAnimation != null)
		{
			mAnimation.init(Ernest11Anim.ANIM_MOVE);
			//mAnimation.setImpulsion(impulsion);
			mAnimation.setImpulsion(Ernest11Anim.TRANSLATION_IMPULSION);
		}	
		return true;
	}
	
	protected boolean stepForward(int impulsion) 
	{
		//return goForward(TRANSLATION_STEP);
		return goForward((float)impulsion / 10f * Ernest11Anim.TRANSLATION_IMPULSION);
	}
	
//	protected Vector3f getNonCollidingLocalTrans(Vector3f localTarget) {
//		Vector3f localTrans = new Vector3f();
//		float s;
//		for(s=.1f; s<=1; s+=.1f) {
//			localTrans.scale(s, localTarget);
//			if (isColliding(localTrans))
//				break;
//		}
//		s -= .1f;
//		localTrans.scale(s, localTarget);
//		return localTrans;
//	}
//	protected boolean isColliding(Vector3f localTrans) {
//		Vector3f point = new Vector3f();
//		point.scaleAdd(mLocalBoundingSphere.getRadius(), DIRECTION_AHEAD, localTrans);
//		if (!affordWalk(localToParentRef(point)))
//			return true;
//		point.scaleAdd(mLocalBoundingSphere.getRadius(), DIRECTION_AHEAD_LEFT, localTrans);
//		if (!affordWalk(localToParentRef(point)))
//			return true;
//		point.scaleAdd(mLocalBoundingSphere.getRadius(), DIRECTION_AHEAD_RIGHT, localTrans);
//		if (!affordWalk(localToParentRef(point)))
//			return true;
//		point.scaleAdd(mLocalBoundingSphere.getRadius(), DIRECTION_LEFT, localTrans);
//		if (!affordWalk(localToParentRef(point)))
//			return true;
//		point.scaleAdd(mLocalBoundingSphere.getRadius(), DIRECTION_RIGHT, localTrans);
//		if (!affordWalk(localToParentRef(point)))
//			return true;
//		return false;
//	}
	protected boolean isBumping(Vector3f localTarget, Vector3f localTrans) {
		Vector3f diff = new Vector3f();
		diff.sub(localTarget, localTrans);
		return (diff.lengthSquared() > 0.01);
	}
	
	protected boolean affordWalk(Vector3f pos) 
	{
		int blockID = mSceneMgr.getBlockID(Math.round(pos.x), Math.round(pos.y), Math.round(pos.z), mName);
		if (blockID == -1)
			return true;
		return Block.blockList[blockID].getMaterial().affordWalk();
	}

	protected boolean affordTouchSoft(Vector3f pos) 
	{
		int blockID = mSceneMgr.getBlockID(Math.round(pos.x), Math.round(pos.y), Math.round(pos.z), mName);
		if (blockID == -1)
			return false;
		return Block.blockList[blockID].getMaterial().affordTouchSoft();
	}

	protected boolean affordEat(Vector3f pos) 
	{
		int blockID = mSceneMgr.getBlockID(Math.round(pos.x), Math.round(pos.y), Math.round(pos.z), mName);
		if (blockID == -1 || blockID == 0)
			return false;
		return Block.blockList[blockID].getMaterial().affordEat();
	}
	
	protected boolean affordCuddle(Vector3f pos) 
	{
	    IEntity entity = mSceneMgr.getEntity(pos, mName);
	    // Only entities and all entities afford cuddle.
		if (entity == null)
			return false;
		else
			return true;
	}

	protected void setCuddled(boolean cuddle)
	{
		mCuddled = cuddle;
	}
	
	protected Vector3f entityCenter(Vector3f pos) 
	{
	    IEntity entity = mSceneMgr.getEntity(pos, mName);
	    // Only entities and all entities afford cuddle.
		if (entity == null)
			return null;
		else
			return entity.getPosition();
	}
	
	protected void updateDashBoardWidgets() {
		mCycleWidget.setText(""+mCycle);
		if (mBundleMapWidget!=null)
			updateBundleMapWidget();
		if (mVisionWidget!=null)
			updateVisionWidget();
		if (mTactitionWidget!=null)
			updateTactitionWidget();
		if (mGustationWidget!=null)
			updateGustationWidget();
	}
	protected void updateVisionWidget() {
		// (0, 0) is top-left, angles are clockwise by default
		mVisionWidget.clear();
		//mVisionWidget.fillBackground(new Color(255, 255, 255));

		Color unamitedColor = KnownColor.GRAY;
		Color[] pixelColor = new Color[Ernest.RESOLUTION_RETINA]; //The arcs are clockwise
		for (int i = 0; i < Ernest.RESOLUTION_RETINA; i++)
			pixelColor[i] = unamitedColor;
		
		if (mErnest != null && mRetina != null) {
			//Eye color
			for (int i = 0; i < Ernest.RESOLUTION_RETINA; i++) {
				if (mRetina[i]!=null)
					pixelColor[i] = mRetina[i].getRight();
			}
		}
		
		//Draw the retina
		Seca.dashboardMgr.drawPie(mVisionWidget, -90, 90, pixelColor);
	}
	protected void updateTactitionWidget() {
		// (0, 0) is top-left
		mTactitionWidget.clear();
		//mTactitionWidget.fillBackground(new Color(0, 255, 0));
		
		Color[][] somatoMapColor = new Color[3][3];
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				somatoMapColor[i][j] = KnownColor.WHITE;
		
		if (mErnest != null && mSomatoMap != null) 
		{
			// Somatomap color
			int [] somatoMap = somatoMap();
			somatoMapColor[2][2] = SecaColor.getColor(somatoMap[0]);
			somatoMapColor[2][1] = SecaColor.getColor(somatoMap[1]);
			somatoMapColor[2][0] = SecaColor.getColor(somatoMap[2]);
			somatoMapColor[1][0] = SecaColor.getColor(somatoMap[3]);
			somatoMapColor[0][0] = SecaColor.getColor(somatoMap[4]);
			somatoMapColor[0][1] = SecaColor.getColor(somatoMap[5]);
			somatoMapColor[0][2] = SecaColor.getColor(somatoMap[6]);
			somatoMapColor[1][2] = SecaColor.getColor(somatoMap[7]);
			somatoMapColor[1][1] = SecaColor.getColor(somatoMap[8]);
		}
		
		//Draw the somatomap
		Seca.dashboardMgr.drawCircularGrid(mTactitionWidget, new CircularGrid<Color>(somatoMapColor));
	}
	protected void updateGustationWidget() 
	{
		Color[][] cellColors = new Color[1][3];		
		for (int i = 0; i < 3; i++)
			cellColors[0][i] = KnownColor.WHITE;

		if (mPapillae == Ernest.STIMULATION_GUSTATORY_FISH)
			cellColors[0][0] = SecaColor.getColor(Ernest.STIMULATION_GUSTATORY_FISH);
			//mGustationWidget.setText("Fish");
		if (mPapillae == Ernest.STIMULATION_GUSTATORY_CUDDLE)
			cellColors[0][1] = SecaColor.getColor(Ernest.STIMULATION_GUSTATORY_CUDDLE);
			//mGustationWidget.setText("Cuddle");
		if (!mStatus)
			cellColors[0][2] = SecaColor.getColor(Ernest.STIMULATION_KINEMATIC_BUMP);
		//else
		//	mGustationWidget.setText(""); // or nothing

		Seca.dashboardMgr.drawRectangularGrid(mGustationWidget, cellColors);
		
	}
	protected void updateBundleMapWidget() {
		// (0, 0) is top-left
		mBundleMapWidget.clear();
		//mTactitionWidget.fillBackground(new Color(0, 255, 0));
		
		Color[][] bundleMapColor = new Color[3][3];
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 3; j++)
				bundleMapColor[i][j] = KnownColor.WHITE;
		
		if (mErnest != null) {
			//Somatomap color
			for (int i = 0; i < 3; i++)
				for (int j = 0; j < 3; j++)
					bundleMapColor[i][j] = SecaColor.getColor(mErnest.getValue(i, j));
		}
		
		//Draw the local space memory
//		Seca.dashboardMgr.drawCircularGrid(mBundleMapWidget, bundleMapColor);
		Seca.dashboardMgr.drawCircularGrid(mBundleMapWidget, new CircularGrid<Color>(bundleMapColor));
	}
	
	//Agent related
	private IErnest mErnest;
	private Visual100SensorymotorSystem mSensorymotorSystem;
	private ITracer mTracer;
	private String mSchema;
	private int mImpulsion;
//	/** The angular field of each eye. */
//	private float mEyeAngle;
	private int[][] mStimuliMatrix;
	Pair<Integer, Color>[] mRetina;
	int mPapillae;
	int [] mSomatoMap;

	//Dashboard Widgets
	private Panel mPanelWidget;
	private Label mCycleWidget;
	private Surface mVisionWidget;
	private Surface mTactitionWidget;
	private Surface mGustationWidget;
	private Surface mBundleMapWidget;
	private Panel mControlWidget;
	
	/**
	 * Value of the diagonal projection in 2D:
	 * 1 for a square diagonal,
	 * 1/sqrt(2) for a circle diagonal.
	 */
	final private float DIAG2D_PROJ = SecaMath.INV_SQRT_2;
	/**
	 * Value of the diagonal projection in 3D:
	 * 1 for a cube diagonal,
	 * 1/sqrt(3) for a sphere diagonal.
	 */
	final private float DIAG3D_PROJ = SecaMath.INV_SQRT_3;
	//Local directions
	public final Vector3f DIRECTION_AHEAD = new Vector3f(1, 0, 0);
	final public Vector3f DIRECTION_BEHIND = new Vector3f(-1, 0, 0);
	final public Vector3f DIRECTION_LEFT = new Vector3f(0, 1, 0);
	final public Vector3f DIRECTION_RIGHT = new Vector3f(0, -1, 0);
	final public Vector3f DIRECTION_AHEAD_LEFT = new Vector3f(DIAG2D_PROJ, DIAG2D_PROJ, 0);
	final public Vector3f DIRECTION_AHEAD_RIGHT = new Vector3f(DIAG2D_PROJ, -DIAG2D_PROJ, 0);
	final public Vector3f DIRECTION_BEHIND_LEFT = new Vector3f(-DIAG2D_PROJ, DIAG2D_PROJ, 0);
	final public Vector3f DIRECTION_BEHIND_RIGHT = new Vector3f(-DIAG2D_PROJ, -DIAG2D_PROJ, 0);
	
	// Absolute directions in Cartesian coordinates (0,0) bottom left.
	final protected Vector3f DIRECTION_NORTH = new Vector3f(0, 1, 0);
	final protected Vector3f DIRECTION_NORTHEAST = new Vector3f(DIAG2D_PROJ, DIAG2D_PROJ, 0);
	final protected Vector3f DIRECTION_EAST = new Vector3f(1, 0, 0);
	final protected Vector3f DIRECTION_SOUTHEAST = new Vector3f(DIAG2D_PROJ, -DIAG2D_PROJ, 0);
	final protected Vector3f DIRECTION_SOUTH = new Vector3f(0, -1, 0);
	final protected Vector3f DIRECTION_SOUTHWEST = new Vector3f(-DIAG2D_PROJ, -DIAG2D_PROJ, 0);
	final protected Vector3f DIRECTION_WEST = new Vector3f(-1, 0, 0);
	final protected Vector3f DIRECTION_NORTHWEST = new Vector3f(-DIAG2D_PROJ, DIAG2D_PROJ, 0);

}
