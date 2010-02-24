/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class SinglePopulationInclude implements Population {

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
            Map<String, Population> membership) {
        this.members = members;
        populations.add(this);
        for (ClientInterface client : members) {
            membership.put(client.getFullName(), this);
        }
    }

    public void initialize(long timestamp, PeriodConfig periodConfig) {
        periodStartTime = timestamp;
        lastEvalTime = timestamp;
        lastStrategies = new HashMap<ClientInterface, float[]>();
        for (ClientInterface client : members) {
            lastStrategies.put(client, client.getStrategy());
        }
        if (periodConfig.twoStrategyPayoffFunction != null) {
            updateTwoStrategies();
        } else if (periodConfig.RPSPayoffFunction != null) {
            updateThreeStrategies();
        } else {
            assert false;
        }
    }

    private float getTwoStrategyPayoffs(
            ClientInterface client,
            float percent, float percentInStrategyTime, float inStrategyTime,
            PeriodConfig periodConfig) {
        float[] last = lastStrategies.get(client);
        float points = periodConfig.twoStrategyPayoffFunction.getPayoff(
                percent,
                last[0], 1 - last[0],
                averageStrategy[0], 1 - averageStrategy[0]);
        if (!periodConfig.pointsPerSecond) {
            points *= percentInStrategyTime;
        } else {
            points *= inStrategyTime / 1000f;
        }
        return points;
    }

    private float getThreeStrategyPayoffs(
            ClientInterface client,
            float percent, float percentInStrategyTime, float inStrategyTime,
            PeriodConfig periodConfig) {
        float[] last = lastStrategies.get(client);
        float points = periodConfig.RPSPayoffFunction.getPayoff(
                percent,
                last[0], last[1], last[2],
                averageStrategy[0], averageStrategy[1], averageStrategy[2]);
        if (!periodConfig.pointsPerSecond) {
            points *= percentInStrategyTime;
        } else {
            points *= inStrategyTime / 1000f;
        }
        return points;
    }

    private void updateTwoStrategies() {
        averageStrategy = new float[1];
        averageStrategy[0] = 0;
        for (ClientInterface client : members) {
            float[] strategy = client.getStrategy();
            averageStrategy[0] += strategy[0];
        }
        averageStrategy[0] /= members.size();
        for (ClientInterface client : members) {
            client.setOpponentStrategy(averageStrategy);
            lastStrategies.put(client, client.getStrategy());
        }
    }

    private void updateThreeStrategies() {
        averageStrategy = new float[3];
        averageStrategy[0] = 0;
        averageStrategy[1] = 0;
        averageStrategy[2] = 0;
        for (ClientInterface client : members) {
            float[] strategy = client.getStrategy();
            averageStrategy[0] += strategy[0];
            averageStrategy[1] += strategy[1];
            averageStrategy[2] += strategy[2];
        }
        averageStrategy[0] /= members.size();
        averageStrategy[1] /= members.size();
        averageStrategy[2] /= members.size();
        for (ClientInterface client : members) {
            client.setOpponentStrategy(averageStrategy);
            lastStrategies.put(client, client.getStrategy());
        }
    }

    public void strategyChanged(String name, long timestamp, PeriodConfig periodConfig) {
        // update clients with payoff information for last strategy
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (periodConfig.length * 1000f);
        long inStrategyTime = System.currentTimeMillis() - lastEvalTime;
        float percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
        for (ClientInterface client : members) {
            float points = 0;
            if (periodConfig.twoStrategyPayoffFunction != null) {
                points = getTwoStrategyPayoffs(
                        client, percent, percentInStrategyTime, inStrategyTime, periodConfig);
            } else if (periodConfig.RPSPayoffFunction != null) {
                points = getThreeStrategyPayoffs(
                        client, percent, percentInStrategyTime, inStrategyTime, periodConfig);
            } else {
                assert false;
            }
            client.addToPeriodPoints(points);
        }
        lastEvalTime = timestamp;
        // update clients with new strategy information
        if (periodConfig.twoStrategyPayoffFunction != null) {
            updateTwoStrategies();
        } else if (periodConfig.RPSPayoffFunction != null) {
            updateThreeStrategies();
        } else {
            assert false;
        }
    }
}
