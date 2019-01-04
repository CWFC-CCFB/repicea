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
		String request = "create;repicea.net.server.FakeArrayList";
		Object result = processRequest(request);
		return result;
	}
	
	protected Object addThisToArrayList(Object arrayList, String toBeAdded) throws BasicClientException {
		String request = "method;" + arrayList.toString().replace("JavaObject;repicea.net.server.FakeArrayList@", "java.objecthashcode") + ";add;" + toBeAdded;
		Object result = processRequest(request);
		return result;
	}

	protected Object testThisDoubleWrapper(Object arrayList) throws BasicClientException {
		String request = "method;" + arrayList.toString().replace("JavaObject;repicea.net.server.FakeArrayList@", "java.objecthashcode") +
				";processThisDouble;" + "numeric0";
		Object result = processRequest(request);
		return result;
	}
}
