package edu.ucsc.leeps.fire.cong.server;

import edu.ucsc.leeps.fire.cong.config.Config;
import java.util.Map;

/**
 *
 * @author jpettit
 */
public class QWERTYPayoffFunction extends TwoStrategyPayoffFunction {

    public float[][][] payoffs = new float[][][]{
        { // Platform A payoffs
            {0, 6, 6},
            {0, 10, 7},
            {0, 13, 12},},
        { // Platform B payoffs
            {0, 3, 3},
            {0, 9, 6},
            {0, 12, 11},}
    };

    @Override
    public float getMin() {
        return 3;
    }

    @Override
    public float getMax() {
        return 13;
    }

    @Override
    public float getPayoff(
            int id, float percent,
            Map<Integer, float[]> popStrategies, Map<Integer, float[]> matchPopStrategies,
            Config config) {
        float[] strategy = popStrategies.get(id);
        float[][] platformPayoffs = payoffs[(int) strategy[0]];
        int numSameType = getInSame(id, strategy, popStrategies);
        int numDiffType = getInSame(id, strategy, matchPopStrategies);
        return platformPayoffs[numDiffType][numSameType];
    }

    public int getInSame(int id, float[] myStrategy, Map<Integer, float[]> strategies) {
        // the number of players with strategy equal to yours
        int count = 0;
        for (float[] strategy : strategies.values()) {
            boolean same = true;
            for (int i = 0; i < strategy.length; i++) {
                if (myStrategy[i] != strategy[i]) {
                    same = false;
                    break;
                }
            }
            if (same) {
                count++;
            }
        }
        return count;
    }

    public int getNotInSame(int id, float[] myStrategy, Map<Integer, float[]> strategies) {
        // the number of players with strategy not equal to yours
        int count = 0;
        for (float[] strategy : strategies.values()) {
            boolean same = true;
            for (int i = 0; i < strategy.length; i++) {
                if (myStrategy[i] != strategy[i]) {
                    same = false;
                    break;
                }
            }
            if (!same) {
                count++;
            }
        }
        return count;
    }
}
