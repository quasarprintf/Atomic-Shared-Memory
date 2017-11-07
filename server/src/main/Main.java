package main;

import java.net.SocketException;

import dataserver.MemoryDataServer;

/**
 * 
 * Version 1.0.0 - One-To-Many capable
 * Implemented a -h for command line running
 * Implemented sleep functions for testing
 * 		* wait puts the server to sleep until it is woken up
 * 		* wake wakes the server up from being asleep
 * 
 * Version 0.1.1
 * Ability to give the server an IP address from command line arguments
 * 
 * 
 * Version 0.1.0
 * Storage of data through memory, not disk
 * Reception of messages through listening to a port and receiving UDP packets
 * 
 * 
 * 
 * @author Christian
 *
 */
public class Main {
	public static void main(String[] args) {
		
		// address the developer last had on their machine so they could run the project from the IDE and not
		// the terminal
		
		String address;
		int port;
		
		
		if (args.length == 0) {
			 address = "137.99.128.56";
			 port = 2000;
		}
		
		else if (args[0].equals("-h") || args[0].equals("--h") || args[0].equals("help")) {
			System.out.println(helpString());
			System.exit(0);
			return;
		}
		else {
			address = args[0];
			port = Integer.parseInt(args[1]);
		}


		
		process(address, port);
		
		
	
		


	}
	
	public static void process(String address, int port) {
		
		MemoryDataServer server;
		try {
			server = new MemoryDataServer(0, port, address);
			Thread serverThread = server.start();
			serverThread.join();
			server.close();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SocketException e) {
			process(address, port + 1);
		}
	}
	
	
	public static String helpString() {
		String out =
				"Run this in the form java Main <address> <port>"
						+ "\n" + "-h:			display this help message";
		
		
		return out;	
	}
}
