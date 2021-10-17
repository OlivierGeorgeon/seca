package gwt.seca.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;

import gwt.seca.client.services.XMLStreamTracerService;
import gwt.seca.shared.FieldVerifier;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class XMLStreamTracerServiceImpl extends RemoteServiceServlet implements
	XMLStreamTracerService {

	@Override
	public String postNewTrace(String url, String cookie) throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidURL(url)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"URL must not be null or empty");
		}
		// TODO Auto-generated method stub
		String traceId = "";
		try
		{
			URL base = new URL(url + "newStreamedTrace.php");
			HttpURLConnection baseCon = (HttpURLConnection) base.openConnection();
			baseCon.setRequestMethod("POST");
			
			String data = URLEncoder.encode("streamcookie", "UTF-8") + "=" + URLEncoder.encode(cookie, "UTF-8");

			baseCon.setDoOutput(true);
			baseCon.setDoInput(true);
			baseCon.setUseCaches (false);
			
			OutputStream os = baseCon.getOutputStream();
			os.write(data.getBytes());
			os.flush();
			os.close();
			
			int response = baseCon.getResponseCode();
			if(response >= 300 || response < 200)
			{
				System.out.println("XMLStreamTracerServlet: Post failed: \"\"\"");
				InputStream es = baseCon.getErrorStream();
				for(int ch = es.read(); ch != -1; ch = es.read())
				{
					System.out.print((char)ch);
				}
				System.out.println("\"\"\"");
			}else{
				InputStream is = baseCon.getInputStream();
				byte[] b = new byte[1000];
				int len = is.read(b);
				traceId = new String(Arrays.copyOf(b, len));
				System.out.println("XMLStreamTracerServlet: Trace ID received: "+traceId);
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println("XMLStreamTracerServlet: Invalid URL (" + url + ").");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("XMLStreamTracerServlet: Couldn't reach the server (" + url + ").");
			System.exit(0);
		};
		return traceId;
	}

	@Override
	public String postObsel(String url, String traceID, String cookie, String obselData)
			throws IllegalArgumentException {
		// Verify that the input url is valid. 
		if (!FieldVerifier.isValidURL(url)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"URL must not be null or empty");
		}
		// TODO Auto-generated method stub
		try
		{
			URL base = new URL(url + "streamTrace.php");
			HttpURLConnection baseCon = (HttpURLConnection) base.openConnection();
			baseCon.setRequestMethod("POST");
			
			String data = URLEncoder.encode("traceId", "UTF-8")+
			            "="+URLEncoder.encode(traceID, "UTF-8");
			data += "&"+URLEncoder.encode("streamcookie", "UTF-8")+
            			"=" + URLEncoder.encode(cookie, "UTF-8");
			data += "&"+URLEncoder.encode("data", "UTF-8")+
						"=" + URLEncoder.encode(obselData, "UTF-8");

			System.out.println("XMLStreamTracerServlet: Obsel data: "+obselData);
			System.out.println("XMLStreamTracerServlet: Sent data: "+data);
			
			baseCon.setDoOutput(true);
			baseCon.setDoInput(true);
			baseCon.setUseCaches (false);
			
			OutputStream os = baseCon.getOutputStream();
			os.write(data.getBytes());
			os.flush();
			os.close();
			
			int response = baseCon.getResponseCode();
			if(response >= 300 || response < 200)
			{
				System.out.println("XMLStreamTracerServlet: Post failed: \"\"\"");
				InputStream es = baseCon.getErrorStream();
				for(int ch = es.read(); ch != -1; ch = es.read())
				{
					System.out.print((char)ch);
				}
				System.out.println("\"\"\"");
			}else{
				System.out.println("XMLStreamTracerServlet: Event posted.");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println("XMLStreamTracerServlet: Invalid URL (" + url + ").");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("XMLStreamTracerServlet: Couldn't reach the server (" + url + ").");
			System.exit(0);
		};
		return null;
	}

	@Override
	public String postEndOfTrace(String url, String traceID, String cookie)
			throws IllegalArgumentException {
		// Verify that the input is valid. 
		if (!FieldVerifier.isValidURL(url)) {
			// If the input is not valid, throw an IllegalArgumentException back to
			// the client.
			throw new IllegalArgumentException(
					"URL must not be null or empty");
		}
		// TODO Auto-generated method stub
		try
		{
			URL base = new URL(url + "endOfStream.php");
			HttpURLConnection baseCon = (HttpURLConnection) base.openConnection();
			baseCon.setRequestMethod("POST");
			
			String data = URLEncoder.encode("traceId", "UTF-8")+
			            "="+URLEncoder.encode(traceID, "UTF-8");
			data += "&"+URLEncoder.encode("streamcookie", "UTF-8")+
			            "=" + URLEncoder.encode(cookie, "UTF-8");

			baseCon.setDoOutput(true);
			baseCon.setDoInput(true);
			baseCon.setUseCaches (false);
			
			OutputStream os = baseCon.getOutputStream();
			os.write(data.getBytes());
			os.flush();
			os.close();
			
			int response = baseCon.getResponseCode();
			if(response >= 300 || response < 200)
			{
				System.out.println("XMLStreamTracerServlet: Post failed: \"\"\"");
				InputStream es = baseCon.getErrorStream();
				for(int ch = es.read(); ch != -1; ch = es.read())
				{
					System.out.print((char)ch);
				}
				System.out.println("\"\"\"");
			}else{
				System.out.println("XMLStreamTracerServlet: Trace ended.");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			System.out.println("XMLStreamTracerServlet: Invalid URL (" + url + ").");
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("XMLStreamTracerServlet: Couldn't reach the server (" + url + ").");
			System.exit(0);
		};
		return null;
	}

}
