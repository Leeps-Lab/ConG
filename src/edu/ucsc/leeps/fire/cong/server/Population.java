package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.FIRE;
import edu.ucsc.leeps.fire.cong.client.ClientInterface;
import edu.ucsc.leeps.fire.cong.config.Config;
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
public class Population implements Serializable {

    private Map<Integer, ClientInterface> members;
    private long periodStartTime;
    private Set<Tuple> tuples;
    private Map<Integer, Tuple> tupleMap;

    public Population() {
        tuples = new HashSet<Tuple>();
        tupleMap = new HashMap<Integer, Tuple>();
    }

    public void configure(Map<Integer, ClientInterface> members) {
        this.members = members;
        tuples.clear();
        tupleMap.clear();
        setupTuples();
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
        for (Tuple tuple : tuples) {
            tuple.endSubperiod(subperiod);
        }
    }

    public void endPeriod() {
        long timestamp = System.currentTimeMillis();
        for (Tuple tuple : tuples) {
            tuple.endPeriod(timestamp);
        }
    }

    public void logTick(int subperiod, int millisLeft) {
    }

    private class Tuple {

        public Set<Integer> members;
        public long evalTime;
        public float[] strategy;
        public Map<Integer, float[]> strategyExclude;
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
            strategyExclude = new HashMap<Integer, float[]>();
        }

        public void update(int changed, float[] strategy, long timestamp) {
            if (FIRE.server.getConfig().subperiods == 0) {
                evaluate(timestamp);
                match.evaluate(timestamp);
            }
            strategies.put(changed, strategy);
            mergeStrategies();

        }

        public void mergeStrategies() {
            if (this == this.match && FIRE.server.getConfig().excludeSelf) {
                for (int member : members) {
                    mergeStrategies(member);
                }
            } else {
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
        }

        public void mergeStrategies(int exclude) {
            float[] s = new float[strategy.length];
            for (int i = 0; i < s.length; i++) {
                s[i] = 0;
            }
            for (int member : members) {
                if (member != exclude) {
                    float[] s1 = strategies.get(member);
                    for (int i = 0; i < s.length; i++) {
                        s[i] += s1[i];
                    }
                }
            }
            for (int i = 0; i < s.length; i++) {
                s[i] /= (members.size() - 1);
            }
            strategyExclude.put(exclude, s);
        }

        public void evaluate(long timestamp) {
            float percent = (timestamp - periodStartTime) / (FIRE.server.getConfig().length * 1000f);
            float percentElapsed = (timestamp - evalTime) / (FIRE.server.getConfig().length * 1000f);
            evaluate(percent, percentElapsed);
            evalTime = timestamp;
        }

        public void evaluate(float percent, float percentElapsed) {
            for (int member : members) {
                PayoffFunction u = FIRE.server.getConfig(member).payoffFunction;
                float[] otherStrategy;
                if (this == this.match && FIRE.server.getConfig().excludeSelf) {
                    otherStrategy = strategyExclude.get(member);
                } else {
                    otherStrategy = match.strategy;
                }
                float payoff = u.getPayoff(percent, strategies.get(member), otherStrategy);
                payoff *= percentElapsed;
                Population.this.members.get(member).setCounterpartStrategy(otherStrategy);
                FIRE.server.addToPeriodPoints(member, payoff);
            }
        }

        public void endSubperiod(int subperiod) {
            float percentElapsed = 1f / FIRE.server.getConfig().subperiods;
            float percent = subperiod * percentElapsed;
            evaluate(percent, percentElapsed);
            match.evaluate(percent, percentElapsed);
            for (int member : members) {
                float[] otherStrategy;
                if (this == this.match && FIRE.server.getConfig().excludeSelf) {
                    otherStrategy = strategyExclude.get(member);
                } else {
                    otherStrategy = match.strategy;
                }
                Population.this.members.get(member).endSubperiod(subperiod, strategies.get(member), otherStrategy);
            }
            mergeStrategies();

        }

        public void endPeriod(long timestamp) {
            evaluate(timestamp);
            match.evaluate(timestamp);
        }
    }

    public void setupTuples() {
        if (FIRE.server.getConfig().numTuples == 1
                || (members.size() / FIRE.server.getConfig().tupleSize) == 1) {
            setupSinglePopTuples();
        } else {
            ArrayList<Integer> randomMembers = new ArrayList<Integer>();
            randomMembers.addAll(members.keySet());
            Collections.shuffle(randomMembers, FIRE.server.getRandom());
            if (FIRE.server.getConfig().tupleSize == -1) {
                FIRE.server.getConfig().tupleSize = members.size() / FIRE.server.getConfig().numTuples;
            }
            Tuple current = null;
            ArrayList<Tuple> randomTuples = new ArrayList<Tuple>();
            while (randomMembers.size() > 0) {
                if (current == null || current.members.size() == FIRE.server.getConfig().tupleSize) {
                    current = new Tuple();
                    randomTuples.add(current);
                }
                int member = randomMembers.remove(0);
                current.members.add(member);
                tupleMap.put(member, current);
            }
            Collections.shuffle(randomTuples, FIRE.server.getRandom());
            while (randomTuples.size() > 0) {
                Tuple tuple = randomTuples.remove(0);
                tuple.match = randomTuples.remove(0);
                tuple.match.match = tuple;
                Config def = FIRE.server.getConfig();
                Tuple tuple1;
                if (FIRE.server.getRandom().nextBoolean()) {
                    tuple1 = tuple;
                } else {
                    tuple1 = tuple.match;
                }
                for (int member : tuple1.members) {
                    Config config = FIRE.server.getConfig(member);
                    config.isCounterpart = false;
                    config.payoffFunction = def.payoffFunction;
                    config.counterpartPayoffFunction = def.counterpartPayoffFunction;
                }
                for (int member : tuple1.match.members) {
                    Config config = FIRE.server.getConfig(member);
                    config.isCounterpart = true;
                    config.payoffFunction = def.counterpartPayoffFunction;
                    config.counterpartPayoffFunction = def.payoffFunction;
                }
            }
            assert (randomMembers.size() == 0);
            assert (randomTuples.size() == 0);
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
