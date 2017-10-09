package main;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;

import client.Client;
import dataserver.DataServer;
import dataserver.MemoryDataServer;

public class Main {
	public static void main(String[] args) throws IOException, InterruptedException {
		
		System.out.println("This machine's address is: " + Inet4Address.getLocalHost().getHostAddress());
		
		Client client0 = new Client(0, 1000, new InetAddress[] {InetAddress.getLocalHost()});
		Client client1 = new Client(1, 1000, new InetAddress[] {InetAddress.getLocalHost()});
		MemoryDataServer server = new MemoryDataServer(0, null, 2000);
		
		
		client0.sendMessage("write-request:" + client0.getId() + ":key:value0:1");
		client1.sendMessage("read-request:" + client1.getId() + ":key");
		server.start();
		
		//client0.start();
		//client1.start();
		
	//	System.exit(0);
	}
}
