package server;

import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
        ServerModel.setMaxVerbindung(3);
		ServerModel serverModel = new ServerModel();
		ServerControl serverControl = new ServerControl(serverModel);
	}

}
