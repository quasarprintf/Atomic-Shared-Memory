package client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Iterator;
import util.Message;

public class ClientClass {
	int pcid;	//save the pcid of the machine in the class
	InetAddress[] addresses;	//list of server addresses. Can be modified with functions (WIP)
	int port = 2000;	//port to use for everything
	int reqID = 0;
	
	ClientClass(int PCID, int PORT, InetAddress[] ADDRESSES)
	{
		pcid = PCID;
		port = PORT;
		addresses = ADDRESSES.clone();
	}
	
	public String read(String key) throws IOException
	{
		reqID++;
		Message value = readMessage(key);
		writeMessage(key, value.getValue(), value.getSeqID() + 1);
		return value.getValue();
	}
	
	public void write(String key, String value) throws IOException
	{
		reqID++;
		Message seqID = readMessage(key);
		writeMessage(key, value, seqID.getSeqID() + 1);
	}
	
	private Message readMessage(String key) throws IOException
	{
		//send the request
		DatagramPacket packet;
		DatagramSocket socket = new DatagramSocket(port);
		Message message = new Message(reqID + ":" + "read-request:" + pcid + ":" + key);
		byte[] messageBytes = message.formatMessage().getBytes();
		packet = new DatagramPacket(messageBytes, messageBytes.length, addresses[0], port);
		HashSet<InetAddress> resendSet = new HashSet<InetAddress>(addresses.length);
		for (int i = 0; i < addresses.length; i++)
		{
			resendSet.add(addresses[i]);
			packet = new DatagramPacket(messageBytes, messageBytes.length, addresses[i], port);
			socket.send(packet);
		}
		
		//wait for and read responses
		socket.setSoTimeout(1000);	//TODO : get better timeout duration
		Message response;
		String value = "0";
		int maxSeq = -1;
		int maxPc = -1;

		int i = 0;
		while (i < (addresses.length / 2) + 1) //address.length / 2 is an int and should self-truncate
		{
			
			packet = new DatagramPacket(new byte[1024], 1024);
			try	{socket.receive(packet);}	//wait for packets until it gets one or times out
				catch (SocketTimeoutException e){}
			if (packet.getData() != null)	//found a packet
			{
				response = new Message(String.valueOf((packet.getData())));
				//TODO : validate  that the packet is indeed a response to this read and not a prior read/write
				if (message.getReqID() == reqID)
				{
					resendSet.remove(packet.getAddress());
					//track most recent data
					if (response.getSeqID() > maxSeq || (response.getSeqID() == maxSeq && response.getPcID() > maxPc))
					{
						value = new String(packet.getData());
						maxSeq = response.getSeqID();
						maxPc = response.getPcID();
					}	
					i++;
				}
				
			}
			else	//timed out without finding a packet, so retransmit to remaining servers
			{
				Iterator<InetAddress> resendIterator = resendSet.iterator();
				for (int j = 0; j < resendSet.size(); j++)
				{
					try {packet = new DatagramPacket(messageBytes, messageBytes.length, resendIterator.next(), port);}
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
	
	public void writeMessage(String key, String value, int seqId) throws IOException
	{
		//send the request
		DatagramPacket packet;
		DatagramSocket socket = new DatagramSocket(port);
		Message message = new Message(reqID + ":" + "write-request:" + pcid + ":" + seqId + ":" + key + ":" + value);
		byte[] messageBytes = message.formatMessage().getBytes();
		HashSet<InetAddress> resendSet = new HashSet<InetAddress>(addresses.length);
		for (int i = 0; i < addresses.length; i++)
		{
			resendSet.add(addresses[i]);
			packet = new DatagramPacket(messageBytes, messageBytes.length, addresses[i], port);
			socket.send(packet);
		}
		
		//wait for responses
		socket.setSoTimeout(1000);	//TODO : get better timeout duration
		Message response;
		
		int i = 0;
		while (i < (addresses.length / 2) + 1) //address.length / 2 is an int and should self-truncate
		{
			packet = new DatagramPacket(new byte[0], 0);
			try	{socket.receive(packet);}	//wait for packets until it gets one or times out
				catch (SocketTimeoutException e){}
			if (packet.getData() != null)	//found a packet
			{
				response = new Message(String.valueOf((packet.getData())));
				if (response.getReqID() == reqID)
				{
					resendSet.remove(packet.getAddress());
					i++;
				}
				
			}
			else	//timed out without finding a packet, so retransmit to remaining servers
			{
				Iterator<InetAddress> resendIterator = resendSet.iterator();
				for (int j = 0; j < resendSet.size(); j++)
				{
					try {packet = new DatagramPacket(messageBytes, messageBytes.length, resendIterator.next(), port);}
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
