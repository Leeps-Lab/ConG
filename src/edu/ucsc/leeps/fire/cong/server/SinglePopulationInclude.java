/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class SinglePopulationInclude implements Population, Serializable {

    private List<ClientInterface> members;
    private long periodStartTime;
    private long lastEvalTime;
    private float[] averageStrategy;
    private Map<ClientInterface, float[]> lastStrategies;

    public SinglePopulationInclude() {
        members = new LinkedList<ClientInterface>();
        lastStrategies = new HashMap<ClientInterface, float[]>();
    }

    public void setMembers(
            List<ClientInterface> members,
            List<Population> populations,
            Map<Integer, Population> membership) {
        this.members = members;
        populations.add(this);
        for (ClientInterface client : members) {
            membership.put(client.getID(), this);
        }
    }

    public void initialize(long timestamp, PeriodConfig periodConfig) {
        periodStartTime = timestamp;
        lastEvalTime = timestamp;
        lastStrategies = new HashMap<ClientInterface, float[]>();
        for (ClientInterface client : members) {
            lastStrategies.put(client, client.getStrategy());
        }
        updateStrategies(periodConfig);
    }

    private void updatePayoffs(
            ClientInterface client,
            float percent, float percentInStrategyTime, float inStrategyTime,
            PeriodConfig periodConfig) {
        float[] last = lastStrategies.get(client);
        float points = periodConfig.payoffFunction.getPayoff(
                percent, last, averageStrategy);
        if (!periodConfig.pointsPerSecond) {
            points *= percentInStrategyTime;
        } else {
            points *= inStrategyTime / 1000f;
        }
        client.addToPeriodPoints(points);
    }

    private void updateStrategies(PeriodConfig periodConfig) {
        if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
            averageStrategy = new float[1];
            averageStrategy[0] = 0;
        } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
            averageStrategy = new float[3];
            averageStrategy[0] = 0;
            averageStrategy[1] = 0;
            averageStrategy[2] = 0;
        }
        for (ClientInterface client : members) {
            float[] strategy = client.getStrategy();
            for (int i = 0; i < averageStrategy.length; i++) {
                averageStrategy[i] += strategy[i];
            }
        }
        for (int i = 0; i < averageStrategy.length; i++) {
            averageStrategy[i] /= members.size();
        }
        for (ClientInterface client : members) {
            client.setCounterpartStrategy(averageStrategy);
            lastStrategies.put(client, client.getStrategy());
        }
    }

    public void strategyChanged(float[] newStrategy, Integer id, long timestamp, PeriodConfig periodConfig) {
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (periodConfig.length * 1000f);
        long inStrategyTime = System.currentTimeMillis() - lastEvalTime;
        float percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
        for (ClientInterface client : members) {
            updatePayoffs(
                    client, percent, percentInStrategyTime, percentInStrategyTime, periodConfig);
        }
        lastEvalTime = timestamp;
        updateStrategies(periodConfig);
    }
}
