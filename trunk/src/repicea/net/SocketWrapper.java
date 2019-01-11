package repicea.net;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;

public interface SocketWrapper extends Closeable {

	public Object readObject() throws Exception;

	public Object readObject(int timeout) throws Exception;
	
	
	public void writeObject(Object obj) throws IOException;
	
	public boolean isClosed();
	
	public InetAddress getInetAddress();
}
