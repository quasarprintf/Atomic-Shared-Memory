package util;


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
	
	public String get(int index) {
		if (index > this.parts.length || index < 0)
			return null;
		else
			return this.parts[index];
	}
	
	public Address sender() {
		return this.sender;
	}
	public Address recipient() {
		return this.recipient;
	}
}