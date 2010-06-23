package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.FIREServerInterface;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.logging.EventLog;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.logging.TickLog;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author jpettit
 */
public class Server implements ServerInterface, FIREServerInterface<ClientInterface, Config> {

    private Map<Integer, ClientInterface> clients;
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
                id, timestamp, eventLog);
    }

    public boolean readyToStart() {
        return clients.size() >= 1;
    }

    public void configurePeriod() {
        configurePopulations();
        configureStrategies();
    }

    public void startPeriod() {
        long periodStartTime = System.currentTimeMillis();
        tickLog.periodStartTime = periodStartTime;
        eventLog.periodStartTime = periodStartTime;
        population.initialize(periodStartTime);
    }

    public void endPeriod() {
        population.endPeriod();
    }

    public void tick(int secondsLeft) {
        return;
    }

    public void quickTick(int millisLeft) {
        tickLog.period = FIRE.server.getConfig().number;
        tickLog.timestamp = System.currentTimeMillis();
        tickLog.millisLeft = millisLeft;
        tickLog.payoffFunction = FIRE.server.getConfig().payoffFunction;
        tickLog.counterpartPayoffFunction = FIRE.server.getConfig().counterpartPayoffFunction;
        population.logTick(tickLog);
    }

    private void configurePopulations() {
        membership = new HashMap<Integer, Population>();
        Map<Integer, ClientInterface> members = new HashMap<Integer, ClientInterface>();
        members.clear();
        members.putAll(clients);
        population = FIRE.server.getConfig().population;
        population.setMembers(members, membership);
    }

    private void configureStrategies() {
        for (Integer client : clients.keySet()) {
            if (FIRE.server.getConfig(client).payoffFunction instanceof TwoStrategyPayoffFunction) {
                FIRE.server.getConfig(client).initialStrategy = new float[]{random.nextFloat()};
            } else if (FIRE.server.getConfig(client).payoffFunction instanceof ThreeStrategyPayoffFunction) {
                FIRE.server.getConfig(client).initialStrategy = new float[]{0.33f, 0.33f, 0.33f};
            } else {
                assert false;
            }
        }
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
