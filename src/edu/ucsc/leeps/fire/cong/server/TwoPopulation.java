/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class TwoPopulation implements Population, Serializable {

    private Map<Integer, ClientInterface> members1, members2;
    private long periodStartTime;
    private long lastEvalTime1, lastEvalTime2;
    private Map<ClientInterface, float[]> lastStrategiesMine;
    private Map<ClientInterface, float[]> lastStrategiesOpposing;

    public void setMembers(
            List<ClientInterface> members,
            List<Population> populations,
            Map<Integer, Population> membership) {
        members1 = new HashMap<Integer, ClientInterface>();
        members2 = new HashMap<Integer, ClientInterface>();
        for (ClientInterface client : members) {
            if (members1.size() >= members2.size()) {
                members1.put(client.getID(), client);
            } else {
                members2.put(client.getID(), client);
            }
            membership.put(client.getID(), this);
        }
        populations.add(this);
    }

    public void initialize(long timestamp, PeriodConfig periodConfig) {
        periodStartTime = timestamp;
        lastEvalTime1 = timestamp;
        lastEvalTime2 = timestamp;
        lastStrategiesMine = new HashMap<ClientInterface, float[]>();
        lastStrategiesOpposing = new HashMap<ClientInterface, float[]>();
        updateStrategies(members1.values(), periodConfig);
        updateStrategies(members2.values(), periodConfig);
    }

    public void strategyChanged(Integer id, long timestamp, PeriodConfig periodConfig) {
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (periodConfig.length * 1000f);
        float percentInStrategyTime;
        Collection<ClientInterface> membersPaid, membersChanged;
        if (members1.containsKey(id)) {
            long inStrategyTime = System.currentTimeMillis() - lastEvalTime2;
            percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
            lastEvalTime2 = timestamp;
            membersChanged = members1.values();
            membersPaid = members2.values();
        } else if (members2.containsKey(id)) {
            long inStrategyTime = System.currentTimeMillis() - lastEvalTime1;
            percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
            lastEvalTime1 = timestamp;
            membersChanged = members2.values();
            membersPaid = members1.values();
        } else {
            assert false;
            percentInStrategyTime = Float.NaN;
            lastEvalTime1 = Long.MIN_VALUE;
            lastEvalTime2 = Long.MIN_VALUE;
            membersChanged = null;
            membersPaid = null;
        }
        for (ClientInterface client : membersPaid) {
            updatePayoffs(
                    client, percent, percentInStrategyTime, percentInStrategyTime, periodConfig);
        }
        updateStrategies(membersChanged, periodConfig);
        ClientInterface changed = null;
        if (members1.containsKey(id)) {
            changed = members1.get(id);
        } else if (members2.containsKey(id)) {
            changed = members2.get(id);
        }
        changed.setMyStrategy(lastStrategiesMine.get(changed));
    }

    private void updatePayoffs(
            ClientInterface client,
            float percent, float percentInStrategyTime, float inStrategyTime,
            PeriodConfig periodConfig) {
        float[] myLast = lastStrategiesMine.get(client);
        float[] otherLast = lastStrategiesOpposing.get(client);
        float points = periodConfig.payoffFunction.getPayoff(
                percent, myLast, otherLast);
        if (!periodConfig.pointsPerSecond) {
            points *= percentInStrategyTime;
        } else {
            points *= inStrategyTime / 1000f;
        }
        client.addToPeriodPoints(points);
    }

    private void updateStrategies(Collection<ClientInterface> members, PeriodConfig periodConfig) {
        for (ClientInterface client : members) {
            float[] averageStrategy = null;
            if (periodConfig.payoffFunction instanceof TwoStrategyPayoffFunction) {
                averageStrategy = new float[1];
                averageStrategy[0] = 0;
            } else if (periodConfig.payoffFunction instanceof ThreeStrategyPayoffFunction) {
                averageStrategy = new float[3];
                averageStrategy[0] = 0;
                averageStrategy[1] = 0;
                averageStrategy[2] = 0;
            } else {
                assert false;
            }
            for (ClientInterface other : members) {
                if (other != client) {
                    float[] strategy = other.getStrategy();
                    for (int i = 0; i < averageStrategy.length; i++) {
                        averageStrategy[i] += strategy[i];
                    }
                }
            }
            for (int i = 0; i < averageStrategy.length; i++) {
                averageStrategy[i] /= (members.size() - 1);
            }
            client.setOpponentStrategy(averageStrategy);
            lastStrategiesMine.put(client, client.getStrategy());
            lastStrategiesOpposing.put(client, averageStrategy);
        }
    }
}
