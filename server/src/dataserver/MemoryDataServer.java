package dataserver;

import java.util.HashMap;

import util.Address;
import util.messages.Message;

/**
 * A subclass of DataServer.
 * 
 * This is a non-abstract object that specifies the abstract read() and write() operations of the superclass to read
 * and write to memory instead of the disk.
 * 
 * @author Christian
 *
 */
public class MemoryDataServer extends DataServer {

	
	
	/**
	 * The map that stores all data associated with keys put in this object
	 */
	private HashMap<String, String> DATA = new HashMap<String, String>();
	
	/**
	 * The map that stores all the timestamps associated with keys put in this object
	 */
	private HashMap<String, String> TIME = new HashMap<String, String>();
	
	
	/**
	 * The primary constructor for a MemoryDataServer object.
	 * @param serverid	The identification number that distinguishes this DataServer from other DataServers
	 * @param ADDRESSES	The other servers in the network; this object is stored as a volatile array and can be updated
	 * @param port	The port at the local address that this object should listen to for UDP messages
	 */
	public MemoryDataServer(int serverid, Address[] ADDRESSES, int port, String address) {
		super(serverid, ADDRESSES, port, address);
	}

	
	protected void write(String key, String value, String timestamp, Address returnAddress) {
		
		if (this.DATA.containsKey(key)) {
			int localStamp = Integer.parseInt(this.TIME.get(key), 16);
			int newStamp = Integer.parseInt(timestamp, 16);
			
			System.out.println("localStamp: " + localStamp);
			System.out.println("newStamp: " + newStamp);
			
			if (localStamp < newStamp) { // if the local stamp is older than the new stamp, we update
				//System.out.println("We're updating!");
				this.TIME.put(key, timestamp);
				this.DATA.put(key, value);
			}
		}
		else {
			this.DATA.put(key, value);
			this.TIME.put(key, timestamp);
		}
		
		// no matter what, send a receipt
		Message message = new Message(
				new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), 
				returnAddress, 
				DataServer.WRITE_RECEIPT_FLAG + ":" + this.id + ":" + key + ":" + value + ":" + timestamp);
		
		this.send(message);
		
		
		
	}

	@Override
	protected void read(String key, Address returnAddress) {
		String value = this.DATA.get(key);
		String timestamp = this.TIME.get(key);
		
		if (value == null && timestamp == null)
			this.send(new Message(new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), returnAddress, "read-return:" + this.id + ":" + "null" + ":" + "-1"));
		else if (value == null || timestamp == null)
			this.send(new Message(new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), returnAddress, "read-return:" + this.id + ":" + "data-sync-err" + ":" + "-1"));
		else {
			this.send(new Message(new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), returnAddress, "read-return:" + this.id + ":" + value + ":" + timestamp));
		}
	}
	
	

}
