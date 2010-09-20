package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.config.Config;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author jpettit
 */
public class Population implements Serializable {

    public enum Configuration {

        Paired, SinglePopInclude, SinglePopExclude
    }
    private Configuration configuration;
    private Map<Integer, ClientInterface> members;
    private long periodStartTime;
    private Set<Tuple> tuples;
    private Map<Integer, Tuple> tupleMap;

    public Population() {
        tuples = new HashSet<Tuple>();
        tupleMap = new HashMap<Integer, Tuple>();
    }

    public void configure(Map<Integer, ClientInterface> members) {
        configuration = FIRE.server.getConfig().population;
        this.members = members;
        tuples.clear();
        tupleMap.clear();
        switch (configuration) {
            case Paired:
                setupPairedTuples();
                break;
            case SinglePopInclude:
            case SinglePopExclude:
                setupSinglePopTuples();
                break;
        }
        if (FIRE.server.getConfig().preLength == 0) {
            for (int client : members.keySet()) {
                float[] s;
                if (FIRE.server.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
                    s = new float[2];
                    s[0] = FIRE.server.getRandom().nextFloat();
                    s[1] = 1 - s[0];
                } else {
                    s = new float[3];
                    s[0] = FIRE.server.getRandom().nextFloat();
                    s[1] = (1 - s[0]) * FIRE.server.getRandom().nextFloat();
                    s[2] = 1 - s[0] - s[1];
                }
                FIRE.server.getConfig(client).initialStrategy = s;
            }
            for (Tuple tuple : tuples) {
                for (int member : tuple.members) {
                    tuple.strategies.put(member, FIRE.server.getConfig(member).initialStrategy);
                }
                tuple.mergeStrategies();
            }
        }
    }

    public void setPeriodStartTime(long timestamp) {
        periodStartTime = timestamp;
        for (Tuple tuple : tuples) {
            tuple.evalTime = timestamp;
        }
    }

    public void strategyChanged(
            float[] newStrategy,
            float[] targetStrategy,
            Integer changed, long timestamp) {
        tupleMap.get(changed).update(changed, newStrategy, timestamp);
    }

    public void endSubperiod(int subperiod) {
    }

    public void endPeriod() {
    }

    public void logTick(int subperiod, int millisLeft) {
    }

    private class Tuple {

        public Set<Integer> members;
        public long evalTime;
        public float[] strategy;
        public Map<Integer, float[]> strategies;
        public Tuple match;

        public Tuple() {
            tuples.add(this);
            members = new HashSet<Integer>();
            strategies = new HashMap<Integer, float[]>();
            if (FIRE.server.getConfig().payoffFunction instanceof TwoStrategyPayoffFunction) {
                strategy = new float[2];
            } else {
                strategy = new float[3];
            }
        }

        public void update(int changed, float[] strategy, long timestamp) {
            evaluate(timestamp);
            match.evaluate(timestamp);
            strategies.put(changed, strategy);
            mergeStrategies();
        }

        public void mergeStrategies() {
            for (int i = 0; i < strategy.length; i++) {
                strategy[i] = 0;
            }
            for (int member : members) {
                float[] s = strategies.get(member);
                for (int i = 0; i < strategy.length; i++) {
                    strategy[i] += s[i];
                }
            }
            for (int i = 0; i < strategy.length; i++) {
                strategy[i] /= members.size();
            }
        }

        public void evaluate(long timestamp) {
            float percent = (timestamp - periodStartTime) / (FIRE.server.getConfig().length * 1000f);
            float percentElapsed = (timestamp - evalTime) / (FIRE.server.getConfig().length * 1000f);
            for (int member : members) {
                PayoffFunction u = FIRE.server.getConfig(member).payoffFunction;
                float payoff = u.getPayoff(percent, strategies.get(member), match.strategy);
                payoff *= percentElapsed;
                Population.this.members.get(member).setCounterpartStrategy(match.strategy);
                FIRE.server.addToPeriodPoints(member, payoff);
            }
            evalTime = timestamp;
        }
    }

    /*
     * Constructs one tuple per subject
     * Links up tuples in pair formation
     */
    private void setupPairedTuples() {
        for (int member : members.keySet()) {
            Tuple tuple = new Tuple();
            tuple.members.add(member);
            tupleMap.put(member, tuple);
        }
        Tuple[] a = tuples.toArray(new Tuple[0]);
        for (Tuple tuple : tuples) {
            if (tuple.match == null) {
                int r = FIRE.server.getRandom().nextInt(a.length);
                while (tuple == a[r]) {
                    r = FIRE.server.getRandom().nextInt(a.length);
                }
                tuple.match = a[r];
                tuple.match.match = tuple;
                Config def = FIRE.server.getConfig();
                Config config1, config2;
                if (FIRE.server.getRandom().nextBoolean()) {
                    config1 = FIRE.server.getConfig(tuple.members.toArray(new Integer[0])[0]);
                    config2 = FIRE.server.getConfig(tuple.match.members.toArray(new Integer[0])[0]);
                } else {
                    config1 = FIRE.server.getConfig(tuple.match.members.toArray(new Integer[0])[0]);
                    config2 = FIRE.server.getConfig(tuple.members.toArray(new Integer[0])[0]);
                }
                config1.isCounterpart = false;
                config2.isCounterpart = true;
                config1.payoffFunction = def.payoffFunction;
                config2.payoffFunction = def.counterpartPayoffFunction;
                config1.counterpartPayoffFunction = def.counterpartPayoffFunction;
                config2.counterpartPayoffFunction = def.payoffFunction;
            }
        }
    }

    /*
     * Constructs a single tuple comprising all subjects
     * Tuple is linked to itself
     */
    private void setupSinglePopTuples() {
        Tuple tuple = new Tuple();
        tuple.members = members.keySet();
        tuple.match = tuple;
        for (int member : members.keySet()) {
            Config def = FIRE.server.getConfig();
            Config config = FIRE.server.getConfig(member);
            config.isCounterpart = false;
            config.payoffFunction = def.payoffFunction;
            config.counterpartPayoffFunction = def.payoffFunction;
            tupleMap.put(member, tuple);
        }
    }
}
