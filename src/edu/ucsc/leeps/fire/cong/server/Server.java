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
    private PeriodConfig currentPeriodConfig;

    public Server() {
        super();
        currentPeriodConfig = new PeriodConfig();
        currentPeriodConfig.setInitialStrategy(0.5f);
        currentPeriodConfig.setNumber(1);
        currentPeriodConfig.setTimeConstrained(true);
        currentPeriodConfig.setLength(60);
        _currentPeriodConfig = currentPeriodConfig;
    }

    public void setStrategy(String name, float strategy) {
        clients.get(name).setStrategy(strategy);
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        Server.start(server, ClientInterface.class);
    }

    public boolean readyToStart() {
        return clients.size() >= 1;
    }

    public void setClients(Map<String, edu.ucsc.leeps.fire.client.ClientInterface> _clients) {
        clients = new HashMap<String, ClientInterface>();
        for (String name : _clients.keySet()) {
            ClientInterface client = (ClientInterface) _clients.get(name);
            clients.put(name, client);
        }
    }
}
