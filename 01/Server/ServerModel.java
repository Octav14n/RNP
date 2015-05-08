import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class ServerModel {

	private int port;
	private ServerSocket serverSocket;
	private Socket socket;
	private LinkedList<Socket> sockets;
	private boolean clientsAnnehmen;
	private boolean serverBeenden;
	private int maxVerbindung;
	private String passwort;

	public ServerModel() {
		setPort(50000);
		setSockets(new LinkedList<Socket>());
		setClientsAnnehmen(true);
		setServerBeenden(false);
		setMaxVerbindung(3);
		setPasswort("123456");
	}

	public void serverSocketStart() {
		System.out.print("Server startet" + "\n");
		try {
			setServerSocket(new ServerSocket(getPort()));
			// Maximale Zeichenfolge eines Strings
			getServerSocket().setReceiveBufferSize(255);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void annahme() {
		try {
			setSocket(getServerSocket().accept());
		} catch (IOException e) {
			// e.printStackTrace();
		}
	}

	public void clientsAnnehmen() {

		boolean stopp = false;
		while (!stopp) {
			// Server nimmt keine Anfragen mehr entgegen, nachdem der
			// Shutdown Befehll eingegeben wurde.
			if (!isServerBeenden()) {
				// Maximale Verbindungen test
				setClientsAnnehmen(getSockets().size() < getMaxVerbindung());
				// Wenn das Maximum erricht wurde, werden keine Weiteren
				// angenommen.
				if (isClientsAnnehmen()) {
					System.out.print("Warte auf neue Verbindung" + "\n");
					// Wartet auf anfragen eines Clients
					annahme();
					Thread thread = new Thread() {
						public void run() {
							// Die Verbindungen bekommen einen eigenen
							// Thread.
							verbindungAnnehmen(getSocket());
						}
					};
					thread.start();
					// Die Verbindung wird in eine Liste eingereiht.
					addSocket(getSocket());
				}
			} else {
				setClientsAnnehmen(false);
				// Wenn die Liste leer ist, kann der Server komplett
				// herunter fahren.
				if (getSockets().isEmpty()) {
					stopp = true;
				}
			}
		}
		System.out.print("Server gestoppt" + "\n");
	}

	private void verbindungAnnehmen(Socket socket) {
		System.out.print("Verbindung hergestellt" + "\n");
		try {
			boolean stopp = false;
			while (!stopp && !socket.isClosed()) {
				// Lese die eingehenden Daten.
				String text = leseDaten(socket);
				if (text != "") {
					System.out.print("eingang: " + text + "");
					// Ermittle Antwort der Eingabe.
					String antwort = antwort(text);
					System.out.print("ausgang: " + antwort + "");
					// Die Antwort zum Client schicken.
					schreibeDaten(socket, antwort);

					if (antwort.equals("BYE\n")) {
						socket.close();
						stopp = true;
					}
				}
			}
			socket.close();
			removeSocket(socket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.print("Verbindung getrennt" + "\n");
	}

	private String leseDaten(Socket socket) {
		String text = "";
		try {
			InputStream inputStream = socket.getInputStream();
			int availableBytes = inputStream.available();
			if (availableBytes > 0) {
				byte[] bytes = new byte[availableBytes];
				inputStream.read(bytes);
				text = new String(bytes);
			}
		} catch (IOException e) {
//			e.printStackTrace();
		}
		return text;
	}

	private void schreibeDaten(Socket socket, String text) {
		try {
			byte[] bytes = text.getBytes("UTF-8");
			OutputStream outputStream = socket.getOutputStream();
			outputStream.write(bytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String antwort(String text) {
		String antwort = "";
		String[] woerter = text.split(" ");
		if (woerter.length > 0) {
			woerter[0] = woerter[0].replace("\n", "");
			String rest = text.replace("\n", "");
			rest = rest.replaceAll(woerter[0] + " ", "");
			switch (woerter[0]) {
			case "UPPERCASE":
				antwort = "OK " + rest.toUpperCase() + "\n";
				break;
			case "LOWERCASE":
				antwort = "OK " + rest.toLowerCase() + "\n";
				break;
			case "REVERSE":
				antwort = "OK " + reverse(rest) + "\n";
				break;
			case "BYE":
				antwort = "BYE\n";
				break;
			case "SHUTDOWN":
				if (getPasswort().equals(rest)) {
					antwort = "BYE\n";
					setServerBeenden(true);
					try {
						serverSocket.close();
					} catch (IOException e) {
						// e.printStackTrace();
						System.out.print("Server wurde geschossen." + "\n");
					}
				} else {
					antwort = "ERROR: Falsches Passwort\n";
				}
				break;
			default:
				antwort = "ERROR: Unbekannter Befehl\n";
			}
		}
		return antwort;
	}

	private String reverse(String nachricht) {
		String ergebnis = "";
		char[] buchstaben = nachricht.toCharArray();
		for (int i = buchstaben.length - 1; i >= 0; i--) {
			ergebnis += buchstaben[i];
		}
		return ergebnis;
	}

	public int getPort() {
		return port;
	}

	private void setPort(int port) {
		this.port = port;
	}

	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	private void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}

	public Socket getSocket() {
		return socket;
	}

	private void setSocket(Socket socket) {
		this.socket = socket;
	}

	public LinkedList<Socket> getSockets() {
		return sockets;
	}

	private void setSockets(LinkedList<Socket> sockets) {
		this.sockets = sockets;
	}

	private void addSocket(Socket socket) {
		this.sockets.add(socket);
	}

	private void removeSocket(Socket socket) {
		this.sockets.remove(socket);
	}

	public boolean isClientsAnnehmen() {
		return clientsAnnehmen;
	}

	public void setClientsAnnehmen(boolean clientsAnnehmen) {
		this.clientsAnnehmen = clientsAnnehmen;
	}

	public boolean isServerBeenden() {
		return serverBeenden;
	}

	private void setServerBeenden(boolean serverBeenden) {
		this.serverBeenden = serverBeenden;
	}

	public int getMaxVerbindung() {
		return maxVerbindung;
	}

	private void setMaxVerbindung(int maxVerbindung) {
		this.maxVerbindung = maxVerbindung;
	}

	private String getPasswort() {
		return passwort;
	}

	private void setPasswort(String passwort) {
		this.passwort = passwort;
	}
}
