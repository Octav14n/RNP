public class Main {

	public static void main(String[] args) {
		ClientModel clientModel = new ClientModel();
		ClientView clientView = new ClientView();
		ClientControl clientControl = new ClientControl(clientModel, clientView);
	}

}
