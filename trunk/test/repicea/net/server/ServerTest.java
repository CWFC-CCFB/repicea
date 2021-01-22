package repicea.net.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Vector;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import repicea.lang.codetranslator.REnvironment;
import repicea.net.server.AbstractServer.ServerReply;
import repicea.net.server.ServerConfiguration.Protocol;

public class ServerTest {

	@Test
	public void testDistantTCPServerWithSingleRequest() throws Exception {
		ServerConfiguration configuration = new ServerConfiguration(1, 0, 18002, 18805);
		System.out.println("Configuration instantiated");
		FakeDistantServer server = new FakeDistantServer(configuration);		
		System.out.println("Server instantiated");
		server.startApplication();
		 
		InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 18002);
		FakeClient client = new FakeClient(socketAddress, Protocol.TCP, true);	// true: is a Java application
		Object callback = client.sendFakeRequest();
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.equals(ServerReply.ClosingConnection));
//		client.close();
		System.out.println("TCP Server basic implementation answer, reply and close successfully tested!");
	}
	
	@SuppressWarnings({"rawtypes" })
	@Test
	public void testLocalTCPServerMultipleRequests() throws Exception {
		REnvironment env = new REnvironment();
		JavaLocalGatewayServer server = new JavaLocalGatewayServer(new ServerConfiguration(18001, Protocol.TCP), env, false);	// false: will not shutdown after loosing the connection	
		System.out.println("Server instantiated");
		server.startApplication();
		 
		InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 18001);
		FakeClient client = new FakeClient(socketAddress, Protocol.TCP, false);	// false: it does as if was not a Java application
		client.sendTimeToServer();

		long start = System.currentTimeMillis();
		Object arrayListRepresentation = client.createAnArrayList(); // creates a FakeArrayList object which will be used to test the Double vs double method call
		double diff = (System.currentTimeMillis() - start) * .001;
		System.out.println("Time to process the creation of an ArrayList instance in TCP : " + diff);
		Assert.assertTrue(arrayListRepresentation != null);
		Assert.assertTrue(arrayListRepresentation.toString().startsWith("JavaObject" + REnvironment.MainSplitter + "repicea.net.server.FakeArrayList"));
		int hashCode = Integer.parseInt(arrayListRepresentation.toString().substring(arrayListRepresentation.toString().indexOf("@") + 1));
		ArrayList trueArrayList = (ArrayList) env.get(hashCode);
		
		Object callback = client.addThisToArrayList(arrayListRepresentation, "characterhello world!");		// here we add "hello world!" to the arraylist
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.toString().equals("logicaltrue"));
		Assert.assertTrue(trueArrayList.get(0).toString().equals("hello world!"));

		callback = client.addThisToArrayList(arrayListRepresentation, "integer1");  	// here we add the integer 1 to the arraylist
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.toString().equals("logicaltrue"));
		Assert.assertTrue((int) trueArrayList.get(1) == 1);

		callback = client.testThisDoubleWrapper(arrayListRepresentation);		// here we test if a method with Double as argument will be match to a double 
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.toString().equals("numeric0.0"));

		callback = client.createAVectorWithArguments();
		Assert.assertTrue(callback != null);
		hashCode = Integer.parseInt(callback.toString().substring(callback.toString().indexOf("@") + 1));
		Vector listWithCapacityOf3 = (Vector) env.get(hashCode);
		Assert.assertEquals("Testing capacity", listWithCapacityOf3.capacity(), 3);

		callback = client.createMultipleVectorWithArguments();			// creating several objects at once
		Assert.assertTrue(callback != null);
		String[] objectReps = callback.toString().split(REnvironment.SubSplitter);
		int expCapacity = 3;
		for (String objRep : objectReps) {
			hashCode = Integer.parseInt(objRep.toString().substring(objRep.toString().indexOf("@") + 1));
			Vector vec = (Vector) env.get(hashCode);
			Assert.assertEquals("Testing capacity", vec.capacity(), expCapacity);
			expCapacity++;
		}

//		client.close();			
		System.out.println("TCP Server implementation with multiple requests (3) successfully tested!");
		server.bypassShutdownForTesting = true;
		server.requestShutdown();
	}


	@Ignore
	@SuppressWarnings({"rawtypes" })
	@Test
	public void testLocalUDPServerMultipleRequests() throws Exception {
		
		REnvironment env = new REnvironment();
		JavaLocalGatewayServer server = new JavaLocalGatewayServer(new ServerConfiguration(18000, Protocol.UDP), env, false);	// false: will not shutdown after loosing the connection	
		System.out.println("Server instantiated");
		server.startApplication();
		
		long time = System.currentTimeMillis();
		while (System.currentTimeMillis() - time < 2000) {}
		
		InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 18000);
		@SuppressWarnings("resource")
		FakeClient client = new FakeClient(socketAddress, Protocol.UDP, false);	// false: it does as if was not a Java application
		client.sendTimeToServer();

		long start = System.currentTimeMillis();
		Object arrayListRepresentation = client.createAnArrayList(); // creates a FakeArrayList object which will be used to test the Double vs double method call
		double diff = (System.currentTimeMillis() - start) * .001;
		System.out.println("Time to process the creation of an ArrayList instance in UDP : " + diff);
		Assert.assertTrue(arrayListRepresentation != null);
		Assert.assertTrue(arrayListRepresentation.toString().startsWith("JavaObject" + REnvironment.MainSplitter + "repicea.net.server.FakeArrayList"));
		int hashCode = Integer.parseInt(arrayListRepresentation.toString().substring(arrayListRepresentation.toString().indexOf("@") + 1));
		ArrayList trueArrayList = (ArrayList) env.get(hashCode);
		
		Object callback = client.addThisToArrayList(arrayListRepresentation, "characterhello world!");		// here we add "hello world!" to the arraylist
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.toString().equals("logicaltrue"));
		Assert.assertTrue(trueArrayList.get(0).toString().equals("hello world!"));

		callback = client.addThisToArrayList(arrayListRepresentation, "integer1");  	// here we add the integer 1 to the arraylist
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.toString().equals("logicaltrue"));
		Assert.assertTrue((int) trueArrayList.get(1) == 1);

		callback = client.testThisDoubleWrapper(arrayListRepresentation);		// here we test if a method with Double as argument will be match to a double 
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.toString().equals("numeric0.0"));

		callback = client.createAVectorWithArguments();
		Assert.assertTrue(callback != null);
		hashCode = Integer.parseInt(callback.toString().substring(callback.toString().indexOf("@") + 1));
		Vector listWithCapacityOf3 = (Vector) env.get(hashCode);
		Assert.assertEquals("Testing capacity", listWithCapacityOf3.capacity(), 3);

		callback = client.createMultipleVectorWithArguments();			// creating several objects at once
		Assert.assertTrue(callback != null);
		String[] objectReps = callback.toString().split(REnvironment.SubSplitter);
		int expCapacity = 3;
		for (String objRep : objectReps) {
			hashCode = Integer.parseInt(objRep.toString().substring(objRep.toString().indexOf("@") + 1));
			Vector vec = (Vector) env.get(hashCode);
			Assert.assertEquals("Testing capacity", vec.capacity(), expCapacity);
			expCapacity++;
		}

//		client.close();			
		System.out.println("UDP Server implementation with multiple requests (3) successfully tested!");
		server.bypassShutdownForTesting = true;
		server.requestShutdown();
	}

	
}
