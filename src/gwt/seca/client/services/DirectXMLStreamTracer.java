package gwt.seca.client.services;

import java.io.InputStream;

import tracing.ITracer;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

import gwt.seca.shared.FieldVerifier;

public class DirectXMLStreamTracer implements ITracer<Element> {
	
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
	 * Initialize the tracer.
	 * @param fileName The name of the trace file
	 */
	public DirectXMLStreamTracer(String url, String cookie) {
		mURL = url;
		mTraceID = "test";
		mCookie = cookie;
		
		this.postNewTrace();
		
		mDocument = XMLParser.createDocument();
		mEventStarted = false;
	}

	/**
	 * Write the XML document to the file trace.xml
	 * from http://java.developpez.com/faq/xml/?page=xslt#creerXmlDom
	 */
	@Override
	public boolean close() {
		
		this.postEndOfTrace();
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
		this.postObsel(mRoot.toString());
		
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
	
	////
	
	public void postNewTrace() throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidURL(mURL)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"URL must not be null or empty");
		}
		
		try {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, mURL + "newStreamedTrace.php");
//			String data = URLEncoder.encode("streamcookie", "UTF-8") + "=" + URLEncoder.encode(mCookie, "UTF-8");
			String data = "streamcookie" + "=" + mCookie;
			Request response = builder.sendRequest(data, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
					System.out.println("DirectXMLStreamTracer: Error while sending the request: \"\"\"");
					System.out.println(exception.getMessage());
					System.out.println("\"\"\"");
				}

				public void onResponseReceived(Request request, Response response) {
					if(response.getStatusCode() >= 300 || response.getStatusCode() < 200) {
						System.out.println("DirectXMLStreamTracer: Post failed: \"\"\"");
						System.out.println(response.getStatusText());
						System.out.println("\"\"\"");
					} else {
						mTraceID = response.getText();
						System.out.println("DirectXMLStreamTracer: Trace ID received: "+mTraceID);
					}
				}
			});
		} catch (RequestException e) {
			Window.alert("DirectXMLStreamTracer: Failed to send the request: " + e.getMessage());
		}
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Window.alert("DirectXMLStreamTracer: Failed to encode the message: " + e.getMessage());
//		}

	}

	public void postObsel(String obselData)
			throws IllegalArgumentException {
		// Verify that the input url is valid. 
		if (!FieldVerifier.isValidURL(mURL)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"URL must not be null or empty");
		}
		
		try {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, mURL + "streamTrace.php");
//			String data = URLEncoder.encode("traceId", "UTF-8")+
//            	"="+URLEncoder.encode(mTraceID, "UTF-8");
//			data += "&"+URLEncoder.encode("streamcookie", "UTF-8")+
//				"=" + URLEncoder.encode(mCookie, "UTF-8");
//			data += "&"+URLEncoder.encode("data", "UTF-8")+
//				"=" + URLEncoder.encode(obselData, "UTF-8");
			String data = "traceId" + "=" + mTraceID;
			data += "&" + "streamcookie" + "=" + mCookie;
			data += "&" + "data" + "=" + obselData;

			System.out.println("DirectXMLStreamTracer: Obsel data: "+obselData);
			System.out.println("DirectXMLStreamTracer: Sent data: "+data);

			Request response = builder.sendRequest(data, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
					System.out.println("DirectXMLStreamTracer: Error while sending the request: \"\"\"");
					System.out.println(exception.getMessage());
					System.out.println("\"\"\"");
				}

				public void onResponseReceived(Request request, Response response) {
					if(response.getStatusCode() >= 300 || response.getStatusCode() < 200) {
						System.out.println("DirectXMLStreamTracer: Post failed: \"\"\"");
						System.out.println(response.getText());
						System.out.println("\"\"\"");
					} else {
						System.out.println("DirectXMLStreamTracer: Event posted.");
					}
				}
			});
		} catch (RequestException e) {
			Window.alert("DirectXMLStreamTracer: Failed to send the request: " + e.getMessage());
		}
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Window.alert("DirectXMLStreamTracer: Failed to encode the message: " + e.getMessage());
//		}

	}

	public void postEndOfTrace()
			throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidURL(mURL)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"URL must not be null or empty");
		}
		
		try {
			RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, mURL + "endOfStream.php");
//			String data = URLEncoder.encode("traceId", "UTF-8")+
//            	"="+URLEncoder.encode(mTraceID, "UTF-8");
//			data += "&"+URLEncoder.encode("streamcookie", "UTF-8")+
//            	"=" + URLEncoder.encode(mCookie, "UTF-8");
			String data = "traceId" + "=" + mTraceID;
			data += "&" + "streamcookie" + "=" + mCookie;

			Request response = builder.sendRequest(data, new RequestCallback() {

				public void onError(Request request, Throwable exception) {
					System.out.println("DirectXMLStreamTracer: Error while sending the request: \"\"\"");
					System.out.println(exception.getMessage());
					System.out.println("\"\"\"");
				}

				public void onResponseReceived(Request request, Response response) {
					if(response.getStatusCode() >= 300 || response.getStatusCode() < 200) {
						System.out.println("DirectXMLStreamTracer: Post failed: \"\"\"");
						System.out.println(response.getText());
						System.out.println("\"\"\"");
					} else {
						System.out.println("DirectXMLStreamTracer: Trace ended.");
					}
				}
			});
		} catch (RequestException e) {
			Window.alert("DirectXMLStreamTracer: Failed to send the request: " + e.getMessage());
		}
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Window.alert("DirectXMLStreamTracer: Failed to encode the message: " + e.getMessage());
//		}
		
	}

}
