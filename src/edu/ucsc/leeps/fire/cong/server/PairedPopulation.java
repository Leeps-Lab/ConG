/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.config.PeriodConfig;
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
    private long periodStartTime;
    private Set<ClientInterface> counterparts;
    private Map<ClientInterface, ClientInterface> pairs;
    private Map<ClientInterface, Long> lastEvalTimes;
    private Map<ClientInterface, float[]> lastStrategies;

    public void setMembers(
            List<ClientInterface> members,
            List<Population> populations,
            Map<Integer, Population> membership) {
        this.members = new HashMap<Integer, ClientInterface>();
        for (ClientInterface client : members) {
            this.members.put(client.getID(), client);
            membership.put(client.getID(), this);
        }
        populations.add(this);
    }

    public void initialize(long timestamp, PeriodConfig periodConfig) {
        periodStartTime = timestamp;
        lastEvalTimes = new HashMap<ClientInterface, Long>();
        for (ClientInterface client : members.values()) {
            lastEvalTimes.put(client, timestamp);
        }
        lastStrategies = new HashMap<ClientInterface, float[]>();
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

    public void strategyChanged(float[] newStrategy, Integer id, long timestamp, PeriodConfig periodConfig) {
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
}
