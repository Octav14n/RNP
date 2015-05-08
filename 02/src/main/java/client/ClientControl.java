package client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.UnknownHostException;

public class ClientControl {

	@Getter @Setter(AccessLevel.PROTECTED)
	private ClientModel clientModel;

	public ClientControl(ClientModel clientModel) {
		setClientModel(clientModel);
		standard();
	}

	private void standard() {
		connect();
		//disconnect();
	}

	public void retrieveMessage() {

	}

	private void connect() {
		if (!getClientModel().istVerbunden()) {
			try {
				// Versuche die Verbindung zum Server herzustellen.
				getClientModel().verbindungAufbauen();
				if (getClientModel().istVerbunden()) {
					// Den Thread zum Zuhoeren starten.
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void disconnect() {
		if (getClientModel().istVerbunden()) {
			// Versuche die Verbindung zum Server trennen.
			// getClientModel().verbindungTrennen();
			if (!getClientModel().istVerbunden()) {
				System.out.println("Verbindung beendet.");
			}
		}
	}

}
