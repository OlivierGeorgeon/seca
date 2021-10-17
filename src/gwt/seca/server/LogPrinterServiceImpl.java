package gwt.seca.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import gwt.seca.client.services.LogPrinterService;
import gwt.seca.client.services.XMLStreamTracerService;
import gwt.seca.shared.FieldVerifier;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class LogPrinterServiceImpl extends RemoteServiceServlet implements LogPrinterService {

	@Override
	public String print(String str) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (str == null || str.isEmpty()) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"The String should not be null or empty");
		}
		System.out.print("LogPrinterService: "+str);
		return null;
	}

	@Override
	public String println(String str) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (str == null || str.isEmpty()) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"The String should not be null or empty");
		}
		System.out.println("LogPrinterService: "+str);
		return null;
	}

}
