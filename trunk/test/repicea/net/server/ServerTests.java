package repicea.net.server;

import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Test;

import repicea.net.server.AbstractServer.Mode;
import repicea.net.server.AbstractServer.ServerReply;

public class ServerTests {

	@Test
	public void testServerConnectionSingleRequest() throws Exception {
		ServerConfiguration configuration = new ServerConfiguration(1, 18001, 18805);
		System.out.println("Configuration instantiated");
		FakeServer server = new FakeServer(configuration, Mode.AnswerProcessAndClose);		
		System.out.println("Server instantiated");
		server.startApplication();
		 
		InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 18001);
		FakeClient client = new FakeClient(socketAddress);
		Object callback = client.sendFakeRequest();
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.equals(ServerReply.ClosingConnection));
		client.close();
		System.out.println("Server basic implementation answer, reply and close successfully tested!");
	}
	
	
	@Test
	public void testServerConnectionMultipleRequests() throws Exception {
		ServerConfiguration configuration = new ServerConfiguration(1, 18000, 18804);
		System.out.println("Configuration instantiated");
		FakeServer server = new FakeServer(configuration, Mode.AnswerAndProcessUntilClientClose);		
		System.out.println("Server instantiated");
		server.startApplication();
		 
		InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getLoopbackAddress(), 18000);
		FakeClient client = new FakeClient(socketAddress);
		Object callback = client.sendFakeRequest();
		
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.equals(ServerReply.RequestReceivedAndProcessed));
		
		callback = client.sendFakeRequest();
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.equals(ServerReply.RequestReceivedAndProcessed));
		
		callback = client.sendFakeRequest();
		Assert.assertTrue(callback != null);
		Assert.assertTrue(callback.equals(ServerReply.RequestReceivedAndProcessed));
		
		client.close();
		System.out.println("Server implementation with multiple requests (3) successfully tested!");
	}

	
	
}
