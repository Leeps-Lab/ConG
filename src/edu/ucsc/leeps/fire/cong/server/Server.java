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

    public synchronized void strategyChanged(final Integer id) {
        final long timestamp = System.currentTimeMillis();
        membership.get(id).strategyChanged(id, timestamp, periodConfig);
        (new Thread() {

            @Override
            public void run() {
                eventLog.timestamp = timestamp;
                eventLog.subjectId = id;
                eventLog.strategy = clients.get(id).getStrategy();
                eventLog.commit();
            }
        }).start();
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
        if (periodConfig.serverInitHeatmaps
                && periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            initHeatmaps();
        }
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

    private void initHeatmaps() {
        int size = 50;
        final float[][][] payoffBuffer = new float[periodConfig.length][size][size];
        final float[][][] counterpartPayoffBuffer = new float[periodConfig.length][size][size];
        for (int tick = 0; tick < periodConfig.length; tick++) {
            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    float A = 1 - (y / (float) size);
                    float a = 1 - (x / (float) size);
                    payoffBuffer[tick][x][y] = periodConfig.payoffFunction.getPayoff(
                            tick / (float) periodConfig.length,
                            new float[]{A},
                            new float[]{a}) / periodConfig.payoffFunction.getMax();
                    counterpartPayoffBuffer[tick][x][y] = periodConfig.counterpartPayoffFunction.getPayoff(
                            tick / (float) periodConfig.length,
                            new float[]{A},
                            new float[]{a}) / periodConfig.counterpartPayoffFunction.getMax();
                }
            }
        }
        final List<ClientInterface> finished = new LinkedList<ClientInterface>();
        for (final ClientInterface client : clients.values()) {
            new Thread() {

                @Override
                public void run() {
                    client.setTwoStrategyHeatmapBuffers(payoffBuffer, counterpartPayoffBuffer);
                    finished.add(client);
                }
            }.start();
        }
        while (finished.size() != clients.size()) {
            Thread.yield();
        }
    }
}
