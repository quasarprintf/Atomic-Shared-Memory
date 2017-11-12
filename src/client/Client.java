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
	
	/**
	 * 
	 * @param pcid
	 * @param port
	 * @param addresses	The String representations of the IPv4 addresses this client is meant to query
	 */
	public Client(int pcid, int port, String[] addresses) {
		this.port = port;
		this.pcid = pcid;
		
		System.out.print("Client " + this.pcid + " created:"
				+ "\n\t" + "Port: " + this.port
				+ "\n\t" + "Addr:");
		
		
		this.addresses = new InetAddress[addresses.length];
		
		for (int i = 0; i < addresses.length; i++) {
			System.out.print(" " + addresses[i]);
			try {
				this.addresses[i] = InetAddress.getByName(addresses[i]);
			} catch (UnknownHostException e) {
				this.addresses[i] = null;
				System.out.print(" (ERROR: Unknown Host Exception)");
			}
			
		}
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
	/**
	 * Sends a message to all addresses given to this client when it was initialized
	 * @param message	The message to send; the local reqid appended to the front, delimited from the rest of
	 * 					the message by a ":" character
	 */
	public void sendMessage(String message) {
		// TODO sloppy fix; consider changing
		message = this.reqid + ":" + message;
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
	
	private int reqid = 0;
	public String getReqId() {
		return Integer.toHexString(reqid);
	}
	public void incReqId() {
		this.reqid++;
	}
}