package client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Iterator;

import util.ByteArray;
import util.Message;
import util.Server;

public class Client {
	int pcid;	//save the pcid of the machine in the class
	HashSet<Server> servers = new HashSet<Server>(5);	//set of Servers. Can be modified with functions
	int port = 2000;	//port to use for everything
	int reqID = 0;
	
	public Client(int PCID, int PORT, HashSet<Server> SERVERS)
	{
		pcid = PCID;
		port = PORT;
		Iterator<Server> serverIterator = SERVERS.iterator();
		while (serverIterator.hasNext())
		{
			servers.add(serverIterator.next());
		}
		
	}
	
	public void addServer(Server SERVER)
	{
		servers.add(SERVER);
	}
	
	public void removeServer(Server SERVER)
	{
		Iterator<Server> removeIterator = servers.iterator();
		Server checkServer;
		while (removeIterator.hasNext())
		{
			checkServer = removeIterator.next();
			if (checkServer.equals(SERVER))
			{
				servers.remove(checkServer);
				return;
			}
		}
	}
	
	//the public read command that gets a value from the servers
	public String read(String key) throws IOException
	{
		reqID++;
		Message value = readMessage(key);
		writeMessage(key, value.getValue(), value.getSeqID() + 1);
		return value.getValue();
	}
	
	//the public write command that writes a value to the servers
	public void write(String key, String value) throws IOException
	{
		reqID++;
		Message seqID = readMessage(key);
		writeMessage(key, value, seqID.getSeqID() + 1);
	}
	
	//specifically reads from the servers. Used privately by both the read and write functions
	private Message readMessage(String key) throws IOException
	{
		//send the request
		DatagramPacket packet;
		DatagramSocket socket = new DatagramSocket(port);
		Message message = new Message(reqID + ":" + "read-request:" + pcid + ":" + key);
		byte[] messageBytes = message.formatMessage().getBytes();
		Iterator<Server> serverIterator = servers.iterator();
		HashSet<Server> resendSet = new HashSet<Server>(servers.size());
		Server destinationServer;
		while (serverIterator.hasNext())
		{
			destinationServer = serverIterator.next();
			resendSet.add(destinationServer);
			packet = new DatagramPacket(messageBytes, messageBytes.length, destinationServer.getAddress(), destinationServer.getPort());
			socket.send(packet);
		}
		
		//wait for and read responses
		socket.setSoTimeout(5000);	//TODO : get better timeout duration
		Message response;
		String value = "0";
		int maxSeq = -1;
		int maxPc = -1;
		boolean timeout = false;
		Server receivedServer;
		Server checkServer;
		Iterator<Server> removeIterator;

		int i = 0;
		while (i < (servers.size() / 2) + 1) //address.length / 2 is an int and should self-truncate
		{
			
			packet = new DatagramPacket(new byte[1024], 1024);
			timeout = false;
			System.out.printf("about to wait for responses to a read\n");
			try	{socket.receive(packet);}	//wait for packets until it gets one or times out
				catch (SocketTimeoutException e)
					{
						timeout = true;
						System.out.printf("timed out while waiting for responses to a read\n");
					}
			if (!timeout)	//found a packet
			{
				response = new Message(ByteArray.parseToString(packet.getData()));
				System.out.printf("got a response\n");
				//validate  that the packet is indeed a response to this read and not a prior read/write
				if (message.getReqID() == reqID)
				{
					System.out.printf("response was good\n");
					
					//TODO: MAKE THIS NOT HORRIBLE
					receivedServer = new Server(packet.getAddress(), packet.getPort());
					removeIterator = servers.iterator();
					
					while (removeIterator.hasNext())
					{
						checkServer = removeIterator.next();
						if (checkServer.equals(receivedServer))
						{
							resendSet.remove(checkServer);
							break;
						}
					}
					
					//track most recent data
					if (response.getSeqID() > maxSeq || (response.getSeqID() == maxSeq && response.getPcID() > maxPc))
					{
						value = ByteArray.parseToString(packet.getData());
						maxSeq = response.getSeqID();
						maxPc = response.getPcID();
					}	
					i++;
				}
				else
				{
					System.out.printf("response was bad");
				}
				
			}
			else	//timed out without finding a packet, so retransmit to remaining servers
			{
				Iterator<Server> resendIterator = resendSet.iterator();
				while (resendIterator.hasNext())
				{
					destinationServer = resendIterator.next();
					try {packet = new DatagramPacket(messageBytes, messageBytes.length, destinationServer.getAddress(), destinationServer.getPort());}
						catch (RuntimeException e)
						{
							socket.close();
							throw new RuntimeException("ERROR - resendSet smaller than expected");
						}
					socket.send(packet);
				}
			}
		}
		socket.close();
		Message returnMessage = new Message(value);
		return returnMessage;
	}
	
	//specifically writes to the servers. Used privately by both the read and write functions
	private void writeMessage(String key, String value, int seqId) throws IOException
	{
		//send the request
		DatagramPacket packet;
		DatagramSocket socket = new DatagramSocket(port);
		Message message = new Message(reqID + ":" + "write-request:" + pcid + ":" + seqId + ":" + key + ":" + value);
		byte[] messageBytes = message.formatMessage().getBytes();	
		Iterator<Server> serverIterator = servers.iterator();
		HashSet<Server> resendSet = new HashSet<Server>(servers.size());
		Server destinationServer;
		while (serverIterator.hasNext())
		{
			destinationServer = serverIterator.next();
			resendSet.add(destinationServer);
			packet = new DatagramPacket(messageBytes, messageBytes.length, destinationServer.getAddress(), destinationServer.getPort());
			socket.send(packet);
		}
		
		//wait for responses
		socket.setSoTimeout(5000);	//TODO : get better timeout duration
		Message response;
		boolean timeout = false;
		Server receivedServer;
		Server checkServer;
		Iterator<Server> removeIterator;
		
		int i = 0;
		while (i < (servers.size() / 2) + 1) //address.length / 2 is an int and should self-truncate
		{
			packet = new DatagramPacket(new byte[1024], 1024);
			timeout = false;
			System.out.printf("about to wait for responses to a write\n");
			try	{socket.receive(packet);}	//wait for packets until it gets one or times out
				catch (SocketTimeoutException e)
				{
					timeout = true;
				}
			if (!timeout)	//found a packet
			{
				response = new Message(ByteArray.parseToString(packet.getData()));
				if (response.getReqID() == reqID)
				{
					System.out.printf("got a response\n");
					
					//TODO: MAKE THIS NOT HORRIBLE
					receivedServer = new Server(packet.getAddress(), packet.getPort());
					removeIterator = servers.iterator();
					
					while (removeIterator.hasNext())
					{
						checkServer = removeIterator.next();
						if (checkServer.equals(receivedServer))
						{
							resendSet.remove(checkServer);
							break;
						}
					}
					

					i++;
				}
				
			}
			else	//timed out without finding a packet, so retransmit to remaining servers
			{
				Iterator<Server> resendIterator = resendSet.iterator();
				while (resendIterator.hasNext())
				{
					destinationServer = resendIterator.next();
					try {packet = new DatagramPacket(messageBytes, messageBytes.length, destinationServer.getAddress(), destinationServer.getPort());}
						catch (RuntimeException e)
						{
							socket.close();
							throw new RuntimeException("ERROR - resendSet smaller than expected");
						}
					socket.send(packet);
				}
			}
			
		}
		socket.close();
	}
	
}
