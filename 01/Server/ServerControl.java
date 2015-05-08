import java.io.IOException;
import java.net.ServerSocket;

public class ServerControl {

	private ServerModel serverModel;

	public ServerControl(ServerModel serverModel) {
		setServerModel(serverModel);
		standard();
	}
	
	private void standard(){
		getServerModel().serverSocketStart();
		getServerModel().clientsAnnehmen();
	}

	public ServerModel getServerModel() {
		return serverModel;
	}

	private void setServerModel(ServerModel serverModel) {
		this.serverModel = serverModel;
	}
}
