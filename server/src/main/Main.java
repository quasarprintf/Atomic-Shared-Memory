package main;

import java.io.IOException;
import java.util.Scanner;

import client.Client;
import dataserver.MemoryDataServer;

/**
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
	public static void main(String[] args) throws IOException, InterruptedException {
		
		// address the developer last had on their machine so they could run the project from the IDE and not
		// the terminal
		String address = "10.66.117.76";

		MemoryDataServer server;
		//String[] addresses;
		//Client client0, client1;
		
		if (args.length == 0) {

			//addresses = new String[] {
				//	address
			//};
			
		}
		else {
			address = args[0];
		//	addresses = args[1].split(":");
			
		}

		server = new MemoryDataServer(0, null, 2000, address);

		server.start();
		
		//client0 = new Client(0, 2000, addresses);
		//client1 = new Client(1, 2000, addresses);
		/*
		Scanner kbd = new Scanner(System.in);
		String in;
		
		System.out.print(">");
		
		while ( (in = kbd.nextLine()) != "exit") {
			
			if (in.equals("!exit"))
				break; // not necessary, but here for clarity
			else if (in.equals("!help"))
				System.out.println(Main.helpString());
			else if (in.equals("!send"))
				client0.sendMessage(in.substring(1));

			System.out.print(">");
		}
		
		kbd.close();
		*/
		server.close();

	}
	
	
	public static String helpString() {
		
		String[] messages = new String[] {
				"bang	:	Only strings starting with '!' will be read; all others will be ignored",
				"help	:	Enter '!help' to display this help message",
				"exit	:	Enter '!exit' to exit the program and close the socket listening thread.",
				"send	:	Enter '!send' to prepare a message to send to the server using a mock client",
		};
		
		String out = "Commands for this terminal:";
		
		for (String message : messages)
			out = out + "\n\t" + message;
		
		return out;
		
	}
}
