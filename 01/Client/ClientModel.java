import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientModel {

	private String ip;
	private int port;
	private Socket socket;

	public ClientModel() {
		setIp("127.0.0.1");
		setPort(50000);
		System.out.print("127.0.0.1" + " -> " + getIp()
				+ " (Client -> Server)." + "\n");
	}

	public ClientModel(String ip, int port) {
		setIp(ip);
		setPort(port);
		System.out.print("127.0.0.1" + " -> " + getIp()
				+ " (Client -> Server)." + "\n");
	}

	public void verbindungAufbauen() throws UnknownHostException, IOException {
		// Es wird versucht eine Verbindung zum Server herzustellen.
		if(!istVerbunden()){
			setSocket(new Socket(getIp(), getPort()));
		}
	}

	public void verbindungTrennen() throws IOException {
		// Es wird versucht eine Verbindung zum Server trennen.
		if(istVerbunden()){
			getSocket().close();
			setSocket(null);
		}
	}
	
	public boolean istVerbunden(){
		if(getSocket() == null){
			return false;
		}
		if(getSocket().isClosed()){
			return false;
		}
		if(getSocket().isConnected()){
			return true;
		}
		return false;
	}

	public String getIp() {
		return ip;
	}

	private void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	private void setPort(int port) {
		this.port = port;
	}

	public Socket getSocket() {
		return socket;
	}

	private void setSocket(Socket socket) {
		this.socket = socket;
	}
}
