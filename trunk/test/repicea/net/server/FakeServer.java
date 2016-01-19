package repicea.net.server;

class FakeServer extends AbstractServer {

	protected static class FakeClientThread extends ClientThread {

		protected FakeClientThread(AbstractServer caller, int workerID) {
			super(caller, workerID);
		}

		@Override
		protected void processRequest() throws Exception {
			String request = (String) getSocket().readObject();
			String[] requestStrings = request.split(";");
			if (requestStrings.length == 3) {
				double latitude = Double.parseDouble(requestStrings[0]);
				double longitude = Double.parseDouble(requestStrings[1]);
				float altitude = Float.parseFloat(requestStrings[2]);
				int u = 0;
			}
		}
		
	}
	
	
	FakeServer(ServerConfiguration configuration) throws Exception {
		super(configuration);
	}

	@Override
	protected ClientThread createClientThread(AbstractServer server, int id) {
		return new FakeClientThread(server, id);
	}

	public static void main(String[] args) {
		try {
			ServerConfiguration configuration = new ServerConfiguration(5, 18000, 18804);
			System.out.println("Configuration instantiated");
			FakeServer server = new FakeServer(configuration);		
			System.out.println("Server instantiated");
			server.startApplication();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
	}

}
