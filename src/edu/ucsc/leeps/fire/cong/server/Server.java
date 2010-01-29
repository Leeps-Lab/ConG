package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class Server extends edu.ucsc.leeps.fire.server.Server implements ServerInterface {

    private Map<String, ClientInterface> clients;
    private PeriodConfig periodConfig;
    private List<Population> populations;
    private Map<String, Population> populationMap;
    private long periodStartTime;
    private Map<ClientInterface, Long> lastStrategyChangeTimes;
    private Map<ClientInterface, float[]> lastStrategies;

    public Server() {
        super(PeriodConfig.class);
        clients = new HashMap<String, ClientInterface>();
    }

    private void doPopulationIncludeStrategyChanged(String name, long timestamp) {
        Population population = populationMap.get(name);
        // update clients with payoff information for last strategy
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (periodConfig.length * 1000f);
        long inStrategyTime = System.currentTimeMillis() - population.lastEvalTime;
        float percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
        for (ClientInterface client : population.members) {
            if (periodConfig.twoStrategyPayoffFunction != null) {
                float[] last = lastStrategies.get(client);
                float points = periodConfig.twoStrategyPayoffFunction.getPayoff(
                        percent,
                        last[0], 1 - last[0],
                        population.averageStrategy_a, 1 - population.averageStrategy_a);
                if (!periodConfig.pointsPerSecond) {
                    points *= percentInStrategyTime;
                } else {
                    points *= inStrategyTime / 1000f;
                }
                client.addToPeriodPoints(points);
            } else if (periodConfig.RPSDPayoffFunction != null) {
                float[] last = lastStrategies.get(client);
                float points = periodConfig.RPSDPayoffFunction.getPayoff(
                        last[0], last[1], last[2], last[3],
                        population.averageStrategy_r,
                        population.averageStrategy_p,
                        population.averageStrategy_s,
                        population.averageStrategy_d);
                if (!periodConfig.pointsPerSecond) {
                    points *= percentInStrategyTime;
                } else {
                    points *= inStrategyTime / 1000f;
                }
                client.addToPeriodPoints(Math.round(points));
            }
        }
        population.lastEvalTime = timestamp;
        // update clients with new strategy information
        if (periodConfig.twoStrategyPayoffFunction != null) {
            population.averageStrategy_a = 0;
            for (ClientInterface client : population.members) {
                float[] strategy = client.getStrategyAB();
                population.averageStrategy_a += strategy[0];
            }
            population.averageStrategy_a /= population.members.size();
            for (ClientInterface client : population.members) {
                float[] strategy = client.getStrategyAB();
                client.setStrategyAB(
                        strategy[0], strategy[1],
                        population.averageStrategy_a, 1 - population.averageStrategy_a);
                lastStrategies.put(client, strategy);
            }
        } else if (periodConfig.RPSDPayoffFunction != null) {
            population.averageStrategy_r = 0;
            population.averageStrategy_p = 0;
            population.averageStrategy_s = 0;
            population.averageStrategy_d = 0;
            for (ClientInterface client : population.members) {
                float[] strategy = client.getStrategyRPSD();
                population.averageStrategy_r += strategy[0];
                population.averageStrategy_p += strategy[1];
                population.averageStrategy_s += strategy[2];
                population.averageStrategy_d += strategy[3];
            }
            population.averageStrategy_r /= clients.size();
            population.averageStrategy_p /= clients.size();
            population.averageStrategy_s /= clients.size();
            population.averageStrategy_d /= clients.size();
            for (ClientInterface client : population.members) {
                float[] strategy = client.getStrategyRPSD();
                client.setStrategyRPSD(
                        strategy[0], strategy[1], strategy[2], strategy[3],
                        population.averageStrategy_r,
                        population.averageStrategy_p,
                        population.averageStrategy_s,
                        population.averageStrategy_d);
                lastStrategies.put(client, strategy);
            }
        }
        for (ClientInterface client : population.members) {
            lastStrategyChangeTimes.put(client, timestamp);
        }
    }

    @Override
    public void strategyChanged(String name) {
        long timestamp = System.currentTimeMillis();
        doPopulationIncludeStrategyChanged(name, timestamp);
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
        periodConfig.pointsPerSecond = false;
        HomotopyPayoffFunction homotopyPayoffFunction = new HomotopyPayoffFunction();
        homotopyPayoffFunction.AaStart = 10;
        homotopyPayoffFunction.AaEnd = 100;
        homotopyPayoffFunction.Ab = 0;
        homotopyPayoffFunction.Ba = 50;
        homotopyPayoffFunction.Bb = 50;
        periodConfig.twoStrategyPayoffFunction = homotopyPayoffFunction;
        //periodConfig.twoStrategyPayoffFunction = null;
        //periodConfig.RPSDPayoffFunction = new RPSDPayoffFunction();
    }

    @Override
    public void startPeriod() {
        super.startPeriod();
        lastStrategyChangeTimes = new HashMap<ClientInterface, Long>();
        lastStrategies = new HashMap<ClientInterface, float[]>();
        periodStartTime = System.currentTimeMillis();
        initPopulations();
        initStrategies();
    }

    @Override
    public void endPeriod() {
        super.endPeriod();
    }

    private void initPopulations() {
        populations = new LinkedList<Population>();
        populationMap = new HashMap<String, Population>();
        Population population = new Population();
        populations.add(population);
        for (ClientInterface client : clients.values()) {
            population.members.add(client);
            populationMap.put(client.getFullName(), population);
        }
    }

    private void initStrategies() {
        for (ClientInterface client : clients.values()) {
            if (periodConfig.twoStrategyPayoffFunction != null) {
                client.setStrategyAB(0, 1, 0, 1);
                lastStrategies.put(client, new float[]{0});
            } else if (periodConfig.RPSDPayoffFunction != null) {
                client.setStrategyRPSD(0, 0, 0, 1, 0, 0, 0, 1);
                lastStrategies.put(client, new float[]{0, 0, 0, 1});
            }
            lastStrategyChangeTimes.put(client, periodStartTime);
        }
        for (Population population : populations) {
            population.lastEvalTime = periodStartTime;
        }
    }
}
