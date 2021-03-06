package filecopy;

/* FileCopyClient.java
 Version 0.1 - Muss erg�nzt werden!!
 Praktikum 3 Rechnernetze BAI4 HAW Hamburg
 Autoren:
 */

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class FileCopyClient extends Thread {

	// -------- Constants
	public final static boolean TEST_OUTPUT_MODE = false;

	public final int SERVER_PORT = 23000;

	public final int UDP_PACKET_SIZE = 1008;

	// -------- Public parms
	public String servername;

	public String sourcePath;

	public String destPath;

	public int windowSize;

	public long serverErrorRate;

	// -------- Variables
	// current default timeout in nanoseconds
	private long timeoutValue = 100000000L;

	// ... ToDo
	Path path;
	byte[] datei;
	int size = 1000;
	ArrayList<FCpacket> filepuffer;
	ArrayList<FCpacket> sendepuffer;
	long sendbase;
	long nextSeqNum;

	DatagramSocket aSocket;
	InetAddress aHost;
	DatagramPacket request;
	DatagramPacket reply;

	// static final Lock lock = new ReentrantLock();
	// static final Condition notFull = lock.newCondition();

	static boolean blocked = false;

	// Auswertung
	long sendTime;
	int finishedTimerCounter;
	int resendCounter;
	int ackCounter;
	int sendCounter;

	// Constructor
	public FileCopyClient(String serverArg, String sourcePathArg,
			String destPathArg, String windowSizeArg, String errorRateArg) {
		servername = serverArg;
		sourcePath = sourcePathArg;
		destPath = destPathArg;
		windowSize = Integer.parseInt(windowSizeArg);
		serverErrorRate = Long.parseLong(errorRateArg);

	}

	public void runFileCopyClient() {
		// ToDo!!
		try {
			path = Paths.get(sourcePath);
			datei = Files.readAllBytes(path);
			filepuffer = new ArrayList<FCpacket>();
			sendepuffer = new ArrayList<FCpacket>();

			aSocket = new DatagramSocket();
			aHost = InetAddress.getByName(servername);

			byte[] receiveData = new byte[UDP_PACKET_SIZE];
			reply = new DatagramPacket(receiveData, receiveData.length);
			ReceivedThread readthread = new ReceivedThread();
			SendThread writethread = new SendThread();

			// Paket Anfertigung
			createAllPacket();
			packetCheck();

			// Auswertung
			finishedTimerCounter = 0;
			resendCounter = 0;
			ackCounter = 0;
			sendCounter = 0;
			long startTime = System.currentTimeMillis();

			try {
				// Annahmevorbereitung
				readthread.start();
				// Versenden
				writethread.start();
				// Warten bis die Arbeiten erledigt sind
				readthread.join();
				writethread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Auswertung
			long endTime = System.currentTimeMillis();
			sendTime = endTime - startTime;

			aSocket.close();
			System.out.print("Clean end" + "\n");

			// Auswertung
			System.out.print("Gesantuebertragungszeit:" + sendTime + "\n");
			System.out.print("Timerablaeufe:" + finishedTimerCounter
					+ ", wiederholtes Senden:" + resendCounter + "\n");
			System.out.print("ACKs:" + ackCounter + ", gesendet:" + sendCounter
					+ "\n");
			System.out.print("RTT:" + timeoutValue + "\n");

		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		}

	}

	// Seperates Erstversenden vom Client zum Server
	private class SendThread extends Thread {

		public void run() {
			try {
				int n = 0;
				while (!isAllSent() && !isLast(n)) {
					// lock.lock();
					// Lese Paket der Datei ein
					FCpacket packet = filepuffer.get(n);
					// Paket gelesen
					// Lege das Paket in den Sendepuffer, wenn nicht voll
					while (isPufferFull()) {
						// System.out.print("Puffer full" + "\n");
						// notFull.await();
						// System.out.print("Send not wait" + "\n");
						sleep(1);
					}
					while (blocked) {
						sleep(1);
					}
					addToPuffer(packet);
					// Sende das Paket
					sendPacket(packet);
					// Startzeit
					long time = System.currentTimeMillis();
					packet.setTimestamp(time);
					// Starte Timer fuer das Paket
					startTimer(packet);
					// Erhoehe seqNum
					n = n + 1;
					// lock.unlock();
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Unexspected Error! " + e.toString());
				System.exit(-1);
			}
		}
	}

	// Seperate annahme der Antwort vom Server
	private class ReceivedThread extends Thread {

		public void run() {
			try {
				while (!isAllSent()) {
					// lock.lock();
					// ACK empfangen
					aSocket.receive(reply);
					byte[] rec = reply.getData();
					long recSeqNum = makeLong(rec, 0, 8);
					FCpacket packetInPuffer = filepuffer.get((int) recSeqNum);
					// Paket im Sendepuffer
					if (sendepuffer.contains(packetInPuffer)) {
						long time = System.currentTimeMillis();
						// Markiere als quittiert
						packetInPuffer.setValidACK(true);
						// Timer stoppen
						cancelTimer(packetInPuffer);
						// Timeout neu berechnen (RTT)
						computeTimeoutValue(time
								- packetInPuffer.getTimestamp());
						// Loesche Paket vom Puffer
						FCpacket firstPacket = sendepuffer.get(0);
						if (recSeqNum == firstPacket.getSeqNum()) {
							blocked = true;
							Thread.sleep(1);
							ArrayList<FCpacket> removePackets = new ArrayList<FCpacket>();
							Iterator<FCpacket> iterator = sendepuffer
									.iterator();
							while (iterator.hasNext()) {
								FCpacket packet = iterator.next();
								if ((packet != null) && packet.isValidACK()) {
									removePackets.add(packet);
								}
							}
							sendepuffer.removeAll(removePackets);
							blocked = false;
							// removeFromPuffer(packetInPuffer);
						}
					}
					// if (!isPufferFull()) {
					// System.out.print("Puffer not full" + "\n");
					// notFull.signal();
					// }
					// lock.unlock();

					// Auswertung
					ackCounter++;
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Unexspected Error! " + e.toString());
				System.exit(-1);
			}
		}
	}

	// Erstellt alle Pakete
	private void createAllPacket() {
		int packetCounter = 0;
		int packetMax = 1 + (int) Math.ceil(datei.length / (double) size);
		for (; packetCounter < packetMax; packetCounter++) {
			filepuffer.add(makeDataPacket(packetCounter));
		}
		System.out.print("Created " + packetMax + " packet with a lengh of "
				+ datei.length + "\n");
	}

	private void packetCheck() {
		int size = 0;
		byte[] packetData;
		for (FCpacket packet : filepuffer) {
			if (packet.getSeqNum() != 0) {
				packetData = packet.getData();
				size += packetData.length;
			}
		}
		byte[] combined = new byte[size];
		int pos = 0;
		for (FCpacket packet : filepuffer) {
			if (packet.getSeqNum() != 0) {
				packetData = packet.getData();
				System.arraycopy(packetData, 0, combined, pos,
						packetData.length);
			}
		}
		System.out.print("Test packet rebuild file with " + datei.length
				+ " | Result was " + combined.length + "\n");
	}

	// Prueft, ob alle Pakete angekommen sind
	private boolean isAllSent() {
		for (FCpacket packet : filepuffer) {
			if (!packet.isValidACK()) {
				return false;
			}
		}
		return true;
	}

	// Prueft, ob es das letzte Paket von der Datei ist
	private boolean isLast(int value) {
		return ((filepuffer.size()) == value);
	}

	// Macht ein Paket und merkt, ob es sich um das Steuerpaket handelt
	private FCpacket makeDataPacket(int value) {
		FCpacket packet;
		if (value == 0) {
			packet = makeControlPacket();
		} else {
			packet = makeFilePacket(value);
		}
		return packet;
	}

	// Macht aus einem Teilstueck der Datei ein FCpacket
	// Dateistuecke sind nur Ranges (0= 0-19, 1= 20-39, ...)
	private FCpacket makeFilePacket(int value) {
		FCpacket packet;
		byte[] packetData;
		int firstData;
		int lastData;
		firstData = (value - 1) * size;
		lastData = (value) * size;
		if (firstData >= datei.length) {
			firstData = datei.length;
		}
		if (lastData >= datei.length) {
			lastData = datei.length;
		}
		packetData = Arrays.copyOfRange(datei, firstData, lastData);
		packet = new FCpacket((long) value, packetData, packetData.length);

		return packet;
	}

	// Prueft, ob der Puffer voll ist
	private boolean isPufferFull() {
		return (sendepuffer.size() >= windowSize);
	}

	// Fuegt das Paket dem Puffer hinzu
	private void addToPuffer(FCpacket packet) {
		if (packet == null) {
			return;
		}
		// System.out.print("Packet seqNum (add):" + packet.getSeqNum() + "\n");
		sendepuffer.add(packet);
	}

	private void removeFromPuffer(FCpacket packet) {
		if (packet == null) {
			return;
		}
		// System.out.print("Packet seqNum (rem):" + packet.getSeqNum() + "\n");
		sendepuffer.remove(packet);
	}

	// Versendet das Paket (UDP)
	private void sendPacket(FCpacket packet) throws IOException {
		if (packet == null) {
			return;
		}
		byte[] packetTrans;
		packetTrans = packet.getSeqNumBytesAndData();
		request = new DatagramPacket(packetTrans, packetTrans.length, aHost,
				SERVER_PORT);
		System.out.print("Packet seqNum (send):" + packet.getSeqNum()
				+ " | Packet size:" + packet.getData().length
				+ " | Current timeout:" + timeoutValue + "\n");
		aSocket.send(request);

		// Auswertung
		sendCounter++;
	}

	// byte to long for seqNum
	private long makeLong(byte[] buf, int i, int length) {
		long r = 0;
		length += i;

		for (int j = i; j < length; j++)
			r = (r << 8) | (buf[j] & 0xffL);

		return r;
	}

	/**
	 * 
	 * Timer Operations
	 */
	public void startTimer(FCpacket packet) {
		if (packet == null) {
			return;
		}
		/* Create, save and start timer for the given FCpacket */
		FC_Timer timer = new FC_Timer(timeoutValue, this, packet.getSeqNum());
		packet.setTimer(timer);
		timer.start();
	}

	public void cancelTimer(FCpacket packet) {
		if (packet == null) {
			return;
		}
		/* Cancel timer for the given FCpacket */
		testOut("Cancel Timer for packet" + packet.getSeqNum());

		if (packet.getTimer() != null) {
			packet.getTimer().interrupt();
		}
	}

	/**
	 * Implementation specific task performed at timeout
	 * 
	 * @throws IOException
	 */
	public void timeoutTask(long seqNum) {
		try {
			FCpacket packet = filepuffer.get((int) seqNum);
			if (!packet.isValidACK()) {
				// Sende das Paket
				sendPacket(packet);

				// Auswertung
				resendCounter++;

				// timeoutValue = 2 * timeoutValue;
				// Startzeit
				long time = System.currentTimeMillis();
				packet.setTimestamp(time);
				// Starte Timer fuer das Paket
				startTimer(packet);
			}

			// Auswertung
			finishedTimerCounter++;

		} catch (IOException e) {
			// System.out.print(""+"\n");
		}
	}

	/**
	 * 
	 * Computes the current timeout value (in nanoseconds)
	 */
	public void computeTimeoutValue(long sampleRTT) {
		// double x = 0.25;
		// double y = x / 2;
		// double RTT = timeoutValue;
		// double expRTT = ((1 - y) * sampleRTT) + (y * RTT);
		// double Jitter = ((1 - x) * Jitter) + (x * Math.abs(RTT - expRTT));
		// timeoutValue = (long) (expRTT + (4 * Jitter));

		timeoutValue = (long) ((0.8 * sampleRTT) + (0.2 * timeoutValue));
	}

	/**
	 * 
	 * Return value: FCPacket with (0 destPath;windowSize;errorRate)
	 */
	public FCpacket makeControlPacket() {
		/*
		 * Create first packet with seq num 0. Return value: FCPacket with (0
		 * destPath ; windowSize ; errorRate)
		 */
		String sendString = destPath + ";" + windowSize + ";" + serverErrorRate;
		byte[] sendData = null;
		try {
			sendData = sendString.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return new FCpacket(0, sendData, sendData.length);
	}

	public void testOut(String out) {
		if (TEST_OUTPUT_MODE) {
			System.err.printf("%,d %s: %s\n", System.nanoTime(), Thread
					.currentThread().getName(), out);
		}
	}

	public static void main(String argx[]) throws Exception {
		String serverArg = "localhost";
		String sourcePathArg = "C:\\Users\\Ch4mP\\Desktop\\FCdata.pdf";
		String destPathArg = "C:\\Users\\Ch4mP\\Desktop\\FCdata2.pdf";
		String windowSizeArg = "1";
		String errorRateArg = "10";
		String[] argv = { serverArg, sourcePathArg, destPathArg, windowSizeArg,
				errorRateArg };
		FileCopyClient myClient = new FileCopyClient(argv[0], argv[1], argv[2],
				argv[3], argv[4]);
		myClient.runFileCopyClient();
	}

}
