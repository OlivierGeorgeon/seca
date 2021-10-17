package gwt.seca.client.agents;

import gwt.seca.client.SoundManager;

import javax.vecmath.Vector3f;

import com.allen_sauer.gwt.voices.client.Sound;

public abstract class AbstractAnim {
	
	final static int ANIM_NONE = 0;
	
	public AbstractAnim(AbstractModel model) {
		mModel = model;
	}
	
	public void init(int animID) {
		if (animID == 0 || mModel == null)
			return;
		//System.out.println("Animation: init");
		mAnimID = animID;
		mAnimStep = 0;
		mAnimStepPeriod = mModel.getStepPeriod()/(float) mAnimMaxStep; // 500 / 8
		mStartPosition = mModel.getPosition();
		mStartRotation = mModel.getOrientation();
		mSound = null;
		mSoundStart = 0;
		mSoundPlayed = false;
	}

	public void start() {
		if (mAnimID == 0 || mModel == null )//|| mAnimStep>0)
			return;
		//mEndPosition = mModel.getPosition();
		//mEndOrientation = mModel.getRotation();
		mModel.setPosition(mStartPosition);
		mModel.setOrientation(mStartRotation);
		//mAnimStep++;
		Vector3f trans = new Vector3f();
		trans.sub(mEndPosition, mStartPosition);
		Vector3f rot = new Vector3f();
		rot.sub(mEndOrientation, mStartRotation);
		startImpl(trans, rot);
		if (mSoundStart==0) {
			mSoundStartingStep = 0;
			if (mSound != null) {
				mSound.play();
				mSoundPlayed = true;
			}
		} else {
			if (mSoundStart<0) mSoundStart = 0;
			if (mSoundStart>1) mSoundStart = 1;
			mSoundStartingStep = (int) (mSoundStart*mAnimMaxStep);
		}
	}
	abstract protected void startImpl(Vector3f trans, Vector3f rot);
	
	public boolean update(double elapsedTime) 
	{
		boolean status = true;
		if (mAnimID == 0 || mModel == null)
			return status;
		//System.out.println("Animation: update");
		int animStep = (int) Math.round(elapsedTime/mAnimStepPeriod);
		if (animStep >= mAnimStep) 
		{
			int elapsedSteps = animStep - mAnimStep;
			mAnimStep = animStep;
			if (mSound != null && mAnimStep>=mSoundStartingStep && !mSoundPlayed) 
			{
				mSound.play();
				mSoundPlayed = true;
			}
			float s;
			if (mNumberOfCycles == 0) 
			{
				//The animation is not a cycle. (translation, rotations)
				s = animStep/(float) mAnimMaxStep; // 8 max steps
			}
			else 
			{
				//The animation is one or more cycles (bump).
				int stepsPerCycle = mAnimMaxStep/mNumberOfCycles;
				//If the cycles are done, it stays at the end of the animation
				if (animStep > stepsPerCycle*mNumberOfCycles) 
					s = 1;
				else {
					//Compute the step number in the current cycle
					int cycleStep = animStep%stepsPerCycle;
//					//Long and explicit version to compute s:
//					if (cycleStep <= stepsPerCycle/2) {
//						//Forward part of the cycle
//						int forwardStep = cycleStep;
//						s = 2*forwardStep/(float) stepsPerCycle;
//					} else {
//						//Backward part of the cycle
//						int backwardStep = cycleStep-(stepsPerCycle/2);
//						s = 1-(2*backwardStep/(float) stepsPerCycle);
//					}
					//Short version:
					s = 1 - Math.abs(2*(cycleStep/(float) stepsPerCycle)-1);
				}
			}
			//int animLegCycle = Math.round(animStep/(float) mNumberOfLegCycles);
			//Second scale:
			float s2;
			if (mNumberOfCycles2 == 0) {
				//The animation is not a cycle.
				s2 = animStep/(float) mAnimMaxStep;
			} else {
				//The animation is one or more cycles.
				int stepsPerCycle = mAnimMaxStep/mNumberOfCycles2;
				//If the cycles are done, it stops
				if (animStep > stepsPerCycle*mNumberOfCycles2) 
					s2 = 1;
				else {
					//Compute the step number in the current cycle
					int cycleStep = animStep%stepsPerCycle;
					//Short version:
					s2 = 1 - Math.abs(2*(cycleStep/(float) stepsPerCycle)-1);
				}
			}
			updateImpl(s, s2);
			status = updateImpulse(elapsedSteps);
		}
		return status;
	}
	
	abstract protected void updateImpl(float scale, float scale2);
	
	protected boolean updateImpulse(int elapsedSteps)
	{
		return true;
	}
	
	public void stop() {
		if (mAnimID == 0 || mModel == null)
			return;
		mAnimID = 0;
		//mModel.setPosition(mEndPosition);
		//mModel.setRotation(mEndOrientation);
		stopImpl();
	}
	abstract protected void stopImpl();
	
	public boolean isStarted() {
		return (mAnimID>ANIM_NONE);
	}
	
	public void setEndPosition(Vector3f endPosition)
	{
		mEndPosition = endPosition;
	}
	public void setEndOrientation(Vector3f endOrientation)
	{
		mEndOrientation = endOrientation;
	}
	
	public void setImpulsion(float impulsion)
	{
		mImpulsion = impulsion;
	}
		
	protected AbstractModel mModel;
	protected int mAnimID = 0;
	protected int mAnimMaxStep = 8; //8;
	protected int mAnimStep = 0;
	protected double mAnimStepPeriod = 0;
	protected int mNumberOfCycles;
	protected int mNumberOfCycles2;
	protected Sound mSound;
	protected float mSoundStart; //In range 0 to 1
	protected float mImpulsion;
	private int mSoundStartingStep;
	private boolean mSoundPlayed;
	protected Vector3f mStartPosition = new Vector3f();
	protected Vector3f mStartRotation = new Vector3f();
	private Vector3f mEndPosition = new Vector3f();
	private Vector3f mEndOrientation = new Vector3f();

}
