import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPClient {
	public UDPClient() {
		try {
			String daten = "hallo";
			String ipDns = "localhost";
			int serverPort = 6789;
			byte[] m = daten.getBytes();
			byte[] buffer = new byte[1000];

			DatagramSocket aSocket = new DatagramSocket();
			InetAddress aHost = InetAddress.getByName(ipDns);
			DatagramPacket request = new DatagramPacket(m, daten.length(),
					aHost, serverPort);
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

			aSocket.send(request);
			aSocket.receive(reply);
			System.out.println("Reply: " + new String(reply.getData()));
			aSocket.close();
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}
	}
}
