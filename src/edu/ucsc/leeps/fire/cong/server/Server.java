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
    private TickLog tickLog;
    private List<Population> populations;
    private Map<String, Population> membership;

    public Server() {
        super(PeriodConfig.class, TickLog.class);
        tickLog = new TickLog();
        clients = new HashMap<String, ClientInterface>();
    }

    @Override
    public void strategyChanged(String name) {
        long timestamp = System.currentTimeMillis();
        membership.get(name).strategyChanged(name, timestamp, periodConfig);
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
        TwoStrategyHomotopyPayoffFunction homotopyPayoffFunction = new TwoStrategyHomotopyPayoffFunction();
        homotopyPayoffFunction.AaStart = 100;
        homotopyPayoffFunction.AaEnd = 100;
        homotopyPayoffFunction.Ab = 0;
        homotopyPayoffFunction.Ba = 0;
        homotopyPayoffFunction.Bb = 900;
        periodConfig.payoffFunction = homotopyPayoffFunction;
        //periodConfig.payoffFunction = new RPSPayoffFunction();
        for (ClientInterface client : clients.values()) {
            client.setPeriodConfig(periodConfig);
        }
    }

    @Override
    public void initPeriod() {
        super.initPeriod();
        long periodStartTime = System.currentTimeMillis();
        initPopulations(periodStartTime);
        initStrategies(periodStartTime);
        startPeriod();
    }

    @Override
    public void tick(int secondsLeft) {
        super.tick(secondsLeft);
    }

    @Override
    public void quickTick(int millisLeft) {
        super.quickTick(millisLeft);
    }

    @Override
    public void endPeriod() {
        super.endPeriod();
    }

    private void initPopulations(long periodStartTime) {
        populations = new LinkedList<Population>();
        membership = new HashMap<String, Population>();
        SinglePopulationInclude population = new SinglePopulationInclude();
        List<ClientInterface> members = new LinkedList<ClientInterface>();
        members.addAll(clients.values());
        population.setMembers(members, populations, membership);
    }

    private void initStrategies(long periodStartTime) {
        for (ClientInterface client : clients.values()) {
            if (periodConfig.payoffFunction instanceof TwoStrategyHomotopyPayoffFunction) {
                client.setMyStrategy(new float[]{0});
            } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
                client.setMyStrategy(new float[]{0.33f, 0.33f, 0.33f});
            } else {
                assert false;
            }
        }
        for (Population population : populations) {
            population.initialize(periodStartTime, periodConfig);
        }
    }
}
