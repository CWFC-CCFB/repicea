package repicea.net.server;

import java.net.SocketAddress;

public class FakeClient extends BasicClient {

	protected FakeClient(SocketAddress socketAddress, boolean isAJavaApplication) throws BasicClientException {
		super(socketAddress, 30, isAJavaApplication);
	}
	
	protected Object sendFakeRequest() throws BasicClientException {
		Double latitude = 46d;
		Double longitude = -71d;
		Double altitude = 300d;

		String request = latitude.toString().concat(longitude.toString()).concat(altitude.toString());
		Object result = processRequest(request);
		return result;
	}	
	
	protected Object createAnArrayList() throws BasicClientException {
		String request = "create;java.util.ArrayList";
		Object result = processRequest(request);
		return result;
	}
	
	protected Object addThisToArrayList(Object arrayList, String toBeAdded) throws BasicClientException {
		String request = "method;" + arrayList.toString().replace("JavaObject;java.util.ArrayList@", "java.objecthashcode") + ";add;" + toBeAdded;
		Object result = processRequest(request);
		return result;
	}
}
