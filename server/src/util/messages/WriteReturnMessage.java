package util.messages;

import util.Address;

public class WriteReturnMessage extends Message {
	
	private final String key;
	
	public WriteReturnMessage(Address sender, Address recipient, String reqid, String flag, String pcid, String key) {
		super(sender, recipient, reqid, flag, pcid, key);
		this.key = key;
	}
	
	public String getKey() {
		return this.key;
	}
	
}
