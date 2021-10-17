package gwt.seca.client;

import static gwt.g3d.client.math.MatrixStack.MODELVIEW;
import static gwt.g3d.client.math.MatrixStack.PROJECTION;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import gwt.g2d.client.graphics.Color;
import gwt.g3d.client.camera.AbstractCamera;
import gwt.seca.client.agents.AbstractModel;
import gwt.seca.client.util.AxisAlignedBox;
import gwt.seca.client.util.Plane;
import gwt.seca.client.util.Ray;
import gwt.seca.client.util.SecaMath;
import gwt.seca.client.util.Sphere;

public class Camera extends AbstractCamera {
	
	public final String mName;
	
	public Camera(String name) {
		mName = name;
		
		setFov(45);
		setAspect(1);
		setNear(0.1f);
		setFar(100);
		mViewport = false;
	}
	public void setSceneMgr(SceneManager sceneMgr) {
		mSceneMgr = sceneMgr;
	}
	public void setRenderer(Renderer renderer) {
		mRenderer = renderer;
		setAspect(mRenderer.getAspectRatio());
		if (!mViewport) {
			Vector4f viewport = mRenderer.getDefaultViewport();
			mViewportX = viewport.x;
			mViewportY = viewport.y;
			mViewportWidth = viewport.z;
			mViewportHeight = viewport.w;
		}
	}
	public void setViewport(float x, float y, float width, float height) {
		mViewport = true;
		mViewportX = x;
		mViewportY = y;
		mViewportWidth = width;
		mViewportHeight = height;
		setAspect(width/height);
	}
	
	public boolean isBinded() {
		return mBinded;
	}
	public void bind() {
		if (mSceneMgr==null) {
			return;
		}
		//Unbind the current camera
		if (mSceneMgr.mActiveCam!=null) {
			mSceneMgr.mActiveCam.unbind();
		}
		mSceneMgr.mActiveCam = this;
		mBinded = true;
		//Change the viewport if necessary
		if (mViewport) {
			mRenderer.setViewport(mViewportX, mViewportY, mViewportWidth, mViewportHeight);
		}
		//Change the projection of the engine
		PROJECTION.pushIdentity();
		if (mOrthographicProj)
			PROJECTION.ortho(-10, 10, -10/getAspect(), 10/getAspect(), getNear(), getFar());
		else
			PROJECTION.perspective(getFov(), getAspect(), getNear(), getFar());
		mRenderer.setPMatrixUniform();
		PROJECTION.pop();
	}
	public void unbind() {
		mBinded = false;
		if (mViewport) {
			mRenderer.setDefaultViewport();
		}
	}
	
	public void render() {
		if (mSceneMgr==null) {
			return;
		}
		update();
		if (mBinded) {
			//Init the view of the engine
			MODELVIEW.push(this.getViewMatrix());
		} else {
			//Draw a cube
			AxisAlignedBox box = new AxisAlignedBox(-.1f, -.1f, -.1f, .1f, .1f, .1f);
			box.translate(this.getEye());
			mRenderer.addAABoxToRenderQueue(box, new Color(0, 255, 0));
		}
	}
	public void setViewDir(Vector3f viewDirection) {
		Vector3f target = new Vector3f();
		target.add(this.getEye(), viewDirection);
		this.setTarget(target);
	}
	public void translate(float dX, float dY, float dZ) {
		Vector3f vec = new Vector3f();
		this.getEye(vec);
		this.setEye(vec.getX()+dX, vec.getY()+dY, vec.getZ()+dZ);
		this.getTarget(vec);
		this.setTarget(vec.getX()+dX, vec.getY()+dY, vec.getZ()+dZ);
	}
	public void translateAlongAxis(Vector3f trans) {
		Vector3f vec = new Vector3f();
		this.getEye(vec);
		vec.add(trans);
		this.setEye(vec);
		this.getTarget(vec);
		vec.add(trans);
		this.setTarget(vec);
	}
	public void translateAlongScreen(Vector2f screenTrans) {
		Vector3f eyeOffset = this.getEyeOffset();
		//Theta is the rotation angle around Z, X being a null rotation: Theta = atan(eyeOffset.y, eyeOffset.x)
		//ThetaY is the rotation angle around Z, Y being a null rotation: ThetaY = Theta - Pi/2
		float thetaY = (float) (Math.atan2(eyeOffset.y, eyeOffset.x)-Math.PI/2);
		Vector3f trans = new Vector3f(); //the scene translation
		trans.x -= (float) (screenTrans.x*Math.cos(thetaY) - screenTrans.y*Math.sin(thetaY));
		trans.y -= (float) (screenTrans.x*Math.sin(thetaY) + screenTrans.y*Math.cos(thetaY));
		translateAlongAxis(trans);
//		Vector3f vec = new Vector3f();
//		this.getEye(vec);
//		vec.add(trans);
//		this.setEye(vec);
//		this.getTarget(vec);
//		vec.add(trans);
//		this.setTarget(vec);
	}
	/**
	 * Translate the camera along the view direction - direction built from the eye and the target of the camera. 
	 * A positive value of the scale translates the camera in direction of the target 
	 * and a negative value translates the camera in the opposite direction.
	 * @param s Scale of the translation. 
	 */
	public void translateToTarget(float s) {
		Vector3f trans = new Vector3f(this.getViewDir());
		trans.normalize();
		trans.scale(s);
		translateAlongAxis(trans);
	}
	public void rotate(Vector3f rot) {
		Vector3f targetOffset = this.getViewDir();
		SecaMath.rotateZ(targetOffset, rot.z, rot.x);
		this.setViewDir(targetOffset);
	}
	public void rotateX(float angleX) {
		rotate(new Vector3f(angleX, 0, 0));
	}
	public void rotateZ(float angleZ) {
		rotate(new Vector3f(0, 0, angleZ));
	}
	public void rotateAroundTarget(float angleX, float angleZ) {
		Vector3f eyeOffset = this.getEyeOffset();
		SecaMath.rotateZ(eyeOffset, angleZ, angleX);
//		if (Math.abs(angleX) > 0.001f) {
//			//Passage en coord sphériques
//			float r = (float) Math.sqrt(eyeOffset.x*eyeOffset.x+eyeOffset.y*eyeOffset.y+eyeOffset.z*eyeOffset.z);
//			float theta = (float) Math.atan2(eyeOffset.y, eyeOffset.x);
//			float phi = (float) Math.atan2(Math.sqrt(eyeOffset.x*eyeOffset.x+eyeOffset.z*eyeOffset.z), eyeOffset.y);
//			phi += Math.toRadians(angleX);
//			if (phi<0.001f) {
//				phi = 0.001f;
//			} else if (phi > Math.PI) {
//				phi = (float) (Math.PI - 0.001f);
//			}
//			eyeOffset.x = (float) (r*Math.sin(phi)*Math.cos(theta));
//			eyeOffset.y = (float) (r*Math.sin(phi)*Math.sin(theta));
//			eyeOffset.z = (float) (r*Math.cos(phi));
//		}
//		if (Math.abs(angleZ) > 0.001f) {
//			Matrix3f rot = new Matrix3f();
//			rot.rotY((float) Math.toRadians(angleZ));
//			rot.transform(eyeOffset);
//		}
		this.setEyeOffset(eyeOffset);
	}
	public void zoom(float zoom) {
		Vector3f eyeOffset = this.getEyeOffset();
		eyeOffset.scale(1+zoom/100);
		this.setEyeOffset(eyeOffset);
	}
	
	public int isVisible(AxisAlignedBox boundingBox) {
		Plane[] frustumPlanes = getFrustumPlanes();
		
		return 0;
	}
//	public boolean isVisible(Sphere boundingSphere) {
//		
//		return true;
//	}
	public Plane[] getFrustumPlanes() {
		//Ref: http://crazyjoke.free.fr/doc/3D/plane%20extraction.pdf
		Plane[] planes = new Plane[6];
		Matrix4f m = getViewProjectionMatrix();
		//Left plane
		planes[0] = new Plane(m.m30 + m.m00, m.m31 + m.m01, m.m32 + m.m02, m.m33 + m.m03);
		//Right plane
		planes[0] = new Plane(m.m30 - m.m00, m.m31 - m.m01, m.m32 - m.m02, m.m33 - m.m03);
		//Bottom plane
		planes[0] = new Plane(m.m30 + m.m10, m.m31 + m.m22, m.m32 + m.m12, m.m33 + m.m13);
		//Top plane
		planes[0] = new Plane(m.m30 - m.m10, m.m31 - m.m11, m.m32 - m.m12, m.m33 - m.m13);
		//Near plane
		planes[0] = new Plane(m.m30 + m.m20, m.m31 + m.m21, m.m32 + m.m22, m.m33 + m.m23);
		//Far plane
		planes[0] = new Plane(m.m30 - m.m20, m.m31 - m.m21, m.m32 - m.m22, m.m33 - m.m23);
		return planes;
	}
	
	public void setOrthographicProj(boolean ortho) {
		if (mOrthographicProj == ortho) 
			return;
		mOrthographicProj = ortho;
		bind();
	}
	
	public void followEntity(IEntity agent, Vector3f relativeEye, Vector3f relativeTarget, Vector3f relativeUp) {
		mEntity = agent;
		mRelativeEye = relativeEye;
		mRelativeTarget = relativeTarget;
		mRelativeUp = relativeUp;
	}
	private void update() {
		if (mEntity != null) {
			if (mRelativeEye!=null)
				setEye(mEntity.localToParentRef(mRelativeEye));
			if (mRelativeTarget!=null)
				setTarget(mEntity.localToParentRef(mRelativeTarget));
			if (mRelativeUp!=null)
				setUp(mEntity.localToParentRef(mRelativeUp));
		}
	}
	public Ray getCameraToViewportRay(int x, int y) {
		Vector3f origin = new Vector3f(getEye());
		Vector3f direction = new Vector3f(getViewDir());
		//Ref: http://trac.bookofhook.com/bookofhook/trac.cgi/wiki/MousePicking
		Vector3f v = new Vector3f(x, y, 1);
//		System.out.println("Viewport: " + v.toString());
		//Viewport to Clip
		Vector4f c = new Vector4f();
		c.x = 2*v.x/mViewportWidth - 1;
		c.y = 1 - 2*v.y/mViewportHeight;
		c.z = 2*v.z - 1;
		c.w = 1;
//		System.out.println("Clipping Space: " + c.toString());
		//Clipping Space to View Space
		Vector4f vs = new Vector4f();
		Matrix4f invP = new Matrix4f();
		try {
			invP.invert(this.getProjectionMatrix());
		} catch (SingularMatrixException e) {
			System.out.println(e.getMessage());
			throw e;
		}
		invP.transform(c, vs);
		vs.scale(1/vs.w);
//		System.out.println("View Space: " + vs.toString());
		//vs.setW(0); //To get the ray direction instead of a point in the Model Space.
		//View Space to Model Space (World Space)
		Vector4f w = new Vector4f();
		Matrix4f invM = new Matrix4f();
		try {
			invM.invert(this.getViewMatrix());
		} catch (SingularMatrixException e) {
			System.out.println(e.getMessage());
			throw e;
		}
		invM.transform(vs, w);
//		System.out.println("Model Space: " + w.toString());
		//Ray
		direction.x = w.x - origin.x;
		direction.y = w.y - origin.y;
		direction.z = w.z - origin.z;
		System.out.println("Ray Origin: " + origin.toString() + ", Ray Direction: " + direction.toString());
		return new Ray(origin, direction);
	}
	
	private static final float THETA_UPPER_LIMIT = 89.0f * (float) Math.PI / 180.0f;
	private static final float THETA_LOWER_LIMIT = -THETA_UPPER_LIMIT;
	
	private SceneManager mSceneMgr;
	private Renderer mRenderer;
	private boolean mBinded;
	private boolean mViewport;
	private float mViewportX;
	private float mViewportY;
	private float mViewportWidth;
	private float mViewportHeight;
	private boolean mOrthographicProj;
	private Plane[] mFrustumPlanes = new Plane[6];
	private IEntity mEntity;
	private Vector3f mRelativeEye;
	private Vector3f mRelativeTarget;
	private Vector3f mRelativeUp;
	
}
