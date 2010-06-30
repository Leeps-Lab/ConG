package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.logging.StrategyChangeEvent;
import edu.ucsc.leeps.fire.cong.logging.TickEvent;
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
public class PairedPopulation implements Population {

    private Map<Integer, ClientInterface> members;
    private long periodStartTime;
    private Set<Integer> counterparts;
    private Map<Integer, Integer> pairs;
    private Map<Integer, Long> lastEvalTimes;
    private Map<Integer, float[]> lastStrategies;
    private Map<Integer, float[]> lastTargetStrategies;
    private Map<Integer, float[]> lastHoverStrategies_A;
    private Map<Integer, float[]> lastHoverStrategies_a;

    public PairedPopulation() {
        lastEvalTimes = new HashMap<Integer, Long>();
        lastStrategies = new HashMap<Integer, float[]>();
        lastTargetStrategies = new HashMap<Integer, float[]>();
        lastHoverStrategies_A = new HashMap<Integer, float[]>();
        lastHoverStrategies_a = new HashMap<Integer, float[]>();
        pairs = new HashMap<Integer, Integer>();
        counterparts = new HashSet<Integer>();
    }

    public void setMembers(
            Map<Integer, ClientInterface> members,
            Map<Integer, Population> membership) {
        this.members = members;
        for (Integer id : members.keySet()) {
            membership.put(id, this);
        }
        lastEvalTimes.clear();
        lastStrategies.clear();
        lastTargetStrategies.clear();
        lastHoverStrategies_A.clear();
        lastHoverStrategies_a.clear();
        pairs.clear();
        counterparts.clear();
        List<Integer> partners = new ArrayList<Integer>();
        partners.addAll(members.keySet());
        Collections.shuffle(partners);
        if (partners.size() % 2 != 0) {
            System.err.println("Error while making pairs, odd number of subjects to pair up");
        }
        while (partners.size() > 0) {
            Integer client1 = partners.remove(0);
            Integer client2 = partners.remove(0);
            pairs.put(client1, client2);
            pairs.put(client2, client1);
            FIRE.server.getConfig(client1).isCounterpart = false;
            FIRE.server.getConfig(client1).payoffFunction = FIRE.server.getConfig().payoffFunction;
            FIRE.server.getConfig(client1).counterpartPayoffFunction = FIRE.server.getConfig().counterpartPayoffFunction;
            FIRE.server.getConfig(client2).isCounterpart = true;
            FIRE.server.getConfig(client1).payoffFunction = FIRE.server.getConfig().counterpartPayoffFunction;
            FIRE.server.getConfig(client1).counterpartPayoffFunction = FIRE.server.getConfig().payoffFunction;
            counterparts.add(client2);
        }
    }

    public void initialize(long timestamp) {
        periodStartTime = timestamp;
        for (Integer id : members.keySet()) {
            lastEvalTimes.put(id, timestamp);
        }
        updateAllStrategies();
    }

    public void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            float[] hoverStrategy_A,
            float[] hoverStrategy_a,
            Integer changed, long timestamp) {
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (FIRE.server.getConfig().length * 1000f);
        float percentInStrategyTime;
        int other = pairs.get(changed);
        long inStrategyTime = timestamp - lastEvalTimes.get(changed);
        percentInStrategyTime = inStrategyTime / (FIRE.server.getConfig().length * 1000f);
        lastEvalTimes.put(changed, timestamp);
        lastEvalTimes.put(other, timestamp);
        updatePayoffs(
                newStrategy,
                changed, other,
                percent, percentInStrategyTime, percentInStrategyTime);
        // log the event
        StrategyChangeEvent event = new StrategyChangeEvent();
        event.changedId = changed;
        event.counterpartId = other;
        event.currentStrategy = newStrategy;
        event.targetStrategy = targetStrategy;
        event.hoverStrategy_A = hoverStrategy_A;
        event.hoverStrategy_a = hoverStrategy_a;
        event.counterpartCurrentStrategy = lastStrategies.get(other);
        event.counterpartTargetStrategy = lastTargetStrategies.get(other);
        event.counterpartHoverStrategy_A = lastHoverStrategies_A.get(other);
        event.counterpartHoverStrategy_a = lastHoverStrategies_a.get(other);
        if (counterparts.contains(changed)) {
            event.payoffFunction = FIRE.server.getConfig().counterpartPayoffFunction;
            event.counterpartPayoffFunction = FIRE.server.getConfig().payoffFunction;
        } else {
            event.payoffFunction = FIRE.server.getConfig().payoffFunction;
            event.counterpartPayoffFunction = FIRE.server.getConfig().counterpartPayoffFunction;
        }
        if (event.targetStrategy == null) {
            event.targetStrategy = event.currentStrategy;
        }
        if (event.counterpartTargetStrategy == null) {
            event.counterpartTargetStrategy = event.counterpartCurrentStrategy;
        }
        if (event.hoverStrategy_A == null) {
            event.hoverStrategy_A = new float[event.currentStrategy.length];
            for (int i = 0; i < event.hoverStrategy_A.length; i++) {
                event.hoverStrategy_A[i] = Float.NaN;
            }
        }
        if (event.hoverStrategy_a == null) {
            event.hoverStrategy_a = new float[event.currentStrategy.length];
            for (int i = 0; i < event.hoverStrategy_a.length; i++) {
                event.hoverStrategy_a[i] = Float.NaN;
            }
        }
        if (event.counterpartHoverStrategy_A == null) {
            event.counterpartHoverStrategy_A = new float[event.currentStrategy.length];
            for (int i = 0; i < event.counterpartHoverStrategy_A.length; i++) {
                event.counterpartHoverStrategy_A[i] = Float.NaN;
            }
        }
        if (event.counterpartHoverStrategy_a == null) {
            event.counterpartHoverStrategy_a = new float[event.currentStrategy.length];
            for (int i = 0; i < event.counterpartHoverStrategy_a.length; i++) {
                event.counterpartHoverStrategy_a[i] = Float.NaN;
            }
        }
        FIRE.server.commit(event);
        // save the strategies
        lastTargetStrategies.put(changed, targetStrategy);
        lastHoverStrategies_A.put(changed, hoverStrategy_A);
        lastHoverStrategies_a.put(changed, hoverStrategy_a);
    }

    private void updatePayoffs(
            float[] newStrategy,
            int changed, int other,
            float percent, float percentInStrategyTime, float inStrategyTime) {
        PayoffFunction changedPayoff, otherPayoff;
        if (counterparts.contains(changed)) {
            changedPayoff = FIRE.server.getConfig().counterpartPayoffFunction;
            otherPayoff = FIRE.server.getConfig().payoffFunction;
        } else {
            changedPayoff = FIRE.server.getConfig().payoffFunction;
            otherPayoff = FIRE.server.getConfig().counterpartPayoffFunction;
        }
        float[] changedLast = lastStrategies.get(changed);
        float[] otherLast = lastStrategies.get(other);
        float changedPoints = changedPayoff.getPayoff(
                percent, changedLast, otherLast);
        float otherPoints = otherPayoff.getPayoff(
                percent, otherLast, changedLast);
        if (!FIRE.server.getConfig().pointsPerSecond) {
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

    public void endSubperiod(int subperiod) {
        long timestamp = System.currentTimeMillis();
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (FIRE.server.getConfig().length * 1000f);
        float subperiodPercent = 1f / FIRE.server.getConfig().subperiods;
        updateAllStrategies();
        for (Integer client1 : counterparts) {
            Integer client2 = pairs.get(client1);
            updatePayoffs(
                    lastStrategies.get(client1),
                    client1, client2,
                    percent, subperiodPercent, subperiodPercent);
        }
        for (Entry<Integer, ClientInterface> entry : members.entrySet()) {
            float[] subperiodStrategy = lastStrategies.get(entry.getKey());
            float[] counterpartSubperiodStrategy = lastStrategies.get(pairs.get(entry.getKey()));
            entry.getValue().endSubperiod(subperiod, subperiodStrategy, counterpartSubperiodStrategy);
        }
    }

    public void endPeriod() {
        float percentInStrategyTime;
        for (Integer client1 : counterparts) {
            Integer client2 = pairs.get(client1);
            long lastEvalTime1 = lastEvalTimes.get(client1);
            long lastEvalTime2 = lastEvalTimes.get(client2);
            long lastEvalTime = Math.max(lastEvalTime1, lastEvalTime2);
            long inStrategyTime = (periodStartTime + (FIRE.server.getConfig().length * 1000)) - lastEvalTime;
            percentInStrategyTime = inStrategyTime / (FIRE.server.getConfig().length * 1000f);
            updatePayoffs(
                    lastStrategies.get(client1),
                    client1, client2,
                    1.0f, percentInStrategyTime, percentInStrategyTime);
        }
    }

    public void logTick() {
        TickEvent event = new TickEvent();
        for (Entry<Integer, Integer> pair : pairs.entrySet()) {
            Integer p1 = pair.getKey();
            Integer p2 = pair.getValue();
            event.id = p1;
            event.counterpartId = p2;
            event.currentStrategy = lastStrategies.get(p1);
            event.targetStrategy = lastTargetStrategies.get(p1);
            event.hoverStrategy_A = lastHoverStrategies_A.get(p1);
            event.hoverStrategy_a = lastHoverStrategies_a.get(p1);
            event.counterpartCurrentStrategy = lastStrategies.get(p2);
            event.counterpartTargetStrategy = lastTargetStrategies.get(p2);
            event.counterpartHoverStrategy_A = lastHoverStrategies_A.get(p2);
            event.counterpartHoverStrategy_a = lastHoverStrategies_a.get(p2);
            if (event.targetStrategy == null) {
                event.targetStrategy = event.currentStrategy;
            }
            if (event.counterpartTargetStrategy == null) {
                event.counterpartTargetStrategy = event.counterpartCurrentStrategy;
            }
            if (event.hoverStrategy_A == null) {
                event.hoverStrategy_A = new float[event.currentStrategy.length];
                for (int i = 0; i < event.hoverStrategy_A.length; i++) {
                    event.hoverStrategy_A[i] = Float.NaN;
                }
            }
            if (event.hoverStrategy_a == null) {
                event.hoverStrategy_a = new float[event.currentStrategy.length];
                for (int i = 0; i < event.hoverStrategy_a.length; i++) {
                    event.hoverStrategy_a[i] = Float.NaN;
                }
            }
            if (event.counterpartHoverStrategy_A == null) {
                event.counterpartHoverStrategy_A = new float[event.currentStrategy.length];
                for (int i = 0; i < event.counterpartHoverStrategy_A.length; i++) {
                    event.counterpartHoverStrategy_A[i] = Float.NaN;
                }
            }
            if (event.counterpartHoverStrategy_a == null) {
                event.counterpartHoverStrategy_a = new float[event.currentStrategy.length];
                for (int i = 0; i < event.counterpartHoverStrategy_a.length; i++) {
                    event.counterpartHoverStrategy_a[i] = Float.NaN;
                }
            }
            FIRE.server.commit(event);
        }
    }
}
