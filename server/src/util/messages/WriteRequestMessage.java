package util.messages;

import util.Address;

public class WriteRequestMessage extends Message {
	
	public WriteRequestMessage(Address recipient, Address sender, String message) {
		super(recipient, sender, message);
	}
	
	public String getKey() {
		return super.get(4);
	}
	public String getVal() {
		return super.get(5);
	}
	public int getSeqId() {
		return Integer.parseInt(super.get(3), 16);
	}

}
