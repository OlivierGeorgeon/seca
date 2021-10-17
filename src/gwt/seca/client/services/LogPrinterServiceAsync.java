package gwt.seca.client.services;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>LogPrinterService</code>.
 */
public interface LogPrinterServiceAsync {
	void print(String str, AsyncCallback<String> callback) 
	throws IllegalArgumentException;
	void println(String str, AsyncCallback<String> callback) 
		throws IllegalArgumentException;
}
