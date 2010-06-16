package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
import edu.ucsc.leeps.fire.cong.logging.EventLog;
import edu.ucsc.leeps.fire.cong.logging.TickLog;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author jpettit
 */
public class PairedPopulation implements Population, Serializable {

    private Map<Integer, ClientInterface> members;
    private long periodStartTime;
    private Set<Integer> counterparts;
    private Map<Integer, Integer> pairs;
    private Map<Integer, Long> lastEvalTimes;
    private Map<Integer, float[]> lastStrategies;
    private Map<Integer, float[]> lastTargetStrategies;
    private Map<Integer, float[]> lastHoverStrategies_A;
    private Map<Integer, float[]> lastHoverStrategies_a;

    public void setMembers(
            Map<Integer, ClientInterface> members,
            Map<Integer, Population> membership) {
        this.members = members;
        for (Integer id : members.keySet()) {
            membership.put(id, this);
        }
    }

    public void initialize(long timestamp, PeriodConfig periodConfig) {
        periodStartTime = timestamp;
        lastEvalTimes = new HashMap<Integer, Long>();
        for (Integer id : members.keySet()) {
            lastEvalTimes.put(id, timestamp);
        }
        lastStrategies = new HashMap<Integer, float[]>();
        lastTargetStrategies = new HashMap<Integer, float[]>();
        lastHoverStrategies_A = new HashMap<Integer, float[]>();
        lastHoverStrategies_a = new HashMap<Integer, float[]>();
        pairs = new HashMap<Integer, Integer>();
        List<Integer> partners = new ArrayList<Integer>();
        partners.addAll(members.keySet());
        Collections.shuffle(partners);
        if (partners.size() % 2 != 0) {
            System.err.println("Error while making pairs, odd number of subjects to pair up");
        }
        this.counterparts = new HashSet<Integer>();
        while (partners.size() > 0) {
            Integer client1 = partners.remove(0);
            Integer client2 = partners.remove(0);
            pairs.put(client1, client2);
            pairs.put(client2, client1);
            members.get(client1).setIsCounterpart(false);
            members.get(client2).setIsCounterpart(true);
            counterparts.add(client2);
        }
        updateAllStrategies();
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
        int other = pairs.get(changed);
        long inStrategyTime = timestamp - lastEvalTimes.get(changed);
        percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
        lastEvalTimes.put(changed, timestamp);
        lastEvalTimes.put(other, timestamp);
        updatePayoffs(
                newStrategy,
                changed, other,
                percent, percentInStrategyTime, percentInStrategyTime, periodConfig);
        // log the event
        eventLog.period = periodConfig.number;
        eventLog.timestamp = timestamp;
        eventLog.changedId = changed;
        eventLog.counterpartId = other;
        eventLog.currentStrategy = newStrategy;
        eventLog.targetStrategy = targetStrategy;
        eventLog.hoverStrategy_A = hoverStrategy_A;
        eventLog.hoverStrategy_a = hoverStrategy_a;
        eventLog.counterpartCurrentStrategy = lastStrategies.get(other);
        eventLog.counterpartTargetStrategy = lastTargetStrategies.get(other);
        eventLog.counterpartHoverStrategy_A = lastHoverStrategies_A.get(other);
        eventLog.counterpartHoverStrategy_a = lastHoverStrategies_a.get(other);
        if (counterparts.contains(changed)) {
            eventLog.isCounterpart = true;
            eventLog.payoffFunction = periodConfig.counterpartPayoffFunction;
            eventLog.counterpartPayoffFunction = periodConfig.payoffFunction;
        } else {
            eventLog.isCounterpart = false;
            eventLog.payoffFunction = periodConfig.payoffFunction;
            eventLog.counterpartPayoffFunction = periodConfig.counterpartPayoffFunction;
        }
        if (eventLog.targetStrategy == null) {
            eventLog.targetStrategy = eventLog.currentStrategy;
        }
        if (eventLog.counterpartTargetStrategy == null) {
            eventLog.counterpartTargetStrategy = eventLog.counterpartCurrentStrategy;
        }
        if (eventLog.hoverStrategy_A == null) {
            eventLog.hoverStrategy_A = new float[eventLog.currentStrategy.length];
            for (int i = 0; i < eventLog.hoverStrategy_A.length; i++) {
                eventLog.hoverStrategy_A[i] = Float.NaN;
            }
        }
        if (eventLog.hoverStrategy_a == null) {
            eventLog.hoverStrategy_a = new float[eventLog.currentStrategy.length];
            for (int i = 0; i < eventLog.hoverStrategy_a.length; i++) {
                eventLog.hoverStrategy_a[i] = Float.NaN;
            }
        }
        if (eventLog.counterpartHoverStrategy_A == null) {
            eventLog.counterpartHoverStrategy_A = new float[eventLog.currentStrategy.length];
            for (int i = 0; i < eventLog.counterpartHoverStrategy_A.length; i++) {
                eventLog.counterpartHoverStrategy_A[i] = Float.NaN;
            }
        }
        if (eventLog.counterpartHoverStrategy_a == null) {
            eventLog.counterpartHoverStrategy_a = new float[eventLog.currentStrategy.length];
            for (int i = 0; i < eventLog.counterpartHoverStrategy_a.length; i++) {
                eventLog.counterpartHoverStrategy_a[i] = Float.NaN;
            }
        }
        //eventLog.commit();
        // save the strategies
        lastTargetStrategies.put(changed, targetStrategy);
        lastHoverStrategies_A.put(changed, hoverStrategy_A);
        lastHoverStrategies_a.put(changed, hoverStrategy_a);
    }

    private void updatePayoffs(
            float[] newStrategy,
            int changed, int other,
            float percent, float percentInStrategyTime, float inStrategyTime,
            PeriodConfig periodConfig) {
        PayoffFunction changedPayoff, otherPayoff;
        if (counterparts.contains(changed)) {
            changedPayoff = periodConfig.counterpartPayoffFunction;
            otherPayoff = periodConfig.payoffFunction;
        } else {
            changedPayoff = periodConfig.payoffFunction;
            otherPayoff = periodConfig.counterpartPayoffFunction;
        }
        float[] changedLast = lastStrategies.get(changed);
        float[] otherLast = lastStrategies.get(other);
        float changedPoints = changedPayoff.getPayoff(
                percent, changedLast, otherLast);
        float otherPoints = otherPayoff.getPayoff(
                percent, otherLast, changedLast);
        if (!periodConfig.pointsPerSecond) {
            changedPoints *= percentInStrategyTime;
            otherPoints *= percentInStrategyTime;
        } else {
            changedPoints *= inStrategyTime / 1000f;
            otherPoints *= inStrategyTime / 1000f;
        }
        FIRE.server.addToPeriodPoints(changed, changedPoints);
        FIRE.server.addToPeriodPoints(other, otherPoints);
        updateStrategiesFast(newStrategy, changed, other);
    }

    private void updateStrategiesFast(
            float[] newStrategy, int changed, int other) {
        float[] c1s = newStrategy;
        members.get(other).setCounterpartStrategy(c1s);
        lastStrategies.put(changed, c1s);
    }

    private void updateStrategiesSlow(int client1, int client2) {
        float[] c1s = members.get(client1).getStrategy();
        float[] c2s = members.get(client2).getStrategy();
        members.get(client1).setCounterpartStrategy(c2s);
        members.get(client2).setCounterpartStrategy(c1s);
        lastStrategies.put(client1, c1s);
        lastStrategies.put(client2, c2s);
    }

    private void updateAllStrategies() {
        Set<Integer> notified = new HashSet<Integer>();
        for (int client : members.keySet()) {
            if (!notified.contains(client)) {
                updateStrategiesSlow(client, pairs.get(client));
                notified.add(client);
                notified.add(pairs.get(client));
            }
        }
    }

    public void endPeriod(PeriodConfig periodConfig) {
        float percentInStrategyTime;
        for (Integer client1 : counterparts) {
            Integer client2 = pairs.get(client1);
            long lastEvalTime1 = lastEvalTimes.get(client1);
            long lastEvalTime2 = lastEvalTimes.get(client2);
            long lastEvalTime = Math.max(lastEvalTime1, lastEvalTime2);
            long inStrategyTime = (periodStartTime + (periodConfig.length * 1000)) - lastEvalTime;
            percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
            updatePayoffs(
                    lastStrategies.get(client1),
                    client1, client2,
                    1.0f, percentInStrategyTime, percentInStrategyTime, periodConfig);
        }
    }

    public void logTick(TickLog tickLog, PeriodConfig periodConfig) {
        for (Entry<Integer, Integer> pair : pairs.entrySet()) {
            Integer p1 = pair.getKey();
            Integer p2 = pair.getValue();
            tickLog.id = p1;
            tickLog.counterpartId = p2;
            tickLog.currentStrategy = lastStrategies.get(p1);
            tickLog.targetStrategy = lastTargetStrategies.get(p1);
            tickLog.hoverStrategy_A = lastHoverStrategies_A.get(p1);
            tickLog.hoverStrategy_a = lastHoverStrategies_a.get(p1);
            tickLog.counterpartCurrentStrategy = lastStrategies.get(p2);
            tickLog.counterpartTargetStrategy = lastTargetStrategies.get(p2);
            tickLog.counterpartHoverStrategy_A = lastHoverStrategies_A.get(p2);
            tickLog.counterpartHoverStrategy_a = lastHoverStrategies_a.get(p2);
            if (tickLog.targetStrategy == null) {
                tickLog.targetStrategy = tickLog.currentStrategy;
            }
            if (tickLog.counterpartTargetStrategy == null) {
                tickLog.counterpartTargetStrategy = tickLog.counterpartCurrentStrategy;
            }
            if (tickLog.hoverStrategy_A == null) {
                tickLog.hoverStrategy_A = new float[tickLog.currentStrategy.length];
                for (int i = 0; i < tickLog.hoverStrategy_A.length; i++) {
                    tickLog.hoverStrategy_A[i] = Float.NaN;
                }
            }
            if (tickLog.hoverStrategy_a == null) {
                tickLog.hoverStrategy_a = new float[tickLog.currentStrategy.length];
                for (int i = 0; i < tickLog.hoverStrategy_a.length; i++) {
                    tickLog.hoverStrategy_a[i] = Float.NaN;
                }
            }
            if (tickLog.counterpartHoverStrategy_A == null) {
                tickLog.counterpartHoverStrategy_A = new float[tickLog.currentStrategy.length];
                for (int i = 0; i < tickLog.counterpartHoverStrategy_A.length; i++) {
                    tickLog.counterpartHoverStrategy_A[i] = Float.NaN;
                }
            }
            if (tickLog.counterpartHoverStrategy_a == null) {
                tickLog.counterpartHoverStrategy_a = new float[tickLog.currentStrategy.length];
                for (int i = 0; i < tickLog.counterpartHoverStrategy_a.length; i++) {
                    tickLog.counterpartHoverStrategy_a[i] = Float.NaN;
                }
            }
            //tickLog.commit();
        }
    }
}
