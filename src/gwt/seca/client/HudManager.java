package gwt.seca.client;

import static gwt.g3d.client.math.MatrixStack.MODELVIEW;
import static gwt.g3d.client.math.MatrixStack.PROJECTION;
import gwt.g2d.client.graphics.Color;
import gwt.g3d.client.Surface3D;
import gwt.g3d.client.gl2.GL2;
import gwt.g3d.client.gl2.WebGLBuffer;
import gwt.g3d.client.gl2.array.Float32Array;
import gwt.g3d.client.gl2.enums.BeginMode;
import gwt.g3d.client.gl2.enums.BlendingFactorDest;
import gwt.g3d.client.gl2.enums.BlendingFactorSrc;
import gwt.g3d.client.gl2.enums.BufferTarget;
import gwt.g3d.client.gl2.enums.BufferUsage;
import gwt.g3d.client.gl2.enums.ClearBufferMask;
import gwt.g3d.client.gl2.enums.DataType;
import gwt.g3d.client.gl2.enums.EnableCap;
import gwt.g3d.client.text.TextRenderer;
import gwt.g3d.client.texture.Texture2D;
import gwt.seca.client.util.SecaTextRenderer;

public class HudManager {

	//Heads-Up Display
	//orthographic projection
	
	public HudManager(SimEngine engine, SceneManager sceneMgr, Renderer renderer) {
		mEngine = engine;
		mSurface = mEngine.getSurface();
		mGL = mSurface.getGL();
		mSceneMgr = sceneMgr;
		mRenderer = renderer;
		mRenderingStarted = false;
		//mGL.disable(EnableCap.DEPTH_TEST);
//		mGL.enable(EnableCap.BLEND);
//		//mGL.blendFunc(BlendingFactorSrc.SRC_ALPHA, BlendingFactorDest.ONE_MINUS_SRC_ALPHA);
//		mGL.blendColor(0, 0, 0, 0);
	}
	
	public void render() {
		startRendering();
		renderDashboard();
		endRendering();
	}
	private void startRendering() {
		mRenderingStarted = true;
		//mGL.clear(ClearBufferMask.DEPTH_BUFFER_BIT);
		PROJECTION.pushIdentity();
		PROJECTION.ortho(-mSurface.getWidth(), 0, -mSurface.getHeight(), 0, -10, 10);
		//PROJECTION.perspective(45, 2, .1f, 100);
		mRenderer.setPMatrixUniform();
		PROJECTION.pop();
		MODELVIEW.push();
		mRenderer.enableTextures();
		mRenderer.disableLighting();
	}
	private void renderDashboard() {
		int dashboardWidth = mSurface.getWidth();
		int dashboardHeight = 40;
		Texture2D fpsTex = Resources.mTexList[Resources.TEX_ID_TEXT];
		//TextRenderer textRenderer = new TextRenderer(mEngine.getSurface(), 100, 100);
		SecaTextRenderer textRenderer = new SecaTextRenderer(mEngine.getSurface(), dashboardWidth, dashboardHeight);
		textRenderer.setFont("15px sans-serif");
		textRenderer.setBackgroundColor(new Color(155, 155, 155, 1));
		//Display FPS
		displayFPS(textRenderer, 0, 0, new Color(0, 0, 0, 1));
		//displayFPS(textRenderer, 50, 10, new Color(0, 255, 0, 1));
		
		textRenderer.toTexture(fpsTex);
		fpsTex.bind();
		MODELVIEW.translate(dashboardWidth/2f, mSurface.getHeight()-dashboardHeight/2f, 0);
		MODELVIEW.rotate((float) Math.toRadians(90), 1, 0, 0);
		MODELVIEW.scale(dashboardWidth/2f, 1, dashboardHeight/2f);
		mRenderer.setMatrixUniforms();
		Resources.mMeshList[Resources.MESH_ID_PLANE].draw();
		//4 black lines around the dashboard
		drawBorders(new Color(0, 0, 0, 1));
	}
	private void displayFPS(SecaTextRenderer textRenderer, int x, int y, Color color) {
		textRenderer.setTextColor(color);
		float fps = Math.round(mEngine.getFPS()*100)/100f;
		textRenderer.fillText(new StringBuilder().append("FPS: ").append(fps).toString(), x, y);
	}
	private void endRendering() {
		MODELVIEW.pop();
		mRenderingStarted = false;
		mSceneMgr.mActiveCam.bind();
	}
	
	private void drawBorders(Color color) {
		mRenderer.enableArrayDrawing();
		mRenderer.disableUniformColor();
		mRenderer.disableTextures();
		WebGLBuffer lineVertexPositionBuffer = mGL.createBuffer();
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexPositionBuffer);
	    float[] vertices = {
	    		-1, 0, -1,
	    		-1, 0, 1,
	    		1, 0, 1,
	    		1, 0, -1
	    		};
	    mGL.bufferData(BufferTarget.ARRAY_BUFFER, 
	        Float32Array.create(vertices), BufferUsage.STATIC_DRAW);
	    mGL.vertexAttribPointer(mRenderer.getVertexPositionAttrib(), 3, DataType.FLOAT, false, 0, 0);
	    
	    WebGLBuffer lineVertexColorBuffer = mGL.createBuffer();
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexColorBuffer);
	    float[] colors = new float[17*4];
	    for(int idx=0; idx<4; idx++) {
	    	colors[idx*4] = color.getR()/255f;
	    	colors[idx*4+1] = color.getG()/255f;
	    	colors[idx*4+2] = color.getB()/255f;
	    	colors[idx*4+3] = (float) color.getAlpha();
	    }
	    mGL.bufferData(BufferTarget.ARRAY_BUFFER,
	        Float32Array.create(colors), BufferUsage.STATIC_DRAW);
	    //mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexColorBuffer);
	    mGL.vertexAttribPointer(mRenderer.getVertexColorAttrib(), 4, DataType.FLOAT, false, 0, 0);
	    
	    mGL.drawArrays(BeginMode.LINE_LOOP, 0, 4);
	    
	    mGL.deleteBuffer(lineVertexPositionBuffer);
	    mGL.deleteBuffer(lineVertexColorBuffer);
	    mRenderer.disableArrayDrawing();
	}
	
	private SimEngine mEngine;
	private Surface3D mSurface;
	private GL2 mGL;
	private SceneManager mSceneMgr;
	private Renderer mRenderer;
	private boolean mRenderingStarted;
}
