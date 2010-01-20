package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class Server extends edu.ucsc.leeps.fire.server.Server implements ServerInterface {

    private Map<String, ClientInterface> clients;

    public Server() {
        super(PeriodConfig.class);
        clients = new HashMap<String, ClientInterface>();
    }

    public void setStrategy(String name, float strategy) {
        clients.get(name).setStrategy(strategy);
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        String serverHost = null;
        String clientHost = null;
        if (args.length == 2) {
            serverHost = args[0];
            clientHost = args[1];
        }
        Server.start(serverHost, clientHost, server);
    }

    public boolean readyToStart() {
        return clients.size() >= 1;
    }

    public void setClients(Map<String, edu.ucsc.leeps.fire.client.ClientInterface> _clients) {
        clients.clear();
        for (String name : _clients.keySet()) {
            ClientInterface client = (ClientInterface) _clients.get(name);
            clients.put(name, client);
        }
    }
}
