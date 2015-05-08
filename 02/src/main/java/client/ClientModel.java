package client;

import helpers.PopState;
import helpers.UTF8Util;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class ClientModel implements Runnable {

	@Getter @Setter(AccessLevel.PROTECTED)
	private String ip;
	@Getter @Setter(AccessLevel.PROTECTED)
	private int port;
	@Getter @Setter(AccessLevel.PROTECTED)
	private Socket socket;
	@Getter @Setter(AccessLevel.PRIVATE)
	private String username;
	@Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PROTECTED)
	private String password;
	@Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED)
	private PopState state = PopState.DISCONECTED;

	@Override
	public void run() {
		if (PopState.DISCONECTED == getState() || PopState.EXPECTING_EXIT == getState())
			throw new RuntimeException("Can't start Client with state: " + getState().toString());
		while (PopState.EXPECTING_EXIT != getState()) {
			switch (getState()) {
				case CONNECTED:
					authenticate();
					break;
				case AUTHORIZED:
					fetchMails();
					break;
			}
		}
	}

	private void authenticate() {
		UTF8Util.schreibeBytes(socket.getOutputStream(), UTF8Util.stringToBytes("USER username"));
	}

	public ClientModel(String username, String password) {
		this("127.0.0.1", 50000, username, password);
	}

	public ClientModel(String ip, int port, String username, String password) {
		setIp(ip);
		setPort(port);
		System.out.print("127.0.0.1" + " -> " + getIp()
				+ " (Client -> Server)." + "\n");
	}

	public void verbindungAufbauen() throws UnknownHostException, IOException {
		// Es wird versucht eine Verbindung zum Server herzustellen.
		if(!istVerbunden()){
			setSocket(new Socket(getIp(), getPort()));
			setState(PopState.CONNECTED);
		}
	}

	public void verbindungTrennen() throws IOException {
		// Es wird versucht eine Verbindung zum Server trennen.
		if(istVerbunden()){
			getSocket().close();
			setSocket(null);
			setState(PopState.DISCONECTED);
		}
	}
	
	public boolean istVerbunden(){
		if(PopState.DISCONECTED == getState() || getSocket() == null || getSocket().isClosed()){
			return false;
		}
		if(getSocket().isConnected()){
			return true;
		}
		return false;
	}
}
