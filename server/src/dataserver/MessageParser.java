package dataserver;


import util.messages.*;


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
		System.out.println("message from\t" + message.sender().addr() + "\t:\t" + message);
		
		String flag = message.getFlag();
		// Pings another server; used for testing
		// TODO	deprecate this
		
		
		if (message instanceof WriteRequestMessage) {
			this.server.write(
					((WriteRequestMessage) message).getKey(), 
					((WriteRequestMessage) message).getVal(), 
					((WriteRequestMessage) message).getSeqId() + "", 
					message.sender());
		}
		else if (message instanceof ReadRequestMessage) {
			this.server.read(
					((ReadRequestMessage) message).getKey(), 
					message.sender());
		}
		
		// Test Cases
		else if (flag.equals("respond"))
			this.server.send(new Message(message.recipient(), message.sender(),"response"));
		
		// Replies back to server 
		// TODO deprecate this
		else if (flag.equals("echo"))
			this.server.send(new Message(
					message.recipient(), 
					message.sender(),  
					message.toString()));
		
		// TODO more if statements... or case? whatever
		
		
		
		return;
		
	}
}
