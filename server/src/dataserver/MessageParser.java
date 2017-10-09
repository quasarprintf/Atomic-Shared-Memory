package dataserver;


import util.Message;


/**
 * This object is meant to interpret the messages received by 
 * @author Christian
 *
 */
public class MessageParser {
	
	DataServer server;
	
	public MessageParser(DataServer server) {
		this.server = server;
	}
	
	/**
	 * Parses the last command received from datagram packet and performs the operation designated in the request
	 * @param message
	 */
	protected void parse(Message message) {
		System.out.println("message: " + message);
		
		String flag = message.get(0);
		// Pings another server; used for testing
		// TODO	deprecate this
		if (flag.equals("respond"))
			this.server.send(new Message(message.recipient(), message.sender(), "response"));
		
		// Replies back to server 
		// TODO deprecate this
		else if (flag.equals("echo"))
			this.server.send(new Message(message.recipient(), message.sender(),  "echo", message.get(1)));
		
		else if (flag.equals("write-request")) {
			this.server.write(message.get(2), message.get(3), message.get(4), message.sender());
		}
		else if (flag.equals("read-request")) {
			this.server.read(message.get(2), message.sender());
		}
		
		// TODO more if statements... or case? whatever
		
		
		
		return;
		
	}
}
