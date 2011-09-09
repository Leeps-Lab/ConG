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
    public final List<Strategy> strategiesTime, matchStrategiesTime;
    public volatile float subperiodPayoff, subperiodMatchPayoff;
    public float[] target;
    public StrategyChanger strategyChanger;

    public State(StrategyChanger changer) {
        this.strategyChanger = changer;
        strategiesTime = new LinkedList<Strategy>();
        matchStrategiesTime = new LinkedList<Strategy>();
    }

    public void startPeriod() {
        subperiod = 0;
        currentPercent = 0;
        synchronized (strategiesTime) {
            strategiesTime.clear();
        }
        synchronized (matchStrategiesTime) {
            matchStrategiesTime.clear();
        }
        strategies = new HashMap<Integer, float[]>();
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

        public Strategy(long timestamp, Map<Integer, float[]> strategies) {
            this.timestamp = timestamp;
            this.strategies = strategies;
        }
    }
}
