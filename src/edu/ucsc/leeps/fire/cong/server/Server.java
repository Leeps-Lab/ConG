package edu.ucsc.leeps.fire.cong.server;

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

    private Map<String, ClientInterface> clients;
    private PeriodConfig periodConfig;
    private TickLog tickLog;
    private EventLog eventLog;
    private List<Population> populations;
    private Map<String, Population> membership;
    private Random random;

    public Server() {
        super(PeriodConfig.class, TickLog.class);
        tickLog = new TickLog();
        eventLog = new EventLog();
        clients = new HashMap<String, ClientInterface>();
        //addLog(tickLog);
        //addLog(eventLog);
        random = new Random();
    }

    //@Override
    public synchronized void strategyChanged(String name) {
        long timestamp = System.currentTimeMillis();
        membership.get(name).strategyChanged(name, timestamp, periodConfig);
        eventLog.timestamp = timestamp;
        eventLog.subjectId = clients.get(name).getID();
        eventLog.strategy = clients.get(name).getStrategy();
        eventLog.commit();
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

    //@Override
    public boolean readyToStart() {
        return clients.size() >= 1;
    }

    //@Override
    public void setClients(Map<String, edu.ucsc.leeps.fire.client.BaseClientInterface> superClients) {
        clients.clear();
        for (String name : superClients.keySet()) {
            ClientInterface client = (ClientInterface) superClients.get(name);
            clients.put(name, client);
        }
    }

    @Override
    public void setPeriodConfig(edu.ucsc.leeps.fire.server.BasePeriodConfig superPeriodConfig) {
        super.setPeriodConfig(superPeriodConfig);
        periodConfig = (PeriodConfig) superPeriodConfig;
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
        tickLog.secondsLeft = secondsLeft;
        for (Population population : populations) {
            tickLog.population = population;
            tickLog.commit();
        }
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
        List<ClientInterface> members = new LinkedList<ClientInterface>();
        if (periodConfig.population instanceof Pair) {
            assert clients.values().size() % 2 == 0;
            List<ClientInterface> pool = new LinkedList<ClientInterface>();
            pool.addAll(clients.values());
            while (pool.size() > 0) {
                ClientInterface p1 = pool.remove(random.nextInt(pool.size()));
                ClientInterface p2 = pool.remove(random.nextInt(pool.size()));
                Pair pair = new Pair();
                members.clear();
                members.add(p1);
                members.add(p2);
                pair.setMembers(members, populations, membership);
            }
        } else if (periodConfig.population instanceof SinglePopulationInclude) {
            SinglePopulationInclude population = new SinglePopulationInclude();
            members.clear();
            members.addAll(clients.values());
            population.setMembers(members, populations, membership);
        } else if (periodConfig.population instanceof SinglePopulationExclude) {
            SinglePopulationExclude population = new SinglePopulationExclude();
            members.clear();
            members.addAll(clients.values());
            population.setMembers(members, populations, membership);
        } else {
            assert false;
        }
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
