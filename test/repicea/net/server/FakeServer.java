package repicea.net.server;

import repicea.net.server.BasicClient.ClientRequest;

class FakeServer extends AbstractServer {

	protected static class FakeClientThread extends ClientThread {

		protected FakeClientThread(AbstractServer caller, int workerID) {
			super(caller, workerID);
		}

		@SuppressWarnings("unused")
		@Override
		protected Object processRequest() throws Exception {
			Object crudeRequest = getSocket().readObject();
			if (crudeRequest instanceof ClientRequest) {
				return crudeRequest;
			} else if (crudeRequest instanceof String) {
				String request = (String) crudeRequest;
				String[] requestStrings = request.split(";");
				if (requestStrings.length == 3) {
					double latitude = Double.parseDouble(requestStrings[0]);
					double longitude = Double.parseDouble(requestStrings[1]);
					float altitude = Float.parseFloat(requestStrings[2]);
				}
			}
			return null;
		}
		
	}
	
	
	FakeServer(ServerConfiguration configuration, Mode mode) throws Exception {
		super(configuration, mode, true);
	}

	@Override
	protected ClientThread createClientThread(AbstractServer server, int id) {
		return new FakeClientThread(server, id);
	}
}
