package client;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.Test;

import javax.mail.internet.MimeMessage;

/**
 * Created by octavian on 04.05.15.
 */
public class ClientControlTest extends TestCase {
    private final static String UNICODE = "✉";
    private final static String PASSWORD = "pa$$w0rd" + UNICODE;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.POP3);
    private ClientControl client;
    private final GreenMailUser USER_TEST;

    public ClientControlTest() {
        USER_TEST = greenMail.setUser("test", PASSWORD);
        int port = greenMail.getPop3().getPort();

        client = new ClientControl(new ClientModel("127.0.0.1", port, "test", PASSWORD));


    }

    @Test
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
