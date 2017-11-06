package util.messages;

import util.Address;

/**
 * This object should act like an envelope: it has a recipient address, a sender's address, and a message wrapped inside.
 * @author Christian
 *
 */
public class Message {
	
	public static final String DELIMITER = ":";
	
	String[] parts;
	String message;
	Address sender, recipient;
	public Message(Address sender, Address recipient, String ... messageParts) {
		
		
		this.sender = sender;
		this.recipient = recipient;
		this.parts = messageParts;
		this.message = "";
		for (String part : parts)
			this.message = this.message + Message.DELIMITER + part;
		
		this.message = this.message.substring(Message.DELIMITER.length());
		
			
		
	}
	public Message(Address sender, Address recipient, String messageParts) {
		this.sender = sender;
		this.recipient = recipient;
		this.parts = messageParts.split(Message.DELIMITER);
		this.message = messageParts;
	}
	
	
	@Override
	public String toString() {
		return this.message;
	}
	
	protected String get(int index) {
		if (index >= this.parts.length || index < 0)
			return null;
		else
			return this.parts[index];
	}
	
	public String getFlag() {
		return this.get(1);
	}
	public int getPCID() {
		return Integer.parseInt(this.get(2), 16);
	}
	public int getReqID() {
		return Integer.parseInt(this.get(0), 16);
	}
	
	
	
	public Address sender() {
		return this.sender;
	}
	public Address recipient() {
		return this.recipient;
	}
	
	public static Message construct(Address recipient, Address sender, String message) {
		Message out = new Message(recipient, sender, message);
		String flag = out.getFlag();
		if (flag == null) 
			return out;
		else if (flag.equals("read-request"))
			return new ReadRequestMessage(recipient, sender, message);
		else if (flag.equals("write-request"))
			return new WriteRequestMessage(recipient, sender, message);
		else
			return out;
	}
}