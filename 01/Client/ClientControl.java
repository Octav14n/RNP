import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;

public class ClientControl {

	private ClientModel clientModel;
	private ClientView clientView;

	public ClientControl(ClientModel clientModel, ClientView clientView) {
		setClientModel(clientModel);
		setClientView(clientView);
		standard();
	}

	public void standard() {
		standardTextFieldUndSendButton();
		standardConnectButton();
		standardDisconnectButton();
	}

	private void standardTextFieldUndSendButton() {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				// Text extrahieren.
				String textNew = getClientView().getTextField().getText();
				String textOld = getClientView().getTextArea().getText();
				// Feld leeren.
				getClientView().getTextField().setText("");
				// Text in den Log schreiben.
				getClientView().getTextArea().setText(textNew + "\n" + textOld);
				// getClientView().getTextArea().setText(
				// "Client: " + textNew + "\n" + textOld);
				// Hier wird versucht den eingegebenen Text zum Server zu
				// senden.
				sendeZumServer(textNew);
			}
		};
		getClientView().getTextField().addActionListener(actionListener);
		getClientView().getSendButton().addActionListener(actionListener);
	}

	private void standardConnectButton() {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if (!getClientModel().istVerbunden()) {
					try {
						// Versuche die Verbindung zum Server herzustellen.
						getClientModel().verbindungAufbauen();
						if (getClientModel().istVerbunden()) {
							// Den Thread zum Zuhoeren starten.
							empfangeVomServer();
							// Text extrahieren.
							String textOld = getClientView().getTextArea()
									.getText();
							// Text in den Log schreiben.
							getClientView().getTextArea().setText(
									"Verbunden" + "\n" + textOld);
						}

					} catch (UnknownHostException e) {
						// Text extrahieren.
						String textOld = getClientView().getTextArea()
								.getText();
						// Text in den Log schreiben.
						getClientView().getTextArea().setText(
								e + "\n" + textOld);
					} catch (IOException e) {
						// Text extrahieren.
						String textOld = getClientView().getTextArea()
								.getText();
						// Text in den Log schreiben.
						getClientView().getTextArea().setText(
								e + "\n" + textOld);
					}
				}
			}
		};
		getClientView().getConnectButton().addActionListener(actionListener);
	}

	private void standardDisconnectButton() {
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if (getClientModel().istVerbunden()) {
					// Versuche die Verbindung zum Server trennen.
					// getClientModel().verbindungTrennen();
					sendeZumServer("BYE");
					if (!getClientModel().istVerbunden()) {
						// Text extrahieren.
						String textOld = getClientView().getTextArea()
								.getText();
						// Text in den Log schreiben.
						getClientView().getTextArea().setText(
								"Getrennt" + "\n" + textOld);
					}
				}
			}
		};
		getClientView().getDisconnectButton().addActionListener(actionListener);
		// getClientView().getDisconnectButton().setEnabled(false);
	}

	private void empfangeVomServer() {
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
							// Text extrahieren.
							String textOld = getClientView().getTextArea()
									.getText();
							// Text in den Log schreiben.
							getClientView().getTextArea().setText(
									textNew + "" + textOld);
							// getClientView().getTextArea().setText(
							// "Server: " + textNew + "\n" + textOld);
							if (textNew.equals("BYE\n")) {
								getClientModel().verbindungTrennen();
								stopp = true;
								// Text extrahieren.
								textOld = getClientView().getTextArea()
										.getText();
								// Text in den Log schreiben.
								getClientView().getTextArea().setText(
										"Verbindung getrennt" + "\n" + textOld);
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

	private void sendeZumServer(String text) {
		if (getClientModel().istVerbunden()) {
			byte[] bytes;
			try {
				bytes = stringToBytes(text + "\n");
				schreibeBytes(getClientModel().getSocket().getOutputStream(),
						bytes);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				// Text extrahieren.
				String textOld = getClientView().getTextArea().getText();
				// Text in den Log schreiben.
				getClientView().getTextArea().setText(e + "\n" + textOld);
			} catch (IOException e) {
				e.printStackTrace();
				// Text extrahieren.
				String textOld = getClientView().getTextArea().getText();
				// Text in den Log schreiben.
				getClientView().getTextArea().setText(e + "\n" + textOld);
			}
		} else {
			// Text extrahieren.
			String textOld = getClientView().getTextArea().getText();
			// Text in den Log schreiben.
			getClientView().getTextArea().setText(
					"You are not connected" + "\n" + textOld);
		}

	}

	// private byte[] leseBytes(InputStream inputStream) throws IOException {
	// int anzahl = inputStream.available();
	// byte[] bytes = new byte[anzahl];
	// inputStream.read(bytes);
	// return bytes;
	// }

	private String bytesToString(byte[] bytes)
			throws UnsupportedEncodingException {
		return new String(bytes, "UTF-8");
	}

	private void schreibeBytes(OutputStream outputStream, byte[] bytes)
			throws IOException {
		outputStream.write(bytes);
	}

	private byte[] stringToBytes(String string)
			throws UnsupportedEncodingException {
		return string.getBytes("UTF-8");
	}

	public ClientModel getClientModel() {
		return clientModel;
	}

	public void setClientModel(ClientModel clientModel) {
		this.clientModel = clientModel;
	}

	public ClientView getClientView() {
		return clientView;
	}

	public void setClientView(ClientView clientView) {
		this.clientView = clientView;
	}
}
