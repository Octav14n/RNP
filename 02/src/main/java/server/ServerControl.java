package server;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

public class ServerControl {

    @Getter @Setter(AccessLevel.PRIVATE)
	private ServerModel serverModel;

	public ServerControl(ServerModel serverModel) throws IOException {
		setServerModel(serverModel);
		serverModel.clientsAnnehmen();
	}
}
