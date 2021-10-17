package gwt.seca.client.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("logprinter")
public interface LogPrinterService extends RemoteService {
	String print(String str) throws IllegalArgumentException;
	String println(String str) throws IllegalArgumentException;
}
