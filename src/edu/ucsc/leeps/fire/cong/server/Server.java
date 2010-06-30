package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.FIREServerInterface;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.logging.StrategyChangeEvent;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.logging.TickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TimerTask;

/**
 *
 * @author jpettit
 */
public class Server implements ServerInterface, FIREServerInterface<ClientInterface, Config> {

    private Map<Integer, ClientInterface> clients;
    private TickEvent tickLog;
    private StrategyChangeEvent eventLog;
    private Population population;
    private Map<Integer, Population> membership;
    private Random random;
    private boolean impulse = false;

    public Server() {
        clients = new HashMap<Integer, ClientInterface>();
        random = new Random();
    }

    public synchronized void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            float[] hoverStrategy_A,
            float[] hoverStrategy_a,
            Integer id) {
        if (FIRE.server.getConfig().subperiods == 0) {
            long timestamp = System.currentTimeMillis();
            membership.get(id).strategyChanged(
                    newStrategy, targetStrategy, hoverStrategy_A, hoverStrategy_a,
                    id, timestamp);
        }
    }

    public boolean readyToStart() {
        return clients.size() >= 1;
    }

    public void configurePeriod() {
        configurePopulations();
        configureStrategies();
        configureImpulses();
        configureSubperiods();
    }

    public void startPeriod(long periodStartTime) {
        population.initialize(periodStartTime);
    }

    public void endPeriod() {
        if (FIRE.server.getConfig().subperiods == 0) {
            population.endPeriod();
        }
    }

    public void tick(int secondsLeft) {
        return;
    }

    public void quickTick(int millisLeft) {
        population.logTick();
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

    private void configureSubperiods() {
        if (FIRE.server.getConfig().subperiods == 0) {
            return;
        }
        long millisPerSubperiod = Math.round(
                (FIRE.server.getConfig().length / (float) FIRE.server.getConfig().subperiods) * 1000);
        FIRE.server.getTimer().scheduleAtFixedRate(new TimerTask() {

            private int subperiod = 1;

            @Override
            public void run() {
                if (subperiod <= FIRE.server.getConfig().subperiods) {
                    population.endSubperiod(subperiod);
                    subperiod++;
                }
            }
        }, millisPerSubperiod, millisPerSubperiod);
    }

    private void configureImpulses() {
        if (FIRE.server.getConfig().impulse != 0f) {
            long impulseTimeMillis = Math.round(
                    (FIRE.server.getConfig().length * 1000f) * FIRE.server.getConfig().impulse);
            FIRE.server.getTimer().schedule(new TimerTask() {

                @Override
                public void run() {
                    doImpulse();
                }
            }, impulseTimeMillis);
        }
    }

    private void doImpulse() {
        for (Map.Entry<Integer, ClientInterface> entry : clients.entrySet()) {
            int id = entry.getKey();
            ClientInterface client = entry.getValue();
            float[] newStrategy = new float[]{random.nextFloat()};
            client.setMyStrategy(newStrategy);
            strategyChanged(newStrategy, null, null, null, id);
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
