package testing;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import util.Server;


/*
   To create a server:					"newserver" *serverName* *IpAddress* *port*
   To create a serverSet:				"newserverset" *serverSetName*
   To add a server to a serverSet:		"addserverset" *serverSetName* *server*
   To remove a server from a serverSet:	"removeserverset" *serversSetName* *serverName*
   To set the port to use:				"outport" *port#*
   
   To set a location for a machine:		"setloc" *machineName* *xfloat* *yfloat*
   To set drop rate for a server:		"drop" *drop%* *serverName*
   To set drop rate for server set:		"dropset" *drop%* *serverName*
   To kill a server:					"kill" *serverName*
   To kill all servers in a set:		"killset" *serverSetName*
   To revive a server:					"revive" *serverName*
   To revive all servers in a set:		"reviveset" *serverSetName*
   
   To exit:								"end"
 */

// Make sure anything that the commands can throw is listed here
interface Command {
	void run(String[] args) throws NumberFormatException, UnknownHostException, IOException;
}

public class DynamicRuntimeTests {
	// An elegant mapping of commands to the method to run them
	private static final Map<String, Command> commands = new HashMap<>();
	static {
		commands.put("newserver",		(String[] args)		-> 	createServer(args));
		commands.put("addserverset",	(String[] args)		-> 	addServerSet(args));
		commands.put("removeserverset",	(String[] args)		-> 	removeServerSet(args));
		commands.put("newserverset",	(String[] args)		-> 	createServerSet(args));
		commands.put("outport",			(String[] args)		-> 	outport(args));
		commands.put("pcid",			(String[] args)		-> 	pcid(args));
		commands.put("setloc",			(String[] args)		-> 	setloc(args));
		commands.put("kill",			(String[] args)		-> 	kill(args));
		commands.put("killset",			(String[] args)		-> 	killSet(args));
		commands.put("revive",			(String[] args)		-> 	revive(args));
		commands.put("reviveset",		(String[] args)		-> 	reviveSet(args));
		commands.put("drop",			(String[] args)		-> 	drop(args));
		commands.put("dropset",			(String[] args)		-> 	dropSet(args));
	}
	//NOT CASE SENSITIVE

	private static HashMap<String, Server> servers = new HashMap<String, Server>(10);
	private static HashMap<String, HashSet<Server>> serverSet = new HashMap<String, HashSet<Server>>(2);
	private static int outport = 1998;
	private static DatagramSocket socket;
	private static int reqID = 0;
	private static int pcID = 99;
	
	private static void pcid(String[] input)
	{
		pcID = (int)Integer.valueOf(input[1]);
	}
	
	private static void outport(String[] input) throws SocketException
	{
		socket.close();
		outport = (int)Integer.valueOf(input[1]);
		socket = new DatagramSocket(outport);
	}
	
	private static void setloc(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":set-location:" + pcID + input[2] + input[3]).getBytes();
		DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, servers.get(input[1]).getAddress(), servers.get(input[1]).getPort());
		socket.send(packet);
	}
	
	private static void kill(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":wait").getBytes();
		DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, servers.get(input[1]).getAddress(), servers.get(input[1]).getPort());
		socket.send(packet);
	}
	
	private static void killSet(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":wait").getBytes();
		for (Server server:serverSet.get(input[1]))
		{
			DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, server.getAddress(), server.getPort());
			socket.send(packet);
		}
	}
	
	private static void revive(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":wake").getBytes();
		DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, servers.get(input[1]).getAddress(), servers.get(input[1]).getPort());
		socket.send(packet);
	}
	
	private static void reviveSet(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":wake").getBytes();
		for (Server server:serverSet.get(input[1]))
		{
			DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, server.getAddress(), server.getPort());
			socket.send(packet);
		}
	}
	
	private static void drop(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":drop:" + input[1]).getBytes();
		DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, servers.get(input[2]).getAddress(), servers.get(input[2]).getPort());
		socket.send(packet);
	}
	
	private static void dropSet(String[] input) throws IOException
	{
		byte[] messageBytes = (reqID++ + ":drop:" + input[1]).getBytes();
		for (Server server:serverSet.get(input[2]))
		{
			DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, server.getAddress(), server.getPort());
			socket.send(packet);
		}
	}
	
	
	private static void createServer(String[] input) throws NumberFormatException, UnknownHostException {
		servers.put(input[1], new Server(InetAddress.getByName(input[2]), (int)Integer.valueOf(input[3])));
		System.out.println("[i]\tServer Created: \t" + input[1]);
	}
	
	private static void addServerSet(String[] input) {
		serverSet.get(input[1]).add(servers.get(input[2]));
		System.out.println("[i]\tServer Added To Set\t" + input[2] + "->" + input[1]);
	}
	
	private static void removeServerSet(String[] input) {
		serverSet.get(input[1]).remove(servers.get(input[2]));
		System.out.println("[i]\tServer Removed From Client:\t" + input[2] + "-<" + input[1]);
	}
	
	private static void createServerSet(String[] input)	{
		serverSet.put(input[1], new HashSet<Server>(5));
		System.out.println("[i]\tServerSet Created: \t" + input[1]);
	}
	
	
	
	public static void parseCommand(String input) throws NumberFormatException, UnknownHostException, IOException {
		String[] commandParsed = input.split(" ");
		commands.get(commandParsed[0]).run(commandParsed);
	}

	public static void runDynamicTests() throws UnknownHostException, SocketException 
	{
		socket = new DatagramSocket(outport);
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
			try {
				commands.get(commandParsed[0]).run(commandParsed);
			} catch (NumberFormatException e) {
				System.out.println("[e]\tFailed to convert port value to integer");
			} catch (UnknownHostException e) {
				System.out.println("[e]\tCould not find host");
			} catch (IOException e) {
				System.out.println("[e]\tAn IO exception has occurred");
			} catch (Exception e) {
				if (!commands.containsKey(commandParsed[0]))
				{
					System.out.printf("[e]\tCommand not found: %s\n", commandParsed[0]);
				}
				else System.out.println("[e]\tBad Input in " + commandParsed[0]);
			}
		}
			
	}
}