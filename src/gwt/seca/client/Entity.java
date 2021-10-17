package gwt.seca.client;

import gwt.seca.client.util.AxisAlignedBox;

import javax.vecmath.Vector3f;

public class Entity implements IEntity {
	
	public final String mName;
	
	public Entity(String name) {
		mName = name;
	}
	//TODO

	@Override
	public void update(double elapsedTime) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(Renderer renderer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Vector3f getPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AxisAlignedBox getAABB() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Vector3f localToParentRef(Vector3f localVec) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean isInCell(int i, int j, int k) {
		return false;
	}
	
	public boolean overlap(Vector3f position) 
	{
		return false;
	}
	
	public String getName()
	{
		return mName;
	}
	
}
