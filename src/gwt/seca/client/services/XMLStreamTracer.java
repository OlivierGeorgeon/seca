package gwt.seca.client.services;

import tracing.ITracer;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;


public class XMLStreamTracer implements ITracer<Element> {
	
	private String mURL;
	private String mTraceID;
	private String mCookie;
	
	private Document mDocument;
	private boolean mEventStarted;
	private Element mCurrentEvent;
	private Element mRoot;
	private int mLastCycle;
	private int mID = 0;
	
	
	/**
	 * Create a remote service proxy to talk to the server-side XMLStreamTracer service.
	 */
	private final XMLStreamTracerServiceAsync xmlStreamTracerService = GWT
			.create(XMLStreamTracerService.class);
	
	/**
	 * Initialize the tracer.
	 * @param fileName The name of the trace file
	 */
	public XMLStreamTracer(String url, String cookie) {
		mURL = url;
		mTraceID = "test";
		mCookie = cookie;
		
		xmlStreamTracerService.postNewTrace(mURL, mCookie, 
				new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				// Show the RPC error message to the user
				System.out.println("XMLStreamTracer: Failed to start a new trace.");
			}

			public void onSuccess(String result) {
				mTraceID = result;
				System.out.println("XMLStreamTracer: New trace started.");
			}
		});
		mDocument = XMLParser.createDocument();
		mEventStarted = false;
	}

	/**
	 * Write the XML document to the file trace.xml
	 * from http://java.developpez.com/faq/xml/?page=xslt#creerXmlDom
	 */
	@Override
	public boolean close() {
		// TODO Auto-generated method stub
		xmlStreamTracerService.postEndOfTrace(mURL, mTraceID, mCookie, 
				new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				// Show the RPC error message to the user
				System.out.println("XMLStreamTracer: Failed to end the trace.");
			}

			public void onSuccess(String result) {
				System.out.println("XMLStreamTracer: Trace ended.");
			}
		});
		return true;
	}
	
	/**
	 * Create an event that can be populated using its reference.
	 * @param type The event's type.
	 * @param t The event's time stamp.
	 * @return The pointer to the event.
	 */
	@Override
	public Element newEvent(String source, String type, int t) {
		// TODO Auto-generated method stub
		if(!mEventStarted) {
			mRoot = mDocument.createElement("slice");
			mRoot.setAttribute("date", Integer.toString(t));
			mDocument.appendChild(mRoot);
		} else if (t != mLastCycle) {
			this.finishEvent();
			mRoot = mDocument.createElement("slice");
			mRoot.setAttribute("date", Integer.toString(t));
			mDocument.appendChild(mRoot);
		}
		mLastCycle = t;
		mEventStarted = true;
		
		mID++;
		mCurrentEvent = mDocument.createElement("event");
		mCurrentEvent.setAttribute("id", Integer.toString(mID));
		mCurrentEvent.setAttribute("source", source);
		mCurrentEvent.setAttribute("date", Integer.toString(t));
		Element typeElement = mDocument.createElement("type");
//		typeElement.setTextContent(type);
		typeElement.appendChild(mDocument.createTextNode(type));
		mCurrentEvent.appendChild(typeElement);
		mRoot.appendChild(mCurrentEvent);

		return mCurrentEvent;
	}

	/**
	 * Create a new event that can be populated with elements later.
	 * @param t the time stamp
	 */
	@Override
	public void startNewEvent(int t) {
		// TODO Auto-generated method stub
		this.newEvent("Ernest", "action", t);
	}

	@Override
	public void finishEvent() {
		// TODO Auto-generated method stub
		if(!mEventStarted)
			return;
		
		mEventStarted = false;
		
		xmlStreamTracerService.postObsel(mURL, mTraceID, mCookie, mRoot.toString(),
				new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				// Show the RPC error message to the user
				System.out.println("XMLStreamTracer: Failed to post the event.");
			}

			public void onSuccess(String result) {
				System.out.println("XMLStreamTracer: Event posted.");
			}
		});
		
		mDocument.removeChild(mRoot);
	}

	/**
	 * Add a new element to the current event
	 * @param name The element's name
	 * @param textContent The element's textual content
	 * @return a pointer to the element that can be used to add sub elements.
	 */
	@Override
	public Element addEventElement(String name) {
		return this.addEventElementImpl(name, "", true);
	}
	@Override
	public Element addEventElement(String name, boolean display) {
		return this.addEventElementImpl(name, "", display);
	}
	@Override
	public void addEventElement(String name, String textContent) {
		this.addEventElementImpl(name, textContent, true);
		
	}
	private Element addEventElementImpl(String name, String textContent, boolean display) {
		if (mCurrentEvent != null) {
			Element element = mDocument.createElement(name);
//			element.setTextContent(textContent);
			element.appendChild(mDocument.createTextNode(textContent));
			if (!display) element.setAttribute("display","no");
			mCurrentEvent.appendChild(element);
			return element;
		}
		else 
			return null;
	}

	public Element addSubelement(Element element, String name) {
		return this.addSubelementImpl(element, name, "");
	}
	public void addSubelement(Element element, String name, String textContent) {
		this.addSubelementImpl(element, name, textContent);
	}
	private Element addSubelementImpl(Element element, String name, String textContent) {
		if (element != null) {
			Element subElement = mDocument.createElement(name);
//			subElement.setTextContent(textContent);
			subElement.appendChild(mDocument.createTextNode(textContent));
			element.appendChild(subElement);
			return subElement;
		}
		else 
			return null;
	}
	
}
