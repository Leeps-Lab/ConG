package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.logging.EventLog;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.logging.TickLog;
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
    private Population population;
    private Map<Integer, Population> membership;
    private Random random;

    public Server() {
        super(PeriodConfig.class);
        tickLog = new TickLog();
        eventLog = new EventLog();
        addLog(tickLog);
        addLog(eventLog);
        clients = new HashMap<Integer, ClientInterface>();
        random = new Random();
    }

    public synchronized void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            float[] hoverStrategy_A,
            float[] hoverStrategy_a,
            Integer id) {
        long timestamp = System.currentTimeMillis();
        membership.get(id).strategyChanged(
                newStrategy, targetStrategy, hoverStrategy_A, hoverStrategy_a,
                id, timestamp, periodConfig, eventLog);
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
    public void setPeriodConfig(edu.ucsc.leeps.fire.server.BasePeriodConfig basePeriodConfig) {
        super.setPeriodConfig(basePeriodConfig);
        this.periodConfig = (PeriodConfig) basePeriodConfig;
    }

    @Override
    public void initPeriod() {
        super.initPeriod();
        long periodStartTime = System.currentTimeMillis();
        tickLog.periodStartTime = periodStartTime;
        eventLog.periodStartTime = periodStartTime;
        initPopulations();
        initStrategies(periodStartTime);
        if (periodConfig.serverInitHeatmaps
                && periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            initHeatmaps();
        }
        startPeriod();
    }

    @Override
    public void endPeriod() {
        population.endPeriod(periodConfig);
        super.endPeriod();
    }

    @Override
    public void quickTick(int millisLeft) {
        tickLog.period = configurator.getCurrentPeriodNum();
        tickLog.timestamp = System.currentTimeMillis();
        tickLog.millisLeft = millisLeft;
        tickLog.payoffFunction = periodConfig.payoffFunction;
        tickLog.counterpartPayoffFunction = periodConfig.counterpartPayoffFunction;
        population.logTick(tickLog, periodConfig);
        super.quickTick(millisLeft);
    }

    private void initPopulations() {
        membership = new HashMap<Integer, Population>();
        List<ClientInterface> members = new LinkedList<ClientInterface>();
        members.clear();
        members.addAll(clients.values());
        population = periodConfig.population;
        population.setMembers(members, membership);
    }

    private void initStrategies(long periodStartTime) {
        for (ClientInterface client : clients.values()) {
            if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
                client.initMyStrategy(new float[]{random.nextFloat()});
            } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
                client.initMyStrategy(new float[]{0.33f, 0.33f, 0.33f});
            } else {
                assert false;
            }
        }
        population.initialize(periodStartTime, periodConfig);
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
