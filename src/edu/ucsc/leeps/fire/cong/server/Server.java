package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class Server extends edu.ucsc.leeps.fire.server.Server implements ServerInterface {

    Map<String, ClientInterface> clients;

    public Server() {
        super();
        clients = new HashMap<String, ClientInterface>();
    }

    public void setStrategy(String name, float strategy) {
        clients.get(name).setStrategy(strategy);
    }

    @Override
    public void register(String name, edu.ucsc.leeps.fire.client.ClientInterface client) {
        super.register(name, client);
        clients.put(name, (ClientInterface)getClient(name));
        if (clients.size() >= 1) {
            for (ClientInterface c : clients.values()) {
                c.startTicking(60);
            }
        }
    }

    @Override
    public void unregister(String name) {
        super.unregister(name);
        clients.remove(name);
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        Server.start(server, ClientInterface.class);
    }
}