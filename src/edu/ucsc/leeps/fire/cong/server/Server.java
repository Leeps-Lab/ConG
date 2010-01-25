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
        if (periodConfig.twoStrategyPayoffFunction != null) {
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
        } else if (periodConfig.RPSDPayoffFunction != null) {
            float r, p, s, d;
            r = p = s = d = 0;
            for (ClientInterface client : clients.values()) {
                float[] strategy = client.getStrategyRPSD();
                r += strategy[0];
                p += strategy[1];
                s += strategy[2];
                d += strategy[3];
            }
            r /= clients.size();
            p /= clients.size();
            s /= clients.size();
            d /= clients.size();
            for (ClientInterface client : clients.values()) {
                float[] strategy = client.getStrategyRPSD();
                client.setStrategyRPSD(
                        strategy[0], strategy[1], strategy[2], strategy[3],
                        r, p, s, d);
            }
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
        periodConfig.twoStrategyPayoffFunction = new HomotopyPayoffFunction();
        periodConfig.twoStrategyPayoffFunction = null;
        periodConfig.RPSDPayoffFunction = new RPSDPayoffFunction();
    }
}
