package gwt.seca.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("xmlstreamtracer")
public interface XMLStreamTracerService extends RemoteService {

	String postNewTrace(String url, String cookie) throws IllegalArgumentException;
	String postObsel(String url, String traceID, String cookie, String obselData) throws IllegalArgumentException;
	String postEndOfTrace(String url, String traceID, String cookie) throws IllegalArgumentException;
}
