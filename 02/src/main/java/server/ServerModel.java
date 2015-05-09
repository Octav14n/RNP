package server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;

public class ServerModel {
    @Getter @Setter
    private static int maxVerbindung;
    @Getter @Setter
    private static Map<String, String> credentials;

    @Getter
    private final String ip;
    @Getter(AccessLevel.PRIVATE)
    private final int port;
    @Getter(AccessLevel.PRIVATE)
    private final ServerSocket serverSocket;
    @Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE)
    private boolean clientsAnnehmen = true;
    @Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE)
    private boolean serverBeenden = false;
    @Getter(AccessLevel.PRIVATE)
    private LinkedList<ServerClient> clients;

	public ServerModel() throws IOException {
		port = 50000;
        ip = "localhost";
		clients = new LinkedList<>();
        serverSocket = new ServerSocket(getPort());

        // Maximale Zeichenfolge eines Strings
        getServerSocket().setReceiveBufferSize(255);
	}

	public void clientsAnnehmen() throws IOException {

		boolean stopp = false;
		while (!stopp) {
			// Server nimmt keine Anfragen mehr entgegen, nachdem der
			// Shutdown Befehll eingegeben wurde.
			if (!isServerBeenden()) {
				// Maximale Verbindungen test
				setClientsAnnehmen(getClients().size() < getMaxVerbindung());
				// Wenn das Maximum erricht wurde, werden keine Weiteren
				// angenommen.
				if (isClientsAnnehmen()) {
					System.out.print("Warte auf neue Verbindung" + "\n");
					// Wartet auf anfragen eines Clients
                    ServerClient serverClient = new ServerClient(this, getServerSocket().accept());

					// Die Verbindung wird in eine Liste eingereiht.
					addClient(serverClient);
				}
			} else {
				setClientsAnnehmen(false);
				// Wenn die Liste leer ist, kann der Server komplett
				// herunter fahren.
				if (getClients().isEmpty()) {
					stopp = true;
				}
			}
		}
		System.out.print("Server gestoppt" + "\n");
	}

    private void addClient(ServerClient serverClient) {
        getClients().add(serverClient);
    }

    public void removeClient(ServerClient serverClient) {
        getClients().remove(serverClient);
    }
}
