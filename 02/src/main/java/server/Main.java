package server;

public class Main {

	public static void main(String[] args) {
		ServerModel serverModel = new ServerModel();
		ServerControl serverControl = new ServerControl(serverModel);
	}

}
