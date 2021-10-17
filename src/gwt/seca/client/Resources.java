package gwt.seca.client;

import gwt.g3d.client.gl2.GL2;
import gwt.g3d.client.gl2.enums.TextureMagFilter;
import gwt.g3d.client.gl2.enums.TextureMinFilter;
import gwt.g3d.client.gl2.enums.TextureWrapMode;
import gwt.g3d.client.mesh.StaticMesh;
import gwt.g3d.client.primitive.PrimitiveFactory;
import gwt.g3d.client.shader.AbstractShader;
import gwt.g3d.client.texture.Texture2D;
import gwt.g3d.resources.client.ExternalTexture2DResource;
import gwt.g3d.resources.client.MagFilter;
import gwt.g3d.resources.client.MeshResource;
import gwt.g3d.resources.client.MinFilter;
import gwt.g3d.resources.client.ShaderResource;
import gwt.g3d.resources.client.Texture2DResource;
import gwt.seca.client.IResources;
import gwt.seca.client.util.SecaPrimitiveFactory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ResourceCallback;
import com.google.gwt.resources.client.ResourceException;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.user.client.Window;

public class Resources {
	
	//01-09: Mesh group for primitives
	public final static int MESH_ID_BOX = 1;
	public final static int MESH_ID_SPHERE = 2;
	public final static int MESH_ID_PLANE = 3;
	public final static int MESH_ID_HEMISPHERE = 4;
	//10-19: Mesh group for plants
	public final static int MESH_ID_PLANT_STOPLIGHT = 10;
	//10-19: Mesh group for animals
	public final static int MESH_ID_FISH = 20;
	public final static int MESH_LIST_LENGTH = 30;
	//Textures
	public final static int TEX_ID_NONE = 0;
	//01-09: Texture group for stone
	public final static int TEX_ID_STONE_BLACK = 1;
	public final static int TEX_ID_STONE_GRAY = 2;
	public final static int TEX_ID_STONE_GREEN = 3;
	public final static int TEX_ID_STONE_ORANGE = 4;
	public final static int TEX_ID_STONE_YELLOW = 5;
	//10-19: Texture group for dirt
	
	//20-29: Texture group for sand
	public final static int TEX_ID_SAND_DARK = 20;
	//30-39: Texture group for grass
	public final static int TEX_ID_GRASS_GREEN = 30;
	//40-49: Texture group for other building
	public final static int TEX_ID_BRICKS = 40;
	public final static int TEX_ID_CRATE = 41;
	//50-59: Texture group for plants
	public final static int TEX_ID_PLANT_STOPLIGHT_YELLOW = 50;
	public final static int TEX_ID_PLANT_STOPLIGHT_CYAN = 51;
	public final static int TEX_ID_PLANT_STOPLIGHT_MAGENTA = 52;
	public final static int TEX_ID_PLANT_STOPLIGHT_RED = 53;
	public final static int TEX_ID_PLANT_STOPLIGHT_GREEN = 54;
	public final static int TEX_ID_PLANT_STOPLIGHT_BLUE = 55;
	//60-69: Texture group for animals
	public final static int TEX_ID_FISH = 60;
	//70-79: Other textures
	public final static int TEX_ID_TEXT = 70;
	public final static int TEX_LIST_LENGTH = 100;
	
	public static StaticMesh[] mMeshList = new StaticMesh[MESH_LIST_LENGTH];
	public static Texture2D[] mTexList = new Texture2D[TEX_LIST_LENGTH];
	public static int mBindedTexID = 0;
	
	public Resources(GL2 gl, AbstractShader shader) {
		mGL = gl;
		mShader = shader;
		initMeshes();
		initTextures();
	}
	private void initMeshes() {
		StaticMesh meshBox;
		meshBox = new StaticMesh(mGL, PrimitiveFactory.makeBox());
		meshBox.setPositionIndex(mShader.getAttributeLocation("aVertexPosition"));
		meshBox.setNormalIndex(mShader.getAttributeLocation("aVertexNormal"));
		meshBox.setTexCoordIndex(mShader.getAttributeLocation("aTextureCoord"));
		mMeshList[MESH_ID_BOX] = meshBox;
		
		StaticMesh meshSphere;
		meshSphere = new StaticMesh(mGL, PrimitiveFactory.makeSphere(6, 12));
		meshSphere.setPositionIndex(mShader.getAttributeLocation("aVertexPosition"));
		meshSphere.setNormalIndex(mShader.getAttributeLocation("aVertexNormal"));
		meshSphere.setTexCoordIndex(mShader.getAttributeLocation("aTextureCoord"));
		mMeshList[MESH_ID_SPHERE] = meshSphere;
		
		StaticMesh meshPlane;
		meshPlane = new StaticMesh(mGL, PrimitiveFactory.makePlane());
		meshPlane.setPositionIndex(mShader.getAttributeLocation("aVertexPosition"));
		meshPlane.setNormalIndex(mShader.getAttributeLocation("aVertexNormal"));
		meshPlane.setTexCoordIndex(mShader.getAttributeLocation("aTextureCoord"));
		mMeshList[MESH_ID_PLANE] = meshPlane;
		
		StaticMesh meshHemisphere;
		meshHemisphere = new StaticMesh(mGL, SecaPrimitiveFactory.makeHemisphere(6, 7));
		meshHemisphere.setPositionIndex(mShader.getAttributeLocation("aVertexPosition"));
		meshHemisphere.setNormalIndex(mShader.getAttributeLocation("aVertexNormal"));
		meshHemisphere.setTexCoordIndex(mShader.getAttributeLocation("aTextureCoord"));
		mMeshList[MESH_ID_HEMISPHERE] = meshHemisphere;
		
		IResources resource = IResources.INSTANCE;
		resource.plantStoplightObj().getMesh(new ResourceCallback<MeshResource>() {
			@Override
			public void onSuccess(MeshResource resource) {
				mMeshList[MESH_ID_PLANT_STOPLIGHT] = resource.createMesh(mGL);
				mMeshList[MESH_ID_PLANT_STOPLIGHT].setPositionIndex(mShader.getAttributeLocation("aVertexPosition"));
				mMeshList[MESH_ID_PLANT_STOPLIGHT].setNormalIndex(mShader.getAttributeLocation("aVertexNormal"));
				mMeshList[MESH_ID_PLANT_STOPLIGHT].setTexCoordIndex(mShader.getAttributeLocation("aTextureCoord"));
			}

			@Override
			public void onError(ResourceException e) {
				Window.alert(e.getMessage());
			}
		});
		
		resource.fishObj().getMesh(new ResourceCallback<MeshResource>() {
			@Override
			public void onSuccess(MeshResource resource) {
				mMeshList[MESH_ID_FISH] = resource.createMesh(mGL);
				mMeshList[MESH_ID_FISH].setPositionIndex(mShader.getAttributeLocation("aVertexPosition"));
				mMeshList[MESH_ID_FISH].setNormalIndex(-1);
				mMeshList[MESH_ID_FISH].setTexCoordIndex(mShader.getAttributeLocation("aTextureCoord"));
			}

			@Override
			public void onError(ResourceException e) {
				Window.alert(e.getMessage());
			}
		});
	}
	private void initTextures() {
		IResources resource = IResources.INSTANCE;
		resource.crate().getTexture(new ResourceCallback<Texture2DResource>() {
			@Override
			public void onSuccess(Texture2DResource resource) {
				mTexList[TEX_ID_CRATE] = resource.createTexture(mGL);
			}
			@Override
			public void onError(ResourceException e) {
				Window.alert("Fail to load image.");
			}
		});
		
		resource.grassGreen().getTexture(new ResourceCallback<Texture2DResource>() {
			@Override
			public void onSuccess(Texture2DResource resource) {
				mTexList[TEX_ID_GRASS_GREEN] = resource.createTexture(mGL);
			}
			@Override
			public void onError(ResourceException e) {
				Window.alert("Fail to load image.");
			}
		});
		
//		resource.metal().getTexture(new ResourceCallback<Texture2DResource>() {
//			@Override
//			public void onSuccess(Texture2DResource resource) {
//				mTexList[TEX_ID_METAL] = resource.createTexture(mGL);
//			}
//			@Override
//			public void onError(ResourceException e) {
//				Window.alert("Fail to load image.");
//			}
//		});
		
		resource.stoneGreen().getTexture(new ResourceCallback<Texture2DResource>() {
			@Override
			public void onSuccess(Texture2DResource resource) {
				mTexList[TEX_ID_STONE_GREEN] = resource.createTexture(mGL);
			}
			@Override
			public void onError(ResourceException e) {
				Window.alert("Fail to load image.");
			}
		});
		
		resource.sand().getTexture(new ResourceCallback<Texture2DResource>() {
			@Override
			public void onSuccess(Texture2DResource resource) {
				mTexList[TEX_ID_SAND_DARK] = resource.createTexture(mGL);
			}
			@Override
			public void onError(ResourceException e) {
				Window.alert("Fail to load image.");
			}
		});
		
		resource.bricks().getTexture(new ResourceCallback<Texture2DResource>() {
			@Override
			public void onSuccess(Texture2DResource resource) {
				mTexList[TEX_ID_BRICKS] = resource.createTexture(mGL);
			}
			@Override
			public void onError(ResourceException e) {
				Window.alert("Fail to load image.");
			}
		});
		
		Texture2D textTexture = new Texture2D();
		textTexture.init(mGL);
		textTexture.bind();
		textTexture.setMagFilter(TextureMagFilter.LINEAR);
		textTexture.setMinFilter(TextureMinFilter.LINEAR);
		textTexture.setWrapMode(TextureWrapMode.CLAMP_TO_EDGE);
		mTexList[TEX_ID_TEXT] = textTexture;
		
		resource.plantStoplightTexYellow().getTexture(new ResourceCallback<Texture2DResource>() {
			@Override
			public void onSuccess(Texture2DResource resource) {
				mTexList[TEX_ID_PLANT_STOPLIGHT_YELLOW] = resource.createTexture(mGL);
			}
			@Override
			public void onError(ResourceException e) {
				Window.alert("Fail to load image.");
			}
		});
		resource.plantStoplightTexCyan().getTexture(new ResourceCallback<Texture2DResource>() {
			@Override
			public void onSuccess(Texture2DResource resource) {
				mTexList[TEX_ID_PLANT_STOPLIGHT_CYAN] = resource.createTexture(mGL);
			}
			@Override
			public void onError(ResourceException e) {
				Window.alert("Fail to load image.");
			}
		});
		resource.plantStoplightTexMagenta().getTexture(new ResourceCallback<Texture2DResource>() {
			@Override
			public void onSuccess(Texture2DResource resource) {
				mTexList[TEX_ID_PLANT_STOPLIGHT_MAGENTA] = resource.createTexture(mGL);
			}
			@Override
			public void onError(ResourceException e) {
				Window.alert("Fail to load image.");
			}
		});
		resource.plantStoplightTexRed().getTexture(new ResourceCallback<Texture2DResource>() {
			@Override
			public void onSuccess(Texture2DResource resource) {
				mTexList[TEX_ID_PLANT_STOPLIGHT_RED] = resource.createTexture(mGL);
			}
			@Override
			public void onError(ResourceException e) {
				Window.alert("Fail to load image.");
			}
		});
		resource.plantStoplightTexGreen().getTexture(new ResourceCallback<Texture2DResource>() {
			@Override
			public void onSuccess(Texture2DResource resource) {
				mTexList[TEX_ID_PLANT_STOPLIGHT_GREEN] = resource.createTexture(mGL);
			}
			@Override
			public void onError(ResourceException e) {
				Window.alert("Fail to load image.");
			}
		});
		resource.plantStoplightTexBlue().getTexture(new ResourceCallback<Texture2DResource>() {
			@Override
			public void onSuccess(Texture2DResource resource) {
				mTexList[TEX_ID_PLANT_STOPLIGHT_BLUE] = resource.createTexture(mGL);
			}
			@Override
			public void onError(ResourceException e) {
				Window.alert("Fail to load image.");
			}
		});
		
		resource.fishTex().getTexture(new ResourceCallback<Texture2DResource>() {
			@Override
			public void onSuccess(Texture2DResource resource) {
				mTexList[TEX_ID_FISH] = resource.createTexture(mGL);
			}
			@Override
			public void onError(ResourceException e) {
				Window.alert("Fail to load image.");
			}
		});
	}
	
	private GL2 mGL;
	private AbstractShader mShader;
	
}
