package edu.ucsc.leeps.fire.cong.client;

import edu.ucsc.leeps.fire.cong.FIRE;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class State {

    public int id;
    public int subperiod;
    public volatile float currentPercent;
    public Map<Integer, float[]> strategies, matchStrategies;
    public final List<Strategy> strategiesTime;
    public volatile float subperiodPayoff, subperiodMatchPayoff;
    public float[] target;
    public StrategyChanger strategyChanger;

    public State(StrategyChanger changer) {
        this.strategyChanger = changer;
        strategiesTime = new LinkedList<Strategy>();
        strategies = new HashMap<Integer, float[]>();
        matchStrategies = new HashMap<Integer, float[]>();
    }

    public void startPeriod() {
        subperiod = 0;
        currentPercent = 0;
        synchronized (strategiesTime) {
            strategiesTime.clear();
        }
        strategies.clear();
        matchStrategies.clear();
        setMyStrategy(FIRE.client.getConfig().initialStrategy);
    }

    public void endPeriod() {
        currentPercent = 1f;
    }

    public void setMyStrategy(float[] strategy) {
        float[] s = new float[strategy.length];
        System.arraycopy(strategy, 0, s, 0, s.length);
        strategies.put(id, s);
        target = new float[strategy.length];
        System.arraycopy(strategy, 0, target, 0, target.length);
    }

    public float[] getMyStrategy() {
        return strategies.get(id);
    }

    public void setStrategies(int whoChanged, Map<Integer, float[]> strategies, long timestamp) {
        synchronized (strategiesTime) {
            strategiesTime.add(new Strategy(timestamp, copyMap(strategies), copyMap(matchStrategies)));
        }
        this.strategies = strategies;
    }

    public void setMatchStrategies(int whoChanged, Map<Integer, float[]> matchStrategies, long timestamp) {
        synchronized (strategiesTime) {
            strategiesTime.add(new Strategy(timestamp, copyMap(strategies), copyMap(matchStrategies)));
        }
        this.matchStrategies = matchStrategies;
    }

    public Map<Integer, float[]> getFictitiousStrategies(int id, float[] strategy) {
        Map<Integer, float[]> fake = new HashMap<Integer, float[]>();
        for (int i : strategies.keySet()) {
            if (i == id) {
                fake.put(i, strategy);
            } else {
                fake.put(i, strategies.get(i));
            }
        }
        return fake;
    }

    public Map<Integer, float[]> getFictitiousStrategies(float[] strategy) {
        Map<Integer, float[]> fake = new HashMap<Integer, float[]>();
        for (int i : strategies.keySet()) {
            if (i == id) {
                fake.put(i, strategy);
            } else {
                fake.put(i, strategies.get(i));
            }
        }
        return fake;
    }

    public Map<Integer, float[]> getFictitiousMatchStrategies(float[] matchStrategy) {
        Map<Integer, float[]> fake = new HashMap<Integer, float[]>();
        for (int i : strategies.keySet()) {
            fake.put(i, matchStrategy);
        }
        return fake;
    }

    public static class Strategy {

        public final long timestamp;
        public final Map<Integer, float[]> strategies;
        public final Map<Integer, float[]> matchStrategies;

        public Strategy(long timestamp, Map<Integer, float[]> strategies, Map<Integer, float[]> matchStrategies) {
            this.timestamp = timestamp;
            this.strategies = strategies;
            this.matchStrategies = matchStrategies;
        }
    }

    public static Map<Integer, float[]> copyMap(Map<Integer, float[]> m) {
        Map<Integer, float[]> copy = new HashMap<Integer, float[]>();
        for (int id : m.keySet()) {
            float[] f = new float[m.get(id).length];
            System.arraycopy(m.get(id), 0, f, 0, f.length);
            copy.put(id, f);
        }
        return copy;
    }
}
