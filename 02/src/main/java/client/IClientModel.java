package client;

import helpers.PopState;

import java.io.IOException;

public interface IClientModel {
    String getUsername();
    int getMailCount();
    String getFilePrefix();

    void verbindungAufbauen() throws IOException;

    void run();

    /**
     * Runs the finite state machine.
     *
     * @param tillState (Testing only) determines at which state the function returns.
     */
    void run(PopState tillState);

    boolean istVerbunden();
}
