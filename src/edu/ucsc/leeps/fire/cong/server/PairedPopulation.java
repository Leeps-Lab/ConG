/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

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
import java.util.Set;

/**
 *
 * @author jpettit
 */
public class PairedPopulation implements Population, Serializable {

    private Map<Integer, ClientInterface> members;
    private Map<ClientInterface, Integer> ids;
    private long periodStartTime;
    private Set<ClientInterface> counterparts;
    private Map<ClientInterface, ClientInterface> pairs;
    private Map<ClientInterface, Long> lastEvalTimes;
    private Map<ClientInterface, float[]> lastStrategies;
    private Map<ClientInterface, float[]> lastTargetStrategies;
    private Map<ClientInterface, float[]> lastHoverStrategies_A;
    private Map<ClientInterface, float[]> lastHoverStrategies_a;

    public void setMembers(
            List<ClientInterface> members,
            Map<Integer, Population> membership) {
        this.members = new HashMap<Integer, ClientInterface>();
        this.ids = new HashMap<ClientInterface, Integer>();
        for (ClientInterface client : members) {
            int id = client.getID();
            this.members.put(id, client);
            this.ids.put(client, id);
            membership.put(client.getID(), this);
        }
    }

    public void initialize(long timestamp, PeriodConfig periodConfig) {
        periodStartTime = timestamp;
        lastEvalTimes = new HashMap<ClientInterface, Long>();
        for (ClientInterface client : members.values()) {
            lastEvalTimes.put(client, timestamp);
        }
        lastStrategies = new HashMap<ClientInterface, float[]>();
        lastTargetStrategies = new HashMap<ClientInterface, float[]>();
        lastHoverStrategies_A = new HashMap<ClientInterface, float[]>();
        lastHoverStrategies_a = new HashMap<ClientInterface, float[]>();
        pairs = new HashMap<ClientInterface, ClientInterface>();
        List<ClientInterface> partners = new ArrayList<ClientInterface>();
        partners.addAll(members.values());
        Collections.shuffle(partners);
        if (partners.size() % 2 != 0) {
            System.err.println("Error while making pairs, odd number of subjects to pair up");
        }
        this.counterparts = new HashSet<ClientInterface>();
        while (partners.size() > 0) {
            ClientInterface client1 = partners.remove(0);
            ClientInterface client2 = partners.remove(0);
            pairs.put(client1, client2);
            pairs.put(client2, client1);
            client1.setIsCounterpart(false);
            client2.setIsCounterpart(true);
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
        ClientInterface changed = members.get(id);
        ClientInterface other = pairs.get(changed);
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
        eventLog.commit();
        // save the strategies
        lastTargetStrategies.put(changed, targetStrategy);
        lastHoverStrategies_A.put(changed, hoverStrategy_A);
        lastHoverStrategies_a.put(changed, hoverStrategy_a);
    }

    private void updatePayoffs(
            float[] newStrategy,
            ClientInterface changed, ClientInterface other,
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
            float[] newStrategy, ClientInterface changed, ClientInterface other) {
        float[] c1s = newStrategy;
        other.setCounterpartStrategy(c1s);
        lastStrategies.put(changed, c1s);
    }

    private void updateStrategiesSlow(ClientInterface client1, ClientInterface client2) {
        float[] c1s = client1.getStrategy();
        float[] c2s = client2.getStrategy();
        client1.setCounterpartStrategy(c2s);
        client2.setCounterpartStrategy(c1s);
        lastStrategies.put(client1, c1s);
        lastStrategies.put(client2, c2s);
    }

    private void updateAllStrategies() {
        Set<ClientInterface> notified = new HashSet<ClientInterface>();
        for (ClientInterface client : members.values()) {
            if (!notified.contains(client)) {
                updateStrategiesSlow(client, pairs.get(client));
                notified.add(client);
                notified.add(pairs.get(client));
            }
        }
    }

    public void endPeriod(PeriodConfig periodConfig) {
        float percentInStrategyTime;
        for (ClientInterface client1 : counterparts) {
            ClientInterface client2 = pairs.get(client1);
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
        for (Map.Entry<ClientInterface, ClientInterface> pair : pairs.entrySet()) {
            ClientInterface p1 = pair.getKey();
            ClientInterface p2 = pair.getValue();
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
            tickLog.commit();
        }
    }
}
