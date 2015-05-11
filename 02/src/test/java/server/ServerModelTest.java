package server;

import javax.mail.*;
import java.io.IOException;
import java.util.Properties;

import org.testng.annotations.*;
import static org.testng.Assert.*;

/**
 * Created by octavian on 11.05.15.
 */
public class ServerModelTest {
    private static final int PORT = 50000;
    private static final String HOST = "localhost";
    private static final String USERNAME = "test@localhost";
    private static final String PASSWORD = "pa$$w0rd";
    Folder folder;


    public ServerModelTest() throws IOException {
        ServerModel.initAccounts();
        ServerModel.setMaxVerbindung(3);
    }

    @AfterTest
    public void tearDown() throws MessagingException {
        folder.close(true);
    }

    @Test(timeOut = 2000)
    public void statSuccess() throws IOException, MessagingException {
        ServerModel model = new ServerModel();
        model.clientsAnnehmen();

        Properties properties = new Properties();
        Session session = Session.getInstance(properties);
        javax.mail.Store store = session.getStore(new URLName(
                "pop3",
                HOST,
                PORT,
                null,
                USERNAME,
                PASSWORD
        ));
        store.connect();

        folder = store.getFolder("INBOX");
        folder.open(Folder.READ_ONLY);
        int count = folder.getMessageCount();

        assertEquals(count, 2);
    }

    @Test(timeOut = 2000, dependsOnMethods = "statSuccess")
    public void listSuccess() throws MessagingException {
        Message message = folder.getMessage(1);
        assertEquals(message.getSubject(), "Test Nachricht.");
    }
}
