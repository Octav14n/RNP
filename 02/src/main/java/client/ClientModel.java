package client;

import helpers.PopState;
import helpers.UTF8Util;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.Socket;

public class ClientModel implements Runnable {

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private String ip;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private int port;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Socket socket;
    @Getter
    @Setter(AccessLevel.PRIVATE)
    private String username;
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PROTECTED)
    private String password;
    @Getter(AccessLevel.PROTECTED) //@Setter(AccessLevel.PROTECTED)
    private PopState state = PopState.DISCONECTED;
    @Getter(AccessLevel.PUBLIC)
    @Setter(AccessLevel.PRIVATE)
    private int mail_count;

    @Override
    public void run() {
        run(PopState.EXPECTING_EXIT);
    }

    /**
     * Runs the finite state machine.
     *
     * @param tillState (Testing only) determines at which state the function returns.
     */
    public void run(PopState tillState) {
        if (PopState.DISCONECTED == getState() || PopState.EXPECTING_EXIT == getState())
            throw new RuntimeException("Can't start Client with state: " + getState().toString());
        while (PopState.EXPECTING_EXIT != getState() && getState() != tillState) {
            try {
                switch (getState()) {
                    case CONNECTED:
                        authenticate_username();
                        break;
                    case USERNAME_SEND:
                        authenticate_password();
                        break;
                    case PASSWORD_SEND:
                        authenticate_final();
                        break;
                    case TRANSACTION:
                        mail_check();
                        break;
                    case MAIL_AVAILABLE:
                        mail_fetch();
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    quit();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                setState(PopState.EXPECTING_EXIT);
            }
        }
        if (getState() == PopState.EXPECTING_EXIT) {
            try {
                verbindungTrennen();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void mail_fetch() throws Exception {
        UTF8Util.schreibe(socket.getOutputStream(), "LIST");
        UTF8Util.leseAssertOK(socket.getInputStream(), "List did not work.");
        String antwort = UTF8Util.lese(socket.getInputStream());
        assert antwort.indexOf(' ') != -1;

        int mID = Integer.parseInt(antwort.substring(0, antwort.indexOf(' ')));

        UTF8Util.schreibe(socket.getOutputStream(), "RETR " + mID);
        UTF8Util.leseAssertOK(socket.getInputStream(), "RETR " + mID + " did not work.");
        String msg = UTF8Util.lese(socket.getInputStream());

        UTF8Util.schreibe(socket.getOutputStream(), "DELE " + mID);
        UTF8Util.leseAssertOK(socket.getInputStream(), "DELE " + mID + " did not work.");

        System.out.println(String.format("Msg retrieved:\n'%s' mail_count: %d", msg, getMail_count()));

        setMail_count(getMail_count() - 1);
        if (getMail_count() == 0) {
            setState(PopState.TRANSACTION);
        }
    }

    private void mail_check() throws Exception {
        UTF8Util.schreibe(socket.getOutputStream(), "STAT");
        String antwort = UTF8Util.leseAssertOK(socket.getInputStream(), "Stat did not work.");
        assert antwort.indexOf(' ') != -1;

        setMail_count(Integer.parseInt(antwort.substring(0, antwort.indexOf(' '))));
        if (mail_count > 0) {
            setState(PopState.MAIL_AVAILABLE);
        }
    }

    protected void setState(PopState state) {
        System.out.println(String.format("Switch state from: %s to: %s", getState().toString(), state.toString()));
        this.state = state;
    }

    private void quit() throws Exception {
        UTF8Util.schreibe(socket.getOutputStream(), "QUIT");
        UTF8Util.leseAssertOK(socket.getInputStream(), "Quit did not work.");
        setState(PopState.EXPECTING_EXIT);
    }

    private void authenticate_final() throws Exception {
        UTF8Util.leseAssertOK(socket.getInputStream(), "Username or Password not accepted: '%s'");
        setState(PopState.TRANSACTION);
    }

    private void authenticate_username() throws Exception {
        UTF8Util.leseAssertOK(socket.getInputStream(), "Server does not greet: '%s'");

        UTF8Util.schreibe(socket.getOutputStream(), "USER " + username);
        setState(PopState.USERNAME_SEND);
    }

    private void authenticate_password() throws Exception {
        UTF8Util.leseAssertOK(socket.getInputStream(), "Username not accepted: '%s'");

        UTF8Util.schreibe(socket.getOutputStream(), "PASS " + password);
        setState(PopState.PASSWORD_SEND);
    }

    public ClientModel(String username, String password) {
        this("127.0.0.1", 50000, username, password);
    }

    public ClientModel(String ip, int port, String username, String password) {
        setIp(ip);
        setPort(port);
        setUsername(username);
        setPassword(password);
        System.out.print("127.0.0.1" + " -> " + getIp()
                + ":" + getPort() + " (Client -> Server)." + "\n");
    }

    public void verbindungAufbauen() throws IOException {
        // Es wird versucht eine Verbindung zum Server herzustellen.
        if (!istVerbunden()) {
            setSocket(new Socket(getIp(), getPort()));
            setState(PopState.CONNECTED);
        }
    }

    public void verbindungTrennen() throws IOException {
        // Es wird versucht eine Verbindung zum Server trennen.
        if (istVerbunden()) {
            getSocket().close();
            setSocket(null);
            setState(PopState.DISCONECTED);
        }
    }

    public boolean istVerbunden() {
        return !(
                (getState() == PopState.DISCONECTED) || (getSocket() == null) || getSocket().isClosed()
        ) && getSocket().isConnected();
    }
}
