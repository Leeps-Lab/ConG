package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class Server extends edu.ucsc.leeps.fire.server.Server implements ServerInterface {

    private Map<String, ClientInterface> clients;
    private PeriodConfig periodConfig;

    public Server() {
        super(PeriodConfig.class);
        clients = new HashMap<String, ClientInterface>();
    }

    public void strategyChanged(String name) {
        ClientInterface client = clients.get(name);
        client.setStrategy(
                periodConfig.strategySetGenerator.getStrategy(
                client,
                new LinkedList<ClientInterface>(clients.values())));
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        String serverHost = null;
        String clientHost = null;
        String configPath = null;
        if (args.length == 3) {
            serverHost = args[0];
            clientHost = args[1];
            configPath = args[2];
        }
        Server.start(serverHost, clientHost, configPath, server);
    }

    public boolean readyToStart() {
        return clients.size() >= 1;
    }

    public void setClients(Map<String, edu.ucsc.leeps.fire.client.ClientInterface> superClients) {
        clients.clear();
        for (String name : superClients.keySet()) {
            ClientInterface client = (ClientInterface) superClients.get(name);
            clients.put(name, client);
        }
    }

    public void setPeriodConfig(edu.ucsc.leeps.fire.server.PeriodConfig _periodConfig) {
        periodConfig = (PeriodConfig) _periodConfig;
        periodConfig.payoffFunction = new HomotopyPayoffFunction();
        periodConfig.strategySetGenerator = new PopulationIncludeGenerator();
    }
}
