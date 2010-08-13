package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class SinglePopulationInclude implements Population {

    private Map<Integer, ClientInterface> members;
    private long periodStartTime;
    private long lastEvalTime;
    private float[] averageStrategy;
    private Map<Integer, float[]> lastStrategies;

    public SinglePopulationInclude() {
        members = new HashMap<Integer, ClientInterface>();
        lastStrategies = new HashMap<Integer, float[]>();
    }

    public void setMembers(
            Map<Integer, ClientInterface> members,
            Map<Integer, Population> membership) {
        this.members = members;
        for (Integer clientID : members.keySet()) {
            membership.put(clientID, this);
        }
    }

    public void initialize(long timestamp) {
        periodStartTime = timestamp;
        lastEvalTime = timestamp;
        lastStrategies.clear();
        for (Integer client : members.keySet()) {
            lastStrategies.put(client, members.get(client).getStrategy());
        }
        updateStrategies();
    }

    private void updatePayoffs(
            Integer client,
            float percent, float percentInStrategyTime, float inStrategyTime) {
        float[] last = lastStrategies.get(client);
        if (last != null && averageStrategy != null) {
            float points = FIRE.server.getConfig().payoffFunction.getPayoff(
                    percent, last, averageStrategy);
            if (!FIRE.server.getConfig().pointsPerSecond) {
                points *= percentInStrategyTime;
            } else {
                points *= inStrategyTime / 1000f;
            }
            FIRE.server.addToPeriodPoints(client, points);
        }
    }

    private void updateStrategies() {
        if (FIRE.server.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
            averageStrategy = new float[2];
            averageStrategy[0] = 0;
            averageStrategy[1] = 0;
        } else if (FIRE.server.getConfig().payoffFunction instanceof ThreeStrategyPayoffFunction) {
            averageStrategy = new float[3];
            averageStrategy[0] = 0;
            averageStrategy[1] = 0;
            averageStrategy[2] = 0;
        }
        for (Integer client : members.keySet()) {
            float[] strategy = members.get(client).getStrategy();
            for (int i = 0; i < averageStrategy.length; i++) {
                averageStrategy[i] += strategy[i];
            }
        }
        for (int i = 0; i < averageStrategy.length; i++) {
            averageStrategy[i] /= members.size();
        }
        for (Integer client : members.keySet()) {
            members.get(client).setCounterpartStrategy(averageStrategy);
            lastStrategies.put(client, members.get(client).getStrategy());
        }
    }

    public void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            float[] hoverStrategy_A,
            float[] hoverStrategy_a,
            Integer id, long timestamp) {
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (FIRE.server.getConfig().length * 1000f);
        long inStrategyTime = System.currentTimeMillis() - lastEvalTime;
        float percentInStrategyTime = inStrategyTime / (FIRE.server.getConfig().length * 1000f);
        for (Integer client : members.keySet()) {
            updatePayoffs(
                    client, percent, percentInStrategyTime, percentInStrategyTime);
        }
        lastEvalTime = timestamp;
        updateStrategies();
    }

    public void endSubperiod(int subperiod) {
    }

    public void endPeriod() {
    }

    public void logTick(int subperiod, int millisLeft) {
    }
}
