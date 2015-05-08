package client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

public class ClientControl {

	@Getter @Setter(AccessLevel.PROTECTED)
	private ClientModel clientModel;

	public ClientControl(ClientModel clientModel) {
		setClientModel(clientModel);
		standard();
	}

	public void standard() {
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
					empfangeAsync();
				}

			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void disconnect() {
		if (getClientModel().istVerbunden()) {
			// Versuche die Verbindung zum Server trennen.
			// getClientModel().verbindungTrennen();
			sende("BYE");
			if (!getClientModel().istVerbunden()) {
				System.out.println("Verbindung beendet.");
			}
		}
	}

	/**
	 * Startet einen Thread um von dem Server Daten zu empfangen.
	 */
	private void empfangeAsync() {
		Thread thread = new Thread() {
			public void run() {
				boolean stopp = false;
				while (!stopp) {
					try {
						InputStream inputStream = getClientModel().getSocket()
								.getInputStream();
						int anzahl = inputStream.available();
						if (anzahl > 0) {
							byte[] bytes = new byte[anzahl];
							inputStream.read(bytes);

							// byte[] bytes =
							// leseBytes(getClientModel().getSocket()
							// .getInputStream());
							String textNew = bytesToString(bytes);

							System.out.println(textNew);
							// getClientView().getTextArea().setText(
							// "Server: " + textNew + "\n" + textOld);
							if (textNew.equals("BYE\n")) {
								getClientModel().verbindungTrennen();
								stopp = true;
								System.out.println("Verbindung getrennt");
							}
						}
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		};
		thread.start();
	}

	/**
	 * Sendet den übergebenen Text an den Server. (Blockierend)
	 * @param text Zu übertragender Text.
	 * @throws IllegalStateException Wird geworfen, wenn gesendet werden soll obwohl der Client nicht connected ist.
	 */
	private void sende(String text) throws IllegalStateException {
		if (getClientModel().istVerbunden()) {
			byte[] bytes;
			try {
				bytes = stringToBytes(text + "\n");
				schreibeBytes(getClientModel().getSocket().getOutputStream(),
						bytes);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			// Text in den Log schreiben.
			throw new IllegalStateException("You are not connected");
		}
	}

}
