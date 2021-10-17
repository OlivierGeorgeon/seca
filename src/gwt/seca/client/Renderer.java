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
import gwt.g3d.client.gl2.enums.DepthFunction;
import gwt.g3d.client.gl2.enums.EnableCap;
import gwt.g3d.client.gl2.enums.TextureMagFilter;
import gwt.g3d.client.gl2.enums.TextureMinFilter;
import gwt.g3d.client.gl2.enums.TextureUnit;
import gwt.g3d.client.shader.AbstractShader;
import gwt.g3d.client.shader.ShaderException;
import gwt.g3d.client.texture.Texture2D;
import gwt.g3d.resources.client.ExternalTexture2DResource;
import gwt.g3d.resources.client.MagFilter;
import gwt.g3d.resources.client.MinFilter;
import gwt.g3d.resources.client.ShaderResource;
import gwt.g3d.resources.client.Texture2DResource;
import gwt.seca.client.IResources;
import gwt.seca.client.agents.AbstractModel;
import gwt.seca.client.util.AxisAlignedBox;
import gwt.seca.client.util.Line;
import gwt.seca.client.util.Pair;
import gwt.seca.client.util.Quad;
import gwt.seca.client.util.Ray;
import gwt.seca.client.util.SecaColor;
import gwt.seca.client.util.SecaMath;
import gwt.seca.client.util.Triangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundleWithLookup;
import com.google.gwt.resources.client.ResourceCallback;
import com.google.gwt.resources.client.ResourceException;
import com.google.gwt.resources.client.ClientBundle.Source;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;

public class Renderer {
	
	final public static int RENDER_POINTCLOUD = 0;
	final public static int RENDER_WIREFRAME = 1;
	final public static int RENDER_SOLID_COLOR = 2;
	final public static int RENDER_SOLID_TEXTURE = 3;
	final public static int PROJ_PERSPECTIVE = 0;
	final public static int PROJ_ORTHOGRAPHIC = 1;
	
	public Renderer(SimEngine engine, SceneManager sceneMgr) {
		mEngine = engine;
		mSurface = mEngine.getSurface();
		mGL = mSurface.getGL();
		mSceneMgr = sceneMgr;
		initGL();
		initShaders();
		initResources(); //Initialization of meshes and textures
		mRenderMode = RENDER_SOLID_TEXTURE;
		mProjectionMode = PROJ_PERSPECTIVE;
//		mRenderTimeLabel = Seca.dashboardMgr.createLabel(Seca.dashboardMgr.getPanel("Debug"));
	}
	private void initGL() {
		mGL.clearColor(1f, 1f, 1f, 1f);
		mGL.clearDepth(1);
		setDefaultViewport();

		mGL.enable(EnableCap.DEPTH_TEST);
		mGL.depthFunc(DepthFunction.LEQUAL);
		mGL.clear(ClearBufferMask.COLOR_BUFFER_BIT, ClearBufferMask.DEPTH_BUFFER_BIT);
	}
	private void initShaders() {
		try {
			ShaderResource shaderResource = ((ShaderResource) getClientBundle()
					.getResource("shader"));
			mShader = shaderResource.createShader(mGL);
			mShader.bind();
		} catch (ShaderException e) {
			Window.alert(e.getMessage());
			return;
		}

		mGL.activeTexture(TextureUnit.TEXTURE0);
		mGL.uniform1i(mShader.getUniformLocation("uSampler"), 0);

		//Default projection
		PROJECTION.pushIdentity();
		PROJECTION.perspective(45, 1, .1f, 100);
		setPMatrixUniform();
		PROJECTION.pop();

	}
	private void initResources() {
		mResources = new Resources(mGL, mShader);
	}

	public void dispose() {
		mShader.dispose();
		Seca.dashboardMgr.deleteWidget(mRenderTimeLabel);
	}
	
	public void clearDisplay() {
        mGL.clear(ClearBufferMask.COLOR_BUFFER_BIT, ClearBufferMask.DEPTH_BUFFER_BIT);
	}

	public int renderFrame() {
		long startTime = System.currentTimeMillis();
		startRendering();
		this.enableLighting();
		this.disableBlending();
		mSceneRenderTime = renderScene();
		mSceneQueuesRenderTime = renderSceneQueues();
		//Render queues of primitives
		this.disableLighting();
		this.enableBlending();
		mTrianglesRenderTime = renderTrianglesInQueue();
		mAABoxesRenderTime = renderAABoxInQueue();
		mLinesRenderTime = renderLinesInQueue();
		mPointsRenderTime = renderPointsInQueue();
		endRendering();
		mRenderTime = (int) (System.currentTimeMillis() - startTime);
//		mRenderTimeLabel.setText("Render time: "+mRenderTime+" (Scene: "+mSceneRenderTime+"ms, Scene queues: "+mSceneQueuesRenderTime+"ms, Points: "+mPointsRenderTime+"ms, Lines: "+mLinesRenderTime+"ms, Triangles: "+mTrianglesRenderTime+"ms, AABoxes: "+mAABoxesRenderTime+"ms)");
		return mRenderTime;
	}
	
	private void startRendering() {
		mRenderingStarted = true;
		switch (mRenderMode) {
		case RENDER_POINTCLOUD:
			enablePointCloud();
			break;
		case RENDER_WIREFRAME:
			enableWireFrame();
			break;
		}
		mBlocksToRender.clear();
		mEntitiesToRender.clear();
		clearTrianglesFromRenderQueue();
		clearAABoxFromRenderQueue();
		mRenderTime = 0;
		mSceneRenderTime = 0;
		mSceneQueuesRenderTime = 0;
		mPointsRenderTime = 0;
		mLinesRenderTime = 0;
		mTrianglesRenderTime = 0;
		mAABoxesRenderTime = 0;
	}
	private int renderScene() {
		long startTime = System.currentTimeMillis();
		//TODO add lighting to the scene
		boolean lighting = true;
		mGL.uniform1i(mShader.getUniformLocation("uUseLighting"), lighting ? 1 : 0);
		//with lesson7 shaders
		if (lighting) {
			mGL.uniform(mShader.getUniformLocation("uAmbientColor"), new Vector3f(0.2f, 0.2f, 0.2f));
			Vector3f lightingDirection = new Vector3f(-0.25f, -0.25f, -1.0f);
			lightingDirection.normalize();
			lightingDirection.scale(-1);
			mGL.uniform(mShader.getUniformLocation("uLightingDirection"), 
					lightingDirection);

			mGL.uniform(mShader.getUniformLocation("uDirectionalColor"),
					new Vector3f(0.8f, 0.8f, 0.8f));
		}
		
		if (!renderActiveCamera())
			return (int) (System.currentTimeMillis() - startTime);
		if (mEngine.areCamerasVisible())
			renderCameras();
		renderAllBlocks();
		renderEntities();
		//renderAgents();
		
		//Render the AABB of the block under the mouse
//		if (mSceneMgr.inScene(mEngine.getMouseCursorPosition())) {
//			Block block = mSceneMgr.getBlock(mEngine.getMouseCursorPosition());
//			if (block != null) {
//				//The block is valid
//				renderBlockAABB(mEngine.getMouseCursorPosition(), new Color(0, 0, 0, 1));
//				AxisAlignedBox boundingBox = SecaMath.getTranslatedAAB(block.getBlockAABB(), mEngine.getMouseCursorPosition());
//				Quad face = boundingBox.getExtractedFace(mEngine.getMouseCursorDirection());
//				if (face!=null) {
//					//The face is valid
//					Pair<Triangle, Triangle> triangles = face.getTriangles();
//					this.addTriangleToRenderQueue(triangles.mLeft, SecaColor.TransparentGreen);
//					this.addTriangleToRenderQueue(triangles.mRight, SecaColor.TransparentRed);
//				}
//			}
//		}
		//Render the target of the active cam
		if (mEngine.isCamTargetVisible())
			renderCell(mSceneMgr.mActiveCam.getTarget(), new Color(255, 0, 0, 1), true, true);
		//Render the cell selected with the keyboard
		if (mEngine.isKeyCursorVisible())
			renderCell(mEngine.getKeyboardCursorPosition(), new Color(0, 0, 255, 1), false);
		return (int) (System.currentTimeMillis() - startTime);
	}
	private int renderSceneQueues() {
		long startTime = System.currentTimeMillis();
		//Render queues of scene's objects
		renderCellsInQueue();
		return (int) (System.currentTimeMillis() - startTime);
	}
	private boolean renderCameras() {
		for(String cam: mSceneMgr.getCameraNames()) {
			if (!cam.equals(mSceneMgr.mActiveCam.mName)) // Do not show the camera
				mSceneMgr.getCamera(cam).render();
		}
		return true;
	}
	private boolean renderActiveCamera() {
		//TODO take out the MODELVIEW.push();
		//TODO add getActiveCamera() to the sceneManager
		if (mSceneMgr.mActiveCam == null)
			return false;
		mSceneMgr.mActiveCam.render();
		return true;
	}
	private void renderAllBlocks() {
		//TODO replace the scene's renderer
		mGL.uniform1i(mShader.getUniformLocation("uUseTextures"), 1);
		renderAllBlocksByBlockID();
		//renderAllBlocksByIndex();
	}
	private boolean renderAllBlocksByBlockID() {
		//Sort blocks
		@SuppressWarnings("unchecked")
		ArrayList<Integer>[] blockIdxByID = new ArrayList[Block.BLOCK_LIST_LENGTH];
		for(int i=1; i<Block.BLOCK_LIST_LENGTH; i++) {
			blockIdxByID[i] = new ArrayList<Integer>();
		}
		for(int idx=0; idx<mSceneMgr.getBlockArrayLength(); idx++) {
			if (mSceneMgr.getBoardBlockID(idx)>0) {
				blockIdxByID[mSceneMgr.getBoardBlockID(idx)].add(idx);
			}
		}
		for(int id=1; id<Block.BLOCK_LIST_LENGTH; id++) {
			if (blockIdxByID[id].size()>0) {
				boolean applyTex = (mRenderMode == Renderer.RENDER_SOLID_TEXTURE);
				int textureID = Block.blockList[id].getTexID();
				//Force to apply a color if no texture is available
				if (textureID==0 || textureID>=Resources.TEX_LIST_LENGTH)
					applyTex = false;
				else if (Resources.mTexList[textureID]==null)
					applyTex = false;
				if (applyTex) {
					//Apply texture
					enableTextures();
					(Resources.mTexList[textureID]).bind();
				} else {
					//Apply uniform color
					enableUniformColor();
					Color color = Block.blockList[id].getColor();
					setColorUniform(new Vector4f(color.getR()/255f, color.getG()/255f, color.getB()/255f, (float) color.getAlpha()*0.8f));
				}
				for(int j=0; j<blockIdxByID[id].size(); j++) {
					int idx = blockIdxByID[id].get(j);
					renderBlock(idx, false);
				}
			}
		}
		return true;
	}
	@SuppressWarnings("unused")
	private boolean renderAllBlocksByIndex() {
		for(int idx=0; idx<mSceneMgr.getBlockArrayLength(); idx++) {
			if (mSceneMgr.getBoardBlockID(idx)>0) {
				renderBlock(idx, true);
			}
		}
		return true;
	}
	private boolean renderBlock(int idx, boolean bindTexOrColor)  {
		Block block = Block.blockList[mSceneMgr.getBoardBlockID(idx)];
		Vector3f pos = mSceneMgr.getPosition(idx);
		return renderBlockWithPosition(block, pos, bindTexOrColor);
	}
	public boolean renderBlockWithPosition(Block block, int i, int j, int k, boolean bindTexOrColor) {
		MODELVIEW.push();
		MODELVIEW.translate(i, j, k);
		setMatrixUniforms();
		block.render(this, mRenderMode == RENDER_SOLID_TEXTURE, bindTexOrColor);
		MODELVIEW.pop();
		return true;
	}
	public boolean renderBlockWithPosition(Block block, Vector3f pos, boolean bindTexOrColor) {
		return renderBlockWithPosition(block, (int) pos.getX(), (int) pos.getY(), (int) pos.getZ(), bindTexOrColor);
	}
	
	private void renderEntities() {
		mGL.uniform1i(mShader.getUniformLocation("uUseTextures"), 1);
		Set<String> entityNames = mSceneMgr.getEntityNames();
		Iterator<String> iterator = entityNames.iterator();
		while (iterator.hasNext()) {
	    	IEntity entity = mSceneMgr.getEntity(iterator.next());
	    	entity.render(this);
	    }
		//Humans
		//Agents
	}
	
	private void renderBlockAABB(Point3i pos, Color color) {
		if (pos == null)
			throw new IllegalArgumentException("Illegal null position.");
		if (!mSceneMgr.inScene(pos))
			return;
		AxisAlignedBox localAABB = mSceneMgr.getBlock(pos).getBlockAABB();
		renderAxisAlignedBox(SecaMath.getTranslatedAAB(localAABB, pos), color);
	}
	
	private void renderCell(Point3i pos, Color color, boolean onlySceneCells) {
		renderCell(new Vector3f(pos.x, pos.y, pos.z), color, onlySceneCells, false);
	}
	
	private int renderPointsInQueue() {
		long startTime = System.currentTimeMillis();
		if (mColoredPointQueue==null)
			return 0;
		Vector3f[] points = new Vector3f[mColoredPointQueue.size()];
		Color[] colors = new Color[mColoredPointQueue.size()];
		int i=0;
		for(Pair<Vector3f, Color> coloredPoint : mColoredPointQueue) {
			points[i] = coloredPoint.mLeft;
			colors[i] = coloredPoint.mRight;
			i++;
		}
		renderPoints(points, colors);
		return (int) (System.currentTimeMillis() - startTime);
	}
	private int renderLinesInQueue() {
		long startTime = System.currentTimeMillis();
		if (mColoredLineQueue==null)
			return 0;
		Line[] lines = new Line[mColoredLineQueue.size()];
		Color[] colors = new Color[mColoredLineQueue.size()];
		int i=0;
		for(Pair<Line, Color> coloredLine : mColoredLineQueue) {
			lines[i] = coloredLine.mLeft;
			colors[i] = coloredLine.mRight;
			i++;
		}
		renderLines(lines, colors);
		return (int) (System.currentTimeMillis() - startTime);
	}
	private int renderTrianglesInQueue() {
		long startTime = System.currentTimeMillis();
		if (mColoredTriangleQueue==null)
			return 0;
		Triangle[] triangles = new Triangle[mColoredTriangleQueue.size()];
		Color[] colors = new Color[mColoredTriangleQueue.size()];
		int i=0;
		for(Pair<Triangle, Color> coloredTriangle : mColoredTriangleQueue) {
			triangles[i] = coloredTriangle.mLeft;
			colors[i] = coloredTriangle.mRight;
			i++;
		}
		renderTriangles(triangles, colors);
		return (int) (System.currentTimeMillis() - startTime);
	}
	private int renderAABoxInQueue() {
		long startTime = System.currentTimeMillis();
		if (mColoredAABoxQueue==null)
			return 0;
		for(Pair<AxisAlignedBox, Color> coloredAAB : mColoredAABoxQueue) {
			renderAxisAlignedBox(coloredAAB.mLeft, coloredAAB.mRight);
		}
		return (int) (System.currentTimeMillis() - startTime);
	}
	private int renderCellsInQueue() {
		long startTime = System.currentTimeMillis();
		if (mColoredCellQueue==null)
			return 0;
		for(Pair<Point3i, Color> coloredCell : mColoredCellQueue) {
			renderCell(coloredCell.mLeft, coloredCell.mRight, false);
		}
		return (int) (System.currentTimeMillis() - startTime);
	}
	
	private void endRendering() {
		//MODELVIEW.pop();
		switch (mRenderMode) {
		case RENDER_POINTCLOUD:
			disablePointCloud();
			break;
		case RENDER_WIREFRAME:
			disableWireFrame();
			break;
		}
		mRenderingStarted = false;
	}
	
	public void addPointToRenderQueue(Vector3f point, Color color) {
		if (point==null || color==null || mColoredPointQueue==null)
			return;
		mColoredPointQueue.add(Pair.create(point, color));
	}
	public void clearPointsFromRenderQueue() {
		if (mColoredPointQueue==null)
			return;
		mColoredPointQueue.clear();
	}
	
	public void addLineToRenderQueue(Line line, Color color) {
		if (line==null || color==null || mColoredLineQueue==null)
			return;
		mColoredLineQueue.add(Pair.create(line, color));
	}
	public void addRayToRenderQueue(Ray ray, float t) {
		if (ray==null || mColoredLineQueue==null)
			return;
		System.out.println(ray.toString());
		//mColoredLinesToRender.clear();
		if (mColoredLineQueue.size()>50)
			mColoredLineQueue.remove(0);
		if (t==-1)
			t = 100f;
		Line line = new Line(ray, t);
		Vector3f startPoint = line.getStartPoint();
		startPoint.setZ(startPoint.getZ()+.0001f);
		line.setStartPoint(startPoint);
		addLineToRenderQueue(line, new Color(255, 0, 0, 1));
	}
	public void clearLinesFromRenderQueue() {
		if (mColoredLineQueue==null)
			return;
		mColoredLineQueue.clear();
	}
	
	public void addTriangleToRenderQueue(Triangle triangle, Color color) {
		if (triangle==null || color==null || mColoredTriangleQueue==null)
			return;
		mColoredTriangleQueue.add(Pair.create(triangle, color));
	}
	
	public void clearTrianglesFromRenderQueue() {
		if (mColoredTriangleQueue==null)
			return;
		mColoredTriangleQueue.clear();
	}
	
	public void addAABoxToRenderQueue(AxisAlignedBox box, Color color) {
		if (box==null || color==null || mColoredAABoxQueue==null)
			return;
		mColoredAABoxQueue.add(Pair.create(box, color));
	}
	public void clearAABoxFromRenderQueue() {
		if (mColoredAABoxQueue==null)
			return;
		mColoredAABoxQueue.clear();
	}
	
	//-------Methods to render primitives
	/**
	 * 
	 * @param points
	 * @param pointColors
	 */
	private void renderPoints(Vector3f[] points, Color[] pointColors) {
		assert (points != null): points;
		boolean uniqueColor = false;
		if (pointColors == null || pointColors.length<points.length) {
			uniqueColor = true;
			if (pointColors == null) 
				pointColors = new Color[1];
			if (pointColors[0] == null)
				pointColors[0] = new Color(0, 0, 0, 1);
		}
		enableArrayDrawing();
		disableUniformColor();
		disableTextures();
		disableLighting();
		MODELVIEW.push();
		setMatrixUniforms();

	    WebGLBuffer lineVertexPositionBuffer = mGL.createBuffer();
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexPositionBuffer);
	    float[] vertices = new float[points.length*3]; //3 coords per point
	    for(int idx=0; idx<points.length; idx++) {
	    	vertices[idx*3] = points[idx].getX();
	    	vertices[idx*3+1] = points[idx].getY();
	    	vertices[idx*3+2] = points[idx].getZ();
	    }
	    mGL.bufferData(BufferTarget.ARRAY_BUFFER, 
	        Float32Array.create(vertices), BufferUsage.STATIC_DRAW);
	    mGL.vertexAttribPointer(getVertexPositionAttrib(), 3, DataType.FLOAT, false, 0, 0);
	    
	    WebGLBuffer lineVertexColorBuffer = mGL.createBuffer();
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexColorBuffer);
	    float[] colors = new float[points.length*4]; //4 values per color
	    for(int idx=0; idx<points.length; idx++) {
	    	if (uniqueColor) {
		    	colors[idx*4] = pointColors[0].getR()/255f;
		    	colors[idx*4+1] = pointColors[0].getG()/255f;
		    	colors[idx*4+2] = pointColors[0].getB()/255f;
		    	colors[idx*4+3] = (float) pointColors[0].getAlpha();
	    	} else {
	    		colors[idx*4] = pointColors[idx].getR()/255f;
		    	colors[idx*4+1] = pointColors[idx].getG()/255f;
		    	colors[idx*4+2] = pointColors[idx].getB()/255f;
		    	colors[idx*4+3] = (float) pointColors[idx].getAlpha();
	    	}
	    }
	    mGL.bufferData(BufferTarget.ARRAY_BUFFER,
	        Float32Array.create(colors), BufferUsage.STATIC_DRAW);
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexColorBuffer);
	    mGL.vertexAttribPointer(getVertexColorAttrib(), 4, DataType.FLOAT, false, 0, 0);
	    
	    mGL.drawArrays(BeginMode.POINTS, 0, points.length);
	    
	    mGL.deleteBuffer(lineVertexPositionBuffer);
	    mGL.deleteBuffer(lineVertexColorBuffer);
	    MODELVIEW.pop();
	    
	    disableArrayDrawing();
	}
	private void renderLines(Line[] lines, Color[] lineColors) {
		assert (lines != null): lines;
		boolean uniqueColor = false;
		if (lineColors == null || lineColors.length<lines.length) {
			uniqueColor = true;
			if (lineColors == null) 
				lineColors = new Color[1];
			if (lineColors[0] == null)
				lineColors[0] = new Color(0, 0, 0, 1);
		}
		enableArrayDrawing();
		disableUniformColor();
		disableTextures();
		disableLighting();
		MODELVIEW.push();
		setMatrixUniforms();

	    WebGLBuffer lineVertexPositionBuffer = mGL.createBuffer();
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexPositionBuffer);
	    float[] vertices = new float[lines.length*6]; //2 points per line * 3 coords per point
	    for(int idx=0; idx<lines.length; idx++) {
	    	vertices[idx*6] = lines[idx].getStartPoint().getX();
	    	vertices[idx*6+1] = lines[idx].getStartPoint().getY();
	    	vertices[idx*6+2] = lines[idx].getStartPoint().getZ();
	    	vertices[idx*6+3] = lines[idx].getEndPoint().getX();
	    	vertices[idx*6+4] = lines[idx].getEndPoint().getY();
	    	vertices[idx*6+5] = lines[idx].getEndPoint().getZ();
	    }
	    mGL.bufferData(BufferTarget.ARRAY_BUFFER, 
	        Float32Array.create(vertices), BufferUsage.STATIC_DRAW);
	    mGL.vertexAttribPointer(getVertexPositionAttrib(), 3, DataType.FLOAT, false, 0, 0);
	    
	    WebGLBuffer lineVertexColorBuffer = mGL.createBuffer();
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexColorBuffer);
	    float[] colors = new float[lines.length*8]; //2 points per line * 4 values per color
	    for(int idx=0; idx<lines.length; idx++) {
	    	if (uniqueColor) {
		    	colors[idx*8] = lineColors[0].getR()/255f;
		    	colors[idx*8+1] = lineColors[0].getG()/255f;
		    	colors[idx*8+2] = lineColors[0].getB()/255f;
		    	colors[idx*8+3] = (float) lineColors[0].getAlpha();
		    	colors[idx*8+4] = lineColors[0].getR()/255f;
		    	colors[idx*8+5] = lineColors[0].getG()/255f;
		    	colors[idx*8+6] = lineColors[0].getB()/255f;
		    	colors[idx*8+7] = (float) lineColors[0].getAlpha();
	    	} else {
	    		colors[idx*8] = lineColors[idx].getR()/255f;
		    	colors[idx*8+1] = lineColors[idx].getG()/255f;
		    	colors[idx*8+2] = lineColors[idx].getB()/255f;
		    	colors[idx*8+3] = (float) lineColors[idx].getAlpha();
		    	colors[idx*8+4] = lineColors[idx].getR()/255f;
		    	colors[idx*8+5] = lineColors[idx].getG()/255f;
		    	colors[idx*8+6] = lineColors[idx].getB()/255f;
		    	colors[idx*8+7] = (float) lineColors[idx].getAlpha();
	    	}
	    }
	    mGL.bufferData(BufferTarget.ARRAY_BUFFER,
	        Float32Array.create(colors), BufferUsage.STATIC_DRAW);
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexColorBuffer);
	    mGL.vertexAttribPointer(getVertexColorAttrib(), 4, DataType.FLOAT, false, 0, 0);
	    
	    mGL.drawArrays(BeginMode.LINES, 0, lines.length*2);
	    
	    mGL.deleteBuffer(lineVertexPositionBuffer);
	    mGL.deleteBuffer(lineVertexColorBuffer);
	    MODELVIEW.pop();
	    
	    disableArrayDrawing();
	}
	private void renderTriangles(Triangle[] triangles, Color[] triangleColors) {
		assert (triangles != null): triangles;
		boolean uniqueColor = false;
		if (triangleColors == null || triangleColors.length<triangles.length) {
			uniqueColor = true;
			if (triangleColors == null) 
				triangleColors = new Color[1];
			if (triangleColors[0] == null)
				triangleColors[0] = new Color(0, 0, 0, 1);
		}
		enableArrayDrawing();
		disableUniformColor();
		disableTextures();
		disableLighting();
		MODELVIEW.push();
		setMatrixUniforms();

	    WebGLBuffer lineVertexPositionBuffer = mGL.createBuffer();
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexPositionBuffer);
	    float[] vertices = new float[triangles.length*9]; //3 points per triangle * 3 coords per point
	    for(int idx=0; idx<triangles.length; idx++) {
	    	vertices[idx*9] = triangles[idx].getVerticeA().getX();
	    	vertices[idx*9+1] = triangles[idx].getVerticeA().getY();
	    	vertices[idx*9+2] = triangles[idx].getVerticeA().getZ();
	    	vertices[idx*9+3] = triangles[idx].getVerticeB().getX();
	    	vertices[idx*9+4] = triangles[idx].getVerticeB().getY();
	    	vertices[idx*9+5] = triangles[idx].getVerticeB().getZ();
	    	vertices[idx*9+6] = triangles[idx].getVerticeC().getX();
	    	vertices[idx*9+7] = triangles[idx].getVerticeC().getY();
	    	vertices[idx*9+8] = triangles[idx].getVerticeC().getZ();
	    }
	    mGL.bufferData(BufferTarget.ARRAY_BUFFER, 
	        Float32Array.create(vertices), BufferUsage.STATIC_DRAW);
	    mGL.vertexAttribPointer(getVertexPositionAttrib(), 3, DataType.FLOAT, false, 0, 0);
	    
	    WebGLBuffer lineVertexColorBuffer = mGL.createBuffer();
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexColorBuffer);
	    float[] colors = new float[triangles.length*12]; //3 points per triangle * 4 values per color
	    for(int idx=0; idx<triangles.length; idx++) {
	    	if (uniqueColor) {
		    	colors[idx*12] = triangleColors[0].getR()/255f;
		    	colors[idx*12+1] = triangleColors[0].getG()/255f;
		    	colors[idx*12+2] = triangleColors[0].getB()/255f;
		    	colors[idx*12+3] = (float) triangleColors[0].getAlpha();
		    	colors[idx*12+4] = triangleColors[0].getR()/255f;
		    	colors[idx*12+5] = triangleColors[0].getG()/255f;
		    	colors[idx*12+6] = triangleColors[0].getB()/255f;
		    	colors[idx*12+7] = (float) triangleColors[0].getAlpha();
		    	colors[idx*12+8] = triangleColors[0].getR()/255f;
		    	colors[idx*12+9] = triangleColors[0].getG()/255f;
		    	colors[idx*12+10] = triangleColors[0].getB()/255f;
		    	colors[idx*12+11] = (float) triangleColors[0].getAlpha();
	    	} else {
	    		colors[idx*12] = triangleColors[idx].getR()/255f;
		    	colors[idx*12+1] = triangleColors[idx].getG()/255f;
		    	colors[idx*12+2] = triangleColors[idx].getB()/255f;
		    	colors[idx*12+3] = (float) triangleColors[idx].getAlpha();
		    	colors[idx*12+4] = triangleColors[idx].getR()/255f;
		    	colors[idx*12+5] = triangleColors[idx].getG()/255f;
		    	colors[idx*12+6] = triangleColors[idx].getB()/255f;
		    	colors[idx*12+7] = (float) triangleColors[idx].getAlpha();
		    	colors[idx*12+8] = triangleColors[idx].getR()/255f;
		    	colors[idx*12+9] = triangleColors[idx].getG()/255f;
		    	colors[idx*12+10] = triangleColors[idx].getB()/255f;
		    	colors[idx*12+11] = (float) triangleColors[idx].getAlpha();
	    	}
	    }
	    mGL.bufferData(BufferTarget.ARRAY_BUFFER,
	        Float32Array.create(colors), BufferUsage.STATIC_DRAW);
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexColorBuffer);
	    mGL.vertexAttribPointer(getVertexColorAttrib(), 4, DataType.FLOAT, false, 0, 0);
	    
	    mGL.drawArrays(BeginMode.TRIANGLES, 0, triangles.length*3);
	    
	    mGL.deleteBuffer(lineVertexPositionBuffer);
	    mGL.deleteBuffer(lineVertexColorBuffer);
	    MODELVIEW.pop();
	    
	    disableArrayDrawing();
	}
	
	private void renderAxisAlignedBox(AxisAlignedBox box, Color color) {
		assert (box != null): box;
		if (color == null)
			color = new Color(0, 0, 0, 1);
		enableArrayDrawing();
		disableUniformColor();
		disableTextures();
		disableLighting();
		Vector3f min = box.getCorner(AxisAlignedBox.MINIMUM);
		Vector3f max = box.getCorner(AxisAlignedBox.MAXIMUM);
		MODELVIEW.push();
	    MODELVIEW.translate(min.x, min.y, min.z);
	    MODELVIEW.scale(max.x-min.x, max.y-min.y, max.z-min.z);
	    setMatrixUniforms();
	    
	    WebGLBuffer lineVertexPositionBuffer = mGL.createBuffer();
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexPositionBuffer);
	    float[] vertices = {
	    		0f,  0f,  0f,
	    		0f, 0f,  1f,
	    		1f, 0f,  1f,
	    		1f, 0f, 0f,
	    		0f,  0f,  0f,
	    		0f,  1f,  0f,
	    		0f, 1f,  1f,
	    		1f, 1f,  1f,
	    		1f, 1f, 0f,
	    		0f,  1f,  0f,
	    		0f, 1f,  1f,
	    		0f, 0f,  1f,
	    		1f, 0f,  1f,
	    		1f, 1f,  1f,
	    		1f, 1f,  0f,
	    		1f, 0f,  0f,
	    		};
	    mGL.bufferData(BufferTarget.ARRAY_BUFFER, 
	        Float32Array.create(vertices), BufferUsage.STATIC_DRAW);
	    mGL.vertexAttribPointer(getVertexPositionAttrib(), 3, DataType.FLOAT, false, 0, 0);
	    
	    WebGLBuffer lineVertexColorBuffer = mGL.createBuffer();
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexColorBuffer);
	    float[] colors = new float[17*4];
	    for(int idx=0; idx<17; idx++) {
	    	colors[idx*4] = color.getR()/255f;
	    	colors[idx*4+1] = color.getG()/255f;
	    	colors[idx*4+2] = color.getB()/255f;
	    	colors[idx*4+3] = (float) color.getAlpha();
	    }
	    mGL.bufferData(BufferTarget.ARRAY_BUFFER,
	        Float32Array.create(colors), BufferUsage.STATIC_DRAW);
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexColorBuffer);
	    mGL.vertexAttribPointer(getVertexColorAttrib(), 4, DataType.FLOAT, false, 0, 0);
	    
	    mGL.drawArrays(BeginMode.LINE_LOOP, 0, 16);
	    
	    mGL.deleteBuffer(lineVertexPositionBuffer);
	    mGL.deleteBuffer(lineVertexColorBuffer);
	    MODELVIEW.pop();
	    
	    disableArrayDrawing();
	}
	
	public void addCellToRenderQueue(Point3i cell, Color color) {
		if (cell==null)
			throw new IllegalArgumentException("Illegal null cell.");
		if (color==null)
			throw new IllegalArgumentException("Illegal null color.");
		assert (mColoredCellQueue==null): "The colored cell queue is not initialized.";
		if (mColoredCellQueue.size()>3)
			mColoredCellQueue.remove(0);
		mColoredCellQueue.add(Pair.create(cell, color));
	}
	public void clearCellInRenderQueue() {
		if (mColoredCellQueue==null)
			return;
		mColoredCellQueue.clear();
	}
	
	//-------Methods to render scene objects
	private void renderCell(Vector3f pos, Color color, boolean onlySceneCells, boolean withTruePoint) {
		assert (pos != null): pos;
		if (onlySceneCells && !mSceneMgr.inScene(pos))
			return;
		if (color == null)
			color = new Color(0, 0, 0, 1);
		enableArrayDrawing();
		disableUniformColor();
		disableTextures();
		disableLighting();
		int i = Math.round(pos.x);
		int j = Math.round(pos.y);
		int k = Math.round(pos.z);
		MODELVIEW.push();
	    MODELVIEW.translate(i-0.5f, j-0.5f, k-0.5f);
	    setMatrixUniforms();
	    
	    WebGLBuffer lineVertexPositionBuffer = mGL.createBuffer();
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexPositionBuffer);
	    float[] vertices = {
	    		0f,  0f,  0f,
	    		0f, 0f,  1f,
	    		1f, 0f,  1f,
	    		1f, 0f, 0f,
	    		0f,  0f,  0f,
	    		0f,  1f,  0f,
	    		0f, 1f,  1f,
	    		1f, 1f,  1f,
	    		1f, 1f, 0f,
	    		0f,  1f,  0f,
	    		0f, 1f,  1f,
	    		0f, 0f,  1f,
	    		1f, 0f,  1f,
	    		1f, 1f,  1f,
	    		1f, 1f,  0f,
	    		1f, 0f,  0f,
	    		0.5f+(pos.x-i), 0.5f+(pos.y-j), 0.5f+(pos.z-k) //Real point
	    		};
	    mGL.bufferData(BufferTarget.ARRAY_BUFFER, 
	        Float32Array.create(vertices), BufferUsage.STATIC_DRAW);
	    mGL.vertexAttribPointer(getVertexPositionAttrib(), 3, DataType.FLOAT, false, 0, 0);
	    
	    WebGLBuffer lineVertexColorBuffer = mGL.createBuffer();
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexColorBuffer);
	    float[] colors = new float[17*4];
	    for(int idx=0; idx<17; idx++) {
	    	colors[idx*4] = color.getR()/255f;
	    	colors[idx*4+1] = color.getG()/255f;
	    	colors[idx*4+2] = color.getB()/255f;
	    	colors[idx*4+3] = (float) color.getAlpha();
	    }
	    mGL.bufferData(BufferTarget.ARRAY_BUFFER,
	        Float32Array.create(colors), BufferUsage.STATIC_DRAW);
	    mGL.bindBuffer(BufferTarget.ARRAY_BUFFER, lineVertexColorBuffer);
	    mGL.vertexAttribPointer(getVertexColorAttrib(), 4, DataType.FLOAT, false, 0, 0);
	    
	    mGL.drawArrays(BeginMode.LINE_LOOP, 0, 16);
	    if (withTruePoint)
	    	mGL.drawArrays(BeginMode.POINTS, 16, 1);
	    
	    mGL.deleteBuffer(lineVertexPositionBuffer);
	    mGL.deleteBuffer(lineVertexColorBuffer);
	    MODELVIEW.pop();
	    
	    disableArrayDrawing();
	}
	
	
	
	//Features
	public void setRenderMode(int renderMode) {
		if (!mRenderingStarted)
			mRenderMode = renderMode;
	}
	public void setDefaultViewport() {
		mGL.viewport(0, 0, mSurface.getWidth(), mSurface.getHeight());
	}
	public void setViewport(float x, float y, float width, float height) {
		mGL.viewport(x, y, width, height);
	}
	public float getAspectRatio() {
		return mSurface.getWidth()/(float) mSurface.getHeight();
	}
	public Vector4f getDefaultViewport() {
		return new Vector4f(0, 0, mSurface.getWidth(), mSurface.getHeight());
	}
	public void changeProjectionMode() {
//		//Detailed version
//		if (mProjectionMode == PROJ_PERSPECTIVE) {
//			mSceneMgr.mActiveCam.setOrthographicProj(true);
//			mProjectionMode = PROJ_ORTHOGRAPHIC;
//		} else {
//			mSceneMgr.mActiveCam.setOrthographicProj(false);
//			mProjectionMode = PROJ_PERSPECTIVE;
//		}
		//Short version
		mProjectionMode = 1-mProjectionMode;
		mSceneMgr.mActiveCam.setOrthographicProj(mProjectionMode==PROJ_ORTHOGRAPHIC);
	}
	public void enableUniformColor() {
		mGL.uniform1i(mShader.getUniformLocation("uUseUniformColor"), 1);
		disableTextures();
	}
	public void disableUniformColor() {
		mGL.uniform1i(mShader.getUniformLocation("uUseUniformColor"), 0);
	}
	public void enableTextures() {
		mGL.uniform1i(mShader.getUniformLocation("uUseTextures"), 1);
		disableUniformColor();
	}
	public void disableTextures() {
		mGL.uniform1i(mShader.getUniformLocation("uUseTextures"), 0);
	}
	public void enableLighting() {
		mGL.uniform1i(mShader.getUniformLocation("uUseLighting"), 1);
	}
	public void disableLighting() {
		mGL.uniform1i(mShader.getUniformLocation("uUseLighting"), 0);
	}
	public void enableBlending() {
//		mGL.blendFunc(BlendingFactorSrc.SRC_ALPHA, BlendingFactorDest.ONE);
		//Use this function to see transparent colors on white
		mGL.blendFunc(BlendingFactorSrc.SRC_ALPHA, BlendingFactorDest.ONE_MINUS_SRC_ALPHA);
		mGL.enable(EnableCap.BLEND);
	}
	public void disableBlending() {
		mGL.disable(EnableCap.BLEND);
	}
	public void enableArrayDrawing() {
		mGL.enableVertexAttribArray(mShader.getAttributeLocation("aVertexPosition"));
		mGL.enableVertexAttribArray(mShader.getAttributeLocation("aVertexColor"));
	}
	public void disableArrayDrawing() {
		mGL.disableVertexAttribArray(mShader.getAttributeLocation("aVertexPosition"));
	    mGL.disableVertexAttribArray(mShader.getAttributeLocation("aVertexColor"));
	}
	public int getVertexPositionAttrib() {
		return mShader.getAttributeLocation("aVertexPosition");
	}
	public int getVertexColorAttrib() {
		return mShader.getAttributeLocation("aVertexColor");
	}
	
	public void setPMatrixUniform() {
		mGL.uniformMatrix(mShader.getUniformLocation("uPMatrix"), PROJECTION.get());
	}
	public void setMatrixUniforms() {
		mGL.uniformMatrix(mShader.getUniformLocation("uMVMatrix"), MODELVIEW.get());
		MODELVIEW.getInvertTranspose(nMatrix);
		mGL.uniformMatrix(mShader.getUniformLocation("uNMatrix"), nMatrix);
	}
	public void setColorUniform(Vector4f color) {
		mGL.uniform(mShader.getUniformLocation("uColor"), color);
	}

	//Private features
	private void enablePointCloud() {
		for(int i=1; i<Resources.MESH_LIST_LENGTH; i++) {
			if (Resources.mMeshList[i] != null)
				Resources.mMeshList[i].setBeginMode(BeginMode.POINTS);
		}
	}
	private void disablePointCloud() {
		for(int i=1; i<Resources.MESH_LIST_LENGTH; i++) {
			if (Resources.mMeshList[i] != null)
				Resources.mMeshList[i].setBeginMode(BeginMode.TRIANGLES);
		}
	}
	private void enableWireFrame() {
		for(int i=1; i<Resources.MESH_LIST_LENGTH; i++) {
			if (Resources.mMeshList[i] != null)
				Resources.mMeshList[i].setBeginMode(BeginMode.LINE_STRIP);
		}
	}
	private void disableWireFrame() {
		for(int i=1; i<Resources.MESH_LIST_LENGTH; i++) {
			if (Resources.mMeshList[i] != null)
				Resources.mMeshList[i].setBeginMode(BeginMode.TRIANGLES);
		}
	}
	
	//Core
	private Surface3D mSurface;
	private GL2 mGL;
	private AbstractShader mShader;
	private final Matrix3f nMatrix = new Matrix3f();
	private SimEngine mEngine;
	private SceneManager mSceneMgr;
	private Resources mResources;
	private int mRenderMode;
	private int mProjectionMode;
	private boolean mRenderingStarted;
	//Scene Object Queues
	private ArrayList<Integer> mBlocksToRender = new ArrayList<Integer>();
	private ArrayList<Entity> mEntitiesToRender = new ArrayList<Entity>();
	private ArrayList<Pair<Point3i, Color>> mColoredCellQueue = new ArrayList<Pair<Point3i, Color>>();
	//Primitive Queues
	private ArrayList<Pair<Vector3f, Color>> mColoredPointQueue = new ArrayList<Pair<Vector3f, Color>>();
	private ArrayList<Pair<Line, Color>> mColoredLineQueue = new ArrayList<Pair<Line, Color>>();
	private ArrayList<Pair<Triangle, Color>> mColoredTriangleQueue = new ArrayList<Pair<Triangle, Color>>();
	private ArrayList<Pair<AxisAlignedBox, Color>> mColoredAABoxQueue = new ArrayList<Pair<AxisAlignedBox, Color>>();
	
	private Label mRenderTimeLabel;
	private int mRenderTime;
	private int mSceneRenderTime;
	private int mSceneQueuesRenderTime;
	private int mPointsRenderTime;
	private int mLinesRenderTime;
	private int mTrianglesRenderTime;
	private int mAABoxesRenderTime;
	
	/** For resources. */
	private ClientBundleWithLookup getClientBundle() {
		return IResources.INSTANCE;
	}
}
