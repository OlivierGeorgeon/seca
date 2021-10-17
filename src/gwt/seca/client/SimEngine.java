package gwt.seca.client;

import java.util.Random;

import javax.vecmath.Point2i;
import javax.vecmath.Point3i;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Panel;

import gwt.g2d.client.input.KeyboardManager;
import gwt.g2d.client.input.MouseManager;
import gwt.g2d.client.util.FpsTimer;
import gwt.g3d.client.Surface3D;
import gwt.g3d.client.gl2.GL2;
import gwt.g3d.client.gl2.enums.ClearBufferMask;

import gwt.seca.client.agents.AbstractModel;
import gwt.seca.client.agents.Ernest11Model;
import gwt.seca.client.services.DirectXMLStreamTracer;
import gwt.seca.client.services.XMLStreamTracer;
import gwt.seca.client.util.Keys;
import gwt.seca.client.util.Pair;
import gwt.seca.client.util.RandomString;
import gwt.seca.client.util.Ray;
import gwt.seca.client.util.SecaMath;
import gwt.seca.client.util.SecaMath.CardinalDirection;

/**
 * The whole simulation engine.
 * Provide methods to manage the simulation from the buttons.
 * When the simulation starts, it instantiates the scene, the renderer, and the sound manager.
 * Control the simulation loop.
 * Handles user events in the simulation surface.
 * @author Olivier
 */
public class SimEngine implements MouseDownHandler, MouseUpHandler, MouseMoveHandler, MouseWheelHandler {
	final static int SIMU_INIT = 1;		//Transition 	-> MENU
	final static int SIMU_MENU = 2;		//Empty			-> STARTING
	final static int SIMU_STARTING = 3; //Transition 	-> FREEZE
	final static int SIMU_FREEZE = 4;	//Loop
	final static int SIMU_RUN = 5;		//Loop
	final static int SIMU_STEP = 6;		//Transition	-> FREEZE
	final static int SIMU_RESTART = 7;	//Transition 	-> STARTING
	final static int SIMU_EXIT = 8;		//Transition 	-> INIT
	
	//From parent
	private Surface3D mSurface;

	//Intern attributes
	private int mSimuState;
	private int mError = 0;
//	protected GL2 mGL;
	
	FpsTimer mTimer;
	private double mLastTime;
	private final Point2i mMousePos = new Point2i();
	private final Point2i mLastMousePos = new Point2i();
	private final Point2i mMouseDownPos = new Point2i();
	private boolean mMouseDown;

	/** Keyboard handlers */
	private final KeyboardManager mKeyboardManager = new KeyboardManager();
	/** Mouse handler registration*/
	private HandlerRegistration mMouseDownRegistration, mMouseUpRegistration;//, mMouseMoveRegistration, 
	private HandlerRegistration mMouseWheelRegistration;
	/** The scene */
	private SceneManager mSceneMgr;
	private Renderer mRenderer;
	private SoundManager mSoundMgr;
	/** Events handling */
	private boolean mLastKeyUp;
	private double mLastKeyTime;
	private boolean mViewChanged;
	private Point3i mKeyboardCursorPos = new Point3i(0, 0, 1);
	private Point3i mMouseCursorPos = new Point3i();
	private CardinalDirection mMouseCursorDir;
	/** Time computation */
	private float mTotalTime;
	private float mHandleInputsTime;
	private float mUpdateSceneTime;
	private float mRenderFrameTime;
	/** Configuration */
	private final boolean mDisplayCamTarget = false;
	private final boolean mDisplayKeyCursor = true;
	private final boolean mDisplayCameras = false;
	private final boolean mInteractiv = true;
	private final boolean mSaveTrace = true;
	private final boolean mDemoMode = false;
	/** Dashboard widgets*/
	private Panel mDebugPanel;
	private Label mComputingTimeLabel;
	private Label mMousePickingLabel;
	private Panel mSimulation;
	private Label mFPSLabel;
	private ListBox mBlockListBox;
	private Panel mTracePanel;
	private Frame mALiteFrame;
	private String mTracerBaseURL;
	private String mStreamCookie;
	private String mTraceTransfo;
	
	/**
	 * Initializes the engine.
	 * 
	 * @param surface The surface to display the simulation.
	 * @return true if the surface was ok.
	 */
	public boolean init(Surface3D surface) 
	{
		if (mSaveTrace)
			this.initTracePanel();
		mSurface = surface;
		if (surface.getGL() == null) {
			Window.alert("No WebGL context found. Exiting.");
			return false;
		}
		return true;
	}
	
	/**
	 * Initialize the tracer and the trace panel.
	 */
	public void initTracePanel()
	{
		// mTracerBaseURL = "http://vm.liris.cnrs.fr:34080/abstract/lite"; // vm 
		mTracerBaseURL = "http://macbook-pro-de-olivier-2.local/alite/"; // Local
		mStreamCookie = "seca-" + RandomString.getAlphaNum(10);
		// mTraceTransfo = "vhf-xWXH-d"; // vm admin
		mTraceTransfo = "LVBpULgQjM"; // local Ernest104
		mTracePanel = Seca.dashboardMgr.createPanel(null, "Trace Ernest 1", false, true);
		mALiteFrame = Seca.dashboardMgr.createFrame(mTracePanel, mTracerBaseURL + "/autoconf-seca.php?streamcookie=" + mStreamCookie + "&transformationId=" + mTraceTransfo);
		mALiteFrame.setHeight("400px");
		mALiteFrame.setWidth("1000px");
	}
	
	/**
	 * Starts the simulation.
	 * SIMU_INIT.
	 * Create a clock that will trigger the simulation loop.
	 */
	public void start() {
		mSimuState = SIMU_INIT;
		mTimer = new FpsTimer(60) {
			public void update() {
				float startTime = System.currentTimeMillis();
				updateEngine();
				mSurface.setFocus(true);
				mTotalTime = System.currentTimeMillis() - startTime;
			}
		};
		mTimer.start();
	}
	/**
	 * Stop (Exit) the simulation.
	 * SIMU_EXIT.
	 */
	public void stop() {
		mSimuState = SIMU_EXIT;
	}
	/**
	 * Play the simulation.
	 * SIMU_RUN.
	 */
	public void play() {
		mSimuState = SIMU_RUN;
	}
	/**
	 * Pause the simulation
	 * SIMU_FREEZE.
	 */
	public void pause() {
		mSimuState = SIMU_FREEZE;
	}
	/**
	 * Play the simulation one step.
	 * SIMU_STEP.
	 * Trigger one simulation cycle then freeze.
	 */
	public void step() {
		mSimuState = SIMU_STEP;
	}
	/**
	 * Reset the simulation
	 * SIMU_RESTART.
	 * Dump the board and start the simulation.
	 */
	public void restart() {
		mSimuState = SIMU_RESTART;
	}

	private int updateEngine() {
		//Implementation of the main simulation loop
		mError = 0;
		//What state is the simulation loop in
		switch(mSimuState) {
		case SIMU_INIT: //The simulation is initializing
			//Allocate all memory and resources
			initImpl();
			mSimuState = SIMU_MENU; //Move to menu state
			break;
		case SIMU_MENU:  //The simulation is in the menu mode
			//Call the main menu function and let it switch states
			//mSimuState = menu();
			mSimuState = SIMU_STARTING; //Note: We force to go in the next state here
			break;
		case SIMU_STARTING:   //The simulation is about to run
			//This state is optional, but usually used to
			// set things up right before the simulation is run
			loadImpl();
			mViewChanged = true;
			mSimuState = SIMU_FREEZE; //Switch to freeze state
			break;
		case SIMU_FREEZE:    //The simulation is frozen
			//This section let the user move in the scene but the agents are paused.
			simulationLoop(true);
			//The only way that state can be changed is through user interaction.
			break;
		case SIMU_RUN:    //The simulation is now running
			// this section contains the entire simulation logic loop
			simulationLoop(false);
			//The only way that state can be changed is through user interaction.
			break;
		case SIMU_STEP:    //The simulation is now running
			// this section contains the entire simulation logic loop
			simulationLoop(false);
			mSimuState = SIMU_FREEZE; //Switch back to the freeze state
			break;
		case SIMU_RESTART:  //The simulation is restarting
			//This section is a cleanup state used to
			// fix up any loose ends before
			// running again
			//disposeImpl(true);
			//mSimuState = SIMU_MENU; //Switch back to the menu
			
			disposeImpl(false); // Dump the board
			mSimuState = SIMU_INIT; //Switch back to the initialization
			break;
		case SIMU_EXIT:   //The simulation is exiting
			//If the simu is in this state then
			// it's time to bail and kill everything
			disposeImpl(false); //Release_And_Cleanup();
			mTimer.cancel();
			// set the error word to whatever
			mError = 1;
			//Note: We don't have to switch states. 
			//The init state will be selected when the method start is called.
			break;
		default: break;
		}  // end switch

		// return error code to operating system
		return mError;
	}
	
	private void initImpl() {
		// Initialize the Debug dashboard panel
//		mDebugPanel = Seca.dashboardMgr.createPanel(null, "Debug", false, true);
//		mComputingTimeLabel = Seca.dashboardMgr.createLabel(mDebugPanel);
//		mMousePickingLabel = Seca.dashboardMgr.createLabel(mDebugPanel);
		
		// Initialize the Interaction dashboard panel
//		mSimulation = Seca.dashboardMgr.createPanel(null, "Interaction", false, true);
//		mFPSLabel = Seca.dashboardMgr.createLabel(mSimulation);
//		mBlockListBox = Seca.dashboardMgr.createListBox(mSimulation, "Choose an object to insert ");
//		for(int i=0; i<Block.BLOCK_LIST_LENGTH; i++)
//			mBlockListBox.addItem(Block.blockList[i].getName());
//		mBlockListBox.setSelectedIndex(0);
		
		// Initialize the scene managers
		mSceneMgr = new SceneManager();
		mRenderer = new Renderer(this, mSceneMgr);
		mRenderer.setRenderMode(Renderer.RENDER_SOLID_TEXTURE);
		mSceneMgr.setRenderer(mRenderer);
		mSoundMgr = new SoundManager();
		mSceneMgr.setSoundManager(mSoundMgr);
		//Initialization of the event handlers
		mKeyboardManager.manage(mSurface);
		mKeyboardManager.setPreventDefault(true);
		mMouseDownRegistration = mSurface.addMouseDownHandler(this);
	    mMouseUpRegistration = mSurface.addMouseUpHandler(this);
//	    mMouseMoveRegistration = mSurface.addMouseMoveHandler(this);
//	    mMouseWheelRegistration = mSurface.addMouseWheelHandler(this);
	    //Other initializations
	    mMouseCursorDir = CardinalDirection.None;
		mLastTime = System.currentTimeMillis();
		
	}
	private void loadImpl() {
		//Create Agents
//		//For Ernest7
//		AbstractModel ernest1 = mSceneMgr.createAgent("Ernest1", SceneManager.Model.Ernest7);
//		ernest1.init();
//		ernest1.setPosition(new Vector3f(1, 6, 1));
//		//For Ernest8
//		AbstractModel ernest1 = mSceneMgr.createAgent("Ernest1", SceneManager.Model.Ernest8);
//		ernest1.init();
//		ernest1.setPosition(new Vector3f(4, 1, 1));
		//For Ernest10
		//Ernest10Model ernest1 = (Ernest10Model) mSceneMgr.createAgent("Ernest 1", SceneManager.Model.Ernest10);
		//For Ernest11
		Ernest11Model ernest1 = (Ernest11Model) mSceneMgr.createAgent(1, SceneManager.Model.Ernest11);
		
		if (mSaveTrace) {
			XMLStreamTracer tracer = new XMLStreamTracer(mTracerBaseURL + "/php/stream/", mStreamCookie);
//			DirectXMLStreamTracer tracer = new DirectXMLStreamTracer(mTracerBaseURL + "/php/stream/", mStreamCookie);
			ernest1.setTracer(tracer);
		}
		ernest1.init();
		ernest1.setPosition(new Vector3f(1, 4, 1));
		ernest1.setOrientation(new Vector3f(0, 0, 1.4f));
		
		Ernest11Model ernest2 = (Ernest11Model) mSceneMgr.createAgent(2, SceneManager.Model.Ernest11);
		ernest2.init();
		ernest2.setPosition(new Vector3f(3, 3, 1));
		ernest2.setOrientation(new Vector3f(0, 0, .1f));
		
		//Create cameras
		Camera observerCam = mSceneMgr.createCamera("ObserverCam");
		observerCam.setEye(7, -4, 15);
		observerCam.setTarget(7, 5, 1);
		observerCam.setUp(0, 0, 1);
		observerCam.bind();
//		Camera agentCam = mSceneMgr.createCamera("AgentCam");
//		agentCam.setEye(0, 0, 5);
//		agentCam.setTarget(7, 7, 0);
//		agentCam.setUp(0, 0, 1);
//		agentCam.setViewport(0, 0, 200, 1);
		Camera agentCam = mSceneMgr.createCamera("AttachedCam");
		agentCam.setUp(0, 0, 1);
		agentCam.followEntity(ernest1, new Vector3f(-2,0,1), new Vector3f(0,0,.5f), null);
		//agentCam.bind();
	}
	private void simulationLoop(boolean frozenScene) {
		
		//Handle inputs
		mHandleInputsTime = System.currentTimeMillis();
		handleKeys();
		mHandleInputsTime = System.currentTimeMillis() - mHandleInputsTime;
		mUpdateSceneTime = 0;
		mRenderFrameTime = 0;
		if (!frozenScene) {
			//Clear the display
			mRenderer.clearDisplay();
			//The scene is not paused
			//Perform logic and ai
			double currTime = System.currentTimeMillis();
			double elapsedTime = currTime - mLastTime;
			mLastTime = currTime;
			mUpdateSceneTime = System.currentTimeMillis();
			mSceneMgr.update(elapsedTime);
			mUpdateSceneTime = System.currentTimeMillis() - mUpdateSceneTime;
			//Display the next frame of animation
			mRenderFrameTime = mRenderer.renderFrame();
			//Dashboard
			
		} else if (mViewChanged) {
			//Clear the display
			mRenderer.clearDisplay();
			//Display the next frame of animation
			mRenderFrameTime = System.currentTimeMillis();
			mRenderer.renderFrame();
			mRenderFrameTime = System.currentTimeMillis() - mRenderFrameTime;
			mViewChanged = false;
		}
		
		//TODO Remove the memory leak in the HUD
		//mHudMgr.render();
		if (mFPSLabel!=null)
			mFPSLabel.setText("FPS: " + getFPS());
		if (mComputingTimeLabel!=null)
			mComputingTimeLabel.setText("Computing time: "+mTotalTime+" (Inputs: "+mHandleInputsTime+"ms, Update: "+mUpdateSceneTime+"ms, Render: "+mRenderFrameTime+"ms)");
		if (mMousePickingLabel!=null) {
			if (mMouseCursorPos==null)
				mMousePickingLabel.setText("Mouse Picking: null");
			else
				mMousePickingLabel.setText("Mouse Picking: " + mMouseCursorPos.toString());
		}
	}
	private void disposeImpl(boolean onlyScene) {
		mSceneMgr.clear();
		if (!onlyScene) {
			if (mRenderer != null)
				mRenderer.dispose();
			if (mKeyboardManager != null)
				mKeyboardManager.unmanage();
			if (mMouseDownRegistration != null)
				mMouseDownRegistration.removeHandler();
			if (mMouseUpRegistration != null)
				mMouseUpRegistration.removeHandler();
//			if (mMouseMoveRegistration != null)
//				mMouseMoveRegistration.removeHandler();
			if (mMouseWheelRegistration != null)
				mMouseWheelRegistration.removeHandler();
//			mMeshBox.dispose();
//			mMeshSphere.dispose();
//			mShader.dispose();
			//Dashboard
			if (Seca.dashboardMgr != null) {
				Seca.dashboardMgr.deleteWidgets(mComputingTimeLabel, mMousePickingLabel, mFPSLabel, mBlockListBox);
				Seca.dashboardMgr.deletePanels(mDebugPanel, mSimulation);
			}
		}
	}
	
	/**
	 * Returns the GL surface.
	 * @return The surface
	 */
	public Surface3D getSurface() {
		return mSurface;
	}
	
	/**
	 * Returns the position of the cursor controlled with the keyboard.
	 * @return The position of the cursor controlled with the keyboard.
	 */
	public Point3i getKeyboardCursorPosition() {
		return new Point3i(mKeyboardCursorPos);
	}
	/**
	 * Returns the position of the cursor controlled with the mouse.
	 * @return The position of the cursor controlled with the mouse.
	 */
	public Point3i getMouseCursorPosition() {
		return new Point3i(mMouseCursorPos);
	}
	/**
	 * Returns the direction of the face selected by the cursor controlled with the mouse.
	 * @return The direction of the face selected by the cursor controlled with the mouse.
	 */
	public CardinalDirection getMouseCursorDirection() {
		return mMouseCursorDir;
	}
	
	/**
	 * Returns the frame rate.
	 * @return The frame rate.
	 */
	public float getFPS() {
		float fps = mTimer.getFps();
		fps = Math.round(fps*100)/100f;
		return fps;
	}
	
	/**
	 * @return true if the main camera's target is configured to be rendered.
	 */
	public boolean isCamTargetVisible() {
		return mDisplayCamTarget;
	}
	/**
	 * @return true if the keboard cursor is configured to be rendered.
	 */
	public boolean isKeyCursorVisible() {
		return mDisplayKeyCursor;
	}
	/**
	 * @return true if the cameras are configured to be rendered.
	 */
	public boolean areCamerasVisible() {
		return mDisplayCameras;
	}
	
//	private void computeMousePicking(Point2i mouse) {
//		try {
//			Ray ray = mSceneMgr.mActiveCam.getCameraToViewportRay(mouse.x, mouse.y);
//			ray.normalize();
//			mRenderer.clearCellInRenderQueue();
////			mRenderer.addRayToRenderQueue(ray, 20);
//			Pair<Point3i, CardinalDirection> result = mSceneMgr.rayTrace(ray, 50);
//			if (result.mRight != CardinalDirection.None) {
//				mMouseCursorPos.set(result.mLeft);
//				mMouseCursorDir = result.mRight;
//			} else {
//				mMouseCursorPos.set(-1, -1, -1); //Out of the grid
//				mMouseCursorDir = CardinalDirection.None;
//			}
//		} catch (SingularMatrixException e) {
//			mMouseCursorPos.set(-1, -1, -1); //Out of the grid
//			mMouseCursorDir = CardinalDirection.None;
//			Seca.logPrinter.println("One of the matrices cannot be inverted");
//		}
//	}
	
	/** Events handlers */
	/**
	 * Handle events from the keyboard.
	 */
	private void handleKeys() {
		
		double currTime = System.currentTimeMillis();
		double elapsedTime = currTime - mLastKeyTime;
		
		//Rendering management
		if (!mKeyboardManager.isButtonDown(Keys.KEY_CTRL) && !mKeyboardManager.isButtonDown(Keys.KEY_ALT)) {
			if (mKeyboardManager.isButtonDown(Keys.KEY_F1)) {
				mRenderer.setRenderMode(Renderer.RENDER_POINTCLOUD);
				mViewChanged = true;
			} else if (mKeyboardManager.isButtonDown(Keys.KEY_F2)) {
				mRenderer.setRenderMode(Renderer.RENDER_WIREFRAME);
				mViewChanged = true;
			} else if (mKeyboardManager.isButtonDown(Keys.KEY_F3)) {
				mRenderer.setRenderMode(Renderer.RENDER_SOLID_COLOR);
				mViewChanged = true;
			} else if (mKeyboardManager.isButtonDown(Keys.KEY_F4)) {
				mRenderer.setRenderMode(Renderer.RENDER_SOLID_TEXTURE);
				mViewChanged = true;
			} else if (mKeyboardManager.isButtonDown(Keys.KEY_F5)) {
				mRenderer.changeProjectionMode();
				mViewChanged = true;
			}
		}
		//Cursor management:
		//- Translate the cursor: ALT + Arrows
		//- Fire the cursor: ALT + F1-F12
		if (!mKeyboardManager.isButtonDown(Keys.KEY_CTRL) && !mKeyboardManager.isButtonDown(Keys.KEY_ALT) && elapsedTime > 0 && mInteractiv) {
			mLastKeyTime = currTime;
			mViewChanged = true;
			//Translation keys
//			if (mKeyboardManager.isButtonDown(Keys.KEY_PAGE_UP))
//				mKeyboardCursorPos.add(new Point3i(0, 0, 1));
//			else if (mKeyboardManager.isButtonDown(Keys.KEY_PAGE_DOWN))
//				mKeyboardCursorPos.add(new Point3i(0, 0, -1));
			if (mKeyboardManager.isButtonDown(Keys.KEY_LEFT_ARROW)) {
				if (mLastKeyUp) mKeyboardCursorPos.add(new Point3i(-1, 0, 0));
				mLastKeyUp = false;}
			else if (mKeyboardManager.isButtonDown(Keys.KEY_RIGHT_ARROW)) {
				if (mLastKeyUp) mKeyboardCursorPos.add(new Point3i(1, 0, 0));
				mLastKeyUp = false;}
			else if (mKeyboardManager.isButtonDown(Keys.KEY_UP_ARROW)) {
				if (mLastKeyUp) mKeyboardCursorPos.add(new Point3i(0, 1, 0));
				mLastKeyUp = false;}
			else if (mKeyboardManager.isButtonDown(Keys.KEY_DOWN_ARROW)) {
				if (mLastKeyUp) mKeyboardCursorPos.add(new Point3i(0, -1, 0));
				mLastKeyUp = false;}
			//Action keys
			else if (mKeyboardManager.isButtonDown(Keys.KEY_SPACE)) {
				if (mLastKeyUp) mSceneMgr.setBlockID(mKeyboardCursorPos, 0);
				mLastKeyUp = false;}
			else if (mKeyboardManager.isButtonDown(Keys.KEY_W)) {
				if (mLastKeyUp)	mSceneMgr.setBlockID(mKeyboardCursorPos, Block.stone.getBlockID());
				mLastKeyUp = false;}
//			else if (mKeyboardManager.isButtonDown(Keys.KEY_F3))
//				mSceneMgr.setBlockID(mKeyboardCursorPos, Block.grass.getBlockID());
//			else if (mKeyboardManager.isButtonDown(Keys.KEY_F4))
//				mSceneMgr.setBlockID(mKeyboardCursorPos, Block.crate.getBlockID());
			else if (mKeyboardManager.isButtonDown(Keys.KEY_Y)) {
				if (mLastKeyUp) mSceneMgr.setBlockID(mKeyboardCursorPos, Block.plantStoplightYellow.getBlockID());
				mLastKeyUp = false;}
			else if (mKeyboardManager.isButtonDown(Keys.KEY_G)) {
				if (mLastKeyUp) mSceneMgr.setBlockID(mKeyboardCursorPos, Block.plantStoplightGreen.getBlockID());
				mLastKeyUp = false;}
//			else if (mKeyboardManager.isButtonDown(Keys.KEY_F7))
//				mSceneMgr.setBlockID(mKeyboardCursorPos, Block.ballBlue.getBlockID());
			else if (mKeyboardManager.isButtonDown(Keys.KEY_F)) {
				if (mLastKeyUp) mSceneMgr.setBlockID(mKeyboardCursorPos, Block.animalFish.getBlockID());
				mLastKeyUp = false;}
			else if (mKeyboardManager.isButtonDown(Keys.KEY_E) && mSceneMgr.inScene(mKeyboardCursorPos)) {
				if (mLastKeyUp)
				{
					AbstractModel newErnest = mSceneMgr.createAgent();
					newErnest.init();
					newErnest.setPosition(mKeyboardCursorPos);
				}
				mLastKeyUp = false;
			}
			// Shift to the next camera
			else if (mKeyboardManager.isButtonDown(KeyCodes.KEY_TAB)) {// && !mDemoMode)
				if (mLastKeyUp) mSceneMgr.getNextCamera(mSceneMgr.mActiveCam).bind();
				mLastKeyUp = false;
			}
			else
				mLastKeyUp = true;
		}
		//Add food at random positions
		if (mKeyboardManager.isButtonDown(Keys.KEY_SPACE) && elapsedTime > 100) {
			mLastKeyTime  = currTime;
			mViewChanged = true;
			Random rand = new Random();
			SoundManager.bubble9.play();
			for(int counter=1000; counter>0; counter--) {
				int i = rand.nextInt(mSceneMgr.getWidth());
				int j = rand.nextInt(mSceneMgr.getDepth());
				if (mSceneMgr.getBlockID(i, j, 1, "any") == 0) {
//					mSceneMgr.setBlockID(i, j, 1, Block.ballBlue.getBlockID()+rand.nextInt(2));
//					mSceneMgr.setBlockID(i, j, 1, Block.animalFish.getBlockID());
					break;
				}
			}
		}
			
		float scale = 1;
		//Precision key
		if (mKeyboardManager.isButtonDown(Keys.KEY_SHIFT)) {
			scale = 0.1f;
		}
		//- Translate the active camera: CTRL + Arrows
		if (mKeyboardManager.isButtonDown(Keys.KEY_CTRL) && !mKeyboardManager.isButtonDown(Keys.KEY_ALT)) {
			mViewChanged = true;
			//Translation keys
			if (mKeyboardManager.isButtonDown(Keys.KEY_PAGE_UP) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_9)) {
				mSceneMgr.mActiveCam.translate(0, 0, 1);
			}
			if (mKeyboardManager.isButtonDown(Keys.KEY_PAGE_DOWN) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_3)) {
				mSceneMgr.mActiveCam.translate(0, 0, -1);
			}
			int count = 0;
			if (mKeyboardManager.isButtonDown(Keys.KEY_LEFT_ARROW) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_4)) {
				count++;
				mSceneMgr.mActiveCam.translateAlongScreen(new Vector2f(-.1f*scale, 0f*scale)); //Axis are X and Y
			}
			if (mKeyboardManager.isButtonDown(Keys.KEY_RIGHT_ARROW) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_6)) {
				count++;
				if (count>1) 
					scale /= 1.42f; //sqrt(2)=1.4142
				mSceneMgr.mActiveCam.translateAlongScreen(new Vector2f(.1f*scale, 0f*scale)); //Axis are X and Y
			}
			if (mKeyboardManager.isButtonDown(Keys.KEY_UP_ARROW) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_8)) {
				count++;
				if (count>1) 
					scale /= 1.42f;
				mSceneMgr.mActiveCam.translateAlongScreen(new Vector2f(0f*scale, .1f*scale)); //Axis are X and Y
			}
			if (mKeyboardManager.isButtonDown(Keys.KEY_DOWN_ARROW) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_2)) {
				count++;
				if (count>1) 
					scale /= 1.42f;
				mSceneMgr.mActiveCam.translateAlongScreen(new Vector2f(0f*scale, -.1f*scale)); //Axis are X and Y
			}
		}
		
		//- Rotate the active camera: ALT + Arrows
		if (!mKeyboardManager.isButtonDown(Keys.KEY_CTRL) && mKeyboardManager.isButtonDown(Keys.KEY_ALT)) {
			mViewChanged = true;
			//Rotation keys
			if (mKeyboardManager.isButtonDown(Keys.KEY_PAGE_UP) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_9)) {
				mSceneMgr.mActiveCam.zoom(-5f*scale);
				mViewChanged = true;
			}
			if (mKeyboardManager.isButtonDown(Keys.KEY_PAGE_DOWN) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_3)) {
				mSceneMgr.mActiveCam.zoom(5f*scale);
				mViewChanged = true;
			}
			if (mKeyboardManager.isButtonDown(Keys.KEY_LEFT_ARROW) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_4)) {
				mSceneMgr.mActiveCam.rotateAroundTarget(0f, -1f*scale); //Rotation Axis are X and Z
				mViewChanged = true;
			}
			if (mKeyboardManager.isButtonDown(Keys.KEY_RIGHT_ARROW) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_6)) {
				mSceneMgr.mActiveCam.rotateAroundTarget(0f, 1f*scale); //Rotation Axis are X and Z
				mViewChanged = true;
			}
			if (mKeyboardManager.isButtonDown(Keys.KEY_UP_ARROW) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_8)) {
				mSceneMgr.mActiveCam.rotateAroundTarget(-1f*scale, 0); //Rotation Axis are X and Z
				mViewChanged = true;
			}
			if (mKeyboardManager.isButtonDown(Keys.KEY_DOWN_ARROW) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_2)) {
				mSceneMgr.mActiveCam.rotateAroundTarget(1f*scale, 0); //Rotation Axis are X and Z
				mViewChanged = true;
			}
			if (mKeyboardManager.isButtonDown(12) || mKeyboardManager.isButtonDown(Keys.KEY_NUMPAD_5)) {
				mViewChanged = true;
				Vector3f eyeOffset = mSceneMgr.mActiveCam.getEyeOffset();
				eyeOffset.setY((float) -Math.sqrt(eyeOffset.getX()*eyeOffset.getX()+eyeOffset.getY()*eyeOffset.getY()));
				eyeOffset.setX(0);
				mSceneMgr.mActiveCam.setEyeOffset(eyeOffset);
			}
		}
	}
	
	public void onMouseDown(MouseDownEvent event) {
		mMouseDownPos.set(event.getX(), event.getY());
		mLastMousePos.set(event.getX(), event.getY());
		mMouseDown = true;
	}
	public void onMouseMove(MouseMoveEvent event) {
		mMousePos.set(event.getX(), event.getY());
		event.getNativeButton();
		if (!mMouseDown) {
			//Mouse move without any button down
			mViewChanged = true;
			//computeMousePicking(mMousePos);
			return;
		} else if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
			//Mouse move with the left button down
			//mSceneMgr.mActiveCam.rotate(new Vector3f(-(mLastMousePos.y - mMousePos.y)*1.0f, 0, (mLastMousePos.x - mMousePos.x)*1.0f)); //Rotation Axis are X and Z
			mViewChanged = true;
			mLastMousePos.set(mMousePos);
		}
	}
	public void onMouseUp(MouseUpEvent event) {
		if (mMouseDown) {
			Point2i mouseMove = new Point2i(event.getX(), event.getY());
			mouseMove.sub(mMouseDownPos);
			mouseMove.absolute();
			if (mouseMove.getX()+mouseMove.getY() < 5) {
				//The event is a mouse click
				if (event.getNativeButton() == NativeEvent.BUTTON_LEFT) {
					//The event is a left click
					if (mSceneMgr.inScene(mMouseCursorPos) && mSceneMgr.getBlock(mMouseCursorPos).isBuildingBlock()) {
						//The block under the mouse allow to build another block in the adjacent cell
						Point3i pos = SecaMath.getAdjacent(mMouseCursorPos, mMouseCursorDir);
//						if (mSceneMgr.inScene(pos) && mSceneMgr.getBlockID(pos)==0) {
//							//The adjacent cell is in the scene and is empty
//							int blockID = mBlockListBox.getSelectedIndex();
//							mSceneMgr.setBlockID(pos, blockID);
//						}
					}
				} else if (event.getNativeButton() == NativeEvent.BUTTON_RIGHT) {
					//The event is a right click
					mSceneMgr.setBlockID(mMouseCursorPos, 0);
				}
			}
		}
		mMouseDown = false;
	}
	public void onMouseWheel(MouseWheelEvent event) {
		mViewChanged = true;
		mSceneMgr.mActiveCam.translateToTarget(-event.getDeltaY());
		event.preventDefault();
	}

}
