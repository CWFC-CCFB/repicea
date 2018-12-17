package repicea.net.server;

import java.net.SocketAddress;

public class FakeClient extends BasicClient {

	protected FakeClient(SocketAddress socketAddress) throws BasicClientException {
		super(socketAddress, 30);
	}
	
	protected Object sendFakeRequest() throws BasicClientException {
		Double latitude = 46d;
		Double longitude = -71d;
		Double altitude = 300d;

		String request = latitude.toString().concat(longitude.toString()).concat(altitude.toString());
		Object result = processRequest(request);
		return result;
	}	
	
}
