package repicea.net.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;

public class FakeClient extends BasicClient {

	protected FakeClient(SocketAddress socketAddress) throws UnknownHostException, IOException, ClassNotFoundException {
		super(socketAddress);
	}

	
	public static void main(String[] args) {
		InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 18000);
		try {
			FakeClient client = new FakeClient(socketAddress);
			Object result = client.processRequest("46.5;-70;323");
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
}
;