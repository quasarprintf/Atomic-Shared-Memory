package util.messages;

import util.Address;

public class ReadReturnMessage extends Message {
	
	private final String seqid, val;
	
	public ReadReturnMessage(Address sender, Address recipient, String reqid, String flag, String pcid, String val, String seqid) {
		super(sender, recipient, reqid, flag, pcid, val, seqid);
		this.seqid = seqid;
		this.val = val;
	}
	
	public String getSeqID() {
		return this.seqid;
	}
	
	public String getVal() {
		return this.val;
	}
	
}
