package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client extends Thread {
	private int port;
	private InetAddress[] addresses;
	private int pcid;
	public Client(int pcid, int port, InetAddress[] addresses) {
		this.port = port;
		this.addresses = addresses;
		this.pcid = pcid;
		
		System.out.print("Client " + this.pcid + " created:"
				+ "\n\t" + "Port: " + this.port
				+ "\n\t" + "Addr:");
		for (InetAddress addr : this.addresses)
			System.out.print(" " + addr);
		System.out.println();
	}
	
	public void run() {
		int count = 0;
		while (true) {
			this.sendMessage("Client " + this.pcid + " sent a message! Number: " + count++);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void sendMessage(String message) {
		DatagramSocket socket;
		for (InetAddress addr : this.addresses)
			try {
				byte[] data = message.getBytes();
				socket = new DatagramSocket();
				socket.send(new DatagramPacket(data, data.length, addr, 2000));
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnknownHostException uhe) {
				uhe.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}
}