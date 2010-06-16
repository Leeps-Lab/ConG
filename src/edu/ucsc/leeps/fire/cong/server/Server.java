package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.FIREServerInterface;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.logging.EventLog;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.logging.TickLog;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author jpettit
 */
public class Server implements ServerInterface, FIREServerInterface<ClientInterface, PeriodConfig> {

    private Map<Integer, ClientInterface> clients;
    private PeriodConfig periodConfig;
    private TickLog tickLog;
    private EventLog eventLog;
    private Population population;
    private Map<Integer, Population> membership;
    private Random random;

    public Server() {
        tickLog = new TickLog();
        eventLog = new EventLog();
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

    public boolean readyToStart() {
        return clients.size() >= 1;
    }

    public void setPeriodConfig(PeriodConfig periodConfig) {
        this.periodConfig = periodConfig;
    }

    public void startPeriod() {
        long periodStartTime = System.currentTimeMillis();
        tickLog.periodStartTime = periodStartTime;
        eventLog.periodStartTime = periodStartTime;
        initPopulations();
        initStrategies(periodStartTime);
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
        Map<Integer, ClientInterface> members = new HashMap<Integer, ClientInterface>();
        members.clear();
        members.putAll(clients);
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

    public void unregister(int id) {
        clients.remove(id);
    }

    public static void main(String[] args) {
        FIRE.startServer();
    }

    public boolean register(int id, ClientInterface client) {
        clients.put(id, client);
        return true;
    }
}
