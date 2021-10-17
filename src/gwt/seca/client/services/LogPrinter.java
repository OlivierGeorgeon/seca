package gwt.seca.client.services;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LogPrinter {
	
	/**
	 * Create a remote service proxy to talk to the server-side LogPrinter service.
	 */
	private final LogPrinterServiceAsync logPrinterService = GWT
			.create(LogPrinterService.class);
	
	/**
	 * Initialize the log printer.
	 * @param fileName The name of the trace file
	 */
	public LogPrinter() {

	}
	
	/**
	 * Print a String.
	 */
	public boolean print(String str) {
		// TODO Auto-generated method stub
		logPrinterService.print(str,
				new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				// Show the RPC error message to the user
				System.out.println("LogPrinter: Failed to print the String.");
			}

			public void onSuccess(String result) {
				System.out.println("LogPrinter: String printed.");
			}
		});
		return true;
	}

	/**
	 * Print a String and then terminate the line.
	 */
	public boolean println(String str) {
		// TODO Auto-generated method stub
		logPrinterService.println(str,
				new AsyncCallback<String>() {
			public void onFailure(Throwable caught) {
				// Show the RPC error message to the user
				System.out.println("LogPrinter: Failed to print the String.");
			}

			public void onSuccess(String result) {
				System.out.println("LogPrinter: String printed.");
			}
		});
		return true;
	}

}
