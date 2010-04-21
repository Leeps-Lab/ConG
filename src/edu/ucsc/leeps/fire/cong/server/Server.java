package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.logging.TickLog;
import edu.ucsc.leeps.fire.cong.logging.EventLog;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author jpettit
 */
public class Server extends edu.ucsc.leeps.fire.server.BaseServer implements ServerInterface {

    private Map<Integer, ClientInterface> clients;
    private PeriodConfig periodConfig;
    private TickLog tickLog;
    private EventLog eventLog;
    private List<Population> populations;
    private Map<Integer, Population> membership;
    private Random random;

    public Server() {
        super(PeriodConfig.class, TickLog.class);
        tickLog = new TickLog();
        eventLog = new EventLog();
        clients = new HashMap<Integer, ClientInterface>();
        addLog(tickLog);
        addLog(eventLog);
        random = new Random();
    }

    //@Override
    public synchronized void strategyChanged(Integer id) {
        long timestamp = System.currentTimeMillis();
        membership.get(id).strategyChanged(id, timestamp, periodConfig);
        eventLog.timestamp = timestamp;
        eventLog.subjectId = id;
        eventLog.strategy = clients.get(id).getStrategy();
        eventLog.commit();
    }

    public static void main(String[] args) throws Exception {
        Server server = new Server();
        String periodConfigFilePath = null;
        String clientConfigFilePath = null;
        if (args.length == 2) {
            periodConfigFilePath = args[0];
            clientConfigFilePath = args[1];
        }
        Server.start(periodConfigFilePath, clientConfigFilePath, server);
    }

    //@Override
    public boolean readyToStart() {
        return clients.size() >= 1;
    }

    //@Override
    public void setClients(Map<String, edu.ucsc.leeps.fire.client.BaseClientInterface> superClients) {
        clients.clear();
        for (String name : superClients.keySet()) {
            ClientInterface client = (ClientInterface) superClients.get(name);
            clients.put(client.getID(), client);
        }
    }

    @Override
    public void setPeriodConfig(edu.ucsc.leeps.fire.server.BasePeriodConfig superPeriodConfig) {
        super.setPeriodConfig(superPeriodConfig);
        periodConfig = (PeriodConfig) superPeriodConfig;
    }

    @Override
    public void initPeriod() {
        super.initPeriod();
        long periodStartTime = System.currentTimeMillis();
        initPopulations();
        initStrategies(periodStartTime);
        startPeriod();
    }

    @Override
    public void tick(int secondsLeft) {
        super.tick(secondsLeft);
        tickLog.secondsLeft = secondsLeft;
        for (Population population : populations) {
            tickLog.population = population;
            tickLog.commit();
        }
        if (secondsLeft == 0) {
            endPeriod();
        }
    }

    private void initPopulations() {
        populations = new LinkedList<Population>();
        membership = new HashMap<Integer, Population>();
        List<ClientInterface> members = new LinkedList<ClientInterface>();
        members.clear();
        members.addAll(clients.values());
        periodConfig.population.setMembers(members, populations, membership);
    }

    private void initStrategies(long periodStartTime) {
        for (ClientInterface client : clients.values()) {
            if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
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
