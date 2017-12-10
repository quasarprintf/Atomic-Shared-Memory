package dataserver;

import java.net.SocketException;
import java.util.HashMap;

import util.Address;
import util.messages.ReadReturnMessage;
import util.messages.WriteReturnMessage;

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
	public MemoryDataServer(int serverid, int port, String address) throws SocketException {
		super(serverid, port, address);
	}

	
	protected void write(String key, String value, String timestamp, Address returnAddress, String reqid) {
		
		if (this.DATA.containsKey(key)) {
			int localStamp = Integer.parseInt(this.TIME.get(key));
			int newStamp = Integer.parseInt(timestamp);
			
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
		WriteReturnMessage message = new WriteReturnMessage(
				new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), 
				returnAddress, 
				reqid, 
				DataServer.WRITE_RECEIPT_FLAG,
				this.id + "",
				key);
		
		this.send(message);
		
		
		
	}

	@Override
	protected void read(String key, Address returnAddress, String reqid) {
		String value = this.DATA.get(key);
		String timestamp = this.TIME.get(key);
		
		if (value == null && timestamp == null)
			this.send(new ReadReturnMessage(new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), returnAddress, reqid, "read-return", this.id + "", "-1", "null"));
		else if (value == null || timestamp == null)
			this.send(new ReadReturnMessage(new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), returnAddress, reqid, "read-return", this.id + "",  "-1", "data-sync-error"));
		else {
			this.send(new ReadReturnMessage(new Address(this.soc.getLocalAddress(), this.soc.getLocalPort()), returnAddress, reqid, "read-return", this.id + "", timestamp, value));
		}
	}
	
	

}
