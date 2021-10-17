package gwt.seca.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>XMLStreamTracerService</code>.
 */
public interface XMLStreamTracerServiceAsync {
	void postNewTrace(String url, String cookie, AsyncCallback<String> callback) 
		throws IllegalArgumentException;
	void postObsel(String url, String traceID, String cookie, String obselData, AsyncCallback<String> callback) 
		throws IllegalArgumentException;
	void postEndOfTrace(String url, String traceID, String cookie, AsyncCallback<String> callback) 
		throws IllegalArgumentException;
}
