package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientState;
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
import java.util.Set;

/**
 *
 * @author jpettit
 */
public class PairedPopulation implements Population, Serializable {

    private Map<Integer, ClientState> members;
    private Map<ClientState, Integer> ids;
    private long periodStartTime;
    private Set<ClientState> counterparts;
    private Map<ClientState, ClientState> pairs;
    private Map<ClientState, Long> lastEvalTimes;
    private Map<ClientState, float[]> lastStrategies;
    private Map<ClientState, float[]> lastTargetStrategies;
    private Map<ClientState, float[]> lastHoverStrategies_A;
    private Map<ClientState, float[]> lastHoverStrategies_a;

    public void setMembers(
            List<ClientState> members,
            Map<Integer, Population> membership) {
        this.members = new HashMap<Integer, ClientState>();
        this.ids = new HashMap<ClientState, Integer>();
        for (ClientState client : members) {
            int id = client.getID();
            this.members.put(id, client);
            this.ids.put(client, id);
            membership.put(client.getID(), this);
        }
    }

    public void initialize(long timestamp, PeriodConfig periodConfig) {
        periodStartTime = timestamp;
        lastEvalTimes = new HashMap<ClientState, Long>();
        for (ClientState client : members.values()) {
            lastEvalTimes.put(client, timestamp);
        }
        lastStrategies = new HashMap<ClientState, float[]>();
        lastTargetStrategies = new HashMap<ClientState, float[]>();
        lastHoverStrategies_A = new HashMap<ClientState, float[]>();
        lastHoverStrategies_a = new HashMap<ClientState, float[]>();
        pairs = new HashMap<ClientState, ClientState>();
        List<ClientState> partners = new ArrayList<ClientState>();
        partners.addAll(members.values());
        Collections.shuffle(partners);
        if (partners.size() % 2 != 0) {
            System.err.println("Error while making pairs, odd number of subjects to pair up");
        }
        this.counterparts = new HashSet<ClientState>();
        while (partners.size() > 0) {
            ClientState client1 = partners.remove(0);
            ClientState client2 = partners.remove(0);
            pairs.put(client1, client2);
            pairs.put(client2, client1);
            client1.client.setIsCounterpart(false);
            client2.client.setIsCounterpart(true);
            counterparts.add(client2);
        }
        updateAllStrategies();
    }

    public void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            float[] hoverStrategy_A,
            float[] hoverStrategy_a,
            Integer id, long timestamp,
            PeriodConfig periodConfig,
            EventLog eventLog) {
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (periodConfig.length * 1000f);
        float percentInStrategyTime;
        ClientState changed = members.get(id);
        ClientState other = pairs.get(changed);
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
        eventLog.changedId = id;
        eventLog.counterpartId = ids.get(other);
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
            ClientState changed, ClientState other,
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
        changed.addToPeriodPoints(changedPoints);
        other.addToPeriodPoints(otherPoints);
        updateStrategiesFast(newStrategy, changed, other);
    }

    private void updateStrategiesFast(
            float[] newStrategy, ClientState changed, ClientState other) {
        float[] c1s = newStrategy;
        other.client.setCounterpartStrategy(c1s);
        lastStrategies.put(changed, c1s);
    }

    private void updateStrategiesSlow(ClientState client1, ClientState client2) {
        float[] c1s = client1.client.getStrategy();
        float[] c2s = client2.client.getStrategy();
        client1.client.setCounterpartStrategy(c2s);
        client2.client.setCounterpartStrategy(c1s);
        lastStrategies.put(client1, c1s);
        lastStrategies.put(client2, c2s);
    }

    private void updateAllStrategies() {
        Set<ClientState> notified = new HashSet<ClientState>();
        for (ClientState client : members.values()) {
            if (!notified.contains(client)) {
                updateStrategiesSlow(client, pairs.get(client));
                notified.add(client);
                notified.add(pairs.get(client));
            }
        }
    }

    public void endPeriod(PeriodConfig periodConfig) {
        float percentInStrategyTime;
        for (ClientState client1 : counterparts) {
            ClientState client2 = pairs.get(client1);
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
        for (Map.Entry<ClientState, ClientState> pair : pairs.entrySet()) {
            ClientState p1 = pair.getKey();
            ClientState p2 = pair.getValue();
            tickLog.id = ids.get(p1);
            tickLog.counterpartId = ids.get(p2);
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
