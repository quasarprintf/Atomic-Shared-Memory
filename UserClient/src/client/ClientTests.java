package client;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashSet;

public class ClientTests {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		HashSet<InetAddress> servers = new HashSet<InetAddress>(1);
		servers.add(InetAddress.getByName("67.221.92.26"));
		ClientClass messenger = new ClientClass(1, 2000, 2000, 20, servers);
		System.out.printf("starting test 1: write x = 1\n");
		messenger.write("x", "1");
		System.out.printf("test 1: done.\nStarting test 2: read x, should equal 1\n");
		String x = messenger.read("x");
		System.out.printf("test 2 done.\nValue of x is %s, should be 1.\n Starting test 3: write x = 2 and read again\n", x);
		messenger.write("x", "2");
		x = messenger.read("x");
		System.out.printf("test 3 done.\nValue of x is %s, should be 2.", x);
	}

}
