package gwt.seca.client;

import gwt.g3d.client.Surface3D;
import gwt.g3d.client.gl2.WebGLContextAttributes;
import gwt.seca.client.services.LogPrinter;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Main class in Seca
 * @author Olivier
 */
public class Seca implements EntryPoint {
	
	/**
	 * The public dashboard manager
	 */
	final public static DashboardManager dashboardMgr = new DashboardManager();
	/**
	 * The public log printer
	 */
	final public static LogPrinter logPrinter = new LogPrinter();

	private SimEngine mSimu = new SimEngine();
	private Surface3D mSurface;
	private boolean mRunnable = false;
	//UI
//	private final Label mFPSLabel = new Label();
	private Button mStartButton;
	private Button mStopButton;
	private Button mPlayButton;
	private Button mPauseButton;
	private Button mStepButton;
	private Button mRestartButton;

	/**
	 * This is the entry point method.
	 * (Called when the web page is loaded)
	 * Instantiate a simulation engine.
	 * Initialize the visualization
	 * Start the simulation engine.
	 */
	public void onModuleLoad() {
		// Initialize the simulation
		WebGLContextAttributes contextAttribs = new WebGLContextAttributes();
		contextAttribs.setStencilEnable(true);
		mSurface = new Surface3D(1000, 500, contextAttribs);
//		mSurface.sinkEvents(Event.ONMOUSEDOWN | Event.ONMOUSEMOVE | Event.ONMOUSEUP | Event.ONMOUSEWHEEL);
		mRunnable = mSimu.init(mSurface);
		
		// Initialize the buttons
		initButtons();
		RootPanel rp = RootPanel.get("seca-Player");
		if (rp==null)
			rp = RootPanel.get();
		rp.add(createWidget());
		mSurface.setFocus(true);
		
		// Starts the simulation
		mSimu.start();
	}
	
	/**
	 * Initialize the buttons.
	 */
	private void initButtons() {
		mStartButton = new Button("Initialize", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				mStartButton.setEnabled(false);
				mStopButton.setEnabled(true);
				mPlayButton.setEnabled(true);
				mPauseButton.setEnabled(false);
				mStepButton.setEnabled(true);
				mRestartButton.setEnabled(false);
				mSimu.start();
				mSurface.setFocus(true);
			}
		});
		mStopButton = new Button("Kill", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				mStartButton.setEnabled(true);
				mStopButton.setEnabled(false);
				mPlayButton.setEnabled(false);
				mPauseButton.setEnabled(false);
				mStepButton.setEnabled(false);
				mRestartButton.setEnabled(false);
				mSimu.stop();
				mSurface.setFocus(true);
			}
		});
		mPlayButton = new Button("Play", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				mStartButton.setEnabled(false);
				mStopButton.setEnabled(true);
				mPlayButton.setEnabled(false);
				mPauseButton.setEnabled(true);
				mStepButton.setEnabled(false);
				mRestartButton.setEnabled(true);
				mSimu.play();
				mSurface.setFocus(true);
			}
		});
		mPauseButton = new Button("Pause", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				mStartButton.setEnabled(false);
				mStopButton.setEnabled(true);
				mPlayButton.setEnabled(true);
				mPauseButton.setEnabled(false);
				mStepButton.setEnabled(true);
				mRestartButton.setEnabled(true);
				mSimu.pause();
				mSurface.setFocus(true);
			}
		});
		mStepButton = new Button("Step", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				mStartButton.setEnabled(false);
				mStopButton.setEnabled(true);
				mPlayButton.setEnabled(true);
				mPauseButton.setEnabled(false);
				mStepButton.setEnabled(true);
				mRestartButton.setEnabled(true);
				mSimu.step();
				mSurface.setFocus(true);
			}
		});
		mRestartButton = new Button("Reset", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				// TODO Auto-generated method stub
				mStartButton.setEnabled(false);
				mStopButton.setEnabled(true);
				mPlayButton.setEnabled(true);
				mPauseButton.setEnabled(false);
				mStepButton.setEnabled(true);
				mRestartButton.setEnabled(false);
				mSimu.restart();
				mSurface.setFocus(true);
			}
		});
		if (mRunnable)
			mStartButton.setEnabled(true);
		else
			mStartButton.setEnabled(false);
		mStopButton.setEnabled(false);
		mPlayButton.setEnabled(true);
		mPauseButton.setEnabled(false);
		mStepButton.setEnabled(true);
		mRestartButton.setEnabled(true);
	}
	
	/**
	 * Creates the main widget.
	 */
	private Widget createWidget() {
		Panel widget = new FlowPanel();
		// The button panel
		Panel buttonPanel = new HorizontalPanel();
		//buttonPanel.setPixelSize(1000, 20);
//		buttonPanel.add(mStartButton);
//		buttonPanel.add(mStopButton);
		buttonPanel.add(mPlayButton);
		buttonPanel.add(mPauseButton);
		buttonPanel.add(mRestartButton);
		widget.add(buttonPanel);
		
		// The display of the Frame Per Second rate
		//widget.add(mFPSLabel);
		
		// The simulation window
		Panel surfacePanel = new FlowPanel();
		//surfacePanel.setTitle("surfacePanel");
		surfacePanel.setPixelSize(mSurface.getWidth(), mSurface.getHeight());
		surfacePanel.add(mSurface);
		widget.add(surfacePanel);
		//leftPanel.getElement().setId("seca.player");
		return widget;
	}
	
//	public void onBrowserEvent(Event event) {
//		GWT.log("onBrowserEvent", null);
//		event.cancelBubble(true);//This will stop the event from being propagated
//		event.preventDefault();
//		switch (DOM.eventGetType(event)) {
//		case Event.ONMOUSEUP:
////			if (DOM.eventGetButton(event) == Event.BUTTON_LEFT) {
////				GWT.log("Event.BUTTON_LEFT", null);
////				mSimu.onClick(this, event);
////			}
////
////			if (DOM.eventGetButton(event) == Event.BUTTON_RIGHT) {
////				GWT.log("Event.BUTTON_RIGHT", null);
////				mSimu.onRightClick(this, event);
////			}
//			mSimu.onMouseUp(event);
//			break;
//		case Event.ONDBLCLICK:
//			break;
//
//		case Event.ONCONTEXTMENU:
//			GWT.log("Event.ONCONTEXTMENU", null);
//			break;
//
//		default:
//			break; // Do nothing
//		}//end switch
//	}
}
