package testing;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import client.Client;
import util.Server;

public class DynamicRuntimeTests {
	
	//NOT CASE SENSITIVE

	private static HashMap<String, Client> clients = new HashMap<String, Client>(1);
	private static HashMap<String, Server> servers = new HashMap<String, Server>(10);
	private static HashMap<String, HashSet<Server>> serverSet = new HashMap<String, HashSet<Server>>(2);
	
	private static void createClient(String[] input)	//"new" "client" *clientName* *pcId* *port* *serverSet*
	{
		try
		{
			clients.put(input[2], new Client((int)Integer.valueOf(input[3]), (int)Integer.valueOf(input[4]), serverSet.get(input[5])));
			System.out.println("Client created");
		}
		catch (Exception e)
		{
			System.out.println("bad input, failed to create Client");
			e.printStackTrace();
		}
		
	}
	
	private static void createServer(String[] input)	//"new" "server" *serverName* *IpAddress* *port*
	{
		try
		{
			servers.put(input[2], new Server(InetAddress.getByName(input[3]), (int)Integer.valueOf(input[4])));
			System.out.println("Server created");
		}
		catch (Exception e)
		{
			System.out.println("bad input, failed to create Server");
			e.printStackTrace();
		}
		
	}
	
	private static void createServerSet(String[] input)	//"new" "serverset" *serversetName*
	{
		try
		{
			serverSet.put(input[2], new HashSet<Server>(5));
			System.out.println("ServerSet created");
		}
		catch (Exception e)
		{
			System.out.println("bad input, failed to create ServerSet");
			e.printStackTrace();
		}
		
	}
	
	private static void addServerSet(String[] input)		//"add" "serverSet" *serverSetName* *server*
	{
		try
		{
			serverSet.get(input[2]).add(servers.get(input[3]));
			System.out.println("Server added to serverSet");
		}
		catch (Exception e)
		{
			System.out.println("bad input, failed to add server to serverSet");
			e.printStackTrace();
		}
	}
	
	private static void addServer(String[] input)			//"add" "server" *clientName* *serverName*
	{
		try
		{
			clients.get(input[2]).addServer(servers.get(input[3]));
			System.out.println("Server added to client");
		}
		catch (Exception e)
		{
			System.out.println("bad input, failed to add server to client");
			e.printStackTrace();
		}
	}
	
	private static void removeServer(String[] input)			//"add" "server" *clientName* *serverName*
	{
		try
		{
			clients.get(input[2]).removeServer(servers.get(input[3]));
			System.out.println("Server added to client");
		}
		catch (Exception e)
		{
			System.out.println("bad input, failed to remove server from client");
			e.printStackTrace();
		}
	}
	
	private static void read(String[] input)			//"read" *clientName* *key*
	{
		try
		{
			System.out.println(clients.get(input[1]).read(input[2]));
			
		}
		catch (Exception e)
		{
			System.out.println("bad input, failed to read");
			e.printStackTrace();
		}
	}
	
	private static void write(String[] input)			//"read" *clientName* *key* *value*
	{
		try
		{
			clients.get(input[1]).write(input[2], input[3]);
			System.out.println("Write successful");
		}
		catch (Exception e)
		{
			System.out.println("bad input, failed to write");
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws UnknownHostException 
	{
		Scanner input = new Scanner(System.in);
		String command;
		String[] commandParsed;
		
		while (true)
		{
			
			command = input.nextLine();
			command.toLowerCase();
			if (command.equals("end"))
			{
				input.close();
				return;
			}
			commandParsed = command.split(" ");
			if (commandParsed[0].equals("new"))
			{
				if (commandParsed[1].equals("client"))
					{createClient(commandParsed);}
				else if (commandParsed[1].equals("server"))
					{createServer(commandParsed);}
				else if (commandParsed[1].equals("serverset"))
					{createServerSet(commandParsed);}
				else
					{System.out.println("unknown new command");}
			}
			else if (commandParsed[0].equals("add"))
			{
				if (commandParsed[1].equals("serverset"))
					{addServerSet(commandParsed);}
				else if (commandParsed[1].equals("server"))
					{addServer(commandParsed);}
				else
					{System.out.println("unknown add command");}
			}
			else if (commandParsed[0].equals("remove"))
			{
				if (commandParsed[1].equals("server"))
					{removeServer(commandParsed);}
			}
			else if (commandParsed[0].equals("read"))
					{read(commandParsed);}
			else if (commandParsed[0].equals("write"))
				{write(commandParsed);}
			else
				{System.out.println("failed to parse input");}
		}
	}
		

}
