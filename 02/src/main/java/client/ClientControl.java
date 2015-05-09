package client;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public class ClientControl implements Runnable {
    public static long interval = 1000;

    @Getter @Setter(AccessLevel.PRIVATE)
    public boolean shallEnd = false;
	@Getter @Setter(AccessLevel.PRIVATE)
	private IClientModel clientModel;
    private Thread thread;


	public ClientControl(IClientModel clientModel) {
		setClientModel(clientModel);
        thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
	}

    public void end() throws InterruptedException {
        setShallEnd(true);
        thread.interrupt();
        thread.join(100);
    }

    public boolean isEnded() {
        return !thread.isAlive();
    }

	public void run() {
        while (!isShallEnd()) {
            try {
                clientModel.verbindungAufbauen();
                clientModel.run();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                // Shall we die now?
                if (!isShallEnd())
                    e.printStackTrace();
            }
        }
    }

}
