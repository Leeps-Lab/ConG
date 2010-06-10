package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientState;
import edu.ucsc.leeps.fire.cong.logging.EventLog;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
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
public class Server implements
        ServerInterface,
        edu.ucsc.leeps.fire.server.ServerInterface<PeriodConfig> {

    private Map<Integer, ClientState> clients;
    private PeriodConfig periodConfig;
    private TickLog tickLog;
    private EventLog eventLog;
    private Population population;
    private Map<Integer, Population> membership;
    private Random random;

    public Server() {
        tickLog = new TickLog();
        eventLog = new EventLog();
        clients = new HashMap<Integer, ClientState>();
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

    public boolean readyToStart() {
        return clients.size() >= 1;
    }

    public void setPeriodConfig(PeriodConfig periodConfig) {
        this.periodConfig = periodConfig;
    }

    public void initPeriod() {
        long periodStartTime = System.currentTimeMillis();
        tickLog.periodStartTime = periodStartTime;
        eventLog.periodStartTime = periodStartTime;
        initPopulations();
        initStrategies(periodStartTime);
        /*
        if (periodConfig.serverInitHeatmaps
        && periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
        initHeatmaps();
        }
         * 
         */
    }

    public void endPeriod() {
        population.endPeriod(periodConfig);
    }

    public void tick(int secondsLeft) {
        return;
    }

    public void quickTick(int millisLeft) {
        tickLog.period = periodConfig.number;
        tickLog.timestamp = System.currentTimeMillis();
        tickLog.millisLeft = millisLeft;
        tickLog.payoffFunction = periodConfig.payoffFunction;
        tickLog.counterpartPayoffFunction = periodConfig.counterpartPayoffFunction;
        population.logTick(tickLog, periodConfig);
    }

    private void initPopulations() {
        membership = new HashMap<Integer, Population>();
        List<ClientState> members = new LinkedList<ClientState>();
        members.clear();
        members.addAll(clients.values());
        population = periodConfig.population;
        population.setMembers(members, membership);
    }

    private void initStrategies(long periodStartTime) {
        for (ClientState client : clients.values()) {
            if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
                client.client.initMyStrategy(new float[]{random.nextFloat()});
            } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
                client.client.initMyStrategy(new float[]{0.33f, 0.33f, 0.33f});
            } else {
                assert false;
            }
        }
        population.initialize(periodStartTime, periodConfig);
    }

    public void register(edu.ucsc.leeps.fire.client.ClientState client) {
        clients.put(client.getID(), (ClientState) client);
    }

    public void unregister(int id) {
        clients.remove(id);
    }

    /*
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
    final List<ClientState<ServerInterface, ClientInterface, PeriodConfig>> finished = new LinkedList<ClientState<ServerInterface, ClientInterface, PeriodConfig>>();
    for (final ClientState<ServerInterface, ClientInterface, PeriodConfig> client : clients.values()) {
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
     * 
     */
}
