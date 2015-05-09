package client;

import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import helpers.PopState;

import org.testng.annotations.*;
import static org.testng.Assert.*;

import javax.mail.*;
import javax.mail.internet.MimeMessage;

import java.io.IOException;
import java.util.Properties;

public class ClientModelTest {
    private final static String UNICODE = "";//"✉";
    private final static String PASSWORD = "pa$$w0rd" + UNICODE;
    private final static String HOST = "localhost";

    public final GreenMail greenMail = new GreenMail(ServerSetupTest.POP3);
    private final GreenMailUser TEST_USER;
    public final int port;
    private ClientModel client;

    public ClientModelTest() {
        assertNotNull(greenMail);
        greenMail.start();

        // Create a test User
        TEST_USER = greenMail.setUser("test", PASSWORD);

        port = greenMail.getPop3().getPort();
        assertTrue(port != 0, "TestServer wurde nicht gestartet.");
    }

    @AfterTest
    public void terdown() {
        greenMail.stop();
    }

    @Test(timeOut = 1000)
    public void authenticateSuccess() throws IOException {
        client = new ClientModel(HOST, port, TEST_USER.getLogin(), TEST_USER.getPassword());
        client.verbindungAufbauen();
        client.run(PopState.TRANSACTION);

        assertEquals(client.getMailCount(), 0);
        assertEquals(client.getState(), PopState.TRANSACTION);
    }

    @Test(timeOut = 2000, dependsOnMethods = "authenticateSuccess")
    public void retrieveSuccess() throws MessagingException {
        deliverEmails();

        client.run(PopState.MAIL_AVAILABLE);
        assertEquals(client.getMailCount(), 2);
        assertEquals(client.getState(), PopState.MAIL_AVAILABLE);
        client.run();
        assertEquals(client.getState(), PopState.DISCONECTED);
        assertEquals(client.getMailCount(), 0);
        assertEquals(getMessageCount(TEST_USER), 0);
    }

    private int getMessageCount(GreenMailUser user) throws MessagingException {
        Properties properties = new Properties();
        Session session = Session.getInstance(properties);
        System.out.println(user.getLogin());
        System.out.println(user.getPassword());
        javax.mail.Store store = session.getStore(new URLName(
                "pop3",
                HOST,
                port,
                null,
                user.getLogin(),
                user.getPassword()
        ));
        store.connect();

        Folder folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        int count = folder.getMessageCount();
        folder.close(true);
        return count;
    }

    /** Setup some E-Mails for fetching later. */
    private void deliverEmails() throws MessagingException {
        MimeMessage msg = new MimeMessage((Session) null);
        msg.addRecipients(Message.RecipientType.TO, "test@127.0.0.1");
        msg.setFrom("simon.kosch@haw-hamburg.de");
        msg.setSubject("Test Nachricht. " + UNICODE);
        msg.setText("Dies ist eine Testnachricht\n" +
                "Diese Nachricht zerstört sich selber\n\r\n in..." +
                UNICODE +
                "Kurzer Zeit.\n");
        TEST_USER.deliver(msg);

        msg = new MimeMessage((Session) null);
        msg.addRecipients(Message.RecipientType.TO, "test@127.0.0.1");
        msg.setFrom("simon.kosch@haw-hamburg.de");
        msg.setSubject("Test Nachricht 2. " + UNICODE);
        msg.setText("Dies ist eine Testnachricht 2\n" +
                "Diese Nachricht zerstört sich selber\r\n..\r\n in..." +
                UNICODE +
                "Kurzer Zeit.\n");
        TEST_USER.deliver(msg);
    }
}
