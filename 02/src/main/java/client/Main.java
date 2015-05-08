package client;

public class Main {

	public static void main(String[] args) {
		ClientModel clientModel = new ClientModel("username", "password");
		ClientControl clientControl = new ClientControl(clientModel);
	}

}
