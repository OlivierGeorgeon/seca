package gwt.seca.client.agents;

import static gwt.g3d.client.math.MatrixStack.MODELVIEW;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

import gwt.seca.client.IEntity;
import gwt.seca.client.Renderer;
import gwt.seca.client.SceneManager;
import gwt.seca.client.SoundManager;
import gwt.seca.client.util.AxisAlignedBox;
import gwt.seca.client.util.Sphere;

public abstract class AbstractModel implements IEntity {
	
	public static enum Version {};
	
	public AbstractModel(SceneManager sceneMgr, int num) {
		mSceneMgr = sceneMgr;
		mNumber = num;
		mName = "Ernest " + num;
		mSoundMgr = null;
		mLocalAABB = new AxisAlignedBox(-.5f, -.5f, -.5f, .5f, .5f, .5f);
		mLocalBoundingSphere = new Sphere(new Vector3f(), BOUNDING_RADIUS);
		mPosition = new Vector3f();
		mOrientation = new Vector3f();
		mTranslation = new Vector3f();
		mRotation = new Vector3f();
		mAnimation = null;
		mCycle = 1;
		mLastStepTime = 0;
		mStepPeriod = 400;
		mDimensions = 2;
		mDiscrete = true;
	}
	abstract public void init();
	abstract public void dispose();
	
	/**
	 * Update the agent when the environment is refreshed (environment tick).
	 * @param elapsedTime
	 */
	public void update(double elaspsedTime) 
	{
		double currTime = System.currentTimeMillis();
		
		// If the step period elapsed then it is a new cognitive loop.
		if (currTime - mLastStepTime > mStepPeriod) 
		{
			if (mAnimation != null) 
				mAnimation.stop();
			mLastStepTime = currTime;
			
			if (mControlMode == CONTROL_PLAY || mControlMode == CONTROL_STEP)
			{
				// Run a new cognitive loop.
				cognitiveLoop(mStatus, elaspsedTime);
				mCycle++;
				if (mAnimation != null && mAnimation.isStarted())
				{
					mStatus = true;
					mCuddled = false;
					mAnimation.start();
				}
				if (mControlMode == CONTROL_STEP)
					mControlMode = CONTROL_PAUSE;
			}

			// If Pause mode then update the dashboard here because the agent is not updated. 
			if (mControlMode == CONTROL_PAUSE)
			{
				sense();
				updateDashBoardWidgets();
			}
		} 
		
		// Update the agent's animation
		if (mAnimation != null)
		{
			boolean resultStatus = mAnimation.update(currTime-mLastStepTime);
			if (!resultStatus) mStatus = false; // if it has bumped at some point in its course.
		}
	}
	abstract protected void updateDashBoardWidgets() ;
	
	abstract protected void cognitiveLoop(boolean status, double elaspsedTime);
	
	abstract protected void sense();
	
	public void render(Renderer renderer) {
		//Order of transformations in OpenGL
		MODELVIEW.push();
		////Move entity to its position
		MODELVIEW.translate(mPosition.x, mPosition.y, mPosition.z);
		////Rotate entity around its origin
		MODELVIEW.rotate(mOrientation.z, 0, 0, 1); //+Pi/2 so the agent face Y when Theta == 0
		////Draw entity
		drawAgent(renderer);
		MODELVIEW.pop();
	}
	abstract protected void drawAgent(Renderer renderer);
	
	public void setSoundManager(SoundManager soundMgr) {
		mSoundMgr = soundMgr;
	}
	public SoundManager getSoundManager() {
		return mSoundMgr;
	}
	
	public AxisAlignedBox getAABB() {
		AxisAlignedBox AABB = new AxisAlignedBox(mLocalAABB);
		AABB.translate(mPosition);
		return AABB;
	}
	public AxisAlignedBox getLocalAABB() {
		return mLocalAABB;
	}
	
	//abstract protected boolean turn(float angle);
	//abstract protected boolean goForward(float distance);
	
	public void setPosition(Vector3f pos) {
		mPosition.set(pos);
	}
	public void setPosition(Point3i pos) {
		mPosition.setX(pos.getX());
		mPosition.setY(pos.getY());
		mPosition.setZ(pos.getZ());
	}
	public Vector3f getPosition() {
		return new Vector3f(mPosition);
	}
	public void setOrientation(Vector3f rot) {
		mOrientation.set(rot);
	}
	public Vector3f getOrientation() {
		return new Vector3f(mOrientation);
	}
	public double getStepPeriod() {
		return mStepPeriod;
	}
	
	public Vector3f localToParentRef(Vector3f localVec) {
		Matrix3f rot = new Matrix3f();
		if (mDimensions == 2) {
			rot.rotZ(mOrientation.z);
		} else if (mDimensions == 3) {
			if (mOrientation.x == 0) {
				rot.rotY(mOrientation.y);
				Matrix3f rotZ = new Matrix3f();
				rotZ.rotZ(mOrientation.z);
				rot.mul(rotZ);
			} else {
				rot.rotX(mOrientation.x);
				Matrix3f rotY = new Matrix3f();
				rotY.rotY(mOrientation.y);
				rot.mul(rotY);
				Matrix3f rotZ = new Matrix3f();
				rotZ.rotZ(mOrientation.z);
				rot.mul(rotZ);
			}
		} else {
			rot.setIdentity();
		}
		Vector3f parentVec = new Vector3f();
		rot.transform(localVec, parentVec);
		parentVec.add(mPosition);
		return parentVec;
	}
	
	public boolean isInCell(int i, int j, int k)
	{
		if (i == Math.round(mPosition.getX()) && j == Math.round(mPosition.getY()) && k == Math.round(mPosition.getZ()))
			return true;
		else
			return false;
	}
	
	public boolean overlap(Vector3f position) 
	{
		Vector3f dist = new Vector3f(position);
		dist.sub(mPosition);
		if (dist.length() < mLocalBoundingSphere.getRadius() )
		//if (dist.length() < 1 ) // check a little outside the bound.
			return true;
		else
			return false;
	}

	
	public Vector3f cellCenter(Vector3f position)
	{
		Vector3f cellCenter = new Vector3f(Math.round(position.getX()), Math.round(position.getY()), Math.round(position.getZ()));
		return cellCenter;
	}
	
	public void keepDistance(Vector3f position, Vector3f point, float distance)
	{
		if (point != null)
		{
			Vector3f toPoint = new Vector3f(point);
			toPoint.sub(position);
			if (toPoint.length() < distance)
			{
				//position.add(toPoint);
				position.set(point);
				toPoint.normalize();
				toPoint.scale(- distance);
				position.add(toPoint);
			}
		}
	}
	
	public String getName()
	{
		return mName;
	}
	
	protected void initControls()
	{
		mPlayButton = new Button("Play", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				mPlayButton.setEnabled(false);
				mPauseButton.setEnabled(true);
				mStepButton.setEnabled(true);
				mRemoveButton.setEnabled(false);
				mControlMode = CONTROL_PLAY;
			}
		});
		mPlayButton.setEnabled(false);
		mPauseButton = new Button("Pause", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				mPlayButton.setEnabled(true);
				mPauseButton.setEnabled(false);
				mStepButton.setEnabled(true);
				mRemoveButton.setEnabled(true);
				mControlMode = CONTROL_PAUSE;
			}
		});
		mStepButton = new Button("Step", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				mPlayButton.setEnabled(true);
				mPauseButton.setEnabled(false);
				mStepButton.setEnabled(true);
				mControlMode = CONTROL_STEP;
			}
		});
		mRemoveButton = new Button("Terminate", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				mPlayButton.setEnabled(false);
				mPauseButton.setEnabled(false);
				mStepButton.setEnabled(false);
				mRemoveButton.setEnabled(false);
				dispose();
			}
		});
		mRemoveButton.setEnabled(false);

	}
	
	//Scene related
	protected String mName;
	protected int mNumber;
	protected SceneManager mSceneMgr;
	protected SoundManager mSoundMgr;
	protected boolean mStatus;
	protected boolean mCuddled;

	//Agent related
	protected Vector3f mPosition;
	protected Vector3f mOrientation; // radian
	protected AxisAlignedBox mLocalAABB; //Local Axis Aligned Bounding Box
	protected Sphere mLocalBoundingSphere;
	protected boolean mDiscrete; //Or continuous
	protected int mDimensions;
	
	/** The translation speed.*/
	protected Vector3f mTranslation = new Vector3f(0,0,0);
	/** The angular rotation speed (radian)*/
	protected Vector3f mRotation = new Vector3f(0,0,0);

	// Animation
	protected AbstractAnim mAnimation;
	// Update
	protected int mCycle;
	protected double mLastStepTime;
	protected double mStepPeriod;

	// Controls
	protected Button mPlayButton;
	protected Button mPauseButton;
	protected Button mStepButton;
	protected Button mRemoveButton;

	public static final int CONTROL_PLAY = 0;
	public static final int CONTROL_PAUSE = 1;
	public static final int CONTROL_STEP = 2;
	
	public static final float BOUNDING_RADIUS = .4f;
	public static final float TACTILE_RADIUS = .8f;

	protected int mControlMode = CONTROL_PLAY;

	
}
