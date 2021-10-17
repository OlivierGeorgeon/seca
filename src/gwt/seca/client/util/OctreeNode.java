package gwt.seca.client.util;


import java.util.ArrayList;

public class OctreeNode {
	
	public OctreeNode(OctreeNode parent) {
		mParent = parent;
		if (parent==null) {
			mNodeLevel = 0;
		} else {
			mNodeLevel = parent.getLevel()+1;
			mMaxLevel = parent.getMaxLevel();
		}
		mChilds = null;
	}
	public OctreeNode() {
		this((short) 5);
	}
	public OctreeNode(short maxLevel) {
		this(null);
		mMaxLevel = maxLevel;
	}
	public int getLevel() {
		return mNodeLevel;
	}
	public short getMaxLevel() {
		return mMaxLevel;
	}
	public boolean isLeave() {
		return mIsLeave;
	}
	public void addObject(SceneNode sceneNode) {
		sceneNode.getClass().getName();
		if (mIsLeave) {
			mSceneNodeList.add(sceneNode);
			if (mSceneNodeList.size()>8 && mNodeLevel<mMaxLevel) {
				split();
			}
		} else {
			int i, j, k;
			i = 0; j = 0; k = 0;
			//TODO
			mChilds[i][j][k].addObject(sceneNode);
		}
	}
	private void split() {
		mIsLeave = false;
		mChilds = new OctreeNode[2][2][2];
	}
	
	private OctreeNode mParent;
	private int mNodeLevel;
	private short mMaxLevel;
	private boolean mIsLeave;
	private OctreeNode[][][] mChilds; //
	private ArrayList<SceneNode> mSceneNodeList; //Block, entity
}
