package client;

import helpers.UTF8Util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class Main {
    static Collection<ClientControl> clientControls = new ArrayList<>();

	public static void main(String[] args) throws IOException {
        if (args.length > 0)
            UTF8Util.timeout = Integer.parseInt(args[0]);
        else
            UTF8Util.timeout = 5000;

        FileReader fileReader = new FileReader("client.cfg");
        BufferedReader reader = new BufferedReader(fileReader);
        String refresh = reader.readLine();
        ClientControl.interval = Integer.parseInt(refresh);

        String server;
        while (!(server = reader.readLine()).equals("")){
            int port = Integer.parseInt(reader.readLine());
            String user = reader.readLine();
            String pass = reader.readLine();
            ClientModel clientModel = new ClientModel(server, port, user, pass);
            clientControls.add(new ClientControl(clientModel));
        }
	}

}
