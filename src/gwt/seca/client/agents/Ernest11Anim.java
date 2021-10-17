package gwt.seca.client.agents;

import javax.vecmath.Vector3f;

public class Ernest11Anim extends AbstractAnim {

	final static int ANIM_MOVE = 1;
	final static int ANIM_BUMP = 2;
	final static int ANIM_TURN_LEFT = 3;
	final static int ANIM_TURN_RIGHT = 4;

	final static float TRANSLATION_IMPULSION = .15f; // .13f
	final static float TRANSLATION_FRICTION = .90f; // .95f
	
	final static float ROTATION_IMPULSION = (float)Math.toRadians(7f); // degrees   . 5.5f
	final static float ROTATION_FRICTION = .9f; // .95f
	
	public Ernest11Anim(Ernest11Model model) {
		super(model);
		mModel = model;
	}
	
	protected void startImpl(Vector3f trans, Vector3f rot) {
		float dist = trans.length();
		float rotZ = rot.getZ();
		switch (mAnimID) {
		case ANIM_MOVE:
			mModel.mTranslation.add(new Vector3f(TRANSLATION_IMPULSION , 0, 0));
			//mModel.mTranslation.add(new Vector3f(mImpulsion , 0, 0));
			mNumberOfCycles = 0;
			mExtremeBodyTrans.set(dist, 0, 0);
			mExtremeBodyRot.set(0, 0, 0);
			mExtremePectoralFinsRot.set(30, 0, 0);
			mExtremePelvicFinsRot.set(0, 0, 0);
			mExtremeCaudalFinRot.set(0, 0, 0);
			mExtremeDorsalFinRot.set(0, 0, 0);
			break;
		case ANIM_BUMP:
			mNumberOfCycles = 1;
			mExtremeBodyTrans.set(0.2f, 0, 0);
			mExtremeBodyRot.set(0, 20, 0);
			mExtremePectoralFinsRot.set(0, 0, 0);
			mExtremePelvicFinsRot.set(0, 0, 0);
			mExtremeCaudalFinRot.set(0, 20, 0);
			mExtremeDorsalFinRot.set(0, 0, 0);
			mSound = mModel.mSoundBump;
			mSoundStart = 0;
			break;
		case ANIM_TURN_LEFT:
			//mModel.mRotation.add(new Vector3f(0, 0, ROTATION_IMPULSION));
			mModel.mRotation.add(new Vector3f(0, 0, mImpulsion));
			mNumberOfCycles = 0;
			mExtremeBodyTrans.set(0, 0, 0);
			mExtremeBodyRot.set(0, 0, rotZ);
			mExtremePectoralFinsRot.set(0, 0, 0);
			mExtremePelvicFinsRot.set(0, 0, 0);
			mExtremeCaudalFinRot.set(0, 0, - rotZ);//-30);
			mExtremeDorsalFinRot.set(0, 0, 0);
			break;
		case ANIM_TURN_RIGHT:
			//mModel.mRotation.add(new Vector3f(0, 0, - ROTATION_IMPULSION));
			mModel.mRotation.add(new Vector3f(0, 0, - mImpulsion));
			mNumberOfCycles = 0;
			mExtremeBodyTrans.set(0, 0, 0);
			mExtremeBodyRot.set(0, 0, rotZ);
			mExtremePectoralFinsRot.set(0, 0, 0);
			mExtremePelvicFinsRot.set(0, 0, 0);
			mExtremeCaudalFinRot.set(0, 0, - rotZ);//30);
			mExtremeDorsalFinRot.set(0, 0, 0);
			break;
		default:
			mAnimID = 0;
			mModel = null;
		}
	}
	
	public void updateImpl(float scale, float scale2) {
		//mModel.mPosition.set(mStartPosition);
		//mModel.mOrientation.set(mStartRotation);
		//Vector3f trans = new Vector3f(mExtremeBodyTrans);
		//trans.scale(scale);
		//if (!mModel.isColliding(trans))
		//	mModel.mPosition.set(mModel.localToParentRef(trans));
		//mModel.mOrientation.scaleAdd(scale, mExtremeBodyRot, mStartRotation);
		mModel.mPectoralFinsRot.scale(scale, mExtremePectoralFinsRot);
		mModel.mPelvicFinsRot.scale(scale, mExtremePelvicFinsRot);
		mModel.mCaudalFinRot.scale(scale, mExtremeCaudalFinRot);
		mModel.mDorsalFinRot.scale(scale, mExtremeDorsalFinRot);
	}
	
	public boolean updateImpulse(int elapsedSteps)
	{
		boolean status = true;

		for (int i = 1; i <= elapsedSteps; i++)
		{			
			mModel.mPosition.set(mModel.localToParentRef(mModel.mTranslation));
			mModel.mOrientation.z += mModel.mRotation.z;
			if (mModel.mOrientation.z < - Math.PI) mModel.mOrientation.z += 2 * Math.PI;
			if (mModel.mOrientation.z > Math.PI) mModel.mOrientation.z -= 2 * Math.PI;

			// Bumping ====

			float HBradius = mModel.mLocalBoundingSphere.getRadius();
			
			// Stay away from north wall
			Vector3f point = new Vector3f(mModel.DIRECTION_NORTH);
			point.scaleAdd(HBradius, mModel.mPosition);
			if (!mModel.affordWalk(point))
			{
				if (mModel.mOrientation.z > (float)Math.PI/4 && mModel.mOrientation.z < 3*(float)Math.PI/4)
					// It counts as a bump only if the angle is closer to perpendicular plus or minus PI/4
					status = false;
				mModel.mPosition.y = Math.round(point.y) - 0.5f - HBradius;
			}
			// Stay away from east wall
			point = new Vector3f(mModel.DIRECTION_EAST);
			point.scaleAdd(HBradius, mModel.mPosition);
			if (!mModel.affordWalk(point))
			{
				if (mModel.mOrientation.z > - (float)Math.PI/4 && mModel.mOrientation.z < (float)Math.PI/4) 
					status = false;
				mModel.mPosition.x = Math.round(point.x) - 0.5f - HBradius;
			}
			// Stay away from south wall
			point = new Vector3f(mModel.DIRECTION_SOUTH);
			point.scaleAdd(HBradius, mModel.mPosition);
			if (!mModel.affordWalk(point))
			{
				if (mModel.mOrientation.z < - (float)Math.PI/4 && mModel.mOrientation.z > - 3 *(float)Math.PI/4)
					status = false;
				mModel.mPosition.y = Math.round(point.y) + 0.5f + HBradius;
			}
			// Stay away from west wall
			point = new Vector3f(mModel.DIRECTION_WEST);
			point.scaleAdd(HBradius, mModel.mPosition);
			if (!mModel.affordWalk(point))
			{
				if (mModel.mOrientation.z > 3*(float)Math.PI/4 || mModel.mOrientation.z < - 3*(float)Math.PI/4)
					status = false;
				mModel.mPosition.x = Math.round(point.x) + 0.5f + HBradius;
			}
			// Stay away from ahead left wall
			Vector3f localPoint = new Vector3f(mModel.DIRECTION_AHEAD_LEFT);
			localPoint.scale(HBradius);
			point = mModel.localToParentRef(localPoint);
			if (!mModel.affordWalk(point))
				mModel.keepDistance(mModel.mPosition, mModel.cellCenter(point), HBradius + .5f);
			
			// Stay away from Ahead right wall
			localPoint = new Vector3f(mModel.DIRECTION_AHEAD_RIGHT);
			localPoint.scale(HBradius);
			point = mModel.localToParentRef(localPoint);
			if (!mModel.affordWalk(point))
				mModel.keepDistance(mModel.mPosition, mModel.cellCenter(point), HBradius + .5f);
			
			// Northeast
			point = new Vector3f(mModel.DIRECTION_NORTHEAST);
			point.scaleAdd(HBradius, mModel.mPosition);
			if (!mModel.affordWalk(point))
				mModel.keepDistance(mModel.mPosition, mModel.cellCenter(point), HBradius + .5f);
			// Southeast
			point = new Vector3f(mModel.DIRECTION_SOUTHEAST);
			point.scaleAdd(HBradius, mModel.mPosition);
			if (!mModel.affordWalk(point))
				mModel.keepDistance(mModel.mPosition, mModel.cellCenter(point), HBradius + .5f);
			// Southwest
			point = new Vector3f(mModel.DIRECTION_SOUTHWEST);
			point.scaleAdd(HBradius, mModel.mPosition);
			if (!mModel.affordWalk(point))
				mModel.keepDistance(mModel.mPosition, mModel.cellCenter(point), HBradius + .5f);
			// Northwest
			point = new Vector3f(mModel.DIRECTION_NORTHWEST);
			point.scaleAdd(HBradius, mModel.mPosition);
			if (!mModel.affordWalk(point))
				mModel.keepDistance(mModel.mPosition, mModel.cellCenter(point), HBradius + .5f);

			// Stay away from agent ahead
			localPoint = new Vector3f(mModel.DIRECTION_AHEAD);
			localPoint.scale(HBradius);
			point = mModel.localToParentRef(localPoint);
			if (mModel.affordCuddle(point))
			{
				mModel.keepDistance(mModel.mPosition, mModel.entityCenter(point), 2 * HBradius ); // Allow some overlap
				if (!mModel.mCuddled)
					mModel.mTranslation.scale(.5f); // slowing down makes it look more like cuddling.
				mModel.setCuddled(true);
			}
			
			// Apply friction to the speed vectors
			mModel.mTranslation.scale(TRANSLATION_FRICTION);
			mModel.mRotation.scale(ROTATION_FRICTION);
		}
		
		return status;

	}
	
	public void stopImpl() 
	{
		// Reset translation and rotation speed
		//mModel.mTranslation = new Vector3f();
		//mModel.mRotation = new Vector3f();
		
		mModel.mPectoralFinsRot.set(0, 0, 0);
		mModel.mPelvicFinsRot.set(0, 0, 0);
		mModel.mCaudalFinRot.set(0, 0, 0);
		mModel.mDorsalFinRot.set(0, 0, 0);
	}
	
	protected Ernest11Model mModel;
	private Vector3f mExtremeBodyTrans = new Vector3f();
	private Vector3f mExtremeBodyRot = new Vector3f();
	private Vector3f mExtremePectoralFinsRot = new Vector3f(); //Rotation of the left fin. Rotation of the right fin is the opposite on the axis X and Z
	private Vector3f mExtremePelvicFinsRot = new Vector3f(); //Rotation of the left fin. Rotation of the right fin is the opposite
	private Vector3f mExtremeCaudalFinRot = new Vector3f();
	private Vector3f mExtremeDorsalFinRot = new Vector3f();
}
