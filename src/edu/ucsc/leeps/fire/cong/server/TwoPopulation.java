package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.logging.EventLog;
import edu.ucsc.leeps.fire.cong.logging.TickLog;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author jpettit
 */
public class TwoPopulation implements Population, Serializable {

    private Map<Integer, ClientInterface> members1, members2;
    private long periodStartTime;
    private long lastEvalTime1, lastEvalTime2;
    private Map<Integer, float[]> lastStrategiesMine;
    private Map<Integer, float[]> lastStrategiesOpposing;

    public void setMembers(
            Map<Integer, ClientInterface> members,
            Map<Integer, Population> membership) {
        members1 = new HashMap<Integer, ClientInterface>();
        members2 = new HashMap<Integer, ClientInterface>();
        for (Entry<Integer, ClientInterface> entry : members.entrySet()) {
            if (members1.size() >= members2.size()) {
                members1.put(entry.getKey(), entry.getValue());
            } else {
                members2.put(entry.getKey(), entry.getValue());
            }
            membership.put(entry.getKey(), this);
        }
    }

    public void initialize(long timestamp, PeriodConfig periodConfig) {
        periodStartTime = timestamp;
        lastEvalTime1 = timestamp;
        lastEvalTime2 = timestamp;
        lastStrategiesMine = new HashMap<Integer, float[]>();
        lastStrategiesOpposing = new HashMap<Integer, float[]>();
        updateStrategies(members1.keySet(), periodConfig);
        updateStrategies(members2.keySet(), periodConfig);
    }

    public void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            float[] hoverStrategy_A,
            float[] hoverStrategy_a,
            Integer changed, long timestamp,
            PeriodConfig periodConfig,
            EventLog eventLog) {
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (periodConfig.length * 1000f);
        float percentInStrategyTime;
        Collection<Integer> membersPaid, membersChanged;
        if (members1.containsKey(changed)) {
            long inStrategyTime = System.currentTimeMillis() - lastEvalTime2;
            percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
            lastEvalTime2 = timestamp;
            membersChanged = members1.keySet();
            membersPaid = members2.keySet();
        } else if (members2.containsKey(changed)) {
            long inStrategyTime = System.currentTimeMillis() - lastEvalTime1;
            percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
            lastEvalTime1 = timestamp;
            membersChanged = members2.keySet();
            membersPaid = members1.keySet();
        } else {
            assert false;
            percentInStrategyTime = Float.NaN;
            lastEvalTime1 = Long.MIN_VALUE;
            lastEvalTime2 = Long.MIN_VALUE;
            membersChanged = null;
            membersPaid = null;
        }
        for (Integer client : membersPaid) {
            updatePayoffs(
                    client, percent, percentInStrategyTime, percentInStrategyTime, periodConfig);
        }
        updateStrategies(membersChanged, periodConfig);
        if (members1.containsKey(changed)) {
            members1.get(changed).setMyStrategy(lastStrategiesMine.get(changed));
        } else if (members2.containsKey(changed)) {
            members2.get(changed).setMyStrategy(lastStrategiesMine.get(changed));
        } else {
            assert false;
        }
    }

    private void updatePayoffs(
            int client,
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
        FIRE.server.addToPeriodPoints(client, points);
    }

    private void updateStrategies(Collection<Integer> members, PeriodConfig periodConfig) {
        for (Integer client : members) {
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
            for (Integer other : members) {
                if (other != client) {
                    float[] strategy;
                    if (members1.containsKey(other)) {
                        strategy = members1.get(other).getStrategy();
                    } else if (members2.containsKey(other)) {
                        strategy = members2.get(other).getStrategy();
                    } else {
                        assert false;
                        strategy = null;
                    }
                    for (int i = 0; i < averageStrategy.length; i++) {
                        averageStrategy[i] += strategy[i];
                    }
                }
            }
            for (int i = 0; i < averageStrategy.length; i++) {
                averageStrategy[i] /= (members.size() - 1);
            }
            if (members1.containsKey(client)) {
                members1.get(client).setCounterpartStrategy(averageStrategy);
                lastStrategiesMine.put(client, members1.get(client).getStrategy());
            } else if (members2.containsKey(client)) {
                members2.get(client).setCounterpartStrategy(averageStrategy);
                lastStrategiesMine.put(client, members2.get(client).getStrategy());
            } else {
                assert false;
            }
            lastStrategiesOpposing.put(client, averageStrategy);
        }
    }

    public void endPeriod(PeriodConfig periodConfig) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    public void logTick(TickLog tickLog, PeriodConfig periodConfig) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
