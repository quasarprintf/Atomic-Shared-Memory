package client;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import util.Message;

public class ClientClass {
	int pcId;	//pcId of the machine
	HashSet<InetAddress> addresses;	//list of server addresses. Can be modified with functions (WIP)
	int readPort = 2000;	//port to use for reading
	int serverPort = 2000; //server port
	HashSet<Integer> writePorts = new HashSet<Integer>(20); //each write needs to listen to a different port
	int reqId = 0;		//the request number, used to discard old responses from the server and to kill old write threads
	Lock ClientInfo = new ReentrantLock(); //controls access to these class variables
	HashMap<String, Integer> currentlyWriting = new HashMap<String, Integer>(); //reqID's of the current writes for each key. Used to kill obsolete write threads 
	HashMap<String, Condition> writeWait = new HashMap<String, Condition>();	//used to synchronize write threads with the same key.
	
	ClientClass(int PCID, int SERVERPORT, int PORTSTART, int PORTEND, HashSet<InetAddress> ADDRESSES)
	//serverport is port the servers listen on. Could be combined with addresses as a hashmap if the servers listen on different ports
	//portstart and portend determine the ports used by the client. Inclusive on both bounds.
	//addresses is a hashset of the InetAddresses of the servers
	{
		pcId = PCID;
		serverPort = SERVERPORT;
		readPort = PORTSTART;
		//initialize set of server addresses
		Iterator<InetAddress> addressCopier = ADDRESSES.iterator();
		while (addressCopier.hasNext())
		{
			addresses.add(addressCopier.next());
		}
		//initialize set of ports to use for writing
		for (int i = PORTSTART+1; i < PORTEND+1; i++)
		{
			writePorts.add(new Integer(i));
		}
	}
	
	public String read(String key)
	{
		reqId++;
		
		
		ClientInfo.lock();
		
		while (currentlyWriting.containsKey(key))	//wait until nobody is writing this, to ensure it reads the most recent write
		{
			try {
				writeWait.get(key).await();
			} catch (InterruptedException e) {
				e.printStackTrace();
				Thread.currentThread().interrupt();
				throw new RuntimeException("ERROR - main thread was interrupted");
			}
		}
		
		int tempReadPort = readPort;
		int tempServerPort = serverPort;
		int tempReqId = reqId;
		int tempPcId = pcId;
		
		int port = writePorts.iterator().next();		//pick a random port from the writePorts set to use
		writePorts.remove(port);
		ClientInfo.unlock();
		Message value;
		
		try {
			value = readMessage(key, tempReadPort, tempServerPort, tempReqId, tempPcId);	//the actual read
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("ERROR - readMessage threw an I/O error during a read command");
		}
		
		ClientInfo.lock();
		reqId++;
		tempReqId = reqId;
		ClientInfo.unlock();
		WriteThread writer = new WriteThread(key, value.getValue(), tempReqId, value.getSeqID());	//update all the servers to have the result of this read, because that should be the most recent value.
		new Thread(writer).start();
		return value.getValue();
	}
	
	public void write(String key, String value) throws IOException
	{
		ClientInfo.lock();
		reqId++;
		int tempReqId = reqId;
		ClientInfo.unlock();
		WriteThread writer = new WriteThread(key, value, tempReqId, -1);	//this does all the actual work
		new Thread(writer).start();
	}

	private Message readMessage(String key, int readPort, int serverPort, int reqId, int pcId) throws IOException
	{
		//send the request
		DatagramPacket packet;
		DatagramSocket socket = new DatagramSocket(readPort);
		Message message = new Message(reqId + ":" + "read-request:" + pcId + ":" + key);
		byte[] messageBytes = message.formatMessage().getBytes();
		
		
		HashSet<InetAddress> resendSet = new HashSet<InetAddress>(addresses.size());
		Iterator<InetAddress> destinationIterator = addresses.iterator();
		while(destinationIterator.hasNext())
		{
			InetAddress destinationAddress = destinationIterator.next();
			resendSet.add(destinationAddress);
			packet = new DatagramPacket(messageBytes, messageBytes.length, destinationAddress, serverPort);
			socket.send(packet);
		}
		
		//wait for and read responses
		socket.setSoTimeout(1000);	//TODO : get better timeout duration
		Message response;
		String value = "0";
		int maxSeq = -1;
		int maxPc = -1;

		int i = 0;
		while (i < (addresses.size() / 2) + 1) //until over half have responded
		{
			
			packet = new DatagramPacket(new byte[1024], 1024);
			try	{socket.receive(packet);}	//wait for packets until it gets one or times out
				catch (SocketTimeoutException e){}
			if (packet.getData() != null)	//found a packet
			{
				response = new Message(String.valueOf((packet.getData())));
				//TODO : validate  that the packet is indeed a response to this read and not a prior read/write
				if (message.getReqID() == reqId)
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
					try {packet = new DatagramPacket(messageBytes, messageBytes.length, resendIterator.next(), serverPort);}
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
	
	private void writeMessage(String key, String value, int port, int serverPort, int reqId, int pcId, int seqId)
	{
		//send the request
		DatagramPacket packet;
		DatagramSocket socket = null;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e1) {
			System.out.printf("Socket failed to initialize while writing\n");
			e1.printStackTrace();
			return;
		}
		Message message = new Message(reqId + ":" + "write-request:" + pcId + ":" + seqId + ":" + key + ":" + value);
		byte[] messageBytes = message.formatMessage().getBytes();
		HashSet<InetAddress> resendSet = new HashSet<InetAddress>(addresses.size());
		Iterator<InetAddress> destinationIterator = addresses.iterator();
		while(destinationIterator.hasNext())
		{
			InetAddress destinationAddress = destinationIterator.next();
			resendSet.add(destinationAddress);
			packet = new DatagramPacket(messageBytes, messageBytes.length, destinationAddress, serverPort);
			try {
				socket.send(packet);
			} catch (IOException e) {
				System.out.printf("I/O error while sending write message\n");
				e.printStackTrace();
			}
		}
		
		//wait for responses
		try {
			socket.setSoTimeout(1000); //TODO : get better timeout duration
		} catch (SocketException e1) {
			System.out.printf("error while setting socket timeout duration while writing\n");
			e1.printStackTrace();
		}	
		Message response;
		
		int i = 0;
		while (i < (addresses.size() / 2) + 1) //until over half have responded
		{
			packet = new DatagramPacket(new byte[0], 0);
			try	{socket.receive(packet);}	//wait for packets until it gets one or times out
				catch (SocketTimeoutException e){}
				catch (IOException e) {
					System.out.printf("I/O error while receiving write responses\n");
					e.printStackTrace();
				}
			if (packet.getData() != null)	//found a packet
			{
				response = new Message(String.valueOf((packet.getData())));
				if (response.getReqID() == reqId)
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
					try {packet = new DatagramPacket(messageBytes, messageBytes.length, resendIterator.next(), serverPort);}
						catch (RuntimeException e)
						{
							socket.close();
							throw new RuntimeException("ERROR - resendSet smaller than expected");
						}
					try {
						socket.send(packet);
					} catch (IOException e) {
						System.out.printf("I/O error while resending write message\n");
						e.printStackTrace();
					}
				}
			}
			
		}
		socket.close();
		
		
		ClientInfo.lock();
		writePorts.add(port);			//release the port back into the writePorts set for some other thread to use
		ClientInfo.unlock();
	}
	
	class WriteThread implements Runnable
	{
		String key, value;
		int port, reqID, readPort, serverPort, pcId, seqId = -1;
		
		WriteThread(String KEY, String VALUE, int REQID, int SEQID)
		{
			key = KEY;
			value = VALUE;
			reqId = REQID;
			seqId = SEQID;
		}
		
		public void run()
		{
			//get the relevant class variables
			ClientInfo.lock();
			pcId = ClientClass.this.pcId;
			serverPort = ClientClass.this.serverPort;
			readPort = ClientClass.this.readPort;
			
			port = writePorts.iterator().next();		//pick a random port from the writePorts set to use
			writePorts.remove(port);
			
			if (currentlyWriting.containsKey(key))		//if this key is currently being written by another thread
			{
				if (currentlyWriting.get(key) < reqId)	//if the most recent thread to try to write this key is older than this thread
					{currentlyWriting.put(key, reqId);}	//update the map to note that this thread is the most recent one to write this key
				
				try {
					writeWait.get(key).await();			//wait for any thread that is currently writing this key to finish
				} catch (InterruptedException e) {
					e.printStackTrace();
					throw new RuntimeException("ERROR - readThread interrupted while waiting on a write, before getting something to return");
				}
				if (currentlyWriting.get(key) > reqId)	//if there is a newer thread trying to write this key, then this thread kills itself
				{
					ClientInfo.lock();
					writePorts.add(port);			//release the port back into the writePorts set for some other thread to use
					ClientInfo.unlock();
					return;							//return because there is a newer write thread for this key
				}
			}
			else									//this is the only thread trying to write this key, so update the maps to note this thread's age and to add a condition
			{
				currentlyWriting.put(key, reqId);
				writeWait.put(key, ClientInfo.newCondition());
			}
			ClientInfo.unlock();
			
			if (seqId == -1)	//if this is being called by the tail-end of a read command, then this should be the only write thread for this key, and seqId is known, so bypass the read
			{
				try {
					seqId = readMessage(key, readPort, serverPort, reqId, pcId).getSeqID();	//read specifically to get the seqId
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("ERROR - WriteThread received an IOException error while querying servers for seqId");
				}
			}
			
			writeMessage(key, value, port, serverPort, reqId, pcId, seqId + 1);	//write to all the servers
			
			ClientInfo.lock();
			if (currentlyWriting.get(key) == reqId)
			{
				currentlyWriting.remove(key);
				writeWait.get(key).signal();
				writeWait.remove(key);
			}
			else
			{
				writeWait.get(key).signalAll();
			}
			ClientInfo.unlock();
		}
	}
	
}
