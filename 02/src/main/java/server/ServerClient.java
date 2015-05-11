package server;

import client.ClientModel;
import com.sun.mail.iap.BadCommandException;
import helpers.PopState;
import helpers.UTF8Util;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import javax.mail.AuthenticationFailedException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

public class ServerClient implements Runnable {
    private final ServerModel server;
    private final Socket socket;
    @Getter(AccessLevel.PRIVATE) @Setter(AccessLevel.PRIVATE)
    private PopState state;
    @Getter @Setter(AccessLevel.PRIVATE)
    private String filePrefix;
    @Getter @Setter(AccessLevel.PRIVATE)
    private String username;
    @Getter
    private Collection<Integer> toDelete = new ArrayList<>();

    public ServerClient(ServerModel server, Socket socket) {
        this.socket = socket;
        this.server = server;
        setState(PopState.CONNECTED);
        Thread thread = new Thread(this);
        thread.run();
    }

    @Override
    public void run() {
        System.out.print("Verbindung hergestellt" + "\n");
        try {
            UTF8Util.schreibeOK(socket.getOutputStream(), "Hello, Welcome in my net.");
            boolean stopp = false;
            while (!stopp && !socket.isClosed()) {
                // Lese die eingehenden Daten.
                String text;
                try {
                    text = UTF8Util.lese(socket.getInputStream());
                    if (!text.equals("")) {
                        System.out.print("eingang: " + text + "");
                        // Ermittle Antwort der Eingabe.
                        try {
                            String antwort = antwort(text);
                            System.out.print("ausgang: " + antwort + "");
                            // Die Antwort zum Client schicken.
                            UTF8Util.schreibeOK(socket.getOutputStream(), antwort);

                            if (antwort.equals("BYE")) {
                                socket.close();
                                stopp = true;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (socket.isOutputShutdown())
                                stopp = true;
                            else
                                UTF8Util.schreibeERR(socket.getOutputStream(), e.toString());
                        }
                    }
                } catch (TimeoutException e) {
                    e.printStackTrace();
                    stopp = true;
                }
            }
            socket.close();
            server.removeClient(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(Integer msgID : toDelete) {
            File file = new File(getFilePrefix() + msgID);
            if (file.exists() && file.canWrite())
                file.delete();
        }
        System.out.print("Verbindung getrennt" + "\n");
    }

    private String antwort(String text) throws Exception {
        String antwort = "";
        String[] woerter = text.split(" ");
        if (woerter.length > 0) {
            switch (woerter[0]) {
                case "USER":
                    if (getState() != PopState.CONNECTED)
                        throw new BadCommandException();
                    if (woerter.length < 2)
                        throw new IllegalArgumentException("USER needs one argument.");
                    if (woerter[1].indexOf('@') == -1)
                        throw new IllegalArgumentException("USER name needs one at.");
                    if (woerter[1].indexOf('/') != -1 || woerter[1].indexOf('\\') != -1)
                        throw new IllegalArgumentException("USER name is not valid.");

                    antwort = "now use PASS to authenticate yourself.";
                    setState(PopState.USERNAME_SEND);
                    String username = woerter[1].substring(0, woerter[1].indexOf('@'));
                    String server = woerter[1].substring(woerter[1].indexOf('@') + 1);
                    setUsername(woerter[1]);
                    setFilePrefix(ClientModel.DIR_PREFIX + server + "/" + username + "/");
                    break;
                case "PASS":
                    if (getState() != PopState.USERNAME_SEND)
                        throw new BadCommandException();
                    if (woerter.length < 2)
                        throw new IllegalArgumentException("PASS needs one argument.");
                    if (!woerter[1].equals(ServerModel.getCredentials().get(getUsername())))
                        throw new AuthenticationFailedException();

                    setState(PopState.TRANSACTION);
                    break;
                case "STAT":
                    if (getState() != PopState.TRANSACTION)
                        throw new BadCommandException();

                    File dirStat = new File(getFilePrefix());
                    if (dirStat.listFiles() == null)
                        antwort = "0 -1";
                    else
                        antwort = (dirStat.listFiles().length - getToDelete().size()) + " -1";
                    break;
                case "LIST":
                    if (getState() != PopState.TRANSACTION)
                        throw new BadCommandException();

                    if (woerter.length == 1) {
                        // List all messages.
                        File dirList = new File(getFilePrefix());
                        if (dirList.listFiles() != null) {
                            for (File mail : dirList.listFiles()) {
                                if (getToDelete().contains(Integer.parseInt(mail.getName())))
                                    continue; // Don't print deleted messages.
                                antwort += "\r\n" + mail.getName() + " " + mail.length();
                            }
                        }
                        antwort += "\r\n";
                    } else {
                        // List one message.
                        if (getToDelete().contains(Integer.parseInt(woerter[1])))
                            throw new IllegalArgumentException("File is marked for deletion.");

                        File mail = new File(getFilePrefix() + Integer.parseInt(woerter[1]));
                        if (!mail.exists())
                            throw new IllegalArgumentException("The requested message does not exist.");

                        antwort += "\r\n" + mail.getName() + " " + mail.length();
                    }
                    break;
                case "RETR":
                    if (getState() != PopState.TRANSACTION)
                        throw new BadCommandException();

                    if (woerter.length < 2)
                        throw new IllegalArgumentException("RETR needs one argument");
                    if (getToDelete().contains(Integer.parseInt(woerter[1])))
                        throw new IllegalArgumentException("File is marked for deletion.");

                    File mailRetr = new File(getFilePrefix() + Integer.parseInt(woerter[1]));
                    if (!mailRetr.exists())
                        throw new IllegalArgumentException("The requested message does not exist.");

                    BufferedReader reader = new BufferedReader(new FileReader(mailRetr));
                    antwort = "\r\n";
                    int readed = 0;
                    char b[] = new char[1024];

                    while ((readed = reader.read(b)) != -1) {
                        antwort += String.valueOf(b, 0, readed);
                    }
                    break;
                case "DELE":
                    if (getState() != PopState.TRANSACTION)
                        throw new BadCommandException();

                    if (woerter.length < 2)
                        throw new IllegalArgumentException("DELE needs one argument");

                    File mailDele = new File(getFilePrefix() + Integer.parseInt(woerter[1]));
                    if (!mailDele.exists())
                        throw new IllegalArgumentException("The requested message does not exist.");

                    getToDelete().add(Integer.parseInt(mailDele.getName()));
                    break;
                case "RSET":
                    if (getState() != PopState.TRANSACTION)
                        throw new BadCommandException();

                    getToDelete().clear();
                    break;
                case "UIDL":
                    if (getState() != PopState.TRANSACTION)
                        throw new BadCommandException();

                    break;
                case "NOOP":
                    if (getState() != PopState.TRANSACTION)
                        throw new BadCommandException();

                    antwort = "";
                    break;
                case "QUIT":
                    antwort = "BYE";
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("Operation '%s' is not supported.", woerter[0]));
            }
        }
        return antwort;
    }
}
