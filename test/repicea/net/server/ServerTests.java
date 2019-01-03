package repicea.net.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import repicea.lang.codetranslator.REnvironment;
import repicea.net.server.AbstractServer.ServerReply;

public class ServerTests {

	// TODO FIX THIS TEST IT CAUSES the JVM to exit
	@SuppressWarnings("resource")
	@Test
	public void testDistantServerWithSingleRequest() throws Exception {
		ServerConfiguration configuration = new ServerConfiguration(1, 18001, 18805);
		System.out.println("Configuration instantiated");
		FakeDistantServer server = new FakeDistantServer(configuration);		
		System.out.println("Server instantiated");
		server.startApplication();
		 
		InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 18001);
		FakeClient client = new FakeClient(socketAddress, true);	// true: is a Java application
		Object callback = client.sendFakeRequest();
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.equals(ServerReply.ClosingConnection));
		client.close();
		System.out.println("Server basic implementation answer, reply and close successfully tested!");
	}
	
//	@SuppressWarnings({ "resource", "rawtypes" })
//	@Test
//	public void testLocalServerMultipleRequests() throws Exception {
//		REnvironment env = new REnvironment();
//		JavaLocalGatewayServer server = new JavaLocalGatewayServer(18000, env);		
//		System.out.println("Server instantiated");
//		server.startApplication();
//		 
//		InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 18000);
//		FakeClient client = new FakeClient(socketAddress, false);	// false: it does as if was not a Java application
//		Object arrayListRepresentation = client.createAnArrayList();
//		Assert.assertTrue(arrayListRepresentation != null);
//		Assert.assertTrue(arrayListRepresentation.toString().startsWith("JavaObject;java.util.ArrayList"));
//		int hashCode = Integer.parseInt(arrayListRepresentation.toString().substring(arrayListRepresentation.toString().indexOf("@") + 1));
//		ArrayList trueArrayList = (ArrayList) env.get(hashCode);
//		
//		Object callback = client.addThisToArrayList(arrayListRepresentation, "characterhello world!");		// here we add "hello world!" to the arraylist
//		Assert.assertTrue(callback != null);
//		Assert.assertTrue(callback.toString().equals("logicaltrue"));
//		Assert.assertTrue(trueArrayList.get(0).toString().equals("hello world!"));
//
//		callback = client.addThisToArrayList(arrayListRepresentation, "integer1");  	// here we add the integer 1 to the arraylist
//		Assert.assertTrue(callback != null);
//		Assert.assertTrue(callback.toString().equals("logicaltrue"));
//		Assert.assertTrue((int) trueArrayList.get(1) == 1);
//
//		// TODO handle multiple  caller as in the vector logic 
//		// TODO handle the switch from primitive to wrapper for instance a list of Double should add double
////		callback = client.sendFakeRequest();
////		Assert.assertTrue(callback != null);
////		Assert.assertTrue(callback.equals(ServerReply.RequestReceivedAndProcessed));
//		
////		client.close();			// otherwise the JVM is shutdown and the test series aborts
//		System.out.println("Server implementation with multiple requests (3) successfully tested!");
//	}

	
	
}
