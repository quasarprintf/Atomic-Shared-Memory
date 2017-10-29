package dataserver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import util.Address;
import util.messages.*;

/**
 * This is the abstract object that outlines what a DataServer might look like. The abstract portion of this object is
 * the read() and write(), which are subject to change due to the nature of storing our data; it is possible to store
 * data to and read data from the disk or memory, so the abstraction has been made to accommodate subclasses that specify
 * which method is preferred.
 * 
 * 
 * 
 * @author Christian
 *
 */
public abstract class DataServer {

	protected final static String WRITE_RECEIPT_FLAG = "write-receipt";
	
	/**
	 * This is the list of other data servers in the network
	 */
	private volatile Address[] ADDRESSES;
	public final int port, id;
	
	protected int seqcount = 0;
	public DatagramSocket soc;

	private final static int REFRESH = 10000000;

	/**
	 * The primary constructor for a DataServer object.
	 * @param serverid	The identification number that distinguishes this DataServer from other DataServers
	 * @param ADDRESSES	The other servers in the network; this object is stored as a volatile array and can be updated
	 * @param port	The port at the local address that this object should listen to for UDP messages
	 */
	public DataServer(int serverid, Address[] ADDRESSES, int port, String address) {

		this.id = serverid;
		this.port = port;
		
		
		try {
			this.ADDRESSES = ADDRESSES;
			this.soc = new DatagramSocket(port, InetAddress.getByName(address));
			System.out.println("Data Server " + this.id + " created: "
					+ "\n\t" + "Port: " + this.soc.getLocalPort()
					+ "\n\t" + "Addr: " + this.soc.getLocalAddress());

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Delegates port listening to a thread so that methods in this object can be called while it is listening.
	 * Note that the object "thread" is a MessageListenerThread, which is a superclass abstraction of the class
	 * SocketListenerThread. For now, the DataServer will receive messages through UDP messages on a port at the
	 * local address, but it has been expressed to the group that this might not be the case for future versions
	 * of the project.
	 * 
	 * The reasoning behind making this a thread as opposed to a while loop is because this object might listen to
	 * several different objects at the same time that should be checked simultaneously instead of in sequence
	 * 
	 * TODO change this if the method of receiving messages changes
	 */
	public void start() {
		
		
		
		MessageListenerThread thread = new SocketListenerThread(this, this.soc, DataServer.REFRESH);
		thread.start();
		
	}

	
	protected abstract void read(String key, Address returnAddress);
	
	
	/**
	 * 
	 * @param key	The key of the key value pair
	 * @param value	The value of the key value pair
	 * @param timestamp	The timestamp showing the freshness of this value
	 * @param returnAddress	The IP/port combination this message came from; used for sending receipts
	 */
	protected abstract void write(String key, String value, String timestamp, Address returnAddress);
	
	
	/**
	 * Sends a message. The recipient of this message is stored in the Message object
	 * @param message The message to be sent
	 */
	protected void send(Message message) {
		System.out.println("Sending to\t" + message.recipient().addr() + "\t:\t" + message.toString());
		try {
			Address recip = message.recipient();
			this.soc.send(new DatagramPacket(message.toString().getBytes(), message.toString().getBytes().length, recip.addr(), recip.port()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			
			e.printStackTrace();
		}
	}
	
	public void close() {
		this.soc.close();
	}




	
	
	

}
