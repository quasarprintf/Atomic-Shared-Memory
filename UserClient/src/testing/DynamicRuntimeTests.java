package testing;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

import client.Client;
import util.Server;


/*
   To create a client: 				"new" "client" *clientName* *pcId* *port* *serverSet*
   To create a server: 				"new" "server" *serverName* *IpAddress* *port*
   To create a serverSet: 			"new" "serverset" *serversetName*
   To add a server to a serverSet: 	"add" "serverSet" *serverSetName* *server*
   To add a server to a client:		"add" "server" *clientName* *serverName*
   To remove a server from a client:"remove" "server" *clientName* *serverName*
   To read:							"read" *clientName* *key*
   To write:						"write" *clientName* *key* *value*
   To exit:							"end"
 */

// Make sure anything that the commands can throw is listed here
interface Command {
	void run(String[] args) throws NumberFormatException, UnknownHostException, IOException;
}

public class DynamicRuntimeTests {
	// An elegant mapping of commands to the method to run them
	private static final Map<String, Command> commands = new HashMap<>();
	static {
		commands.put("read", 		 (String[] args) 	-> 	read(args));
		commands.put("write", 		 (String[] args) 	-> 	write(args));
		commands.put("addserver", 	 (String[] args) 	-> 	addServer(args));
		commands.put("newserver",    (String[] args) 	-> 	createServer(args));
		commands.put("newclient", 	 (String[] args) 	-> 	createClient(args));
		commands.put("addserverset", (String[] args) 	-> 	addServerSet(args));
		commands.put("removeserver", (String[] args) 	-> 	removeServer(args));
		commands.put("newserverset", (String[] args) 	-> 	createServerSet(args));
	}
	//NOT CASE SENSITIVE

	private static HashMap<String, Client> clients = new HashMap<String, Client>(1);
	private static HashMap<String, Server> servers = new HashMap<String, Server>(10);
	private static HashMap<String, HashSet<Server>> serverSet = new HashMap<String, HashSet<Server>>(2);
	
	private static void read(String[] input) throws IOException {
		System.out.println("[r]\t" + input[2] + "->" + clients.get(input[1]).read(input[2]));
	}
	
	private static void write(String[] input) throws IOException {
		clients.get(input[1]).write(input[2], input[3]);
		System.out.println("[w]\t" + input[2] + "->" + input[3]);
	}
	
	private static void addServer(String[] input) {
		clients.get(input[1]).addServer(servers.get(input[2]));
		System.out.println("[i]\tServer Added To Client:\t" + input[2] + "->" + input[1]);
	}
	
	private static void createServer(String[] input) throws NumberFormatException, UnknownHostException {
		servers.put(input[1], new Server(InetAddress.getByName(input[2]), (int)Integer.valueOf(input[3])));
		System.out.println("[i]\tServer Created: \t" + input[1]);
	}
	
	private static void createClient(String[] input) {
		clients.put(input[1], new Client((int)Integer.valueOf(input[2]), (int)Integer.valueOf(input[3]), serverSet.get(input[4])));
		System.out.println("[i]\tClient Created: \t" + input[1]);
	}
	
	private static void addServerSet(String[] input) {
		serverSet.get(input[1]).add(servers.get(input[2]));
		System.out.println("[i]\tServer Added To Set\t" + input[2] + "->" + input[1]);
	}
	
	private static void removeServer(String[] input) {
		clients.get(input[1]).removeServer(servers.get(input[2]));
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

	public static void runDynamicTests() throws UnknownHostException 
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
			try {
				commands.get(commandParsed[0]).run(commandParsed);
			} catch (NumberFormatException e) {
				System.out.println("[e]\tFailed to convert port value to integer");
			} catch (UnknownHostException e) {
				System.out.println("[e]\tCould not find host");
			} catch (IOException e) {
				System.out.println("[e]\tAn IO exception has occured");
			} catch (Exception e) {
				if (!commands.containsKey(commandParsed[0])) System.out.println("[e]\tCommand not found");
				else System.out.println("[e]\tBad Input in " + commandParsed[0]);
			}
		}
			
	}
}