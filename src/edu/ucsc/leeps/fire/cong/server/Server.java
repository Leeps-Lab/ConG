package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.FIREServerInterface;
import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.config.Config;
import edu.ucsc.leeps.fire.cong.logging.StrategyChangeEvent;
import edu.ucsc.leeps.fire.server.ServerController.State;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

/**
 *
 * @author jpettit
 */
public class Server implements ServerInterface, FIREServerInterface<ClientInterface, Config> {

    private Map<Integer, ClientInterface> clients;
    private Population population;
    private final Object eventLock = new Object();
    private Map<Integer, StrategyChangeEvent> strategyChangeEvents;
    private StrategyProcessor strategyProcessor;

    public Server() {
        clients = new HashMap<Integer, ClientInterface>();
        strategyChangeEvents = new HashMap<Integer, StrategyChangeEvent>();
        strategyProcessor = new StrategyProcessor();
        strategyProcessor.start();
    }

    public void strategyChanged(
            final float[] newStrategy,
            final float[] targetStrategy,
            final Integer id) {
        if (FIRE.server.getState() == State.RUNNING_PERIOD) {
            synchronized (eventLock) {
                StrategyChangeEvent newEvent = new StrategyChangeEvent(System.nanoTime(), id, newStrategy, targetStrategy);
                strategyChangeEvents.put(id, newEvent);
            }
        }
    }

    public void configurePeriod() {
        Map<Integer, ClientInterface> members = new HashMap<Integer, ClientInterface>();
        members.clear();
        members.putAll(clients);
        population = new Population();
        population.configure(members);
    }

    public boolean readyToEndPrePeriod() {
        for (Integer id : clients.keySet()) {
            if (!clients.get(id).haveInitialStrategy()) {
                return false;
            }
        }
        return true;
    }

    public void startPeriod(long periodStartTime) {
        population.setPeriodStartTime();
        configureImpulses();
        configureSubperiods();
    }

    public void endPeriod() {
        if (FIRE.server.getConfig().subperiods == 0) {
            population.endPeriod();
        } else {
            population.endSubperiod(FIRE.server.getConfig().subperiods);
            population.logTick(FIRE.server.getConfig().subperiods, 0);
        }

        for (int id : clients.keySet()) {
            float points = FIRE.server.getPeriodPoints(id);
            float cost = clients.get(id).getCost();
            if (cost > points && !FIRE.server.getConfig().negativePayoffs) {
                cost = points;
            }
            FIRE.server.setPeriodPoints(id, points - cost);
        }
    }

    public void tick(int secondsLeft) {
        if (FIRE.server.getConfig().subperiods == 0) {
            population.logTick(0, secondsLeft);
            population.evaluate();
            System.err.println(strategyChangeEvents.size());
        }
    }

    public void quickTick(int millisLeft) {
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
                    population.logTick(subperiod, 0);
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
        /*
        for (Map.Entry<Integer, ClientInterface> entry : clients.entrySet()) {
        int id = entry.getKey();
        ClientInterface client = entry.getValue();
        float r = FIRE.server.getRandom().nextFloat();
        float[] newStrategy = new float[]{r, 1 - r};
        client.setStrategy(newStrategy);
        strategyChanged(newStrategy, newStrategy, id);
        }
         * 
         */
    }

    public void newMessage(String message, int senderID) {
        for (Map.Entry<Integer, ClientInterface> entry : clients.entrySet()) {
            ClientInterface client = entry.getValue();
            client.newMessage(message, senderID);

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

    private class StrategyProcessor extends Thread {

        @Override
        public void run() {
            while (true) {
                synchronized (eventLock) {
                    for (Integer id : strategyChangeEvents.keySet()) {
                        StrategyChangeEvent event = strategyChangeEvents.get(id);
                        if (event != null) {
                            population.strategyChanged(event.newStrategy, event.targetStrategy, event.id);
                            strategyChangeEvents.put(id, null);
                        }
                    }
                }
            }
        }
    }
}
