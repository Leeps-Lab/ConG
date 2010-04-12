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
        while (partners.size() > 0) {
            ClientInterface client1 = partners.remove(0);
            ClientInterface client2 = partners.remove(0);
            pairs.put(client1, client2);
            pairs.put(client2, client1);
        }
        updateAllStrategies();
    }

    public void strategyChanged(Integer id, long timestamp, PeriodConfig periodConfig) {
        long periodTimeElapsed = timestamp - periodStartTime;
        float percent = periodTimeElapsed / (periodConfig.length * 1000f);
        float percentInStrategyTime;
        ClientInterface changed = members.get(id);
        ClientInterface other = pairs.get(changed);
        long inStrategyTime = System.currentTimeMillis() - lastEvalTimes.get(changed);
        percentInStrategyTime = inStrategyTime / (periodConfig.length * 1000f);
        lastEvalTimes.put(changed, timestamp);
        lastEvalTimes.put(other, timestamp);
        updatePayoffs(changed, other, percent, percentInStrategyTime, percentInStrategyTime, periodConfig);
    }

    private void updatePayoffs(
            ClientInterface client1, ClientInterface client2,
            float percent, float percentInStrategyTime, float inStrategyTime,
            PeriodConfig periodConfig) {
        float[] last1 = lastStrategies.get(client1);
        float[] last2 = lastStrategies.get(client2);
        float points1 = periodConfig.payoffFunction.getPayoff(
                percent, last1, last2);
        float points2 = periodConfig.payoffFunction.getPayoff(
                percent, last2, last1);
        if (!periodConfig.pointsPerSecond) {
            points1 *= percentInStrategyTime;
            points2 *= percentInStrategyTime;
        } else {
            points1 *= inStrategyTime / 1000f;
            points2 *= inStrategyTime / 1000f;
        }
        client1.addToPeriodPoints(points1);
        client2.addToPeriodPoints(points2);
        updateStrategies(client1, client2);
    }

    private void updateStrategies(ClientInterface client1, ClientInterface client2) {
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
                updateStrategies(client, pairs.get(client));
                notified.add(client);
                notified.add(pairs.get(client));
            }
        }
    }
}
