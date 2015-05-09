package client;

import helpers.PopState;
import org.testng.annotations.*;

import java.io.IOException;

import static org.testng.Assert.*;

public class ClientTest {
    private ClientControl control;

    @Test
    public void runSuccess() throws Exception {
        control = new ClientControl(new IClientModel() {
            @Override
            public String getUsername() {
                return null;
            }

            @Override
            public int getMailCount() {
                return 0;
            }

            @Override
            public String getFilePrefix() {
                return null;
            }

            @Override
            public void verbindungAufbauen() throws IOException {

            }

            @Override
            public void run() {

            }

            @Override
            public void run(PopState tillState) {

            }

            @Override
            public boolean istVerbunden() {
                return false;
            }
        });
    }

    @Test(dependsOnMethods = "runSuccess")
    public void endSuccess() throws Exception {
        control.end();
        assertTrue(control.isEnded());
    }
}
