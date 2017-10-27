package util;

import java.net.InetAddress;

public class Server {
	
	private InetAddress address;
	private int port;
	
	Server(InetAddress ADDRESS, int PORT)
	{
		address = ADDRESS;
		port = PORT;
	}
	
	
	//setters
	
	public void setAddress(InetAddress ADDRESS)
	{
		address = ADDRESS;
	}
	
	public void setPort(int PORT)
	{
		port = PORT;
	}
	
	
	//getters
	
	public InetAddress getAddress()
	{
		return address;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public boolean equals(Server other)
	{
		if (address.equals(other.getAddress()) && port == other.getPort())
			{return true;}
		else
			{return false;}
	}

}
