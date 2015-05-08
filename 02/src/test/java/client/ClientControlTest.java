package client;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import helpers.PopState;
import org.testng.annotations.*;
import static org.testng.Assert.*;

import java.io.IOException;

public class ClientControlTest {
    private final static String UNICODE = "✉";
    private final static String PASSWORD = "pa$$w0rd" + UNICODE;

    public final GreenMail greenMail = new GreenMail(ServerSetupTest.POP3);
    public final int port;
    private ClientControl client;

    public ClientControlTest() {
        assertNotNull(greenMail);
        greenMail.start();

        // Create a test User
        greenMail.setUser("test", PASSWORD);

        port = greenMail.getPop3().getPort();
        assertTrue(port != 0, "TestServer wurde nicht gestartet.");
    }

    @AfterTest
    public void terdown() {
        greenMail.stop();
    }

    @Test(timeOut = 1000)
    public void authenticateSuccess() throws IOException {
        ClientModel client = new ClientModel("127.0.0.1", port, "test", PASSWORD);
        client.verbindungAufbauen();
        client.run(PopState.AUTHORIZED);
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
