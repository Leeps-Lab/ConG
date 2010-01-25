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
    private PeriodConfig periodConfig;

    public Server() {
        super(PeriodConfig.class);
        clients = new HashMap<String, ClientInterface>();
    }

    @Override
    public void strategyChanged(String name) {
        float populationAverage = 0;
        for (ClientInterface client : clients.values()) {
            float[] strategy = client.getStrategyAB();
            populationAverage += strategy[0];
        }
        populationAverage /= clients.size();
        for (ClientInterface client : clients.values()) {
            float[] strategy = client.getStrategyAB();
            client.setStrategyAB(
                    strategy[0], strategy[1],
                    populationAverage, 1 - populationAverage);
        }
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

    @Override
    public boolean readyToStart() {
        return clients.size() >= 1;
    }

    @Override
    public void setClients(Map<String, edu.ucsc.leeps.fire.client.ClientInterface> superClients) {
        clients.clear();
        for (String name : superClients.keySet()) {
            ClientInterface client = (ClientInterface) superClients.get(name);
            clients.put(name, client);
        }
    }

    @Override
    public void setPeriodConfig(edu.ucsc.leeps.fire.server.PeriodConfig superPeriodConfig) {
        periodConfig = (PeriodConfig) superPeriodConfig;
        periodConfig.payoffFunction = new HomotopyPayoffFunction();
    }
}
