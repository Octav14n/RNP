package client;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Created by octavian on 04.05.15.
 */
public class ClientControlTest {
    private final static String UNICODE = "✉";
    private final static String PASSWORD = "pa$$w0rd" + UNICODE;

    public final GreenMail greenMail = new GreenMail(ServerSetupTest.POP3);
    public final int port;
    private ClientControl client;

    public ClientControlTest() {
        assertNotNull(greenMail);
        greenMail.start();
        greenMail.setUser("test", PASSWORD);
        //ServerSetup server = new ServerSetup(50000, "127.0.0.1", "pop3");
        port = greenMail.getPop3().getPort();
        assertTrue("TestServer wurde nicht gestartet.", port != 0);
        System.out.println("setup complete");

        //client = new ClientControl(new ClientModel("127.0.0.1", port, "test", PASSWORD));


    }

    @After
    public void terdown() {
        greenMail.stop();
    }

    @Test
    public void authenticateSuccess() throws IOException {
        ClientModel client = new ClientModel("127.0.0.1", port, "test", PASSWORD);
        client.verbindungAufbauen();
        client.run();
    }

    //@Test
    public void retrieveOneSuccess() {
        client.retrieveMessage();
        assertEquals(1, greenMail.getReceivedMessages().length);
    }

    /** Setup some E-Mails for fetching later. */
    private void deliverEmails() {
        GreenMailUtil.sendTextEmailTest(
                "test@127.0.0.1",
                "simon.kosch@haw-hamburg.de",
                "Test Nachricht. " + UNICODE,
                "Dies ist eine Testnachricht\n" +
                        "Diese Nachricht zerstört sich selber\n\n in..." +
                        UNICODE +
                        "Kurzer Zeit.\n"
        );
        GreenMailUtil.sendTextEmailTest(
                "test@127.0.0.1",
                "simon.kosch@haw-hamburg.de",
                "Test Nachricht 2. " + UNICODE,
                "Dies ist eine Testnachricht 2\n" +
                        "Diese Nachricht zerstört sich selber\n\n in..." +
                        UNICODE +
                        "Kurzer Zeit.\n"
        );
    }
}
